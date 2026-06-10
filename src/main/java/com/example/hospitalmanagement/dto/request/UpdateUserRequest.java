package com.example.hospitalmanagement.dto.request;

import com.example.hospitalmanagement.entity.User;
import jakarta.validation.constraints.Email;
import lombok.Data;

// FR-05: Admin cập nhật thông tin user
@Data
public class UpdateUserRequest {

    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    private String fullName;
    private String phone;
    private User.UserStatus status;  // ACTIVE / INACTIVE
}
