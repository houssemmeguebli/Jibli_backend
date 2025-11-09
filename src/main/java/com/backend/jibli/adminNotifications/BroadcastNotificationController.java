package com.backend.jibli.adminNotifications;

import com.backend.jibli.adminNotifications.BroadcastNotificationDTO;
import com.backend.jibli.adminNotifications.BroadcastNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/broadcast")
@Slf4j
public class BroadcastNotificationController {

    @Autowired
    private BroadcastNotificationService broadcastService;

    /**
     * Send broadcast immediately to all users
     * Uses same logic as /api/notifications/test which is WORKING
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendBroadcast(@RequestBody BroadcastNotificationDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("📨 Received broadcast request - Title: {}, Type: {}, Audience: {}",
                    dto.getTitle(), dto.getType(), dto.getTargetAudience());

            // Validate input
            if (dto.getTitle() == null || dto.getTitle().isEmpty()) {
                response.put("success", false);
                response.put("message", "Title is required");
                log.warn("⚠️ Validation failed: Title is empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (dto.getBody() == null || dto.getBody().isEmpty()) {
                response.put("success", false);
                response.put("message", "Body is required");
                log.warn("⚠️ Validation failed: Body is empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Set default audience if not specified
            if (dto.getTargetAudience() == null || dto.getTargetAudience().isEmpty()) {
                dto.setTargetAudience("ALL");
            }

            // Set default type if not specified
            if (dto.getType() == null || dto.getType().isEmpty()) {
                dto.setType("ANNOUNCEMENT");
            }

            log.info("✅ Validation passed. Creating broadcast...");
            BroadcastNotificationDTO result = broadcastService.createAndSendBroadcast(dto);

            response.put("success", true);
            response.put("message", "Broadcast is being sent to " + result.getTargetAudience() + " users");
            response.put("data", result);
            response.put("status", "SENDING");

            log.info("✅ Broadcast initiated - ID: {}, Audience: {}",
                    result.getNotificationId(), result.getTargetAudience());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error sending broadcast: {}", e.getMessage(), e);
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("error_details", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Schedule broadcast for later
     */
    @PostMapping("/schedule")
    public ResponseEntity<Map<String, Object>> scheduleBroadcast(@RequestBody BroadcastNotificationDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("📅 Received schedule broadcast request - Title: {}, ScheduledAt: {}",
                    dto.getTitle(), dto.getScheduledAt());

            // Validate input
            if (dto.getTitle() == null || dto.getTitle().isEmpty()) {
                response.put("success", false);
                response.put("message", "Title is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (dto.getScheduledAt() == null) {
                response.put("success", false);
                response.put("message", "Scheduled time is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Set default audience if not specified
            if (dto.getTargetAudience() == null || dto.getTargetAudience().isEmpty()) {
                dto.setTargetAudience("ALL");
            }

            // Set default type if not specified
            if (dto.getType() == null || dto.getType().isEmpty()) {
                dto.setType("ANNOUNCEMENT");
            }

            log.info("✅ Validation passed. Scheduling broadcast...");
            BroadcastNotificationDTO result = broadcastService.scheduleBroadcast(dto);

            response.put("success", true);
            response.put("message", "Broadcast scheduled successfully");
            response.put("data", result);
            response.put("status", "SCHEDULED");

            log.info("✅ Broadcast scheduled - ID: {}, Time: {}",
                    result.getNotificationId(), result.getScheduledAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error scheduling broadcast: {}", e.getMessage(), e);
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("error_details", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all broadcasts
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllBroadcasts() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<BroadcastNotificationDTO> broadcasts = broadcastService.getAllBroadcasts();
            response.put("success", true);
            response.put("count", broadcasts.size());
            response.put("data", broadcasts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error fetching broadcasts: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Deactivate broadcast
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deactivateBroadcast(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            broadcastService.deactivateBroadcast(id);
            response.put("success", true);
            response.put("message", "Broadcast deactivated successfully");

            log.info("✅ Broadcast {} deactivated", id);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error deactivating broadcast: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}