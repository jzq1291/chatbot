package com.example.chatbot.controller;

import com.example.chatbot.dto.ChatRequest;
import com.example.chatbot.dto.ChatResponse;
import com.example.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatResponse sendMessage(@RequestBody ChatRequest request) {
        return chatService.sendMessage(request);
    }

    @GetMapping("/history/{sessionId}")
    public List<ChatResponse> getHistory(@PathVariable String sessionId) {
        return chatService.getHistory(sessionId);
    }
} 