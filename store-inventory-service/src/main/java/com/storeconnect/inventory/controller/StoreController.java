package com.storeconnect.inventory.controller;

import com.storeconnect.inventory.dto.StoreDTO;
import com.storeconnect.inventory.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Stores", description = "Physical retail store locations and lookup")
@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @Operation(summary = "List all active stores (optional city filter)")
    @GetMapping
    public ResponseEntity<List<StoreDTO>> getAllStores(@RequestParam(required = false) String city) {
        return ResponseEntity.ok(storeService.getAllActiveStores(city));
    }

    @Operation(summary = "Get store details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<StoreDTO> getStoreById(@PathVariable String id) {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    @Operation(summary = "Create store location (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreDTO> createStore(@Valid @RequestBody StoreDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.createStore(request));
    }
}
