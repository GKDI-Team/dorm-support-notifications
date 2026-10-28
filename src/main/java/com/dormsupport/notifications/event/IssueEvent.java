package com.dormsupport.notifications.event;

import com.dormsupport.notifications.notification.IssueStatus;

import java.time.Instant;
import java.util.UUID;

public record IssueEvent(
        UUID eventId,
        String eventType,
        UUID issueId,
        IssueStatus status,
        IssueStatus previousStatus,
        Instant occurredAt,
        String title,
        String descriptionPreview,
        UUID blockId,
        String roomNumber,
        String locationDetails,
        String categoryName) {
}
