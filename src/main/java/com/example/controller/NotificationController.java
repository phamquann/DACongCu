package com.example.controller;

import com.example.model.Notification;
import com.example.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications() {
        List<Notification> notifications = notificationService.getUnreadNotifications();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lấy thông báo chưa đọc thành công");
        response.put("total", notifications.size());
        response.put("data", notifications);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllNotifications() {
        List<Notification> notifications = notificationService.getAllNotifications();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lấy tất cả thông báo thành công");
        response.put("total", notifications.size());
        response.put("data", notifications);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đánh dấu thông báo đã đọc thành công");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }
}
