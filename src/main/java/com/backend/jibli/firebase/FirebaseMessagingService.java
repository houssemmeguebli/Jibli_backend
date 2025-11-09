package com.backend.jibli.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@Slf4j
public class FirebaseMessagingService {

    /**
     * Send notification to single device
     */
    public String sendNotification(String fcmToken, String title, String body,
                                   Map<String, String> data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setNotification(notification)
                    .setToken(fcmToken);

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance()
                    .send(messageBuilder.build());

            log.info("✅ Notification sent successfully: {}", response);
            return response;
        } catch (Exception e) {
            log.error("❌ Error sending notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    /**
     * Send notification to multiple devices
     */
    public void sendNotificationToMultipleDevices(java.util.List<String> fcmTokens,
                                                  String title, String body,
                                                  Map<String, String> data) {
        for (String token : fcmTokens) {
            try {
                sendNotification(token, title, body, data);
            } catch (Exception e) {
                log.error("Failed to send notification to token: {}", token);
            }
        }
    }

    /**
     * Send notification to topic (all subscribers)
     */
    public String sendNotificationToTopic(String topic, String title, String body,
                                          Map<String, String> data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setNotification(notification)
                    .setTopic(topic);

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance()
                    .send(messageBuilder.build());

            log.info("✅ Topic notification sent successfully: {}", response);
            return response;
        } catch (Exception e) {
            log.error("❌ Error sending topic notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send topic notification", e);
        }
    }
}