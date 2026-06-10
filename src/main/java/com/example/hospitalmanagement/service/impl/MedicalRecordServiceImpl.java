package com.example.hospitalmanagement.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.hospitalmanagement.dto.response.MedicalRecordResponse;
import com.example.hospitalmanagement.entity.Appointment;
import com.example.hospitalmanagement.entity.MedicalRecord;
import com.example.hospitalmanagement.entity.User;
import com.example.hospitalmanagement.exception.BadRequestException;
import com.example.hospitalmanagement.exception.ResourceNotFoundException;
import com.example.hospitalmanagement.repository.AppointmentRepository;
import com.example.hospitalmanagement.repository.MedicalRecordRepository;
import com.example.hospitalmanagement.repository.UserRepository;
import com.example.hospitalmanagement.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    // Các định dạng file được phép (NFR: max 10MB đã config trong application.properties)
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/jpg", "application/pdf"
    );

    // ─── FR-09: Doctor tải lên hồ sơ bệnh án ─────────────────────────────────

    @Override
    @Transactional
    public MedicalRecordResponse uploadRecord(String doctorUsername,
                                              Long appointmentId,
                                              MultipartFile file,
                                              String diagnosis,
                                              String description) {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException(
                    "Invalid file type. Allowed: JPG, PNG, PDF. Got: " + file.getContentType());
        }

        // Lấy thông tin doctor
        User doctor = userRepository.findByUsername(doctorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // Lấy thông tin appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + appointmentId));

        // Kiểm tra appointment đã APPROVED chưa
        if (appointment.getStatus() != Appointment.AppointmentStatus.APPROVED) {
            throw new BadRequestException(
                    "Can only upload records for APPROVED appointments. Current status: "
                            + appointment.getStatus());
        }

        User patient = userRepository.findById(appointment.getPatient().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Upload lên Cloudinary
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "hospital/medical_records",
                            "resource_type", "auto",        // tự detect image/pdf
                            "public_id", "record_" + appointmentId + "_" + System.currentTimeMillis()
                    )
            );

            String fileUrl  = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            log.info("[MEDICAL] Doctor '{}' uploaded file to Cloudinary: {}", doctorUsername, fileUrl);

            // Lưu vào DB
            MedicalRecord record = MedicalRecord.builder()
                    .appointment(appointment)
                    .doctor(doctor)
                    .patient(patient)
                    .fileName(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .publicId(publicId)
                    .fileType(file.getContentType())
                    .diagnosis(diagnosis)
                    .description(description)
                    .build();

            MedicalRecord saved = medicalRecordRepository.save(record);
            log.info("[MEDICAL] Medical record saved with id: {}", saved.getId());

            return MedicalRecordResponse.fromEntity(saved);

        } catch (IOException e) {
            log.error("[MEDICAL] Cloudinary upload failed: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to cloud storage: " + e.getMessage());
        }
    }

    // ─── Xem danh sách hồ sơ ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getMyRecords(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<MedicalRecord> records;
        if (user.getRole() == User.Role.PATIENT) {
            records = medicalRecordRepository.findByPatientWithDetails(user);
        } else {
            records = medicalRecordRepository.findByDoctorWithDetails(user);
        }

        return records.stream()
                .map(MedicalRecordResponse::fromEntity)
                .toList();
    }
}
