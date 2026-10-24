package com.storeconnect.inventory.service;

import com.storeconnect.inventory.dto.StoreDTO;
import com.storeconnect.inventory.entity.Store;
import com.storeconnect.inventory.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public List<StoreDTO> getAllActiveStores(String city) {
        List<Store> stores = (city != null && !city.isBlank())
                ? storeRepository.findByIsActiveTrueAndCityIgnoreCase(city.trim())
                : storeRepository.findByIsActiveTrue();

        return stores.stream()
                .map(s -> new StoreDTO(s.getId(), s.getName(), s.getCode(), s.getAddress(), s.getCity(), s.getPhone(), s.getIsActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    public StoreDTO getStoreById(String id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + id));
        return new StoreDTO(store.getId(), store.getName(), store.getCode(), store.getAddress(), store.getCity(), store.getPhone(), store.getIsActive());
    }
}
