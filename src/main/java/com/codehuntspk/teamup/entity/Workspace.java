package com.codehuntspk.teamup.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "workspaces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"channels", "members"})
public class Workspace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"workspaces", "channels", "messages", "roles"})
    private User owner;

    private String imageUrl;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL)
    private Set<Channel> channels = new HashSet<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL)
    private Set<WorkspaceMember> members = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

