package com.example.chatbot.service;

import com.example.chatbot.dto.ChatRequest;
import com.example.chatbot.dto.ChatResponse;
import com.example.chatbot.entity.ChatMessage;
import com.example.chatbot.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatClient chatClient;

    @Transactional
    public ChatResponse processMessage(ChatRequest request) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        // 保存用户消息
        ChatMessage userMessage = new ChatMessage();
        userMessage.setContent(request.getMessage());
        userMessage.setRole("user");
        userMessage.setSessionId(sessionId);
        chatMessageRepository.save(userMessage);

        // 获取历史消息
        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        StringBuilder context = new StringBuilder();
        for (ChatMessage msg : history) {
            context.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
        }

        // 调用AI模型
        String aiResponse = chatClient.prompt()
                .user(context.toString())
                .call()
                .content();

        // 保存AI响应
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setContent(aiResponse);
        assistantMessage.setRole("assistant");
        assistantMessage.setSessionId(sessionId);
        chatMessageRepository.save(assistantMessage);

        return ChatResponse.builder()
                .message(assistantMessage.getContent())
                .sessionId(sessionId)
                .build();
    }

    public List<ChatResponse> getHistory(String sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return messages.stream()
                .map(msg -> ChatResponse.builder()
                        .message(msg.getContent())
                        .sessionId(msg.getSessionId())
                        .role(msg.getRole())
                        .build())
                .collect(Collectors.toList());
    }

    public List<String> getAllSessions() {
        return chatMessageRepository.findDistinctSessionIdBy();
    }
} 