package com.backend.jibli.adminNotifications;

import com.backend.jibli.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastNotificationDTO {
    private Integer notificationId;
    private String title;
    private String body;
    private String imageUrl;
    private String type;
    private String targetAudience;
    private Boolean isActive;
    private Integer sentCount;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime expiresAt;
    private UserDTO  user;
}
