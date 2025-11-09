package com.backend.jibli.notification;

import com.backend.jibli.user.User;
import com.backend.jibli.user.IUserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private IUserRepository userRepository;

    /**
     * Save FCM token for user
     */
    public boolean saveFCMToken(int userId, String fcmToken) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setFcmToken(fcmToken);
                userRepository.save(user);
                log.info("✅ FCM token saved for user: {}", userId);
                return true;
            }
            log.warn("⚠️ User not found: {}", userId);
            return false;
        } catch (Exception e) {
            log.error("❌ Error saving FCM token: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send notification to specific user
     */
    public boolean sendNotificationToUser(Long userId, String title, String body, Map<String, String> data) {
        try {
            Optional<User> userOpt = userRepository.findById(Math.toIntExact(userId));
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String fcmToken = user.getFcmToken();

                if (fcmToken != null && !fcmToken.isEmpty()) {
                    return sendNotification(fcmToken, title, body, data);
                } else {
                    log.warn("⚠️ No FCM token found for user: {}", userId);
                }
            } else {
                log.warn("⚠️ User not found: {}", userId);
            }
            return false;
        } catch (Exception e) {
            log.error("❌ Error sending notification: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send notification to multiple users
     */
    public void sendNotificationToMultipleUsers(Iterable<Long> userIds, String title, String body, Map<String, String> data) {
        for (Long userId : userIds) {
            sendNotificationToUser(userId, title, body, data);
        }
    }

    /**
     * Send raw notification using FCM token - FIXED VERSION
     */
    private boolean sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            log.info("📤 Sending notification to token: {}", fcmToken.substring(0, 20) + "...");

            // Build data payload with notification info
            Map<String, String> payload = new java.util.HashMap<>();
            if (data != null) {
                payload.putAll(data);
            }
            payload.put("title", title);
            payload.put("body", body);
            payload.put("click_action", "FLUTTER_NOTIFICATION_CLICK");

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .putAllData(payload)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            log.info("📨 Message created:");
            log.info("  Title: {}", title);
            log.info("  Body: {}", body);
            log.info("  Data keys: {}", payload.keySet());

            String response = FirebaseMessaging.getInstance().send(message);

            log.info("✅ Notification sent successfully");
            log.info("Message ID: {}", response);
            return true;
        } catch (Exception e) {
            log.error("❌ Error sending notification: {}", e.getMessage());
            log.error("Full error: ", e);
            return false;
        }
    }
}