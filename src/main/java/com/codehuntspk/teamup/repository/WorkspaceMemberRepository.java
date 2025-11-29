package com.codehuntspk.teamup.repository;

import com.codehuntspk.teamup.entity.WorkspaceMember;
import com.codehuntspk.teamup.entity.Workspace;
import com.codehuntspk.teamup.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    List<WorkspaceMember> findByWorkspace(Workspace workspace);
    List<WorkspaceMember> findByUser(User user);
    Optional<WorkspaceMember> findByWorkspaceAndUser(Workspace workspace, User user);
    boolean existsByWorkspaceAndUser(Workspace workspace, User user);
}

