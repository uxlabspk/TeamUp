package io.github.uxlabspk.teamup.repository;

import io.github.uxlabspk.teamup.model.Attachment;
import io.github.uxlabspk.teamup.model.Message;
import io.github.uxlabspk.teamup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    
    List<Attachment> findByMessage(Message message);
    
    List<Attachment> findByUploader(User uploader);
    
    @Query("SELECT a FROM Attachment a WHERE a.message.channel.id = :channelId")
    List<Attachment> findByChannelId(Long channelId);
    
    @Query("SELECT a FROM Attachment a WHERE a.contentType LIKE :contentTypePrefix%")
    List<Attachment> findByContentTypeStartingWith(String contentTypePrefix);
    
    @Query("SELECT SUM(a.fileSize) FROM Attachment a WHERE a.uploader.id = :userId")
    Long getTotalUploadSizeByUser(Long userId);
}