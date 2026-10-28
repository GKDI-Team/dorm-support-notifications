package com.dormsupport.notifications.event;

import com.dormsupport.notifications.notification.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class IssueEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(IssueEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public IssueEventConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "${events.issue-topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            IssueEvent event = objectMapper.readValue(record.value(), IssueEvent.class);
            log.info("Received issue event: eventId={}, type={}, issueId={}, topic={}, partition={}, offset={}",
                    event.eventId(), event.eventType(), event.issueId(),
                    record.topic(), record.partition(), record.offset());
            notificationService.process(event);
        } catch (JsonProcessingException exception) {
            log.error("Skipping invalid issue event from topic {} at offset {}",
                    record.topic(), record.offset(), exception);
        }
    }
}
