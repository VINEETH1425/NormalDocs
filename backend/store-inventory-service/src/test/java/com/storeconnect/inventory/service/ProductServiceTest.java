package com.storeconnect.inventory.service;

import com.storeconnect.inventory.dto.ProductDTO;
import com.storeconnect.inventory.entity.Category;
import com.storeconnect.inventory.entity.Product;
import com.storeconnect.inventory.exception.BadRequestException;
import com.storeconnect.inventory.exception.ResourceNotFoundException;
import com.storeconnect.inventory.repository.CategoryRepository;
import com.storeconnect.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category("CAT-PRODUCE", "Fresh Produce", "Fresh fruits and vegetables");
        product = new Product(
                "PROD-APPLE-01",
                "PROD-APPLE-01",
                "Shimla Royal Gala Apples (1kg)",
                "Crisp and sweet fresh apples sourced from Himachal orchards",
                "CAT-PRODUCE",
                new BigDecimal("180.00")
        );
    }

    @Test
    @DisplayName("searchProducts: returns filtered list with category names")
    void testSearchProducts() {
        when(productRepository.findByIsActiveTrue()).thenReturn(List.of(product));
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<ProductDTO> results = productService.searchProducts(null, "Shimla");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Shimla Royal Gala Apples (1kg)");
        assertThat(results.get(0).getCategoryName()).isEqualTo("Fresh Produce");
    }

    @Test
    @DisplayName("getProductById: returns product DTO when found by id")
    void testGetProductById_Success() {
        when(productRepository.findById("PROD-APPLE-01")).thenReturn(Optional.of(product));
        when(categoryRepository.findById("CAT-PRODUCE")).thenReturn(Optional.of(category));

        ProductDTO result = productService.getProductById("PROD-APPLE-01");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("PROD-APPLE-01");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("180.00"));
    }

    @Test
    @DisplayName("getProductById: throws ResourceNotFoundException when product does not exist")
    void testGetProductById_NotFound() {
        when(productRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());
        when(productRepository.findBySkuIgnoreCase("NONEXISTENT")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById("NONEXISTENT"));
    }

    @Test
    @DisplayName("createProduct: successfully creates and returns new product DTO")
    void testCreateProduct_Success() {
        ProductDTO newDto = new ProductDTO(
                null,
                "PROD-MANGO-01",
                "Alphonso Mangoes (1kg)",
                "Ratnagiri sweet mangoes",
                "CAT-PRODUCE",
                null,
                new BigDecimal("350.00"),
                true
        );

        when(productRepository.existsBySkuIgnoreCase("PROD-MANGO-01")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(categoryRepository.findById("CAT-PRODUCE")).thenReturn(Optional.of(category));

        ProductDTO created = productService.createProduct(newDto);

        assertThat(created).isNotNull();
        assertThat(created.getSku()).isEqualTo("PROD-MANGO-01");
        assertThat(created.getName()).isEqualTo("Alphonso Mangoes (1kg)");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct: duplicate SKU throws BadRequestException")
    void testCreateProduct_DuplicateSku() {
        ProductDTO newDto = new ProductDTO(
                null,
                "PROD-APPLE-01",
                "Apples",
                null,
                "CAT-PRODUCE",
                null,
                new BigDecimal("180.00"),
                true
        );

        when(productRepository.existsBySkuIgnoreCase("PROD-APPLE-01")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> productService.createProduct(newDto));
        verify(productRepository, never()).save(any());
    }
}

