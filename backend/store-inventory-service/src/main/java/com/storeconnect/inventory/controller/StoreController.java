package com.storeconnect.inventory.controller;

import com.storeconnect.inventory.dto.StoreDTO;
import com.storeconnect.inventory.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public ResponseEntity<List<StoreDTO>> getAllStores(@RequestParam(name = "city", required = false) String city) {
        return ResponseEntity.ok(storeService.getAllActiveStores(city));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDTO> getStoreById(@PathVariable("id") String id) {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }
}
