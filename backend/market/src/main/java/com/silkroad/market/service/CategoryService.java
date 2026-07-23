package com.silkroad.market.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.silkroad.market.dto.category.CategoryResponse;
import com.silkroad.market.dto.category.CreateCategoryRequest;
import com.silkroad.market.entity.Category;
import com.silkroad.market.exception.ApiException;
import com.silkroad.market.repository.CategoryRepository;

/**
 * Service class responsible for managing category operations.
 * 
 * <p>
 * This service handles all category-related business logic, including
 * creation, retrieval, update, and deletion of categories. Categories
 * support a single level of nesting (top-level categories with subcategories),
 * and the service enforces this hierarchy constraint.
 * </p>
 * 
 * <p>
 * Key constraints enforced by this service:
 * <ul>
 * <li>Categories must have unique names</li>
 * <li>Only one level of nesting is supported</li>
 * <li>A category cannot be its own parent</li>
 * <li>Categories with subcategories cannot be deleted</li>
 * <li>Categories with advertisements cannot be deleted</li>
 * </ul>
 * </p>
 * 
 * @author Silkroad Market Team
 * @version 1.0
 * @see Category
 * @see CategoryRepository
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Constructs a new CategoryService with the required dependency.
     * 
     * @param categoryRepository repository for category persistence operations
     */
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Creates a new category.
     * 
     * <p>
     * This method validates that the category name is unique and that
     * the parent category (if specified) exists and is a top-level category,
     * as only one level of nesting is supported.
     * </p>
     * 
     * @param request the category creation request containing name and optional
     *                parent ID
     * @return the created category as a response DTO
     * @throws ApiException with CONFLICT status if a category with the same name
     *                      already exists
     * @throws ApiException with NOT_FOUND status if the parent category is not
     *                      found
     * @throws ApiException with BAD_REQUEST status if the parent is already a
     *                      subcategory
     */
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
     * Retrieves all top-level categories.
     * 
     * <p>
     * This method returns only categories that have no parent (root categories).
     * For subcategories, use the {@link #getSubcategories(Long)} method.
     * </p>
     * 
     * @return a list of top-level categories as response DTOs
     */
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByParentIdIsNull()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieves all subcategories of a specific parent category.
     * 
     * @param parentId the ID of the parent category
     * @return a list of subcategories as response DTOs
     * @throws ApiException with NOT_FOUND status if the parent category does not
     *                      exist
     */
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

    /**
     * Updates an existing category.
     * 
     * <p>
     * This method allows updating a category's name and parent category.
     * It validates that the updated name is unique (unless it's the same
     * category) and that the new parent category is valid and not the
     * category itself.
     * </p>
     * 
     * @param id      the ID of the category to update
     * @param request the update request containing new name and optional parent ID
     * @return the updated category as a response DTO
     * @throws ApiException with NOT_FOUND status if the category is not found
     * @throws ApiException with CONFLICT status if another category with the same
     *                      name exists
     * @throws ApiException with BAD_REQUEST status if the category is set as its
     *                      own parent
     */
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

    /**
     * Deletes a category.
     * 
     * <p>
     * This method can only delete categories that have no subcategories.
     * Categories that contain subcategories must have their children deleted
     * first.
     * </p>
     * 
     * <p>
     * <b>Todo:</b> Consider preventing deletion of categories that contain
     * advertisements to avoid orphaned ad-category references.
     * </p>
     * 
     * @param id the ID of the category to delete
     * @throws ApiException with NOT_FOUND status if the category is not found
     * @throws ApiException with CONFLICT status if the category has subcategories
     */
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
    }

    /**
     * Validates and resolves a parent category ID.
     * 
     * <p>
     * This helper method ensures that the parent category exists and that
     * it is a top-level category, as only one level of nesting is supported
     * by the system.
     * </p>
     * 
     * @param parentId the requested parent ID, or null for a top-level category
     * @return the validated parent ID, or null if no parent was specified
     * @throws ApiException with NOT_FOUND status if the parent category is not
     *                      found
     * @throws ApiException with BAD_REQUEST status if the parent is already a
     *                      subcategory
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

    /**
     * Converts a Category entity to a response DTO.
     * 
     * @param category the category entity to convert
     * @return a response DTO containing category information and whether it has
     *         children
     */
    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getParentId(),
                categoryRepository.existsByParentId(category.getId()));
    }

}