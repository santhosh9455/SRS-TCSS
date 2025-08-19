package com.example.srs.Repository;

import com.example.srs.Model.AttendanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepo extends JpaRepository<AttendanceEntity,Long> {


    @Query("SELECT a FROM AttendanceEntity a " +
            "WHERE (:studentId IS NULL OR a.student.id = :studentId) " +
            "AND (:subjectId IS NULL OR a.subject.id = :subjectId) " +
            "AND (:status IS NULL OR LOWER(a.status) LIKE LOWER(CONCAT('%', :status, '%')))")
    Page<AttendanceEntity> filterAttendances(
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId,
            @Param("status") String status,
            Pageable pageable);



}
