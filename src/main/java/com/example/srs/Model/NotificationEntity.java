package com.example.srs.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.OffsetDateTime;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notification")
public class NotificationEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long notificationId;

        private String message;

        private OffsetDateTime sentAt = OffsetDateTime.now();

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "student_id", nullable = true)
        @JsonIgnore
        @ToString.Exclude
        private StudentEntity student;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "profile_id", nullable = true)
        @JsonIgnore
        @ToString.Exclude
        private UserProfileEntity profile;


}
