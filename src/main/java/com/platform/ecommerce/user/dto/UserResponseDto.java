package com.platform.ecommerce.user.dto;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto implements Serializable {
    // Add this line
//    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String email;
}
