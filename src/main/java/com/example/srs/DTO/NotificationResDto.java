package com.example.srs.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NotificationResDto {

    private Long notificationId;

    private String message;

    private Long studentId;

    private String studentName;

    private String profileName;

    private OffsetDateTime SentAt;

    private Long profileId;
}
