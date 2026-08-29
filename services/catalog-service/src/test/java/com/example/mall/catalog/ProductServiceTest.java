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

    @Test
    void ownsAndReturnsItsProductData() {
        ProductService.ProductView created = service.create(
                new ProductService.CreateProduct(22, "二手显示器", new BigDecimal("620.00")));
        assertThat(service.find(created.productId()).sellerId()).isEqualTo(22);
        assertThat(service.list()).extracting(ProductService.ProductView::name).contains("二手显示器");
    }
}
