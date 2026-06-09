package com.typingtutor.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "user_password", nullable = false)
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserPerformance> performances;

    public User() {}
    public User(Long id, String username, String password, LocalDateTime createdAt, List<UserPerformance> performances) {
        this.id = id; this.username = username; this.password = password;
        this.createdAt = createdAt; this.performances = performances;
    }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String username, password;
        public Builder username(String v) { this.username = v; return this; }
        public Builder password(String v) { this.password = v; return this; }
        public User build() { User u = new User(); u.username = username; u.password = password; return u; }
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<UserPerformance> getPerformances() { return performances; }
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}
