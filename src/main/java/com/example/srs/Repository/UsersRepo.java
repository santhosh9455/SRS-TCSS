package com.example.srs.Repository;

import com.example.srs.Model.UsersEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepo extends JpaRepository<UsersEntity, Long> {
    UsersEntity findByUsername(String username);

    boolean existsByUsername(String user);


    @Query("""
    SELECT u FROM UsersEntity u
    LEFT JOIN u.role r
    WHERE (:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:roleId IS NULL OR r.id = :roleId)
""")
    Page<UsersEntity> findFilteredUsers(@Param("search") String search, @Param("roleId") Long roleId, Pageable pageable);

}
