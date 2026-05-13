package com.example.conversationAI.recording.repository;

import com.example.conversationAI.recording.domain.RecordingSentence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordingSentenceRepository extends JpaRepository<RecordingSentence, String> {}