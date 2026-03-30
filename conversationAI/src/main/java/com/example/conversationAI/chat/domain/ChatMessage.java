package com.example.conversationAI.chat.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voice_model_id", nullable = false)
    private Long voiceModelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ChatMessage() {}

    public static ChatMessage of(Long voiceModelId, Role role, String content) {
        ChatMessage msg = new ChatMessage();
        msg.voiceModelId = voiceModelId;
        msg.role = role;
        msg.content = content;
        return msg;
    }

    public static ChatMessage ofWithAudio(Long voiceModelId, Role role, String content, String audioUrl) {
        ChatMessage msg = of(voiceModelId, role, content);
        msg.audioUrl = audioUrl;
        return msg;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId()             { return id; }
    public Long getVoiceModelId()   { return voiceModelId; }
    public Role getRole()           { return role; }
    public String getContent()      { return content; }
    public String getAudioUrl()     { return audioUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public enum Role { USER, AI }
}