package io.github.uxlabspk.teamup.service.impl;

import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;
import io.github.uxlabspk.teamup.repository.UserRepository;
import io.github.uxlabspk.teamup.repository.WorkspaceRepository;
import io.github.uxlabspk.teamup.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Workspace createWorkspace(Workspace workspace, User owner) {
        if (existsByName(workspace.getName())) {
            throw new IllegalArgumentException("Workspace name already exists");
        }

        workspace.setCreatedAt(LocalDateTime.now());
        workspace.setOwner(owner);

        // Add owner to workspace members
        workspace.getUsers().add(owner);
        owner.getWorkspaces().add(workspace);

        return workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public Workspace updateWorkspace(Workspace workspace) {
        return workspaceRepository.save(workspace);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Workspace> getWorkspaceById(Long id) {
        return workspaceRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Workspace> getWorkspaceByName(String name) {
        return workspaceRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workspace> getAllWorkspaces() {
        return workspaceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workspace> getWorkspacesByOwner(User owner) {
        return workspaceRepository.findByOwner(owner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workspace> getWorkspacesByUserId(Long userId) {
        return workspaceRepository.findWorkspacesByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return workspaceRepository.existsByName(name);
    }

    @Override
    @Transactional
    public void deleteWorkspace(Long id) {
        workspaceRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addUserToWorkspace(Long workspaceId, Long userId) {
        Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (workspaceOpt.isPresent() && userOpt.isPresent()) {
            Workspace workspace = workspaceOpt.get();
            User user = userOpt.get();

            workspace.getUsers().add(user);
            user.getWorkspaces().add(workspace);
            workspaceRepository.save(workspace);
        } else {
            throw new IllegalArgumentException("Workspace or User not found");
        }
    }

    @Override
    @Transactional
    public void removeUserFromWorkspace(Long workspaceId, Long userId) {
        Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (workspaceOpt.isPresent() && userOpt.isPresent()) {
            Workspace workspace = workspaceOpt.get();
            User user = userOpt.get();

            // Don't remove the owner
            if (workspace.getOwner().equals(user)) {
                throw new IllegalArgumentException("Cannot remove the workspace owner");
            }

            workspace.getUsers().remove(user);
            user.getWorkspaces().remove(workspace);
            workspaceRepository.save(workspace);
        } else {
            throw new IllegalArgumentException("Workspace or User not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getWorkspaceMembers(Long workspaceId) {
        Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);

        if (workspaceOpt.isPresent()) {
            return new ArrayList<>(workspaceOpt.get().getUsers());
        } else {
            throw new IllegalArgumentException("Workspace not found");
        }
    }
}
