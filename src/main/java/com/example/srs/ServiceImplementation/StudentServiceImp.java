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

        // Basic Info
        dto.setId(student.getId());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setAge(student.getAge());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setGender(student.getGender());
        dto.setEmail(student.getEmail());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setStatus(student.getStatus() != null ? student.getStatus().name() : "PENDING");
        dto.setCourseStatus(student.getCourseStatus() != null ? student.getCourseStatus().name() : "NOT_SPECIFIED");

        // Academic Info
        dto.setProgrammeLevel(student.getProgrammeLevel());
        dto.setProgrammeOfStudy(student.getProgrammeOfStudy());
        dto.setAadharNumber(student.getAadharNumber());

        // Parents / Guardian Info
        dto.setFatherName(student.getFatherName());
        dto.setFatherMobile(student.getFatherMobile());
        dto.setFatherOccupation(student.getFatherOccupation());
        dto.setMotherName(student.getMotherName());
        dto.setMotherMobile(student.getMotherMobile());
        dto.setMotherOccupation(student.getMotherOccupation());
        dto.setGuardianName(student.getGuardianName());
        dto.setGuardian_phone(student.getGuardian_phone());

        // Address Info
        dto.setStreet(student.getStreet());
        dto.setTaluk(student.getTaluk());
        dto.setCity(student.getCity());
        dto.setState(student.getState());
        dto.setPincode(student.getPincode());
        dto.setCountry(student.getCountry());

        // Other Info
        dto.setSchoolName(student.getSchoolName());
        dto.setHostelBusService(student.getHostelBusService());
        dto.setBoardingPoint(student.getBoardingPoint());
        dto.setAdmission_date(student.getAdmission_date());
        dto.setCreated_at(student.getCreated_at());
        dto.setUpdated_at(student.getUpdated_at());
        dto.setEnrollment_status(student.getEnrollment_status());

        // Department
        if (student.getDepartment() != null) {
            dto.setDepartmentName(student.getDepartment().getDepartmentName());
        } else {
            dto.setDepartmentName("Not Assigned");
        }

        // Course
        if (student.getCourse() != null) {
            dto.setCourseName(student.getCourse().getCourseName());
        } else {
            dto.setCourseName("Not Enrolled");
        }

        // Subjects
        if (student.getSubjects() != null && !student.getSubjects().isEmpty()) {
            dto.setSubjectName(student.getSubjects().stream()
                    .map(SubjectEntity::getSubjectName)
                    .collect(Collectors.toList()));
        } else {
            dto.setSubjectName(Collections.singletonList("Not Registered"));
        }

        // File Paths
        dto.setProfileImagePath(student.getProfileImagePath());
        dto.setMarksheetImagePath10th(student.getMarksheetImagePath10th());
        dto.setMarksheetImagePath12th(student.getMarksheetImagePath12th());
        dto.setUgCertificate(student.getUgCertificate());

        // User Info
        dto.setUsername(student.getUser() != null ? student.getUser().getUsername() : null);

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

        String email = updatedStudent.getEmail();
        String fullName = updatedStudent.getFirstName() + " " + updatedStudent.getLastName();
        String courseName = course.getCourseName();

// Email to student (HTML)
        String studentSubject = "Course Registration Request Submitted";
        String studentBodyHtml = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                        "<h2 style='color:#2E86C1;'>Dear %s,</h2>" +
                        "<p>Your course registration request for the course <strong>%s</strong> has been successfully submitted and is currently under review.</p>" +
                        "<p>Once approved, you will receive a confirmation email from the Head of Department (HOD).</p>" +
                        "<p>Thank you for your interest in our institution.</p>" +
                        "<br>" +
                        "<p>Best regards,<br><strong>Admin Team</strong></p>" +
                        "</body>" +
                        "</html>",
                fullName, courseName
        );

// Email to admin (HTML)
        String adminSubject = String.format("%s has submitted a course registration request", fullName);
        String adminBodyHtml = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                        "<h2 style='color:#C0392B;'>Dear Admin,</h2>" +
                        "<p>Student <strong>%s</strong> has requested to register for the course <strong>%s</strong>.</p>" +
                        "<p>Please review the request and take the necessary action.</p>" +
                        "<br>" +
                        "<p>Best regards,<br><strong>Course Registration System</strong></p>" +
                        "</body>" +
                        "</html>",
                fullName, courseName
        );

