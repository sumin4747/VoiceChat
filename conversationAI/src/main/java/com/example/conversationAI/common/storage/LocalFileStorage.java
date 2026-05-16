package com.example.conversationAI.common.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class LocalFileStorage implements FileStorage {

    @Value("${storage.base-path}")
    private String basePath;

    @Value("${storage.base-url}")
    private String baseUrl;

    @Override
    public String uploadTtsResult(Long voiceId, byte[] audioBytes, String ext) {
        String relativePath = "voices/tts/" + voiceId + "/" + UUID.randomUUID() + "." + ext;
        saveBytesToDisk(audioBytes, relativePath);
        return baseUrl + "/" + relativePath;
    }

    @Override
    public String uploadRecording(Long userId, String sentenceId, MultipartFile file) throws IOException {
        String relativePath = "recordings/" + userId + "/wavs/" + sentenceId + ".wav";
        saveBytesToDisk(file.getBytes(), relativePath);
        return baseUrl + "/" + relativePath;
    }

    public String uploadTtsResult(Long voiceId, byte[] audioBytes) {
        return uploadTtsResult(voiceId, audioBytes, "wav");
    }

    public String saveRawVoice(Long voiceId, MultipartFile file) {
        try {
            return saveRawVoiceBytes(voiceId, file.getBytes(), file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 실패: " + e.getMessage(), e);
        }
    }

    public String saveRawVoiceBytes(Long voiceId, byte[] bytes, String originalFilename) {
        String ext = getExtension(originalFilename, "wav");
        String relativePath = "voices/raw/" + voiceId + "/" + UUID.randomUUID() + "." + ext;
        Path target = saveBytesToDisk(bytes, relativePath);
        return target.toAbsolutePath().toString();
    }

    private Path saveBytesToDisk(byte[] bytes, String relativePath) {
        try {
            Path target = Paths.get(basePath, relativePath);
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
            return target;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + relativePath, e);
        }
    }

    private String getExtension(String filename, String defaultExt) {
        if (filename == null || !filename.contains(".")) return defaultExt;
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}