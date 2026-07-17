package com.silkroad.market.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.silkroad.market.entity.Chat;
import com.silkroad.market.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatOrderBySentAtAsc(Chat chat);
}