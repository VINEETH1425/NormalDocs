package com.storeconnect.inventory.controller;

import com.storeconnect.inventory.dto.AvailabilityResponse;
import com.storeconnect.inventory.dto.InventoryDTO;
import com.storeconnect.inventory.dto.StockUpdateRequest;
import com.storeconnect.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventory", description = "Real-time stock checking, store inventory, and stock updates")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "Check product availability across all stores (Public)")
    @GetMapping("/availability/{productId}")
    public ResponseEntity<AvailabilityResponse> getProductAvailability(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getAvailability(productId));
    }

    @Operation(summary = "Get inventory stock for a specific store (Store Operator or Admin)")
    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('STORE_OPERATOR', 'ADMIN')")
    public ResponseEntity<List<InventoryDTO>> getStoreInventory(@PathVariable String storeId) {
        return ResponseEntity.ok(inventoryService.getInventoryByStore(storeId));
    }

    @Operation(summary = "Get inventory stock for a specific store and product")
    @GetMapping("/store/{storeId}/product/{productId}")
    public ResponseEntity<InventoryDTO> getStoreProductStock(
            @PathVariable String storeId,
            @PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getStoreProductStock(storeId, productId));
    }

    @Operation(summary = "Update store product stock levels (Store Operator or Admin)")
    @PutMapping("/store/{storeId}/product/{productId}")
    @PreAuthorize("hasAnyRole('STORE_OPERATOR', 'ADMIN')")
    public ResponseEntity<InventoryDTO> updateStock(
            @PathVariable String storeId,
            @PathVariable String productId,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.updateStock(storeId, productId, request));
    }

    @Operation(summary = "Get low-stock inventory alert report (Admin only)")
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryDTO>> getLowStockReport() {
        return ResponseEntity.ok(inventoryService.getLowStockInventories());
    }
}
