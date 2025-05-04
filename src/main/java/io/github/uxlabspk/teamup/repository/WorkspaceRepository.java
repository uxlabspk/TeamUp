package io.github.uxlabspk.teamup.repository;

import io.github.uxlabspk.teamup.model.User;
import io.github.uxlabspk.teamup.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    
    Optional<Workspace> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Workspace> findByOwner(User owner);
    
    @Query("SELECT w FROM Workspace w JOIN w.users u WHERE u.id = :userId")
    List<Workspace> findWorkspacesByUserId(Long userId);
}