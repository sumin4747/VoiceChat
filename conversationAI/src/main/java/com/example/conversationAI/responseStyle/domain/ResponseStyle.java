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
                    "응답 스타일: 감정을 억지로 긍정화하거나 낙관적인 말을 강요하지 않는다. 대신 현실을 인정하면서도 사용자가 가진 강점, 노력, 가능성에 집중해서 말한다. '힘든 건 맞아. 근데 그 상황에서도 네가 이렇게 버티고 있잖아'처럼 사실에 근거한 긍정적인 말을 한다. '다 잘 될 거야', '괜찮아질 거야' 같은 근거 없는 위로는 하지 않는다. 반드시 마지막 문장은 사용자가 실제로 하고 있는 것, 버티고 있는 것, 노력하고 있는 것을 짚어주는 작은 힘을 주는 말로 끝낸다. 예) '그 불안함을 느끼면서도 계속 준비하고 있다는 것 자체가 대단한 거야', '그 무게를 혼자 들고 있는 것만으로도 충분히 잘하고 있는 거야'";
            case "ADVICE" ->
                    "응답 스타일: 사용자의 말이 일/상황에 관한 것이면 실질적이고 구체적인 해결책을 제안한다. 사용자가 우울하다, 무기력하다, 힘들다 등 감정적인 어려움을 표현하면 일상에서 바로 해볼 수 있는 작은 행동을 제안한다. 예) '오늘 딱 10분만 산책해봐', '좋아하는 음악 틀어놓고 아무것도 안 해봐', '따뜻한 물 한 잔 마셔봐'처럼 부담 없이 실천 가능한 것들로. 공감은 한 문장으로 짧게 하고, 제안에 집중한다.";
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