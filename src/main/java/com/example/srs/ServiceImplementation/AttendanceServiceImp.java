package com.example.srs.ServiceImplementation;

import com.example.srs.DTO.AttendanceDto;
import com.example.srs.DTO.AttendanceResDto;
import com.example.srs.DTO.AttendanceUpdateDto;
import com.example.srs.Model.AttendanceEntity;
import com.example.srs.Model.StudentEntity;
import com.example.srs.Model.SubjectEntity;
import com.example.srs.Repository.AttendanceRepo;
import com.example.srs.Repository.StudentRepo;
import com.example.srs.Repository.SubjectRepo;
import com.example.srs.Service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImp implements AttendanceService {

    @Autowired
    private AttendanceRepo attendanceRepo;

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private SubjectRepo subRepo;

    @Override
    public Optional<AttendanceEntity> getAttendance(Long id) {

        Optional<AttendanceEntity> entity = attendanceRepo.findById(id);
        if (entity.isEmpty())
            throw new RuntimeException("Attendance not found");

        return entity;
    }

    @Override
    public Page<AttendanceResDto> getAttendances(Long studentId, Long subjectId, String status, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return     attendanceRepo.filterAttendances(studentId, subjectId, status, pageable)
                .map(this::mapToDto);

    }

    @Override
    public AttendanceResDto createAttendance(AttendanceDto dto) {
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setStatus(dto.getStatus().toUpperCase());

        studentRepo.findById(dto.getStudentId())
                .ifPresent(attendance::setStudent);

        if (dto.getSubjectId() != null) {
            subRepo.findById(dto.getSubjectId())
                    .ifPresent(attendance::setSubject);
        }
            AttendanceEntity saved = attendanceRepo.save(attendance);
        return mapToDto(saved);
    }

    @Override
    public AttendanceResDto updateAttendance(Long id , AttendanceUpdateDto dto) {
        Optional<AttendanceEntity> optionalAttendance = attendanceRepo.findById(id);
        if (optionalAttendance.isEmpty()) throw new RuntimeException("Attendance not found");

        AttendanceEntity attendance = optionalAttendance.get();
        if (dto.getStatus() != null){
            attendance.setStatus(dto.getStatus().toUpperCase());
        }

        if (dto.getStudentId() != null){
            StudentEntity student = studentRepo.findById(dto.getStudentId())
                    .orElseThrow(()->new RuntimeException("Student Not Found"));
            attendance.setStudent(student);
        }

        if (dto.getSubjectId() != null){
            SubjectEntity subject = subRepo.findById(dto.getSubjectId())
                    .orElseThrow(()->new RuntimeException("subject Not Found"));
            attendance.setSubject(subject);
        }

        AttendanceEntity saved = attendanceRepo.save(attendance);
        return mapToDto(saved);

    }

    @Override
    public Map<String, String> deleteAttendance(Long id) {
        AttendanceEntity entity = attendanceRepo.findById(id)
                .orElseThrow(()->new RuntimeException("Attendance record not found"));
        entity.setSubject(null);
        entity.setStudent(null);

        attendanceRepo.deleteById(id);

        Map<String,String> res = new HashMap<>();
        res.put("Message","Attendance record deleted sucssufully");
        return res;
    }

    private AttendanceResDto mapToDto(AttendanceEntity entity) {

        AttendanceResDto dto = new AttendanceResDto();

        dto.setAttendanceId(entity.getAttendanceId());
        dto.setAttendanceDate(entity.getAttendanceDate());
        dto.setStatus(entity.getStatus().toUpperCase());

        if (entity.getStudent() != null) {
            dto.setStudentId(entity.getStudent().getId());  // assuming StudentEntity has getStudentId()
            dto.setStudentName(entity.getStudent().getFirstName() + " " + entity.getStudent().getLastName());
        }

        if (entity.getSubject() != null) {
            dto.setSubjectId(entity.getSubject().getId()); // assuming SubjectEntity has getId()
            dto.setSubjectName(entity.getSubject().getSubjectName());
        }

    return dto;
    }
}
