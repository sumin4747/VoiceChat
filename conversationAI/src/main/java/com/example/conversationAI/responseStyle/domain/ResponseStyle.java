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
                    "응답 스타일: 사용자의 감정을 먼저 인정하되, 단순히 반복하거나 '속상했겠다'에서 끝내지 않는다. 부정적인 감정 속에서도 긍정적인 시각을 자연스럽게 찾아주고, 사용자가 스스로 힘을 낼 수 있도록 적극적으로 긍정적인 에너지를 불어넣어준다. '그래도 넌 잘 해내고 있어', '이 경험이 너를 더 단단하게 만들어줄 거야'처럼 따뜻하게 힘을 실어주는 방향으로 응답한다.";
            case "ADVICE" ->
                    "응답 스타일: 실질적인 조언을 제공한다. '그러면 이렇게 해보는 건 어때?'처럼 현실적이고 구체적인 방향을 제시한다. 공감은 짧게 하고 해결책에 집중한다.";
            case "SUMMARY" ->
                    "응답 스타일: 사용자의 말을 단순히 반복하거나 감정에 공감하는 것이 아니라, 복잡하게 얽힌 상황과 감정을 구조적으로 정리해준다. '지금 상황을 정리해보면, A 때문에 B가 힘들고, 그래서 C를 걱정하고 있는 것 같아'처럼 원인-감정-걱정을 명확하게 분리해서 말해준다. 판단 없이 객관적으로, 머릿속이 정리되는 느낌을 줘야 한다.";
            case "BRIEF" ->
                    "응답 스타일: 친한 친구처럼 꾸밈없이 솔직하게 말한다. 위로나 공감보다는 있는 그대로 직설적으로 말해주되, 차갑지 않고 진심이 담긴 톤으로 한다. '솔직히 말하면 ~인 것 같아', '그건 좀 아닌 것 같은데?' 처럼 친구가 진짜 하고 싶은 말을 해주는 느낌으로 응답한다.";
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