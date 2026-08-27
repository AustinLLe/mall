package com.example.shopping_back.cart;

import com.example.shopping_back.cart.CartDtos.AddCartRequest;
import com.example.shopping_back.cart.CartDtos.CartItemView;
import com.example.shopping_back.cart.CartDtos.SelectCartRequest;
import com.example.shopping_back.cart.CartDtos.UpdateCartRequest;
import com.example.shopping_back.common.dto.ApiResult;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ApiResult<List<CartItemView>> list(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResult.ok(cartService.list(bearerToken(authorization)));
    }

    @PostMapping
    public ApiResult<List<CartItemView>> add(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AddCartRequest body) {
        return ApiResult.ok(cartService.add(bearerToken(authorization), body));
    }

    @PutMapping("/{cartId}")
    public ApiResult<List<CartItemView>> updateQuantity(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("cartId") Integer cartId,
            @RequestBody(required = false) UpdateCartRequest body) {
        return ApiResult.ok(cartService.updateQuantity(bearerToken(authorization), cartId, body));
    }

    @DeleteMapping("/{cartId}")
    public ApiResult<List<CartItemView>> remove(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("cartId") Integer cartId) {
        return ApiResult.ok(cartService.remove(bearerToken(authorization), cartId));
    }

    @PutMapping("/{cartId}/select")
    public ApiResult<List<CartItemView>> select(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("cartId") Integer cartId,
            @RequestBody(required = false) SelectCartRequest body) {
        return ApiResult.ok(cartService.select(bearerToken(authorization), cartId, body));
    }

    private static String bearerToken(String authorization) {
        if (authorization == null) {
            return null;
        }
        String v = authorization.trim();
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return v.substring(7).trim();
        }
        return null;
    }
}
