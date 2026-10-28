package com.dormsupport.notifications.notification;

import com.dormsupport.notifications.event.IssueEvent;
import com.dormsupport.notifications.telegram.TelegramClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final TelegramClient telegramClient;

    public NotificationService(NotificationRepository notificationRepository, TelegramClient telegramClient) {
        this.notificationRepository = notificationRepository;
        this.telegramClient = telegramClient;
    }

    @Transactional
    public void process(IssueEvent event) {
        if (notificationRepository.existsByEventId(event.eventId())) {
            log.info("Skipping duplicate notification event {}", event.eventId());
            return;
        }

        Notification notification = notificationRepository.save(new Notification(event));
        if (!telegramClient.isEnabled()) {
            notification.markSkipped();
            log.info("Telegram delivery is disabled; notification skipped: notificationId={}, eventId={}, issueId={}",
                    notification.getId(), event.eventId(), event.issueId());
            return;
        }

        try {
            telegramClient.sendMessage(formatMessage(event));
            notification.markSent();
            log.info("Telegram notification sent: notificationId={}, eventId={}, issueId={}",
                    notification.getId(), event.eventId(), event.issueId());
        } catch (RuntimeException exception) {
            String error = exception.getMessage() == null
                    ? exception.getClass().getSimpleName()
                    : exception.getMessage();
            notification.markFailed(error);
            log.error("Telegram notification failed: notificationId={}, eventId={}, issueId={}",
                    notification.getId(), event.eventId(), event.issueId(), exception);
        }
    }

    private String formatMessage(IssueEvent event) {
        return "DormSupport: новая заявка\n"
                + "Тип события: " + event.eventType()
                + "\nСтатус: " + event.status()
                + "\nID заявки: " + event.issueId();
    }
}
