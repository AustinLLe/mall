package com.example.mall.catalog;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {
    private final StoreService service;

    public StoreController(StoreService service) { this.service = service; }

    @GetMapping public List<StoreService.StoreView> list() { return service.list(); }

    @GetMapping("/mine")
    public StoreService.StoreView mine(@RequestHeader("X-User-Id") Long userId) {
        return service.mine(CatalogUser.required(userId, null, null).userId());
    }

    @GetMapping("/{id}") public StoreService.StoreView find(@PathVariable long id) { return service.find(id); }

    @GetMapping("/{id}/products")
    public List<ProductService.ProductView> products(@PathVariable long id) { return service.products(id); }
}
