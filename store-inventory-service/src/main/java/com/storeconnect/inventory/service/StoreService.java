package com.storeconnect.inventory.service;

import com.storeconnect.inventory.dto.StoreDTO;
import com.storeconnect.inventory.entity.Store;
import com.storeconnect.inventory.exception.StoreNotFoundException;
import com.storeconnect.inventory.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StoreService {

    private static final Logger log = LoggerFactory.getLogger(StoreService.class);

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public List<StoreDTO> getAllActiveStores(String city) {
        List<Store> stores = (city != null && !city.isBlank())
                ? storeRepository.findByCityIgnoreCaseAndIsActiveTrue(city.trim())
                : storeRepository.findByIsActiveTrue();

        return stores.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public StoreDTO getStoreById(String id) {
        return storeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new StoreNotFoundException(id));
    }

    @Transactional
    public StoreDTO createStore(StoreDTO request) {
        String id = request.id() != null && !request.id().isBlank()
                ? request.id()
                : "STORE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Store store = Store.builder()
                .id(id)
                .name(request.name().trim())
                .code(request.code().trim().toUpperCase())
                .address(request.address().trim())
                .city(request.city().trim())
                .phone(request.phone())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        Store saved = storeRepository.save(store);
        log.info("Created store with code: {}", saved.getCode());
        return toDTO(saved);
    }

    public StoreDTO toDTO(Store store) {
        return new StoreDTO(
                store.getId(),
                store.getName(),
                store.getCode(),
                store.getAddress(),
                store.getCity(),
                store.getPhone(),
                store.getIsActive()
        );
    }
}
