package com.example.conversationAI.common.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorage {
    String uploadTtsResult(Long voiceModelId, byte[] audioBytes, String extension);
    String uploadRecording(Long userId, String sentenceId, MultipartFile file) throws IOException;
}