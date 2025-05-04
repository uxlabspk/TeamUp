package io.github.uxlabspk.teamup.service;

import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;

import java.util.List;
import java.util.Optional;

public interface WorkspaceService {
    
    Workspace createWorkspace(Workspace workspace, User owner);
    
    Workspace updateWorkspace(Workspace workspace);
    
    Optional<Workspace> getWorkspaceById(Long id);
    
    Optional<Workspace> getWorkspaceByName(String name);
    
    List<Workspace> getAllWorkspaces();
    
    List<Workspace> getWorkspacesByOwner(User owner);
    
    List<Workspace> getWorkspacesByUserId(Long userId);
    
    boolean existsByName(String name);
    
    void deleteWorkspace(Long id);
    
    void addUserToWorkspace(Long workspaceId, Long userId);
    
    void removeUserFromWorkspace(Long workspaceId, Long userId);
    
    List<User> getWorkspaceMembers(Long workspaceId);
}