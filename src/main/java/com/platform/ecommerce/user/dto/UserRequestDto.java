package com.platform.ecommerce.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message ="Invalid email format")
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
            message = "Email must be valid"
    )
//    @Pattern(
//            regexp = "^(?=.*[A-Za-z])[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
//            message = "Email must contain at least one alphabet before @"
//    )
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 6 , message="Password must be at least 6 alphnumeric characters")
    private String password;
}
