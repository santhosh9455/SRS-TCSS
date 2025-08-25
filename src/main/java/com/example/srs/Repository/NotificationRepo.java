package com.example.srs.Repository;

import com.example.srs.Model.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<NotificationEntity,Long> {

    List<NotificationEntity> findAllByOrderBySentAtAsc();

    List<NotificationEntity> findAllByOrderBySentAtDesc();

    // Optional filtering
    Page<NotificationEntity> findByStudentId(Long studentId, Pageable pageable);
    Page<NotificationEntity> findByProfileId(Long profileId, Pageable pageable);

}
