package com.example.chatbot.repository;

import com.example.chatbot.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {
    List<KnowledgeBase> findByCategory(String category);
    
    @Query(value = "SELECT * FROM knowledge_base WHERE " +
            "LOWER(title) LIKE LOWER(:pattern) OR " +
            "LOWER(content) LIKE LOWER(:pattern)", nativeQuery = true)
    List<KnowledgeBase> searchByKeyword(@Param("pattern") String pattern);
}