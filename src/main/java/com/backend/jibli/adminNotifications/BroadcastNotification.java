package com.backend.jibli.adminNotifications;

import com.backend.jibli.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "broadcast_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    private String title;
    private String body;
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // UPDATE, PROMO, ANNOUNCEMENT, MAINTENANCE

    private String targetAudience; // ALL, CUSTOMERS, SELLERS, DRIVERS

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "sent_count")
    private Integer sentCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

}