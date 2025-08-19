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
public class AttendanceUpdateDto {

    private String status;
    private Long studentId;
    private Long subjectId;

}
