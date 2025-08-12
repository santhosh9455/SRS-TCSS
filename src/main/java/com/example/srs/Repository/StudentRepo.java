package com.example.srs.Repository;

import com.example.srs.Enum.CourseRequestStatusEnum;
import com.example.srs.Enum.StatusEnum;
import com.example.srs.Model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StudentRepo extends JpaRepository<StudentEntity, Long> {
    List<StudentEntity> findAllByStatus(StatusEnum approved);

    boolean existsByUser(UsersEntity user);

    StudentEntity findByUser(UsersEntity user);

    List<StudentEntity> findByCourse(CourseEntity course);

    Page<StudentEntity> findByCourse_Id(Long courseId, Pageable pageable);

    List<StudentEntity> findByCourseAndStatus(CourseEntity course, CourseRequestStatusEnum statusEnum);

    List<StudentEntity> findByCourseAndCourseStatus(CourseEntity course, CourseRequestStatusEnum statusEnum);


    List<StudentEntity> findByDepartment(DepartmentEntity department);

    List<StudentEntity> findByDepartment_DepartmentName(String departmentName);

    List<StudentEntity> findAllByStatusAndDepartment_Id(StatusEnum statusEnum, Long id);



    @Query("""
  SELECT s FROM StudentEntity s
  LEFT JOIN s.department d
  LEFT JOIN s.user u
  WHERE 
    (:search IS NULL OR LOWER(s.name) LIKE CONCAT('%', LOWER(:search), '%') 
                     OR (u.username IS NOT NULL AND LOWER(u.username) LIKE CONCAT('%', LOWER(:search), '%')))
    AND (:departmentId IS NULL OR d.id = :departmentId)
    AND (:status IS NULL OR s.status = :status)
""")
    Page<StudentEntity> findFiltered(
            @Param("search") String search,
            @Param("departmentId") Long departmentId,
            @Param("status") StatusEnum status,
            Pageable pageable
    );



    @Query("SELECT s FROM StudentEntity s WHERE " +
            "(:departmentId IS NULL OR s.department.id = :departmentId) AND " +
            "(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:status IS NULL OR s.status = :status)")
    Page<StudentEntity> findFilteredByDepartmentAndStatusAndName(
            @Param("departmentId") Long departmentId,
            @Param("name") String name,
            @Param("status") StatusEnum status,
            Pageable pageable
    );

    boolean existsByEmail(String email);

    Page<StudentEntity> findByCourseAndNameContainingIgnoreCase(CourseEntity course, String trim, Pageable pageable);

    Page<StudentEntity> findByCourse_IdAndNameContainingIgnoreCase(Long id, String trim, Pageable pageable);

    @Query("SELECT s FROM StudentEntity s WHERE s.course.id = :courseId " +
            "AND (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:status IS NULL OR s.courseStatus = :status)")
    Page<StudentEntity> findByCourseAndFilters(
            @Param("courseId") Long courseId,
            @Param("name") String name,
            @Param("status") CourseRequestStatusEnum status,
            Pageable pageable);


    @Query("""
    SELECT DISTINCT s FROM StudentEntity s
    JOIN s.subjects subj
    WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
    AND (:subjectIds IS NULL OR subj.id IN :subjectIds)
""")
    Page<StudentEntity> findBySubjectsWithNameFilter(
            @Param("subjectIds") List<Long> subjectIds,
            @Param("name") String name,
            Pageable pageable
    );



    Optional<StudentEntity> findByUser_Id(Long id);

    List<StudentEntity> findBySubjects(List<SubjectEntity> subject);
}
