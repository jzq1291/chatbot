package com.example.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed-origins}")
    private String allowedOriginsStr;
    
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        if (!StringUtils.hasText(allowedOriginsStr)) {
            throw new IllegalStateException("cors.allowed-origins must be configured in application.yml");
        }

        String[] allowedOrigins = Arrays.stream(allowedOriginsStr.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);

        if (allowedOrigins.length == 0) {
            throw new IllegalStateException("No valid origins found in cors.allowed-origins configuration");
        }

        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600); // 1小时的预检请求缓存
    }
}
