package com.codehuntspk.teamup.service;

import com.codehuntspk.teamup.dto.MessageDto;
import com.codehuntspk.teamup.entity.Channel;
import com.codehuntspk.teamup.entity.Message;
import com.codehuntspk.teamup.entity.User;
import com.codehuntspk.teamup.repository.ChannelRepository;
import com.codehuntspk.teamup.repository.MessageRepository;
import com.codehuntspk.teamup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageDto sendMessage(Long channelId, Long senderId, String content,
                                  String attachmentUrl, String attachmentType, String attachmentName) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = new Message();
        message.setChannel(channel);
        message.setSender(sender);
        message.setContent(content);
        message.setAttachmentUrl(attachmentUrl);
        message.setAttachmentType(attachmentType);
        message.setAttachmentName(attachmentName);

        message = messageRepository.save(message);

        MessageDto messageDto = convertToDto(message);

        // Broadcast to channel subscribers via WebSocket
        messagingTemplate.convertAndSend("/topic/channel/" + channelId, messageDto);

        return messageDto;
    }

    public List<MessageDto> getChannelMessages(Long channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        List<Message> messages = messageRepository.findByChannelOrderByTimestampDesc(channel);
        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public Page<MessageDto> getChannelMessages(Long channelId, Pageable pageable) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        return messageRepository.findByChannelOrderByTimestampDesc(channel, pageable)
                .map(this::convertToDto);
    }

    private MessageDto convertToDto(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setChannelId(message.getChannel().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setSenderAvatar(message.getSender().getAvatar());
        dto.setContent(message.getContent());
        dto.setAttachmentUrl(message.getAttachmentUrl());
        dto.setAttachmentType(message.getAttachmentType());
        dto.setAttachmentName(message.getAttachmentName());
        dto.setTimestamp(message.getTimestamp());
        dto.setEdited(message.isEdited());
        return dto;
    }
}

