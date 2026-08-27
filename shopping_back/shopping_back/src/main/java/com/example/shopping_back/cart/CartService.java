package com.example.shopping_back.cart;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CartService {
    private final CartMapper cartMapper;
    private final ShopProductMapper productMapper;
    private final AuthService authService;

    public CartService(CartMapper cartMapper, ShopProductMapper productMapper, AuthService authService) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
        this.authService = authService;
        ensureSchema();
    }

    public List<CartItemView> list(String token) {
        AuthUserView user = current(token);
        ensureSchema();
        return cartMapper.selectItems(user.getUserId()).stream().map(this::toView).toList();
    }

    public List<CartItemView> add(String token, AddCartRequest request) {
        AuthUserView user = current(token);
        ensureSchema();
        Integer goodsId = request == null ? null : request.goodsId();
        if (goodsId == null || goodsId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品信息缺失");
        }
        ProductRecord product = productMapper.selectById(goodsId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        int quantity = request.quantity() == null || request.quantity() <= 0 ? 1 : request.quantity();
        cartMapper.addItem(user.getUserId(), goodsId, quantity);
        return list(token);
    }

    public List<CartItemView> updateQuantity(String token, Integer cartId, UpdateCartRequest request) {
        AuthUserView user = current(token);
        ensureSchema();
        requireOwnCart(user.getUserId(), cartId);
        int quantity = request == null || request.quantity() == null ? 0 : request.quantity();
        if (quantity <= 0) {
            cartMapper.deleteItem(cartId, user.getUserId());
        } else {
            cartMapper.updateQuantity(cartId, user.getUserId(), quantity);
        }
        return list(token);
    }

    public List<CartItemView> remove(String token, Integer cartId) {
        AuthUserView user = current(token);
        ensureSchema();
        requireOwnCart(user.getUserId(), cartId);
        cartMapper.deleteItem(cartId, user.getUserId());
        return list(token);
    }

    public List<CartItemView> select(String token, Integer cartId, SelectCartRequest request) {
        AuthUserView user = current(token);
        ensureSchema();
        requireOwnCart(user.getUserId(), cartId);
        boolean selected = request == null || request.selected() == null || request.selected();
        cartMapper.updateSelected(cartId, user.getUserId(), selected);
        return list(token);
    }

    private void requireOwnCart(Integer userId, Integer cartId) {
        if (cartId == null || cartMapper.countByCartId(cartId, userId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "购物车记录不存在");
        }
    }

    private AuthUserView current(String token) {
        return authService.me(token);
    }

    private void ensureSchema() {
        try {
            cartMapper.createTable();
        } catch (RuntimeException ignored) {
        }
    }

    private CartItemView toView(CartItemRecord record) {
        BigDecimal price = record.getPrice() == null ? BigDecimal.ZERO : record.getPrice();
        int quantity = record.getQuantity() == null ? 1 : record.getQuantity();
        BigDecimal subtotal = price.multiply(new BigDecimal(quantity));
        return new CartItemView(
                record.getCartId(),
                record.getGoodsId(),
                record.getGoodsName() == null ? "商品" : record.getGoodsName(),
                record.getImage() == null ? "" : record.getImage(),
                price,
                quantity,
                record.isSelected(),
                subtotal
        );
    }
}
