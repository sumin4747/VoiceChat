package com.example.conversationAI.connector.tts;

/**
 * TTS 클라이언트 인터페이스
 */
public interface TtsClient {

    /**
     * 텍스트를 음성으로 변환
     * @param text      변환할 텍스트
     * @param referenceAudio 참조 음성 (Qwen3에서는 사용 안 함, null 가능)
     * @param instruct  음성 톤 지시문 (예: "Gentle tone.")
     * @return wav 바이너리
     */
    byte[] synthesize(String text, byte[] referenceAudio, String instruct);
}