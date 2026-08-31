package com.example.mall.catalog;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) { this.service = service; }

    @GetMapping public List<ProductService.ProductView> list() { return service.list(); }

    @GetMapping("/mine")
    public List<ProductService.ProductView> mine(@RequestHeader("X-User-Id") Long userId) {
        return service.mine(CatalogUser.required(userId, null, null).userId());
    }

    @GetMapping("/{id}") public ProductService.ProductView find(@PathVariable long id) { return service.find(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductService.ProductView create(@Valid @RequestBody CreateProductRequest request) {
        return service.create(new ProductService.CreateProduct(request.sellerId(), request.name(), request.price()));
    }

    @PutMapping("/{id}")
    public ProductService.ProductView update(@PathVariable long id, @Valid @RequestBody UpdateProductRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return service.update(id, new ProductService.UpdateProduct(request.name(), request.category(),
                request.description(), request.condition(), request.story(), request.price(), request.floorPrice(),
                request.location(), request.image()), CatalogUser.required(userId, role, username));
    }

    public record CreateProductRequest(@Positive long sellerId, @NotBlank @Size(max = 255) String name,
                                       @DecimalMin("0.01") BigDecimal price) {}
    public record UpdateProductRequest(@JsonAlias("title") @NotBlank @Size(max = 255) String name,
            @NotBlank @Size(max = 100) String category, @Size(max = 2000) String description,
            @Size(max = 100) String condition, @Size(max = 2000) String story,
            @DecimalMin("0.01") BigDecimal price, @DecimalMin("0.00") BigDecimal floorPrice,
            @Size(max = 255) String location, @Size(max = 1000) String image) {}
}
