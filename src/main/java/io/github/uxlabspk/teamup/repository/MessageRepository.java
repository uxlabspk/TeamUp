package io.github.uxlabspk.teamup.repository;

import io.github.uxlabspk.teamup.model.Channel;
import io.github.uxlabspk.teamup.model.Message;
import io.github.uxlabspk.teamup.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    Page<Message> findByChannelOrderByCreatedAtDesc(Channel channel, Pageable pageable);
    
    List<Message> findByChannelAndCreatedAtAfterOrderByCreatedAtAsc(Channel channel, LocalDateTime after);
    
    List<Message> findByParentMessageOrderByCreatedAtAsc(Message parentMessage);
    
    List<Message> findBySender(User sender);
    
    @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId AND m.content LIKE %:keyword%")
    Page<Message> searchMessagesInChannel(Long channelId, String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.channel.id = :channelId AND m.createdAt > :since")
    long countNewMessagesInChannel(Long channelId, LocalDateTime since);
}