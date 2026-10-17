package com.storeconnect.inventory.service;

import com.storeconnect.inventory.dto.*;
import com.storeconnect.inventory.entity.StoreInventory;
import com.storeconnect.inventory.exception.InsufficientStockException;
import com.storeconnect.inventory.kafka.InventoryEventProducer;
import com.storeconnect.inventory.repository.ProductRepository;
import com.storeconnect.inventory.repository.StoreInventoryRepository;
import com.storeconnect.inventory.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private StoreInventoryRepository inventoryRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryEventProducer eventProducer;

    @InjectMocks
    private InventoryService inventoryService;

    private StoreInventory testInventory;

    @BeforeEach
    void setUp() {
        testInventory = StoreInventory.builder()
                .id("INV-001")
                .storeId("STORE-01")
                .productId("PROD-01")
                .quantityAvailable(10)
                .quantityReserved(2)
                .reorderThreshold(5)
                .build();
    }

    @Test
    @DisplayName("Stock validation succeeds when sufficient stock is available")
    void testValidateStock_Success() {
        when(inventoryRepository.findByStoreIdAndProductId("STORE-01", "PROD-01"))
                .thenReturn(Optional.of(testInventory));

        StockValidateRequest request = new StockValidateRequest("STORE-01", List.of(
                new StockValidateItem("PROD-01", 3)
        ));

        StockValidateResponse response = inventoryService.validateStock(request);
        assertTrue(response.isAvailable());
        assertTrue(response.issues().isEmpty());
    }

    @Test
    @DisplayName("Stock validation fails when requested quantity exceeds available stock")
    void testValidateStock_Insufficient() {
        when(inventoryRepository.findByStoreIdAndProductId("STORE-01", "PROD-01"))
                .thenReturn(Optional.of(testInventory));

        StockValidateRequest request = new StockValidateRequest("STORE-01", List.of(
                new StockValidateItem("PROD-01", 15)
        ));

        StockValidateResponse response = inventoryService.validateStock(request);
        assertFalse(response.isAvailable());
        assertEquals(1, response.issues().size());
        assertEquals("Insufficient stock", response.issues().get(0).message());
    }

    @Test
    @DisplayName("Reserve stock decrements available and increments reserved quantity")
    void testReserveStock_Success() {
        when(inventoryRepository.findByStoreIdAndProductId("STORE-01", "PROD-01"))
                .thenReturn(Optional.of(testInventory));

        StockReservationRequest request = new StockReservationRequest("ORD-100", "STORE-01", List.of(
                new StockValidateItem("PROD-01", 4)
        ));

        inventoryService.reserveStock(request);

        assertEquals(6, testInventory.getQuantityAvailable());
        assertEquals(6, testInventory.getQuantityReserved());
        verify(inventoryRepository, times(1)).save(testInventory);
    }

    @Test
    @DisplayName("Reserve stock throws InsufficientStockException when quantity is deficient")
    void testReserveStock_ThrowsException() {
        when(inventoryRepository.findByStoreIdAndProductId("STORE-01", "PROD-01"))
                .thenReturn(Optional.of(testInventory));

        StockReservationRequest request = new StockReservationRequest("ORD-100", "STORE-01", List.of(
                new StockValidateItem("PROD-01", 20)
        ));

        assertThrows(InsufficientStockException.class, () -> inventoryService.reserveStock(request));
        verify(inventoryRepository, never()).save(testInventory);
    }
}
