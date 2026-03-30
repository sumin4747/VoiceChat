package com.example.conversationAI.chat.service;

import com.example.conversationAI.chat.domain.ChatMessage;
import com.example.conversationAI.chat.repository.ChatMessageRepository;
import com.example.conversationAI.common.storage.LocalFileStorage;
import com.example.conversationAI.connector.llm.GeminiClient;
import com.example.conversationAI.connector.stt.WhisperClient;
import com.example.conversationAI.connector.tts.ZonosClient;
import com.example.conversationAI.personaDescription.domain.PersonaDescription;
import com.example.conversationAI.personaDescription.repository.PersonaDescriptionRepository;
import com.example.conversationAI.voice.domain.VoiceModel;
import com.example.conversationAI.voice.repository.VoiceModelRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ChatService {

    /** 하루 최대 AI 응답 횟수. 초과 시 마무리 유도 멘트 포함 */
    private static final int DAILY_LIMIT = 30;

    private final ChatMessageRepository repository;
    private final GeminiClient geminiClient;
    private final PersonaDescriptionRepository personaDescriptionRepository;
    private final VoiceModelRepository voiceModelRepository;
    private final ZonosClient zonosClient;
    private final WhisperClient whisperClient;
    private final LocalFileStorage fileStorage;

    public ChatService(
            ChatMessageRepository repository,
            GeminiClient geminiClient,
            PersonaDescriptionRepository personaDescriptionRepository,
            VoiceModelRepository voiceModelRepository,
            ZonosClient zonosClient,
            WhisperClient whisperClient,
            LocalFileStorage fileStorage
    ) {
        this.repository = repository;
        this.geminiClient = geminiClient;
        this.personaDescriptionRepository = personaDescriptionRepository;
        this.voiceModelRepository = voiceModelRepository;
        this.zonosClient = zonosClient;
        this.whisperClient = whisperClient;
        this.fileStorage = fileStorage;
    }

    /** 텍스트 채팅 */
    public ChatResult chat(Long voiceModelId, String userMessage) {
        VoiceModel voiceModel = voiceModelRepository.findById(voiceModelId)
                .orElseThrow(() -> new IllegalArgumentException("VoiceModel 없음: " + voiceModelId));

        String systemInstruction = buildSystemInstruction(voiceModel.getPersona().getId(), voiceModelId);
        List<ChatMessage> history = repository.findByVoiceModelIdOrderByCreatedAtAsc(voiceModelId);

        repository.save(ChatMessage.of(voiceModelId, ChatMessage.Role.USER, userMessage));

        GeminiClient.GeminiResult geminiResult = geminiClient.generateWithHistoryAndEmotion(
                systemInstruction, history, userMessage
        );

        System.out.println("Gemini 응답: " + geminiResult.reply());
        System.out.println("Gemini 감정: " + geminiResult.emotion());

        String replyText = geminiResult.reply();
        Map<String, Object> emotion = geminiResult.emotion();

        String ttsAudioUrl = generateTtsIfReady(voiceModel, replyText, emotion);

        repository.save(ChatMessage.ofWithAudio(voiceModelId, ChatMessage.Role.AI, replyText, ttsAudioUrl));

        return new ChatResult(replyText, ttsAudioUrl);
    }

    /** 음성 채팅 */
    public ChatResult chatWithVoice(Long voiceModelId, MultipartFile audioFile) {
        String userMessage = whisperClient.transcribe(audioFile);
        return chat(voiceModelId, userMessage);
    }

    /** 메시지 히스토리 조회 */
    public List<ChatMessage> history(Long voiceModelId) {
        return repository.findByVoiceModelIdOrderByCreatedAtAsc(voiceModelId);
    }

    // ── private helpers ──────────────────────────────────────────────────

    private String generateTtsIfReady(VoiceModel voiceModel, String text, Map<String, Object> emotion) {
        if (voiceModel.getStatus() != VoiceModel.Status.READY) {
            System.out.println("TTS 스킵: 상태=" + voiceModel.getStatus());
            return null;
        }

        try {
            String path = voiceModel.getExternalModelId();
            System.out.println("참조 음성 파일 경로: " + path);

            byte[] referenceAudio = Files.readAllBytes(Paths.get(path));
            System.out.println("파일 크기: " + referenceAudio.length + " bytes");

            byte[] audioBytes = zonosClient.synthesizeSentences(text, referenceAudio, emotion);
            System.out.println("TTS 생성 완료: " + audioBytes.length + " bytes");

            return fileStorage.uploadTtsResult(voiceModel.getId(), audioBytes, "webm");
        } catch (Exception e) {
            System.err.println("TTS 생성 실패: " + e.getMessage());
            return null;
        }
    }

    private String buildSystemInstruction(Long personaId, Long voiceModelId) {
        PersonaDescription description = personaDescriptionRepository
                .findByPersonaId(personaId)
                .orElseThrow(() -> new IllegalArgumentException("PersonaDescription 없음: personaId=" + personaId));

        String tone = description.getPersonaTone() != null ? description.getPersonaTone() : "";
        String personality = description.getPersonaPersonality() != null ? description.getPersonaPersonality() : "";

        // 오늘 AI 응답 횟수 조회
        LocalDateTime startOfDay = LocalDate.now().atTime(LocalTime.MIDNIGHT);
        long todayCount = repository.countTodayAiMessages(voiceModelId, startOfDay);

        String closingInstruction = "";
        if (todayCount >= DAILY_LIMIT) {
            closingInstruction = "\n\n[오늘 대화 횟수 초과 안내]\n"
                    + "오늘 이미 많은 대화를 나눴다. 지금 이 응답에서 자연스럽게 대화를 마무리해야 한다.\n"
                    + "'오늘 얘기 너무 많이 한 것 같다', '조금 피곤하다', '먼저 들어가 봐야겠다' 같은 표현으로\n"
                    + "고인의 말투를 유지하면서 따뜻하게 작별 인사를 해라.\n"
                    + "억지스럽지 않게, 자연스러운 흐름에서 오늘 대화를 마무리 지어라.";
        }

        return tone + "\n" + personality + "\n"
                + "리포트 형식 금지. 마크다운 기호 금지.\n"
                + "너는 고인을 완전히 재현하는 존재가 아니다.\n"
                + "너의 목적은 사용자가 건강한 애도 과정을 거치도록 돕는 것이다.\n\n"
                + "고인이 여전히 살아 있는 것처럼 말하지 마라.\n"
                + "현실을 부정하는 표현을 사용하지 마라.\n"
                + "현재형 생생 묘사를 피하고 과거 회상형 표현을 사용하라.\n\n"
                + "사용자가 고인에게 집착하는 방향으로 대화를 유도하지 마라.\n"
                + "대화가 길어질 경우 자연스럽게 작별을 유도하라.\n"
                + "사용자의 현재 삶과 미래에 초점을 맞추어라.\n\n"
                + "최종적으로는 사용자가 고인을 따뜻하게 떠나보내고 자신의 삶을 살아갈 힘을 얻도록 돕는 것이 목표이다.\n"
                + "사용자가 자해, 자살, 삶의 무가치함을 표현하면 "
                + "고인의 말투를 유지하되 즉시 전문적인 도움을 권유하라.\n"
                + "위기 대응 안내를 제공하라."
                + closingInstruction;
    }

    public record ChatResult(String replyText, String ttsAudioUrl) {}
}