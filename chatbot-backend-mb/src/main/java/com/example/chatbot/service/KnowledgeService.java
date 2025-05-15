package com.example.chatbot.service;

import com.example.chatbot.entity.KnowledgeBase;
import com.example.chatbot.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeService {
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private static final Logger log = LoggerFactory.getLogger(KnowledgeService.class);

    @Transactional
    public KnowledgeBase addKnowledge(KnowledgeBase knowledge) {
        log.debug("Adding new knowledge base entry: {}", knowledge.getTitle());
        return knowledgeBaseRepository.save(knowledge);
    }

    @Transactional
    public void deleteKnowledge(Long id) {
        log.debug("Deleting knowledge base entry with id: {}", id);
        knowledgeBaseRepository.deleteById(id);
    }

    public List<KnowledgeBase> searchByKeyword(String keyword) {
        String pattern = "%" + keyword + "%";
        log.debug("Searching knowledge base with pattern: {}", pattern);
        List<KnowledgeBase> results = knowledgeBaseRepository.searchByKeyword(pattern);
        log.debug("Found {} results", results.size());
        if (!results.isEmpty()) {
            log.debug("First result title: {}", results.get(0).getTitle());
        }
        return results;
    }

    public List<KnowledgeBase> findByCategory(String category) {
        return knowledgeBaseRepository.findByCategory(category);
    }

    @Transactional
    public KnowledgeBase updateKnowledge(Long id, KnowledgeBase knowledge) {
        KnowledgeBase existing = knowledgeBaseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("知识不存在"));
        existing.setTitle(knowledge.getTitle());
        existing.setContent(knowledge.getContent());
        existing.setCategory(knowledge.getCategory());
        // updatedAt 字段由 @PreUpdate 自动处理
        return knowledgeBaseRepository.save(existing);
    }
} 