package com.example.conversationAI.personaDescription.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;

@Entity
@Table(
        name = "persona_descriptions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_persona_descriptions_persona", columnNames = "persona_id")
        }
)
public class PersonaDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "persona_id", nullable = false)
    private Long personaId;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "persona_tone", nullable = false, columnDefinition = "TEXT")
    private String personaTone;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "persona_personality", columnDefinition = "TEXT")
    private String personaPersonality;

    protected PersonaDescription() {
    }

    public static PersonaDescription create(Long personaId, String personaTone, String personaPersonality) {
        PersonaDescription d = new PersonaDescription();
        d.personaId = personaId;
        d.personaTone = personaTone;
        d.personaPersonality = personaPersonality;
        return d;
    }

    public void update(String personaTone, String personaPersonality) {
        this.personaTone = personaTone;
        this.personaPersonality = personaPersonality;
    }

    public Long getId() {
        return id;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public String getPersonaTone() {
        return personaTone;
    }

    public String getPersonaPersonality() {
        return personaPersonality;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonaDescription)) return false;
        PersonaDescription other = (PersonaDescription) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
