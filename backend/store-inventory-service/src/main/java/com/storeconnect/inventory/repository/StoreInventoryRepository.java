package com.storeconnect.inventory.repository;

import com.storeconnect.inventory.entity.StoreInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreInventoryRepository extends JpaRepository<StoreInventory, String> {
    Optional<StoreInventory> findByStoreIdAndProductId(String storeId, String productId);
}
