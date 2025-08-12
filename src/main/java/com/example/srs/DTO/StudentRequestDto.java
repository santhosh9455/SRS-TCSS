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

    @NotBlank(message = "Name is Required")
    private String name;


    private Integer age;

    @NotBlank(message = "gender is Required")
    private String gender;

    @Email(message = "Invalid Email id")
    @NotBlank(message = "email is Required")
    private String email;

    @NotBlank(message = "phoneNumber is Required")
    private String phoneNumber;

    @NotBlank(message = "Department Name is Required")
    private Long departmentId;

    @NotNull(message = "Date Of Birth is Required")
    private LocalDate dateOfBirth;

    private Long courseId;
    private List<Long> subjectId;

    private MultipartFile profileImage;
    private MultipartFile marksheetImage;

    public StudentRequestDto(@NotBlank(message = "Name is Required") String name, @NotNull(message = "age is Required") @Min(value = 18) Integer age, @NotBlank(message = "gender is Required") String gender, @Email(message = "Invalid Email id") @NotBlank(message = "email is Required") String email, @NotBlank(message = "phoneNumber is Required") String phoneNumber, @NotBlank(message = "Department Name is Required") String departmentName) {
    }
}
