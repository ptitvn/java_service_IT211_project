package com.example.hospitalmanagement.dto.request;

import com.example.hospitalmanagement.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// FR-05: Admin tạo user mới (Doctor hoặc Patient)
@Data
public class CreateUserRequest {

    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải có từ 4-50 ký tự")
    private String username;

    @NotBlank(message = "Cần mật khẩu")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    @NotBlank(message = "Tên đầy đủ là bắt buộc")
    private String fullName;

    private String phone;

    @NotNull(message = "Vai trò là bắt buộc")
    private User.Role role;    // ADMIN, DOCTOR, PATIENT
}
