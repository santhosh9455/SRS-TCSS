package com.example.srs.Repository;

import com.example.srs.Model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepo extends JpaRepository<RoleEntity, Long> {
    RoleEntity findByName(String role);

//    RoleEntity findByNameIgnoreCase(String roleHod);
}
