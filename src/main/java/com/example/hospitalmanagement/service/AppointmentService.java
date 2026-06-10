package com.example.hospitalmanagement.service;

import com.example.hospitalmanagement.dto.request.AppointmentRequest;
import com.example.hospitalmanagement.dto.request.AppointmentStatusRequest;
import com.example.hospitalmanagement.dto.response.AppointmentResponse;
import com.example.hospitalmanagement.entity.Appointment;
import org.springframework.data.domain.Page;

public interface AppointmentService {
    // FR-06
    AppointmentResponse createAppointment(String username, AppointmentRequest request);
    // FR-07
    Page<AppointmentResponse> getMyAppointments(String username, int page, int size);
    // FR-08
    AppointmentResponse updateAppointmentStatus(Long appointmentId, String username,
                                                AppointmentStatusRequest request);
    // Admin/Doctor xem tất cả lịch
    Page<AppointmentResponse> getAllAppointments(Appointment.AppointmentStatus status, int page, int size);
}
