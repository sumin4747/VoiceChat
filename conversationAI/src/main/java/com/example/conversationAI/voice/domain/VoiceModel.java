package com.example.conversationAI.voice.domain;

import com.example.conversationAI.persona.domain.Persona;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voice_models")
public class VoiceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Column(nullable = false)
    private String provider;

    @Column(name = "external_model_id")
    private String externalModelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected VoiceModel() {}

    public static VoiceModel create(Persona persona, String provider) {
        VoiceModel model = new VoiceModel();
        model.persona = persona;
        model.provider = provider;
        model.status = Status.TRAINING;
        model.progressPercent = 0;
        return model;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void updateProgress(int percent) {
        this.progressPercent = percent;
    }

    public void markReady(String externalModelId) {
        this.status = Status.READY;
        this.externalModelId = externalModelId;
        this.progressPercent = 100;
    }

    public void markFailed() {
        this.status = Status.FAILED;
    }

    public Long getId() { return id; }

    public Long getPersonaId() { return persona != null ? persona.getId() : null; }

    public Persona getPersona() { return persona; }
    public String getProvider() { return provider; }
    public String getExternalModelId() { return externalModelId; }
    public Status getStatus() { return status; }
    public int getProgressPercent() { return progressPercent; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public enum Status {
        TRAINING,
        READY,
        FAILED
    }
}