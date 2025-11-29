package com.codehuntspk.teamup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(unique = true, nullable = false)
    private String roomName;

    @Column(nullable = false)
    private String livekitRoomId;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToMany
    @JoinTable(
        name = "meeting_participants",
        joinColumns = @JoinColumn(name = "meeting_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status = MeetingStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    public enum MeetingStatus {
        ACTIVE, ENDED
    }
}

