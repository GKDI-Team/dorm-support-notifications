package com.dormsupport.notifications.notification;

import com.dormsupport.notifications.event.IssueEvent;
import com.dormsupport.notifications.telegram.TelegramClient;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationDeliveryTest {
    private final NotificationRepository repository = mock(NotificationRepository.class);
    private final TelegramClient telegram = mock(TelegramClient.class);

    private IssueEvent event(String type) {
        return new IssueEvent(UUID.randomUUID(), type, UUID.randomUUID(), IssueStatus.NEW, null,
                Instant.now(), "Розетка", "Не работает", "2", "301", "У окна", "Электрика");
    }

    @Test
    void createdEventIsQueuedAndDuplicateIsIgnored() {
        IssueEvent event = event("issue.created");
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        NotificationService service = new NotificationService(repository);
        service.process(event);
        verify(repository).save(argThat(notification -> notification.getDeliveryStatus() == NotificationDeliveryStatus.PENDING));
        when(repository.existsByEventId(event.eventId())).thenReturn(true);
        service.process(event);
        verify(repository, times(1)).save(any(Notification.class));
    }

    @Test
    void statusEventIsSkipped() {
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        new NotificationService(repository).process(event("issue.status_changed"));
        verify(repository).save(argThat(n -> n.getDeliveryStatus() == NotificationDeliveryStatus.SKIPPED));
    }

    @Test
    void sendsMessageAndMarksSent() {
        Notification notification = new Notification(event("issue.created"));
        when(repository.findForDelivery(eq(NotificationDeliveryStatus.PENDING), any(Pageable.class))).thenReturn(List.of(notification));
        when(telegram.isEnabled()).thenReturn(true);
        new NotificationDelivery(repository, telegram).deliverPending();
        verify(telegram).sendMessage(argThat(message -> message.contains("Розетка") && message.contains("301")));
        assertEquals(NotificationDeliveryStatus.SENT, notification.getDeliveryStatus());
        assertTrue(notification.telegramMessage().contains(notification.getIssueId().toString()));
    }

    @Test
    void disabledTelegramSkipsWithoutSending() {
        Notification notification = new Notification(event("issue.created"));
        when(repository.findForDelivery(eq(NotificationDeliveryStatus.PENDING), any(Pageable.class))).thenReturn(List.of(notification));
        new NotificationDelivery(repository, telegram).deliverPending();
        verify(telegram, never()).sendMessage(any());
        assertEquals(NotificationDeliveryStatus.SKIPPED, notification.getDeliveryStatus());
    }
}
