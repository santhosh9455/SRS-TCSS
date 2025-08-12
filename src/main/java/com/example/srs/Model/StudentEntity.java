package com.example.srs.Model;

import com.example.srs.Enum.CourseRequestStatusEnum;
import com.example.srs.Enum.StatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Student_tbl")
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer age;
    private LocalDate dateOfBirth;
    private String gender;

    @Column(unique = true)
    private String email;

    private String phoneNumber;

    private String profileImagePath;

    private String  marksheetImagePath;

    @Enumerated(EnumType.STRING)
    private StatusEnum status = StatusEnum.PENDING;

    @Enumerated(EnumType.STRING)
    private CourseRequestStatusEnum courseStatus = CourseRequestStatusEnum.NOT_REQUESTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnore
    @ToString.Exclude
    private DepartmentEntity department;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = true, unique = true)
    @JsonIgnore
    @ToString.Exclude
    private UsersEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = true)
    @JsonIgnore
    @ToString.Exclude
    private CourseEntity course;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<SubjectEntity> subjects = new ArrayList<>();

}
