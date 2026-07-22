package com.silkroad.market.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.silkroad.market.dto.category.CategoryResponse;
import com.silkroad.market.dto.category.CreateCategoryRequest;
import com.silkroad.market.entity.Category;
import com.silkroad.market.exception.ApiException;
import com.silkroad.market.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {

        String categoryName = request.getName().trim();

        if (categoryRepository.existsByName(categoryName)) {
            throw new ApiException(
                    "Category already exists",
                    HttpStatus.CONFLICT);
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(resolveParentId(request.getParentId()));

        return toResponse(categoryRepository.save(category));
    }

    /**
     * Only top-level categories (no parent) are returned here. Callers
     * that need a category's children should use {@link #getSubcategories}.
     */
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByParentIdIsNull()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CategoryResponse> getSubcategories(Long parentId) {

        if (!categoryRepository.existsById(parentId)) {
            throw new ApiException(
                    "Category not found",
                    HttpStatus.NOT_FOUND);
        }

        return categoryRepository.findByParentId(parentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse updateCategory(Long id, CreateCategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Category not found",
                        HttpStatus.NOT_FOUND));

        String categoryName = request.getName().trim();

        if (categoryRepository.existsByName(categoryName)
                && !category.getName().equalsIgnoreCase(categoryName)) {

            throw new ApiException(
                    "Category already exists",
                    HttpStatus.CONFLICT);
        }

        Long newParentId = resolveParentId(request.getParentId());

        if (newParentId != null && newParentId.equals(id)) {
            throw new ApiException(
                    "A category cannot be its own parent",
                    HttpStatus.BAD_REQUEST);
        }

        category.setName(categoryName);
        category.setParentId(newParentId);

        return toResponse(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Category not found",
                        HttpStatus.NOT_FOUND));

        if (categoryRepository.existsByParentId(id)) {
            throw new ApiException(
                    "Cannot delete a category that has subcategories",
                    HttpStatus.CONFLICT);
        }

        categoryRepository.delete(category);
        // todo: if the category is non-empty (has ads) it should probably stay
    }

    /**
     * validates a requested parentId: the parent must exist and must itself
     * be a top-level category, since only one level of nesting is supported.
     *
     * @param parentId the requested parent id, or null for a top-level category
     * @return the validated parentId, unchanged
     */
    private Long resolveParentId(Long parentId) {

        if (parentId == null) {
            return null;
        }

        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ApiException(
                        "Parent category not found",
                        HttpStatus.NOT_FOUND));

        if (parent.getParentId() != null) {
            throw new ApiException(
                    "Subcategories cannot be nested more than one level deep",
                    HttpStatus.BAD_REQUEST);
        }

        return parentId;
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getParentId(),
                categoryRepository.existsByParentId(category.getId()));
    }

}
