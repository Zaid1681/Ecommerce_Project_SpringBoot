package com.platform.ecommerce.product.serviceImpl;
import com.platform.ecommerce.category.entity.Category;
import com.platform.ecommerce.category.repository.CategoryRepo;
import com.platform.ecommerce.exceptons.DuplicateResourceException;
import com.platform.ecommerce.exceptons.ResourceNotFoundException;
import com.platform.ecommerce.product.dto.ProductReqDto;
import com.platform.ecommerce.product.dto.ProductResDto;
import com.platform.ecommerce.product.entity.Product;
import com.platform.ecommerce.product.repository.ProductRepository;
import com.platform.ecommerce.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductRepository repo;

    @Autowired
    CategoryRepo catRepo;

    @Override
    public ProductResDto createProduct(ProductReqDto product) {

        // 1. Category must exist
        Category cat = catRepo.findById(product.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // 2. Check duplicate product name
        if (repo.existsByProductName(product.getProductName())) {
            throw new DuplicateResourceException("Product already exists");
        }

        // 3. Create product
        Product prod  = Product.builder()
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(cat)
                .isActive(true)
                .build();
        Product saved = repo.save(prod);
        return mapToResponse(saved);
    }

    @Override
    public ProductResDto getProductById(Long id) {
        Product saved = getProductOrThrow(id);
        return mapToResponse(saved);
    }

    @Override
    public List<ProductResDto> getAllProducts() {
        return repo.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ProductResDto updateProduct(Long id, ProductReqDto product) {

        Category cat = catRepo.findById(product.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product prod = getProductOrThrow(id);
        prod.setProductName(product.getProductName());
        prod.setDescription(product.getDescription());
        prod.setPrice(product.getPrice());
        prod.setCategory(cat);
        Product saved = repo.save(prod);
        return mapToResponse(saved);

    }

    @Override
    public String deleteProduct(Long id) {
        Product prod = getProductOrThrow(id);
        repo.deleteById(id);
        return "Product Deleted Success";
    }

    @Override
    public ProductResDto deactivateProduct(Long id) {
        Product prod = getProductOrThrow(id);
        prod.setIsActive(false);
        repo.save(prod);
        return mapToResponse(prod);
    }

    private Product getProductOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public ProductResDto mapToResponse(Product product){
        return ProductResDto.builder()
                .id(product.getId())
                .name(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategory().getId())
                .isActive(product.getIsActive())
                .build();
    }
}
