package com.example.conversationAI.connector.tts;

public interface TtsClient {

    byte[] synthesize(String text, byte[] referenceAudio, String instruct);

    byte[] synthesize(String text, byte[] referenceAudio, String instruct, String modelPath);
}