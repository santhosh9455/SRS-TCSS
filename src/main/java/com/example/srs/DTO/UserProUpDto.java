package com.example.srs.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserProUpDto {

    private Long id;

    private String name;

    private String email;

    private Integer age;

    private String gender;

    private String phoneNumber;

}
