package com.storeconnect.inventory.repository;

import com.storeconnect.inventory.entity.StoreInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreInventoryRepository extends JpaRepository<StoreInventory, String> {
    Optional<StoreInventory> findByStoreIdAndProductId(String storeId, String productId);
    List<StoreInventory> findByStoreId(String storeId);
    List<StoreInventory> findByProductId(String productId);

    @Query("""
        SELECT si FROM StoreInventory si
        WHERE si.quantityAvailable <= si.reorderThreshold
    """)
    List<StoreInventory> findLowStockInventories();

    @Query("""
        SELECT si FROM StoreInventory si
        WHERE si.productId = :productId AND si.quantityAvailable > 0
    """)
    List<StoreInventory> findStoresWithAvailableStock(@Param("productId") String productId);
}
