package com.example.srs.DTO;

import com.example.srs.Enum.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StudentUpdateRequestDto {

    private String name;
    private Integer age;
    private String gender;
    private String email;
    private String phoneNumber;
    private Long departmentId;
    private Long courseId;
    private List<Long> subjectId;
    private String status;
    private LocalDate dateOfBirth;
    private String username;
    private String courseStatus;
    private String profileImagePath;
    private String marksheetImagePath;
}
