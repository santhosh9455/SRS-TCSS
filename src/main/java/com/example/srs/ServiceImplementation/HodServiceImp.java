package com.example.srs.ServiceImplementation;

import com.example.srs.DTO.*;
import com.example.srs.Enum.CourseRequestStatusEnum;
import com.example.srs.Enum.StatusEnum;
import com.example.srs.Model.*;
import com.example.srs.Repository.*;
import com.example.srs.Service.HodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class HodServiceImp implements HodService {

    @Autowired
    private DepartmentRepo deptRepo;

    @Autowired
    private SubjectRepo subRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserProfileRepo profileRepo;


    @Override
    public StudentResDto approveStudent(Long id) {
        // 1. Get the authenticated HOD
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("Unauthorized HOD access.");
        }

        UserProfileEntity hodProfile = profileRepo.findByUser_Id(user.getId());
        DepartmentEntity hodDept = hodProfile.getDepartment();
        if (hodDept == null) {
            throw new RuntimeException("HOD does not belong to any department.");
        }

        // 2. Fetch the student
        StudentEntity student = studentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found."));

        // 3. Check if the student belongs to the HOD's department
        if (!student.getDepartment().getId().equals(hodDept.getId())) {
            throw new RuntimeException("You are not authorized to approve this student.");
        }

        // 4. Check if already approved
        if (student.getStatus() == StatusEnum.APPROVED) {
            throw new RuntimeException("Student " + student.getName() + " is already approved.");
        }

        // 5. Approve and save
        student.setStatus(StatusEnum.APPROVED);
        StudentEntity saved = studentRepo.save(student);

        // 6. Send notification
        emailService.sendSimpleEmail(
                saved.getEmail(),
                "Your registration request has been approved",
                saved.getName() + ", your registration request has been approved by HOD.\nWelcome to the department! \n" +
                        "Other details will be in our website once you got the Authentication details you can see that" +
                        "\n Best regards," +
                        "\n Head of the department"
        );

        return mapToDto(saved);
    }

    private StudentResDto mapToDto(StudentEntity entity) {
        StudentResDto dto = new StudentResDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAge(entity.getAge());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setGender(entity.getGender());
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().toString() : "PENDING");
        dto.setDepartmentName(entity.getDepartment().getDepartmentName());

        // Safely check if user is not null before accessing username
        if (entity.getUser() != null && entity.getUser().getUsername() != null) {
            dto.setUsername(entity.getUser().getUsername());
        } else {
            dto.setUsername("Not Found");
        }
        // Safely handle null course
        dto.setCourseName(
                entity.getCourse() != null ? entity.getCourse().getCourseName() : "Not Assigned"
        );

        dto.setCourseId(
                entity.getCourse() != null ? entity.getCourse().getId() : null
        );

        dto.setCourseStatus(entity.getCourseStatus() != null ? entity.getCourseStatus().name() : "NOT_REQUESTED");

        dto.setMarksheetImagePath(entity.getMarksheetImagePath());
        dto.setProfileImagePath(entity.getProfileImagePath());

        // Subjects
        if (entity.getSubjects() != null && !entity.getSubjects().isEmpty()) {
            List<String> subjectNames = entity.getSubjects().stream()
                    .map(SubjectEntity::getSubjectName)
                    .filter(Objects::nonNull)
                    .toList();
            dto.setSubjectName(subjectNames);

            List<Long> subjectIds = entity.getSubjects().stream()
                    .map(SubjectEntity::getId)
                    .filter(Objects::nonNull)
                    .toList();
            dto.setSubjectId(subjectIds);
        } else {
            dto.setSubjectName(Collections.singletonList("Not Assigned"));
            dto.setSubjectId(Collections.emptyList());
        }

        return dto;
    }


    @Override
    public List<StudentResDto> getAllRequestedStud() {
        // 1. Get the currently authenticated HOD
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("Unauthorized access.");
        }

        UserProfileEntity hodProfile = profileRepo.findByUser_Id(user.getId());
        DepartmentEntity hodDept = hodProfile.getDepartment();
        if (hodDept == null) {
            throw new RuntimeException("HOD does not belong to any department.");
        }

        // 2. Fetch students only from the HOD's department
        List<StudentEntity> students = studentRepo.findByDepartment_DepartmentName(hodDept.getDepartmentName());

        // 3. Map to DTO and return
        return students.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

    }

    @Override
    public StudentResDto rejectStudent(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("Unauthorized access.");
        }

        UserProfileEntity hodProfile = profileRepo.findByUser_Id(user.getId());
        DepartmentEntity hodDept = hodProfile.getDepartment();

        if (hodDept == null) {
            throw new RuntimeException("HOD does not belong to any department.");
        }

        // Step 2: Fetch the student
        StudentEntity student = studentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found in the requested list"));

        // Step 3: Validate that the student belongs to the same department
        if (!student.getDepartment().getId().equals(hodDept.getId())) {
            throw new RuntimeException("Access denied: You can only reject students from your department.");
        }

        // Step 4: Check if already rejected
        if (student.getStatus() == StatusEnum.REJECTED) {
            throw new RuntimeException("Student " + student.getName() + " is already rejected.");
        }

        // Step 5: Reject and save
        student.setStatus(StatusEnum.REJECTED);
        StudentEntity saved = studentRepo.save(student);

        // Step 6: Send email
        String name = saved.getName();
        String to = saved.getEmail();
        String subject = "Request has been rejected by HOD";
        String body = name + ", your registration request has been rejected by HOD.";
        emailService.sendSimpleEmail(to, subject, body);

        return mapToDto(saved);

    }

    @Override
    public List<StudentResDto> approvedStudentlist() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("Unauthorized access.");
        }

        UserProfileEntity hodProfile = profileRepo.findByUser_Id(user.getId());
        DepartmentEntity hodDept = hodProfile.getDepartment();

        if (hodDept == null) {
            throw new RuntimeException("HOD does not belong to any department.");
        }

        // 2. Get only APPROVED students in the same department
        List<StudentEntity> approved = studentRepo.findAllByStatusAndDepartment_Id(StatusEnum.APPROVED, hodDept.getId());

        return approved.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentResDto> rejectedStudentlist() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Get HOD user and validate
        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("Unauthorized access.");
        }

        UserProfileEntity hodProfile = profileRepo.findByUser_Id(user.getId());
        DepartmentEntity hodDept = hodProfile.getDepartment();
        if (hodDept == null) {
            throw new RuntimeException("HOD does not belong to any department.");
        }

        // Get rejected students only from the HOD's department
        List<StudentEntity> rejected = studentRepo.findAllByStatusAndDepartment_Id(StatusEnum.REJECTED, hodDept.getId());

        return rejected.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public StudentResDto getStudent(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UsersEntity user = usersRepo.findByUsername(username);

        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("Unauthorized access.");
        }

        UserProfileEntity hodProfile = profileRepo.findByUser_Id(user.getId());
        DepartmentEntity hodDept = hodProfile.getDepartment();

        if (hodDept == null) {
            throw new RuntimeException("HOD does not belong to any department.");
        }

        // 2. Fetch student
        StudentEntity student = studentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        // 3. Check if student belongs to the same department
        if (!student.getDepartment().getId().equals(hodDept.getId())) {
            throw new RuntimeException("Access denied: Student does not belong to your department.");
        }

        // 4. Return student DTO
        return mapToDto(student);
    }

    @Override
    public Map<String, String> deleteByid(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UsersEntity user = usersRepo.findByUsername(username);

        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("Unauthorized access.");
        }

        DepartmentEntity hodDept = userProfile.getDepartment();
        if (hodDept == null) {
            throw new RuntimeException("HOD does not belong to any department.");
        }

        // Find student
        StudentEntity student = studentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        // Ensure student is in same department
        if (!student.getDepartment().getId().equals(hodDept.getId())) {
            throw new RuntimeException("Access denied: Student does not belong to your department.");
        }

        String name = student.getName();
            student.setUser(null);
            student.setSubjects(null);
            student.setCourse(null);
            student.setDepartment(null);
        studentRepo.deleteById(id);
        Map<String,String> res = new HashMap<>();
        res.put("message","Student " + name + " deleted successfully");
        return res;
    }


    @Override
    public StaffResDto createStaff(StaffDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("Logged-in user or user profile not found.");
        }

        // 2. Validate department of HOD/Admin
        DepartmentEntity department = userProfile.getDepartment();
        if (department == null) {
            throw new RuntimeException("Your profile is not associated with a department.");
        }

        // 3. Validate subject
        List<SubjectEntity> subject = null;
        if (dto.getSubjectId() != null){
             subject = subRepo.findAllById(dto.getSubjectId());
            if (subject == null) {
                throw new RuntimeException("Subject not found");
            }
        }


        // 4. Create and populate UserProfileEntity
        UserProfileEntity staffProfile = new UserProfileEntity();
        staffProfile.setName(dto.getName());
        staffProfile.setEmail(dto.getEmail());
        staffProfile.setPhoneNumber(dto.getPhoneNumber());
        staffProfile.setGender(dto.getGender());
        staffProfile.setAge(dto.getAge());
        staffProfile.setSubjects(subject);
        staffProfile.setDepartment(department);

        // 5. Optionally assign course
        if (dto.getCourseName() != null && !dto.getCourseName().isBlank()) {
            CourseEntity course = courseRepo.findByCourseName(dto.getCourseName());
            if (course == null) {
                throw new RuntimeException("The given Course does not exist.");
            }
            staffProfile.setCourse(course);
        }

        // 6. Save and return response
        UserProfileEntity savedProfile = profileRepo.save(staffProfile);
        return mapToDto(savedProfile);

    }

    private StaffResDto mapToDto(UserProfileEntity staff) {
        StaffResDto dto = new StaffResDto();

        dto.setId(staff.getId());
        dto.setName(staff.getName());
        dto.setEmail(staff.getEmail());
        dto.setAge(staff.getAge());
        dto.setDateOfBirth(staff.getDateOfBirth());
        dto.setGender(staff.getGender());
        dto.setPhoneNumber(staff.getPhoneNumber());

        // Username
        if (staff.getUser() != null && staff.getUser().getUsername() != null) {
            dto.setUsername(staff.getUser().getUsername());
        } else {
            dto.setUsername("Not Assigned");
        }

        // Department
        if (staff.getDepartment() != null && staff.getDepartment().getDepartmentName() != null) {
            dto.setDepartmentName(staff.getDepartment().getDepartmentName());
        } else {
            dto.setDepartmentName("Not Assigned");
        }

        // Subjects (assuming it's getNewSubjects() of type Set<newSubjectEntity>)
//        if(staff.getSubjects() != null){
//            List<String> subjectNames = Optional.ofNullable(staff.getSubjects())
//                    .orElse(Collections.emptyList())
//                    .stream()
//                    .map(SubjectEntity::getSubjectName)
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//            dto.setSubjectNme(subjectNames); // Ensure your DTO uses correct naming
//            if (subjectNames.isEmpty()){
//                dto.setSubjectNme(Collections.singletonList("Not Assigned"));
//            }
//        }

        // Subjects
        if (staff.getSubjects() != null && !staff.getSubjects().isEmpty()) {
            List<String> subjectNames = staff.getSubjects().stream()
                    .map(SubjectEntity::getSubjectName)
                    .filter(Objects::nonNull)
                    .toList();
            dto.setSubjectNme(subjectNames);

            List<Long> subjectIds = staff.getSubjects().stream()
                    .map(SubjectEntity::getId)
                    .filter(Objects::nonNull)
                    .toList();
            dto.setSubjectId(subjectIds);
        } else {
            dto.setSubjectNme(Collections.singletonList("Not Assigned"));
            dto.setSubjectId(Collections.emptyList());
        }




        // Course
        if (staff.getCourse() != null && staff.getCourse().getCourseName() != null) {
            dto.setCourseName(staff.getCourse().getCourseName());
            dto.setCourseId(staff.getCourse().getId());
        } else {
            dto.setCourseName("Not Assigned");
        }

        return dto;
    }



    @Override
    public SubjectResDto createSubject(String subjectName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("User profile not found.");
        }

        UserProfileEntity profile = profileRepo.findByUser_Id(user.getId());

        DepartmentEntity department = profile.getDepartment();
        if (department == null) {
            throw new RuntimeException("HOD is not assigned to a department.");
        }

        // Check if subject already exists in this department
        SubjectEntity existing = subRepo.findBySubjectNameAndDepartment_DepartmentName(
                subjectName.toUpperCase(), department.getDepartmentName());

        if (existing != null) {
            throw new RuntimeException("Subject already exists in your department.");
        }

        SubjectEntity subject = new SubjectEntity();
        subject.setSubjectName(subjectName.toUpperCase());
        subject.setDepartment(department);

        SubjectEntity saved = subRepo.save(subject);

        SubjectResDto resDto = new SubjectResDto();
        resDto.setId(saved.getId());
        resDto.setSubjectName(saved.getSubjectName());
        resDto.setDepartmentName(department.getDepartmentName());
        return resDto;

    }

    @Override
    public List<SubjectResDto> getAllSubject() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 2. Fetch the user entity
        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("User profile not found.");
        }

        // 3. Check if the user is a HOD
        String role = user.getRole().getName();
        if (!"ROLE_HOD".equals(role)) {
            throw new RuntimeException("Only HODs are allowed to view department subjects.");
        }

        // 4. Get HOD's department
        DepartmentEntity department = userProfile.getDepartment();
        if (department == null) {
            throw new RuntimeException("HOD does not belong to any department.");
        }

        // 5. Fetch all subjects in this department
        List<SubjectEntity> subjects = subRepo.findByDepartment_DepartmentName(department.getDepartmentName());

        if (subjects.isEmpty()) {
            throw new RuntimeException("No subjects found for your department.");
        }

        // 6. Convert to DTO
        return subjects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

    }

    private SubjectResDto mapToDto(SubjectEntity entity) {
        SubjectResDto dto = new SubjectResDto();
        dto.setId(entity.getId());
        dto.setSubjectName(entity.getSubjectName());
        if (entity.getDepartment() != null) {
            dto.setDepartmentName(entity.getDepartment().getDepartmentName());
        }
        return dto;
    }

    @Override
    public SubjectResDto getSubjectByName(String subjectName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 2. Fetch the user entity
        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("User profile not found.");
        }

        // 3. Ensure the user is a HOD
        String role = user.getRole().getName();
        if (!"ROLE_HOD".equals(role)) {
            throw new RuntimeException("Access denied. Only HODs can view subject details.");
        }

        // 4. Get HOD's department
        DepartmentEntity department = userProfile.getDepartment();
        if (department == null) {
            throw new RuntimeException("HOD does not belong to any department.");
        }

        // 5. Fetch subject by name and department
        SubjectEntity subject = subRepo.findBySubjectNameAndDepartment_DepartmentName(
                subjectName.toUpperCase(), department.getDepartmentName()
        );

        if (subject == null) {
            throw new RuntimeException("Subject not found in your department with name: " + subjectName);
        }

        // 6. Map to DTO
        return mapToDto(subject);
    }

    @Override
    public SubjectResDto updateSubjectName(Long id, String newSubjectName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("User profile not found.");
        }

        if (!"ROLE_HOD".equals(user.getRole().getName())) {
            throw new RuntimeException("Only HODs are allowed to update subjects.");
        }

        DepartmentEntity hodDept = userProfile.getDepartment();
        if (hodDept == null) {
            throw new RuntimeException("HOD does not belong to any department.");
        }

        // 2. Find subject by ID
        SubjectEntity existingSubject = subRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with ID: " + id));

        // 3. Ensure subject belongs to the same department as the HOD
        if (!existingSubject.getDepartment().getId().equals(hodDept.getId())) {
            throw new RuntimeException("You are not allowed to update subjects from another department.");
        }

        // 4. Check for duplicate subject name within department
        SubjectEntity duplicateCheck = subRepo.findBySubjectNameAndDepartment_DepartmentName(
                newSubjectName.toUpperCase(),
                hodDept.getDepartmentName()
        );

        if (duplicateCheck != null && !duplicateCheck.getId().equals(id)) {
            throw new RuntimeException("Another subject already exists with the name: " + newSubjectName);
        }

        // 5. Update
        existingSubject.setSubjectName(newSubjectName.toUpperCase());
        SubjectEntity updated = subRepo.save(existingSubject);

        return mapToDto(updated);

    }

    @Override
    public StaffResDto updateStaff(Long id, StaffResDto dto) {
        // 1. Fetch staff profile (UserProfileEntity)
        UserProfileEntity staff = profileRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + id));

        // 2. Update basic fields
        if (dto.getName() != null) staff.setName(dto.getName());
        if (dto.getEmail() != null) staff.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) staff.setPhoneNumber(dto.getPhoneNumber());

        if (dto.getGender() != null) staff.setGender(dto.getGender());
        if (dto.getDateOfBirth() != null) {
            staff.setDateOfBirth(dto.getDateOfBirth());
            int age = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
            staff.setAge(age);
        }
        // 3. Update department
        if (dto.getDepartmentName() != null) {
            DepartmentEntity department = deptRepo.findByDepartmentName(dto.getDepartmentName());
            if (department == null) {
                throw new RuntimeException("Department not found: " + dto.getDepartmentName());
            }
            staff.setDepartment(department);
        }

        // 4. Update subject
