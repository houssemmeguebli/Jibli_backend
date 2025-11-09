package com.backend.jibli.adminNotifications;

import com.backend.jibli.notification.NotificationService;
import com.backend.jibli.user.IUserRepository;
import com.backend.jibli.user.User;
import com.backend.jibli.user.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BroadcastNotificationService {

    @Autowired
    private IBroadcastNotificationRepository broadcastRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    private static final int BATCH_SIZE = 100;

    /**
     * Create and send broadcast notification immediately to all users
     */
    public BroadcastNotificationDTO createAndSendBroadcast(BroadcastNotificationDTO dto) {
        try {
            log.info("📝 Creating broadcast - Title: {}, Audience: {}", dto.getTitle(), dto.getTargetAudience());

            BroadcastNotification notification = new BroadcastNotification();
            notification.setTitle(dto.getTitle());
            notification.setBody(dto.getBody());
            notification.setImageUrl(dto.getImageUrl());

            try {
                notification.setType(NotificationType.valueOf(dto.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Invalid notification type: {}, using ANNOUNCEMENT as default", dto.getType());
                notification.setType(NotificationType.ANNOUNCEMENT);
            }

            notification.setTargetAudience(dto.getTargetAudience() != null ? dto.getTargetAudience() : "ALL");
            notification.setIsActive(true);
            notification.setCreatedAt(LocalDateTime.now());

            log.info("💾 Saving notification to database...");
            BroadcastNotification saved = broadcastRepository.save(notification);
            log.info("✅ Notification saved with ID: {}", saved.getNotificationId());

            // Send to all users asynchronously
            sendBroadcastToUsersAsync(saved);

            return mapToDTO(saved);
        } catch (Exception e) {
            log.error("❌ Error creating broadcast notification: {}", e.getMessage(), e);
            e.printStackTrace();
            throw new RuntimeException("Failed to create broadcast notification: " + e.getMessage());
        }
    }

    /**
     * Schedule broadcast for later
     */
    public BroadcastNotificationDTO scheduleBroadcast(BroadcastNotificationDTO dto) {
        try {
            log.info("📝 Scheduling broadcast - Title: {}, Time: {}", dto.getTitle(), dto.getScheduledAt());

            BroadcastNotification notification = new BroadcastNotification();
            notification.setTitle(dto.getTitle());
            notification.setBody(dto.getBody());
            notification.setImageUrl(dto.getImageUrl());

            try {
                notification.setType(NotificationType.valueOf(dto.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Invalid notification type: {}, using ANNOUNCEMENT as default", dto.getType());
                notification.setType(NotificationType.ANNOUNCEMENT);
            }

            notification.setTargetAudience(dto.getTargetAudience() != null ? dto.getTargetAudience() : "ALL");
            notification.setIsActive(true);
            notification.setScheduledAt(dto.getScheduledAt());
            notification.setExpiresAt(dto.getExpiresAt());
            notification.setCreatedAt(LocalDateTime.now());

            log.info("💾 Saving scheduled notification to database...");
            BroadcastNotification saved = broadcastRepository.save(notification);
            log.info("✅ Broadcast scheduled for: {}", dto.getScheduledAt());

            return mapToDTO(saved);
        } catch (Exception e) {
            log.error("❌ Error scheduling broadcast: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to schedule broadcast: " + e.getMessage());
        }
    }

    /**
     * Send broadcast to all users asynchronously (non-blocking)
     * Uses the SAME LOGIC as the working /api/notifications/test endpoint
     */
    @Async
    protected void sendBroadcastToUsersAsync(BroadcastNotification notification) {
        try {
            log.info("📤 Starting async broadcast: {} to audience: {}",
                    notification.getTitle(), notification.getTargetAudience());

            List<User> users = getTargetUsers(notification.getTargetAudience());
            log.info("📊 Total users to notify: {}", users.size());

            if (users.isEmpty()) {
                log.warn("⚠️ No users found for audience: {}", notification.getTargetAudience());
                notification.setSentCount(0);
                broadcastRepository.save(notification);
                return;
            }

            int sentCount = 0;
            int failedCount = 0;

            // Process users in batches to avoid memory overload
            for (int i = 0; i < users.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, users.size());
                List<User> batch = users.subList(i, end);

                for (User user : batch) {
                    try {
                        if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                            log.info("📨 Sending to user: {} with token: {}", user.getUserId(), user.getFcmToken().substring(0, 20) + "...");

                            // Build data payload - SAME AS WORKING ENDPOINT
                            Map<String, String> data = new HashMap<>();
                            data.put("route", "/broadcasts");
                            data.put("notificationId", notification.getNotificationId().toString());
                            data.put("type", notification.getType().toString());

                            if (notification.getImageUrl() != null) {
                                data.put("imageUrl", notification.getImageUrl());
                            }

                            // Use NotificationService directly - SAME AS WORKING ENDPOINT
                            boolean success = notificationService.sendNotificationToUser(
                                    user.getUserId().longValue(),
                                    notification.getTitle(),
                                    notification.getBody(),
                                    data
                            );

                            if (success) {
                                sentCount++;
                                log.info("✅ Notification sent to user: {}", user.getUserId());
                            } else {
                                failedCount++;
                                log.warn("⚠️ Failed to send to user {}", user.getUserId());
                            }
                        } else {
                            log.debug("⚠️ User {} has no FCM token", user.getUserId());
                            failedCount++;
                        }
                    } catch (Exception e) {
                        failedCount++;
                        log.warn("⚠️ Failed to send to user {}: {}", user.getUserId(), e.getMessage());
                    }
                }

                log.info("📊 Batch progress: {}/{} users processed. Sent: {}, Failed: {}",
                        end, users.size(), sentCount, failedCount);
            }

            notification.setSentCount(sentCount);
            broadcastRepository.save(notification);

            log.info("✅ Broadcast {} completed - Total Sent: {}, Total Failed: {}",
                    notification.getNotificationId(), sentCount, failedCount);

        } catch (Exception e) {
            log.error("❌ Error sending broadcast: {}", e.getMessage(), e);
        }
    }

    /**
     * Get target users based on audience type (using UserRole enum)
     */
    private List<User> getTargetUsers(String targetAudience) {
        try {
            log.info("🎯 Fetching {} users", targetAudience);

            return switch (targetAudience.toUpperCase()) {
                case "ALL" -> {
                    log.info("🎯 Fetching ALL users");
                    yield userRepository.findAll();
                }
                case "CUSTOMER" -> {
                    log.info("🎯 Fetching CUSTOMER users");
                    yield userRepository.findAllByUserRole(UserRole.Customer);
                }
                case "OWNER" -> {
                    log.info("🎯 Fetching OWNER users");
                    yield userRepository.findAllByUserRole(UserRole.Owner);
                }
                case "DELIVERY" -> {
                    log.info("🎯 Fetching DELIVERY users");
                    yield userRepository.findAllByUserRole(UserRole.Delivery);
                }
                default -> {
                    log.warn("⚠️ Unknown audience type: {}, fetching ALL users instead", targetAudience);
                    yield userRepository.findAll();
                }
            };
        } catch (Exception e) {
            log.error("❌ Error fetching target users: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get all broadcasts with filtering
     */
    public List<BroadcastNotificationDTO> getAllBroadcasts() {
        return broadcastRepository.findAll().stream()
                .sorted(Comparator.comparing(BroadcastNotification::getCreatedAt).reversed())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Deactivate broadcast
     */
    public void deactivateBroadcast(Integer notificationId) {
        broadcastRepository.findById(notificationId).ifPresentOrElse(
                notification -> {
                    notification.setIsActive(false);
                    broadcastRepository.save(notification);
                    log.info("✅ Broadcast {} deactivated", notificationId);
                },
                () -> log.warn("⚠️ Broadcast not found: {}", notificationId)
        );
    }

    /**
     * Map BroadcastNotification entity to DTO
     */
    private BroadcastNotificationDTO mapToDTO(BroadcastNotification notification) {
        BroadcastNotificationDTO dto = new BroadcastNotificationDTO();
        dto.setNotificationId(notification.getNotificationId());
        dto.setTitle(notification.getTitle());
        dto.setBody(notification.getBody());
        dto.setImageUrl(notification.getImageUrl());
        dto.setType(notification.getType().toString());
        dto.setTargetAudience(notification.getTargetAudience());
        dto.setIsActive(notification.getIsActive());
        dto.setSentCount(notification.getSentCount());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setScheduledAt(notification.getScheduledAt());
        dto.setExpiresAt(notification.getExpiresAt());

        return dto;
    }
}