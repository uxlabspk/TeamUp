package com.codehuntspk.teamup.repository;

import com.codehuntspk.teamup.entity.Channel;
import com.codehuntspk.teamup.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findByWorkspace(Workspace workspace);
    List<Channel> findByWorkspaceAndIsDirectMessage(Workspace workspace, boolean isDirectMessage);
}

