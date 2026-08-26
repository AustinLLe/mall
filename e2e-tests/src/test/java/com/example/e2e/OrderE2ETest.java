package com.example.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

class OrderE2ETest extends BaseE2ETest {

    @Test
    void buyerCanAddProductToCartAndCreateOrder() {
        loginWithPreset(0, "demo", "demo123");
        openPage("pages/home/home");

        List<WebElement> cards = wait.until(
                ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".goods-card"), 0));
        String productTitle = wait.until(currentDriver -> currentDriver
                .findElements(By.cssSelector(".goods-title"))
                .stream()
                .map(element -> element.getText().trim())
                .filter(title -> !title.isBlank())
                .findFirst()
                .orElse(null));
        clickUniElement(cards.get(0));

        waitForUrlContains("/pages/goods/detail");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), productTitle));
        clickUniElement(wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".bottom-action.cart"))));

        openPage("pages/cart/cart");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), productTitle));
        if (driver.findElements(By.cssSelector(".check.on")).isEmpty()) {
            clickUniElement(wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".check"))));
        }
        saveScreenshot("EV-E2E-CART-01");

        clickUniElement(wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".checkout"))));
        waitForUrlContains("/pages/order/confirm");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), productTitle));
        saveScreenshot("EV-E2E-ORDER-CONFIRM-01");

        clickUniElement(wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".pay"))));
        waitForUrlContains("/pages/order/pay-result");
        assertTrue(driver.getCurrentUrl().contains("/pages/order/pay-result"));
        saveScreenshot("EV-E2E-ORDER-CREATED-01");

        openPage("pages/order/list");
        WebElement reviewAction = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".actions .primary")));
        clickUniElement(reviewAction);
        waitForUrlContains("/pages/order/review");

        String reviewContent = "E2E交易评价-" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("MMddHHmmss"));
        WebElement reviewTextarea = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".textarea textarea")));
        setInputValue(reviewTextarea, reviewContent);
        clickUniElement(wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn"))));

        waitForUrlContains("/pages/order/list");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.tagName("body"), reviewContent));
        assertTrue(driver.getPageSource().contains(reviewContent));
        saveScreenshot("EV-E2E-ORDER-REVIEW-01");
    }
}
