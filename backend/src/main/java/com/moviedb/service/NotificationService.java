package com.moviedb.service;

import com.moviedb.entity.Notification;
import com.moviedb.exception.ResourceNotFoundException;
import com.moviedb.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (!n.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only read your own notifications");
        }
        n.setRead(true);
        return notificationRepository.save(n);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (!n.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own notifications");
        }
        notificationRepository.deleteById(notificationId);
    }
}
