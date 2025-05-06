package com.example.chatbot.service;

import com.example.chatbot.dto.ChatRequest;
import com.example.chatbot.dto.ChatResponse;
import java.util.List;

public interface ChatService {
    ChatResponse sendMessage(ChatRequest request);
    List<ChatResponse> getHistory(String sessionId);
} 