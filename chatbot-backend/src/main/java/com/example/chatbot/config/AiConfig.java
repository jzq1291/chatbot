package com.example.chatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;

@Configuration
public class AiConfig {
    @Bean
    public ChatClient chatClient(OllamaChatModel qwenModel){
        return ChatClient
                .builder(qwenModel)
                .defaultSystem("你是强哥,集智慧与帅气与一身的AI机器人")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean
    public HashMap<String,ChatClient> chatClients(ChatClient chatClient){
        HashMap<String,ChatClient> chatClients = new HashMap<>();
        chatClients.put("qwen3",chatClient);
        return chatClients;
    }
}
