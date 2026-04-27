package com.platform.ecommerce.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {

    private String message;
    private int status;
    private LocalDateTime timestamp;
}
