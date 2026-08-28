package com.example.shopping_back.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shopping_back.auth.dto.AuthUserView;
import com.example.shopping_back.shop.ShopDtos.AuditRequest;
import com.example.shopping_back.shop.ShopDtos.AuditResult;
import com.example.shopping_back.shop.ShopDtos.OrderView;
import com.example.shopping_back.shop.ShopDtos.ProductView;
import com.example.shopping_back.shop.ShopDtos.TopicPostView;
import com.example.shopping_back.shop.ShopDtos.TopicView;
import com.example.shopping_back.shop.mapper.ShopOrderMapper;
import com.example.shopping_back.shop.mapper.ShopProductMapper;
import com.example.shopping_back.shop.mapper.ShopStoreMapper;
import com.example.shopping_back.shop.mapper.ShopTopicMapper;
import com.example.shopping_back.shop.model.OrderRecord;
import com.example.shopping_back.shop.model.ProductRecord;
import com.example.shopping_back.shop.model.StoreRecord;
import com.example.shopping_back.shop.model.TopicPostRecord;
import com.example.shopping_back.shop.model.TopicRecord;
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

         StoreRecord mockStore = new StoreRecord();
        mockStore.setStoreId(1);
        mockStore.setSellerId(100);
        mockStore.setStoreName("测试店铺");
        lenient().when(storeMapper.selectBySeller(100)).thenReturn(mockStore);
        lenient().when(storeMapper.followerCount(1)).thenReturn(0);
    }

    @Test
    void createProductSuccess() {
        StoreRecord store = new StoreRecord();
        store.setStoreId(1);
        store.setSellerId(10);
        when(storeMapper.selectBySeller(10)).thenReturn(store);
        doAnswer(invocation -> {
            ProductRecord product = invocation.getArgument(0);
            product.setGoodsId(101);
            return 1;
        }).when(productMapper).insert(any(ProductRecord.class));
        when(productMapper.selectById(101)).thenReturn(validProduct);

        ShopDtos.PublishRequest request = createValidPublishRequest();
        ShopDtos.ProductView result = shopService.publish(request, sellerUser());

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
                () -> shopService.publish(request,sellerUser())
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
                () -> shopService.publish(request, sellerUser())
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

    // ==================== 店铺模块测试（TC-STORE-01 ~ 04） ====================

    @Test
    // TC-STORE-01 查看店铺信息
    void getStoreInfoSuccess() {
        StoreRecord store = new StoreRecord();
        store.setStoreId(1);
        store.setSellerId(10);
        store.setSellerName("卖家A");
        store.setStoreName("测试店铺");
        store.setStoreDesc("这是一个测试店铺");
        store.setStatus("normal");
        when(storeMapper.selectById(1)).thenReturn(store);
        when(storeMapper.followerCount(1)).thenReturn(12);

        ShopDtos.StoreDetailView result = shopService.store("1", null);

        assertNotNull(result);
        assertEquals("测试店铺", result.name());
        assertEquals("这是一个测试店铺", result.desc());
    }

    @Test
    // TC-STORE-02 关注店铺
    void followStoreSuccess() {
        StoreRecord store = new StoreRecord();
        store.setStoreId(1);
        store.setSellerId(10);
        store.setSellerName("卖家A");
        store.setStoreName("测试店铺");
        store.setStoreDesc("这是一个测试店铺");
        store.setStatus("normal");
        when(storeMapper.selectById(1)).thenReturn(store);
        when(storeMapper.follow(100, 1, "测试店铺")).thenReturn(1);
        when(storeMapper.followerCount(1)).thenReturn(13);
        when(storeMapper.isFollowed(100, 1)).thenReturn(1);

        AuthUserView user = new AuthUserView(100, "alice", "138****8000", 100, "buyer", "买家", false, "normal", "");
        ShopDtos.StoreDetailView result = shopService.followStore("1", user);

        assertNotNull(result);
        assertEquals("测试店铺", result.name());
        verify(storeMapper, times(1)).follow(100, 1, "测试店铺");
    }

    @Test
    // TC-STORE-03 取消关注店铺
    void unfollowStoreSuccess() {
        StoreRecord store = new StoreRecord();
        store.setStoreId(1);
        store.setSellerId(10);
        store.setSellerName("卖家A");
        store.setStoreName("测试店铺");
        store.setStoreDesc("这是一个测试店铺");
        store.setStatus("normal");
        when(storeMapper.selectById(1)).thenReturn(store);
        when(storeMapper.unfollow(100, 1)).thenReturn(1);
        when(storeMapper.followerCount(1)).thenReturn(11);
        when(storeMapper.isFollowed(100, 1)).thenReturn(0);

        AuthUserView user = new AuthUserView(100, "alice", "138****8000", 100, "buyer", "买家", false, "normal", "");
        ShopDtos.StoreDetailView result = shopService.unfollowStore("1", user);

        assertNotNull(result);
        assertEquals("测试店铺", result.name());
        verify(storeMapper, times(1)).unfollow(100, 1);
    }

    @Test
    // TC-STORE-04 查看店铺商品列表
    void getStoreProductsSuccess() {
        StoreRecord store = new StoreRecord();
        store.setStoreId(1);
        store.setSellerId(10);
        store.setSellerName("卖家A");
        store.setStoreName("测试店铺");
        store.setStatus("normal");
        when(storeMapper.selectById(1)).thenReturn(store);
        when(productMapper.selectBySeller(10)).thenReturn(List.of(validProduct));

        List<ShopDtos.ProductView> result = shopService.storeProducts("1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试商品", result.get(0).title());
    }

    // ==================== 话题社区模块测试（TC-TOPIC-01 ~ 08） ====================

    @Test
    void listTopicsReturnsMatchingTopics() {
        TopicRecord topic = new TopicRecord();
        topic.setTopicId(1);
        topic.setTitle("校园二手交易");
        topic.setTopicDesc("新品与二手闲聊");
        topic.setType("买家话题");
        topic.setStatus("normal");
        when(topicMapper.selectTopics("all", "校园")).thenReturn(List.of(topic));

        List<ShopDtos.TopicView> result = shopService.topics("all", "校园");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("校园二手交易", result.get(0).title());
    }

    @Test
    void createTopicRequiresBuyerRole() {
        AuthUserView seller = new AuthUserView(10, "seller", "138****8000", 100, "seller", "卖家", false, "normal", "");
        ShopDtos.TopicCreateRequest request = new ShopDtos.TopicCreateRequest("校园二手交易", "一起交流", "买家话题", "", List.of("二手"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> shopService.createTopic(request, seller)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void createTopicPostSuccess() {
        AuthUserView buyer = new AuthUserView(100, "alice", "138****8000", 100, "buyer", "买家", false, "normal", "");
        TopicRecord topic = new TopicRecord();
        topic.setTopicId(1);
        topic.setTitle("校园二手交易");
        topic.setStatus("normal");
        when(topicMapper.selectTopic(1, 100)).thenReturn(topic);

        TopicPostRecord post = new TopicPostRecord();
        post.setPostId(5);
        post.setTopicId(1);
        post.setUsername("alice");
        post.setContent("这件商品很适合在宿舍用");
        post.setCreatedAt(LocalDateTime.now());
        when(topicMapper.selectPosts(1, 100)).thenReturn(List.of(post));

        List<ShopDtos.TopicPostView> result = shopService.createTopicPost(
                "1",
                new ShopDtos.TopicPostRequest("这件商品很适合在宿舍用", List.of(), "", ""),
                buyer
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("这件商品很适合在宿舍用", result.get(0).content());
    }

    @Test
    void createTopicCommentSuccess() {
        AuthUserView buyer = new AuthUserView(100, "alice", "138****8000", 100, "buyer", "买家", false, "normal", "");
        when(topicMapper.topicIdByPost(5)).thenReturn(1);
        when(topicMapper.selectPosts(1, 100)).thenReturn(List.of(topicPostRecord(5, 1, "评论测试")));

        List<ShopDtos.TopicPostView> result = shopService.createTopicComment(
                "5",
                new ShopDtos.TopicCommentRequest("这评论也很有意思"),
                buyer
        );

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void toggleTopicPostLikeSuccess() {
        AuthUserView buyer = new AuthUserView(100, "alice", "138****8000", 100, "buyer", "买家", false, "normal", "");
        when(topicMapper.topicIdByPost(5)).thenReturn(1);
        when(topicMapper.likeExists(5, 100)).thenReturn(0);
        when(topicMapper.insertLike(5, 100)).thenReturn(1);
        when(topicMapper.selectPosts(1, 100)).thenReturn(List.of(topicPostRecord(5, 1, "点赞测试")));

        List<ShopDtos.TopicPostView> result = shopService.toggleTopicPostLike("5", buyer);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(topicMapper).insertLike(5, 100);
    }

    @Test
    void followAndUnfollowTopicSuccess() {
        AuthUserView buyer = new AuthUserView(100, "alice", "138****8000", 100, "buyer", "买家", false, "normal", "");
        TopicRecord topic = new TopicRecord();
        topic.setTopicId(1);
        topic.setTitle("校园二手交易");
        topic.setTopicDesc("聊二手");
        topic.setType("买家话题");
        topic.setStatus("normal");
        when(topicMapper.selectTopic(1, 100)).thenReturn(topic);
        when(topicMapper.insertTopicFollow(100, 1, "校园二手交易")).thenReturn(1);
        when(topicMapper.deleteTopicFollow(1, 100)).thenReturn(1);

        ShopDtos.TopicView followed = shopService.followTopic("1", buyer);
        assertEquals("校园二手交易", followed.title());

        ShopDtos.TopicView unfollowed = shopService.unfollowTopic("1", buyer);
        assertEquals("校园二手交易", unfollowed.title());
    }

    // ==================== 话题社区模块补充测试 ====================

    @Test
    // TC-TOPIC-02 查看话题详情
    void topicDetailReturnsTopic() {
        TopicRecord topic = new TopicRecord();
        topic.setTopicId(1);
        topic.setTitle("校园二手交易");
        topic.setTopicDesc("聊二手");
        topic.setStatus("normal");
        when(topicMapper.selectTopic(1, null)).thenReturn(topic);
        TopicView result = shopService.topic("1", null);
        assertNotNull(result);
        assertEquals("校园二手交易", result.title());
    }

    @Test
    // 查看话题帖子列表
    void topicPostsReturnsPosts() {
        when(topicMapper.selectPosts(1, null)).thenReturn(List.of(topicPostRecord(5, 1, "帖子内容")));
        List<TopicPostView> result = shopService.topicPosts("1", null);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== 订单模块测试（TC-ORD-01 ~ 06） ====================

    @Test
    // TC-ORD-01 创建订单成功
    void createOrderSuccess() {
        validProduct.setSellerId(10);
        when(productMapper.selectById(101)).thenReturn(validProduct);
        when(orderMapper.insertOrder(100, 10, 101, "completed", BigDecimal.valueOf(798))).thenReturn(1);

        OrderRecord order = new OrderRecord();
        order.setOrderId(7);
        order.setBuyerId(100);
        order.setSellerId(10);
        order.setGoodsId(101);
        order.setStatus("completed");
        order.setAmount(BigDecimal.valueOf(798));
        order.setGoodsName("测试商品");
        order.setGoodsImage("/images/test.jpg");
        order.setScene("new");
        order.setSellerName("卖家A");
        order.setCreatedAt(LocalDateTime.now());
        when(orderMapper.selectBuyerOrdersFiltered(eq(100), isNull())).thenReturn(List.of(order));

        List<ShopDtos.OrderView> result = shopService.createOrders(
                new ShopDtos.CreateOrderRequest(List.of(new ShopDtos.CreateOrderItem("101", 2))),
                buyerUser()
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试商品", result.get(0).title());
        assertEquals(BigDecimal.valueOf(798), result.get(0).amount());
    }

    @Test
    // TC-ORD-02 查看订单列表-正常场景（已登录用户）
    void ordersReturnsListForLoggedInUser() {

        OrderRecord order = new OrderRecord();
        order.setOrderId(10);
        order.setBuyerId(100);
        order.setSellerId(50);
        order.setGoodsId(101);
        order.setStatus("completed");
        order.setAmount(BigDecimal.valueOf(699));
        order.setGoodsName("测试耳机");
        order.setGoodsImage("/images/test.jpg");
        order.setScene("new");
        order.setSellerName("测试卖家");

        when(orderMapper.selectBuyerOrdersFiltered(eq(100), isNull())).thenReturn(List.of(order));
        List<OrderView> result = shopService.orders(buyerUser());
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试耳机", result.get(0).title());
        assertEquals(BigDecimal.valueOf(699), result.get(0).amount());
        assertEquals("已完成", result.get(0).status());  // "completed" 映射为 "已完成"
    }

    @Test
    // TC-ORD-03 不能购买自己发布的商品
    void createOrderRejectsSelfPurchase() {
        validProduct.setSellerId(100);
        when(productMapper.selectById(101)).thenReturn(validProduct);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> shopService.createOrders(
                        new ShopDtos.CreateOrderRequest(List.of(new ShopDtos.CreateOrderItem("101", 1))),
                        buyerUser()
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    // TC-ORD-04 商品不存在时拒绝下单
    void createOrderRejectsUnknownProduct() {
        when(productMapper.selectById(404)).thenReturn(null);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> shopService.createOrders(
                        new ShopDtos.CreateOrderRequest(List.of(new ShopDtos.CreateOrderItem("404", 1))),
                        buyerUser()
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    // TC-ORD-05 未登录用户不能查看订单
    void getOrdersRequiresLogin() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> shopService.orders(null)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    // TC-ORD-06 正常评价订单
    void reviewOrderSuccess() {
        OrderRecord order = new OrderRecord();
        order.setOrderId(7);
        order.setBuyerId(100);
        order.setSellerId(10);
        order.setGoodsId(101);
        order.setStatus("completed");
        order.setAmount(BigDecimal.valueOf(399));
        order.setGoodsName("测试商品");
        order.setGoodsImage("/images/test.jpg");
        order.setScene("new");
        order.setSellerName("卖家A");

        OrderRecord reviewed = new OrderRecord();
        reviewed.setOrderId(7);
        reviewed.setBuyerId(100);
        reviewed.setSellerId(10);
        reviewed.setGoodsId(101);
        reviewed.setStatus("completed");
        reviewed.setAmount(BigDecimal.valueOf(399));
        reviewed.setGoodsName("测试商品");
        reviewed.setGoodsImage("/images/test.jpg");
        reviewed.setScene("new");
        reviewed.setSellerName("卖家A");
        reviewed.setProductScore(5);
        reviewed.setSellerScore(5);
        reviewed.setReviewContent("质量很好");
        reviewed.setReviewedAt(LocalDateTime.now());

        when(orderMapper.selectOrder(7)).thenReturn(order, reviewed);
        when(orderMapper.reviewCountByOrder(7)).thenReturn(0);
        when(orderMapper.insertReview(7, 101, 100, 10, 5, 5, "质量很好")).thenReturn(1);

        ShopDtos.OrderView result = shopService.reviewOrder(
                "7",
                new ShopDtos.ReviewRequest(5, 5, "质量很好"),
                buyerUser()
        );

        assertNotNull(result);
        assertEquals("测试商品", result.title());
        assertEquals("质量很好", result.reviewContent());
    }

    @Test
    // TC-ORD-07 未完成订单不能评价
    void reviewOrderRejectsUncompletedOrder() {
        OrderRecord order = new OrderRecord();
        order.setOrderId(7);
        order.setBuyerId(100);
        order.setSellerId(10);
        order.setGoodsId(101);
        order.setStatus("paid");
        when(orderMapper.selectOrder(7)).thenReturn(order);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> shopService.reviewOrder("7", new ShopDtos.ReviewRequest(5, 5, "质量很好"), buyerUser())
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    // TC-ORD-08 已评价订单不能重复评价
    void reviewOrderRejectsDuplicateReview() {
        OrderRecord order = new OrderRecord();
        order.setOrderId(7);
        order.setBuyerId(100);
        order.setSellerId(10);
        order.setGoodsId(101);
        order.setStatus("completed");
        when(orderMapper.selectOrder(7)).thenReturn(order);
        when(orderMapper.reviewCountByOrder(7)).thenReturn(1);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> shopService.reviewOrder("7", new ShopDtos.ReviewRequest(5, 5, "质量很好"), buyerUser())
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    }

    private TopicPostRecord topicPostRecord(Integer postId, Integer topicId, String content) {
        TopicPostRecord record = new TopicPostRecord();
        record.setPostId(postId);
        record.setTopicId(topicId);
        record.setUsername("alice");
        record.setContent(content);
        record.setCreatedAt(LocalDateTime.now());
        record.setLikeCount(0);
        record.setCommentCount(0);
        return record;
    }

    private AuthUserView buyerUser() {
        return new AuthUserView(100, "alice", "138****8000", 100, "buyer", "买家", false, "normal", "");
    }

    private AuthUserView sellerUser() {
        return new AuthUserView(10, "seller", "138****8000", 100, "seller", "卖家", false, "normal", "");
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

    // ==================== 用户中心模块测试（TC-USER-07） ====================

    @Test
    // TC-USER-07 查看我发布的商品
    void myProductsReturnsListForLoggedInUser() {
        // 1. 准备一个商品数据（已审核通过）
        ProductRecord product = new ProductRecord();
        product.setGoodsId(101);
        product.setSellerId(100);
        product.setGoodsName("我的测试商品");
        product.setCategory("数码");
        product.setGoodsDesc("这是我发布的测试商品");
        product.setGoodsCondition("全新");
        product.setPrice(BigDecimal.valueOf(399));
        product.setScene("new");
        product.setAddress("北京");
        product.setImage("/images/test.jpg");
        product.setStatus("approved");
        product.setCreateTime(LocalDateTime.now());

        // 2. Mock Mapper 行为：返回包含该商品的列表
        when(productMapper.selectBySeller(100)).thenReturn(List.of(product));

        // 3. 执行查询（使用 buyerUser() 返回 userId=100 的用户）
        List<ProductView> result = shopService.myProducts(buyerUser());

        // 4. 断言
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("我的测试商品", result.get(0).title());
        assertEquals("approved", result.get(0).status());
        assertEquals("new", result.get(0).scene());
        assertEquals(BigDecimal.valueOf(399), result.get(0).price());
    }

    // ==================== 管理审核模块测试（TC-ADMIN-01 ~ 03） ====================
    @Test
    // TC-ADMIN-01 查看待审核商品列表
    void pendingProductsReturnsList() {
        when(productMapper.selectPending()).thenReturn(List.of(validProduct));
        List<ProductView> result = shopService.pendingProducts();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试商品", result.get(0).title());
    }

    @Test
    // TC-ADMIN-02 审核通过
    void auditApproveSuccess() {
        String productId = "101";
        when(productMapper.updateAuditStatus(101, "approved", "")).thenReturn(1);
        AuditResult result = shopService.audit(productId, new AuditRequest("approve", ""));
        assertNotNull(result);
        assertEquals("approved", result.status());
    }

    @Test
    // TC-ADMIN-03 审核拒绝
    void auditRejectSuccess() {
        String productId = "101";
        String reason = "图片模糊";
        when(productMapper.updateAuditStatus(101, "rejected", reason)).thenReturn(1);
        AuditResult result = shopService.audit(productId, new AuditRequest("reject", reason));
        assertNotNull(result);
        assertEquals("rejected", result.status());
        assertEquals(reason, result.rejectReason());
    }
        // ==================== AI 议价/建议测试（TC-CHAT-08 ~ 09） ====================

    @Test
    // TC-CHAT-08 AI议价-启用（对应 ShopService.assist）
    void assistReturnsAiResponseForProduct() {
        // 1. 准备一个有效商品
        when(productMapper.selectById(101)).thenReturn(validProduct);

        // 2. 构造请求
        ShopDtos.AiAssistRequest request = new ShopDtos.AiAssistRequest("101", "这个价格还能再便宜吗？", BigDecimal.valueOf(300));

        // 3. 执行方法
        ShopDtos.AiAssistResponse response = shopService.assist(request);

        // 4. 断言
        assertNotNull(response);
        assertNotNull(response.answer());
        assertTrue(response.answer().contains("测试商品")); // 应包含商品名
        assertNotNull(response.checklist());
        assertFalse(response.checklist().isEmpty());
        assertNotNull(response.consensus());
    }

    @Test
    // TC-CHAT-09 AI议价-建议生成（对应 ShopService.suggestPublish）
    void suggestPublishReturnsMockSuggestionWhenNoApiKey() {
        // 注意：当前配置的 API Key 为空，所以会走 mock 分支
        ShopDtos.AiPublishSuggestionRequest request = new ShopDtos.AiPublishSuggestionRequest(
                "used",      // scene
                "数码",      // category
                "9成新",     // condition
                "耳机"       // keyword
        );

        ShopDtos.AiPublishSuggestionResponse response = shopService.suggestPublish(request);

        assertNotNull(response);
        assertNotNull(response.title());
        assertTrue(response.title().contains("二手") || response.title().contains("耳机"));
        assertNotNull(response.price());
        assertTrue(response.price().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(response.description());
        assertNotNull(response.story());
        assertEquals("mock_missing_key", response.source()); // 确认走的是 mock 分支
    }
    // ==================== 补充覆盖率测试（提升 shop 模块覆盖率） ====================

    @Test
    // 场景：productMapper 抛异常时，products 方法应优雅降级返回空列表
    void products_handlesMapperExceptionGracefully() {
        when(productMapper.selectApproved("all", ""))
            .thenThrow(new RuntimeException("DB connection error"));
        
        List<ShopDtos.ProductView> result = shopService.products("all", "");
        
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Mapper异常时应返回空列表，而非抛出异常");
    }

    @Test
    // 场景：发布二手商品但未填成色 -> 应返回 400
    void createProductRejectsUsedSceneWithoutCondition() {
        ShopDtos.PublishRequest request = new ShopDtos.PublishRequest(
                "used",                     // scene
                "二手耳机",                  // title
                "/img/headphone.jpg",       // image
                "数码",                     // category
                BigDecimal.valueOf(200),    // price
                "",                         // condition (空)
                "功能完好",                 // description
                "自用转让",                 // story
                BigDecimal.valueOf(180),    // floorPrice
                "北京"                      // location
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> shopService.publish(request, sellerUser())
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    // 场景：发布二手商品但未填故事 -> 应返回 400
    void createProductRejectsUsedSceneWithoutStory() {
        ShopDtos.PublishRequest request = new ShopDtos.PublishRequest(
                "used",                     // scene
                "二手耳机",                  // title
                "/img/headphone.jpg",       // image
                "数码",                     // category
                BigDecimal.valueOf(200),    // price
                "9成新",                    // condition (有)
                "功能完好",                 // description
                "",                         // story (空)
                BigDecimal.valueOf(180),    // floorPrice
                "北京"                      // location
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> shopService.publish(request, sellerUser())
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    // 场景：未登录用户关注店铺 -> 应返回 401
    void followStore_throwsWhenUserNull() {
        assertThrows(ResponseStatusException.class, () -> shopService.followStore("1", null));
    }

    @Test
    // 场景：关注不存在的店铺 -> 应返回 404
    void followStore_throwsWhenStoreNotFound() {
        when(storeMapper.selectById(999)).thenReturn(null);
        
        AuthUserView user = buyerUser();
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> shopService.followStore("999", user)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    // 场景：未登录用户取消关注店铺 -> 应返回 401
    void unfollowStore_throwsWhenUserNull() {
        assertThrows(ResponseStatusException.class, () -> shopService.unfollowStore("1", null));
    }

    @Test
    // 场景：创建订单时 quantity 为 null -> 默认数量应为 1
    void createOrders_quantityNullDefaultsToOne() {
        // 准备商品
        validProduct.setSellerId(10);
        when(productMapper.selectById(101)).thenReturn(validProduct);
        
        // 订单项：quantity 为 null
        ShopDtos.CreateOrderItem item = new ShopDtos.CreateOrderItem("101", null);
        ShopDtos.CreateOrderRequest request = new ShopDtos.CreateOrderRequest(List.of(item));
        
        // Mock 插入行为
        when(orderMapper.insertOrder(anyInt(), anyInt(), anyInt(), anyString(), any(BigDecimal.class)))
            .thenReturn(1);
        
        // 执行
        shopService.createOrders(request, buyerUser());
        
        // 验证：insertOrder 被调用时，数量为 1（即价格 * 1）
        verify(orderMapper, times(1))
            .insertOrder(anyInt(), anyInt(), anyInt(), anyString(), eq(BigDecimal.valueOf(399)));
    }
}
