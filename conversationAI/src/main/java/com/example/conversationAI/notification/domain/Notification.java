package com.example.conversationAI.notification.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_settings")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "notify_hour", nullable = false)
    private int notifyHour;

    @Column(name = "notify_minute", nullable = false)
    private int notifyMinute;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    protected Notification() {}

    public static Notification create(Long userId, int hour, int minute) {
        Notification s = new Notification();
        s.userId = userId;
        s.notifyHour = hour;
        s.notifyMinute = minute;
        s.enabled = true;
        return s;
    }

    public void update(int hour, int minute) {
        this.notifyHour = hour;
        this.notifyMinute = minute;
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public int getNotifyHour() { return notifyHour; }
    public int getNotifyMinute() { return notifyMinute; }
    public boolean isEnabled() { return enabled; }
}