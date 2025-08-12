package com.example.srs.Controller;

import com.example.srs.DTO.DepartmentResDto;
import com.example.srs.ServiceImplementation.DepartmentServiceImp;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dept")
@CrossOrigin(origins = "http://localhost:4200")
public class DepartmentController {

    @Autowired
    private DepartmentServiceImp serviceImp;

    @PostMapping("/createDept")
    public ResponseEntity<DepartmentResDto> createDept(@Valid @RequestParam String departmentName) {
        return ResponseEntity.ok(serviceImp.createDept(departmentName));
    }

    @GetMapping("/AllDept")
    public ResponseEntity<List<DepartmentResDto>> getAllDept() {
        return ResponseEntity.ok(serviceImp.getAll());
    }

    @GetMapping("/AllFilterDept")
    public ResponseEntity<Map<String, Object>> getAllDepartments(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(serviceImp.getFilteredDepartments(search, page, size));
    }


    @GetMapping("/getDept/{id}")
    public ResponseEntity<DepartmentResDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceImp.getdeptById(id));
    }

    @PatchMapping("/updateDept/{id}")
    public ResponseEntity<DepartmentResDto> updeteDept(@PathVariable Long id, @RequestParam String newDepartmentName) {
        return ResponseEntity.ok(serviceImp.updateDept(id, newDepartmentName));
    }

    @DeleteMapping("/deleteDept/{id}")
    public ResponseEntity<Map<String,String>> deleteDept(@PathVariable Long id) {
        return ResponseEntity.ok(serviceImp.delete(id));
    }
}
