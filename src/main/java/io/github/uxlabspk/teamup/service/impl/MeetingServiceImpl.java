package io.github.uxlabspk.teamup.service.impl;

import io.github.uxlabspk.teamup.model.Channel;
import io.github.uxlabspk.teamup.model.Meeting;
import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;
import io.github.uxlabspk.teamup.repository.MeetingRepository;
import io.github.uxlabspk.teamup.repository.UserRepository;
import io.github.uxlabspk.teamup.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Meeting scheduleMeeting(Meeting meeting, Workspace workspace, Channel channel, User creator) {
        meeting.setWorkspace(workspace);
        meeting.setChannel(channel);
        meeting.setCreator(creator);
        meeting.setStatus(Meeting.MeetingStatus.SCHEDULED);

        // Generate a unique meeting code if not provided
        if (meeting.getMeetingCode() == null || meeting.getMeetingCode().isEmpty()) {
            meeting.setMeetingCode(generateMeetingCode());
        }

        // Add creator to participants
        meeting.getParticipants().add(creator);

        return meetingRepository.save(meeting);
    }

    @Override
    @Transactional
    public Meeting updateMeeting(Meeting meeting) {
        return meetingRepository.save(meeting);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Meeting> getMeetingById(Long id) {
        return meetingRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Meeting> getMeetingByCode(String meetingCode) {
        return meetingRepository.findByMeetingCode(meetingCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsByWorkspace(Workspace workspace) {
        return meetingRepository.findByWorkspace(workspace);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsByChannel(Channel channel) {
        return meetingRepository.findByChannel(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsByCreator(User creator) {
        return meetingRepository.findByCreator(creator);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Meeting> getOngoingMeetings() {
        return meetingRepository.findOngoingMeetings();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsBetween(LocalDateTime start, LocalDateTime end) {
        return meetingRepository.findMeetingsBetween(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsByParticipant(Long userId) {
        return meetingRepository.findMeetingsByParticipantId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Meeting> getUpcomingMeetingsByWorkspace(Long workspaceId) {
        return meetingRepository.findUpcomingMeetingsByWorkspaceId(workspaceId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteMeeting(Long id) {
        meetingRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addParticipantToMeeting(Long meetingId, Long userId) {
        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (meetingOpt.isPresent() && userOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();
            User user = userOpt.get();

            // Check if user is a member of the workspace
            if (!meeting.getWorkspace().getUsers().contains(user)) {
                throw new IllegalArgumentException("User is not a member of the workspace");
            }

            meeting.getParticipants().add(user);
            meetingRepository.save(meeting);
        } else {
            throw new IllegalArgumentException("Meeting or User not found");
        }
    }

    @Override
    @Transactional
    public void removeParticipantFromMeeting(Long meetingId, Long userId) {
        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (meetingOpt.isPresent() && userOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();
            User user = userOpt.get();

            // Don't remove the creator
            if (meeting.getCreator().equals(user)) {
                throw new IllegalArgumentException("Cannot remove the meeting creator");
            }

            meeting.getParticipants().remove(user);
            meetingRepository.save(meeting);
        } else {
            throw new IllegalArgumentException("Meeting or User not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<User> getMeetingParticipants(Long meetingId) {
        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);

        if (meetingOpt.isPresent()) {
            return meetingOpt.get().getParticipants();
        } else {
            throw new IllegalArgumentException("Meeting not found");
        }
    }

    @Override
    @Transactional
    public Meeting startMeeting(Long meetingId) {
        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);

        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();

            if (meeting.getStatus() != Meeting.MeetingStatus.SCHEDULED) {
                throw new IllegalStateException("Meeting is not in SCHEDULED state");
            }

            meeting.setStatus(Meeting.MeetingStatus.ONGOING);
            meeting.setStartTime(LocalDateTime.now());

            return meetingRepository.save(meeting);
        } else {
            throw new IllegalArgumentException("Meeting not found");
        }
    }

    @Override
    @Transactional
    public Meeting endMeeting(Long meetingId) {
        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);

        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();

            if (meeting.getStatus() != Meeting.MeetingStatus.ONGOING) {
                throw new IllegalStateException("Meeting is not in ONGOING state");
            }

            meeting.setStatus(Meeting.MeetingStatus.COMPLETED);
            meeting.setEndTime(LocalDateTime.now());

            return meetingRepository.save(meeting);
        } else {
            throw new IllegalArgumentException("Meeting not found");
        }
    }

    @Override
    @Transactional
    public Meeting cancelMeeting(Long meetingId) {
        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);

        if (meetingOpt.isPresent()) {
            Meeting meeting = meetingOpt.get();

            if (meeting.getStatus() == Meeting.MeetingStatus.COMPLETED) {
                throw new IllegalStateException("Cannot cancel a completed meeting");
            }

            meeting.setStatus(Meeting.MeetingStatus.CANCELLED);

            return meetingRepository.save(meeting);
        } else {
            throw new IllegalArgumentException("Meeting not found");
        }
    }

    @Override
    public String generateMeetingCode() {
        // Generate a random alphanumeric code
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            codeBuilder.append(chars.charAt(random.nextInt(chars.length())));
        }

        String code = codeBuilder.toString();

        // Check if code already exists, if so, generate a new one
        if (meetingRepository.findByMeetingCode(code).isPresent()) {
            return generateMeetingCode();
        }

        return code;
    }
}
