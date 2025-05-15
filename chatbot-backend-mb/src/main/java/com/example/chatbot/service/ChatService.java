package com.example.chatbot.service;

import com.example.chatbot.config.ModelProperties;
import com.example.chatbot.dto.ChatRequest;
import com.example.chatbot.dto.ChatResponse;
import com.example.chatbot.entity.ChatMessage;
import com.example.chatbot.entity.KnowledgeBase;
import com.example.chatbot.entity.User;
import com.example.chatbot.mapper.ChatMessageMapper;
import com.example.chatbot.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatClient chatClient;
    private final ModelProperties modelProperties;
    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final KnowledgeService knowledgeService;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userMapper.findByUsername(authentication.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // 保存用户消息
        ChatMessage userMessage = new ChatMessage();
        userMessage.setContent(request.getMessage());
        userMessage.setRole("user");
        userMessage.setSessionId(request.getSessionId());
        userMessage.setUserId(user.getId());
        chatMessageMapper.insert(userMessage);

        // 获取历史消息
        List<ChatMessage> history = chatMessageMapper.findBySessionIdAndUserId(request.getSessionId(), user.getId());
        List<Message> messages = new ArrayList<>();

        // 添加系统消息
        messages.add(new SystemMessage(modelProperties.getSystemPrompt()));

        // 添加历史消息
        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // 添加当前用户消息
        messages.add(new UserMessage(request.getMessage()));

        // 调用AI服务
        AssistantMessage response = chatClient.call(messages, ChatOptions.builder()
                .withTemperature(modelProperties.getTemperature())
                .build());

        // 保存AI响应
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setContent(response.getContent());
        assistantMessage.setRole("assistant");
        assistantMessage.setSessionId(request.getSessionId());
        assistantMessage.setUserId(user.getId());
        chatMessageMapper.insert(assistantMessage);

        return new ChatResponse(response.getContent());
    }

    public List<String> getChatSessions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userMapper.findByUsername(authentication.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return chatMessageMapper.findDistinctSessionIdByUserId(user.getId());
    }

    @Transactional
    public void deleteChatSession(String sessionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userMapper.findByUsername(authentication.getName());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        chatMessageMapper.deleteBySessionIdAndUserId(sessionId, user.getId());
    }
} 