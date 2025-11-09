package com.backend.jibli.firebase;

import com.backend.jibli.firebase.OrderNotificationService;
import com.backend.jibli.order.Order;
import com.backend.jibli.order.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
@Slf4j
public class TestNotificationController {

    @Autowired
    private OrderNotificationService orderNotificationService;

    /**
     * Test: Send order created notification
     */
    @PostMapping("/send-order-created-notification")
    public ResponseEntity<?> sendOrderCreatedNotification(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = Integer.parseInt(request.get("userId").toString());
            Long orderId = Long.parseLong(request.get("orderId").toString());
            Double totalAmount = Double.parseDouble(request.get("totalAmount").toString());

            // Create test order
            Order order = new Order();
            order.setOrderId(Math.toIntExact(orderId));
            order.setOrderStatus(OrderStatus.valueOf("PENDING"));
            order.setTotalAmount(totalAmount);

            // Send notification
            orderNotificationService.notifyOrderCreated(order, userId);

            log.info("✅ Test notification sent to user: {}", userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Test notification sent to user " + userId,
                    "orderId", orderId
            ));
        } catch (Exception e) {
            log.error("❌ Error sending test notification: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    /**
     * Test: Send order status changed notification
     */
    @PostMapping("/send-order-status-notification")
    public ResponseEntity<?> sendOrderStatusNotification(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = Integer.parseInt(request.get("userId").toString());
            Long orderId = Long.parseLong(request.get("orderId").toString());
            String newStatus = request.get("status").toString();

            // Create test order
            Order order = new Order();
            order.setOrderId(Math.toIntExact(orderId));
            order.setOrderStatus(OrderStatus.valueOf(newStatus));

            // Send notification
            orderNotificationService.notifyOrderStatusChanged(order, userId, newStatus);

            log.info("✅ Status notification sent to user: {}", userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Status notification sent"
            ));
        } catch (Exception e) {
            log.error("❌ Error sending status notification: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    /**
     * Test: Send order ready notification
     */
    @PostMapping("/send-order-ready-notification")
    public ResponseEntity<?> sendOrderReadyNotification(@RequestBody Map<String, Object> request) {
        try {
            Integer customerId = Integer.parseInt(request.get("customerId").toString());
            Long orderId = Long.parseLong(request.get("orderId").toString());

            // Create test order
            Order order = new Order();
            order.setOrderId(Math.toIntExact(orderId));
            order.setOrderStatus(OrderStatus.valueOf("READY"));

            // Send notification
            orderNotificationService.notifyOrderReady(order, customerId);

            log.info("✅ Order ready notification sent to customer: {}", customerId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Order ready notification sent"
            ));
        } catch (Exception e) {
            log.error("❌ Error sending ready notification: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }
}