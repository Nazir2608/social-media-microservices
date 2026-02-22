package com.socialmedia.post.kafka;

public interface PostEventPublisher {

    void publishPostCreated(PostCreatedEvent event);
}

