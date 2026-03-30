package com.example.conversationAI.persona.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "personas")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "persona_name", nullable = false, length = 100)
    private String personaName;

    @Column(name = "relationship", length = 50)
    private String relationship;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected Persona() {
    }

    public static Persona create(Long userId, String personaName, String birthDateStr) {
        Persona p = new Persona();
        p.userId = userId;
        p.personaName = personaName;

        if (birthDateStr != null && !birthDateStr.isBlank()) {
            p.birthDate = LocalDate.parse(birthDateStr);
        }
        return p;
    }

    public void softDelete(LocalDateTime now) {
        this.deletedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPersonaName() {
        return personaName;
    }

    public String getRelationship() {
        return relationship;
    }

    public LocalDate getBirthDate() { return birthDate; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void changePersonaName(String personaName) {
        this.personaName = personaName;
    }

    public void changeRelationship(String relationship) {
        this.relationship = relationship;
    }

    public LocalDateTime getDeletedAt() { return deletedAt; }

    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Persona)) return false;
        Persona other = (Persona) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
