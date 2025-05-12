package com.example.chatbot.service;

import com.example.chatbot.dto.ChatRequest;
import com.example.chatbot.dto.ChatResponse;
import com.example.chatbot.entity.ChatMessage;
import com.example.chatbot.entity.KnowledgeBase;
import com.example.chatbot.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final Map<String, ChatClient> chatClients;
    private final KnowledgeService knowledgeService;

    @Transactional
    public ChatResponse processMessage(ChatRequest request) {
        String sessionId = getOrCreateSessionId(request.getSessionId());
        String modelId = request.getModelId() != null ? request.getModelId() : "qwen3";
        ChatClient chatClient = chatClients.get(modelId);
        
        if (chatClient == null) {
            throw new IllegalArgumentException("Invalid model ID: " + modelId);
        }

        // 清理用户消息
        String cleanedMessage = cleanMessage(request.getMessage());
        saveUserMessage(cleanedMessage, sessionId);
        
        // 搜索相关知识库内容
        List<KnowledgeBase> relevantDocs = knowledgeService.searchByKeyword(cleanedMessage);
        StringBuilder contextBuilder = new StringBuilder();
        if (!relevantDocs.isEmpty()) {
            contextBuilder.append("相关文档：\n");
            for (KnowledgeBase doc : relevantDocs) {
                contextBuilder.append("标题：").append(doc.getTitle()).append("\n");
                contextBuilder.append("内容：").append(doc.getContent()).append("\n\n");
            }
        }

        // 构建消息上下文
        List<Message> messages = buildMessageContext(sessionId);
        
        // 如果有相关文档，添加到用户消息中
        if (!contextBuilder.isEmpty()) {
            String enhancedMessage = cleanedMessage + "\n\n" + contextBuilder;
            messages.add(new UserMessage(enhancedMessage));
        } else {
            messages.add(new UserMessage(cleanedMessage));
        }
        
        // 调用AI模型
        String aiResponse = chatClient.prompt()
                .messages(messages)
                .call()
                .content();

        // 清理AI响应
        assert aiResponse != null;
        String cleanedResponse = cleanAiResponse(aiResponse);

        // 保存AI响应
        saveAssistantMessage(cleanedResponse, sessionId);

        return ChatResponse.builder()
                .message(cleanedResponse)
                .sessionId(sessionId)
                .modelId(modelId)
                .build();
    }

    private String getOrCreateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return sessionId;
    }

    private void saveUserMessage(String content, String sessionId) {
        ChatMessage userMessage = new ChatMessage();
        userMessage.setContent(content);
        userMessage.setRole("user");
        userMessage.setSessionId(sessionId);
        chatMessageRepository.save(userMessage);
    }

    private void saveAssistantMessage(String content, String sessionId) {
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setContent(content);
        assistantMessage.setRole("assistant");
        assistantMessage.setSessionId(sessionId);
        chatMessageRepository.save(assistantMessage);
    }

    private List<Message> buildMessageContext(String sessionId) {
        // 获取最近的10条消息
        List<ChatMessage> history = chatMessageRepository.findLast10BySessionIdOrderByCreatedAtDesc(sessionId);
        // 反转列表以保持时间顺序
        Collections.reverse(history);

        // 构建对话上下文
        List<Message> messages = new ArrayList<>();
        
        // 添加系统提示，包含知识库信息
        String systemPrompt = "你是一个专业的客服助手，请根据以下知识库内容回答用户问题。如果知识库中没有相关信息，请明确告知用户。\n\n";
        messages.add(new SystemMessage(systemPrompt));

        // 添加历史消息
        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        return messages;
    }

    private String cleanAiResponse(String response) {
        if (response.contains("<think>")) {
            int startIndex = response.indexOf("<think>");
            int endIndex = response.indexOf("</think>");
            if (startIndex != -1 && endIndex != -1) {
                return response.substring(endIndex + 8).trim();
            }
        }
        return response;
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

    @Transactional
    public void deleteSession(String sessionId) {
        chatMessageRepository.deleteBySessionId(sessionId);
    }

    private String cleanMessage(String message) {
        if (message == null) {
            return "";
        }
        // 去除前后的空白字符和特殊转义符
        return message.trim()
                .replaceAll("^[\\n\\t\\r]+|[\\n\\t\\r]+$", "") // 去除前后的换行、制表符
                .replaceAll("\\s+", " "); // 将中间的多个空白字符替换为单个空格
    }

    public void processMessageStream(String sessionId, String message, String modelId, SseEmitter emitter) {
        try {
            // 获取或创建会话ID
            String currentSessionId = getOrCreateSessionId(sessionId);
            String currentModelId = modelId != null ? modelId : "qwen";
            ChatClient chatClient = chatClients.get(currentModelId);
            
            if (chatClient == null) {
                throw new IllegalArgumentException("Invalid model ID: " + currentModelId);
            }

            // 保存用户消息
            saveUserMessage(message, currentSessionId);

            // 构建消息上下文
            List<Message> messages = buildMessageContext(currentSessionId);

            // 发送流式响应
            String response = chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();

            // 将响应分块发送
            if (response != null) {
                // 每次发送一个字符，模拟流式效果
                for (char c : response.toCharArray()) {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(String.valueOf(c)));
                        Thread.sleep(10); // 添加小延迟使流式效果更明显
                    } catch (IOException | InterruptedException e) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(e.getMessage()));
                        } catch (IOException ex) {
                            // 忽略发送错误消息时的异常
                        }
                    }
                }
            }

            // 保存完整的助手回复
            saveAssistantMessage(response, currentSessionId);

            // 完成流式响应
            emitter.send(SseEmitter.event()
                    .name("done")
                    .data("[DONE]"));
            emitter.complete();

        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(e.getMessage()));
            } catch (IOException ex) {
                // 忽略发送错误消息时的异常
            }
        }
    }
} 