package com.storeconnect.inventory.controller;

import com.storeconnect.inventory.dto.ProductDTO;
import com.storeconnect.inventory.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ResponseEntity.ok(productService.searchProducts(categoryId, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable("id") String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @RequestHeader(value = "X-User-Role", defaultValue = "CUSTOMER") String userRole,
            @Valid @RequestBody ProductDTO request) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }
}
