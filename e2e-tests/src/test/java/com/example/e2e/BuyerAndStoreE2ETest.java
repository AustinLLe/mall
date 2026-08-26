package com.example.e2e;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

class BuyerAndStoreE2ETest extends BaseE2ETest {

    @Test
    void buyerCanFavoriteProductAndSeeItInPersonalCenter() {
        loginWithPreset(0, "demo", "demo123");
        String title = openFirstProduct();

        clickUniElement(wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".side-secondary"))));
        openPage("pages/user/index");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "足迹收藏"));

        WebElement favoriteNav = wait.until(currentDriver -> currentDriver
                .findElements(By.cssSelector(".side-item"))
                .stream()
                .filter(item -> item.getText().contains("商品收藏"))
                .findFirst()
                .orElse(null));
        clickUniElement(favoriteNav);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), title));

        assertTrue(driver.getPageSource().contains(title));
        saveScreenshot("EV-E2E-BUYER-FAVORITE-01");
    }

    @Test
    void buyerCanOpenAndFollowStore() {
        loginWithPreset(0, "demo", "demo123");
        openFirstProduct();

        List<WebElement> storeButtons = wait.until(
                ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".store-btn"), 0));
        clickUniElement(storeButtons.get(0));
        waitForUrlContains("/pages/store/store");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "店铺介绍"));

        WebElement followButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".follow-btn")));
        if (followButton.getText().contains("已关注")) {
            clickUniElement(followButton);
            wait.until(ExpectedConditions.not(ExpectedConditions.attributeContains(
                    By.cssSelector(".follow-btn"), "class", "followed")));
            followButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector(".follow-btn")));
        }
        clickUniElement(followButton);
        wait.until(ExpectedConditions.attributeContains(
                By.cssSelector(".follow-btn"), "class", "followed"));

        assertTrue(driver.findElement(By.cssSelector(".follow-btn"))
                .getAttribute("class").contains("followed"));
        saveScreenshot("EV-E2E-STORE-FOLLOW-01");
    }

    private String openFirstProduct() {
        openPage("pages/home/home");
        List<WebElement> cards = wait.until(
                ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".goods-card"), 0));
        String title = wait.until(currentDriver -> currentDriver
                .findElements(By.cssSelector(".goods-title"))
                .stream()
                .map(element -> element.getText().trim())
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse(null));
        assertFalse(title.isBlank());
        clickUniElement(cards.get(0));
        waitForUrlContains("/pages/goods/detail");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), title));
        return title;
    }
}
