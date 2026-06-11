package com.example.hospitalmanagement.controller;

import com.example.hospitalmanagement.dto.request.AppointmentStatusRequest;
import com.example.hospitalmanagement.dto.response.ApiResponse;
import com.example.hospitalmanagement.dto.response.AppointmentResponse;
import com.example.hospitalmanagement.dto.response.MedicalRecordResponse;
import com.example.hospitalmanagement.entity.Appointment;
import com.example.hospitalmanagement.service.AppointmentService;
import com.example.hospitalmanagement.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    // FR-08: Xem lịch khám
    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAppointments(
            @RequestParam(required = false) Appointment.AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Success",
                appointmentService.getDoctorAppointments(
                        authentication.getName(), status, page, size)));
    }

    // FR-08: Phê duyệt / Từ chối lịch khám
    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tình trạng cuộc hẹn đã được cập nhật thành công",
                appointmentService.updateAppointmentStatus(
                        id, authentication.getName(), request)));
    }

    // FR-09: Tải lên hồ sơ bệnh án
    @PostMapping(value = "/records/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> uploadRecord(
            @RequestParam("file") MultipartFile file,
            @RequestParam("appointmentId") Long appointmentId,
            @RequestParam(value = "diagnosis", required = false) String diagnosis,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {

        MedicalRecordResponse response = medicalRecordService.uploadRecord(
                authentication.getName(), appointmentId, file, diagnosis, description);

        return ResponseEntity.ok(ApiResponse.success(
                "Hồ sơ y tế đã được tải lên thành công", response));
    }

    // Xem danh sách hồ sơ bệnh án đã tải lên
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getMyRecords(
            Authentication authentication) {
        List<MedicalRecordResponse> records =
                medicalRecordService.getMyRecords(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(
                "Đã lấy bản ghi thành công", records));
    }
}