package com.example.conversationAI.voice.service;

import com.example.conversationAI.common.storage.LocalFileStorage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VoiceTrainingService {

    private final LocalFileStorage fileStorage;
    private final VoiceTrainingStatusService statusService;

    public VoiceTrainingService(
            LocalFileStorage fileStorage,
            VoiceTrainingStatusService statusService
    ) {
        this.fileStorage = fileStorage;
        this.statusService = statusService;
    }

    /**
     * 음성 파일 업로드 & 학습 시작.
     * Tomcat 임시파일 삭제 문제를 방지하기 위해
     * @Async 호출 전에 미리 바이트로 읽어서 전달.
     */
    public void startTraining(Long voiceModelId, List<MultipartFile> files) {
        try {
            List<byte[]> fileBytesList = new ArrayList<>();
            List<String> fileNames = new ArrayList<>();

            for (MultipartFile file : files) {
                fileBytesList.add(file.getBytes());
                fileNames.add(file.getOriginalFilename());
            }

            doTrainingAsync(voiceModelId, fileBytesList, fileNames);

        } catch (IOException e) {
            statusService.markFailed(voiceModelId);
        }
    }

    @Async
    public void doTrainingAsync(Long voiceModelId, List<byte[]> fileBytesList, List<String> fileNames) {
        try {
            statusService.updateProgress(voiceModelId, 20);

            int bestIndex = 0;
            for (int i = 1; i < fileBytesList.size(); i++) {
                if (fileBytesList.get(i).length > fileBytesList.get(bestIndex).length) {
                    bestIndex = i;
                }
            }

            statusService.updateProgress(voiceModelId, 50);

            String rawFilePath = fileStorage.saveRawVoiceBytes(
                    voiceModelId,
                    fileBytesList.get(bestIndex),
                    fileNames.get(bestIndex)
            );

            statusService.updateProgress(voiceModelId, 80);
            statusService.markReady(voiceModelId, rawFilePath);

        } catch (Exception e) {
            System.err.println("startTraining 실패: " + e.getMessage());
            e.printStackTrace();
            statusService.markFailed(voiceModelId);
        }
    }
}