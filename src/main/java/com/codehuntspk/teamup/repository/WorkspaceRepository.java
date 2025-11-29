package com.codehuntspk.teamup.repository;

import com.codehuntspk.teamup.entity.Workspace;
import com.codehuntspk.teamup.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findByOwner(User owner);

    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.user.id = :userId")
    List<Workspace> findByMemberUserId(@Param("userId") Long userId);
}

