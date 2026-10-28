package com.dormsupport.notifications.notification;

import com.dormsupport.notifications.telegram.TelegramClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

@Service
public class NotificationDelivery {
    private static final Logger log = LoggerFactory.getLogger(NotificationDelivery.class);
    private final NotificationRepository repository;
    private final TelegramClient telegram;

    public NotificationDelivery(NotificationRepository repository, TelegramClient telegram) {
        this.repository = repository;
        this.telegram = telegram;
    }

    @Scheduled(fixedDelayString = "${telegram.delivery-interval-ms:5000}")
    @Transactional
    public void deliverPending() {
        for (Notification notification : repository.findForDelivery(NotificationDeliveryStatus.PENDING, PageRequest.of(0, 1))) {
            if (!telegram.isEnabled()) {
                notification.markSkipped();
                continue;
            }
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    telegram.sendMessage(notification.telegramMessage());
                    notification.markSent();
                    log.info("Telegram notification sent: eventId={}", notification.getEventId());
                    break;
                } catch (Exception exception) {
                    boolean retryable = !(exception instanceof RestClientResponseException response)
                            || response.getStatusCode().value() == 429
                            || response.getStatusCode().is5xxServerError();
                    if (!retryable || attempt == 3) {
                        notification.markFailed("Telegram delivery failed" + (exception instanceof RestClientResponseException response
                                ? ": HTTP " + response.getStatusCode().value() : ""));
                        log.warn("Telegram notification failed: eventId={}, attempts={}", notification.getEventId(), attempt);
                        break;
                    }
                    try {
                        long delayMillis = attempt * 2000L;
                        if (exception instanceof RestClientResponseException response
                                && response.getStatusCode().value() == 429 && response.getResponseHeaders() != null) {
                            String retryAfter = response.getResponseHeaders().getFirst("Retry-After");
                            if (retryAfter != null) {
                                try {
                                    delayMillis = Math.max(delayMillis, Math.min(60_000L, Long.parseLong(retryAfter) * 1000L));
                                } catch (NumberFormatException ignored) {
                                    // Fall back to the normal delay.
                                }
                            }
                        }
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Telegram delivery interrupted", interrupted);
                    }
                }
            }
        }
    }
}
