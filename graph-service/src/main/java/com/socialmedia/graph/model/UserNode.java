package com.socialmedia.graph.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("User")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNode {

    @Id
    private Long userId;

    private String username;

    private OffsetDateTime createdAt;
}
