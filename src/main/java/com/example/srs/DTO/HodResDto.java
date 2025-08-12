package com.example.srs.DTO;

import com.example.srs.Model.DepartmentEntity;
import com.example.srs.Model.UsersEntity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HodResDto {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String gender;
    private LocalDate date;
    private Integer age;
    private List<String> subjectNames;
    private String courseName;
    private String departmentName;
}
