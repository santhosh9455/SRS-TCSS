package com.example.srs.Controller;

import com.example.srs.DTO.*;
import com.example.srs.Enum.StatusEnum;
import com.example.srs.ServiceImplementation.AdminServiceImp;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:5173"})
public class AdminController {

    @Autowired
    private AdminServiceImp adminservice;

    @PostMapping("/createUserPro")
    public ResponseEntity<HodResDto> createHod(@Valid @RequestBody HodDto dto) {
        return ResponseEntity.ok(adminservice.createHod(dto));
    }

    @GetMapping("/users")
    public CustomPageResponse<UsersResDto> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long roleId,
            Pageable pageable // supports page, size, and sort
    ) {
        return new CustomPageResponse<>(adminservice.getFilteredUser(search, roleId, pageable));
    }


    @PostMapping("/createUsers")
    public ResponseEntity<UsersResDto> createUsers(@Valid @RequestBody UsersDto dto) {
        return ResponseEntity.ok(adminservice.createUsers(dto));
    }


    @PatchMapping("/updateHod")
    public ResponseEntity<Map<String,String>> updateHodPartial(@RequestBody UserProUpdateDto dto) {

        return ResponseEntity.ok(adminservice.updateHodPartial(dto));
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UsersResDto>> getAllUsers() {
        return ResponseEntity.ok(adminservice.getAllUsers());
    }

    @PatchMapping("/updateUsers/{userId}")
    public ResponseEntity<UsersResDto> updateUsers(@PathVariable Long userId, @RequestBody UsersResDto dto) {
        return ResponseEntity.ok(adminservice.updateUsers(userId, dto));
    }

    @PostMapping("/assignAuthUser")
    public ResponseEntity<UsersResDto> roleHod(@RequestParam String username, Long userId) {
        return ResponseEntity.ok(adminservice.roleToHod(username, userId));
    }

    @PostMapping("/assignAuthStud")
    public ResponseEntity<UsersResDto> authStud(@RequestParam String username, Long studId){
        return ResponseEntity.ok(adminservice.authStud(username,studId));
    }

    @PostMapping("/assign-subject-to-students")
    public ResponseEntity<String> assignSubjectToStudents(@RequestParam Long departmentId,Long subjectId) {
        adminservice.assignSubjectToStudents(departmentId,subjectId);
        return ResponseEntity.ok("Subject assigned to all students in " + departmentId);
    }

    @PatchMapping(value = "/UpdateStudents/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentResDto> updateStudent(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LocalDate dateOfBirth,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) List<Long> subjectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String courseStatus,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) MultipartFile profileImage,
            @RequestParam(required = false) MultipartFile marksheetImage) {

        System.out.println("Status code  ..... "+status);
        StudentUpdateRequestDto dto = new StudentUpdateRequestDto();
        dto.setName(name);
        dto.setDateOfBirth(dateOfBirth);
        dto.setGender(gender);
        dto.setEmail(email);
        dto.setPhoneNumber(phoneNumber);
        dto.setDepartmentId(departmentId);
        dto.setUsername(username);
        dto.setStatus(status);
        dto.setCourseId(courseId);
        dto.setCourseStatus(courseStatus);
        dto.setSubjectId(subjectId);

        StudentResDto response = adminservice.updateStudent(id, dto, profileImage, marksheetImage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAllCourses")
    public ResponseEntity<List<CourseResDto>> getAllCourse() {
        return ResponseEntity.ok(adminservice.getAllCourse());
    }

    @GetMapping("/gellAllStud")
    public ResponseEntity<List<StudentResDto>> getStud(){
        return ResponseEntity.ok(adminservice.getAllStudent());
    }

    @GetMapping("/getAllSubjects")
    public ResponseEntity<List<SubjectResDto>> getAllSubject(){
        return ResponseEntity.ok(adminservice.getAllSubjects());
    }

    @GetMapping("/getFilterAllSubjects")
    public ResponseEntity<Page<SubjectResDto>> getAllSubjects(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SubjectResDto> result = adminservice.getFilterAllSubjects(search, pageable);
        return ResponseEntity.ok(result);
    }


    @PatchMapping("/UpdateSubject")
    public ResponseEntity<SubjectResDto> updateSub(@RequestBody SubjectUpDto subDto){
        return ResponseEntity.ok(adminservice.updateSub(subDto));
    }
    @GetMapping("/getRoles")
    public ResponseEntity<List<RoleResDto>> getRoles(){
        return ResponseEntity.ok(adminservice.getRoles());
    }

    @GetMapping("/getAllStaffHod")
    public ResponseEntity<List<StaffResDto>> getAllStaffHod(){
        return ResponseEntity.ok(adminservice.getAllStaffHod());
    }

    @GetMapping("/api/users")
    public ResponseEntity<Page<UserProfileDTO>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserProfileDTO> dtoPage = adminservice.getFilteredUsers(search, role, departmentId, pageable);
        return ResponseEntity.ok(dtoPage);
    }


    @PatchMapping("/updateUserPro")
    public ResponseEntity<Map<String, String>> updateUserPro(@RequestBody UserProUpDto dto){
        return ResponseEntity.ok(adminservice.updateUserPro(dto));
    }
    @DeleteMapping("/deleteStudent/{studentId}")
    public ResponseEntity<Map<String, String>> deleteStud(@PathVariable Long studentId){
        return ResponseEntity.ok(adminservice.deleteStudentById(studentId));
    }
    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping("/deleteUserPro/{userProId}")
    public ResponseEntity<Map<String, String>> deleteUserPro(@PathVariable Long userProId){
        return ResponseEntity.ok(adminservice.deleteUserPro(userProId));
    }

    @DeleteMapping("/deleteUserAuths/{userAuthId}")
    public ResponseEntity<Map<String, String>> deleteUserAuth(@PathVariable Long userAuthId){
        return ResponseEntity.ok(adminservice.deleteAuth(userAuthId));
    }

    @GetMapping("/api/students")
    public CustomPageResponse<StudentResDto> getStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return new CustomPageResponse<>(adminservice.getStudents(search, departmentId, status, PageRequest.of(page, size)));
    }


    @PostMapping(value = "/createStudent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentResDto> registerStudent(@ModelAttribute StudentRequestDto form) {

        StudentRequestDto dto = new StudentRequestDto();
        dto.setName(form.getName());
        dto.setDepartmentId(form.getDepartmentId());
        dto.setEmail(form.getEmail());
        dto.setAge(form.getAge());
        dto.setGender(form.getGender());
        dto.setPhoneNumber(form.getPhoneNumber());
        dto.setCourseId(form.getCourseId());
        dto.setSubjectId(form.getSubjectId());

        StudentResDto response = adminservice.RegisterRequest(dto, form.getProfileImage(), form.getMarksheetImage());

        return ResponseEntity.ok(response);
    }

}
