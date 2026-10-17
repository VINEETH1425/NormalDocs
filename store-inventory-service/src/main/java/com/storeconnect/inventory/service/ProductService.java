package com.storeconnect.inventory.service;

import com.storeconnect.inventory.dto.ProductDTO;
import com.storeconnect.inventory.entity.Category;
import com.storeconnect.inventory.entity.Product;
import com.storeconnect.inventory.exception.DuplicateSkuException;
import com.storeconnect.inventory.exception.ProductNotFoundException;
import com.storeconnect.inventory.repository.CategoryRepository;
import com.storeconnect.inventory.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String categoryId, String keyword) {
        List<Product> products;
        boolean hasCategory = categoryId != null && !categoryId.isBlank();
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (!hasCategory && !hasKeyword) {
            products = productRepository.findByIsActiveTrue();
        } else if (hasCategory && !hasKeyword) {
            products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId.trim());
        } else {
            products = productRepository.searchProducts(
                    hasCategory ? categoryId.trim() : null,
                    hasKeyword ? keyword.trim() : null
            );
        }

        Map<String, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        return products.stream()
                .map(p -> toDTO(p, categoryNames.getOrDefault(p.getCategoryId(), "Unknown")))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseGet(() -> productRepository.findBySkuIgnoreCase(id)
                        .orElseThrow(() -> new ProductNotFoundException(id)));

        String categoryName = product.getCategoryId() != null
                ? categoryRepository.findById(product.getCategoryId()).map(Category::getName).orElse("Unknown")
                : null;

        return toDTO(product, categoryName);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO request) {
        if (productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }

        String id = request.id() != null && !request.id().isBlank()
                ? request.id()
                : "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Product product = Product.builder()
                .id(id)
                .sku(request.sku().trim().toUpperCase())
                .name(request.name().trim())
                .description(request.description())
                .categoryId(request.categoryId())
                .price(request.price())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product: {} ({})", saved.getName(), saved.getSku());

        String categoryName = saved.getCategoryId() != null
                ? categoryRepository.findById(saved.getCategoryId()).map(Category::getName).orElse(null)
                : null;

        return toDTO(saved, categoryName);
    }

    @Transactional
    public ProductDTO updateProduct(String id, ProductDTO request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (!product.getSku().equalsIgnoreCase(request.sku()) && productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }

        product.setSku(request.sku().trim().toUpperCase());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setCategoryId(request.categoryId());
        product.setPrice(request.price());
        if (request.isActive() != null) {
            product.setIsActive(request.isActive());
        }

        Product updated = productRepository.save(product);
        log.info("Updated product: {}", updated.getId());

        String categoryName = updated.getCategoryId() != null
                ? categoryRepository.findById(updated.getCategoryId()).map(Category::getName).orElse(null)
                : null;

        return toDTO(updated, categoryName);
    }

    @Transactional
    public void deactivateProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setIsActive(false);
        productRepository.save(product);
        log.info("Deactivated product: {}", id);
    }

    public ProductDTO toDTO(Product product, String categoryName) {
        return new ProductDTO(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategoryId(),
                categoryName,
                product.getPrice(),
                product.getIsActive()
        );
    }
}
