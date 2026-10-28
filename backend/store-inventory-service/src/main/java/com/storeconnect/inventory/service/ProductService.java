package com.storeconnect.inventory.service;

import com.storeconnect.inventory.dto.ProductDTO;
import com.storeconnect.inventory.entity.Category;
import com.storeconnect.inventory.entity.Product;
import com.storeconnect.inventory.exception.BadRequestException;
import com.storeconnect.inventory.exception.ResourceNotFoundException;
import com.storeconnect.inventory.repository.CategoryRepository;
import com.storeconnect.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductDTO> searchProducts(String categoryId, String keyword) {
        List<Product> products;
        boolean hasCategory = categoryId != null && !categoryId.isBlank();
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasCategory) {
            products = productRepository.findByIsActiveTrueAndCategoryId(categoryId.trim());
        } else {
            products = productRepository.findByIsActiveTrue();
        }

        if (hasKeyword) {
            String lowerKw = keyword.trim().toLowerCase();
            products = products.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(lowerKw))
                              || (p.getSku() != null && p.getSku().toLowerCase().contains(lowerKw)))
                    .toList();
        }

        Map<String, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        return products.stream()
                .map(p -> toDTO(p, categoryNames.getOrDefault(p.getCategoryId(), "General")))
                .toList();
    }

    public ProductDTO getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseGet(() -> productRepository.findBySkuIgnoreCase(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id)));

        String categoryName = product.getCategoryId() != null
                ? categoryRepository.findById(product.getCategoryId()).map(Category::getName).orElse("General")
                : "General";

        return toDTO(product, categoryName);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO request) {
        if (productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new BadRequestException("Product with SKU already exists: " + request.getSku());
        }

        String id = request.getId() != null && !request.getId().isBlank()
                ? request.getId()
                : "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Product product = new Product(
                id,
                request.getSku().trim().toUpperCase(),
                request.getName().trim(),
                request.getDescription(),
                request.getCategoryId(),
                request.getPrice()
        );

        Product saved = productRepository.save(product);

        String categoryName = saved.getCategoryId() != null
                ? categoryRepository.findById(saved.getCategoryId()).map(Category::getName).orElse("General")
                : "General";

        return toDTO(saved, categoryName);
    }

    @Transactional
    public ProductDTO updateProduct(String id, ProductDTO request) {
        Product product = productRepository.findById(id)
                .orElseGet(() -> productRepository.findBySkuIgnoreCase(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id)));

        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription().trim());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            product.setCategoryId(request.getCategoryId().trim());
        }

        Product updated = productRepository.save(product);

        String categoryName = updated.getCategoryId() != null
                ? categoryRepository.findById(updated.getCategoryId()).map(Category::getName).orElse("General")
                : "General";

        return toDTO(updated, categoryName);
    }

    public ProductDTO toDTO(Product p, String categoryName) {
        return new ProductDTO(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getDescription(),
                p.getCategoryId(),
                categoryName,
                p.getPrice(),
                p.getIsActive()
        );
    }
}
