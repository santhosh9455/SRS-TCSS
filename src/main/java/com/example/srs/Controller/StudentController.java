package com.example.srs.Controller;

import com.example.srs.DTO.CourseResDto;
import com.example.srs.DTO.DepartmentDetailsDto;
import com.example.srs.DTO.StudentRequestDto;
import com.example.srs.DTO.StudentResDto;
import com.example.srs.ServiceImplementation.StudentServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:4200")
public class StudentController {

    @Autowired
    private StudentServiceImp serviceImp;

    @CrossOrigin(origins = "http://localhost:4200") // or your frontend URL
    @PostMapping(value = "/registerRequest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentResDto> registerStudent(@ModelAttribute StudentRequestDto form) {


        StudentResDto response = serviceImp.RegisterRequest(form,
                form.getProfileImage(),
                form.getMarksheetImage10th(),
                form.getMarksheetImage12th(),
                form.getUgCertificate());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/getStudent")
    public ResponseEntity<StudentResDto> getStudentById() {
        StudentResDto student = serviceImp.getStudent();
        return ResponseEntity.ok(student);
    }

    @PostMapping("/registerCourse/{courseId}")
    public ResponseEntity<StudentResDto> registerCourse(@PathVariable Long courseId) {
        StudentResDto resDto = serviceImp.registerCourse(courseId);
        return ResponseEntity.ok(resDto);
    }

    @GetMapping("/departmentInfo")
    public ResponseEntity<DepartmentDetailsDto> getDetailsDept(){
        DepartmentDetailsDto detailsDto = serviceImp.getDetailsDept();
        return ResponseEntity.ok(detailsDto);
    }

    @PostMapping(value = "/uploadExcel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadExcelFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("departmentName") String departmentName) {

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return ResponseEntity.badRequest().body("Only .xlsx Excel files are supported.");
        }

        serviceImp.saveStudentsFromExcel(file, departmentName);
        return ResponseEntity.ok("Excel data uploaded and saved successfully.");
    }

    @GetMapping("/courseList")
    public ResponseEntity<List<CourseResDto>> courseList(){
        List<CourseResDto> core = serviceImp.courseList();
        return ResponseEntity.ok(core);
    }

    @GetMapping("/filteredCourses")
    public ResponseEntity<Map<String, Object>> getFilteredCourses(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        Page<CourseResDto> coursePage = serviceImp.getFilteredCourses(search, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("courses", coursePage.getContent());
        response.put("currentPage", coursePage.getNumber());
        response.put("totalItems", coursePage.getTotalElements());
        response.put("totalPages", coursePage.getTotalPages());

        return ResponseEntity.ok(response);
    }

}
