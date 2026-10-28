package com.dormsupport.notifications.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class TelegramClient {
    private final boolean enabled;
    private final String botToken;
    private final String chatId;
    private final RestClient restClient;

    public TelegramClient(
            @Value("${telegram.enabled:false}") boolean enabled,
            @Value("${telegram.bot-token:}") String botToken,
            @Value("${telegram.chat-id:}") String chatId) {
        this.enabled = enabled;
        this.botToken = botToken;
        this.chatId = chatId;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org")
                .build();
    }

    public boolean isEnabled() {
        return enabled && !botToken.isBlank() && !chatId.isBlank();
    }

    public void sendMessage(String message) {
        if (!isEnabled()) {
            return;
        }

        restClient.post()
                .uri("/bot{token}/sendMessage", botToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("chat_id", chatId, "text", message))
                .retrieve()
                .toBodilessEntity();
    }
}
