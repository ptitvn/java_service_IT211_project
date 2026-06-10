package com.example.hospitalmanagement.repository;

import com.example.hospitalmanagement.entity.MedicalRecord;
import com.example.hospitalmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    // Bệnh nhân xem hồ sơ của mình
    @Query("SELECT m FROM MedicalRecord m JOIN FETCH m.doctor JOIN FETCH m.patient " +
            "JOIN FETCH m.appointment WHERE m.patient = :patient ORDER BY m.createdAt DESC")
    List<MedicalRecord> findByPatientWithDetails(@Param("patient") User patient);

    // Doctor xem hồ sơ đã tải lên
    @Query("SELECT m FROM MedicalRecord m JOIN FETCH m.doctor JOIN FETCH m.patient " +
            "JOIN FETCH m.appointment WHERE m.doctor = :doctor ORDER BY m.createdAt DESC")
    List<MedicalRecord> findByDoctorWithDetails(@Param("doctor") User doctor);
}
