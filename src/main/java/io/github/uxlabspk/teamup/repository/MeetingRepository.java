package io.github.uxlabspk.teamup.repository;

import io.github.uxlabspk.teamup.model.Channel;
import io.github.uxlabspk.teamup.model.Meeting;
import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    
    List<Meeting> findByWorkspace(Workspace workspace);
    
    List<Meeting> findByChannel(Channel channel);
    
    List<Meeting> findByCreator(User creator);
    
    Optional<Meeting> findByMeetingCode(String meetingCode);
    
    @Query("SELECT m FROM Meeting m WHERE m.status = 'ONGOING'")
    List<Meeting> findOngoingMeetings();
    
    @Query("SELECT m FROM Meeting m WHERE m.startTime BETWEEN :start AND :end")
    List<Meeting> findMeetingsBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT m FROM Meeting m JOIN m.participants p WHERE p.id = :userId")
    List<Meeting> findMeetingsByParticipantId(Long userId);
    
    @Query("SELECT m FROM Meeting m WHERE m.workspace.id = :workspaceId AND m.status = 'SCHEDULED' AND m.startTime > :now ORDER BY m.startTime ASC")
    List<Meeting> findUpcomingMeetingsByWorkspaceId(Long workspaceId, LocalDateTime now);
}