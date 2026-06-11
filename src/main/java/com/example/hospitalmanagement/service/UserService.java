package com.example.hospitalmanagement.service;

import com.example.hospitalmanagement.dto.request.ChangePasswordRequest;
import com.example.hospitalmanagement.dto.request.CreateUserRequest;
import com.example.hospitalmanagement.dto.request.ResetPasswordRequest;
import com.example.hospitalmanagement.dto.request.UpdateUserRequest;
import com.example.hospitalmanagement.dto.response.UserResponse;
import com.example.hospitalmanagement.entity.User;
import org.springframework.data.domain.Page;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(Long id);
    Page<UserResponse> searchUsers(String keyword, User.Role role, int page, int size);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);

    // FR-10: Đổi mật khẩu (người dùng tự đổi)
    void changePassword(String username, ChangePasswordRequest request);

    // FR-10: Admin reset mật khẩu hộ user
    void resetPassword(Long userId, ResetPasswordRequest request);
}