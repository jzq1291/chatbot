package com.example.chatbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chatbot.dto.ChatRequest;
import com.example.chatbot.dto.ChatResponse;
import com.example.chatbot.entity.ChatMessage;
import com.example.chatbot.entity.ChatSession;
import com.example.chatbot.mapper.ChatMessageMapper;
import com.example.chatbot.mapper.ChatSessionMapper;
import com.example.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final OllamaChatClient chatClient;

    @Override
    @Transactional
    public com.example.chatbot.dto.ChatResponse sendMessage(ChatRequest request) {
        ChatSession session = sessionMapper.selectById(Long.parseLong(request.getSessionId()));
        if (session == null) {
            throw new RuntimeException("Session not found");
        }

        // 保存用户消息
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setRole("user");
        userMessage.setContent(request.getMessage());
        messageMapper.insert(userMessage);

        // 获取历史消息
        List<ChatMessage> history = messageMapper.findBySessionIdOrderByCreatedAtAsc(session.getId());
        String prompt = buildPrompt(history);

        // 调用AI服务
        ChatResponse aiResponse = chatClient.call(new Prompt(prompt));
        String aiMessage = aiResponse.getResult().getOutput().getContent();

        // 保存AI响应
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(aiMessage);
        messageMapper.insert(assistantMessage);

        com.example.chatbot.dto.ChatResponse response = new com.example.chatbot.dto.ChatResponse();
        response.setMessage(aiMessage);
        response.setSessionId(request.getSessionId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.example.chatbot.dto.ChatResponse> getHistory(String sessionId) {
        List<ChatMessage> messages = messageMapper.findBySessionIdOrderByCreatedAtAsc(Long.parseLong(sessionId));
        return messages.stream()
                .map(message -> {
                    com.example.chatbot.dto.ChatResponse response = new com.example.chatbot.dto.ChatResponse();
                    response.setMessage(message.getContent());
                    response.setSessionId(sessionId);
                    return response;
                })
                .collect(Collectors.toList());
    }

    private String buildPrompt(List<ChatMessage> history) {
        StringBuilder prompt = new StringBuilder();
        for (ChatMessage message : history) {
            prompt.append(message.getRole()).append(": ").append(message.getContent()).append("\n");
        }
        return prompt.toString();
    }
} 