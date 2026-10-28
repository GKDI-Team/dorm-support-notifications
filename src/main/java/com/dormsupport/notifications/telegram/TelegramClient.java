package com.dormsupport.notifications.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
public class TelegramClient {
    private static final Logger log = LoggerFactory.getLogger(TelegramClient.class);
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
        log.warn("TEMPORARY SECRET DEBUG: TELEGRAM_ENABLED={}, TELEGRAM_BOT_TOKEN={}, TELEGRAM_CHAT_ID={}",
                enabled, botToken, chatId);
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);
        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org")
                .requestFactory(requestFactory)
                .build();
    }

    public boolean isEnabled() {
        return enabled && !botToken.isBlank() && !chatId.isBlank();
    }

    public void sendMessage(String message) {
        if (!isEnabled()) {
            throw new IllegalStateException("Telegram is not configured");
        }

        restClient.post()
                .uri("/bot{token}/sendMessage", botToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("chat_id", chatId, "text", message))
                .retrieve()
                .toBodilessEntity();
    }
}
