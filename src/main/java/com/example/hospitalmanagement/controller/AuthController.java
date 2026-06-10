package com.example.hospitalmanagement.controller;

import com.example.hospitalmanagement.dto.request.LoginRequest;
import com.example.hospitalmanagement.dto.request.RefreshTokenRequest;
import com.example.hospitalmanagement.dto.request.RegisterRequest;
import com.example.hospitalmanagement.dto.response.ApiResponse;
import com.example.hospitalmanagement.dto.response.AuthResponse;
import com.example.hospitalmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // FR-01: Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Đăng nhập thành công", authResponse));
    }

    // FR-04: Đăng ký Bệnh nhân
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký thành công", authResponse));
    }

    // FR-02: Refresh Token

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(
                ApiResponse.success("Đã làm mới token thành công", authResponse));
    }

    // FR-03: Đăng xuất
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {

        // Tách "Bearer " prefix
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7) : authHeader;

        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công"));
    }
}
