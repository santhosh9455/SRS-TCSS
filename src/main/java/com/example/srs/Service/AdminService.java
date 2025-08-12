package com.example.srs.Service;

import com.example.srs.DTO.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AdminService {
    HodResDto createHod( HodDto dto);

    UsersResDto createUsers( UsersDto dto);


    List<UsersResDto> getAllUsers();

    UsersResDto updateUsers(Long userId, UsersResDto dto);

    UsersResDto roleToStaff(Long UserId, Long StaffId);

    UsersResDto roleToHod(String username, Long hodId);

    UsersResDto authStud(String username, Long studId);

    void assignSubjectToStudents(Long departmentId, Long subjectId);

    StudentResDto updateStudent(Long id, StudentUpdateRequestDto dto, MultipartFile profileImage, MultipartFile marksheetImage);

    List<StudentResDto> getAllStudent();

    List<SubjectResDto> getAllSubjects();

    SubjectResDto updateSub(SubjectUpDto subDto);

    List<RoleResDto> getRoles();

    List<StaffResDto> getAllStaffHod();

    Map<String, String> updateUserPro(UserProUpDto dto);

    Map<String, String> deleteStudentById(Long studentId);

    Map<String, String> deleteUserPro(Long userProId);

    Map<String, String> deleteAuth(Long userAuthId);

    Page<UserProfileDTO> getFilteredUsers(String search, String role, Long departmentId, Pageable pageable);

    List<CourseResDto> getAllCourse();

    public Page<StudentResDto> getStudents(String search, Long departmentId, String status, Pageable pageable);

    Map<String, String> updateHodPartial(UserProUpdateDto dto);

    Page<UsersResDto> getFilteredUser(String search, Long roleId, Pageable pageable);

    StudentResDto RegisterRequest(StudentRequestDto dto, MultipartFile profileImage, MultipartFile marksheetImage);

    Page<SubjectResDto> getFilterAllSubjects(String search,Pageable pageable);
}
