package com.example.shopping_back.shop;

import com.example.shopping_back.common.dto.ApiResult;
import com.example.shopping_back.shop.ShopDtos.AiAssistRequest;
import com.example.shopping_back.shop.ShopDtos.AiAssistResponse;
import com.example.shopping_back.shop.ShopDtos.OrderView;
import com.example.shopping_back.shop.ShopDtos.ProductView;
import com.example.shopping_back.shop.ShopDtos.PublishRequest;
import com.example.shopping_back.shop.ShopDtos.StoreView;
import com.example.shopping_back.shop.ShopDtos.TopicView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShopController {
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/products")
    public ApiResult<List<ProductView>> products(
            @RequestParam(required = false) String scene,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResult.ok(shopService.products(scene, keyword));
    }

    @GetMapping("/products/{id}")
    public ApiResult<ProductView> product(@PathVariable String id) {
        return ApiResult.ok(shopService.product(id));
    }

    @GetMapping("/stores")
    public ApiResult<List<StoreView>> stores() {
        return ApiResult.ok(shopService.stores());
    }

    @GetMapping("/topics")
    public ApiResult<List<TopicView>> topics() {
        return ApiResult.ok(shopService.topics());
    }

    @GetMapping("/orders")
    public ApiResult<List<OrderView>> orders() {
        return ApiResult.ok(shopService.orders());
    }

    @PostMapping("/products")
    public ApiResult<ProductView> publish(@RequestBody PublishRequest request) {
        return ApiResult.ok(shopService.publish(request));
    }

    @GetMapping("/admin/audit")
    public ApiResult<List<ProductView>> auditList() {
        return ApiResult.ok(shopService.products("all", null));
    }

    @PostMapping("/admin/audit/{id}")
    public ApiResult<Map<String, String>> audit(@PathVariable String id, @RequestBody Map<String, String> body) {
        return ApiResult.ok(Map.of("id", id, "status", body.getOrDefault("status", "approved")));
    }

    @PostMapping("/ai/assist")
    public ApiResult<AiAssistResponse> assist(@RequestBody AiAssistRequest request) {
        return ApiResult.ok(shopService.assist(request));
    }
}
