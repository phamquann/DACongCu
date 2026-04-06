package com.example.service;

import com.example.model.Notification;
import com.example.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotification(String title, String content, Notification.NotificationType type) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public void markAsRead(Long id) {
        notificationRepository.markAsRead(id);
    }

    public long getUnreadCount() {
        return getUnreadNotifications().size();
    }
}
