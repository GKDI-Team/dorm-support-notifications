package com.dormsupport.notifications.notification;

import com.dormsupport.notifications.event.IssueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void process(IssueEvent event) {
        if (notificationRepository.existsByEventId(event.eventId())) {
            log.info("Skipping duplicate notification event {}", event.eventId());
            return;
        }

        Notification notification = notificationRepository.save(new Notification(event));
        if (!"issue.created".equals(event.eventType())) {
            notification.markSkipped();
        }
        log.info("Notification saved: notificationId={}, eventId={}, issueId={}, status={}",
                notification.getId(), event.eventId(), event.issueId(), notification.getDeliveryStatus());
    }
}
