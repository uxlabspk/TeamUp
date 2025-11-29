package com.codehuntspk.teamup.service;

import com.codehuntspk.teamup.entity.Channel;
import com.codehuntspk.teamup.entity.Workspace;
import com.codehuntspk.teamup.entity.User;
import com.codehuntspk.teamup.repository.ChannelRepository;
import com.codehuntspk.teamup.repository.WorkspaceRepository;
import com.codehuntspk.teamup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChannelService {

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Channel createChannel(String name, String description, Long workspaceId,
                                 Channel.ChannelType type, Long creatorId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Channel channel = new Channel();
        channel.setName(name);
        channel.setDescription(description);
        channel.setWorkspace(workspace);
        channel.setType(type);
        channel.getMembers().add(creator);

        return channelRepository.save(channel);
    }

    public List<Channel> getWorkspaceChannels(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
        return channelRepository.findByWorkspace(workspace);
    }

    public Channel getChannel(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));
    }

    @Transactional
    public Channel addMemberToChannel(Long channelId, Long userId) {
        Channel channel = getChannel(channelId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        channel.getMembers().add(user);
        return channelRepository.save(channel);
    }

    @Transactional
    public void deleteChannel(Long channelId) {
        channelRepository.deleteById(channelId);
    }
}

