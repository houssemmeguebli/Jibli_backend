package com.backend.jibli.firebase;


import com.backend.jibli.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class OrderNotificationService {

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    @Autowired
    private FcmTokenRepository fcmTokenRepository;

    /**
     * Send notification when order is created
     */
    public void notifyOrderCreated(Order order, Integer sellerId) {
        try {
            Optional<FcmToken> fcmTokenOpt = fcmTokenRepository.findByUserId(sellerId);

            if (fcmTokenOpt.isPresent()) {
                Map<String, String> data = new HashMap<>();
                data.put("orderId", order.getOrderId().toString());
                data.put("orderStatus", String.valueOf(order.getOrderStatus()));
                data.put("type", "order_created");

                String title = "Nouvelle Commande";
                String body = "Commande #" + order.getOrderId() + " reçue";

                firebaseMessagingService.sendNotification(
                        fcmTokenOpt.get().getFcmToken(),
                        title,
                        body,
                        data
                );

                log.info("✅ Order created notification sent to seller: {}", sellerId);
            } else {
                log.warn("⚠️ No FCM token found for seller: {}", sellerId);
            }
        } catch (Exception e) {
            log.error("❌ Error sending order created notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification when order status changes
     */
    public void notifyOrderStatusChanged(Order order, Integer sellerId, String newStatus) {
        try {
            Optional<FcmToken> fcmTokenOpt = fcmTokenRepository.findByUserId(sellerId);

            if (fcmTokenOpt.isPresent()) {
                Map<String, String> data = new HashMap<>();
                data.put("orderId", order.getOrderId().toString());
                data.put("orderStatus", newStatus);
                data.put("type", "order_status_changed");

                String title = "Mise à Jour de Commande";
                String body = getStatusMessage(newStatus);

                firebaseMessagingService.sendNotification(
                        fcmTokenOpt.get().getFcmToken(),
                        title,
                        body,
                        data
                );

                log.info("✅ Order status notification sent to seller: {}", sellerId);
            } else {
                log.warn("⚠️ No FCM token found for seller: {}", sellerId);
            }
        } catch (Exception e) {
            log.error("❌ Error sending order status notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification to customer when order is ready
     */
    public void notifyOrderReady(Order order, Integer customerId) {
        try {
            Optional<FcmToken> fcmTokenOpt = fcmTokenRepository.findByUserId(customerId);

            if (fcmTokenOpt.isPresent()) {
                Map<String, String> data = new HashMap<>();
                data.put("orderId", order.getOrderId().toString());
                data.put("type", "order_ready");

                firebaseMessagingService.sendNotification(
                        fcmTokenOpt.get().getFcmToken(),
                        "Commande Prête",
                        "Votre commande #" + order.getOrderId() + " est prête",
                        data
                );

                log.info("✅ Order ready notification sent to customer: {}", customerId);
            }
        } catch (Exception e) {
            log.error("❌ Error sending order ready notification: {}", e.getMessage(), e);
        }
    }

    private String getStatusMessage(String status) {
        return switch (status) {
            case "PENDING" -> "Commande reçue";
            case "IN_PREPARATION" -> "Commande en préparation";
            case "WAITING" -> "En attente du livreur";
            case "ACCEPTED" -> "Commande acceptée";
            case "PICKED_UP" -> "Commande en route";
            case "DELIVERED" -> "Commande livrée";
            case "REJECTED" -> "Commande refusée";
            case "CANCELED" -> "Commande annulée";
            default -> "Commande mise à jour";
        };
    }
}