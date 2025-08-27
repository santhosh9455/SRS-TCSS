package com.example.srs.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StaffResDto {

    private Long id;

    private String name; //Staff name

    private String email;

    private Integer age;

    private String gender;

    private String phoneNumber;

    private String role;

    private String username;

    private List<String> subjectNme;

    private String upSubjectName;

    private Long courseId;

    private LocalDate dateOfBirth;

    private List<Long> subjectId;

    private String departmentName; // refer the dept

    private String courseName; // Course names handled by this staff

    private OffsetDateTime created_at;
    private OffsetDateTime  updated_at;
    private String profileImagePath;
    private String street;
    private String taluk;
    private String district;
    private String city;
    private String pincode;
}
