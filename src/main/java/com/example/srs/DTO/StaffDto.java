package com.example.srs.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class StaffDto {

    @NotBlank(message = "Staff name is required")
    private String name; //Staff name

    @Min(value = 18)
    private Integer age;

    @NotBlank(message = "gender email is required")
    private String gender;

    @NotBlank(message = "Staff email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Staff phoneNumber is required")
    private String phoneNumber;

    private OffsetDateTime created_at;
    private OffsetDateTime  updated_at;
    private String profileImagePath;
    private String street;
    private String taluk;
    private String district;
    private String city;
    private String pincode;

    private List<Long> subjectId;

    private String courseName;

    private Long courseId;


}
