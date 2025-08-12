package com.example.srs.DTO;

import com.example.srs.Model.UsersEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AdminResDto {

    private Long id;
    private UsersEntity user;
}
