package com.example.conversationAI.recording.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "recording_sentences")
public class RecordingSentence {

    @Id
    @Column(name = "sentence_id", length = 10)
    private String sentenceId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    protected RecordingSentence() {}

    public String getSentenceId() { return sentenceId; }
    public String getText() { return text; }
}