package com.example.hospitalmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// FR-10: Admin reset mật khẩu cho user bất kỳ
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Cần có mật khẩu mới")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String newPassword;
}