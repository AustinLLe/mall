package com.example.shopping_back.shop;

import com.example.shopping_back.auth.AuthService;
import com.example.shopping_back.auth.dto.AuthUserView;
import com.example.shopping_back.common.dto.ApiResult;
import com.example.shopping_back.shop.ShopDtos.AiPublishSuggestionRequest;
import com.example.shopping_back.shop.ShopDtos.AiPublishSuggestionResponse;
import com.example.shopping_back.shop.ShopDtos.AiAssistRequest;
import com.example.shopping_back.shop.ShopDtos.AiAssistResponse;
import com.example.shopping_back.shop.ShopDtos.AuditRequest;
import com.example.shopping_back.shop.ShopDtos.AuditResult;
import com.example.shopping_back.shop.ShopDtos.OrderView;
import com.example.shopping_back.shop.ShopDtos.ProductView;
import com.example.shopping_back.shop.ShopDtos.PublishRequest;
import com.example.shopping_back.shop.ShopDtos.StoreView;
import com.example.shopping_back.shop.ShopDtos.TopicView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ShopController {
    private final ShopService shopService;
    private final AuthService authService;

    public ShopController(ShopService shopService, AuthService authService) {
        this.shopService = shopService;
        this.authService = authService;
    }

    @GetMapping("/products")
    public ApiResult<List<ProductView>> products(
            @RequestParam(required = false) String scene,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResult.ok(shopService.products(scene, keyword));
    }

    @GetMapping("/products/mine")
    public ApiResult<List<ProductView>> myProducts(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResult.ok(shopService.myProducts(currentUserOrNull(authorization)));
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
    public ApiResult<ProductView> publish(
            @Valid @RequestBody PublishRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ApiResult.ok(shopService.publish(request, currentUserOrNull(authorization)));
    }

    @GetMapping("/admin/audit")
    public ApiResult<List<ProductView>> auditList(@RequestHeader(value = "Authorization", required = false) String authorization) {
        ensureAdmin(authorization);
        return ApiResult.ok(shopService.pendingProducts());
    }

    @PostMapping("/admin/audit/{id}")
    public ApiResult<AuditResult> audit(
            @PathVariable String id,
            @Valid @RequestBody AuditRequest body,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        ensureAdmin(authorization);
        return ApiResult.ok(shopService.audit(id, body));
    }

    @PostMapping("/ai/assist")
    public ApiResult<AiAssistResponse> assist(@RequestBody AiAssistRequest request) {
        return ApiResult.ok(shopService.assist(request));
    }

    @PostMapping("/ai/publish-suggestion")
    public ApiResult<AiPublishSuggestionResponse> publishSuggestion(@RequestBody AiPublishSuggestionRequest request) {
        return ApiResult.ok(shopService.suggestPublish(request));
    }

    private void ensureAdmin(String authorization) {
        AuthUserView user = currentUserOrNull(authorization);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录管理员账号");
        }
        if (!"admin".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前用户无管理员权限");
        }
    }

    private AuthUserView currentUserOrNull(String authorization) {
        String token = bearerToken(authorization);
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return authService.me(token);
        } catch (ResponseStatusException ignored) {
            return null;
        }
    }

    private static String bearerToken(String authorization) {
        if (authorization == null) {
            return null;
        }
        String v = authorization.trim();
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return v.substring(7).trim();
        }
        return v;
    }
}
