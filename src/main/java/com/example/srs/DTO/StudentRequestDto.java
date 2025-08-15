package com.example.srs.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StudentRequestDto {

    // Basic Details
    @NotBlank(message = "First Name is Required")
    private String firstName;

    @NotBlank(message = "Last Name is Required")
    private String lastName;


    private Integer age;

    @NotNull(message = "Date of Birth is Required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is Required")
    private String gender;

    @Email(message = "Invalid Email Address")
    @NotBlank(message = "Email is Required")
    private String email;

    @NotBlank(message = "Phone Number is Required")
    private String phoneNumber;

    @NotNull(message = "Department ID is Required")
    private Long departmentId;

    // Academic Details
    @NotBlank(message = "Programme Level is Required")
    private String programmeLevel;///////----



    private Long courseId;
    private List<Long> subjectId;

    // Parent / Guardian Details
    @NotBlank(message = "Aadhar Number is Required")
    private String aadharNumber;

    private String fatherName;

    private String fatherMobile;
    private String fatherOccupation;

    private String motherName;
    private String motherMobile;
    private String motherOccupation;

    private String guardianName;
    private String guardianPhone;

    // Address Details
    private String street;
    private String taluk;

    @NotBlank(message = "City is Required")
    private String city;

    @NotBlank(message = "Pincode is Required")
    private String pincode;

    @NotBlank(message = "district is Required")
    private String district;

    // School / Hostel Info
    @NotBlank(message = "school Name is Required")
    private String schoolName;
    private String hostelBusService;
    private String boardingPoint;

    // Admission Details
    private OffsetDateTime admissionDate;
    private String enrollmentStatus;

    // File Uploads
    @NotNull(message = "profile Image is Required")
    private MultipartFile profileImage;          // profileImagePath
    @NotNull(message = "marksheet Image 12th is Required")
    private MultipartFile marksheetImage12th;    // marksheetImagePath12th
    @NotNull(message = "marksheet Image 10th is Required")
    private MultipartFile marksheetImage10th;    // marksheetImagePath10th

    private MultipartFile ugCertificate;         // ugCertificate

    // Audit Fields
    private OffsetDateTime  createdAt;
    private OffsetDateTime   updatedAt;
}
