package com.codehuntspk.teamup.controller;

import com.codehuntspk.teamup.entity.Channel;
import com.codehuntspk.teamup.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @PostMapping
    public ResponseEntity<Channel> createChannel(
            @RequestBody Map<String, Object> request,
            @RequestParam Long workspaceId,
            @RequestParam Long userId) {

        String name = (String) request.get("name");
        String description = (String) request.get("description");
        String typeStr = (String) request.getOrDefault("type", "PUBLIC");
        Channel.ChannelType type = Channel.ChannelType.valueOf(typeStr);

        Channel channel = channelService.createChannel(name, description, workspaceId, type, userId);
        return ResponseEntity.ok(channel);
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<Channel>> getWorkspaceChannels(@PathVariable Long workspaceId) {
        List<Channel> channels = channelService.getWorkspaceChannels(workspaceId);
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<Channel> getChannel(@PathVariable Long channelId) {
        Channel channel = channelService.getChannel(channelId);
        return ResponseEntity.ok(channel);
    }

    @PostMapping("/{channelId}/members/{userId}")
    public ResponseEntity<Channel> addMember(@PathVariable Long channelId, @PathVariable Long userId) {
        Channel channel = channelService.addMemberToChannel(channelId, userId);
        return ResponseEntity.ok(channel);
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long channelId) {
        channelService.deleteChannel(channelId);
        return ResponseEntity.ok().build();
    }
}

