package com.example.srs.Service;

import com.example.srs.DTO.AttendanceDto;
import com.example.srs.DTO.AttendanceResDto;
import com.example.srs.DTO.AttendanceUpdateDto;
import com.example.srs.Model.AttendanceEntity;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.Optional;

public interface AttendanceService {
    Optional<AttendanceEntity> getAttendance(Long id);

    Page<AttendanceResDto> getAttendances(Long studentId, Long subjectId, String status, int page, int size, String sortBy, String direction);

    AttendanceResDto createAttendance(@Valid AttendanceDto dto);

    AttendanceResDto updateAttendance(Long id , AttendanceUpdateDto dto);

    Map<String, String> deleteAttendance(Long id);
}
