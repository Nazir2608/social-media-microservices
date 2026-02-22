package com.socialmedia.post.repository;

import com.socialmedia.post.entity.PostHashtag;
import com.socialmedia.post.entity.PostHashtagLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHashtagLinkRepository extends JpaRepository<PostHashtagLink, PostHashtag> {
}

