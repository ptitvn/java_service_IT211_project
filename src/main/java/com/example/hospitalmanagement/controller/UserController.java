package com.example.hospitalmanagement.controller;

import com.example.hospitalmanagement.dto.request.ChangePasswordRequest;
import com.example.hospitalmanagement.dto.response.ApiResponse;
import com.example.hospitalmanagement.dto.response.MedicalRecordResponse;
import com.example.hospitalmanagement.service.MedicalRecordService;
import com.example.hospitalmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Các API dùng chung cho tất cả role đã đăng nhập.
 * FR-10: Đổi mật khẩu
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MedicalRecordService medicalRecordService;

    // ─── FR-10: Đổi mật khẩu ──────────────────────────────────────────────────

    /**
     * POST /api/v1/users/change-password
     * Header: Authorization: Bearer <token>
     * Body: { "currentPassword": "...", "newPassword": "...", "confirmPassword": "..." }
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    // ─── Bệnh nhân xem hồ sơ của mình ────────────────────────────────────────

    /**
     * GET /api/v1/users/records
     * Header: Authorization: Bearer <patient_token>
     */
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getMyRecords(
            Authentication authentication) {
        List<MedicalRecordResponse> records = medicalRecordService
                .getMyRecords(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Records retrieved successfully", records));
    }
}
