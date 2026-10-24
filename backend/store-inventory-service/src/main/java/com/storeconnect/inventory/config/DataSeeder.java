package com.storeconnect.inventory.config;

import com.storeconnect.inventory.entity.Category;
import com.storeconnect.inventory.entity.Product;
import com.storeconnect.inventory.entity.Store;
import com.storeconnect.inventory.entity.StoreInventory;
import com.storeconnect.inventory.repository.CategoryRepository;
import com.storeconnect.inventory.repository.ProductRepository;
import com.storeconnect.inventory.repository.StoreInventoryRepository;
import com.storeconnect.inventory.repository.StoreRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final StoreInventoryRepository inventoryRepository;

    public DataSeeder(CategoryRepository categoryRepository,
                      StoreRepository storeRepository,
                      ProductRepository productRepository,
                      StoreInventoryRepository inventoryRepository) {
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    new Category("CAT-PRODUCE", "Fresh Produce", "Organic fruits and farm vegetables"),
                    new Category("CAT-DAIRY", "Dairy & Bakery", "Milk, artisanal cheese, bread, eggs"),
                    new Category("CAT-BEVERAGES", "Beverages", "Juices, organic tea, craft kombucha"),
                    new Category("CAT-SNACKS", "Packaged Snacks", "Healthy roasted snacks, nuts, chips")
            ));
        }

        if (storeRepository.count() == 0) {
            storeRepository.saveAll(List.of(
                    new Store("STORE-GGN-01", "DLF CyberHub SuperStore", "GGN-01", "Ground Floor, CyberHub", "Gurugram", "+91 124 4567890"),
                    new Store("STORE-DEL-01", "Connaught Place Flagship", "DEL-01", "Block B, Inner Circle, CP", "New Delhi", "+91 11 23415678"),
                    new Store("STORE-BLR-01", "Indiranagar Experience Hub", "BLR-01", "100 Feet Road, HAL 2nd Stage", "Bengaluru", "+91 80 41235678")
            ));
        }

        if (productRepository.count() == 0) {
            List<Product> products = List.of(
                    new Product("PROD-001", "SKU-APP-001", "Shimla Royal Apple (1kg)", "Crisp, hand-picked sweet mountain apples", "CAT-PRODUCE", new BigDecimal("180.00")),
                    new Product("PROD-002", "SKU-MILK-001", "A2 Farm Fresh Whole Milk (1L)", "Pure unadulterated cold-chain farm milk", "CAT-DAIRY", new BigDecimal("75.00")),
                    new Product("PROD-003", "SKU-COF-001", "Coorg Dark Roast Coffee Beans (250g)", "Single-estate shade-grown Arabica beans", "CAT-BEVERAGES", new BigDecimal("350.00")),
                    new Product("PROD-004", "SKU-ALM-001", "California Jumbo Roasted Almonds (500g)", "Slow roasted, lightly salted premium almonds", "CAT-SNACKS", new BigDecimal("499.00"))
            );
            productRepository.saveAll(products);

            for (Store s : storeRepository.findAll()) {
                for (Product p : products) {
                    if (inventoryRepository.findByStoreIdAndProductId(s.getId(), p.getId()).isEmpty()) {
                        inventoryRepository.save(new StoreInventory(
                                "INV-" + s.getCode() + "-" + p.getSku(),
                                s.getId(),
                                p.getId(),
                                25,
                                0,
                                5
                        ));
                    }
                }
            }
        }
    }
}
