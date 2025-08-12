package com.example.srs.ServiceImplementation;

import com.example.srs.DTO.DepartmentResDto;
import com.example.srs.Model.DepartmentEntity;
import com.example.srs.Repository.DepartmentRepo;
import com.example.srs.Service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImp implements DepartmentService {

    @Autowired
    private DepartmentRepo repo;

    @Override
    public DepartmentResDto createDept(String dto) {
        DepartmentEntity existing = repo.findByDepartmentName(dto);

        if (existing != null) {
            throw new RuntimeException("Department already exists with name: " + dto);
        }

        DepartmentEntity department = new DepartmentEntity();
        department.setDepartmentName(dto.toUpperCase());

        DepartmentEntity saved = repo.save(department);
        return mapToDto(saved);
    }

    @Override
    public List<DepartmentResDto> getAll() {
        List<DepartmentEntity> deptList = repo.findAll();
        if (deptList.isEmpty()) {
            return Collections.emptyList();
        }
        return deptList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentResDto getdeptById(Long id) {
        DepartmentEntity dept = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        return mapToDto(dept);
    }

    @Override
    public DepartmentResDto updateDept(Long id, String dto) {
        DepartmentEntity existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        if (dto != null && !dto.isBlank()) {
            existing.setDepartmentName(dto.toUpperCase());
        }

        DepartmentEntity updated = repo.save(existing);
        return mapToDto(updated);
    }

    @Override
    public Map<String,String> delete(Long id) {
        DepartmentEntity getDept = repo.findById(id).orElseThrow(() -> new RuntimeException("Id not found"));
        String send = getDept.getDepartmentName();
        repo.deleteById(id);
        Map<String,String> response =new  HashMap<>();
        response.put("message","Department " + send + " Deleted Successfully");
        return response;
    }

    @Override
    public Map<String, Object> getFilteredDepartments(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("departmentName").ascending());

        Page<DepartmentEntity> pagedDepartments;

        if (search == null || search.trim().isEmpty()) {
            pagedDepartments = repo.findAll(pageable);
        } else {
            pagedDepartments = repo.findByDepartmentNameContainingIgnoreCase(search, pageable);
        }

        List<DepartmentResDto> deptDtos = pagedDepartments.getContent()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("departments", deptDtos);
        response.put("currentPage", pagedDepartments.getNumber());
        response.put("totalItems", pagedDepartments.getTotalElements());
        response.put("totalPages", pagedDepartments.getTotalPages());

        return response;
    }

    public DepartmentResDto mapToDto(DepartmentEntity department) {
        DepartmentResDto resDto = new DepartmentResDto();
        resDto.setId(department.getId());
        resDto.setDepartmentName(department.getDepartmentName());
        return resDto;
    }

}
