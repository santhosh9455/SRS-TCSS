package com.example.srs.ServiceImplementation;

import com.example.srs.DTO.*;
import com.example.srs.Enum.CourseRequestStatusEnum;
import com.example.srs.Enum.StatusEnum;
import com.example.srs.Model.*;
import com.example.srs.Repository.*;
import com.example.srs.Service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
;

@Service
public class AdminServiceImp implements AdminService {


    @Autowired
    private DepartmentRepo deptRepo;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private SubjectRepo subjectRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SubjectRepo subRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private RoleRepo role;

    @Autowired
    private UserProfileRepo profileRepo;


    @Override
    public HodResDto createHod(HodDto dto) {
        DepartmentEntity department = deptRepo.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found: " + dto.getDepartmentId()));

        // 2. Validate subject (optional)
        List<SubjectEntity> subject = null;
        if (dto.getSubjectId() != null) {
            subject = subjectRepo.findAllById(dto.getSubjectId());


            if (subject == null){
                throw new RuntimeException("Subject not found: " + dto.getSubjectId());
            }
            // Check if subject is already assigned
            List<UserProfileEntity> subjectAssignedUser = profileRepo.findBySubjectIds(dto.getSubjectId());
            if (!subjectAssignedUser.isEmpty()) {
                throw new RuntimeException("This subject is already assigned to another user. "+dto.getSubjectId());
            }
        }

        // 3. Validate course (optional)
        CourseEntity course = null;
        if (dto.getCourseId() != null) {
            course = courseRepo.findById(dto.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Course not found: " + dto.getCourseId()));

            // Check if course is already assigned
            Optional<UserProfileEntity> courseAssignedUser = profileRepo.findByCourseId(dto.getCourseId());
            if (courseAssignedUser.isPresent()) {
                throw new RuntimeException("This course is already assigned to another user.");
            }
        }

        // 4. Validate role
        RoleEntity hodRole = roleRepo.findById(dto.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // 5. Calculate age
        int age = 0;
        if (dto.getDateOfBirth() != null) {
            age = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
        }

        // 6. Create user profile
        UserProfileEntity profile = new UserProfileEntity();
        profile.setName(dto.getName());
        profile.setEmail(dto.getEmail());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setGender(dto.getGender());
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setAge(age);
        profile.setDepartment(department);
        profile.setSubjects(subject);
        profile.setCourse(course);

        UserProfileEntity savedProfile = profileRepo.save(profile);

        // 7. Prepare response DTO
        HodResDto res = new HodResDto();
        res.setId(savedProfile.getId());
        res.setName(savedProfile.getName());
        res.setEmail(savedProfile.getEmail());
        res.setPhoneNumber(savedProfile.getPhoneNumber());
        res.setGender(savedProfile.getGender());
        res.setAge(savedProfile.getAge());
        res.setDate(savedProfile.getDateOfBirth());
        res.setDepartmentName(department.getDepartmentName());
        List<String> subjectNames = Optional.ofNullable(savedProfile.getSubjects())
                .orElse(Collections.emptyList())
                .stream()
                .map(SubjectEntity::getSubjectName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        res.setSubjectNames(subjectNames);  // Assuming subjectName is a List<String>

        res.setCourseName(course != null ? course.getCourseName() : null);

        return res;
    }

    @Override
    public UsersResDto createUsers(UsersDto dto) {

        UsersEntity user = usersRepo.findByUsername(dto.getUsername());
        if (user != null) throw new RuntimeException("User already exist with name of" + dto.getUsername());
        UsersEntity newUser = new UsersEntity();
        newUser.setUsername(dto.getUsername());
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        RoleEntity role = roleRepo.findById(dto.getRoleId())
                .orElseThrow(()->new RuntimeException("Role not found"));
        newUser.setRole(role);

        UsersEntity saved = usersRepo.save(newUser);

        UsersResDto resDto = new UsersResDto();
        resDto.setId(saved.getId());
        if (saved.getRole() != null) {
            resDto.setRoleId(saved.getRole().getId());  // ✅ Use ID, not name
        }
        resDto.setPassword(saved.getPassword());
        resDto.setUsername(saved.getUsername());
        resDto.setRole(saved.getRole().getName());
        return resDto;
    }


    private UsersResDto mapToDto(UserProfileEntity saved) {
        UsersResDto resDto = new UsersResDto();
        resDto.setId(saved.getId());
        resDto.setUsername(saved.getUser().getUsername());
        resDto.setPassword(saved.getUser().getPassword());
        if (saved.getUser() != null) {
            resDto.setRole(saved.getUser().getRole().getName());
            resDto.setRoleId(saved.getUser().getRole().getId());  // ✅ Use ID, not name
        }

        return resDto;
    }

    @Override
    public List<UsersResDto> getAllUsers() {
        List<UsersEntity> users = usersRepo.findAll();
        return users.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UsersResDto updateUsers(Long userId, UsersResDto dto) {
        UsersEntity user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check for duplicate username (exclude current user's username)
        if (dto.getUsername() != null &&
                !dto.getUsername().equals(user.getUsername()) &&
                usersRepo.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("This username is already taken.");
        }

        // Update username if provided
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            user.setUsername(dto.getUsername());
        }

        // Update password if provided
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Update role if provided
        if (dto.getRoleId() != null) {
            RoleEntity role = roleRepo.findById(dto.getRoleId())
                    .orElseThrow(()->new RuntimeException("Role not found with ID : " + dto.getRoleId()));

            user.setRole(role);
        }

        UsersEntity saved = usersRepo.save(user);
        return mapToDto(saved);
    }

    @Override
    public UsersResDto roleToStaff(Long UserId, Long staffId) {
        // Step 1: Find the user by username
        UsersEntity user = usersRepo.findById(UserId).orElseThrow(() -> new RuntimeException("Username not found."));

        // Step 2: Check if the user is already assigned to a staff
        if (profileRepo.existsByUser(user)) {
            throw new RuntimeException("This user is already assigned to another Person.");
        }

        // Step 3: Find the existing staff by staffId
        UserProfileEntity staff = profileRepo.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + staffId));

        // Step 4: Assign the user to the staff
        staff.setUser(user);

        // Step 5: Save updated staff
        UserProfileEntity saved = profileRepo.save(staff);

        // Step 6: Return DTO (optional)
        return mapToDto(saved);  // This should convert StaffEntity to UsersResDto
    }

    @Override
    public UsersResDto roleToHod(String username, Long targetId ) {
        // Step 1: Find the user by username
        UsersEntity user = usersRepo.findByUsername(username);
        if (user == null) throw new RuntimeException("Username not found.");

        // Step 2: Check if the user is already assigned to a staff
        if (profileRepo.existsByUser(user)) {
            throw new RuntimeException("This username and Auth is already assigned to another User.");
        }

        // Step 3: Find the existing staff by staffId
        UserProfileEntity hod = profileRepo.findById(targetId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + targetId));

        // Step 4: Assign the user to the staff
        hod.setUser(user);

        // Step 5: Save updated staff
        UserProfileEntity saved = profileRepo.save(hod);

        // Step 6: Return DTO (optional)
        return mapToDto(saved);  //
    }

    @Override
    public UsersResDto authStud(String username, Long studId) {
        // Step 1: Find the user by username
        UsersEntity user = usersRepo.findByUsername(username);
        if (user == null) throw new RuntimeException("Username not found.");

        // Step 2: Check if the user is already assigned to a student
        if (studentRepo.existsByUser(user)) {
            throw new RuntimeException("This user is already assigned to another student.");
        }

        // Step 3: Find the existing student by ID
        StudentEntity student = studentRepo.findById(studId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studId));

        // Step 4: Assign the user to the student
        student.setUser(user);

        // Step 5: Save updated student
        StudentEntity saved = studentRepo.save(student);

        // Step 6: Return mapped user response (customize if needed)
        return mapToUserDto(user); // You should write this mapper
    }

