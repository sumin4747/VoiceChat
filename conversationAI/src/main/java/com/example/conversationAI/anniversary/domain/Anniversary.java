package com.example.conversationAI.anniversary.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "anniversaries")
public class Anniversary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "persona_id")
    private Long personaId;

    @Column(name = "event_name", nullable = false, length = 200)
    private String eventName;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "repeat_yearly", nullable = false)
    private boolean repeatYearly = true;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Anniversary() {
    }

    public static Anniversary create(Long userId, Long personaId, String eventName, LocalDate eventDate, boolean repeatYearly, boolean enabled) {
        Anniversary a = new Anniversary();
        a.userId = userId;
        a.personaId = personaId;
        a.eventName = eventName;
        a.eventDate = eventDate;
        a.repeatYearly = repeatYearly;
        a.enabled = enabled;
        return a;
    }

    public void update(Long personaId, String eventName, LocalDate eventDate, boolean repeatYearly, boolean enabled) {
        this.personaId = personaId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.repeatYearly = repeatYearly;
        this.enabled = enabled;
    }

    public void changeEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public String getEventName() {
        return eventName;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public boolean isRepeatYearly() {
        return repeatYearly;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Anniversary)) return false;
        Anniversary other = (Anniversary) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
