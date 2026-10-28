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
    public ResponseEntity<InventoryDTO> getStoreProductStock(@PathVariable("storeId") String storeId,@PathVariable("productId") String productId) {
        return ResponseEntity.ok(inventoryService.getStoreProductStock(storeId, productId));
    }

    @PostMapping("/store/{storeId}/product/{productId}/restock")
    public ResponseEntity<InventoryDTO> restock(@PathVariable("storeId") String storeId,
                                                @PathVariable("productId") String productId,
                                                @RequestHeader(value = "X-User-Role", required = false) String userRole,
                                                @RequestBody java.util.Map<String, Integer> request) {
        if (!"STORE_OPERATOR".equalsIgnoreCase(userRole) && !"ADMIN".equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        int quantity = request.getOrDefault("quantity", 10);
        return ResponseEntity.ok(inventoryService.restock(storeId, productId, quantity));
    }
}
