package com.example.srs.ServiceImplementation;

import com.example.srs.Components.ExcelHelper;
import com.example.srs.DTO.CourseResDto;
import com.example.srs.DTO.DepartmentDetailsDto;
import com.example.srs.DTO.StudentRequestDto;
import com.example.srs.DTO.StudentResDto;
import com.example.srs.Enum.CourseRequestStatusEnum;
import com.example.srs.Model.*;
import com.example.srs.Repository.*;
import com.example.srs.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentServiceImp implements StudentService {

    @Autowired
    private CourseRepo corerepo;

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DepartmentRepo deptRepo;

    @Autowired
    private UserProfileRepo userProfileRepo;

    @Autowired
    private SubjectRepo subjectRepo;

    @Override
    public StudentResDto RegisterRequest(StudentRequestDto dto, MultipartFile profileImage, MultipartFile marksheetImage) {

        if (profileImage == null || profileImage.isEmpty()) {
            throw new IllegalArgumentException("Profile image is required");
        }
        if (marksheetImage == null || marksheetImage.isEmpty()) {
            throw new IllegalArgumentException("Marksheet image is required");
        }

        // 1. Validate Department
        DepartmentEntity dept = deptRepo.findById(dto.getDepartmentId()).orElseThrow(() -> new RuntimeException("Department '" + dto.getDepartmentId() + "' not found"));

        int age = 0;
        if (dto.getDateOfBirth() != null) {
            age = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
        }

        // 2. Create and populate StudentEntity
        StudentEntity student = new StudentEntity();
        student.setName(dto.getName());
        student.setAge(age);
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setGender(dto.getGender());
        student.setEmail(dto.getEmail());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setDepartment(dept);
        // 3. Handle file upload (stores only paths)
        try {
            String uploadDir = "uploads/students/";
            Files.createDirectories(Paths.get(uploadDir));

            // Save profile image
            String profileImageName = "profile_" + UUID.randomUUID() + getFileExtension(profileImage.getOriginalFilename());
            Path profileImagePath = Paths.get(uploadDir + profileImageName);
            Files.copy(profileImage.getInputStream(), profileImagePath, StandardCopyOption.REPLACE_EXISTING);
            student.setProfileImagePath("/" + uploadDir + profileImageName);

            // Save marksheet image
            String marksheetImageName = "marksheet_" + UUID.randomUUID() + getFileExtension(marksheetImage.getOriginalFilename());
            Path marksheetImagePath = Paths.get(uploadDir + marksheetImageName);
            Files.copy(marksheetImage.getInputStream(), marksheetImagePath, StandardCopyOption.REPLACE_EXISTING);
            student.setMarksheetImagePath("/" + uploadDir + marksheetImageName);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store student documents", e);
        }

        // 4. Save to DB
        StudentEntity savedStudent = studentRepo.save(student);

        emailService.sendSimpleEmail(
                savedStudent.getEmail(),
                "Your Registration Request was successfully sent",
                "Hi " + savedStudent.getName() + ",\n\n" +
                        "Your registration request has been successfully submitted to the HOD. " +
                        "You will be notified once it's approved.\n\nBest Regards,\nAdmin"
        );

        // 6. Notify HOD (hardcoded or fetched)
        emailService.sendSimpleEmail(
                "santhoshkumar@dbcyelagiri.edu.in",
                savedStudent.getName() + " sent a registration request",
                "Student " + savedStudent.getName() + " has requested registration.\nPlease review and approve it."
        );

        // 6. Return DTO
        return mapToDto(savedStudent);
    }


    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    @Override
    public StudentResDto getStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;

        if (username == null || username.equals("anonymousUser")) {
            throw new RuntimeException("Unauthenticated access - no user found in security context.");
        }

        // 2. Fetch user by username
        UsersEntity user = usersRepo.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found for username: " + username);
        }

        // 3. Validate user role
        if (user.getRole() == null || !"ROLE_STUDENT".equals(user.getRole().getName())) {
            throw new RuntimeException("Authenticated user must be a student.");
        }

        // 4. Fetch student entity
        StudentEntity student = studentRepo.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found for user ID: " + user.getId()));

        // 5. Map to DTO and return
        return mapToStudent(student);
    }

    private StudentResDto mapToStudent(StudentEntity student) {
        if (student == null) {
            throw new IllegalArgumentException("Student entity cannot be null");
        }

        StudentResDto dto = new StudentResDto();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setAge(student.getAge());
        dto.setGender(student.getGender());
        dto.setEmail(student.getEmail());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setStatus(student.getStatus().toString());

        // Handle course status
        dto.setCourseStatus(student.getCourseStatus() != null ?
                student.getCourseStatus().name() :
                "NOT_SPECIFIED");

        // Handle department - using the direct entity relationship
        if (student.getDepartment() != null) {
            dto.setDepartmentName(student.getDepartment().getDepartmentName());

        } else {
            dto.setDepartmentName("Not Assigned");
        }

        // Handle course
        if (student.getCourse() != null) {
            dto.setCourseName(student.getCourse().getCourseName());
        } else {
            dto.setCourseName("Not Enrolled");
        }

        // Handle subject

// Option 1: If dto.setSubjectName() expects a List<String> (subject names)
        if (student.getSubjects() != null && !student.getSubjects().isEmpty()) {
            List<String> subjectNames = student.getSubjects().stream()
                    .map(SubjectEntity::getSubjectName) // Assuming getName() method exists
                    .collect(Collectors.toList());
            dto.setSubjectName(subjectNames);
        } else {
            dto.setSubjectName(Collections.singletonList("Not Registered"));
        }

        dto.setProfileImagePath(student.getProfileImagePath());
        dto.setMarksheetImagePath(student.getMarksheetImagePath());
        dto.setUsername(student.getUser().getUsername());
        return dto;
    }

    @Override
    public StudentResDto registerCourse(Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Step 2: Get UsersEntity by username
        UsersEntity user = usersRepo.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found.");
        }

        // Step 3: Get StudentEntity by user
        StudentEntity student = studentRepo.findByUser(user);
        if (student == null) {
            throw new RuntimeException("Student not found.");
        }

        // Step 4: Get CourseEntity by ID
        CourseEntity course = corerepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        // Step 5: Check for existing course request
        if (!student.getCourseStatus().equals(CourseRequestStatusEnum.NOT_REQUESTED)) {
            throw new RuntimeException("You have already submitted a course registration request.");
        }

        // Step 6: Assign course and set status
        student.setCourse(course);
        student.setCourseStatus(CourseRequestStatusEnum.PENDING);
        StudentEntity updatedStudent = studentRepo.save(student);

        // Step 7: Send confirmation emails
        String email = updatedStudent.getEmail();
        String name = updatedStudent.getName();
        String courseName = course.getCourseName();

        // Email to student
        emailService.sendSimpleEmail(
                email,
                "Course Registration Request Submitted",
                String.format(
                        "Dear %s,\n\n" +
                                "Your course registration request for **%s** has been successfully submitted and is currently under review.\n\n" +
                                "Once approved, you will receive a confirmation email from the Head of Department (HOD).\n\n" +
                                "Thank you for your interest.\n\nBest regards,\nAdmin Team",
                        name, courseName
                )
        );

        // Email to admin
        emailService.sendSimpleEmail(
                "santhoshkumar@dbcyelagiri.edu.in",
                String.format("%s has submitted a course registration request", name),
                String.format(
                        "Dear Admin,\n\n" +
                                "Student **%s** has requested to register for the course **%s**.\n\n" +
                                "Please review the request and take the necessary action.\n\nBest regards,\nCourse Registration System",
                        name, courseName
                )
        );

        // Step 8: Return student response DTO
        return mapToStudentDto(updatedStudent);
    }

    @Override
    public DepartmentDetailsDto getDetailsDept() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UsersEntity user = usersRepo.findByUsername(username);

        Optional<StudentEntity> student = studentRepo.findByUser_Id(user.getId());
        if (user == null || student.isEmpty()) {
            throw new RuntimeException("Only students can access this resource.");
        }

        DepartmentEntity department = student.get().getDepartment();
        if (department == null) {
            throw new RuntimeException("Student does not belong to any department.");
        }

        // Get HOD details
        List<UserProfileEntity> hodList = userProfileRepo.findByUser_Role_NameAndDepartment_DepartmentName("ROLE_HOD", department.getDepartmentName());
        String hodName = "Not Assigned";
        String hodEmail = "Not Available";

        if (!hodList.isEmpty()) {
            UserProfileEntity hod = hodList.get(0);
            hodName = hod.getName();
            hodEmail = hod.getEmail();
        }

        // Get Staff Names
        List<UserProfileEntity> staffList = userProfileRepo.findByUser_Role_NameAndDepartment_DepartmentName("ROLE_STAFF", department.getDepartmentName());
        List<String> staffNames = staffList.stream().map(UserProfileEntity::getName).collect(Collectors.toList());

        DepartmentDetailsDto dto = new DepartmentDetailsDto();
        dto.setDepartmentName(department.getDepartmentName());
        dto.setHodName(hodName);
        dto.setHodEmail(hodEmail);
        dto.setStaffNames(staffNames);
        return dto;
    }

    @Override
    public void saveStudentsFromExcel(MultipartFile file, String departmentName) {

        DepartmentEntity department = deptRepo.findByDepartmentName(departmentName);
        if (department == null) {
            throw new RuntimeException("Department not found: " + departmentName);
        }

        try {
            List<StudentEntity> students = ExcelHelper.convertExcelToStudents(file.getInputStream(), department);
            studentRepo.saveAll(students);
        } catch (IOException e) {
            throw new RuntimeException("Could not store Excel data: " + e.getMessage());
        }
    }

    @Override
    public List<CourseResDto> courseList() {
        List<CourseEntity> course = corerepo.findAll();
        return course.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CourseResDto> getFilteredCourses(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseEntity> coursePage;

        if (search == null || search.isBlank()) {
            coursePage = corerepo.findAll(pageable);
        } else {
            coursePage = corerepo.findByCourseNameContainingIgnoreCase(search, pageable);
        }

        return coursePage.map(this::mapToDto);
    }

    private CourseResDto mapToDto(CourseEntity course) {
        CourseResDto core = new CourseResDto();
        core.setId(course.getId());
        core.setCourseName(course.getCourseName());

        UserProfileEntity staff = course.getStaff();
        if (staff != null) {
            core.setStaffName(staff.getName());
        } else {
            core.setStaffName("Not Assigned");
        }

        return core;
    }


    private StudentResDto mapToStudentDto(StudentEntity student) {
        StudentResDto dto = new StudentResDto();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setCourseName(student.getCourse().getCourseName());
        // add other fields as needed
        return dto;
    }


    private String saveFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Empty file: " + file.getOriginalFilename());
            }

            String uploadDir = "uploads/"; // Make sure this folder exists
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return path.toString(); // You may adjust this to be relative
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + file.getOriginalFilename(), e);
        }
    }


    public StudentResDto mapToDto(StudentEntity student) {
        StudentResDto dto = new StudentResDto();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setAge(student.getAge());
        dto.setGender(student.getGender());
        dto.setEmail(student.getEmail());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setDepartmentName(student.getDepartment().getDepartmentName());
        dto.setStatus(student.getStatus().toString());
        dto.setCourseName(student.getCourseStatus().name());
        dto.setMarksheetImagePath(student.getMarksheetImagePath());
        dto.setProfileImagePath(student.getProfileImagePath());
        return dto;
    }
}
