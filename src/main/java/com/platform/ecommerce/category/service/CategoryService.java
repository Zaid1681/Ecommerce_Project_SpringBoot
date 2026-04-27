package com.platform.ecommerce.category.service;

import com.platform.ecommerce.category.dto.CategoryReqDto;
import com.platform.ecommerce.category.dto.CategoryResDto;

import java.util.List;

public interface CategoryService {

    CategoryResDto createCat(CategoryReqDto category);
    CategoryResDto getCatById(Long catId);
    List<CategoryResDto> getCatByParentId(Long parentId);
    List<CategoryResDto> getAllCategories();
    List<CategoryResDto> getAllRootCategory();
    CategoryResDto updateCat(Long id, CategoryReqDto category);
    String deleteCategoryById(Long id);
    List<CategoryResDto> getCategoryHierarchy();

}
