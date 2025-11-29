package com.codehuntspk.teamup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invitations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    private String inviteeEmail;

    @Column(unique = true, nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;

    @PrePersist
    public void generateToken() {
        if (this.token == null) {
            this.token = UUID.randomUUID().toString();
        }
        if (this.expiresAt == null) {
            this.expiresAt = LocalDateTime.now().plusDays(7);
        }
    }

    public enum InvitationStatus {
        PENDING, ACCEPTED, EXPIRED, REVOKED
    }
}

