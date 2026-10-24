package com.storeconnect.inventory.controller;

import com.storeconnect.inventory.dto.StockReservationRequest;
import com.storeconnect.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/inventory")
public class InternalInventoryController {

    private final InventoryService inventoryService;

    public InternalInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveStock(@RequestBody StockReservationRequest request) {
        inventoryService.reserveStock(request.getStoreId(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseStock(@RequestBody StockReservationRequest request) {
        inventoryService.releaseStock(request.getStoreId(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }
}