    @Override
    public void assignSubjectToStudents(Long departmentId, Long subjectId) {
        DepartmentEntity department = deptRepo.findById(departmentId)
                .orElseThrow(()->new RuntimeException("Department not found"));


        List<SubjectEntity> subject = subjectRepo.findAllById(subjectId);

        List<StudentEntity> students = studentRepo.findByDepartment(department);

        if (students.isEmpty()) {
            throw new RuntimeException("No students found without subject in this department");
        }

        for (StudentEntity student : students) {
            student.setSubjects(subject);
        }

        studentRepo.saveAll(students);
    }



    @Override
    public List<StudentResDto> getAllStudent() {
        List<StudentEntity> student = studentRepo.findAll();
        return student.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectResDto> getAllSubjects() {
        List<SubjectEntity> subjects = subjectRepo.findAll();
        if (subjects.isEmpty()) throw new RuntimeException("Subjects Not Found");
        return subjects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubjectResDto updateSub(SubjectUpDto subDto) {
        SubjectEntity subjectEntity = subjectRepo.findById(subDto.getId())
                .orElseThrow(() -> new RuntimeException("Subject Not Found with ID " + subDto.getId()));

        if (subDto.getDepartmenId() != null) {
            DepartmentEntity dept = deptRepo.findById(subDto.getDepartmenId())
                    .orElseThrow(() -> new RuntimeException("Department Not Found with ID " + subDto.getDepartmenId()));
            subjectEntity.setDepartment(dept);
        }
        if (subDto.getSubjectName() != null) {
            subjectEntity.setSubjectName(subDto.getSubjectName());
        }
        SubjectEntity saved = subjectRepo.save(subjectEntity);
        return mapToDto(saved);
    }

    @Override
    public List<RoleResDto> getRoles() {
        List<RoleEntity> roles = roleRepo.findAll();
        return roles.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StaffResDto> getAllStaffHod() {
        List<UserProfileEntity> userProfile = profileRepo.findAll();
        return userProfile.stream()
                .map(this::mapToStaffResDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> updateUserPro(UserProUpDto dto) {
        UserProfileEntity entity = profileRepo.findById(dto.getId())
                .orElseThrow(()->new RuntimeException("User not Found with ID "+dto.getId()));
        if (dto.getName() != null){
            entity.setName(dto.getName());
        }
        if (dto.getAge() != null){
            entity.setAge(dto.getAge());
        }
        if (dto.getEmail() != null){
            entity.setEmail(dto.getEmail());
        }
        if (dto.getGender() != null){
            entity.setGender(dto.getGender());
        }
        if (dto.getPhoneNumber() != null){
            entity.setPhoneNumber(dto.getPhoneNumber());
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Updated successfully");
        return response;
    }

    @Override
    public Map<String, String> deleteStudentById(Long studentId) {
        StudentEntity student = studentRepo.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with ID: " + studentId));

        // Optional but recommended: nullify relations to avoid FK constraint violations
        student.setDepartment(null);
        student.setCourse(null);
        student.setSubjects(null);
        student.setUser(null);

        // Now safely delete the student
        studentRepo.delete(student);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Student Deleted Successfully");
        return response;
    }

    @Override
    @Transactional
    public Map<String, String> deleteUserPro(Long userProId) {
        UserProfileEntity profile = profileRepo.findById(userProId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        // Break relationships (if bidirectional)
        if (profile.getUser() != null) {
            UsersEntity user = profile.getUser();
            user.setRole(null);
            profile.setUser(null);
        }

        if (profile.getCourse() != null) {
            profile.setCourse(null);
        }

        if (profile.getDepartment() != null) {
            profile.setDepartment(null);
        }

        if (profile.getSubjects() != null) {
            profile.setSubjects(null);
        }

        profileRepo.delete(profile);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        return response;
    }


    @Override
    public Map<String, String> deleteAuth(Long userAuthId) {
        UsersEntity auth = usersRepo.findById(userAuthId)
                .orElseThrow(()->new EntityNotFoundException("User Auth not found"));
        auth.setRole(null);
        UserProfileEntity user = profileRepo.findByUser_Id(userAuthId);

        if (user != null){
            user.setUser(null);
        }

        Optional<StudentEntity> student = studentRepo.findByUser_Id(userAuthId);
        student.ifPresent(studentEntity -> studentEntity.setUser(null));

        usersRepo.delete(auth);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User auth deleted successfully");
        return response;
    }

    @Override
    public  Page<UserProfileDTO> getFilteredUsers(String search, String role, Long departmentId, Pageable pageable) {
        Page<UserProfileEntity> userPage = profileRepo.findFilteredUsers(search, role, departmentId, pageable);
        System.out.println("UserPage content: " + userPage.getContent());

        return userPage.map(this::toDto);
    }

    @Override
    public List<CourseResDto> getAllCourse() {
        List<CourseEntity> courses = courseRepo.findAll(); // assuming courseRepo is injected

        return courses.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

    }

    @Override
    public Page<StudentResDto> getStudents(String search, Long departmentId, String status, Pageable pageable) {
        StatusEnum statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = StatusEnum.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value: " + status);
            }
        }

        return studentRepo.findFiltered(search, departmentId, statusEnum, pageable)
                .map(this::mapToDto);
    }

    @Override
    public Map<String, String> updateHodPartial(UserProUpdateDto dto) {
        UserProfileEntity profile = profileRepo.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getId()));

        // Validate and update department
        if (dto.getDepartmentId() != null) {
            DepartmentEntity department = deptRepo.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found..."));
            profile.setDepartment(department);
        }

        // Validate subject if updated
        if (dto.getSubjectId() != null) {
            List<SubjectEntity> subjectSet = new ArrayList<>(subjectRepo.findAllById(dto.getSubjectId()));

            List<UserProfileEntity> existingSubjectUser = profileRepo.findBySubjectIdsAndIdNot(dto.getSubjectId(), dto.getId());
//            if (!existingSubjectUser.isEmpty()) {
//                throw new RuntimeException("Subject already assigned to another user.");
//            }


            profile.setSubjects(subjectSet);

        }

        // Validate course if updated
        if (dto.getCourseId() != null) {
            CourseEntity newCourse = courseRepo.findById(dto.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // Check if this course is already assigned to someone else
            Optional<UserProfileEntity> existingCourseUser = profileRepo.findByCourseIdAndIdNot(dto.getCourseId(), dto.getId());
            if (existingCourseUser.isPresent()) {
                throw new RuntimeException("Course already assigned to another user.");
            }

            // Unlink old course (if any)
            if (profile.getCourse() != null) {
                CourseEntity oldCourse = profile.getCourse();
                oldCourse.setStaff(null);
                courseRepo.save(oldCourse);
            }

            // Link new course
            profile.setCourse(newCourse);
            newCourse.setStaff(profile);
            courseRepo.save(newCourse);
        }

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            UsersEntity user = usersRepo.findByUsername(dto.getUsername());
            if (user != null){
                boolean existUser = profileRepo.existsByUser(user);
                if (existUser){
                    throw new RuntimeException("User name already assign to another user");
                }
                profile.setUser(user);
            }else {
                throw new RuntimeException("User name not found");
            }

        }
        // Update other fields if provided
        if (dto.getName() != null) profile.setName(dto.getName());
        if (dto.getEmail() != null) profile.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) profile.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getGender() != null) profile.setGender(dto.getGender());

        if (dto.getDateOfBirth() != null) {
            profile.setDateOfBirth(dto.getDateOfBirth());
            int age = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
            profile.setAge(age);
        }

        UserProfileEntity saved = profileRepo.save(profile);

        CourseEntity course = courseRepo.findByCourseName(profile.getCourse().getCourseName());

        course.setStaff(saved);


        courseRepo.save(course);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Updated successfully");
        return response;
    }

    @Override
    public Page<UsersResDto> getFilteredUser(String search, Long roleId, Pageable pageable) {
        Page<UsersEntity> usersPage = usersRepo.findFilteredUsers(search, roleId, pageable);
        return usersPage.map(this::toResDto);
    }

    @Override
    public StudentResDto RegisterRequest(StudentRequestDto dto, MultipartFile profileImage, MultipartFile marksheetImage) {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new IllegalArgumentException("Profile image is required");
        }
        if (marksheetImage == null || marksheetImage.isEmpty()) {
            throw new IllegalArgumentException("Marksheet image is required");
        }

        // 1. Validate Department
        DepartmentEntity dept = deptRepo.findById(dto.getDepartmentId()).orElseThrow(()->new RuntimeException("Department '" + dto.getDepartmentId() + "' not found"));

        // 2. Create and populate StudentEntity
        StudentEntity student = new StudentEntity();
        student.setAge(dto.getAge());
        student.setGender(dto.getGender());
        student.setEmail(dto.getEmail());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setDepartment(dept);
        if(dto.getCourseId() != null){
            CourseEntity core = courseRepo.findById(dto.getCourseId())
                    .orElseThrow(()->new RuntimeException("Course not found"));
            student.setCourse(core);
        }
        if(dto.getSubjectId() != null){
            List<SubjectEntity> sub = subRepo.findAllById(dto.getSubjectId());
            student.setSubjects(sub);
        }
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
            student.setMarksheetImagePath10th("/" + uploadDir + marksheetImageName);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store student documents", e);
        }


