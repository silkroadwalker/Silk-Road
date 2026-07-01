package com.silkroad.market.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.silkroad.market.dto.category.CreateCategoryRequest;
import com.silkroad.market.entity.Category;
import com.silkroad.market.service.CategoryService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Category> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {

        Category category = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(category);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

}