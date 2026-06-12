package com.example.hospitalmanagement.service.impl;

import com.example.hospitalmanagement.dto.request.ChangePasswordRequest;
import com.example.hospitalmanagement.dto.request.CreateUserRequest;
import com.example.hospitalmanagement.dto.request.ResetPasswordRequest;
import com.example.hospitalmanagement.dto.request.UpdateUserRequest;
import com.example.hospitalmanagement.dto.response.UserResponse;
import com.example.hospitalmanagement.entity.User;
import com.example.hospitalmanagement.exception.BadRequestException;
import com.example.hospitalmanagement.exception.ConflictException;
import com.example.hospitalmanagement.exception.ResourceNotFoundException;
import com.example.hospitalmanagement.repository.UserRepository;
import com.example.hospitalmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new ConflictException("Username '" + request.getUsername() + "' already exists");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email '" + request.getEmail() + "' already exists");

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(request.getRole())
                .status(User.UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);
        log.info("[USER] Đã tạo người dùng: '{}' role {}", saved.getUsername(), saved.getRole());
        return UserResponse.fromEntity(saved);
    }

    @Override
    public UserResponse getUserById(Long id) {
        return UserResponse.fromEntity(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + id)));
    }

    @Override
    public Page<UserResponse> searchUsers(String keyword, User.Role role, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.searchUsers(keyword, role, pageable);
        List<UserResponse> dtoList = userPage.getContent().stream()
                .map(UserResponse::fromEntity).toList();
        return new PageImpl<>(dtoList, pageable, userPage.getTotalElements());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail()))
                throw new ConflictException("Email '" + request.getEmail() + "' đã tồn tại");
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + id));
        if (user.getRole() == User.Role.ADMIN)
            throw new BadRequestException("Không thể xóa tài khoản quản trị");

//        userRepository.deleteById(id);
        user.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("[USER] ID người dùng đã xóa: {}", id);
    }

    // FR-10: Người dùng tự đổi mật khẩu
    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
            throw new BadRequestException("Mật khẩu hiện tại không đúng");

        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new BadRequestException("Mật khẩu mới và xác nhận mật khẩu không khớp");

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword()))
            throw new BadRequestException("Mật khẩu mới phải khác với mật khẩu hiện tại");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("[USER] Password đã được đổi cho: '{}'", username);
    }

    //  FR-10: Admin reset mật khẩu hộ
    @Override
    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + userId));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("[USER] Quản trị viên đặt lại mật khẩu cho ID người dùng: {}", userId);
    }
}