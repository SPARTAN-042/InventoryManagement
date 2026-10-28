package com.example.inventorymanagement.service;

import com.example.inventorymanagement.dto.category.CategoryRequest;
import com.example.inventorymanagement.dto.category.CategoryResponse;
import com.example.inventorymanagement.entity.Category;
import com.example.inventorymanagement.exception.ResourceConflictException;
import com.example.inventorymanagement.exception.ResourceNotFoundException;
import com.example.inventorymanagement.repository.CategoryRepository;
import com.example.inventorymanagement.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new ResourceConflictException(
                    "Category name already exists: " + request.name());
        }

        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());

        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        return toResponse(findCategory(id));
    }

    public CategoryResponse updateCategory(
            Long id, CategoryRequest request) {
        Category category = findCategory(id);

        if (categoryRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new ResourceConflictException(
                    "Category name already exists: " + request.name());
        }

        category.setName(request.name());
        category.setDescription(request.description());

        return toResponse(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        Category category = findCategory(id);

        if (productRepository.existsByCategory_Id(id)) {
            throw new ResourceConflictException(
                    "Cannot delete category that still contains products"
            );
        }
        categoryRepository.delete(category);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

}
