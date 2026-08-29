package com.example.mall.catalog;

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

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductService.ProductView> list() { return service.list(); }

    @GetMapping("/{id}")
    public ProductService.ProductView find(@PathVariable long id) { return service.find(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductService.ProductView create(@Valid @RequestBody CreateProductRequest request) {
        return service.create(new ProductService.CreateProduct(request.sellerId(), request.name(), request.price()));
    }

    public record CreateProductRequest(
            @Positive long sellerId,
            @NotBlank @Size(max = 255) String name,
            @DecimalMin("0.01") BigDecimal price) {}
}
