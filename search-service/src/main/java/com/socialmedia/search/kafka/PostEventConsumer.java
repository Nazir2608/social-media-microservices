package com.socialmedia.search.kafka;

import com.socialmedia.search.service.PostSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostEventConsumer {

    private final PostSearchService postSearchService;

    @KafkaListener(topics = "post_created", groupId = "search-service-group")
    public void consume(PostCreatedEvent event) {
        log.info("Search-service received PostCreatedEvent for postId={} userId={}", event.getPostId(), event.getUserId());
        postSearchService.indexPost(
                postSearchService.fromEvent(
                        event.getPostId(),
                        event.getCaption(),
                        event.getHashtags(),
                        event.getCreatedAt()
                )
        );
    }
}

