package com.example.hospitalmanagement.repository;

import com.example.hospitalmanagement.entity.Appointment;
import com.example.hospitalmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // FR-07: Lịch sử khám của bệnh nhân
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor " +
            "WHERE a.patient = :patient ORDER BY a.createdAt DESC")
    Page<Appointment> findByPatientWithDetails(
            @Param("patient") User patient, Pageable pageable);

    // FR-08: Lịch của bác sĩ - filter theo status
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor " +
            "WHERE a.doctor = :doctor " +
            "AND (:status IS NULL OR a.status = :status) " +
            "ORDER BY a.appointmentTime ASC")
    Page<Appointment> findByDoctorWithDetails(
            @Param("doctor") User doctor,
            @Param("status") Appointment.AppointmentStatus status,
            Pageable pageable);

    // UC-04: Kiểm tra trùng lịch bác sĩ
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor = :doctor " +
            "AND a.appointmentTime = :time " +
            "AND a.status IN ('PENDING', 'APPROVED')")
    boolean existsByDoctorAndAppointmentTimeAndStatusActive(
            @Param("doctor") User doctor,
            @Param("time") LocalDateTime time);

    // Admin xem tất cả lịch
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor " +
            "WHERE (:status IS NULL OR a.status = :status) ORDER BY a.createdAt DESC")
    Page<Appointment> findAllWithDetails(
            @Param("status") Appointment.AppointmentStatus status, Pageable pageable);
}