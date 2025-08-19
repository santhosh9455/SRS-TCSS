package com.example.srs.Controller;

import com.example.srs.DTO.AttendanceDto;
import com.example.srs.DTO.AttendanceResDto;
import com.example.srs.DTO.AttendanceUpdateDto;
import com.example.srs.DTO.CustomPageResponse;
import com.example.srs.Model.AttendanceEntity;
import com.example.srs.ServiceImplementation.AttendanceServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceServiceImp attendance;


    @GetMapping("/getAttendance/{id}")
    private ResponseEntity<Optional<AttendanceEntity>> getAttendance(@PathVariable Long id){

        return ResponseEntity.ok(attendance.getAttendance(id));
    }

    @GetMapping("/getAttendance")
    public CustomPageResponse<AttendanceResDto> getAll(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "attendanceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return new  CustomPageResponse<>( attendance.getAttendances(studentId, subjectId, status, page, size, sortBy, direction));
    }

    @PostMapping("/createAttendance")
    private ResponseEntity<AttendanceResDto> createAttendance(@RequestBody AttendanceDto dto){
        return ResponseEntity.ok(attendance.createAttendance(dto));
    }

    @PatchMapping("/updateAttendance/{id}")
    private ResponseEntity<AttendanceResDto> update(@PathVariable Long id , @RequestBody AttendanceUpdateDto dto){
        return ResponseEntity.ok(attendance.updateAttendance(id, dto));
    }

    @DeleteMapping("/deleteAttendance/{id}")
    private ResponseEntity<Map<String,String>> deleteAttendance(@PathVariable Long id){
        return ResponseEntity.ok(attendance.deleteAttendance(id));
    }
}
