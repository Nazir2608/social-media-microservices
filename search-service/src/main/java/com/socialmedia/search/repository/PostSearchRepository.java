package com.socialmedia.search.repository;

import com.socialmedia.search.document.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Long> {
}

