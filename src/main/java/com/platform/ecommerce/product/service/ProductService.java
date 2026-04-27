package com.platform.ecommerce.product.service;

import com.platform.ecommerce.product.dto.ProductReqDto;
import com.platform.ecommerce.product.dto.ProductResDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProductService {
    public ProductResDto createProduct(ProductReqDto product);
    public ProductResDto getProductById(Long id);
    public List<ProductResDto> getAllProducts();
    public ProductResDto updateProduct(Long id , ProductReqDto product);
    public String deleteProduct(Long id);
    public ProductResDto deactivateProduct(Long id);

}
