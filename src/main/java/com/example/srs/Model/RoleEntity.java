package com.example.srs.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // ADMIN, HOD, STAFF, STUDENT

    @OneToMany(mappedBy = "role")
    @JsonIgnore
    @ToString.Exclude
    private List<UsersEntity> users = new ArrayList<>();

}

