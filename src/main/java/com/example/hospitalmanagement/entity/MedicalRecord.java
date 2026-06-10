package com.example.hospitalmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * UC-05: Hồ sơ bệnh án do Doctor tải lên
 */
@Entity
@Table(name = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lịch khám liên quan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    // Bác sĩ tải lên
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    // Bệnh nhân
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(name = "file_name", nullable = false)
    private String fileName;        // Tên file gốc

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;         // URL từ Cloudinary

    @Column(name = "public_id")
    private String publicId;        // Cloudinary public_id (dùng để xóa file)

    @Column(name = "file_type", length = 50)
    private String fileType;        // image/pdf/etc

    @Column(length = 500)
    private String diagnosis;       // Chuẩn đoán

    @Column(length = 1000)
    private String description;     // Mô tả thêm

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
