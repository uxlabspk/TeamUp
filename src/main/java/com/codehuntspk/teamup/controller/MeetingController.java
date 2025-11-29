package com.codehuntspk.teamup.controller;

import com.codehuntspk.teamup.entity.Meeting;
import com.codehuntspk.teamup.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    @PostMapping("/channel/{channelId}")
    public ResponseEntity<Meeting> createMeeting(
            @PathVariable Long channelId,
            @RequestParam Long userId) {
        Meeting meeting = meetingService.createMeeting(channelId, userId);
        return ResponseEntity.ok(meeting);
    }

    @GetMapping("/{meetingId}/token")
    public ResponseEntity<Map<String, String>> getMeetingToken(
            @PathVariable Long meetingId,
            @RequestParam Long userId) {
        String token = meetingService.generateMeetingToken(meetingId, userId);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{meetingId}/end")
    public ResponseEntity<Void> endMeeting(@PathVariable Long meetingId) {
        meetingService.endMeeting(meetingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{meetingId}")
    public ResponseEntity<Meeting> getMeeting(@PathVariable Long meetingId) {
        Meeting meeting = meetingService.getMeeting(meetingId);
        return ResponseEntity.ok(meeting);
    }
}

