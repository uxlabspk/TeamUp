package com.codehuntspk.teamup.service;

import com.codehuntspk.teamup.entity.Invitation;
import com.codehuntspk.teamup.entity.Workspace;
import com.codehuntspk.teamup.entity.WorkspaceMember;
import com.codehuntspk.teamup.entity.User;
import com.codehuntspk.teamup.repository.InvitationRepository;
import com.codehuntspk.teamup.repository.WorkspaceRepository;
import com.codehuntspk.teamup.repository.WorkspaceMemberRepository;
import com.codehuntspk.teamup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Invitation createInvitation(Long workspaceId, Long inviterId, String inviteeEmail) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Invitation invitation = new Invitation();
        invitation.setWorkspace(workspace);
        invitation.setInviter(inviter);
        invitation.setInviteeEmail(inviteeEmail);

        return invitationRepository.save(invitation);
    }

    @Transactional
    public WorkspaceMember acceptInvitation(String token, Long userId) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (invitation.getStatus() != Invitation.InvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is not pending");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(Invitation.InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new RuntimeException("Invitation has expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if already a member
        if (workspaceMemberRepository.existsByWorkspaceAndUser(invitation.getWorkspace(), user)) {
            throw new RuntimeException("User is already a member");
        }

        // Add user as member
        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(invitation.getWorkspace());
        member.setUser(user);
        member.setRole(WorkspaceMember.MemberRole.MEMBER);
        workspaceMemberRepository.save(member);

        // Update invitation status
        invitation.setStatus(Invitation.InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        return member;
    }

    public List<Invitation> getWorkspaceInvitations(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
        return invitationRepository.findByWorkspace(workspace);
    }
}