        // 4. Save to DB
        StudentEntity savedStudent = studentRepo.save(student);

        // 6. Return DTO
        return mapToDto(savedStudent);
    }

    @Override
    public Page<SubjectResDto> getFilterAllSubjects(String search,Pageable pageable) {
        Page<SubjectEntity> subjectsPage;

        if (search == null || search.trim().isEmpty()) {
            subjectsPage = subjectRepo.findAll(pageable);
        } else {
            subjectsPage = subjectRepo.findBySubjectNameContainingIgnoreCase(search.trim(), pageable);
        }

        if (subjectsPage.isEmpty()) throw new RuntimeException("Subjects Not Found");

        return subjectsPage.map(this::mapToDto);
    }

    @Override
    public StudentResDto updateStudent(Long id, StudentUpdateRequestDto dto, MultipartFile profileImage, MultipartFile marksheetImage, MultipartFile marksheetImage12th, MultipartFile marksheetImage10th, MultipartFile ugCertificateFile) {
        StudentEntity student = studentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));

        // ===== Basic Details =====
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank())
            student.setFirstName(dto.getFirstName());

        if (dto.getLastName() != null && !dto.getLastName().isBlank())
            student.setLastName(dto.getLastName());

        if (dto.getDateOfBirth() != null) {
            int age = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
            student.setAge(age);
            student.setDateOfBirth(dto.getDateOfBirth());
        }

        if (dto.getAge() != null) student.setAge(dto.getAge());
        if (dto.getGender() != null && !dto.getGender().isBlank()) student.setGender(dto.getGender());

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            boolean existMail = studentRepo.existsByEmail(dto.getEmail());
            if (existMail && !dto.getEmail().equals(student.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            student.setEmail(dto.getEmail());
        }

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank())
            student.setPhoneNumber(dto.getPhoneNumber());

        // ===== Academic Details =====
        if (dto.getProgrammeLevel() != null) student.setProgrammeLevel(dto.getProgrammeLevel());
        if (dto.getProgrammeOfStudy() != null) student.setProgrammeOfStudy(dto.getProgrammeOfStudy());

        if (dto.getDepartmentId() != null) {
            DepartmentEntity dept = deptRepo.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with ID: " + dto.getDepartmentId()));
            student.setDepartment(dept);
        }

        if (dto.getCourseId() != null) {
            CourseEntity course = courseRepo.findById(dto.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Course not found with ID: " + dto.getCourseId()));
            student.setCourse(course);
        }

        if (dto.getSubjectId() != null) {
            List<SubjectEntity> subjects = subRepo.findAllById(dto.getSubjectId());
            student.setSubjects(subjects);
        }

        if (dto.getCourseStatus() != null && !dto.getCourseStatus().isBlank()) {
            try {
                student.setCourseStatus(CourseRequestStatusEnum.valueOf(dto.getCourseStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid course status: " + dto.getCourseStatus());
            }
        }

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                student.setStatus(StatusEnum.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + dto.getStatus());
            }
        }

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            UsersEntity user = usersRepo.findByUsername(dto.getUsername());
            if (user != null) {
                boolean existUser = studentRepo.existsByUser(user);
                if (existUser && !student.getUser().equals(user)) {
                    throw new RuntimeException("Username already assigned to another student");
                }
                student.setUser(user);
            } else {
                throw new RuntimeException("Username not found");
            }
        }

        // ===== Family Details =====
        if (dto.getFatherName() != null) student.setFatherName(dto.getFatherName());
        if (dto.getFatherMobile() != null) student.setFatherMobile(dto.getFatherMobile());
        if (dto.getFatherOccupation() != null) student.setFatherOccupation(dto.getFatherOccupation());

        if (dto.getMotherName() != null) student.setMotherName(dto.getMotherName());
        if (dto.getMotherMobile() != null) student.setMotherMobile(dto.getMotherMobile());
        if (dto.getMotherOccupation() != null) student.setMotherOccupation(dto.getMotherOccupation());

        if (dto.getGuardianName() != null) student.setGuardianName(dto.getGuardianName());
        if (dto.getGuardian_phone() != null) student.setGuardian_phone(dto.getGuardian_phone());

        // ===== Address Details =====
        if (dto.getStreet() != null) student.setStreet(dto.getStreet());
        if (dto.getTaluk() != null) student.setTaluk(dto.getTaluk());
        if (dto.getCity() != null) student.setCity(dto.getCity());
        if (dto.getState() != null) student.setState(dto.getState());
        if (dto.getCountry() != null) student.setCountry(dto.getCountry());
        if (dto.getPincode() != null) student.setPincode(dto.getPincode());

        // ===== Other Details =====
        if (dto.getAadharNumber() != null) student.setAadharNumber(dto.getAadharNumber());
        if (dto.getSchoolName() != null) student.setSchoolName(dto.getSchoolName());
        if (dto.getHostelBusService() != null) student.setHostelBusService(dto.getHostelBusService());
        if (dto.getBoardingPoint() != null) student.setBoardingPoint(dto.getBoardingPoint());

        if (dto.getAdmission_date() != null) student.setAdmission_date(dto.getAdmission_date());
        if (dto.getEnrollment_status() != null) student.setEnrollment_status(dto.getEnrollment_status());

        if (dto.getMarksheetImagePath10th() != null) student.setMarksheetImagePath10th(dto.getMarksheetImagePath10th());
        if (dto.getMarksheetImagePath12th() != null) student.setMarksheetImagePath12th(dto.getMarksheetImagePath12th());
        if (dto.getUgCertificate() != null) student.setUgCertificate(dto.getUgCertificate());

        // ===== File Uploads =====
        try {
            String uploadDir = "uploads/students/";
            Files.createDirectories(Paths.get(uploadDir));

            if (profileImage != null && !profileImage.isEmpty()) {
                String profileImageName = "profile_" + UUID.randomUUID() + getFileExtension(profileImage.getOriginalFilename());
                Path profileImagePath = Paths.get(uploadDir + profileImageName);
                Files.copy(profileImage.getInputStream(), profileImagePath, StandardCopyOption.REPLACE_EXISTING);
                student.setProfileImagePath("/" + uploadDir + profileImageName);
            }

            if (marksheetImage != null && !marksheetImage.isEmpty()) {
                String marksheetImageName = "marksheet_" + UUID.randomUUID() + getFileExtension(marksheetImage.getOriginalFilename());
                Path marksheetImagePath = Paths.get(uploadDir + marksheetImageName);
                Files.copy(marksheetImage.getInputStream(), marksheetImagePath, StandardCopyOption.REPLACE_EXISTING);
                student.setMarksheetImagePath10th("/" + uploadDir + marksheetImageName);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload files", e);
        }

        student.setUpdated_at(LocalDate.now());

        StudentEntity updated = studentRepo.save(student);
        return mapToDto(updated);
    }

    private UsersResDto toResDto(UsersEntity user) {
        UsersResDto dto = new UsersResDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getId());
            dto.setRole(user.getRole().getName());
        }
        return dto;
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


    public UserProfileDTO toDto(UserProfileEntity entity) {
        if (entity == null) return null;

        // Initialize lists
        List<SubjectEntity> subjectEntities = Optional.ofNullable(entity.getSubjects()).orElse(Collections.emptyList());

        List<String> subjectNames = subjectEntities.stream()
                .map(SubjectEntity::getSubjectName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Long> subjectIds = subjectEntities.stream()
                .map(SubjectEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        String username = "Not Assigned";
        String role = "No Role";

        if (entity.getUser() != null) {
            if (entity.getUser().getUsername() != null)
                username = entity.getUser().getUsername();

            if (entity.getUser().getRole() != null && entity.getUser().getRole().getName() != null)
                role = entity.getUser().getRole().getName();
        }

        String departmentName = entity.getDepartment() != null && entity.getDepartment().getDepartmentName() != null
                ? entity.getDepartment().getDepartmentName()
                : "No Department";

        Long departmentId = entity.getDepartment() != null ? entity.getDepartment().getId() : null;

        String courseName = entity.getCourse() != null && entity.getCourse().getCourseName() != null
                ? entity.getCourse().getCourseName()
                : "Not Assigned";

        Long courseId = entity.getCourse() != null ? entity.getCourse().getId() : null;

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setGender(entity.getGender());
        dto.setAge(entity.getAge());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setUsername(username);
        dto.setRole(role);
        dto.setDepartmentName(departmentName);
        dto.setDepartmentId(departmentId);
        dto.setSubjectName(subjectNames);
        dto.setSubjectId(subjectIds);  // <-- ✅ List of subject IDs
        dto.setCourseName(courseName);
        dto.setCourseId(courseId);     // <-- ✅ Course ID

        return dto;
    }







    public StaffResDto mapToStaffResDto(UserProfileEntity user){
        StaffResDto dto = new StaffResDto();
        dto.setName(user.getName());
        dto.setAge(user.getAge());
        dto.setEmail(user.getEmail());
        dto.setId(user.getId());
        dto.setGender(user.getGender());
        dto.setPhoneNumber(user.getPhoneNumber());
        if (user.getUser() != null){
            dto.setUsername(user.getUser().getUsername());
            dto.setRole(user.getUser().getRole().getName());
        }
        else {
            dto.setUsername("Not Assigned");
            dto.setRole("Not Assigned");
        }
        if (user.getCourse() != null){
            dto.setCourseName(user.getCourse().getCourseName());
        }
        else {
            dto.setCourseName("Not Assigned");
        }
        if (user.getDepartment() != null){
            dto.setDepartmentName(user.getDepartment().getDepartmentName());
        }else {
            dto.setDepartmentName("Not Assigned");
        }
        if (user.getSubjects() != null){
            List<String> subjectNames = Optional.ofNullable(user.getSubjects())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(SubjectEntity::getSubjectName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            dto.setSubjectNme(subjectNames);
        }else {
            dto.setDepartmentName("Not Assigned");
        }
        return dto;
    }


    public RoleResDto mapToDto(RoleEntity role) {
        RoleResDto dto = new RoleResDto();
        dto.setId(role.getId());
        dto.setRole(role.getName()); // or role.getName() depending on your field
        return dto;
    }

    public SubjectResDto mapToDto(SubjectEntity subject) {
        SubjectResDto dto = new SubjectResDto();
        dto.setId(subject.getId());
        dto.setSubjectName(subject.getSubjectName());

        DepartmentEntity dept = subject.getDepartment();
        if (dept == null || dept.getDepartmentName() == null || dept.getDepartmentName().isEmpty()) {
            throw new RuntimeException("Department not assigned to subject ID: " + subject.getId());
        }

        dto.setDepartmentName(dept.getDepartmentName());
        return dto;
    }


    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

    public StudentResDto mapToDto(StudentEntity entity) {
        StudentResDto dto = new StudentResDto();

        if (entity == null) return dto;

        dto.setId(entity.getId());

        // Personal Info
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setAge(entity.getAge());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setGender(entity.getGender());
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setAadharNumber(entity.getAadharNumber());

        // Academic Info
        dto.setProgrammeLevel(entity.getProgrammeLevel());
        dto.setProgrammeOfStudy(entity.getProgrammeOfStudy());
        dto.setAdmission_date(entity.getAdmission_date());
        dto.setEnrollment_status(entity.getEnrollment_status());

        // Parents Info
        dto.setFatherName(entity.getFatherName());
        dto.setFatherMobile(entity.getFatherMobile());
        dto.setFatherOccupation(entity.getFatherOccupation());
        dto.setMotherName(entity.getMotherName());
        dto.setMotherMobile(entity.getMotherMobile());
        dto.setMotherOccupation(entity.getMotherOccupation());
        dto.setGuardianName(entity.getGuardianName());
        dto.setGuardian_phone(entity.getGuardian_phone());

        // Address Info
        dto.setStreet(entity.getStreet());
        dto.setTaluk(entity.getTaluk());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setPincode(entity.getPincode());
        dto.setCountry(entity.getCountry());
        dto.setBoardingPoint(entity.getBoardingPoint());
        dto.setHostelBusService(entity.getHostelBusService());

        // School Info
        dto.setSchoolName(entity.getSchoolName());

        // Images / Documents
        dto.setProfileImagePath(entity.getProfileImagePath());
        dto.setMarksheetImagePath10th(entity.getMarksheetImagePath10th());
        dto.setMarksheetImagePath12th(entity.getMarksheetImagePath12th());
        dto.setUgCertificate(entity.getUgCertificate());

        // Course
        if (entity.getCourse() != null) {
            dto.setCourseName(entity.getCourse().getCourseName());
            dto.setCourseId(entity.getCourse().getId());
        } else {
            dto.setCourseName("Not Assigned");
            dto.setCourseId(null);
        }

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

        // Department
        if (entity.getDepartment() != null) {
            dto.setDepartmentName(entity.getDepartment().getDepartmentName());
            dto.setDepartmentId(entity.getDepartment().getId());
        } else {
            dto.setDepartmentName("Not Assigned");
            dto.setDepartmentId(null);
        }

        // Enums
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : "PENDING");
        dto.setCourseStatus(entity.getCourseStatus() != null ? entity.getCourseStatus().name() : "NOT_REQUESTED");

        // User
        if (entity.getUser() != null && entity.getUser().getUsername() != null) {
            dto.setUsername(entity.getUser().getUsername());
        } else {
            dto.setUsername("N/A");
        }

        return dto;
    }


    private UsersResDto mapToUserDto(UsersEntity user) {
        UsersResDto resDto = new UsersResDto();
        resDto.setId(user.getId());
        resDto.setUsername(user.getUsername());
        resDto.setPassword(user.getPassword());

        if (user.getRole() != null) {
            resDto.setRole(user.getRole().getName());
            resDto.setRoleId(user.getRole().getId());  // ✅ Use ID, not name
        }

        return resDto;
    }


    private UsersResDto mapToDto(UsersEntity saved) {
        UsersResDto resDto = new UsersResDto();
        resDto.setId(saved.getId());
        resDto.setUsername(saved.getUsername());
        resDto.setPassword(saved.getPassword());

        if (saved.getRole() != null) {
            resDto.setRole(saved.getRole().getName());
            resDto.setRoleId(saved.getRole().getId());  // ✅ Use ID, not name
        }

        return resDto;
    }


}
