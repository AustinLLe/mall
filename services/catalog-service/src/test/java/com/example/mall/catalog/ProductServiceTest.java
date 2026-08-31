package com.example.mall.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:catalog;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password="})
class ProductServiceTest {
    @Autowired ProductService service;
    @Autowired StoreService stores;
    @Autowired ProductRelationService relations;

    @Test
    void ownsAndReturnsItsProductData() {
        ProductService.ProductView created = service.create(
                new ProductService.CreateProduct(22, "二手显示器", new BigDecimal("620.00")));
        assertThat(service.find(created.productId()).sellerId()).isEqualTo(22);
        assertThat(service.list()).extracting(ProductService.ProductView::name).contains("二手显示器");
    }

    @Test
    void sellerCanListAndUpdateOwnProductAndStoreCanBeQueried() {
        ProductService.ProductView created = service.create(
                new ProductService.CreateProduct(31, "旧标题", new BigDecimal("100.00")));

        ProductService.ProductView updated = service.update(created.productId(),
                new ProductService.UpdateProduct("新标题", "数码", "描述", "九成新", "故事",
                        new BigDecimal("88.00"), new BigDecimal("75.00"), "武汉", "/image.png"),
                new CatalogUser(31, "seller", "seller31"));

        assertThat(updated.name()).isEqualTo("新标题");
        assertThat(service.mine(31)).extracting(ProductService.ProductView::productId)
                .contains(created.productId());
        StoreService.StoreView store = stores.mine(31);
        assertThat(store.productCount()).isEqualTo(1);
        assertThat(stores.products(store.storeId())).extracting(ProductService.ProductView::name)
                .containsExactly("新标题");
    }

    @Test
    void sellerCannotUpdateAnotherSellersProduct() {
        ProductService.ProductView created = service.create(
                new ProductService.CreateProduct(41, "不可越权商品", new BigDecimal("50.00")));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.update(created.productId(),
                new ProductService.UpdateProduct("越权修改", "数码", null, null, null,
                        new BigDecimal("20.00"), null, null, null),
                new CatalogUser(42, "seller", "seller42")))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");
    }

    @Test
    void favoriteIsIdempotentAndHistoryCanBeCleared() {
        ProductService.ProductView created = service.create(
                new ProductService.CreateProduct(51, "关系测试商品", new BigDecimal("66.00")));

        relations.add("favorite", 501, created.productId());
        relations.add("favorite", 501, created.productId());
        relations.add("history", 501, created.productId());

        assertThat(relations.list("favorite", 501)).hasSize(1);
        assertThat(relations.list("history", 501)).hasSize(1);
        relations.clear("history", 501);
        assertThat(relations.list("history", 501)).isEmpty();
    }
}
