package com.example.BirdApp.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;
    @Column(nullable = false, name="name")
    private String name;
    @Column(nullable = false, name="email", unique = true)
    private String email;
    @Column(nullable = false, name="oauthProvider")
    private String oauthProvider;
    @Column(nullable = false, name="oauthId")
    private String oauthId;
    @Column(name="profilePicture")
    private String profilePicture;
    @Column(name="role")
    private String role;
    @Column(nullable = false, name="createdAt", updatable = false)
    private Instant createdAt;

    public User() {
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getOauthProvider() {
        return oauthProvider;
    }
    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }
    public String getOauthId() {
        return oauthId;
    }
    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }
    public String getProfilePicture() {
        return profilePicture;
    }
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
