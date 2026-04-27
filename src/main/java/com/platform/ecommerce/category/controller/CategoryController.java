package com.platform.ecommerce.category.controller;

import com.platform.ecommerce.category.dto.CategoryReqDto;
import com.platform.ecommerce.category.dto.CategoryResDto;
import com.platform.ecommerce.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @PostMapping
    public CategoryResDto create(@Valid @RequestBody CategoryReqDto dto) {
        return categoryService.createCat(dto);
    }

    @GetMapping("/{id}")
    public CategoryResDto get(@PathVariable Long id) {
        return categoryService.getCatById(id);
    }

    @GetMapping()
    public List<CategoryResDto> getAllCat() {
        return categoryService.getAllCategories();
    }
    @GetMapping("/roots")
    public List<CategoryResDto> getRoots() {
        return categoryService.getAllRootCategory();
    }

    @GetMapping("/hierarchy")
    public List<CategoryResDto> getHierarchy() {
        return categoryService.getCategoryHierarchy();
    }

    @PutMapping("/{id}")
    public CategoryResDto update(@PathVariable Long id,
                                      @Valid @RequestBody CategoryReqDto dto) {
        return categoryService.updateCat(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        String res = categoryService.deleteCategoryById(id);
        return ResponseEntity.ok(res);
    }
}
