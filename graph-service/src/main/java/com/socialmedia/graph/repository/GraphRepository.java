package com.socialmedia.graph.repository;

import com.socialmedia.graph.model.UserNode;
import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GraphRepository extends Neo4jRepository<UserNode, Long> {

    @Query("""
            MERGE (u:User {userId: $followerId})
            ON CREATE SET u.createdAt = datetime()
            MERGE (t:User {userId: $targetId})
            ON CREATE SET t.createdAt = datetime()
            MERGE (u)-[r:FOLLOWS]->(t)
            ON CREATE SET r.followedAt = datetime()
            """)
    void follow(Long followerId, Long targetId);

    @Query("""
            MATCH (u:User {userId: $followerId})-[r:FOLLOWS]->(t:User {userId: $targetId})
            DELETE r
            """)
    void unfollow(Long followerId, Long targetId);

    @Query("""
            MATCH (u:User {userId: $userId})<-[:FOLLOWS]-(f:User)
            RETURN f
            SKIP $offset LIMIT $limit
            """)
    List<UserNode> findFollowersPaged(Long userId, long offset, int limit);

    @Query("""
            MATCH (u:User {userId: $userId})<-[:FOLLOWS]-(f:User)
            RETURN count(f)
            """)
    long countFollowers(Long userId);

    @Query("""
            MATCH (u:User {userId: $userId})-[:FOLLOWS]->(f:User)
            RETURN f
            SKIP $offset LIMIT $limit
            """)
    List<UserNode> findFollowingPaged(Long userId, long offset, int limit);

    @Query("""
            MATCH (u:User {userId: $userId})-[:FOLLOWS]->(f:User)
            RETURN count(f)
            """)
    long countFollowing(Long userId);
}

