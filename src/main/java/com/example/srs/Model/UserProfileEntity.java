package com.example.srs.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.text.DateFormat;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "user_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String phoneNumber;
    private String gender;
    private Integer age;

    private LocalDate dateOfBirth;


    @OneToOne
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnore // avoid infinite recursion
    private UsersEntity user;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private DepartmentEntity department;

    @ManyToMany
    @JsonIgnore
    @ToString.Exclude
    private List<SubjectEntity> subjects = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "course_id")
    @JsonManagedReference
    private CourseEntity course;   // only used for STAFF
}

