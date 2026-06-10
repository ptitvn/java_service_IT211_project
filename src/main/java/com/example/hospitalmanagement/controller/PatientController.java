package com.example.hospitalmanagement.controller;

import com.example.hospitalmanagement.dto.request.AppointmentRequest;
import com.example.hospitalmanagement.dto.response.ApiResponse;
import com.example.hospitalmanagement.dto.response.AppointmentResponse;
import com.example.hospitalmanagement.dto.response.UserResponse;
import com.example.hospitalmanagement.entity.User;
import com.example.hospitalmanagement.repository.UserRepository;
import com.example.hospitalmanagement.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// * FR-06: Đặt lịch khám bệnh — PATIENT
// * FR-07: Xem lịch sử khám cá nhân — PATIENT

@RestController
@RequestMapping("/api/v1/patient")
@RequiredArgsConstructor
public class PatientController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    //  FR-06: Đặt lịch khám
    @PostMapping("/appointments")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            Authentication authentication) {
        AppointmentResponse response = appointmentService.createAppointment(
                authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã tạo cuộc hẹn thành công", response));
    }

    //  FR-07: Xem lịch sử khám cá nhân
    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Page<AppointmentResponse> appointments = appointmentService.getMyAppointments(
                authentication.getName(), page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy cuộc hẹn thành công", appointments));
    }

    // Lấy danh sách bác sĩ để chọn khi đặt lịch
    @GetMapping("/doctors")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getDoctors() {
        List<UserResponse> doctors = userRepository.findByRole(User.Role.DOCTOR)
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Các bác sĩ đã truy xuất thành công", doctors));
    }
}
