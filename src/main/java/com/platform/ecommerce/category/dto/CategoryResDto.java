package com.platform.ecommerce.category.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryResDto {

    private Long id;
    private String name;
    private Long parentId;
    private List<CategoryResDto> children;

}
