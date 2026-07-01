package com.silkroad.market.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

    public Category createCategory(CreateCategoryRequest request) {

        String categoryName = request.getName().trim();

        if (categoryRepository.existsByName(categoryName)) {
            throw new ApiException(
                    "Category already exists",
                    HttpStatus.CONFLICT);
        }

        Category category = new Category();
        category.setName(categoryName);

        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

}