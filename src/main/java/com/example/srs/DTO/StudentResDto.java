package com.example.srs.DTO;

import com.example.srs.Enum.CourseRequestStatusEnum;
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
public class StudentResDto {

    private Long id;

    private Integer age;

    private String gender;

    private String email;

    private String phoneNumber;

    private String departmentName;

    private Long departmentId;

    private String courseStatus;

    private String status;

    private String courseName;

    private Long courseId;

    private String profileImagePath;

    private String marksheetImagePath12th;

    private List<Long> subjectId;

    private List<String> subjectName;

    private String username;

    private LocalDate dateOfBirth;

    private String city;

    private String pincode;

    private String district;

    private OffsetDateTime admission_date;

    private String guardianName;

    private String guardian_phone;

    private OffsetDateTime  created_at;

    private OffsetDateTime  updated_at;

    private String enrollment_status;

    private String ugCertificate;

    private String firstName;

    private String lastName;

    private String programmeLevel;

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

    private String  marksheetImagePath10th;

}
