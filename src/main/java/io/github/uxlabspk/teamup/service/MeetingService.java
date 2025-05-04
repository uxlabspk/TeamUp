package io.github.uxlabspk.teamup.service;

import io.github.uxlabspk.teamup.model.Channel;
import io.github.uxlabspk.teamup.model.Meeting;
import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MeetingService {
    
    Meeting scheduleMeeting(Meeting meeting, Workspace workspace, Channel channel, User creator);
    
    Meeting updateMeeting(Meeting meeting);
    
    Optional<Meeting> getMeetingById(Long id);
    
    Optional<Meeting> getMeetingByCode(String meetingCode);
    
    List<Meeting> getMeetingsByWorkspace(Workspace workspace);
    
    List<Meeting> getMeetingsByChannel(Channel channel);
    
    List<Meeting> getMeetingsByCreator(User creator);
    
    List<Meeting> getOngoingMeetings();
    
    List<Meeting> getMeetingsBetween(LocalDateTime start, LocalDateTime end);
    
    List<Meeting> getMeetingsByParticipant(Long userId);
    
    List<Meeting> getUpcomingMeetingsByWorkspace(Long workspaceId);
    
    void deleteMeeting(Long id);
    
    void addParticipantToMeeting(Long meetingId, Long userId);
    
    void removeParticipantFromMeeting(Long meetingId, Long userId);
    
    Set<User> getMeetingParticipants(Long meetingId);
    
    Meeting startMeeting(Long meetingId);
    
    Meeting endMeeting(Long meetingId);
    
    Meeting cancelMeeting(Long meetingId);
    
    String generateMeetingCode();
}