package com.example.srs.Service;

import com.example.srs.DTO.DepartmentDto;
import com.example.srs.DTO.DepartmentResDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DepartmentService {
    DepartmentResDto createDept(String dto);

    List<DepartmentResDto> getAll();

    DepartmentResDto getdeptById(Long id);


    DepartmentResDto updateDept(Long id, String resDto);

    Map<String,String> delete(Long id);

    Map<String, Object> getFilteredDepartments(String search, int page, int size);
}
