package com.example.srs.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GetCourseRequestDto {
    private Long id;
    private String studentName;
    private String email;
    private String courseName;
    private String departmentName;
    private String status;
}

