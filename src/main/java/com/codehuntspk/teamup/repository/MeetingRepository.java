package com.codehuntspk.teamup.repository;

import com.codehuntspk.teamup.entity.Meeting;
import com.codehuntspk.teamup.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByChannel(Channel channel);
    Optional<Meeting> findByRoomName(String roomName);
    List<Meeting> findByStatus(Meeting.MeetingStatus status);
}

