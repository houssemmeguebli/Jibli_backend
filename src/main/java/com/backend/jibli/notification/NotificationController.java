package com.backend.jibli.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/users/save-fcm-token")
    public ResponseEntity<Map<String, Object>> saveFCMToken(@RequestBody FCMTokenRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean success = notificationService.saveFCMToken(request.getUserId(), request.getFcmToken());

            if (success) {
                response.put("success", true);
                response.put("message", "FCM token saved successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to save FCM token");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error saving FCM token: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/notifications/send")
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody NotificationRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean success = notificationService.sendNotificationToUser(
                    request.getUserId(),
                    request.getTitle(),
                    request.getBody(),
                    request.getData()
            );

            if (success) {
                response.put("success", true);
                response.put("message", "Notification sent successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to send notification");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error sending notification: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/notifications/test")
    public ResponseEntity<Map<String, Object>> sendTestNotification() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, String> data = new HashMap<>();
            data.put("route", "/orders");
            data.put("orderId", "123");

            boolean success = notificationService.sendNotificationToUser(
                    2L,
                    "🔔 Test Notification",
                    "This is a test notification from Jibli app!",
                    data
            );

            if (success) {
                response.put("success", true);
                response.put("message", "Test notification sent successfully to user 2");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to send test notification");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error sending test notification: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Real-world example: Send order confirmation notification
    @PostMapping("/notifications/order-created/{orderId}")
    public ResponseEntity<Map<String, Object>> sendOrderNotification(
            @PathVariable Long orderId,
            @RequestBody OrderNotificationRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, String> data = new HashMap<>();
            data.put("route", "/orders");
            data.put("orderId", orderId.toString());
            data.put("orderStatus", "CREATED");

            boolean success = notificationService.sendNotificationToUser(
                    request.getUserId(),
                    "✅ Order Confirmed",
                    "Your order #" + orderId + " has been confirmed for $" + request.getTotalAmount(),
                    data
            );

            if (success) {
                response.put("success", true);
                response.put("message", "Order confirmation notification sent");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to send order notification");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Real-world example: Send delivery notification
    @PostMapping("/notifications/delivery-started/{orderId}")
    public ResponseEntity<Map<String, Object>> sendDeliveryNotification(
            @PathVariable Long orderId,
            @RequestBody DeliveryNotificationRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, String> data = new HashMap<>();
            data.put("route", "/tracking");
            data.put("orderId", orderId.toString());
            data.put("deliveryId", request.getDeliveryId());
            data.put("estimatedTime", request.getEstimatedTime());

            boolean success = notificationService.sendNotificationToUser(
                    request.getUserId(),
                    "🚚 Out for Delivery",
                    "Your order #" + orderId + " is on its way! Arriving in " + request.getEstimatedTime(),
                    data
            );

            if (success) {
                response.put("success", true);
                response.put("message", "Delivery notification sent");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to send delivery notification");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Real-world example: Send promotional notification
    @PostMapping("/notifications/promo")
    public ResponseEntity<Map<String, Object>> sendPromoNotification(
            @RequestBody PromoNotificationRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, String> data = new HashMap<>();
            data.put("route", "/deals");
            data.put("promoCode", request.getPromoCode());
            data.put("discount", request.getDiscount());

            boolean success = notificationService.sendNotificationToUser(
                    request.getUserId(),
                    "🎉 Special Offer Just for You!",
                    "Get " + request.getDiscount() + "% off with code: " + request.getPromoCode(),
                    data
            );

            if (success) {
                response.put("success", true);
                response.put("message", "Promo notification sent");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to send promo notification");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

// Request classes
class OrderNotificationRequest {
    private Long userId;
    private Double totalAmount;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
}

class DeliveryNotificationRequest {
    private Long userId;
    private String deliveryId;
    private String estimatedTime;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDeliveryId() { return deliveryId; }
    public void setDeliveryId(String deliveryId) { this.deliveryId = deliveryId; }
    public String getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }
}

class PromoNotificationRequest {
    private Long userId;
    private String promoCode;
    private String discount;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public String getDiscount() { return discount; }
    public void setDiscount(String discount) { this.discount = discount; }
}