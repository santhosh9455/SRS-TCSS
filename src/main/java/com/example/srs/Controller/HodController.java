package com.example.srs.Controller;

import com.example.srs.DTO.*;
import com.example.srs.Enum.StatusEnum;
import com.example.srs.ServiceImplementation.HodServiceImp;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hod")
@CrossOrigin(origins = "http://localhost:4200")
public class HodController {

    @Autowired
    private HodServiceImp serviceImp;

    @PostMapping("/approve/student{id}")
    public ResponseEntity<StudentResDto> Appprove(@PathVariable Long id) {
        return ResponseEntity.ok(serviceImp.approveStudent(id));
    }

    @PostMapping("/reject/student{id}")
    public ResponseEntity<StudentResDto> reject(@PathVariable Long id) {
        return ResponseEntity.ok(serviceImp.rejectStudent(id));
    }

    @GetMapping("/approved/studentList")
    public ResponseEntity<List<StudentResDto>> getApprovedlist() {
        return ResponseEntity.ok(serviceImp.approvedStudentlist());
    }

    @GetMapping("/rejected/studentList")
    public ResponseEntity<List<StudentResDto>> rejectedlist() {
        return ResponseEntity.ok(serviceImp.rejectedStudentlist());
    }

    @GetMapping("/requested/studentList")
    public ResponseEntity<List<StudentResDto>> getAll() {
        return ResponseEntity.ok(serviceImp.getAllRequestedStud());
    }

    @GetMapping("/Allrequested/studentList")
    public ResponseEntity<Map<String, Object>> getRequestedStudents(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int size) {

        StatusEnum statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = StatusEnum.valueOf(status.toUpperCase());  // Convert String -> Enum
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value: " + status);
            }
        }
        Page<StudentResDto> studentPage = serviceImp.getFilteredRequestedStudents(name, statusEnum, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("students", studentPage.getContent());
        response.put("currentPage", studentPage.getNumber());
        response.put("totalItems", studentPage.getTotalElements());
        response.put("totalPages", studentPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get/student/{id}")
    public ResponseEntity<StudentResDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceImp.getStudent(id));
    }

    @PatchMapping(value = "/UpdateStudents/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentResDto> updateStudent(
            @PathVariable Long id,
            @RequestPart("student") StudentUpdateRequestDto dto, // JSON part
            @RequestPart(required = false) MultipartFile profileImage,
            @RequestPart(required = false) MultipartFile marksheetImage10th,
            @RequestPart(required = false) MultipartFile marksheetImage12th,
            @RequestPart(required = false) MultipartFile ugCertificate) {

        StudentResDto response = serviceImp.updateStudent(
                id,
                dto,
                profileImage,
                marksheetImage10th,
                marksheetImage12th,
                ugCertificate
        );

        return ResponseEntity.ok(response);
    }



    @PostMapping("/createStaff")
    public ResponseEntity<StaffResDto> createStaff(@Valid @RequestBody StaffDto dto) {
        return ResponseEntity.ok(serviceImp.createStaff(dto));
    }

    @PostMapping("/createSubject")
    public ResponseEntity<SubjectResDto> createSub(@Valid @RequestParam String subjectName) {
        return ResponseEntity.ok(serviceImp.createSubject(subjectName));
    }

    @GetMapping("/getAllSubject")
    public ResponseEntity<List<SubjectResDto>> getAllSubject() {
        return ResponseEntity.ok(serviceImp.getAllSubject());
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

        Page<SubjectResDto> result = serviceImp.getFilterAllSubjects(search, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getSubjectByName")
    public ResponseEntity<SubjectResDto> getSubjectByName(@RequestParam String subjectName) {
        return ResponseEntity.ok(serviceImp.getSubjectByName(subjectName));
    }

    @PatchMapping("/updateSubject/{id}")
    public ResponseEntity<SubjectResDto> updateSubjectName(@PathVariable Long id,
                                                           @RequestParam String newSubjectName) {

        if (newSubjectName.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        SubjectResDto updated = serviceImp.updateSubjectName(id, newSubjectName);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getAllStaff")
    public ResponseEntity<List<StaffResDto>> getallStaff() {
        return ResponseEntity.ok(serviceImp.getAllStaff());
    }

    @PatchMapping("/staffUpdate/{id}")
    public ResponseEntity<StaffResDto> updateStaff(@PathVariable Long id, @RequestBody StaffResDto dto) {
        return ResponseEntity.ok(serviceImp.updateStaff(id, dto));
    }

    @PostMapping("/createCourse")
    public ResponseEntity<CourseResDto> createCourse(@RequestParam String courseName) {
        return ResponseEntity.ok(serviceImp.createCourse(courseName));
    }

    @PatchMapping("/updateCourse")
    public ResponseEntity<CourseResDto> updateCourse(@RequestBody CourseResDto dto){
        return ResponseEntity.ok(serviceImp.updateCourse(dto));
    }

    @GetMapping("/getAllCourse")
    public ResponseEntity<List<CourseResDto>> getAllCourse() {
        return ResponseEntity.ok(serviceImp.getAllCourse());
    }

    @GetMapping("/filteredCourse")
    public ResponseEntity<Map<String, Object>> getAllCourse(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CourseResDto> coursePage = serviceImp.filteredCourse(search, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("courses", coursePage.getContent());
        response.put("currentPage", coursePage.getNumber());
        response.put("totalItems", coursePage.getTotalElements());
        response.put("totalPages", coursePage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<StaffResDto> profile(){
        return ResponseEntity.ok(serviceImp.profile());
    }

    @DeleteMapping("/deleteCourse/{id}")
    public ResponseEntity<Map<String, String>> deleteCourse(@PathVariable Long id) {
        return ResponseEntity.ok(serviceImp.deleteCourse(id));
    }

    @DeleteMapping("/deleteStaff/{id}")
    public ResponseEntity<Map<String, String>> deleteStaff(@PathVariable Long id) {
        return ResponseEntity.ok(serviceImp.deleteStaff(id));
    }

    @DeleteMapping("/deleteSubject/{id}")
    public ResponseEntity<Map<String, String>> deleteSubject(@PathVariable Long id) {
        return ResponseEntity.ok(serviceImp.deleteSubject(id));
    }

    @DeleteMapping("deleteStudent/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(serviceImp.deleteByid(id));
    }
}
