package com.example.shopping_back.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shopping_back.shop.mapper.ShopOrderMapper;
import com.example.shopping_back.shop.mapper.ShopProductMapper;
import com.example.shopping_back.shop.mapper.ShopStoreMapper;
import com.example.shopping_back.shop.mapper.ShopTopicMapper;
import com.example.shopping_back.shop.model.ProductRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopProductMapper productMapper;

    @Mock
    private ShopStoreMapper storeMapper;

    @Mock
    private ShopOrderMapper orderMapper;

    @Mock
    private ShopTopicMapper topicMapper;

    private ShopService shopService;
    private ProductRecord validProduct;

    @BeforeEach
    void setUp() {
        lenient().when(productMapper.countGoodsColumn(anyString())).thenReturn(1);
        lenient().when(productMapper.statusColumnLength()).thenReturn(20);
        lenient().when(storeMapper.countStoreColumn(anyString())).thenReturn(1);
        lenient().when(storeMapper.countFollowUniqueIndex()).thenReturn(1);
        lenient().when(topicMapper.countPostColumn(anyString())).thenReturn(1);

        shopService = new ShopService(
                productMapper,
                storeMapper,
                orderMapper,
                topicMapper,
                new ObjectMapper(),
                "",
                "gpt-4.1-mini",
                "https://api.openai.com/v1/responses",
                "",
                "qwen-plus",
                "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"
        );

        validProduct = createValidProductRecord();
    }

    @Test
    void createProductSuccess() {
        doAnswer(invocation -> {
            ProductRecord product = invocation.getArgument(0);
            product.setGoodsId(101);
            return 1;
        }).when(productMapper).insert(any(ProductRecord.class));
        when(productMapper.selectById(101)).thenReturn(validProduct);

        ShopDtos.PublishRequest request = createValidPublishRequest();
        ShopDtos.ProductView result = shopService.publish(request, null);

        assertNotNull(result);
        assertEquals("测试商品", result.title());
        verify(productMapper, times(1)).insert(any(ProductRecord.class));
    }

    @Test
    void createProductRejectsEmptyTitle() {
        ShopDtos.PublishRequest request = new ShopDtos.PublishRequest(
                "new",
                "",
                "/images/test.jpg",
                "数码",
                BigDecimal.valueOf(399),
                "9成新",
                "测试描述",
                "测试故事",
                BigDecimal.valueOf(320),
                "北京"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> shopService.publish(request, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void createProductRejectsNonPositivePrice() {
        ShopDtos.PublishRequest request = new ShopDtos.PublishRequest(
                "new",
                "测试商品",
                "/images/test.jpg",
                "数码",
                BigDecimal.ZERO,
                "9成新",
                "测试描述",
                "测试故事",
                BigDecimal.ZERO,
                "北京"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> shopService.publish(request, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void getProductListReturnsList() {
        when(productMapper.selectApproved("all", "")).thenReturn(List.of(validProduct));

        List<ShopDtos.ProductView> result = shopService.products("all", "");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试商品", result.get(0).title());
    }

    @Test
    void searchProductsWithKeyword() {
        when(productMapper.selectApproved("all", "测试")).thenReturn(List.of(validProduct));

        List<ShopDtos.ProductView> result = shopService.products("all", "测试");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试商品", result.get(0).title());
    }

    @Test
    void searchProductsWithNoResult() {
        when(productMapper.selectApproved("all", "暂无结果")).thenReturn(List.of());

        List<ShopDtos.ProductView> result = shopService.products("all", "暂无结果");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getProductDetailWhenExists() {
        when(productMapper.selectById(101)).thenReturn(validProduct);

        ShopDtos.ProductView result = shopService.product("101");

        assertNotNull(result);
        assertEquals("101", result.id());
        assertEquals("测试商品", result.title());
    }

    @Test
    void getProductDetailWhenNotExists() {
        when(productMapper.selectById(404)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> shopService.product("404")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private ShopDtos.PublishRequest createValidPublishRequest() {
        return new ShopDtos.PublishRequest(
                "new",
                "测试商品",
                "/images/test.jpg",
                "数码",
                BigDecimal.valueOf(399),
                "9成新",
                "测试描述",
                "测试故事",
                BigDecimal.valueOf(320),
                "北京"
        );
    }

    private ProductRecord createValidProductRecord() {
        ProductRecord product = new ProductRecord();
        product.setGoodsId(101);
        product.setSellerId(null);
        product.setSellerName("卖家A");
        product.setSellerCredit(96);
        product.setGoodsName("测试商品");
        product.setCategory("数码");
        product.setGoodsDesc("这是测试用商品描述");
        product.setGoodsCondition("9成新");
        product.setStory("使用很少，状态良好");
        product.setPrice(BigDecimal.valueOf(399));
        product.setFloorPrice(BigDecimal.valueOf(320));
        product.setScene("new");
        product.setAddress("北京");
        product.setImage("/images/test.jpg");
        product.setStatus("approved");
        product.setCreateTime(LocalDateTime.now());
        return product;
    }
}
