package io.github.uxlabspk.teamup.service;

import io.github.uxlabspk.teamup.model.Channel;
import io.github.uxlabspk.teamup.model.Message;
import io.github.uxlabspk.teamup.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageService {
    
    Message sendMessage(Message message, Channel channel, User sender);
    
    Message updateMessage(Message message);
    
    Optional<Message> getMessageById(Long id);
    
    Page<Message> getMessagesByChannel(Channel channel, Pageable pageable);
    
    List<Message> getNewMessagesByChannel(Channel channel, LocalDateTime after);
    
    List<Message> getRepliesByParentMessage(Message parentMessage);
    
    List<Message> getMessagesBySender(User sender);
    
    Page<Message> searchMessagesInChannel(Long channelId, String keyword, Pageable pageable);
    
    long countNewMessagesInChannel(Long channelId, LocalDateTime since);
    
    void deleteMessage(Long id);
    
    Message replyToMessage(Message reply, Message parentMessage, User sender);
    
    void addReaction(Long messageId, Long userId, String reactionType);
    
    void removeReaction(Long messageId, Long userId);
}