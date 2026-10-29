package com.example.inventorymanagement.service;

import com.example.inventorymanagement.dto.product.ProductRequest;
import com.example.inventorymanagement.dto.product.ProductResponse;
import com.example.inventorymanagement.entity.Category;
import com.example.inventorymanagement.entity.Product;
import com.example.inventorymanagement.exception.ResourceConflictException;
import com.example.inventorymanagement.exception.ResourceNotFoundException;
import com.example.inventorymanagement.repository.CategoryRepository;
import com.example.inventorymanagement.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void createProduct_savesProductSuccessfully() {
        ProductRequest request = new ProductRequest(
                "Mechanical Keyboard",
                "KEY-001",
                "Wired keyboard",
                new BigDecimal("49.99"),
                25,
                1L
        );

        Category category = new Category();
        category.setName("Electronics");

        when(productRepository.existsBySku("KEY-001"))
                .thenReturn(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation ->
                        (Product) invocation.getArgument(0));

        ProductResponse response =
                productService.createProduct(request);

        assertEquals("Mechanical Keyboard", response.name());
        assertEquals("KEY-001", response.sku());
        assertEquals("Electronics", response.categoryName());
        assertEquals(new BigDecimal("49.99"), response.price());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_rejectsDuplicateSku() {
        ProductRequest request = new ProductRequest(
                "Mechanical Keyboard",
                "KEY-001",
                "Wired keyboard",
                new BigDecimal("49.99"),
                25,
                1L
        );

        when(productRepository.existsBySku("KEY-001"))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> productService.createProduct(request)
        );

        verify(productRepository, never())
                .save(any(Product.class));

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void createProduct_rejectsNonexistentCategory() {
        ProductRequest request = new ProductRequest(
                "Mechanical Keyboard",
                "KEY-001",
                "Wired keyboard",
                new BigDecimal("49.99"),
                25,
                999L
        );

        when(productRepository.existsBySku("KEY-001"))
                .thenReturn(false);

        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.createProduct(request)
        );

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void getProductById_returnsProduct() {
        Product product = sampleProduct();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponse response =
                productService.getProductById(1L);

        assertEquals("Old Keyboard", response.name());
        assertEquals("KEY-OLD", response.sku());
        assertEquals("Electronics", response.categoryName());
    }

    @Test
    void getProductById_whenMissing_throwsNotFound() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(999L)
        );
    }

    @Test
    void updateProduct_updatesExistingProduct() {
        Product product = sampleProduct();

        Category newCategory = new Category();
        newCategory.setName("Accessories");

        ProductRequest request = new ProductRequest(
                "New Keyboard",
                "KEY-NEW",
                "Updated keyboard",
                new BigDecimal("59.99"),
                15,
                2L
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.existsBySkuAndIdNot("KEY-NEW", 1L))
                .thenReturn(false);

        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(newCategory));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response =
                productService.updateProduct(1L, request);

        assertEquals("New Keyboard", response.name());
        assertEquals("KEY-NEW", response.sku());
        assertEquals(new BigDecimal("59.99"), response.price());
        assertEquals(15, response.quantity());
        assertEquals("Accessories", response.categoryName());
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_rejectsDuplicateSku() {
        Product product = sampleProduct();

        ProductRequest request = new ProductRequest(
                "Another Keyboard",
                "KEY-EXISTING",
                "Another keyboard",
                new BigDecimal("59.99"),
                10,
                2L
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.existsBySkuAndIdNot(
                "KEY-EXISTING", 1L))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> productService.updateProduct(1L, request)
        );

        verify(productRepository, never())
                .save(any(Product.class));

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void updateProduct_whenMissing_throwsNotFound() {
        ProductRequest request = new ProductRequest(
                "New Keyboard",
                "KEY-NEW",
                "Updated keyboard",
                new BigDecimal("59.99"),
                15,
                1L
        );

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(999L, request)
        );

        verify(productRepository, never())
                .save(any(Product.class));

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void deleteProduct_deletesExistingProduct() {
        Product product = sampleProduct();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_whenMissing_throwsNotFound() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(999L)
        );

        verify(productRepository, never())
                .delete(any(Product.class));
    }

    private Product sampleProduct() {
        Category category = new Category();
        category.setName("Electronics");

        Product product = new Product();
        product.setName("Old Keyboard");
        product.setSku("KEY-OLD");
        product.setDescription("Old description");
        product.setPrice(new BigDecimal("39.99"));
        product.setQuantity(10);
        product.setCategory(category);

        return product;
    }
}