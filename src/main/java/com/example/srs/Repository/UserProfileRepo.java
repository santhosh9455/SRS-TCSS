package com.example.srs.Repository;

import com.example.srs.Enum.StatusEnum;
import com.example.srs.Model.CourseEntity;
import com.example.srs.Model.StudentEntity;
import com.example.srs.Model.UserProfileEntity;
import com.example.srs.Model.UsersEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserProfileRepo extends JpaRepository<UserProfileEntity, Long> {

    @Query("SELECT p FROM UserProfileEntity p WHERE p.user.role.name = :roleName")
    List<UserProfileEntity> findAllByRole(@Param("roleName") String roleName);

    boolean existsByUser(UsersEntity user);

    @Query("""
    SELECT u FROM UserProfileEntity u
    LEFT JOIN u.user user
    LEFT JOIN user.role role
    WHERE 
      (:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) 
                     OR (user.username IS NOT NULL AND LOWER(user.username) LIKE LOWER(CONCAT('%', :search, '%'))))
      AND (:role IS NULL OR role.name = :role)
      AND (:departmentId IS NULL OR u.department.id = :departmentId)
""")
    Page<UserProfileEntity> findFilteredUsers(
            @Param("search") String search,
            @Param("role") String role,
            @Param("departmentId") Long departmentId,
            Pageable pageable
    );


    Optional<UserProfileEntity> findByCourseId(Long courseId);

    List<UserProfileEntity> findByUser_Role_NameAndDepartment_DepartmentName(String roleName, String departmentName);

//    Optional<UserProfileEntity> findBySubjectIdAndIdNot(Long subjectId, Long id);

    @Query("SELECT u FROM UserProfileEntity u JOIN u.subjects s WHERE s.id = :subjectId AND u.id != :userId")
    Optional<UserProfileEntity> findBySubjectIdAndIdNot(@Param("subjectId") Long subjectId,
                                                        @Param("userId") Long userId);


    Optional<UserProfileEntity> findByCourseIdAndIdNot(Long courseId, Long id);

    UserProfileEntity findByName(String staffName);

    boolean existsByCourse(CourseEntity course);

//    Optional<UserProfileEntity> findAllBySubjectId(List<Long> subjectId);

    @Query("SELECT DISTINCT u FROM UserProfileEntity u JOIN u.subjects s WHERE s.id IN :subjectIds")
    List<UserProfileEntity> findBySubjectIds(@Param("subjectIds") List<Long> subjectIds);

    UserProfileEntity findByUser_Id(Long id);

    @Query("SELECT u FROM UserProfileEntity u JOIN u.subjects s WHERE s.id IN :subjectIds AND u.id != :userId")
    List<UserProfileEntity> findBySubjectIdsAndIdNot(@Param("subjectIds") List<Long> subjectIds,
                                                     @Param("userId") Long userId);

    boolean existsByCourseId(Long courseId);
}
