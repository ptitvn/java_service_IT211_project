package com.example.hospitalmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// FR-02: Xoay vòng Token
@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Cần có token làm mới")
    private String refreshToken;
}
