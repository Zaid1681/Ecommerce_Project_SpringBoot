package com.platform.ecommerce.product.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Long categoryId;
    private Boolean isActive;
}
