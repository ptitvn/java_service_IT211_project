package com.example.hospitalmanagement.dto.request;

import com.example.hospitalmanagement.entity.Appointment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// FR-08: Admin/Doctor phê duyệt hoặc từ chối lịch khám
@Data
public class AppointmentStatusRequest {

    @NotNull(message = "Trạng thái là bắt buộc")
    private Appointment.AppointmentStatus status;  // APPROVED hoặc REJECTED

    private String notes;  // Ghi chú lý do từ chối
}
