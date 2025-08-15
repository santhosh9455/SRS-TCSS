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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
        dto.setPincode(student.getPincode());

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
        // File validation
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

        // Calculate age
        int age = (dto.getDateOfBirth() != null)
                ? Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears()
                : 0;

        StudentEntity student = new StudentEntity();
        student.setAge(age);
        student.setFirstName(defaultIfBlank(dto.getFirstName(), "Not Provided"));
        student.setLastName(defaultIfBlank(dto.getLastName(), "Not Provided"));
        student.setFatherName(defaultIfBlank(dto.getFatherName(), "Not Provided"));
        student.setFatherMobile(defaultIfBlank(dto.getFatherMobile(), "Not Provided"));
        student.setFatherOccupation(defaultIfBlank(dto.getFatherOccupation(), "Not Provided"));
        student.setMotherName(defaultIfBlank(dto.getMotherName(), "Not Provided"));
        student.setMotherMobile(defaultIfBlank(dto.getMotherMobile(), "Not Provided"));
        student.setMotherOccupation(defaultIfBlank(dto.getMotherOccupation(), "Not Provided"));
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setGender(defaultIfBlank(dto.getGender(), "Not Specified"));
        student.setEmail(defaultIfBlank(dto.getEmail(), "noemail@example.com"));
        student.setPhoneNumber(defaultIfBlank(dto.getPhoneNumber(), "Not Provided"));
        student.setProgrammeLevel(defaultIfBlank(dto.getProgrammeLevel(), "Not Specified"));
        student.setAadharNumber(defaultIfBlank(dto.getAadharNumber(), "Not Provided"));
        student.setCity(defaultIfBlank(dto.getCity(), "Not Provided"));
        student.setPincode(defaultIfBlank(dto.getPincode(), "000000"));
        student.setStreet(defaultIfBlank(dto.getStreet(), "Not Provided"));
        student.setTaluk(defaultIfBlank(dto.getTaluk(), "Not Provided"));
        student.setSchoolName(defaultIfBlank(dto.getSchoolName(), "Not Provided"));
        student.setHostelBusService(defaultIfBlank(dto.getHostelBusService(), "Not Provided"));
        student.setBoardingPoint(defaultIfBlank(dto.getBoardingPoint(), "Not Provided"));
        student.setGuardianName(defaultIfBlank(dto.getGuardianName(), "Not Provided"));
        student.setGuardian_phone(defaultIfBlank(dto.getGuardianPhone(), "Not Provided"));
        student.setEnrollment_status(defaultIfBlank(dto.getEnrollmentStatus(), "PENDING"));
        student.setAdmission_date(OffsetDateTime.now());
        student.setCourseStatus(CourseRequestStatusEnum.NOT_REQUESTED);
        student.setDepartment(dept);

        try {
            String uploadDir = "uploads/students/";
            Files.createDirectories(Paths.get(uploadDir));

            // Save profile image
            student.setProfileImagePath("/" + uploadDir + saveFile(profileImage, "profile_"));
            student.setMarksheetImagePath10th("/" + uploadDir + saveFile(marksheetImage10th, "marksheet10th_"));
            student.setMarksheetImagePath12th("/" + uploadDir + saveFile(marksheetImage12th, "marksheet12th_"));

            if (ugCertificate != null && !ugCertificate.isEmpty()) {
                student.setUgCertificate("/" + uploadDir + saveFile(ugCertificate, "ugCertificate_"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store student documents", e);
        }

        StudentEntity savedStudent = studentRepo.save(student);

        System.out.println("Email sending.....");
        // Email to student (HTML)
        String studentEmailBody = String.format(
                """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2 style="color: teal;">Registration Request Submitted Successfully</h2>
                    <p>Dear <strong>%s</strong>,</p>
                    <p>We have received your registration request for the academic programme <strong>%s</strong>.</p>
                    <p><b>Submission Date:</b> %s</p>
                    <p>Our HOD will review your application and get back to you shortly.</p>
                    <br>
                    <p>Best Regards,<br>Admin Team</p>
                </body>
                </html>
                """,
                savedStudent.getFirstName(),
                savedStudent.getProgrammeLevel(),
                OffsetDateTime.now().toLocalDate()
        );

        emailService.sendHtmlEmail(
                savedStudent.getEmail(),
                "Registration Request Received",
                studentEmailBody
        );

        // Email to HOD (HTML)
        String hodEmailBody = String.format(
                """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2 style="color: navy;">New Student Registration Request</h2>
                    <p>A new Application request has been submitted:</p>
                    <ul>
                        <li><b>Name:</b> %s %s</li>
                        <li><b>Programme:</b> %s</li>
                        <li><b>Submitted On:</b> %s</li>
                    </ul>
                    <p>Please review and process this request.</p>
                    <br>
                    <p>Best Regards,<br>System Notification</p>
                </body>
                </html>
                """,
                savedStudent.getFirstName(),
                savedStudent.getLastName(),
                savedStudent.getProgrammeLevel(),
                OffsetDateTime.now().toLocalDate()
        );

        emailService.sendHtmlEmail(
                "santhoshkumar@dbcyelagiri.edu.in",
                "New Student Registration Request",
                hodEmailBody
        );

        System.out.println("Email Completed.....");

        return mapToDto(savedStudent);
    }

    // Utility to save file
    private String saveFile(MultipartFile file, String prefix) throws IOException {
        String uploadDir = "uploads/students/";
        String fileName = prefix + UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
        Path filePath = Paths.get(uploadDir + fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    // Utility for default values
    private String defaultIfBlank(String value, String defaultVal) {
        return (value == null || value.trim().isEmpty()) ? defaultVal : value.trim();
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

        // Basic Info
        dto.setId(student.getId() != null ? student.getId() : 0L);
        dto.setFirstName(Optional.ofNullable(student.getFirstName()).orElse("Unknown"));
        dto.setLastName(Optional.ofNullable(student.getLastName()).orElse("Unknown"));
        dto.setAge(Optional.ofNullable(student.getAge()).orElse(0));
        dto.setGender(Optional.ofNullable(student.getGender()).orElse("Not Specified"));
        dto.setEmail(Optional.ofNullable(student.getEmail()).orElse("no-email@example.com"));
        dto.setPhoneNumber(Optional.ofNullable(student.getPhoneNumber()).orElse("0000000000"));

        // Department
        dto.setDepartmentId(student.getDepartment() != null ? student.getDepartment().getId() : null);
        dto.setDepartmentName(student.getDepartment() != null
                ? student.getDepartment().getDepartmentName()
                : "Not Assigned");

        // Programme Level
        dto.setProgrammeLevel(Optional.ofNullable(student.getProgrammeLevel()).orElse("Not Specified"));

        // Course
        dto.setCourseId(student.getCourse() != null ? student.getCourse().getId() : null);
        dto.setCourseName(student.getCourseStatus() != null
                ? student.getCourseStatus().name()
                : "NOT_REQUESTED");

        // Subject
        if (student.getSubjects() != null && !student.getSubjects().isEmpty()) {
            List<String> subjectNames = student.getSubjects().stream()
                    .map(SubjectEntity::getSubjectName)
                    .filter(Objects::nonNull)
                    .toList();
            dto.setSubjectName(subjectNames);

            List<Long> subjectIds = student.getSubjects().stream()
                    .map(SubjectEntity::getId)
                    .filter(Objects::nonNull)
                    .toList();
            dto.setSubjectId(subjectIds);
        } else {
            dto.setSubjectName(Collections.singletonList("Not Assigned"));
            dto.setSubjectId(Collections.emptyList());
        }
        // Status
        dto.setStatus(Optional.ofNullable(student.getStatus()).map(Enum::toString).orElse("PENDING"));
        dto.setEnrollment_status(Optional.ofNullable(student.getEnrollment_status()).orElse("PENDING"));

        // Address
        dto.setStreet(Optional.ofNullable(student.getStreet()).orElse("N/A"));
        dto.setTaluk(Optional.ofNullable(student.getTaluk()).orElse("N/A"));
        dto.setCity(Optional.ofNullable(student.getCity()).orElse("N/A"));
        dto.setPincode(Optional.ofNullable(student.getPincode()).orElse("000000"));
        dto.setDistrict(Optional.ofNullable(student.getDistrict()).orElse("N/A"));
        // Guardian
        dto.setGuardianName(Optional.ofNullable(student.getGuardianName()).orElse("N/A"));
        dto.setGuardian_phone(Optional.ofNullable(student.getGuardian_phone()).orElse("0000000000"));

        // Parent Info
        dto.setFatherName(Optional.ofNullable(student.getFatherName()).orElse("N/A"));
        dto.setFatherMobile(Optional.ofNullable(student.getFatherMobile()).orElse("0000000000"));
        dto.setFatherOccupation(Optional.ofNullable(student.getFatherOccupation()).orElse("N/A"));

        dto.setMotherName(Optional.ofNullable(student.getMotherName()).orElse("N/A"));
        dto.setMotherMobile(Optional.ofNullable(student.getMotherMobile()).orElse("0000000000"));
        dto.setMotherOccupation(Optional.ofNullable(student.getMotherOccupation()).orElse("N/A"));

        dto.setAadharNumber(Optional.ofNullable(student.getAadharNumber()).orElse("N/A"));

        // School / Hostel Info
        dto.setSchoolName(Optional.ofNullable(student.getSchoolName()).orElse("N/A"));
        dto.setHostelBusService(Optional.ofNullable(student.getHostelBusService()).orElse("N/A"));
        dto.setBoardingPoint(Optional.ofNullable(student.getBoardingPoint()).orElse("N/A"));

        // Dates (keep null if not available)
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setAdmission_date(student.getAdmission_date());
        dto.setCreated_at(student.getCreated_at());
        dto.setUpdated_at(student.getUpdated_at());

        // File Paths (keep null if not available)
        dto.setProfileImagePath(Optional.ofNullable(student.getProfileImagePath()).orElse("/uploads/default/profile.png"));
        dto.setMarksheetImagePath10th(Optional.ofNullable(student.getMarksheetImagePath10th()).orElse("/uploads/default/marksheet10th.png"));
        dto.setMarksheetImagePath12th(Optional.ofNullable(student.getMarksheetImagePath12th()).orElse("/uploads/default/marksheet12th.png"));
        dto.setUgCertificate(Optional.ofNullable(student.getUgCertificate()).orElse("/uploads/default/ugCertificate.png"));

        // Username
        dto.setUsername(student.getUser() != null
                ? Optional.ofNullable(student.getUser().getUsername()).orElse("unknown_user")
                : "unknown_user");

        return dto;
    }


}
