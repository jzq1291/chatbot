package com.example.chatbot.repository;

import com.example.chatbot.entity.ChatMessage;
import com.example.chatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdAndUserOrderByCreatedAtAsc(String sessionId, User user);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.user = :user ORDER BY m.createdAt DESC LIMIT 10")
    List<ChatMessage> findLast10BySessionIdAndUserOrderByCreatedAtDesc(String sessionId, User user);

    @Query("SELECT DISTINCT c.sessionId FROM ChatMessage c WHERE c.user = :user")
    List<String> findDistinctSessionIdByUser(User user);

    void deleteBySessionIdAndUser(String sessionId, User user);
} 