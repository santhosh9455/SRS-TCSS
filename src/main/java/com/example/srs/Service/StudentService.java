package com.example.srs.Service;

import com.example.srs.DTO.CourseResDto;
import com.example.srs.DTO.DepartmentDetailsDto;
import com.example.srs.DTO.StudentRequestDto;
import com.example.srs.DTO.StudentResDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudentService {

    

    StudentResDto getStudent();

    StudentResDto registerCourse(Long courseId);

    DepartmentDetailsDto getDetailsDept();

    void saveStudentsFromExcel(MultipartFile file, String departmentName);

    List<CourseResDto> courseList();

    Page<CourseResDto> getFilteredCourses(String search, int page, int size);

    StudentResDto RegisterRequest(StudentRequestDto form, MultipartFile profileImage, MultipartFile marksheetImage10th, MultipartFile marksheetImage12th, MultipartFile ugCertificate);
}
