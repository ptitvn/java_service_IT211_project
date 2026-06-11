package com.example.hospitalmanagement.controller;

import com.example.hospitalmanagement.dto.request.AppointmentStatusRequest;
import com.example.hospitalmanagement.dto.request.CreateUserRequest;
import com.example.hospitalmanagement.dto.request.UpdateUserRequest;
import com.example.hospitalmanagement.dto.response.ApiResponse;
import com.example.hospitalmanagement.dto.response.AppointmentResponse;
import com.example.hospitalmanagement.dto.response.UserResponse;
import com.example.hospitalmanagement.entity.Appointment;
import com.example.hospitalmanagement.entity.User;
import com.example.hospitalmanagement.service.AppointmentService;
import com.example.hospitalmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.hospitalmanagement.dto.request.ResetPasswordRequest;
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AppointmentService appointmentService;

    //  FR-05: Tạo user mới
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    //  FR-05: Lấy danh sách user (tìm kiếm + phân trang)
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) User.Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> users = userService.searchUsers(keyword, role, page, size);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    //  FR-05: Lấy chi tiết 1 user
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("Người dùng được truy xuất thành công", user));
    }

    //  FR-05: Cập nhật user
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật người dùng thành công", user));
    }

    // FR-05: Xoá (deactivate) user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Người dùng đã hủy kích hoạt thành công"));
    }

    //  FR-08: Xem tất cả lịch khám
    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAllAppointments(
            @RequestParam(required = false) Appointment.AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AppointmentResponse> appointments = appointmentService.getAllAppointments(status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy cuộc hẹn thành công", appointments));
    }

    // FR-08: Admin phê duyệt / từ chối lịch khám
    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request,
            Authentication authentication) {
        AppointmentResponse response = appointmentService.updateAppointmentStatus(
                id, authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái cuộc hẹn thành công", response));
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Password reset successfully for user id: " + id));
    }
}
