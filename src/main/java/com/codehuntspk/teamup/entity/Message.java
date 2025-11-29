package com.codehuntspk.teamup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String attachmentUrl;
    private String attachmentType; // image, document, etc.
    private String attachmentName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    private boolean edited = false;
    private LocalDateTime editedAt;
}

