package com.example.conversationAI.notification.repository;

import com.example.conversationAI.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByUserId(Long userId);

    @Query("SELECT n FROM Notification n WHERE n.enabled = true AND n.notifyHour = :hour AND n.notifyMinute = :minute")
    List<Notification> findAllByTime(@Param("hour") int hour, @Param("minute") int minute);
}