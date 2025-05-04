package io.github.uxlabspk.teamup.service.impl;

import io.github.uxlabspk.teamup.model.Channel;
import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;
import io.github.uxlabspk.teamup.repository.ChannelRepository;
import io.github.uxlabspk.teamup.repository.UserRepository;
import io.github.uxlabspk.teamup.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Channel createChannel(Channel channel, Workspace workspace) {
        if (existsByNameAndWorkspace(channel.getName(), workspace)) {
            throw new IllegalArgumentException("Channel name already exists in this workspace");
        }
        
        channel.setWorkspace(workspace);
        channel.setCreatedAt(LocalDateTime.now());
        
        return channelRepository.save(channel);
    }

    @Override
    @Transactional
    public Channel updateChannel(Channel channel) {
        return channelRepository.save(channel);
    }

    @Override
    public Optional<Channel> getChannelById(Long id) {
        return channelRepository.findById(id);
    }

    @Override
    public Optional<Channel> getChannelByNameAndWorkspace(String name, Workspace workspace) {
        return channelRepository.findByNameAndWorkspace(name, workspace);
    }

    @Override
    public List<Channel> getChannelsByWorkspace(Workspace workspace) {
        return channelRepository.findByWorkspace(workspace);
    }

    @Override
    public List<Channel> getChannelsByUserIdAndWorkspaceId(Long userId, Long workspaceId) {
        return channelRepository.findChannelsByUserIdAndWorkspaceId(userId, workspaceId);
    }

    @Override
    public Optional<Channel> getDirectChannelBetweenUsers(Long workspaceId, Long userId1, Long userId2) {
        return channelRepository.findDirectChannelBetweenUsers(workspaceId, userId1, userId2);
    }

    @Override
    public boolean existsByNameAndWorkspace(String name, Workspace workspace) {
        return channelRepository.existsByNameAndWorkspace(name, workspace);
    }

    @Override
    @Transactional
    public void deleteChannel(Long id) {
        channelRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addUserToChannel(Long channelId, Long userId) {
        Optional<Channel> channelOpt = channelRepository.findById(channelId);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (channelOpt.isPresent() && userOpt.isPresent()) {
            Channel channel = channelOpt.get();
            User user = userOpt.get();
            
            // Check if user is a member of the workspace
            if (!channel.getWorkspace().getUsers().contains(user)) {
                throw new IllegalArgumentException("User is not a member of the workspace");
            }
            
            channel.getMembers().add(user);
            channelRepository.save(channel);
        } else {
            throw new IllegalArgumentException("Channel or User not found");
        }
    }

    @Override
    @Transactional
    public void removeUserFromChannel(Long channelId, Long userId) {
        Optional<Channel> channelOpt = channelRepository.findById(channelId);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (channelOpt.isPresent() && userOpt.isPresent()) {
            Channel channel = channelOpt.get();
            User user = userOpt.get();
            
            // Don't allow removing users from direct channels
            if (channel.getType() == Channel.ChannelType.DIRECT) {
                throw new IllegalArgumentException("Cannot remove users from direct channels");
            }
            
            channel.getMembers().remove(user);
            channelRepository.save(channel);
        } else {
            throw new IllegalArgumentException("Channel or User not found");
        }
    }

    @Override
    public List<User> getChannelMembers(Long channelId) {
        Optional<Channel> channelOpt = channelRepository.findById(channelId);
        
        if (channelOpt.isPresent()) {
            return new ArrayList<>(channelOpt.get().getMembers());
        } else {
            throw new IllegalArgumentException("Channel not found");
        }
    }

    @Override
    @Transactional
    public Channel createDirectChannel(Workspace workspace, User user1, User user2) {
        // Check if direct channel already exists
        Optional<Channel> existingChannel = getDirectChannelBetweenUsers(
                workspace.getId(), user1.getId(), user2.getId());
        
        if (existingChannel.isPresent()) {
            return existingChannel.get();
        }
        
        // Create new direct channel
        Channel directChannel = new Channel();
        directChannel.setName(user1.getUsername() + "-" + user2.getUsername());
        directChannel.setType(Channel.ChannelType.DIRECT);
        directChannel.setWorkspace(workspace);
        directChannel.setCreatedAt(LocalDateTime.now());
        
        // Add both users to the channel
        directChannel.setMembers(new HashSet<>());
        directChannel.getMembers().add(user1);
        directChannel.getMembers().add(user2);
        
        return channelRepository.save(directChannel);
    }
}