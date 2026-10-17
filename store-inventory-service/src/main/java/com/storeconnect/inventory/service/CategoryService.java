package com.storeconnect.inventory.service;

import com.storeconnect.inventory.dto.CategoryDTO;
import com.storeconnect.inventory.entity.Category;
import com.storeconnect.inventory.exception.CategoryNotFoundException;
import com.storeconnect.inventory.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(String id) {
        return categoryRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO request) {
        String id = request.id() != null && !request.id().isBlank()
                ? request.id()
                : "CAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Category category = Category.builder()
                .id(id)
                .name(request.name().trim())
                .description(request.description())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created category with ID: {}", saved.getId());
        return toDTO(saved);
    }

    private CategoryDTO toDTO(Category category) {
        return new CategoryDTO(category.getId(), category.getName(), category.getDescription());
    }
}
