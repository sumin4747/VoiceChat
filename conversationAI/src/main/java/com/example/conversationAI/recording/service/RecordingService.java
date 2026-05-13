package com.example.conversationAI.recording.service;

import com.example.conversationAI.recording.domain.RecordingSentence;
import com.example.conversationAI.recording.dto.response.RecordingSentenceResponse;
import com.example.conversationAI.recording.repository.RecordingSentenceRepository;
import com.example.conversationAI.voice.repository.VoiceModelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class RecordingService {

    private final RecordingSentenceRepository sentenceRepository;
    private final VoiceModelRepository voiceModelRepository;

    @Value("${storage.base-path}")
    private String basePath;

    public RecordingService(
            RecordingSentenceRepository sentenceRepository,
            VoiceModelRepository voiceModelRepository
    ) {
        this.sentenceRepository = sentenceRepository;
        this.voiceModelRepository = voiceModelRepository;
    }

    /** 녹음 문장 목록 조회 */
    public List<RecordingSentenceResponse> getSentences() {
        return sentenceRepository.findAll().stream()
                .map(RecordingSentenceResponse::from)
                .toList();
    }

    /**
     * 문장별 음성 업로드
     * - wavs/{sentenceId}.wav 저장
     * - metadata.txt 갱신 (sentence_id|text 형식)
     */
    public void uploadRecording(Long userId, String sentenceId, MultipartFile audio) {
        RecordingSentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 sentence_id: " + sentenceId));

        try {
            // wavs 디렉토리 생성
            Path wavsDir = Paths.get(basePath, "recordings", String.valueOf(userId), "wavs");
            Files.createDirectories(wavsDir);

            // wav 파일 저장
            String fileName = sentenceId + ".wav";
            Path wavPath = wavsDir.resolve(fileName);
            Files.write(wavPath, audio.getBytes());

            // metadata.txt 갱신
            updateMetadata(userId, sentenceId, sentence.getText(), wavsDir.getParent());

            System.out.println("녹음 저장 완료: " + wavPath);

        } catch (IOException e) {
            throw new RuntimeException("녹음 파일 저장 실패: " + e.getMessage(), e);
        }
    }

    private void updateMetadata(Long userId, String sentenceId, String text, Path baseDir) throws IOException {
        Path metadataPath = baseDir.resolve("metadata.txt");
        File metaFile = metadataPath.toFile();

        // 기존 내용 읽기
        StringBuilder existing = new StringBuilder();
        if (metaFile.exists()) {
            existing.append(Files.readString(metadataPath));
        }

        // 이미 있는 sentenceId면 덮어쓰기 위해 해당 줄 제거
        String updated = existing.toString().lines()
                .filter(line -> !line.startsWith("wavs/" + sentenceId + ".wav|"))
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b);

        // 새 항목 추가
        String newLine = "wavs/" + sentenceId + ".wav|" + text;
        String finalContent = updated.isEmpty() ? newLine : updated + "\n" + newLine;

        try (FileWriter writer = new FileWriter(metaFile, false)) {
            writer.write(finalContent);
        }
    }
}