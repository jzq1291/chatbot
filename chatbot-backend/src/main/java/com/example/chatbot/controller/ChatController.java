package com.example.chatbot.controller;

import com.example.chatbot.dto.ChatRequest;
import com.example.chatbot.dto.ChatResponse;
import com.example.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }
//    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public ResponseEntity<Flux<ChatResponse>> chatStream(@RequestBody ChatRequest request) {
//        Flux<ChatResponse> responseFlux = chatService.processMessageStream(request);
//        return ResponseEntity.ok()
//                .contentType(MediaType.TEXT_EVENT_STREAM)
//                .body(responseFlux);
//    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatResponse>> getHistory(@PathVariable String sessionId) {
        List<ChatResponse> history = chatService.getHistory(sessionId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<String>> getAllSessions() {
        List<String> sessions = chatService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }
}