//        if (dto.getSubjectNme() != null) {
//            List<SubjectEntity> subject = subRepo.findBySubjectName(dto.getUpSubjectName());
//            if (subject == null) {
//                throw new RuntimeException("Subject not found: " + dto.getSubjectNme());
//            }
//            staff.setSubjects(subject);
//        }

        if (dto.getSubjectId() != null) {
            List<SubjectEntity> subject = subRepo.findAllById(dto.getSubjectId());
            staff.setSubjects(subject);
        }

        if (dto.getCourseId() != null) {

            boolean courseExists = profileRepo.existsByCourseId(dto.getCourseId());
            Optional<UserProfileEntity> existingUser = profileRepo.findByCourseId(dto.getCourseId());

            if (courseExists && existingUser.isPresent() && !existingUser.get().getId().equals(staff.getId())) {
                throw new RuntimeException("Course already assigned to another staff");
            } else {
                CourseEntity course = courseRepo.findById(dto.getCourseId())
                        .orElseThrow(() -> new RuntimeException("Course not found with ID: " + dto.getCourseId()));
                staff.setCourse(course);
            }
        }

        // 6. Save and return updated profile
        UserProfileEntity updated = profileRepo.save(staff);
        return mapToDto(updated);

    }

    @Override
    public Map<String, String> deleteStaff(Long id) {
        UserProfileEntity Staff =profileRepo.findById(id)
                        .orElseThrow(()-> new RuntimeException("Staff not Found"));
        if(Staff != null){
            Staff.setUser(null);
            Staff.setCourse(null);
            Staff.setDepartment(null);
            Staff.setSubjects(null);
        }
        profileRepo.deleteById(id);
        Map<String,String> response = new HashMap<>();
        response.put("message","Staff Deleted successfully");
        return response;

    }

    @Override
    public Map<String, String> deleteSubject(Long id) {
        SubjectEntity sub = subRepo.findById(id)
                        .orElseThrow(()->new RuntimeException("Subject not found with give ID"));
        if(sub != null){
            sub.setDepartment(null);
            sub.setStaffProfiles(null);
        }
        subRepo.deleteById(id);
        Map<String,String> response = new HashMap<>();
        response.put("message","Subject Deleted successfully");
        return response;

    }

    @Override
    public List<StaffResDto> getAllStaff() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UsersEntity loggedInUser = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(loggedInUser.getId());
        if (loggedInUser == null || userProfile == null) {
            throw new RuntimeException("Unauthorized access. User profile not found.");
        }

        DepartmentEntity department = userProfile.getDepartment();
        if (department == null) {
            throw new RuntimeException("HOD is not associated with any department.");
        }

        List<UserProfileEntity> filtered = profileRepo.findAll().stream()
                .filter(profile ->
                        profile.getUser() != null &&
                                profile.getDepartment() != null &&
                                profile.getDepartment().getId().equals(department.getId()))
                .toList();

        return filtered.stream().map(this::mapToDto).collect(Collectors.toList());

    }

    @Override
    public CourseResDto createCourse(String courseName) {
        CourseEntity exist = courseRepo.findByCourseName(courseName);
        if (exist != null) throw new RuntimeException("Course already created");
        CourseEntity course = new CourseEntity();
        course.setCourseName(courseName);

        CourseEntity savedCourse = courseRepo.save(course);

        CourseResDto dto = new CourseResDto();
        dto.setId(savedCourse.getId());
        dto.setCourseName(savedCourse.getCourseName());
        return dto;

    }

    @Override
    public List<CourseResDto> getAllCourse() {
        List<CourseEntity> courses = courseRepo.findAll(); // assuming courseRepo is injected

        return courses.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

    }
    private CourseResDto mapToDto(CourseEntity course) {
        CourseResDto dto = new CourseResDto();
        dto.setId(course.getId());
        dto.setCourseName(course.getCourseName());


        if (course.getStaff() != null) {
            dto.setStaffName(course.getStaff().getName());
        } else {
            dto.setStaffName("Not Assigned");
        }

        return dto;
    }


    @Override
    public Map<String, String> deleteCourse(Long id) {
        CourseEntity core = courseRepo.findById(id)
                        .orElseThrow(()->new RuntimeException("ID not found"));
        if (core != null){
            core.setStaff(null);
            core.setStudents(null);
            core.setCourseName(null);
        }else {
            throw new RuntimeException("Course data is null");
        }
        Map<String,String> response = new HashMap<>();
        response.put("message","Course delete successfully");
        courseRepo.deleteById(id);
        return response;

    }

    @Override
    public StudentResDto updateStudent(Long studentId, StudentUpdateRequestDto dto, MultipartFile profileImage, MultipartFile marksheetImage) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("User profile not found.");
        }

        // 2. Check HOD role
        if (!"ROLE_HOD".equals(user.getRole().getName())) {
            throw new RuntimeException("Only HODs are allowed to update student data.");
        }

        DepartmentEntity hodDept = userProfile.getDepartment();
        if (hodDept == null) {
            throw new RuntimeException("HOD does not belong to any department.");
        }

        // 3. Find student and check department match
        StudentEntity student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        if (!student.getDepartment().getId().equals(hodDept.getId())) {
            throw new RuntimeException("You can only update students from your own department.");
        }

        //  Continue with your existing update logic...
        if (dto.getName() != null && !dto.getName().isBlank()) {
            student.setName(dto.getName());
        }
        int age = 0;
        if (dto.getDateOfBirth() != null) {
            age = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
            student.setAge(age);
            student.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getGender() != null && !dto.getGender().isBlank()) {
            student.setGender(dto.getGender());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            student.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            student.setPhoneNumber(dto.getPhoneNumber());
        }

        if (dto.getCourseId() != null){
            CourseEntity core = courseRepo.findById(dto.getCourseId())
                    .orElseThrow(()->new RuntimeException("Course not found"));
            student.setCourse(core);
            student.setCourseStatus(CourseRequestStatusEnum.APPROVED);
        }
        if (dto.getSubjectId() != null){
            List<SubjectEntity> subjects = subRepo.findAllById(dto.getSubjectId());
            student.setSubjects(subjects);
        }
        // Optional: If you allow HODs to move students between departments (usually no)
        if (dto.getDepartmentId() != null ) {
            DepartmentEntity newDept = deptRepo.findById(dto.getDepartmentId())
                    .orElseThrow(()-> new RuntimeException("Department '" + dto.getDepartmentId() + "' not found"));

            if (!newDept.getId().equals(hodDept.getId())) {
                throw new RuntimeException("You are not allowed to move student to another department.");
            }

            student.setDepartment(newDept);
        }

        // File upload logic remains the same
        try {
            String uploadDir = "uploads/students/";
            Files.createDirectories(Paths.get(uploadDir));

            if (profileImage != null && !profileImage.isEmpty()) {
                String profileImageName = "profile_" + UUID.randomUUID() + getFileExtension(profileImage.getOriginalFilename());
                Path profileImagePath = Paths.get(uploadDir, profileImageName);
                Files.copy(profileImage.getInputStream(), profileImagePath, StandardCopyOption.REPLACE_EXISTING);
                student.setProfileImagePath("/" + uploadDir + profileImageName);
            }

            if (marksheetImage != null && !marksheetImage.isEmpty()) {
                String marksheetImageName = "marksheet_" + UUID.randomUUID() + getFileExtension(marksheetImage.getOriginalFilename());
                Path marksheetImagePath = Paths.get(uploadDir, marksheetImageName);
                Files.copy(marksheetImage.getInputStream(), marksheetImagePath, StandardCopyOption.REPLACE_EXISTING);
                student.setMarksheetImagePath("/" + uploadDir + marksheetImageName);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to store updated images", e);
        }

        StudentEntity updatedStudent = studentRepo.save(student);
        return mapToDto(updatedStudent);
    }

    @Override
    public Page<StudentResDto> getFilteredRequestedStudents(String name, StatusEnum status, int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null || userProfile.getDepartment() == null) {
            throw new RuntimeException("Unauthorized or invalid department");
        }

        Long deptId = userProfile.getDepartment().getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<StudentEntity> studentPage = studentRepo.findFilteredByDepartmentAndStatusAndName(
                deptId,
                (name != null && !name.isBlank()) ? name : null,
                status,
                pageable
        );

        return studentPage.map(this::mapToDto);
    }

    @Override
    public Page<CourseResDto> filteredCourse(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("courseName").ascending());

        Page<CourseEntity> courseEntities = courseRepo.findByCourseNameContainingIgnoreCase(search, pageable);

        return courseEntities.map(this::mapToDto); // map each CourseEntity to CourseResDto
    }

    @Override
    public CourseResDto updateCourse(CourseResDto dto) {


        CourseEntity core = courseRepo.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Course not found with ID of " + dto.getId()));

        // Update course name if provided and different
        if (dto.getCourseName() != null && !dto.getCourseName().isBlank()) {
            String incomingName = dto.getCourseName().trim();
            if (!incomingName.equals(core.getCourseName())) {
                CourseEntity existing = courseRepo.findByCourseName(incomingName);
                if (existing != null) {
                    throw new RuntimeException(incomingName + " already exists");
                }
                core.setCourseName(incomingName);
            }
        }

        // Update staff if provided
        if (dto.getStaffName() != null && !dto.getStaffName().isBlank()) {
            String staffName = dto.getStaffName().trim();
            UserProfileEntity staff = profileRepo.findByName(staffName);
            if (staff == null) {
                throw new RuntimeException(staffName + " not found");
            }

            CourseEntity currentCourse = staff.getCourse();
            if (currentCourse != null && !core.equals(currentCourse)) {
                boolean alreadyAssigned = profileRepo.existsByCourse(currentCourse);
                if (alreadyAssigned) {
                    throw new RuntimeException("Staff already assigned to another course");
                }
            }

            core.setStaff(staff);
        }

        CourseEntity saved = courseRepo.save(core);

        CourseResDto res = new CourseResDto();
        res.setId(saved.getId());
        res.setCourseName(saved.getCourseName());
        res.setStaffName(saved.getStaff() != null ? saved.getStaff().getName() : null);

        return res;
    }

    @Override
    public Page<SubjectResDto> getFilterAllSubjects(String search, Pageable pageable) {
        Page<SubjectEntity> subjectsPage;

        if (search == null || search.trim().isEmpty()) {
            subjectsPage = subRepo.findAll(pageable);
        } else {
            subjectsPage = subRepo.findBySubjectNameContainingIgnoreCase(search.trim(), pageable);
        }

        if (subjectsPage.isEmpty()) throw new RuntimeException("Subjects Not Found");

        return subjectsPage.map(this::mapToDto);
    }

    @Override
    public StaffResDto profile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UsersEntity user = usersRepo.findByUsername(username);
        UserProfileEntity userProfile = profileRepo.findByUser_Id(user.getId());
        if (user == null || userProfile == null) {
            throw new RuntimeException("user profile not found.");
        }

        return mapToDto(userProfile);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

}
