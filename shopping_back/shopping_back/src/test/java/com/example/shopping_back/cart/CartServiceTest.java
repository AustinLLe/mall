package com.example.shopping_back.cart;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.shopping_back.auth.AuthService;
import com.example.shopping_back.auth.dto.AuthUserView;
import com.example.shopping_back.cart.CartDtos.AddCartRequest;
import com.example.shopping_back.cart.CartDtos.CartItemView;
import com.example.shopping_back.cart.CartDtos.SelectCartRequest;
import com.example.shopping_back.cart.CartDtos.UpdateCartRequest;
import com.example.shopping_back.shop.mapper.ShopProductMapper;
import com.example.shopping_back.shop.model.ProductRecord;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ShopProductMapper productMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CartService cartService;

    private AuthUserView testUser;
    private ProductRecord testProduct;
    private CartItemRecord testCartItem;

    @BeforeEach
    void setUp() {
        testUser = new AuthUserView(100, "testuser", "138****8000", 100, "buyer", "买家", false, "normal", "");
        testProduct = new ProductRecord();
        testProduct.setGoodsId(101);
        testProduct.setGoodsName("测试商品");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        testProduct.setImage("/images/test.jpg");

        testCartItem = new CartItemRecord();
        testCartItem.setCartId(1);
        testCartItem.setUserId(100);
        testCartItem.setGoodsId(101);
        testCartItem.setQuantity(2);
        testCartItem.setSelected(true);
        testCartItem.setGoodsName("测试商品");
        testCartItem.setPrice(BigDecimal.valueOf(99.99));
        testCartItem.setImage("/images/test.jpg");
    }

    @Test
    // TC-CART-01 添加商品到购物车
    void addItemSuccess() {
        when(authService.me("valid-token")).thenReturn(testUser);
        when(productMapper.selectById(101)).thenReturn(testProduct);
        when(cartMapper.addItem(100, 101, 2)).thenReturn(1);
        when(cartMapper.selectItems(100)).thenReturn(List.of(testCartItem));

        AddCartRequest request = new AddCartRequest(101, 2);
        List<CartItemView> result = cartService.add("valid-token", request);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试商品", result.get(0).title());
        verify(cartMapper, times(1)).addItem(100, 101, 2);
    }

    @Test
    // TC-CART-01 添加商品-未登录
    void addItemRejectsUnauthenticated() {
        when(authService.me("invalid-token")).thenThrow(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录")
        );

        AddCartRequest request = new AddCartRequest(101, 2);
        assertThrows(ResponseStatusException.class, () -> cartService.add("invalid-token", request));
    }

    @Test
    // TC-CART-01 添加商品-商品不存在
    void addItemRejectsProductNotFound() {
        when(authService.me("valid-token")).thenReturn(testUser);
        when(productMapper.selectById(999)).thenReturn(null);

        AddCartRequest request = new AddCartRequest(999, 2);
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.add("valid-token", request)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    // TC-CART-02 查看购物车列表
    void listCartItemsSuccess() {
        when(authService.me("valid-token")).thenReturn(testUser);
        when(cartMapper.selectItems(100)).thenReturn(List.of(testCartItem));

        List<CartItemView> result = cartService.list("valid-token");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试商品", result.get(0).title());
        assertEquals(BigDecimal.valueOf(199.98), result.get(0).subtotal());
    }

    @Test
    // TC-CART-02 查看购物车-空列表
    void listCartItemsEmpty() {
        when(authService.me("valid-token")).thenReturn(testUser);
        when(cartMapper.selectItems(100)).thenReturn(List.of());

        List<CartItemView> result = cartService.list("valid-token");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    // TC-CART-03 更新商品数量
    void updateQuantitySuccess() {
        when(authService.me("valid-token")).thenReturn(testUser);
        when(cartMapper.countByCartId(1, 100)).thenReturn(1);
        when(cartMapper.updateQuantity(1, 100, 3)).thenReturn(1);
        when(cartMapper.selectItems(100)).thenReturn(List.of(testCartItem));

        UpdateCartRequest request = new UpdateCartRequest(3);
        List<CartItemView> result = cartService.updateQuantity("valid-token", 1, request);

        assertNotNull(result);
        verify(cartMapper, times(1)).updateQuantity(1, 100, 3);
    }

    @Test
    // TC-CART-04 更新数量为0 -> 删除商品
    void updateQuantityZeroDeletesItem() {
        when(authService.me("valid-token")).thenReturn(testUser);
        when(cartMapper.countByCartId(1, 100)).thenReturn(1);
        when(cartMapper.deleteItem(1, 100)).thenReturn(1);
        when(cartMapper.selectItems(100)).thenReturn(List.of());

        UpdateCartRequest request = new UpdateCartRequest(0);
        List<CartItemView> result = cartService.updateQuantity("valid-token", 1, request);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cartMapper, times(1)).deleteItem(1, 100);
    }

    @Test
    // TC-CART-05 删除购物车商品
    void removeItemSuccess() {
        when(authService.me("valid-token")).thenReturn(testUser);
        when(cartMapper.countByCartId(1, 100)).thenReturn(1);
        when(cartMapper.deleteItem(1, 100)).thenReturn(1);
        when(cartMapper.selectItems(100)).thenReturn(List.of());

        List<CartItemView> result = cartService.remove("valid-token", 1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cartMapper, times(1)).deleteItem(1, 100);
    }

    @Test
    // TC-CART-06 勾选/取消勾选商品
    void selectItemSuccess() {
        when(authService.me("valid-token")).thenReturn(testUser);
        when(cartMapper.countByCartId(1, 100)).thenReturn(1);
        when(cartMapper.updateSelected(1, 100, false)).thenReturn(1);
        when(cartMapper.selectItems(100)).thenReturn(List.of(testCartItem));

        SelectCartRequest request = new SelectCartRequest(false);
        List<CartItemView> result = cartService.select("valid-token", 1, request);

        assertNotNull(result);
        verify(cartMapper, times(1)).updateSelected(1, 100, false);
    }
}