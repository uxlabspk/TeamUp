package io.github.uxlabspk.teamup.service;

import io.github.uxlabspk.teamup.model.Channel;
import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;

import java.util.List;
import java.util.Optional;

public interface ChannelService {
    
    Channel createChannel(Channel channel, Workspace workspace);
    
    Channel updateChannel(Channel channel);
    
    Optional<Channel> getChannelById(Long id);
    
    Optional<Channel> getChannelByNameAndWorkspace(String name, Workspace workspace);
    
    List<Channel> getChannelsByWorkspace(Workspace workspace);
    
    List<Channel> getChannelsByUserIdAndWorkspaceId(Long userId, Long workspaceId);
    
    Optional<Channel> getDirectChannelBetweenUsers(Long workspaceId, Long userId1, Long userId2);
    
    boolean existsByNameAndWorkspace(String name, Workspace workspace);
    
    void deleteChannel(Long id);
    
    void addUserToChannel(Long channelId, Long userId);
    
    void removeUserFromChannel(Long channelId, Long userId);
    
    List<User> getChannelMembers(Long channelId);
    
    Channel createDirectChannel(Workspace workspace, User user1, User user2);
}