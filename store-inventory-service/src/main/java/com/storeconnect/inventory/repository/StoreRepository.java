package com.storeconnect.inventory.repository;

import com.storeconnect.inventory.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    Optional<Store> findByCodeIgnoreCase(String code);
    List<Store> findByIsActiveTrue();
    List<Store> findByCityIgnoreCaseAndIsActiveTrue(String city);
}
