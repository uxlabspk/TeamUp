package io.github.uxlabspk.teamup.service.impl;

import io.github.uxlabspk.teamup.model.Channel;
import io.github.uxlabspk.teamup.model.Message;
import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.repository.MessageRepository;
import io.github.uxlabspk.teamup.repository.UserRepository;
import io.github.uxlabspk.teamup.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Message sendMessage(Message message, Channel channel, User sender) {
        message.setChannel(channel);
        message.setSender(sender);
        message.setCreatedAt(LocalDateTime.now());
        
        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public Message updateMessage(Message message) {
        message.setUpdatedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    @Override
    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }

    @Override
    public Page<Message> getMessagesByChannel(Channel channel, Pageable pageable) {
        return messageRepository.findByChannelOrderByCreatedAtDesc(channel, pageable);
    }

    @Override
    public List<Message> getNewMessagesByChannel(Channel channel, LocalDateTime after) {
        return messageRepository.findByChannelAndCreatedAtAfterOrderByCreatedAtAsc(channel, after);
    }

    @Override
    public List<Message> getRepliesByParentMessage(Message parentMessage) {
        return messageRepository.findByParentMessageOrderByCreatedAtAsc(parentMessage);
    }

    @Override
    public List<Message> getMessagesBySender(User sender) {
        return messageRepository.findBySender(sender);
    }

    @Override
    public Page<Message> searchMessagesInChannel(Long channelId, String keyword, Pageable pageable) {
        return messageRepository.searchMessagesInChannel(channelId, keyword, pageable);
    }

    @Override
    public long countNewMessagesInChannel(Long channelId, LocalDateTime since) {
        return messageRepository.countNewMessagesInChannel(channelId, since);
    }

    @Override
    @Transactional
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Message replyToMessage(Message reply, Message parentMessage, User sender) {
        reply.setParentMessage(parentMessage);
        reply.setChannel(parentMessage.getChannel());
        reply.setSender(sender);
        reply.setCreatedAt(LocalDateTime.now());
        
        return messageRepository.save(reply);
    }

    @Override
    @Transactional
    public void addReaction(Long messageId, Long userId, String reactionType) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (messageOpt.isPresent() && userOpt.isPresent()) {
            Message message = messageOpt.get();
            User user = userOpt.get();
            
            // Check if user is already in reactions
            if (!message.getReactions().contains(user)) {
                message.getReactions().add(user);
                message.setReactionType(reactionType);
                messageRepository.save(message);
            }
        } else {
            throw new IllegalArgumentException("Message or User not found");
        }
    }

    @Override
    @Transactional
    public void removeReaction(Long messageId, Long userId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (messageOpt.isPresent() && userOpt.isPresent()) {
            Message message = messageOpt.get();
            User user = userOpt.get();
            
            message.getReactions().remove(user);
            
            // If no reactions left, clear the reaction type
            if (message.getReactions().isEmpty()) {
                message.setReactionType(null);
            }
            
            messageRepository.save(message);
        } else {
            throw new IllegalArgumentException("Message or User not found");
        }
    }
}