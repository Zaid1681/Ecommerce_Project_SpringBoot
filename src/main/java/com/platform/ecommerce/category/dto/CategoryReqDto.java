package com.platform.ecommerce.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryReqDto {

    @NotBlank
    private String name;
    private Long parentID;
}
