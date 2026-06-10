package com.example.hospitalmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// FR-01: Đăng nhập
@Data
public class LoginRequest {
    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    private String username;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    private String password;
}
