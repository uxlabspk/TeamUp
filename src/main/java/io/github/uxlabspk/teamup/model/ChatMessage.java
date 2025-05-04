package io.github.uxlabspk.teamup.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private MessageType type;
    private String content;
    private String sender;
    private Long senderId;
    private Long channelId;
    private Long workspaceId;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String profilePicture;

    public enum MessageType {
        CHAT, JOIN, LEAVE, TYPING
    }
}