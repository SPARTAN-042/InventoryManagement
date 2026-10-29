package com.example.inventorymanagement.service;

import com.example.inventorymanagement.dto.category.CategoryRequest;
import com.example.inventorymanagement.dto.category.CategoryResponse;
import com.example.inventorymanagement.entity.Category;
import com.example.inventorymanagement.exception.ResourceConflictException;
import com.example.inventorymanagement.exception.ResourceNotFoundException;
import com.example.inventorymanagement.repository.CategoryRepository;
import com.example.inventorymanagement.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_savesCategorySuccessfully() {
        CategoryRequest request = new CategoryRequest(
                "Electronics",
                "Electronic devices"
        );

        when(categoryRepository.existsByName("Electronics"))
                .thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response =
                categoryService.createCategory(request);

        assertEquals("Electronics", response.name());
        assertEquals("Electronic devices", response.description());

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_rejectsDuplicateName() {
        CategoryRequest request = new CategoryRequest(
                "Electronics",
                "Electronic devices"
        );

        when(categoryRepository.existsByName("Electronics"))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> categoryService.createCategory(request)
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void getCategoryById_whenMissing_throwsNotFound() {
        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(999L)
        );
    }

    @Test
    void deleteCategory_rejectsCategoryWithProducts() {
        Category category = sampleCategory();

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(productRepository.existsByCategory_Id(1L))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> categoryService.deleteCategory(1L)
        );

        verify(categoryRepository, never())
                .delete(any(Category.class));
    }

    @Test
    void deleteCategory_deletesUnusedCategory() {
        Category category = sampleCategory();

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(productRepository.existsByCategory_Id(1L))
                .thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    private Category sampleCategory() {
        Category category = new Category();
        category.setName("Electronics");
        category.setDescription("Electronic devices");

        return category;
    }
}