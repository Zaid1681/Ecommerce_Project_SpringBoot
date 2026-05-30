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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    @Autowired
    ProductRepository repo;

    @Autowired
    CategoryRepo catRepo;

    @Override
    public ProductResDto createProduct(ProductReqDto product) {
        log.info("createProduct called for productName={}", product.getProductName());

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
        log.info("Product created with id={}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public ProductResDto getProductById(Long id) {
        log.info("getProductById called for id={}", id);
        Product saved = getProductOrThrow(id);
        return mapToResponse(saved);
    }

    @Override
    public List<ProductResDto> getAllProducts() {
        log.info("getAllProducts called");
        List<ProductResDto> products = repo.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
        log.info("getAllProducts returned {} products", products.size());
        return products;
    }

    @Override
    public ProductResDto updateProduct(Long id, ProductReqDto product) {
        log.info("updateProduct called for id={}", id);

        Category cat = catRepo.findById(product.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product prod = getProductOrThrow(id);
        prod.setProductName(product.getProductName());
        prod.setDescription(product.getDescription());
        prod.setPrice(product.getPrice());
        prod.setCategory(cat);
        Product saved = repo.save(prod);
        log.info("Product updated id={}", id);
        return mapToResponse(saved);

    }

    @Override
    public String deleteProduct(Long id) {
        log.info("deleteProduct called for id={}", id);
        Product prod = getProductOrThrow(id);
        repo.deleteById(id);
        log.info("Product deleted id={}", id);
        return "Product Deleted Success";
    }

    @Override
    public ProductResDto deactivateProduct(Long id) {
        log.info("deactivateProduct called for id={}", id);
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
