package com.platform.ecommerce.category.repository;

import com.platform.ecommerce.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepo extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull(Long parentId); // root categories
    List<Category> findByParentIsNull();
}
