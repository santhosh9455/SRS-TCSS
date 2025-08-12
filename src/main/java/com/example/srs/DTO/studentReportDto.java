package com.example.srs.DTO;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class studentReportDto {

    @CsvBindByName
    private String FirstName;
    @CsvBindByName
    private String LastName;
    @CsvBindByName
    private Integer age;
    @CsvBindByName
    private String gender;
    @CsvBindByName
    private String email;
    @CsvBindByName
    private String phoneNumber;
    @CsvBindByName
    private String departmentName;
    @CsvBindByName
    private String courseName;
    @CsvBindByName
    private Long courseId;
    @CsvBindByName
    private String username;

    @CsvBindByName
    private String status;

    @CsvBindByName
    private String courseStatus;

    @CsvBindByName
    private String subjectNames; // Flattened version for CSV only
}
