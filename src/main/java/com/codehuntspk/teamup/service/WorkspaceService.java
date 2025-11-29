package com.codehuntspk.teamup.service;

import com.codehuntspk.teamup.entity.Workspace;
import com.codehuntspk.teamup.entity.WorkspaceMember;
import com.codehuntspk.teamup.entity.User;
import com.codehuntspk.teamup.entity.Channel;
import com.codehuntspk.teamup.repository.WorkspaceRepository;
import com.codehuntspk.teamup.repository.WorkspaceMemberRepository;
import com.codehuntspk.teamup.repository.ChannelRepository;
import com.codehuntspk.teamup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkspaceService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Workspace createWorkspace(String name, String description, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Workspace workspace = new Workspace();
        workspace.setName(name);
        workspace.setDescription(description);
        workspace.setOwner(owner);
        workspace = workspaceRepository.save(workspace);

        // Add owner as member
        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace);
        member.setUser(owner);
        member.setRole(WorkspaceMember.MemberRole.OWNER);
        workspaceMemberRepository.save(member);

        // Create default general channel
        Channel generalChannel = new Channel();
        generalChannel.setName("general");
        generalChannel.setDescription("General discussion");
        generalChannel.setWorkspace(workspace);
        generalChannel.setType(Channel.ChannelType.PUBLIC);
        generalChannel.getMembers().add(owner);
        channelRepository.save(generalChannel);

        return workspace;
    }

    public List<Workspace> getUserWorkspaces(Long userId) {
        return workspaceRepository.findByMemberUserId(userId);
    }

    public Workspace getWorkspace(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
    }

    @Transactional
    public Workspace updateWorkspace(Long workspaceId, String name, String description) {
        Workspace workspace = getWorkspace(workspaceId);
        workspace.setName(name);
        workspace.setDescription(description);
        return workspaceRepository.save(workspace);
    }

    @Transactional
    public void deleteWorkspace(Long workspaceId) {
        workspaceRepository.deleteById(workspaceId);
    }

    public List<WorkspaceMember> getWorkspaceMembers(Long workspaceId) {
        Workspace workspace = getWorkspace(workspaceId);
        return workspaceMemberRepository.findByWorkspace(workspace);
    }
}

