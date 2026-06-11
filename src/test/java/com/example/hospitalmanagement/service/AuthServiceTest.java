package com.example.hospitalmanagement.service;

import com.example.hospitalmanagement.dto.request.LoginRequest;
import com.example.hospitalmanagement.dto.request.RegisterRequest;
import com.example.hospitalmanagement.dto.response.AuthResponse;
import com.example.hospitalmanagement.entity.User;
import com.example.hospitalmanagement.exception.ConflictException;
import com.example.hospitalmanagement.repository.RefreshTokenRepository;
import com.example.hospitalmanagement.repository.TokenBlacklistRepository;
import com.example.hospitalmanagement.repository.UserRepository;
import com.example.hospitalmanagement.security.JwtService;
import com.example.hospitalmanagement.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenBlacklistRepository tokenBlacklistRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        // Inject @Value fields
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604800000L);
    }

    // Test 1: Login thành công

    @Test
    @DisplayName("Test 1 - Login với đúng username/password trả về token")
    void login_validCredentials_returnsAuthResponse() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("patient01");
        request.setPassword("123456");

        User mockUser = User.builder()
                .id(1L).username("patient01")
                .email("p@gmail.com").fullName("Nguyen Van A")
                .role(User.Role.PATIENT).status(User.UserStatus.ACTIVE)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByUsername("patient01")).thenReturn(Optional.of(mockUser));
        when(jwtService.generateAccessToken(any())).thenReturn("mock.access.token");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock.access.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getUsername()).isEqualTo("patient01");
        assertThat(response.getUser().getRole()).isEqualTo("PATIENT");
        verify(authenticationManager, times(1)).authenticate(any());
    }

    //  Test 2: Login sai mật khẩu

    @Test
    @DisplayName("Test 2 - Login sai mật khẩu ném BadCredentialsException")
    void login_wrongPassword_throwsBadCredentialsException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("patient01");
        request.setPassword("saimatkhau");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");
    }

    //  Test 3: Register thành công
    @Test
    @DisplayName("Test 3 - Register bệnh nhân mới thành công")
    void register_newUser_returnsAuthResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newpatient");
        request.setPassword("123456");
        request.setEmail("new@gmail.com");
        request.setFullName("New Patient");

        when(userRepository.existsByUsername("newpatient")).thenReturn(false);
        when(userRepository.existsByEmail("new@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPass");

        User savedUser = User.builder()
                .id(2L).username("newpatient")
                .email("new@gmail.com").fullName("New Patient")
                .role(User.Role.PATIENT).status(User.UserStatus.ACTIVE)
                .build();

        when(userRepository.save(any())).thenReturn(savedUser);
        when(jwtService.generateAccessToken(any())).thenReturn("mock.access.token");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUser().getRole()).isEqualTo("PATIENT");
        assertThat(response.getUser().getUsername()).isEqualTo("newpatient");
        verify(userRepository, times(1)).save(any());
    }

    //  Test 4: Register trùng username

    @Test
    @DisplayName("Test 4 - Register trùng username ném ConflictException")
    void register_duplicateUsername_throwsConflictException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("patient01");
        request.setPassword("123456");
        request.setEmail("other@gmail.com");
        request.setFullName("Other");

        when(userRepository.existsByUsername("patient01")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    //  Test 5: Register trùng email

    @Test
    @DisplayName("Test 5 - Register trùng email ném ConflictException")
    void register_duplicateEmail_throwsConflictException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("123456");
        request.setEmail("patient01@gmail.com");
        request.setFullName("New User");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("patient01@gmail.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }
}
