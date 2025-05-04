package io.github.uxlabspk.teamup.repository;

import io.github.uxlabspk.teamup.model.Channel;
import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    
    List<Channel> findByWorkspace(Workspace workspace);
    
    Optional<Channel> findByNameAndWorkspace(String name, Workspace workspace);
    
    boolean existsByNameAndWorkspace(String name, Workspace workspace);
    
    @Query("SELECT c FROM Channel c JOIN c.members m WHERE m.id = :userId AND c.workspace.id = :workspaceId")
    List<Channel> findChannelsByUserIdAndWorkspaceId(Long userId, Long workspaceId);
    
    @Query("SELECT c FROM Channel c WHERE c.type = 'DIRECT' AND c.workspace.id = :workspaceId AND EXISTS (SELECT m FROM c.members m WHERE m.id = :userId1) AND EXISTS (SELECT m FROM c.members m WHERE m.id = :userId2)")
    Optional<Channel> findDirectChannelBetweenUsers(Long workspaceId, Long userId1, Long userId2);
}