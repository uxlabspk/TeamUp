package com.codehuntspk.teamup.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDto {
    private Long id;
    private Long channelId;
    private Long senderId;
    private String senderUsername;
    private String senderAvatar;
    private String content;
    private String attachmentUrl;
    private String attachmentType;
    private String attachmentName;
    private LocalDateTime timestamp;
    private boolean edited;
}

