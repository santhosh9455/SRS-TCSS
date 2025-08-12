package com.example.srs.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DepartmentDetailsDto {

    private String departmentName;
    private List<String> staffNames;
    private String hodName;
    private String hodEmail;
}
