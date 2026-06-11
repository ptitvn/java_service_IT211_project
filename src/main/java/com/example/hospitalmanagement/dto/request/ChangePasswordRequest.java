package com.example.hospitalmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// FR-10: Đổi mật khẩu (người dùng đã đăng nhập)
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Cần mật khẩu hiện tại")
    private String currentPassword;

    @NotBlank(message = "Cần có mật khẩu mới")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu là bắt buộc")
    private String confirmPassword;
}
