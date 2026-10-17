package com.storeconnect.inventory.service;

import com.storeconnect.inventory.dto.*;
import com.storeconnect.inventory.entity.Product;
import com.storeconnect.inventory.entity.Store;
import com.storeconnect.inventory.entity.StoreInventory;
import com.storeconnect.inventory.exception.InsufficientStockException;
import com.storeconnect.inventory.exception.InventoryNotFoundException;
import com.storeconnect.inventory.exception.ProductNotFoundException;
import com.storeconnect.inventory.exception.StoreNotFoundException;
import com.storeconnect.inventory.repository.ProductRepository;
import com.storeconnect.inventory.repository.StoreInventoryRepository;
import com.storeconnect.inventory.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final StoreInventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    public InventoryService(StoreInventoryRepository inventoryRepository,
                            ProductRepository productRepository,
                            StoreRepository storeRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(String productId) {
        Product product = productRepository.findById(productId)
                .orElseGet(() -> productRepository.findBySkuIgnoreCase(productId)
                        .orElseThrow(() -> new ProductNotFoundException(productId)));

        List<StoreInventory> inventories = inventoryRepository.findStoresWithAvailableStock(product.getId());
        Map<String, Store> storeMap = storeRepository.findAllById(
                inventories.stream().map(StoreInventory::getStoreId).toList()
        ).stream().collect(Collectors.toMap(Store::getId, s -> s));

        List<AvailabilityResponse.StoreAvailabilityItem> storeItems = inventories.stream()
                .filter(inv -> storeMap.containsKey(inv.getStoreId()) && Boolean.TRUE.equals(storeMap.get(inv.getStoreId()).getIsActive()))
                .map(inv -> {
                    Store store = storeMap.get(inv.getStoreId());
                    return new AvailabilityResponse.StoreAvailabilityItem(
                            store.getId(),
                            store.getName(),
                            store.getCity(),
                            inv.getQuantityAvailable()
                    );
                })
                .toList();

        int totalAvailable = storeItems.stream()
                .mapToInt(AvailabilityResponse.StoreAvailabilityItem::quantityAvailable)
                .sum();

        return new AvailabilityResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                totalAvailable,
                storeItems
        );
    }

    @Transactional(readOnly = true)
    public List<InventoryDTO> getInventoryByStore(String storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));

        List<StoreInventory> inventories = inventoryRepository.findByStoreId(storeId);
        Map<String, Product> productMap = productRepository.findAllById(
                inventories.stream().map(StoreInventory::getProductId).toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        return inventories.stream()
                .map(inv -> {
                    Product prod = productMap.get(inv.getProductId());
                    return new InventoryDTO(
                            inv.getId(),
                            store.getId(),
                            store.getName(),
                            inv.getProductId(),
                            prod != null ? prod.getName() : "Unknown",
                            prod != null ? prod.getSku() : "N/A",
                            inv.getQuantityAvailable(),
                            inv.getQuantityReserved(),
                            inv.getReorderThreshold(),
                            inv.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryDTO getStoreProductStock(String storeId, String productId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        StoreInventory inv = inventoryRepository.findByStoreIdAndProductId(storeId, productId)
                .orElseGet(() -> StoreInventory.builder()
                        .id("INV-NONE")
                        .storeId(storeId)
                        .productId(productId)
                        .quantityAvailable(0)
                        .quantityReserved(0)
                        .reorderThreshold(5)
                        .build());

        return new InventoryDTO(
                inv.getId(),
                store.getId(),
                store.getName(),
                product.getId(),
                product.getName(),
                product.getSku(),
                inv.getQuantityAvailable(),
                inv.getQuantityReserved(),
                inv.getReorderThreshold(),
                inv.getUpdatedAt()
        );
    }

    @Transactional
    public InventoryDTO updateStock(String storeId, String productId, StockUpdateRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        StoreInventory inventory = inventoryRepository.findByStoreIdAndProductId(storeId, productId)
                .orElseGet(() -> StoreInventory.builder()
                        .id("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .storeId(storeId)
                        .productId(productId)
                        .quantityAvailable(0)
                        .quantityReserved(0)
                        .reorderThreshold(5)
                        .build());

        inventory.setQuantityAvailable(request.quantityAvailable());
        if (request.reorderThreshold() != null) {
            inventory.setReorderThreshold(request.reorderThreshold());
        }
        inventory.setUpdatedAt(LocalDateTime.now());

        StoreInventory saved = inventoryRepository.save(inventory);
        log.info("Updated stock for store {} and product {}: available={}", storeId, productId, saved.getQuantityAvailable());

        return new InventoryDTO(
                saved.getId(),
                store.getId(),
                store.getName(),
                product.getId(),
                product.getName(),
                product.getSku(),
                saved.getQuantityAvailable(),
                saved.getQuantityReserved(),
                saved.getReorderThreshold(),
                saved.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<InventoryDTO> getLowStockInventories() {
        List<StoreInventory> lowStock = inventoryRepository.findLowStockInventories();
        Map<String, Store> storeMap = storeRepository.findAll().stream()
                .collect(Collectors.toMap(Store::getId, s -> s));
        Map<String, Product> productMap = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return lowStock.stream()
                .map(inv -> {
                    Store store = storeMap.get(inv.getStoreId());
                    Product prod = productMap.get(inv.getProductId());
                    return new InventoryDTO(
                            inv.getId(),
                            inv.getStoreId(),
                            store != null ? store.getName() : "Unknown",
                            inv.getProductId(),
                            prod != null ? prod.getName() : "Unknown",
                            prod != null ? prod.getSku() : "N/A",
                            inv.getQuantityAvailable(),
                            inv.getQuantityReserved(),
                            inv.getReorderThreshold(),
                            inv.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public StockValidateResponse validateStock(StockValidateRequest request) {
        List<StockValidateResponse.StockIssue> issues = new ArrayList<>();

        for (StockValidateItem item : request.items()) {
            var inventoryOpt = inventoryRepository.findByStoreIdAndProductId(request.storeId(), item.productId());
            if (inventoryOpt.isEmpty()) {
                issues.add(new StockValidateResponse.StockIssue(
                        item.productId(), item.quantity(), 0, "No inventory record in this store"
                ));
            } else {
                StoreInventory inv = inventoryOpt.get();
                if (inv.getQuantityAvailable() < item.quantity()) {
                    issues.add(new StockValidateResponse.StockIssue(
                            item.productId(), item.quantity(), inv.getQuantityAvailable(), "Insufficient stock"
                    ));
                }
            }
        }

        if (issues.isEmpty()) {
            return StockValidateResponse.success(request.storeId());
        } else {
            return StockValidateResponse.failure(request.storeId(), issues);
        }
    }

    @Transactional
    public void reserveStock(StockReservationRequest request) {
        log.info("Reserving stock for order {} at store {}", request.orderId(), request.storeId());

        for (StockValidateItem item : request.items()) {
            StoreInventory inventory = inventoryRepository.findByStoreIdAndProductId(request.storeId(), item.productId())
                    .orElseThrow(() -> new InventoryNotFoundException(request.storeId(), item.productId()));

            if (inventory.getQuantityAvailable() < item.quantity()) {
                throw new InsufficientStockException(item.productId(), item.quantity(), inventory.getQuantityAvailable());
            }

            inventory.setQuantityAvailable(inventory.getQuantityAvailable() - item.quantity());
            inventory.setQuantityReserved(inventory.getQuantityReserved() + item.quantity());
            inventory.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);
        }
    }

    @Transactional
    public void releaseStock(StockReservationRequest request) {
        log.info("Releasing reserved stock for order {} at store {}", request.orderId(), request.storeId());

        for (StockValidateItem item : request.items()) {
            inventoryRepository.findByStoreIdAndProductId(request.storeId(), item.productId())
                    .ifPresent(inventory -> {
                        int returnQty = Math.min(item.quantity(), inventory.getQuantityReserved());
                        inventory.setQuantityReserved(inventory.getQuantityReserved() - returnQty);
                        inventory.setQuantityAvailable(inventory.getQuantityAvailable() + returnQty);
                        inventory.setUpdatedAt(LocalDateTime.now());
                        inventoryRepository.save(inventory);
                    });
        }
    }
}
