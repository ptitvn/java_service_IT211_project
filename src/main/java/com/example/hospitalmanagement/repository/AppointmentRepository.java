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

    // FR-07: Lịch sử khám của bệnh nhân - dùng JOIN FETCH để load patient+doctor cùng lúc
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor " +
            "WHERE a.patient = :patient ORDER BY a.createdAt DESC")
    Page<Appointment> findByPatientWithDetails(@Param("patient") User patient, Pageable pageable);

    // FR-08: Danh sách lịch của bác sĩ - JOIN FETCH
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor " +
            "WHERE a.doctor = :doctor ORDER BY a.appointmentTime ASC")
    Page<Appointment> findByDoctorWithDetails(@Param("doctor") User doctor, Pageable pageable);

    // Kiểm tra trùng lịch bác sĩ (UC-04)
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor = :doctor " +
            "AND a.appointmentTime = :time " +
            "AND a.status IN ('PENDING', 'APPROVED')")
    boolean existsByDoctorAndAppointmentTimeAndStatusActive(
            @Param("doctor") User doctor,
            @Param("time") LocalDateTime time);

    // Admin xem tất cả lịch - JOIN FETCH
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor " +
            "WHERE (:status IS NULL OR a.status = :status) ORDER BY a.createdAt DESC")
    Page<Appointment> findAllWithDetails(
            @Param("status") Appointment.AppointmentStatus status, Pageable pageable);
}