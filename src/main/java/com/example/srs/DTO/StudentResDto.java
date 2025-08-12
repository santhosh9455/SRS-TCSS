package com.example.srs.DTO;

import com.example.srs.Enum.CourseRequestStatusEnum;
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
public class StudentResDto {

    private Long id;

    private String name;

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

    private String marksheetImagePath;

    private List<Long> subjectId;

    private List<String> subjectName;

    private String username;

    private LocalDate dateOfBirth;

}
