package com.example.srs.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Attendance_tbl")
public class AttendanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long attendanceId;
    private LocalDate attendanceDate;
    private String status;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private StudentEntity student;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    @JsonIgnore
    @ToString.Exclude
    private SubjectEntity subject;

}
