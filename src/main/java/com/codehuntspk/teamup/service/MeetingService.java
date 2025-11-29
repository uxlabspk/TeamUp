package com.codehuntspk.teamup.service;

import com.codehuntspk.teamup.entity.Meeting;
import com.codehuntspk.teamup.entity.Channel;
import com.codehuntspk.teamup.entity.User;
import com.codehuntspk.teamup.repository.MeetingRepository;
import com.codehuntspk.teamup.repository.ChannelRepository;
import com.codehuntspk.teamup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MeetingService {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LiveKitService liveKitService;

    @Transactional
    public Meeting createMeeting(Long channelId, Long creatorId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String roomName = "room_" + UUID.randomUUID().toString();

        Meeting meeting = new Meeting();
        meeting.setChannel(channel);
        meeting.setCreatedBy(creator);
        meeting.setRoomName(roomName);
        meeting.setLivekitRoomId(roomName);
        meeting.setStatus(Meeting.MeetingStatus.ACTIVE);
        meeting.getParticipants().add(creator);

        return meetingRepository.save(meeting);
    }

    public String generateMeetingToken(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return liveKitService.generateToken(meeting.getRoomName(), user.getUsername());
    }

    @Transactional
    public void endMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        meeting.setStatus(Meeting.MeetingStatus.ENDED);
        meeting.setEndedAt(LocalDateTime.now());
        meetingRepository.save(meeting);
    }

    public Meeting getMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
    }
}

