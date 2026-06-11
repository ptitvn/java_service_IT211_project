package com.example.hospitalmanagement.service.impl;

import com.example.hospitalmanagement.dto.request.AppointmentRequest;
import com.example.hospitalmanagement.dto.request.AppointmentStatusRequest;
import com.example.hospitalmanagement.dto.response.AppointmentResponse;
import com.example.hospitalmanagement.entity.Appointment;
import com.example.hospitalmanagement.entity.User;
import com.example.hospitalmanagement.exception.BadRequestException;
import com.example.hospitalmanagement.exception.ConflictException;
import com.example.hospitalmanagement.exception.ResourceNotFoundException;
import com.example.hospitalmanagement.repository.AppointmentRepository;
import com.example.hospitalmanagement.repository.UserRepository;
import com.example.hospitalmanagement.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    // FR-06: Đặt lịch khám
    @Override
    @Transactional
    public AppointmentResponse createAppointment(String username, AppointmentRequest request) {
        User patient = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân"));

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy bác sĩ với id: " + request.getDoctorId()));

        if (doctor.getRole() != User.Role.DOCTOR) {
            throw new BadRequestException("User id " + request.getDoctorId() + " is not a doctor");
        }

        boolean conflict = appointmentRepository.existsByDoctorAndAppointmentTimeAndStatusActive(
                doctor, request.getAppointmentTime());
        if (conflict) {
            throw new ConflictException("Bác sĩ đã có lịch hẹn vào thời gian này");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(request.getAppointmentTime())
                .reason(request.getReason())
                .status(Appointment.AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("[APPOINTMENT] Patient '{}' created appointment id: {} with doctor '{}'",
                username, saved.getId(), doctor.getUsername());

        return AppointmentResponse.fromEntity(saved);
    }

    // FR-07: Xem lịch sử khám cá nhân (Patient)
    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getMyAppointments(String username, int page, int size) {
        User patient = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Appointment> appointments = appointmentRepository
                .findByPatientWithDetails(patient, pageable);

        List<AppointmentResponse> dtoList = appointments.getContent()
                .stream()
                .map(AppointmentResponse::fromEntity)
                .toList();

        return new PageImpl<>(dtoList, pageable, appointments.getTotalElements());
    }

    // FR-08: Phê duyệt / Từ chối lịch khám
    @Override
    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long appointmentId, String username,
                                                       AppointmentStatusRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy cuộc hẹn với id: " + appointmentId));

        if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING) {
            throw new BadRequestException(
                    "Không thể cập nhật cuộc hẹn với trạng thái: " + appointment.getStatus());
        }

        if (request.getStatus() == Appointment.AppointmentStatus.PENDING ||
                request.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Trạng thái phải là APPROVED hoặc REJECTED");
        }

        appointment.setStatus(request.getStatus());
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        Appointment updated = appointmentRepository.save(appointment);

        // Load lại để tránh LazyInitializationException
        User patient = userRepository.findById(updated.getPatient().getId()).orElseThrow();
        User doctor = userRepository.findById(updated.getDoctor().getId()).orElseThrow();
        updated.setPatient(patient);
        updated.setDoctor(doctor);

        log.info("[APPOINTMENT] User '{}' set appointment id: {} to {}",
                username, appointmentId, request.getStatus());

        return AppointmentResponse.fromEntity(updated);
    }

    // FR-08: Doctor xem lịch khám của mình (filter theo status)
    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorAppointments(String username,
                                                           Appointment.AppointmentStatus status, int page, int size) {

        User doctor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        PageRequest pageable = PageRequest.of(page, size, Sort.by("appointmentTime").ascending());

        Page<Appointment> appointments = appointmentRepository
                .findByDoctorWithDetails(doctor, status, pageable);

        List<AppointmentResponse> dtoList = appointments.getContent()
                .stream()
                .map(AppointmentResponse::fromEntity)
                .toList();

        return new PageImpl<>(dtoList, pageable, appointments.getTotalElements());
    }

    // Admin xem tất cả lịch khám
    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAllAppointments(Appointment.AppointmentStatus status,
                                                        int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Appointment> appointments = appointmentRepository.findAllWithDetails(status, pageable);

        List<AppointmentResponse> dtoList = appointments.getContent()
                .stream()
                .map(AppointmentResponse::fromEntity)
                .toList();

        return new PageImpl<>(dtoList, pageable, appointments.getTotalElements());
    }
}