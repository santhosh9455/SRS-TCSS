package com.example.srs.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AttendanceResDto {

    private Long attendanceId;
    private LocalDate attendanceDate;
    private String status;

    // Student basic info
    private Long studentId;
    private String studentName;

    // Subject basic info
    private Long subjectId;
    private String subjectName;

}
