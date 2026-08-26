package com.example.e2e;

import java.io.IOException;
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
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

abstract class BaseE2ETest {

    private static final Path SCREENSHOT_DIR = Path.of("reports", "screenshots");
    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    protected WebDriver driver;
    protected WebDriverWait wait;

    @RegisterExtension
    final TestWatcher browserLifecycle = new TestWatcher() {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            saveScreenshot("FAILED-" + context.getRequiredTestMethod().getName());
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

    @BeforeEach
    void setUpBrowser() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--inprivate");
        options.addArguments("--window-size=1440,1000");

        if (Boolean.parseBoolean(System.getProperty("e2e.headless", "false"))) {
            options.addArguments("--headless=new");
        }

        driver = new EdgeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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

    protected void openPage(String route) {
        driver.get(baseUrl + "/#/" + route);
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
    }

    private static String normalizeBaseUrl(String value) {
        String result = value == null ? "" : value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
