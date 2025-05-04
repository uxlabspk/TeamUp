package io.github.uxlabspk.teamup.repository;

import io.github.uxlabspk.teamup.model.Meeting;
import io.github.uxlabspk.teamup.model.MeetingRecord;
import io.github.uxlabspk.teamup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRecordRepository extends JpaRepository<MeetingRecord, Long> {
    
    List<MeetingRecord> findByMeeting(Meeting meeting);
    
    List<MeetingRecord> findByRecorder(User recorder);
    
    @Query("SELECT mr FROM MeetingRecord mr WHERE mr.meeting.workspace.id = :workspaceId")
    List<MeetingRecord> findByWorkspaceId(Long workspaceId);
    
    @Query("SELECT SUM(mr.fileSize) FROM MeetingRecord mr WHERE mr.meeting.workspace.id = :workspaceId")
    Long getTotalRecordingSizeByWorkspace(Long workspaceId);
    
    @Query("SELECT mr FROM MeetingRecord mr WHERE mr.contentType LIKE :contentTypePrefix%")
    List<MeetingRecord> findByContentTypeStartingWith(String contentTypePrefix);
}