package com.storeconnect.inventory.controller;

import com.storeconnect.inventory.dto.*;
import com.storeconnect.inventory.service.InventoryService;
import com.storeconnect.inventory.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/inventory")
public class InternalInventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    public InternalInventoryController(InventoryService inventoryService, ProductService productService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
    }

    @PostMapping("/validate-stock")
    public ResponseEntity<StockValidateResponse> validateStock(@Valid @RequestBody StockValidateRequest request) {
        return ResponseEntity.ok(inventoryService.validateStock(request));
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveStock(@Valid @RequestBody StockReservationRequest request) {
        inventoryService.reserveStock(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseStock(@Valid @RequestBody StockReservationRequest request) {
        inventoryService.releaseStock(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductDTO> getProductForInternal(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}
