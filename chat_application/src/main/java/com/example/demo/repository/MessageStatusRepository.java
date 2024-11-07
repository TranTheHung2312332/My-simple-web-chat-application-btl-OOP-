package com.example.demo.repository;

import com.example.demo.entity.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageStatusRepository
        extends JpaRepository<MessageStatus, MessageStatus.MessageStatusId> {
}
