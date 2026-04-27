package com.platform.ecommerce.product.controller;

import com.platform.ecommerce.category.dto.CategoryReqDto;
import com.platform.ecommerce.product.dto.ProductReqDto;
import com.platform.ecommerce.product.dto.ProductResDto;
import com.platform.ecommerce.product.entity.Product;
import com.platform.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    ProductService productService;

    @PostMapping
    public ProductResDto createProduct(@Valid @RequestBody ProductReqDto product){
        return productService.createProduct(product);
    }

    @GetMapping
    public List<ProductResDto> getAllProduct(){
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResDto getProductById(@PathVariable Long id){
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    public ProductResDto updateProductById(@PathVariable Long id, @RequestBody ProductReqDto prod){
        return productService.updateProduct(id, prod);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id){
        String res = productService.deleteProduct(id);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/deActivate/{id}")
    public ProductResDto deActivateProduct(@PathVariable Long id){
        return productService.deactivateProduct(id);
    }
}
