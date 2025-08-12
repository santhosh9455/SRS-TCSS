package com.example.srs.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UsersDto {

    @NotBlank(message = "username is Required")
    private String username;

    @NotBlank(message = "password is Required")
    private String password;

    @NotNull(message = "role id is Required")
    private Long roleId;
}
