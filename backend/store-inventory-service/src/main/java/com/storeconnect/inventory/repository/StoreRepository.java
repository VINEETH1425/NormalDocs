package com.storeconnect.inventory.repository;

import com.storeconnect.inventory.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    List<Store> findByIsActiveTrue();
    List<Store> findByIsActiveTrueAndCityIgnoreCase(String city);
}
