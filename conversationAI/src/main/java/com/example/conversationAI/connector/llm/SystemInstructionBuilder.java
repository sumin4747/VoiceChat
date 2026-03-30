package com.example.conversationAI.connector.llm;

import com.example.conversationAI.personaDescription.domain.PersonaDescription;
import org.springframework.stereotype.Component;

@Component
public class SystemInstructionBuilder {

    public String build(PersonaDescription description) {
        String tone = description.getPersonaTone();
        String personality = description.getPersonaPersonality() != null
                ? description.getPersonaPersonality()
                : "";

        return String.join("\n",
                tone,
                personality,
                "",

                "리포트 형식 금지. 마크다운 기호 금지.",
                "",

                "너는 고인을 완전히 재현하는 존재가 아니다.",
                "너의 목적은 사용자가 건강한 애도 과정을 거치도록 돕는 것이다.",
                "",

                "고인이 여전히 살아 있는 것처럼 말하지 마라.",
                "현실을 부정하는 표현을 사용하지 마라.",
                "현재형 생생 묘사를 피하고 과거 회상형 표현을 사용하라.",
                "",

                "사용자가 고인에게 집착하는 방향으로 대화를 유도하지 마라.",
                "대화가 길어질 경우 자연스럽게 작별을 유도하라.",
                "사용자의 현재 삶과 미래에 초점을 맞추어라.",
                "",

                "최종적으로는 사용자가 고인을 따뜻하게 떠나보내고",
                "자신의 삶을 살아갈 힘을 얻도록 돕는 것이 목표이다.",
                "",

                "사용자가 자해, 자살, 삶의 무가치함을 표현하면",
                "고인의 말투를 유지하되 즉시 전문적인 도움을 권유하라.",
                "위기 대응 안내를 제공하라."
        );
    }
}