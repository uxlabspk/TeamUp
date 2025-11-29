package com.codehuntspk.teamup.repository;

import com.codehuntspk.teamup.entity.Message;
import com.codehuntspk.teamup.entity.Channel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChannelOrderByTimestampDesc(Channel channel);
    Page<Message> findByChannelOrderByTimestampDesc(Channel channel, Pageable pageable);
}

