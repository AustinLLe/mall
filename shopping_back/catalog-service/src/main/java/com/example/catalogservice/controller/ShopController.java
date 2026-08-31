package com.example.catalogservice.controller;

import com.example.catalogservice.auth.AuthClient;
import com.example.catalogservice.auth.AuthUserView;
import com.example.common.dto.ApiResult;
import com.example.catalogservice.dto.ShopDtos.AiPublishSuggestionRequest;
import com.example.catalogservice.dto.ShopDtos.AiPublishSuggestionResponse;
import com.example.catalogservice.dto.ShopDtos.AiAssistRequest;
import com.example.catalogservice.dto.ShopDtos.AiAssistResponse;
import com.example.catalogservice.dto.ShopDtos.AuditRequest;
import com.example.catalogservice.dto.ShopDtos.AuditResult;
import com.example.catalogservice.dto.ShopDtos.ProductView;
import com.example.catalogservice.dto.ShopDtos.PublishRequest;
import com.example.catalogservice.dto.ShopDtos.UpdateProductRequest;
import com.example.catalogservice.dto.ShopDtos.StoreDetailView;
import com.example.catalogservice.dto.ShopDtos.StoreUpdateRequest;
import com.example.catalogservice.dto.ShopDtos.StoreView;
import com.example.catalogservice.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final AuthClient authService;

    public ShopController(ShopService shopService, AuthClient authService) {
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
        return ApiResult.ok(shopService.myProducts(requireUser(authorization)));
    }

    @GetMapping("/products/{id}")
    public ApiResult<ProductView> product(@PathVariable String id) {
        return ApiResult.ok(shopService.product(id));
    }

    @GetMapping("/stores")
    public ApiResult<List<StoreView>> stores() {
        return ApiResult.ok(shopService.stores());
    }

    @GetMapping("/stores/mine")
    public ApiResult<StoreDetailView> myStore(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResult.ok(shopService.myStore(currentUserOrNull(authorization)));
    }

    @PutMapping("/stores/mine")
    public ApiResult<StoreDetailView> updateMyStore(
            @Valid @RequestBody StoreUpdateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResult.ok(shopService.updateMyStore(request, currentUserOrNull(authorization)));
    }

    @GetMapping("/stores/{id}")
    public ApiResult<StoreDetailView> store(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResult.ok(shopService.store(id, currentUserOrNull(authorization)));
    }

    @GetMapping("/stores/{id}/products")
    public ApiResult<List<ProductView>> storeProducts(@PathVariable String id) {
        return ApiResult.ok(shopService.storeProducts(id));
    }

    @PostMapping("/stores/{id}/follow")
    public ApiResult<StoreDetailView> followStore(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResult.ok(shopService.followStore(id, currentUserOrNull(authorization)));
    }

    @DeleteMapping("/stores/{id}/follow")
    public ApiResult<StoreDetailView> unfollowStore(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResult.ok(shopService.unfollowStore(id, currentUserOrNull(authorization)));
    }

    @PostMapping("/products")
    public ApiResult<ProductView> publish(
            @Valid @RequestBody PublishRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ApiResult.ok(shopService.publish(request, requireUser(authorization)));
    }

    @PutMapping("/products/{id}")
    public ApiResult<ProductView> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ApiResult.ok(shopService.updateProduct(id, request, requireUser(authorization)));
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

    private AuthUserView requireUser(String authorization) {
        String token = bearerToken(authorization);
        return authService.me(token);
    }

    private static String bearerToken(String authorization) {
        if (authorization == null) {
            return null;
        }
        String v = authorization.trim();
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return v.substring(7).trim();
        }
        // 不携带合法的 Bearer 前缀一律视为未携带 Token
        return null;
    }
}