// Send emails as HTML
        emailService.sendHtmlEmail(email, studentSubject, studentBodyHtml);
        emailService.sendHtmlEmail("santhoshkumar@dbcyelagiri.edu.in", adminSubject, adminBodyHtml);

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

    @Override
    public StudentResDto RegisterRequest(StudentRequestDto dto, MultipartFile profileImage, MultipartFile marksheetImage10th, MultipartFile marksheetImage12th, MultipartFile ugCertificate) {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new IllegalArgumentException("Profile image is required");
        }
        if (marksheetImage10th == null || marksheetImage10th.isEmpty()) {
            throw new IllegalArgumentException("10th Marksheet is required");
        }
        if (marksheetImage12th == null || marksheetImage12th.isEmpty()) {
            throw new IllegalArgumentException("12th Marksheet is required");
        }

        // Department validation
        DepartmentEntity dept = deptRepo.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department '" + dto.getDepartmentId() + "' not found"));

        int age = 0;
        if (dto.getDateOfBirth() != null) {
            age = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
        }

        StudentEntity student = new StudentEntity();
        student.setAge(age);
        student.setFirstName(dto.getFirstName());
        student.setFatherName(dto.getFatherName());
        student.setLastName(dto.getLastName());
        student.setFatherMobile(dto.getFatherMobile());
        student.setMotherName(dto.getMotherName());
        student.setMotherMobile(dto.getMotherMobile());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setGender(dto.getGender());
        student.setEmail(dto.getEmail());
        student.setCity(dto.getCity());
        student.setState(dto.getState());
        student.setPincode(dto.getPincode());
        student.setCountry(dto.getCountry());
        student.setGuardianName(dto.getGuardianName());
        student.setGuardian_phone(dto.getGuardianPhone());
        student.setEnrollment_status("PENDDING");
        student.setAdmission_date(LocalDate.now());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setDepartment(dept);
        student.setCreated_at(LocalDate.now());
        student.setUpdated_at(LocalDate.now());

        try {
            String uploadDir = "uploads/students/";
            Files.createDirectories(Paths.get(uploadDir));

            // Save profile image
            String profileImageName = "profile_" + UUID.randomUUID() + getFileExtension(profileImage.getOriginalFilename());
            Path profileImagePath = Paths.get(uploadDir + profileImageName);
            Files.copy(profileImage.getInputStream(), profileImagePath, StandardCopyOption.REPLACE_EXISTING);
            student.setProfileImagePath("/" + uploadDir + profileImageName);

            // Save 10th marksheet
            String marksheet10thName = "marksheet10th_" + UUID.randomUUID() + getFileExtension(marksheetImage10th.getOriginalFilename());
            Path marksheet10thPath = Paths.get(uploadDir + marksheet10thName);
            Files.copy(marksheetImage10th.getInputStream(), marksheet10thPath, StandardCopyOption.REPLACE_EXISTING);
            student.setMarksheetImagePath10th("/" + uploadDir + marksheet10thName);

            // Save 12th marksheet
            String marksheet12thName = "marksheet12th_" + UUID.randomUUID() + getFileExtension(marksheetImage12th.getOriginalFilename());
            Path marksheet12thPath = Paths.get(uploadDir + marksheet12thName);
            Files.copy(marksheetImage12th.getInputStream(), marksheet12thPath, StandardCopyOption.REPLACE_EXISTING);
            student.setMarksheetImagePath12th("/" + uploadDir + marksheet12thName);

            // Save UG certificate if provided
            if (ugCertificate != null && !ugCertificate.isEmpty()) {
                String ugCertName = "ugCertificate_" + UUID.randomUUID() + getFileExtension(ugCertificate.getOriginalFilename());
                Path ugCertPath = Paths.get(uploadDir + ugCertName);
                Files.copy(ugCertificate.getInputStream(), ugCertPath, StandardCopyOption.REPLACE_EXISTING);
                student.setUgCertificate("/" + uploadDir + ugCertName);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to store student documents", e);
        }

        StudentEntity savedStudent = studentRepo.save(student);

        // Email to student
        emailService.sendSimpleEmail(
                savedStudent.getEmail(),
                "Your Registration Request was successfully sent",
                "Hi " + savedStudent.getFirstName() + ",\n\n" +
                        "Your registration request has been successfully submitted to the HOD. " +
                        "You will be notified once it's approved.\n\nBest Regards,\nAdmin"
        );

        // Email to HOD
        emailService.sendSimpleEmail(
                "santhoshkumar@dbcyelagiri.edu.in",
                savedStudent.getFirstName() + " sent a registration request",
                "Student " + savedStudent.getFirstName() + " has requested registration.\nPlease review and approve it."
        );

        return mapToDto(savedStudent);
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
        if (student == null) {
            throw new IllegalArgumentException("Student entity cannot be null");
        }

        StudentResDto dto = new StudentResDto();
        dto.setId(student.getId());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setAge(student.getAge());
        dto.setGender(student.getGender());
        dto.setEmail(student.getEmail());
        dto.setPhoneNumber(student.getPhoneNumber());

        // Department check
        dto.setDepartmentName(student.getDepartment() != null
                ? student.getDepartment().getDepartmentName()
                : "Not Assigned");

        // Status check
        dto.setStatus(student.getStatus() != null
                ? student.getStatus().toString()
                : "PENDING");

        // Course status
        dto.setCourseName(student.getCourseStatus() != null
                ? student.getCourseStatus().name()
                : "NOT_SPECIFIED");

        // Address fields
        dto.setStreet(student.getStreet());
        dto.setTaluk(student.getTaluk());
        dto.setCity(student.getCity());
        dto.setState(student.getState());
        dto.setPincode(student.getPincode());
        dto.setCountry(student.getCountry());

        // Guardian details
        dto.setGuardianName(student.getGuardianName());
        dto.setGuardian_phone(student.getGuardian_phone());

        // Dates
        dto.setAdmission_date(student.getAdmission_date());
        dto.setCreated_at(student.getCreated_at());
        dto.setUpdated_at(student.getUpdated_at());

        // Enrollment status
        dto.setEnrollment_status(student.getEnrollment_status());

        // Document paths
        dto.setProfileImagePath(student.getProfileImagePath());
        dto.setMarksheetImagePath10th(student.getMarksheetImagePath10th());
        dto.setMarksheetImagePath12th(student.getMarksheetImagePath12th());
        dto.setUgCertificate(student.getUgCertificate());

        // Username
        dto.setUsername(student.getUser() != null ? student.getUser().getUsername() : null);

        return dto;
    }

}
