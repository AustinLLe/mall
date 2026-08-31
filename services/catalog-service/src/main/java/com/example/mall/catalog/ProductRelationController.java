package com.example.mall.catalog;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/center/buyer/items")
public class ProductRelationController {
    private final ProductRelationService service;

    public ProductRelationController(ProductRelationService service) { this.service = service; }

    @GetMapping("/{type}")
    public List<ProductRelationService.RelationItem> list(@PathVariable String type,
            @RequestHeader("X-User-Id") Long userId) {
        return service.list(type, CatalogUser.required(userId, null, null).userId());
    }

    @PostMapping("/{type}")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductRelationService.RelationItem add(@PathVariable String type,
            @RequestHeader("X-User-Id") Long userId, @Valid @RequestBody AddRelationRequest request) {
        return service.add(type, CatalogUser.required(userId, null, null).userId(), request.goodsId());
    }

    @PutMapping("/{type}/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@PathVariable String type, @RequestHeader("X-User-Id") Long userId) {
        service.clear(type, CatalogUser.required(userId, null, null).userId());
    }

    public record AddRelationRequest(@JsonAlias("itemId") @NotNull @Positive Long goodsId) {}
}
