package com.example.srs.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserProfileDTO {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String gender;
    private Integer age;

    private LocalDate dateOfBirth;
    private String username; // from UsersEntity
    private String role;     // from UsersEntity -> RoleEntity
    private String departmentName; // from DepartmentEntity
    private Long departmentId;
    private List<String> subjectName;    // from SubjectEntity
    private List<Long> subjectId;
    private String courseName;     // from CourseEntity
    private Long courseId;

}
