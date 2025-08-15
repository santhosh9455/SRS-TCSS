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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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

    private Integer age;
    private LocalDate dateOfBirth;
    private String gender;

    @Column(unique = true)
    private String email;

    private String phoneNumber;

    private String profileImagePath;

    private String firstName;

    private String lastName;

    private String programmeLevel;

    private String aadharNumber;

    private String fatherName;

    private String fatherMobile;

    private String fatherOccupation;

    private String motherName;

    private String motherMobile;

    private String motherOccupation;

    private String street;

    private String taluk;

    private String district;

    private String schoolName;

    private String hostelBusService;

    private String boardingPoint;

    private String marksheetImagePath12th;

    private String city;

    private String pincode;

    private OffsetDateTime admission_date;

    private String guardianName;

    private String guardian_phone;

    private OffsetDateTime   created_at;

    private OffsetDateTime  updated_at;

    private String enrollment_status = "PENDING";

    private String  marksheetImagePath10th;

    private String ugCertificate;

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
