package com.example.srs.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HodDto {

    private String name;
    private String email;
    private String phoneNumber;
    private String gender;
    private LocalDate dateOfBirth;
    private Long roleId;     // from UsersEntity -> RoleEntity
    private Long departmentId; // from DepartmentEntity
    private List<Long> subjectId;    // from SubjectEntity
    private Long courseId; // from CourseEntity
}
