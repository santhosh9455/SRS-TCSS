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

    private Integer age;
    private String firstName;
    private String lastName;
    private String programmeLevel;
    private String programmeOfStudy;
    private String aadharNumber;
    private String fatherName;
    private String fatherMobile;
    private String fatherOccupation;
    private String motherName;
    private String motherMobile;
    private String motherOccupation;
    private String street;
    private String taluk;
    private String schoolName;
    private String hostelBusService;
    private String boardingPoint;
    private String marksheetImagePath12th;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private LocalDate admission_date;
    private String guardianName;
    private String guardian_phone;
    private LocalDate created_at;
    private LocalDate updated_at;
    private String enrollment_status;
    private String marksheetImagePath10th;
    private String ugCertificate;
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
