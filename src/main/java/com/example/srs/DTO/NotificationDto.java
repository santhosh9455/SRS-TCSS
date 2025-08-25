package com.example.srs.DTO;

import com.example.srs.Model.StudentEntity;
import com.example.srs.Model.UserProfileEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NotificationDto {

    private String message;

    private Long studentId;

    private Long profileId;
}
