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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    @Autowired
    ProductService productService;

    @PostMapping
    public ProductResDto createProduct(@Valid @RequestBody ProductReqDto product){
        log.info("HTTP POST /api/products called for productName={}", product.getProductName());
        return productService.createProduct(product);
    }

    @GetMapping
    public List<ProductResDto> getAllProduct(){
        log.info("HTTP GET /api/products called");
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResDto getProductById(@PathVariable Long id){
        log.info("HTTP GET /api/products/{} called", id);
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    public ProductResDto updateProductById(@PathVariable Long id, @RequestBody ProductReqDto prod){
        log.info("HTTP PUT /api/products/{} called", id);
        return productService.updateProduct(id, prod);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id){
        log.info("HTTP DELETE /api/products/{} called", id);
        String res = productService.deleteProduct(id);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/deActivate/{id}")
    public ProductResDto deActivateProduct(@PathVariable Long id){
        log.info("HTTP PUT /api/products/deActivate/{} called", id);
        return productService.deactivateProduct(id);
    }
}
