package com.example.conversationAI.consent.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "consents")
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "persona_id", nullable = false)
    private Long personaId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "consent_type",
            nullable = false,
            columnDefinition = "ENUM('terms_of_service','privacy_policy','voice_usage','ai_training')"
    )
    private ConsentType consentType;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    protected Consent() {
    }

    public static Consent create(Long userId, Long personaId, ConsentType consentType) {
        Consent c = new Consent();
        c.userId = userId;
        c.personaId = personaId;
        c.consentType = consentType;
        c.revokedAt = null;
        return c;
    }

    public void agree() {
        this.revokedAt = null;
    }

    public void revoke(LocalDateTime now) {
        this.revokedAt = now;
    }

    public boolean isActive() {
        return revokedAt == null;
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

    public ConsentType getConsentType() {
        return consentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Consent)) return false;
        Consent other = (Consent) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
