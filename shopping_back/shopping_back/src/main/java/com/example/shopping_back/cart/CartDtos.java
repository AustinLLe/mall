package com.example.shopping_back.cart;

import java.math.BigDecimal;

public final class CartDtos {
    private CartDtos() {
    }

    public record CartItemView(
            Integer cartId,
            Integer goodsId,
            String title,
            String cover,
            BigDecimal price,
            Integer quantity,
            boolean selected,
            BigDecimal subtotal
    ) {
    }

    public record AddCartRequest(Integer goodsId, Integer quantity) {
    }

    public record UpdateCartRequest(Integer quantity) {
    }

    public record SelectCartRequest(Boolean selected) {
    }
}
