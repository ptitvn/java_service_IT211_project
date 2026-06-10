package com.example.hospitalmanagement.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

// FR-06: Bệnh nhân đặt lịch khám
@Data
public class AppointmentRequest {

    @NotNull(message = "Cần có ID bác sĩ")
    private Long doctorId;

    @NotNull(message = "Cần có thời gian hẹn")
    @Future(message = "Thời gian hẹn phải là trong tương lai")
    private LocalDateTime appointmentTime;

    private String reason;  // Lý do khám (tuỳ chọn)
}
