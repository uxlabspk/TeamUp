package com.codehuntspk.teamup.controller;

import com.codehuntspk.teamup.entity.Workspace;
import com.codehuntspk.teamup.entity.WorkspaceMember;
import com.codehuntspk.teamup.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<Workspace> createWorkspace(
            @RequestBody Map<String, String> request,
            @RequestParam Long userId) {
        Workspace workspace = workspaceService.createWorkspace(
                request.get("name"),
                request.get("description"),
                userId
        );
        return ResponseEntity.ok(workspace);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Workspace>> getUserWorkspaces(@PathVariable Long userId) {
        List<Workspace> workspaces = workspaceService.getUserWorkspaces(userId);
        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<Workspace> getWorkspace(@PathVariable Long workspaceId) {
        Workspace workspace = workspaceService.getWorkspace(workspaceId);
        return ResponseEntity.ok(workspace);
    }

    @PutMapping("/{workspaceId}")
    public ResponseEntity<Workspace> updateWorkspace(
            @PathVariable Long workspaceId,
            @RequestBody Map<String, String> request) {
        Workspace workspace = workspaceService.updateWorkspace(
                workspaceId,
                request.get("name"),
                request.get("description")
        );
        return ResponseEntity.ok(workspace);
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable Long workspaceId) {
        workspaceService.deleteWorkspace(workspaceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<List<WorkspaceMember>> getMembers(@PathVariable Long workspaceId) {
        List<WorkspaceMember> members = workspaceService.getWorkspaceMembers(workspaceId);
        return ResponseEntity.ok(members);
    }
}

