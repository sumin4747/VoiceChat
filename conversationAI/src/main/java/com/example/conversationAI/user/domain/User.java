package com.example.conversationAI.user.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "login_id", unique = true)
    private String loginId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected User() {}

    /** 일반 회원가입 */
    public static User create(String email, String loginId, String passwordHash, String nickname) {
        User user = new User();
        user.email = email;
        user.loginId = loginId;
        user.passwordHash = passwordHash;
        user.nickname = nickname;
        user.status = UserStatus.ACTIVE;
        return user;
    }

    public void delete() {
        if (this.status == UserStatus.DELETED) return;
        this.status = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void block() {
        if (this.status == UserStatus.DELETED) return;
        this.status = UserStatus.BLOCKED;
    }

    public void changePasswordHash(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getLoginId() { return loginId; }
    public String getNickname() { return nickname; }
    public String getPasswordHash() { return passwordHash; }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}