package com.codehuntspk.teamup.controller;

import com.codehuntspk.teamup.entity.Invitation;
import com.codehuntspk.teamup.entity.WorkspaceMember;
import com.codehuntspk.teamup.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @PostMapping
    public ResponseEntity<Invitation> createInvitation(
            @RequestParam Long workspaceId,
            @RequestParam Long inviterId,
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        Invitation invitation = invitationService.createInvitation(workspaceId, inviterId, email);
        return ResponseEntity.ok(invitation);
    }

    @PostMapping("/accept/{token}")
    public ResponseEntity<WorkspaceMember> acceptInvitation(
            @PathVariable String token,
            @RequestParam Long userId) {

        WorkspaceMember member = invitationService.acceptInvitation(token, userId);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<Invitation>> getWorkspaceInvitations(@PathVariable Long workspaceId) {
        List<Invitation> invitations = invitationService.getWorkspaceInvitations(workspaceId);
        return ResponseEntity.ok(invitations);
    }
}

