package com.example.srs.Components;

import com.example.srs.Model.RoleEntity;
import com.example.srs.Model.UsersEntity;
import com.example.srs.Repository.RoleRepo;
import com.example.srs.Repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepo roleRepository;
    private final UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_HOD", "ROLE_STAFF", "ROLE_STUDENT");

        for (String role : roles) {
            if (roleRepository.findByName(role) == null) {
                RoleEntity roleEntity = new RoleEntity();
                roleEntity.setName(role);
                roleRepository.save(roleEntity);
            }
        }

        // ✅ Now inside the method
        if (usersRepo.findByUsername("Admin") == null) {
            UsersEntity admin = new UsersEntity();
            admin.setUsername("Admin");
            admin.setPassword(passwordEncoder.encode("Admin"));
            admin.setRole(roleRepository.findByName("ROLE_ADMIN"));

            usersRepo.save(admin);
            System.out.println("✅ Default admin user created: username=Admin, password=Admin");
        }
    }
}
