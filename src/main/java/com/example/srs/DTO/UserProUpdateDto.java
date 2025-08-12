package com.example.srs.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.experimental.theories.DataPoints;

import java.time.LocalDate;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProUpdateDto {

    private Long id; // Required for identifying the user to update

    // Optional fields (set only the ones you want to update)
    private String name;
    private String email;
    private String phoneNumber;
    private String gender;
    private LocalDate dateOfBirth;

    private Long departmentId;
    private List<Long> subjectId;
    private Long courseId;
    private String username;
}
