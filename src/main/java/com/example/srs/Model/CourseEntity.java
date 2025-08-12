package com.example.srs.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "course_tbl")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;

    // One course is assigned to one staff (UserProfileEntity)
    @OneToOne
    @JoinColumn(name = "staff_id", referencedColumnName = "id", unique = true)
    @JsonBackReference
    @ToString.Exclude
    private UserProfileEntity staff;

    // One course can have many students
    @OneToMany(mappedBy = "course")
    @JsonIgnore
    @ToString.Exclude
    private List<StudentEntity> students = new ArrayList<>();
}
