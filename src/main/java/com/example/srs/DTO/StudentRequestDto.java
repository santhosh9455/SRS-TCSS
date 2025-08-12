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

    @NotNull(message = "Age is Required")
    @Min(value = 1, message = "Age must be greater than 0")
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
    private String programmeLevel;

    @NotBlank(message = "Programme of Study is Required")
    private String programmeOfStudy;

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

    @NotBlank(message = "State is Required")
    private String state;

    @NotBlank(message = "Pincode is Required")
    private String pincode;

    @NotBlank(message = "Country is Required")
    private String country;

    // School / Hostel Info
    private String schoolName;
    private String hostelBusService;
    private String boardingPoint;

    // Admission Details
    private LocalDate admissionDate;
    private String enrollmentStatus;

    // File Uploads
    private MultipartFile profileImage;          // profileImagePath
    private MultipartFile marksheetImage12th;    // marksheetImagePath12th
    private MultipartFile marksheetImage10th;    // marksheetImagePath10th
    private MultipartFile ugCertificate;         // ugCertificate

    // Audit Fields
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
