package com.example.hospitalmanagement.controller;

import com.example.hospitalmanagement.dto.request.LoginRequest;
import com.example.hospitalmanagement.dto.request.RefreshTokenRequest;
import com.example.hospitalmanagement.dto.request.RegisterRequest;
import com.example.hospitalmanagement.dto.response.AuthResponse;
import com.example.hospitalmanagement.exception.BadRequestException;
import com.example.hospitalmanagement.exception.ConflictException;
import com.example.hospitalmanagement.exception.GlobalExceptionHandler;
import com.example.hospitalmanagement.repository.TokenBlacklistRepository;
import com.example.hospitalmanagement.security.JwtAuthenticationFilter;
import com.example.hospitalmanagement.security.JwtService;
import com.example.hospitalmanagement.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private TokenBlacklistRepository tokenBlacklistRepository;

    private AuthResponse mockAuthResponse(String username, String role) {
        return AuthResponse.builder()
                .accessToken("mock.access.token")
                .refreshToken("mock-refresh-uuid")
                .tokenType("Bearer")
                .expiresIn(900)
                .user(AuthResponse.UserInfo.builder()
                        .id(1L).username(username)
                        .email(username + "@gmail.com")
                        .fullName("Test User").role(role)
                        .build())
                .build();
    }

    // Test 6: Login thanh cong -> 200
    @Test
    @DisplayName("Test 6 - POST /api/v1/auth/login thanh cong tra ve 200")
    void login_validRequest_returns200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin01");
        request.setPassword("123456");

        when(authService.login(any())).thenReturn(mockAuthResponse("admin01", "ADMIN"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.data.accessToken").value("mock.access.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"));
    }

    // Test 7: Login thieu password -> 400
    @Test
    @DisplayName("Test 7 - POST /api/v1/auth/login thieu password tra ve 400")
    void login_missingPassword_returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin01");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // Test 8: Register thanh cong -> 201
    @Test
    @DisplayName("Test 8 - POST /api/v1/auth/register thanh cong tra ve 201")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newpatient");
        request.setPassword("123456");
        request.setEmail("newpatient@gmail.com");
        request.setFullName("New Patient");

        when(authService.register(any())).thenReturn(mockAuthResponse("newpatient", "PATIENT"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("PATIENT"));
    }

    // Test 9: Register trung username -> 409
    @Test
    @DisplayName("Test 9 - POST /api/v1/auth/register trung username tra ve 409")
    void register_duplicateUsername_returns409() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("patient01");
        request.setPassword("123456");
        request.setEmail("other@gmail.com");
        request.setFullName("Other");

        when(authService.register(any()))
                .thenThrow(new ConflictException("Username 'patient01' already exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // Test 10: Refresh token khong hop le -> 400
    @Test
    @DisplayName("Test 10 - POST /api/v1/auth/refresh token khong hop le tra ve 400")
    void refreshToken_invalidToken_returns400() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token-xyz");

        when(authService.refreshToken(any()))
                .thenThrow(new BadRequestException("Refresh token not found or invalid"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}