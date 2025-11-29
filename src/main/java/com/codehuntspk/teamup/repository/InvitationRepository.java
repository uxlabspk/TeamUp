package com.codehuntspk.teamup.repository;

import com.codehuntspk.teamup.entity.Invitation;
import com.codehuntspk.teamup.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);
    List<Invitation> findByWorkspace(Workspace workspace);
    List<Invitation> findByInviteeEmail(String email);
}

