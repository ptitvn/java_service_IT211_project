package com.example.hospitalmanagement.dto.response;

import com.example.hospitalmanagement.entity.MedicalRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {
    private Long id;
    private Long appointmentId;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private String patientName;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private String diagnosis;
    private String description;
    private LocalDateTime createdAt;

    public static MedicalRecordResponse fromEntity(MedicalRecord m) {
        return MedicalRecordResponse.builder()
                .id(m.getId())
                .appointmentId(m.getAppointment().getId())
                .doctorId(m.getDoctor().getId())
                .doctorName(m.getDoctor().getFullName())
                .patientId(m.getPatient().getId())
                .patientName(m.getPatient().getFullName())
                .fileName(m.getFileName())
                .fileUrl(m.getFileUrl())
                .fileType(m.getFileType())
                .diagnosis(m.getDiagnosis())
                .description(m.getDescription())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
