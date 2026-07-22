package com.silkroad.market.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.silkroad.market.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    List<Category> findByParentIdIsNull();

    List<Category> findByParentId(Long parentId);

    boolean existsByParentId(Long parentId);
}