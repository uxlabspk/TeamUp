package com.codehuntspk.teamup.controller;

import com.codehuntspk.teamup.dto.MessageDto;
import com.codehuntspk.teamup.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private MessageService messageService;

    @MessageMapping("/chat/{channelId}")
    public void sendMessage(@DestinationVariable Long channelId,
                           @Payload Map<String, Object> payload,
                           SimpMessageHeaderAccessor headerAccessor) {
        Long senderId = Long.parseLong(payload.get("senderId").toString());
        String content = (String) payload.get("content");
        String attachmentUrl = (String) payload.getOrDefault("attachmentUrl", null);
        String attachmentType = (String) payload.getOrDefault("attachmentType", null);
        String attachmentName = (String) payload.getOrDefault("attachmentName", null);

        messageService.sendMessage(channelId, senderId, content, attachmentUrl, attachmentType, attachmentName);
    }

    @MessageMapping("/typing/{channelId}")
    public void typing(@DestinationVariable Long channelId, @Payload Map<String, Object> payload) {
        // Handle typing indicator - will be broadcast to channel subscribers
    }
}

@RestController
@RequestMapping("/api/messages")
class MessageRestController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/channel/{channelId}")
    public List<MessageDto> getChannelMessages(@PathVariable Long channelId) {
        return messageService.getChannelMessages(channelId);
    }
}

