package com.platform.ecommerce.category.serviceImpl;

import com.platform.ecommerce.category.dto.CategoryReqDto;
import com.platform.ecommerce.category.dto.CategoryResDto;
import com.platform.ecommerce.category.entity.Category;
import com.platform.ecommerce.category.repository.CategoryRepo;
import com.platform.ecommerce.category.service.CategoryService;
import com.platform.ecommerce.exceptons.ResourceNotFoundException;
import com.platform.ecommerce.product.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepo repo;

    @Override
    public CategoryResDto createCat(CategoryReqDto category) {
        Category parent = null;

        if (category.getParentID() != null) {
            parent = getCategoryOrThrow(category.getParentID());
        }

        Category cat = Category.builder()
                .name(category.getName())
                .parent(parent)
                .build();

        Category saved = repo.save(cat);

        return mapToResDto(saved);
    }

    @Override
    public CategoryResDto getCatById(Long catId) {
        Category category = getCategoryOrThrow(catId);
        return mapToResDto(category);
    }

    @Override
    public List<CategoryResDto> getCatByParentId(Long parentId) {
        return repo.findByParentIsNull(parentId)
                .stream()
                .map(this::mapToTreeResDto)
                .toList();
    }

    @Override
    public List<CategoryResDto> getAllCategories() {
        return repo.findAll()
                .stream()
                .map(this::mapToTreeResDto)
                .toList();
    }

    @Override
    public List<CategoryResDto> getAllRootCategory() {
        return repo.findByParentIsNull()
                .stream()
                .map(this::mapToResDto)
                .toList();
    }

    @Override
    public CategoryResDto updateCat(Long id, CategoryReqDto category) {
        Category cat = getCategoryOrThrow(id);
        cat.setName(category.getName());
        if(category.getParentID()!=null){
            Category cat2 = getCategoryOrThrow(category.getParentID());
            cat.setParent(cat2);
        }
        Category saved  = repo.save(cat);
        return mapToResDto(saved);
    }

    @Override
    public String deleteCategoryById(Long id) {

        getCategoryOrThrow(id);
        repo.deleteById(id);
        return "Category deleted successfully";
    }

    @Override
    public List<CategoryResDto> getCategoryHierarchy() {
        return repo.findByParentIsNull()
                .stream()
                .map(this::mapToTreeResDto)
                .toList();
    }

    // ===== Helper Functions ======

    private CategoryResDto mapToResDto(Category category) {
        return CategoryResDto.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }
    private CategoryResDto mapToTreeResDto(Category category) {
        return CategoryResDto.builder()
                .id(category.getId())
                .name(category.getName())
                .children(category.getChildren()
                        .stream()
                        .map(this::mapToTreeResDto)
                        .toList())
                .build();
    }


    private Category getCategoryOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}
