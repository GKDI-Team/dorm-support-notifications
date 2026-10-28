package com.dormsupport.notifications.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", uniqueConstraints =
        @UniqueConstraint(name = "uk_notifications_event_id", columnNames = "event_id"))
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "issue_id", nullable = false)
    private UUID issueId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IssueStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private IssueStatus previousStatus;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "description_preview", length = 500)
    private String descriptionPreview;

    @Column(length = 50)
    private String block;

    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @Column(name = "location_details", length = 250)
    private String locationDetails;

    @Column(name = "category_name", length = 80)
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 20)
    private NotificationDeliveryStatus deliveryStatus;

    @Column(name = "delivery_error", columnDefinition = "TEXT")
    private String deliveryError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    protected Notification() {
        // Required by JPA.
    }

    public Notification(com.dormsupport.notifications.event.IssueEvent event) {
        this.eventId = event.eventId();
        this.eventType = event.eventType();
        this.issueId = event.issueId();
        this.status = event.status();
        this.previousStatus = event.previousStatus();
        this.occurredAt = event.occurredAt();
        this.title = event.title();
        this.descriptionPreview = event.descriptionPreview();
        this.block = event.block();
        this.roomNumber = event.roomNumber();
        this.locationDetails = event.locationDetails();
        this.categoryName = event.categoryName();
        this.deliveryStatus = NotificationDeliveryStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (deliveryStatus == null) {
            deliveryStatus = NotificationDeliveryStatus.PENDING;
        }
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getIssueId() { return issueId; }
    public NotificationDeliveryStatus getDeliveryStatus() { return deliveryStatus; }

    public void markSent() {
        deliveryStatus = NotificationDeliveryStatus.SENT;
        sentAt = Instant.now();
        deliveryError = null;
    }

    public void markSkipped() {
        deliveryStatus = NotificationDeliveryStatus.SKIPPED;
        deliveryError = null;
    }

    public void markFailed(String error) {
        deliveryStatus = NotificationDeliveryStatus.FAILED;
        deliveryError = error;
    }
}
