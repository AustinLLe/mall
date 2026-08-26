package com.example.shopping_back.center;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import com.example.shopping_back.auth.AuthService;
import com.example.shopping_back.auth.dto.AuthUserView;
import com.example.shopping_back.center.CenterDtos.AdminCenter;
import com.example.shopping_back.center.CenterDtos.BuyerCenter;
import com.example.shopping_back.center.CenterDtos.InteractionCreateRequest;
import com.example.shopping_back.center.CenterDtos.ModuleCard;
import com.example.shopping_back.center.CenterDtos.RejectRequest;
import com.example.shopping_back.center.CenterDtos.SellerCenter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class CenterServiceTest {

    private CenterMapper mapper;
    private AuthService authService;
    private CenterService centerService;

    @BeforeEach
    void setUp() {
        mapper = mock(CenterMapper.class);
        authService = mock(AuthService.class);
        when(mapper.countColumn(anyString(), anyString())).thenReturn(1);
        when(authService.me("token")).thenReturn(user());
        when(mapper.creditRecordCount(7)).thenReturn(1);
        when(mapper.favoriteCount(7)).thenReturn(0);
        when(mapper.browseCount(7)).thenReturn(0);
        when(mapper.followCount(7)).thenReturn(0);
        when(mapper.followTopicCount(7)).thenReturn(0);
        centerService = new CenterService(mapper, authService);
    }

    @Test
    void addFavoriteStoresActualGoodsId() {
        centerService.addBuyerItem("token", "favorite", new InteractionCreateRequest("42", "显示器", "松果小店"));

        verify(mapper).addFavorite(7, 42, "显示器");
    }

    @Test
    void addBrowseStoresActualGoodsId() {
        centerService.addBuyerItem("token", "history", new InteractionCreateRequest("18", "二手相机", "松果小店"));

        verify(mapper).addBrowse(7, 18, "二手相机");
    }

    @Test
    void addFavoriteRejectsMissingGoodsId() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> centerService.addBuyerItem("token", "favorite", new InteractionCreateRequest("", "显示器", "松果小店")));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    private static AuthUserView user() {
        return new AuthUserView(7, "alice", "138****8000", 100, "buyer", "买家", false, "normal", "");
    }

    // ==================== 用户中心模块测试（TC-USER-01 ~ 03, 10） ====================

    @Test
    // TC-USER-01 买家中心
    void buyerCenterReturnsDashboard() {
        // 1. 准备 Mock 数据
        when(mapper.favoriteCount(7)).thenReturn(5);
        when(mapper.browseCount(7)).thenReturn(3);
        when(mapper.followCount(7)).thenReturn(2);
        when(mapper.followTopicCount(7)).thenReturn(4);
        when(mapper.latestRealName(7)).thenReturn(null);
        when(mapper.latestCreditReason(7)).thenReturn("信用良好");

        // 2. 执行方法
        BuyerCenter result = centerService.buyer("token");

        // 3. 断言
        assertNotNull(result);
        assertEquals("alice", result.username());
        assertEquals("138****8000", result.phoneMasked());
        assertEquals("正常", result.accountStatus());
        assertNotNull(result.modules());
        assertTrue(result.modules().size() >= 4);

        // 验证信用分卡片存在
        boolean hasCreditCard = result.modules().stream()
                .anyMatch(card -> "信用分".equals(card.title()));
        assertTrue(hasCreditCard, "买家中心应包含信用分卡片");
    }

    @Test
    // TC-USER-02 卖家中心/卖家工作台
    void sellerCenterReturnsDashboard() {
        // 1. 准备 Mock 数据（用户角色为 seller）
        AuthUserView sellerUser = new AuthUserView(7, "alice", "138****8000", 100, "seller", "卖家", false, "normal", "");
        when(authService.me("token")).thenReturn(sellerUser);

        when(mapper.storeName(7)).thenReturn("Alice 的店铺");
        when(mapper.storeStatus(7)).thenReturn("normal");
        when(mapper.sellerGoodsCount(7, "0")).thenReturn(10);
        when(mapper.sellerGoodsCount(7, "2")).thenReturn(2);
        when(mapper.sellerOrderCount(7, "pending_ship")).thenReturn(3);
        when(mapper.sellerAmount(7)).thenReturn(BigDecimal.valueOf(1500));
        when(mapper.violationCount(7)).thenReturn(0);
        when(mapper.latestRealName(7)).thenReturn(null);

        // 2. 执行方法
        SellerCenter result = centerService.seller("token");

        // 3. 断言
        assertNotNull(result);
        assertEquals("alice", result.username());
        assertEquals("Alice 的店铺", result.storeName());
        assertEquals("正常", result.storeStatus());
        assertNotNull(result.modules());
        assertTrue(result.modules().size() >= 4);

        // 验证店铺信息卡片存在
        boolean hasStoreCard = result.modules().stream()
                .anyMatch(card -> "店铺信息".equals(card.title()));
        assertTrue(hasStoreCard, "卖家中心应包含店铺信息卡片");
    }

    @Test
    // TC-USER-03 管理员中心/管理后台
    void adminCenterReturnsDashboard() {
        // 1. 准备 Mock 数据（用户角色为 admin）
        AuthUserView adminUser = new AuthUserView(3, "admin", "139****0000", 100, "admin", "管理员", false, "normal", "");
        when(authService.me("token")).thenReturn(adminUser);

        when(mapper.totalUsers()).thenReturn(50);
        when(mapper.roleCount("buyer")).thenReturn(30);
        when(mapper.roleCount("seller")).thenReturn(15);
        when(mapper.disabledUsers()).thenReturn(2);
        when(mapper.totalGoods()).thenReturn(100);
        when(mapper.pendingGoods()).thenReturn(5);
        when(mapper.pendingStores()).thenReturn(2);
        when(mapper.pendingRealName()).thenReturn(3);
        when(mapper.users()).thenReturn(List.of());
        when(mapper.pendingRealNames()).thenReturn(List.of());

        // 2. 执行方法
        AdminCenter result = centerService.admin("token");

        // 3. 断言
        assertNotNull(result);
        assertEquals("admin", result.username());
        assertNotNull(result.modules());
        assertTrue(result.modules().size() >= 4);

        // 验证平台总览卡片存在
        boolean hasOverviewCard = result.modules().stream()
                .anyMatch(card -> "平台总览".equals(card.title()));
        assertTrue(hasOverviewCard, "管理员中心应包含平台总览卡片");

        // 验证用户列表和实名认证审核列表不为 null
        assertNotNull(result.users());
        assertNotNull(result.realNameAudits());
    }

    @Test
    // TC-USER-10 信用分查看（通过 buyer 中心获取）
    void buyerCenterContainsCreditScore() {
        // 1. 准备 Mock 数据（用户信用分已在 setUp 中 mock 为 100）
        when(mapper.favoriteCount(7)).thenReturn(0);
        when(mapper.browseCount(7)).thenReturn(0);
        when(mapper.followCount(7)).thenReturn(0);
        when(mapper.followTopicCount(7)).thenReturn(0);
        when(mapper.latestRealName(7)).thenReturn(null);
        when(mapper.latestCreditReason(7)).thenReturn("系统初始化信用档案");

        // 2. 执行方法
        BuyerCenter result = centerService.buyer("token");

        // 3. 断言
        assertNotNull(result);

        // 查找信用分卡片
        ModuleCard creditCard = result.modules().stream()
                .filter(card -> "信用分".equals(card.title()))
                .findFirst()
                .orElse(null);

        assertNotNull(creditCard, "买家中心应包含信用分卡片");
        assertEquals("100", creditCard.value(), "信用分应为 100");
        assertEquals("优秀", creditCard.status(), "信用等级应为 优秀");
    }
    // ==================== 管理审核模块补充测试（TC-ADMIN-05） ====================

    @Test
    // TC-ADMIN-05 实名认证审核 - 审核通过
    void approveRealNameSuccess() {
        // 1. 准备管理员用户
        AuthUserView adminUser = new AuthUserView(3, "admin", "139****0000", 100, "admin", "管理员", false, "normal", "");
        when(authService.me("admin-token")).thenReturn(adminUser);

        // 2. Mock Mapper 行为：审核通过
        when(mapper.approveRealName(1, 3)).thenReturn(1);
        
        // 3. Mock admin() 依赖的统计数据（避免 NPE）
        when(mapper.totalUsers()).thenReturn(10);
        when(mapper.roleCount("buyer")).thenReturn(5);
        when(mapper.roleCount("seller")).thenReturn(3);
        when(mapper.disabledUsers()).thenReturn(0);
        when(mapper.totalGoods()).thenReturn(20);
        when(mapper.pendingGoods()).thenReturn(0);
        when(mapper.pendingStores()).thenReturn(0);
        when(mapper.pendingRealName()).thenReturn(1);
        when(mapper.users()).thenReturn(List.of());
        when(mapper.pendingRealNames()).thenReturn(List.of());

        // 4. 执行方法
        AdminCenter result = centerService.approveRealName("admin-token", 1);

        // 5. 断言
        assertNotNull(result);
        assertEquals("admin", result.username());
        verify(mapper, times(1)).approveRealName(1, 3);
    }

    @Test
    // TC-ADMIN-05 实名认证审核 - 审核拒绝
    void rejectRealNameSuccess() {
        // 1. 准备管理员用户
        AuthUserView adminUser = new AuthUserView(3, "admin", "139****0000", 100, "admin", "管理员", false, "normal", "");
        when(authService.me("admin-token")).thenReturn(adminUser);

        // 2. 准备拒绝请求
        RejectRequest rejectRequest = new RejectRequest("身份证照片不清晰");

        // 3. Mock Mapper 行为：审核拒绝
        when(mapper.rejectRealName(1, 3, "身份证照片不清晰")).thenReturn(1);
        
        // 4. Mock admin() 依赖的统计数据
        when(mapper.totalUsers()).thenReturn(10);
        when(mapper.roleCount("buyer")).thenReturn(5);
        when(mapper.roleCount("seller")).thenReturn(3);
        when(mapper.disabledUsers()).thenReturn(0);
        when(mapper.totalGoods()).thenReturn(20);
        when(mapper.pendingGoods()).thenReturn(0);
        when(mapper.pendingStores()).thenReturn(0);
        when(mapper.pendingRealName()).thenReturn(1);
        when(mapper.users()).thenReturn(List.of());
        when(mapper.pendingRealNames()).thenReturn(List.of());

        // 5. 执行方法
        AdminCenter result = centerService.rejectRealName("admin-token", 1, rejectRequest);

        // 6. 断言
        assertNotNull(result);
        assertEquals("admin", result.username());
        verify(mapper, times(1)).rejectRealName(1, 3, "身份证照片不清晰");
    }
}
