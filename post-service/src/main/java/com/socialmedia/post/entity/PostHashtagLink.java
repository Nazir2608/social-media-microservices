package com.socialmedia.post.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_hashtag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostHashtagLink {

    @EmbeddedId
    private PostHashtag id;
}
