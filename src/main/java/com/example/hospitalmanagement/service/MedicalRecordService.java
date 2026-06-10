package com.example.hospitalmanagement.service;

import com.example.hospitalmanagement.dto.response.MedicalRecordResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MedicalRecordService {
    // FR-09: Doctor tải lên hồ sơ
    MedicalRecordResponse uploadRecord(String doctorUsername,
                                       Long appointmentId,
                                       MultipartFile file,
                                       String diagnosis,
                                       String description);

    // Bệnh nhân / Doctor xem hồ sơ
    List<MedicalRecordResponse> getMyRecords(String username);
}
