package com.example.mall.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderService.OrderView create(@Valid @RequestBody CreateOrderRequest request) {
        return service.create(new OrderService.CreateOrder(
                request.clientRequestId(), request.buyerId(), request.addressId(), request.productId(), request.quantity()));
    }

    @GetMapping("/{id}")
    public OrderService.OrderView find(@PathVariable long id) { return service.find(id); }

    public record CreateOrderRequest(
            @NotBlank @Size(max = 64) String clientRequestId,
            @Positive long buyerId,
            @Positive long addressId,
            @Positive long productId,
            @Positive int quantity) {}
}
