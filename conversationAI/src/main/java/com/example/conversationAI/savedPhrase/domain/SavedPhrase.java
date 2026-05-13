package com.example.conversationAI.savedPhrase.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_phrases")
public class SavedPhrase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voice_model_id", nullable = false)
    private Long voiceModelId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected SavedPhrase() {}

    public static SavedPhrase create(Long voiceModelId, String content) {
        SavedPhrase p = new SavedPhrase();
        p.voiceModelId = voiceModelId;
        p.content = content;
        return p;
    }

    public void updateAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Long getId() { return id; }
    public Long getVoiceModelId() { return voiceModelId; }
    public String getContent() { return content; }
    public String getAudioUrl() { return audioUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}