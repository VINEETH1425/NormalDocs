package com.storeconnect.inventory.service;

import com.storeconnect.inventory.dto.InventoryDTO;
import com.storeconnect.inventory.entity.Product;
import com.storeconnect.inventory.entity.Store;
import com.storeconnect.inventory.entity.StoreInventory;
import com.storeconnect.inventory.repository.ProductRepository;
import com.storeconnect.inventory.repository.StoreInventoryRepository;
import com.storeconnect.inventory.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryService {

    private final StoreInventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    public InventoryService(StoreInventoryRepository inventoryRepository,
                            StoreRepository storeRepository,
                            ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public InventoryDTO getStoreProductStock(String storeId, String productId) {
        Optional<StoreInventory> inventoryOpt = inventoryRepository.findByStoreIdAndProductId(storeId, productId);
        Product product = productRepository.findById(productId).orElse(null);
        Store store = storeRepository.findById(storeId).orElse(null);

        String productName = product != null ? product.getName() : "Product " + productId;
        String productSku = product != null ? product.getSku() : "";
        String storeName = store != null ? store.getName() : storeId;

        if (inventoryOpt.isPresent()) {
            StoreInventory inv = inventoryOpt.get();
            return new InventoryDTO(
                    inv.getId(),
                    storeId,
                    storeName,
                    productId,
                    productName,
                    productSku,
                    inv.getQuantityAvailable(),
                    inv.getQuantityReserved(),
                    inv.getReorderThreshold(),
                    inv.getUpdatedAt()
            );
        }

        return new InventoryDTO(null, storeId, storeName, productId, productName, productSku, 0, 0, 5, null);
    }

    @Transactional
    public void reserveStock(String storeId, String productId, int quantity) {
        StoreInventory inventory = inventoryRepository.findByStoreIdAndProductId(storeId, productId)
                .orElseThrow(() -> new IllegalArgumentException("No stock record for store " + storeId + " and product " + productId));

        if (inventory.getQuantityAvailable() < quantity) {
            throw new IllegalStateException("Insufficient stock at store. Requested: " + quantity + ", Available: " + inventory.getQuantityAvailable());
        }

        inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
        inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void releaseStock(String storeId, String productId, int quantity) {
        inventoryRepository.findByStoreIdAndProductId(storeId, productId).ifPresent(inv -> {
            inv.setQuantityAvailable(inv.getQuantityAvailable() + quantity);
            inv.setQuantityReserved(Math.max(0, inv.getQuantityReserved() - quantity));
            inventoryRepository.save(inv);
        });
    }
}
