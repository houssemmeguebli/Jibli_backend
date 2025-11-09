package com.backend.jibli.adminNotifications;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IBroadcastNotificationRepository extends JpaRepository<BroadcastNotification, Integer> {
    List<BroadcastNotification> findByIsActiveTrue();
    List<BroadcastNotification> findByType(NotificationType type);
    List<BroadcastNotification> findByScheduledAtBeforeAndIsActiveTrue(LocalDateTime now);
}