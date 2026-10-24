package com.storeconnect.inventory.controller;

import com.storeconnect.inventory.dto.InventoryDTO;
import com.storeconnect.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/store/{storeId}/product/{productId}")
    public ResponseEntity<InventoryDTO> getStoreProductStock(
            @PathVariable("storeId") String storeId,
            @PathVariable("productId") String productId) {
        return ResponseEntity.ok(inventoryService.getStoreProductStock(storeId, productId));
    }
}
