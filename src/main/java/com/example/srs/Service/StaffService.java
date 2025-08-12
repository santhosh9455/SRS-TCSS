package com.example.srs.Service;

import com.example.srs.DTO.CustomPageResponse;
import com.example.srs.DTO.GetCourseRequestDto;
import com.example.srs.DTO.StaffResDto;
import com.example.srs.DTO.StudentResDto;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface StaffService {

    List<GetCourseRequestDto> getCourseRequest();

    StaffResDto getStaff();

    List<GetCourseRequestDto> getRejectedStudent();

    List<GetCourseRequestDto> getApprovedStudent();

    List<GetCourseRequestDto> getAlltudent();

    ResponseEntity<Map<String, String>> approveCoreStudent(Long studentId);

    ResponseEntity<Map<String, String>> rejectStudent(Long studentId);

    List<StudentResDto> getSubjectStudents();


    Page<GetCourseRequestDto> getCourseFilerStud(String name, String courseStatus, int page, int size);


    CustomPageResponse<StudentResDto> getFilterSubjectStudents(String name, int page, int size);
}
