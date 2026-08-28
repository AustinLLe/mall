package com.example.e2e;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

abstract class BaseE2ETest {

    private static final Path ARTIFACT_DIR = Path.of(
            System.getProperty("e2e.artifactDir", "target/e2e-artifacts"));
    private static final Path SCREENSHOT_DIR = ARTIFACT_DIR.resolve("screenshots");
    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    protected WebDriver driver;
    protected WebDriverWait wait;

    @RegisterExtension
    final TestWatcher browserLifecycle = new TestWatcher() {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            saveScreenshot("FAILED-" + context.getRequiredTestMethod().getName());
            savePageSource("FAILED-" + context.getRequiredTestMethod().getName());
            closeBrowser();
        }

        @Override
        public void testSuccessful(ExtensionContext context) {
            closeBrowser();
        }

        @Override
        public void testAborted(ExtensionContext context, Throwable cause) {
            saveScreenshot("ABORTED-" + context.getRequiredTestMethod().getName());
            closeBrowser();
        }
    };

    private final String baseUrl = normalizeBaseUrl(
            System.getProperty("e2e.baseUrl", "http://127.0.0.1:5173"));
    private final int timeoutSeconds = Integer.parseInt(
            System.getProperty("e2e.timeoutSeconds", "20"));

    @BeforeEach
    void setUpBrowser() throws Exception {
        String remoteUrl = System.getProperty("e2e.remoteUrl", "").trim();
        boolean headless = Boolean.parseBoolean(System.getProperty("e2e.headless", "false"));

        if (!remoteUrl.isBlank()) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--window-size=1440,1000");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            if (headless) {
                options.addArguments("--headless=new");
            }
            RemoteWebDriver remoteDriver = new RemoteWebDriver(
                    URI.create(remoteUrl).toURL(), options);
            remoteDriver.setFileDetector(new LocalFileDetector());
            driver = remoteDriver;
        } else if ("chrome".equalsIgnoreCase(System.getProperty("e2e.browser", ""))) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--window-size=1440,1000");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-extensions");
            options.addArguments("--no-first-run");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--user-data-dir=" + Path.of(
                    System.getProperty("java.io.tmpdir"),
                    "newsecondmall-e2e-" + System.nanoTime()));
            String chromeBinary = System.getProperty("e2e.chromeBinary", "").trim();
            if (!chromeBinary.isBlank()) {
                options.setBinary(chromeBinary);
            }
            if (headless) {
                options.addArguments("--headless=new");
            }
            String chromeDriver = System.getProperty("e2e.chromeDriver", "").trim();
            ChromeDriverService.Builder serviceBuilder = new ChromeDriverService.Builder()
                    .withTimeout(Duration.ofSeconds(45));
            if (!chromeDriver.isBlank()) {
                serviceBuilder.usingDriverExecutable(Path.of(chromeDriver).toFile());
            }
            System.out.println("Starting ChromeDriver"
                    + (chromeDriver.isBlank() ? "" : " at " + chromeDriver)
                    + (chromeBinary.isBlank() ? "" : " binary=" + chromeBinary));
            driver = new ChromeDriver(serviceBuilder.build(), options);
            System.out.println("ChromeDriver ready");
        } else {
            EdgeOptions options = new EdgeOptions();
            options.addArguments("--inprivate");
            options.addArguments("--window-size=1440,1000");
            options.addArguments("--user-data-dir=" + Path.of(
                    System.getProperty("java.io.tmpdir"),
                    "newsecondmall-e2e-" + System.nanoTime()));
            if (headless) {
                options.addArguments("--headless=new");
            }
            driver = new EdgeDriver(options);
        }
        driver.manage().window().setSize(new Dimension(1440, 1000));
        wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    private void closeBrowser() {
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                driver = null;
            }
        }
    }

    protected Path saveScreenshot(String evidenceName) {
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            return null;
        }

        try {
            Files.createDirectories(SCREENSHOT_DIR);
            String safeName = evidenceName.replaceAll("[^a-zA-Z0-9._-]", "-");
            Path target = SCREENSHOT_DIR.resolve(
                    safeName + "-" + LocalDateTime.now().format(FILE_TIME) + ".png");
            Files.write(target, screenshotDriver.getScreenshotAs(OutputType.BYTES));
            return target;
        } catch (IOException | WebDriverException screenshotError) {
            System.err.println("Unable to save E2E screenshot: " + screenshotError.getMessage());
            return null;
        }
    }

    protected void savePageSource(String evidenceName) {
        if (driver == null) {
            return;
        }
        try {
            Files.createDirectories(ARTIFACT_DIR);
            String safeName = evidenceName.replaceAll("[^a-zA-Z0-9._-]", "-");
            Path target = ARTIFACT_DIR.resolve(
                    safeName + "-" + LocalDateTime.now().format(FILE_TIME) + ".html");
            Files.writeString(target, driver.getPageSource(), StandardCharsets.UTF_8);
        } catch (IOException | WebDriverException sourceError) {
            System.err.println("Unable to save E2E page source: " + sourceError.getMessage());
        }
    }

    protected WebDriverWait waitFor(Duration timeout) {
        return new WebDriverWait(driver, timeout);
    }

    protected void openPage(String route) {
        // Query string forces a full load. Hash-only changes lose to login's
        // delayed reLaunch, and cannot switchTab into a tabBar page.
        driver.get(baseUrl + "/?e2e=" + System.nanoTime() + "#/" + route);
    }

    protected void waitForUrlContains(String fragment) {
        wait.until(currentDriver -> {
            try {
                return currentDriver.getCurrentUrl().contains(fragment);
            } catch (WebDriverException transientNavigationError) {
                return false;
            }
        });
    }

    protected void setInputValue(WebElement input, String value) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];"
                        + "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));",
                input,
                value);
    }

    protected void clickUniElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new MouseEvent('click', {bubbles: true}));",
                element);
    }

    protected void loginWithPreset(int roleIndex, String username, String password) {
        loginWithCredentials(roleIndex, username, password, false);
    }

    protected void loginWithCredentials(int roleIndex, String username, String password) {
        loginWithCredentials(roleIndex, username, password, true);
    }

    private void loginWithCredentials(
            int roleIndex, String username, String password, boolean replacePreset) {
        openPage("pages/auth/login");

        var roles = wait.until(
                ExpectedConditions.numberOfElementsToBe(By.cssSelector(".role-card"), 3));
        clickUniElement(roles.get(roleIndex));

        var inputs = wait.until(
                ExpectedConditions.numberOfElementsToBe(By.cssSelector(".input input"), 2));
        if (replacePreset) {
            setInputValue(inputs.get(0), username);
            setInputValue(inputs.get(1), password);
        }
        wait.until(ExpectedConditions.attributeToBe(inputs.get(0), "value", username));
        wait.until(ExpectedConditions.attributeToBe(inputs.get(1), "value", password));

        clickUniElement(wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".submit"))));
        wait.until(currentDriver -> {
            try {
                Object token = ((JavascriptExecutor) currentDriver).executeScript(
                        "return window.localStorage.getItem('auth_token');");
                return token != null && !token.toString().isBlank();
            } catch (WebDriverException navigationInProgress) {
                return false;
            }
        });
        wait.until(currentDriver -> {
            try {
                return !currentDriver.getCurrentUrl().contains("/pages/auth/login");
            } catch (WebDriverException navigationInProgress) {
                return false;
            }
        });
    }

    private static String normalizeBaseUrl(String value) {
        String result = value == null ? "" : value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
