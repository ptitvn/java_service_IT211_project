package com.example.hospitalmanagement.service;

import com.example.hospitalmanagement.dto.request.LoginRequest;
import com.example.hospitalmanagement.dto.request.RefreshTokenRequest;
import com.example.hospitalmanagement.dto.request.RegisterRequest;
import com.example.hospitalmanagement.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String accessToken);
}
