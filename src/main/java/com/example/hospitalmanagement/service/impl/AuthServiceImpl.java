package com.example.hospitalmanagement.service.impl;

import com.example.hospitalmanagement.dto.request.LoginRequest;
import com.example.hospitalmanagement.dto.request.RefreshTokenRequest;
import com.example.hospitalmanagement.dto.request.RegisterRequest;
import com.example.hospitalmanagement.dto.response.AuthResponse;
import com.example.hospitalmanagement.entity.RefreshToken;
import com.example.hospitalmanagement.entity.TokenBlacklist;
import com.example.hospitalmanagement.entity.User;
import com.example.hospitalmanagement.exception.BadRequestException;
import com.example.hospitalmanagement.exception.ConflictException;
import com.example.hospitalmanagement.exception.ResourceNotFoundException;
import com.example.hospitalmanagement.repository.RefreshTokenRepository;
import com.example.hospitalmanagement.repository.TokenBlacklistRepository;
import com.example.hospitalmanagement.repository.UserRepository;
import com.example.hospitalmanagement.security.JwtService;
import com.example.hospitalmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // FR-01: Đăng nhập
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Spring Security xác thực username/password (ném BadCredentialsException nếu sai)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Thu hồi toàn bộ refresh token cũ của user này trước khi cấp mới
        refreshTokenRepository.revokeAllUserTokens(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createAndSaveRefreshToken(user);

        log.info("[AUTH] User '{}' logged in successfully with role {}", user.getUsername(), user.getRole());

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    // FR-04: Đăng ký Bệnh nhân
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra trùng username/email
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username '" + request.getUsername() + "' already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email '" + request.getEmail() + "' already exists");
        }

        // Tạo User mới với Role PATIENT
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))  // BCrypt strength 10
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(User.Role.PATIENT)
                .status(User.UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("[AUTH] New PATIENT registered: '{}'", user.getUsername());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createAndSaveRefreshToken(user);

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    // FR-02: Refresh Token
    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy token làm mới hoặc token không hợp lệ"));

        if (storedToken.isRevoked()) {
            throw new BadRequestException("Mã làm mới đã bị thu hồi");
        }
        if (storedToken.isExpired()) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new BadRequestException("Mã thông báo làm mới đã hết hạn. Vui lòng đăng nhập lại");
        }

        User user = storedToken.getUser();

        // Thu hồi token cũ
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        // Cấp AccessToken mới + RefreshToken mới (rotation)
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = createAndSaveRefreshToken(user);

        log.info("[AUTH] Đã làm mới token cho người dùng '{}'", user.getUsername());
        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }

    // FR-03: Đăng xuất (Blacklist)
    @Override
    @Transactional
    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BadRequestException("Cần có mã truy cập");
        }

        // Lấy thời gian hết hạn của token để lưu vào Blacklist
        Date expiration = jwtService.extractExpiration(accessToken);
        LocalDateTime expiresAt = expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        TokenBlacklist blacklistEntry = TokenBlacklist.builder()
                .token(accessToken)
                .expiresAt(expiresAt)
                .build();

        tokenBlacklistRepository.save(blacklistEntry);
        log.info("[AUTH] Token truy cập đã bị thu hồi và được thêm vào danh sách đen");
    }

    // Helpers
    private String createAndSaveRefreshToken(User user) {
        // RefreshToken là một chuỗi UUID ngẫu nhiên (không phải JWT)
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(refreshTokenExpiration / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
