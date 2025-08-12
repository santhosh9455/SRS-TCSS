package com.example.srs.Repository;

import com.example.srs.Model.SubjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SubjectRepo extends JpaRepository<SubjectEntity, Long> {
    List<SubjectEntity> findBySubjectName(String subjectNme);

    SubjectEntity findBySubjectNameAndDepartment_DepartmentName(String upperCase, String departmentName);

    List<SubjectEntity> findByDepartment_DepartmentName(String departmentName);

    Page<SubjectEntity> findBySubjectNameContainingIgnoreCase(String trim, Pageable pageable);

    List<SubjectEntity> findAllById(Long subjectId);
}
