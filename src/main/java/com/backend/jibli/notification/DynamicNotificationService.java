package com.backend.jibli.notification;

import com.backend.jibli.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DynamicNotificationService {

    @Autowired
    private NotificationService notificationService;

    /**
     * Generic notification sender for any entity and event
     */
    public void sendNotification(Integer userId, String entityType, String event, Map<String, Object> data) {
        try {
            if (userId == null) {
                log.warn("⚠️ User ID is null, cannot send notification");
                return;
            }

            NotificationTemplate template = getTemplate(entityType, event);

            if (template == null) {
                log.warn("⚠️ No template found for entity: {}, event: {}", entityType, event);
                return;
            }

            String title = template.buildTitle(data);
            String body = template.buildBody(data);
            Map<String, String> payloadData = template.buildData(data);

            log.info("📤 Sending {} {} notification to user: {}", entityType, event, userId);
            notificationService.sendNotificationToUser(
                    (long) userId,
                    title,
                    body,
                    payloadData
            );
            log.info("✅ {} {} notification sent", entityType, event);

        } catch (Exception e) {
            log.error("❌ Error sending notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Get notification template based on entity and event
     */
    private NotificationTemplate getTemplate(String entityType, String event) {
        return switch (entityType.toUpperCase()) {
            case "ORDER" -> getOrderTemplate(event);
            case "REVIEW" -> getReviewTemplate(event);
            case "PRODUCT" -> getProductTemplate(event);
            case "PAYMENT" -> getPaymentTemplate(event);
            case "DELIVERY" -> getDeliveryTemplate(event);
            case "PROMO" -> getPromoTemplate(event);
            default -> null;
        };
    }

    // ============ ORDER TEMPLATES ============
    private NotificationTemplate getOrderTemplate(String event) {
        return switch (event.toUpperCase()) {
            case "CREATED" -> new NotificationTemplate(
                    data -> "✅ Order Confirmed",
                    data -> String.format("Order #%s confirmed!\nTotal: $%s",
                            data.get("orderId"), data.get("totalAmount")),
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/orders");
                        payload.put("orderId", data.get("orderId").toString());
                        payload.put("type", "ORDER");
                        return payload;
                    }
            );
            case "ACCEPTED" -> new NotificationTemplate(
                    data -> "🎉 Order Accepted",
                    data -> String.format("Order #%s accepted by seller!\nETA: 30-45 mins",
                            data.get("orderId")),
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/orders");
                        payload.put("orderId", data.get("orderId").toString());
                        payload.put("type", "ORDER");
                        return payload;
                    }
            );
            case "IN_PREPARATION" -> new NotificationTemplate(
                    data -> "👨‍🍳 Order Being Prepared",
                    data -> String.format("Order #%s is being prepared!", data.get("orderId")),
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/orders");
                        payload.put("orderId", data.get("orderId").toString());
                        payload.put("type", "ORDER");
                        return payload;
                    }
            );
            case "READY" -> new NotificationTemplate(
                    data -> "📦 Order Ready",
                    data -> String.format("Order #%s is ready for pickup!", data.get("orderId")),
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/orders");
                        payload.put("orderId", data.get("orderId").toString());
                        payload.put("type", "ORDER");
                        return payload;
                    }
            );
            case "DELIVERED" -> new NotificationTemplate(
                    data -> "✅ Delivered",
                    data -> String.format("Order #%s delivered! Thank you!", data.get("orderId")),
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/orders");
                        payload.put("orderId", data.get("orderId").toString());
                        payload.put("type", "ORDER");
                        return payload;
                    }
            );
            case "CANCELLED" -> new NotificationTemplate(
                    data -> "❌ Order Cancelled",
                    data -> String.format("Order #%s cancelled. Refund processing...", data.get("orderId")),
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/orders");
                        payload.put("orderId", data.get("orderId").toString());
                        payload.put("type", "ORDER");
                        return payload;
                    }
            );
            default -> null;
        };
    }

    // ============ REVIEW TEMPLATES ============
    private NotificationTemplate getReviewTemplate(String event) {
        return switch (event.toUpperCase()) {
            case "RECEIVED" -> new NotificationTemplate(
                    data -> "⭐ New Review",
                    data -> String.format("You received a %s-star review from %s",
                            data.get("rating"), data.get("reviewerName")),
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/reviews");
                        payload.put("reviewId", data.get("reviewId").toString());
                        payload.put("type", "REVIEW");
                        return payload;
                    }
            );
            default -> null;
        };
    }

    // ============ PRODUCT TEMPLATES ============
    private NotificationTemplate getProductTemplate(String event) {
        return switch (event.toUpperCase()) {
            case "BACK_IN_STOCK" -> new NotificationTemplate(
                    data -> "📦 Back in Stock",
                    data -> String.format("%s is back in stock!", data.get("productName")),
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/products");
                        payload.put("productId", data.get("productId").toString());
                        payload.put("type", "PRODUCT");
                        return payload;
                    }
            );
            case "PRICE_DROP" -> new NotificationTemplate(
                    data -> "💰 Price Dropped",
                    data -> String.format("%s price dropped to $%s!",
                            data.get("productName"), data.get("newPrice")),
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/products");
                        payload.put("productId", data.get("productId").toString());
                        payload.put("type", "PRODUCT");
                        return payload;
                    }
            );
            default -> null;
        };
    }

    // ============ PAYMENT TEMPLATES ============
    private NotificationTemplate getPaymentTemplate(String event) {
        return switch (event.toUpperCase()) {
            case "SUCCESS" -> new NotificationTemplate(
                    data -> "✅ Payment Successful",
                    data -> String.format("Payment of $%s confirmed!", data.get("amount")),
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/orders");
                        payload.put("orderId", data.get("orderId").toString());
                        payload.put("type", "PAYMENT");
                        return payload;
                    }
            );
            case "FAILED" -> new NotificationTemplate(
                    data -> "❌ Payment Failed",
                    data -> "Payment failed. Please try again.",
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/payment");
                        payload.put("orderId", data.get("orderId").toString());
                        payload.put("type", "PAYMENT");
                        return payload;
                    }
            );
            default -> null;
        };
    }

    // ============ DELIVERY TEMPLATES ============
    private NotificationTemplate getDeliveryTemplate(String event) {
        return switch (event.toUpperCase()) {
            case "ASSIGNED" -> new NotificationTemplate(
                    data -> "🚚 Driver Assigned",
                    data -> String.format("Your order is on the way! Driver: %s",
                            data.get("driverName")),
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/tracking");
                        payload.put("orderId", data.get("orderId").toString());
                        payload.put("type", "DELIVERY");
                        return payload;
                    }
            );
            case "NEARBY" -> new NotificationTemplate(
                    data -> "📍 Driver Nearby",
                    data -> "Your driver is just 5 minutes away!",
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/tracking");
                        payload.put("orderId", data.get("orderId").toString());
                        payload.put("type", "DELIVERY");
                        return payload;
                    }
            );
            default -> null;
        };
    }

    // ============ PROMO TEMPLATES ============
    private NotificationTemplate getPromoTemplate(String event) {
        return switch (event.toUpperCase()) {
            case "NEW_OFFER" -> new NotificationTemplate(
                    data -> "🎉 Special Offer",
                    data -> String.format("Get %s%% off with code: %s",
                            data.get("discount"), data.get("promoCode")),
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/deals");
                        payload.put("promoCode", data.get("promoCode").toString());
                        payload.put("type", "PROMO");
                        return payload;
                    }
            );
            case "FLASH_SALE" -> new NotificationTemplate(
                    data -> "⚡ Flash Sale",
                    data -> "Limited time offer! Check it out now!",
                    data -> {
                        Map<String, String> payload = new HashMap<>();
                        payload.put("route", "/deals");
                        payload.put("type", "PROMO");
                        return payload;
                    }
            );
            default -> null;
        };
    }

    // ============ NOTIFICATION TEMPLATE CLASS ============
    private static class NotificationTemplate {
        private final TitleBuilder titleBuilder;
        private final BodyBuilder bodyBuilder;
        private final DataBuilder dataBuilder;

        public NotificationTemplate(TitleBuilder titleBuilder, BodyBuilder bodyBuilder, DataBuilder dataBuilder) {
            this.titleBuilder = titleBuilder;
            this.bodyBuilder = bodyBuilder;
            this.dataBuilder = dataBuilder;
        }

        public String buildTitle(Map<String, Object> data) {
            return titleBuilder.build(data);
        }

        public String buildBody(Map<String, Object> data) {
            return bodyBuilder.build(data);
        }

        public Map<String, String> buildData(Map<String, Object> data) {
            return dataBuilder.build(data);
        }
    }

    @FunctionalInterface
    private interface TitleBuilder {
        String build(Map<String, Object> data);
    }

    @FunctionalInterface
    private interface BodyBuilder {
        String build(Map<String, Object> data);
    }

    @FunctionalInterface
    private interface DataBuilder {
        Map<String, String> build(Map<String, Object> data);
    }
}