package com.example.hospitalmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// FR-04: Đăng ký tài khoản Bệnh nhân mới
@Data
public class RegisterRequest {

    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải có từ 4-50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    @NotBlank(message = "Tên đầy đủ là bắt buộc")
    private String fullName;

    private String phone;
}
