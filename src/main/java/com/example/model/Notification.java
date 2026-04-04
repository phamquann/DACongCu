package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tiêu đề không được bỏ trống")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Nội dung không được bỏ trống")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "notification_type")
    @Enumerated(EnumType.STRING)
    private NotificationType type = NotificationType.INFO;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        INFO("Thông tin"),
        WARNING("Cảnh báo"),
        ERROR("Lỗi"),
        SUCCESS("Thành công");

        public final String label;

        NotificationType(String label) {
            this.label = label;
        }
    }
}
