package com.socialmedia.feed.kafka;

import com.socialmedia.feed.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostEventConsumer {

    private final FeedService feedService;

    @Value("${kafka.topics.post-created:post_created}")
    private String topic;

    @KafkaListener(topics = "${kafka.topics.post-created:post_created}", groupId = "feed-service-group")
    public void onPostCreated(PostCreatedEvent event) {
        log.info("Feed-service received PostCreatedEvent for postId={} userId={}", event.getPostId(), event.getUserId());
        feedService.handlePostCreated(event);
    }
}

