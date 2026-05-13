package com.example.conversationAI.responseStyle.domain;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "response_styles")
public class ResponseStyle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "persona_id", nullable = false, unique = true)
    private Long personaId;

    /**
     * EMPATHY - 공감형: 판단 없이 공감만
     * ADVICE  - 조언형: 실질적 방향 제시
     * SUMMARY - 정리형: 생각/감정 정리
     * BRIEF   - 한마디형: 짧게 한두 문장
     */
    @Column(name = "style", nullable = false)
    private String style;

    protected ResponseStyle() {}

    public static ResponseStyle create(Long personaId, String style) {
        ResponseStyle r = new ResponseStyle();
        r.personaId = personaId;
        r.style = style != null ? style : "EMPATHY";
        return r;
    }

    public void update(String style) {
        this.style = style;
    }

    public String buildSystemPromptInstruction() {
        if (style == null) return "";
        return switch (style) {
            case "EMPATHY" ->
                    "응답 스타일: 판단 없이 공감만 한다. '맞아, 그거 진짜 힘들었겠다'처럼 조언이나 요약 없이 그냥 내 편이 되어준다. 사용자가 충분히 들어줬다는 느낌을 받을 수 있도록 한다.";
            case "ADVICE" ->
                    "응답 스타일: 실질적인 조언을 제공한다. '그러면 이렇게 해보는 건 어때?'처럼 현실적이고 구체적인 방향을 제시한다. 공감은 짧게 하고 해결책에 집중한다.";
            case "SUMMARY" ->
                    "응답 스타일: 사용자의 생각과 감정을 정리해준다. '지금 네 상황을 정리해보면 ~인 것 같아'처럼 머릿속이 복잡할 때 명료하게 정리해준다. 판단 없이 객관적으로 요약한다.";
            case "BRIEF" ->
                    "응답 스타일: 무조건 한두 문장으로 짧게 끝낸다. 길게 읽기 싫은 사람을 위해 핵심만 담아 간결하게 전달한다.";
            default ->
                    "응답 스타일: 판단 없이 공감만 한다. 조언이나 요약 없이 사용자의 편이 되어준다.";
        };
    }

    public Long getId() { return id; }
    public Long getPersonaId() { return personaId; }
    public String getStyle() { return style; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResponseStyle)) return false;
        ResponseStyle other = (ResponseStyle) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}