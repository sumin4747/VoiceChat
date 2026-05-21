package com.example.conversationAI.connector.tts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class Qwen3TtsClient implements TtsClient {

    private final WebClient webClient;
    private final String speaker;

    public Qwen3TtsClient(
            @Value("${qwen3-tts.url}") String baseUrl,
            @Value("${qwen3-tts.speaker:my_voice}") String speaker
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .build();
        this.speaker = speaker;
    }

    @Override
    public byte[] synthesize(String text, byte[] referenceAudio, String instruct) {
        return synthesize(text, referenceAudio, instruct, null);
    }

    public byte[] synthesize(String text, byte[] referenceAudio, String instruct, String modelPath) {
        // 문장 분할 (50자 단위)
        List<String> sentences = splitText(text, 50);

        if (sentences.size() == 1) {
            // 분할 불필요 - 바로 요청
            return requestTts(sentences.get(0), instruct, modelPath);
        }

        // 여러 문장 → 각각 TTS 요청 → wav 파일로 저장 → ffmpeg로 합치기
        List<Path> tempFiles = new ArrayList<>();
        Path mergedFile = null;

        try {
            for (String sentence : sentences) {
                byte[] audio = requestTts(sentence, instruct, modelPath);
                if (audio != null && audio.length > 0) {
                    Path tempFile = Files.createTempFile("tts_", ".wav");
                    Files.write(tempFile, audio);
                    tempFiles.add(tempFile);
                }
            }

            if (tempFiles.isEmpty()) return null;
            if (tempFiles.size() == 1) return Files.readAllBytes(tempFiles.get(0));

            // ffmpeg로 wav 합치기
            mergedFile = Files.createTempFile("tts_merged_", ".wav");
            Path listFile = Files.createTempFile("tts_list_", ".txt");

            // ffmpeg concat list 파일 생성
            StringBuilder listContent = new StringBuilder();
            for (Path f : tempFiles) {
                listContent.append("file '").append(f.toAbsolutePath()).append("'\n");
            }
            Files.writeString(listFile, listContent.toString());

            // ffmpeg 실행
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y", "-f", "concat", "-safe", "0",
                    "-i", listFile.toAbsolutePath().toString(),
                    "-c", "copy",
                    mergedFile.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("[TTS] ffmpeg 합치기 실패, 첫 번째 파일만 반환");
                return Files.readAllBytes(tempFiles.get(0));
            }

            return Files.readAllBytes(mergedFile);

        } catch (Exception e) {
            System.err.println("[TTS] 문장 분할 TTS 실패: " + e.getMessage());
            return null;
        } finally {
            // 임시 파일 정리
            for (Path f : tempFiles) {
                try { Files.deleteIfExists(f); } catch (Exception ignored) {}
            }
            if (mergedFile != null) {
                try { Files.deleteIfExists(mergedFile); } catch (Exception ignored) {}
            }
        }
    }

    private byte[] requestTts(String text, String instruct, String modelPath) {
        Map<String, String> body = new HashMap<>();
        body.put("text", text);
        body.put("speaker", speaker);
        body.put("instruct", instruct != null ? instruct : "Gentle tone.");
        if (modelPath != null) {
            body.put("modelPath", modelPath);
        }

        try {
            return webClient.post()
                    .uri("/tts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
        } catch (Exception e) {
            System.err.println("[TTS] 요청 실패: " + e.getMessage());
            return null;
        }
    }

    private List<String> splitText(String text, int maxLen) {
        List<String> result = new ArrayList<>();
        // 문장 부호 기준으로 먼저 분리
        String[] rawSentences = text.split("(?<=[.!?。！？,，、])");

        StringBuilder current = new StringBuilder();
        for (String s : rawSentences) {
            if (current.length() + s.length() > maxLen && current.length() > 0) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            }
            current.append(s);
        }
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        // 빈 문장 제거
        result.removeIf(String::isBlank);
        return result.isEmpty() ? List.of(text) : result;
    }
}