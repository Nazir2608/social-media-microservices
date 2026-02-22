package com.socialmedia.post.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPostEventPublisher implements PostEventPublisher {

    private final KafkaTemplate<String, PostCreatedEvent> kafkaTemplate;

    @Value("${kafka.topics.post-created:post_created}")
    private String topic;

    @Override
    public void publishPostCreated(PostCreatedEvent event) {
        log.debug("Publishing PostCreatedEvent to topic={} with postId={}", topic, event.getPostId());
        kafkaTemplate.send(topic, String.valueOf(event.getPostId()), event);
    }
}
