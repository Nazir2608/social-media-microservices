# Social Media Backend – Microservices Architecture

This project is a scalable, event-driven social media backend inspired by Instagram.

It demonstrates how to design and implement a distributed system using:

- Java 21
- Spring Boot 3
- Spring Cloud Gateway
- MySQL
- Neo4j
- Redis
- Apache Kafka
- Elasticsearch
- Docker / Docker Compose

The system is designed to handle:

- User registration, login and JWT-based authentication
- Media uploads and post management
- Social graph (follow / unfollow)
- Personalized feed generation
- Full‑text and hashtag search
- High-throughput event processing via Kafka

The architecture follows:

- Microservices architecture
- API Gateway + JWT auth at the edge
- Event-driven communication (Kafka)
- CQRS-style read models (feed-service, search-service)
- Distributed data management with specialized stores

---

## High-Level System Design

### Core Services

- **API Gateway (Spring Cloud Gateway)**
  - Single entry point for all clients (Postman, web, mobile).
  - Validates JWT using `JwtUtil`.
  - Injects `X-User-Id` header for downstream services.
  - Routes:
    - `/api/users/**` → user-service
    - `/api/posts/**` → post-service
    - `/api/graph/**` → graph-service
    - `/api/feed/**` → feed-service
    - `/api/search/**` → search-service

- **User Service**
  - Stack: Spring Boot, Spring Security, JPA, MySQL, Redis.
  - Responsibilities:
    - Register / login users.
    - Hash passwords with BCrypt.
    - Issue JWT tokens with `userId` + `username` claims.
    - Manage user profile and bio.
  - Data:
    - `users` table in MySQL.

- **Post Service**
  - Stack: Spring Boot, JPA, MySQL, Kafka.
  - Responsibilities:
    - Accept media uploads and captions.
    - Parse and normalize hashtags.
    - Persist posts and hashtags.
    - Publish `PostCreatedEvent` to Kafka topic `post_created`.
  - Data:
    - `posts` table.
    - `hashtags` and link tables for post–hashtag relations.

- **Graph Service**
  - Stack: Spring Boot, Spring Data Neo4j, Neo4j.
  - Responsibilities:
    - Manage follow / unfollow relationships.
    - Store the social graph: `(:User)-[:FOLLOWS]->(:User)`.
    - Provide paginated APIs to fetch followers and following.
  - Data:
    - `User` nodes in Neo4j with `userId` and `username`.

- **Feed Service**
  - Stack: Spring Boot, Redis, Kafka, RestTemplate.
  - Responsibilities:
    - Consume `PostCreatedEvent` from Kafka.
    - For each new post:
      - Fetch followers of the author from graph-service.
      - Fan out post IDs into Redis sorted sets:
        - `feed:{userId}` with score = post creation time.
    - On feed request:
      - Read top post IDs from Redis for `X-User-Id`.
      - Call post-service for post details.
      - Call user-service to resolve usernames.
      - Return a clean, paginated feed.
  - Data:
    - Redis sorted sets for each user’s feed.

- **Search Service**
  - Stack: Spring Boot, Spring Data Elasticsearch, Kafka.
  - Responsibilities:
    - Consume `PostCreatedEvent` from Kafka.
    - Index posts into Elasticsearch index `posts_index`.
    - Provide APIs:
      - `GET /api/search?query=...`
      - `GET /api/search/hashtag/{tag}`
    - Search on caption text and hashtags.
  - Data:
    - `PostDocument` documents in Elasticsearch.

### Infrastructure Components

- **MySQL**
  - Primary relational database for user and post data.
  - Hosted in Docker as `social-media-mysql`.

- **Neo4j**
  - Graph database for social relations.
  - Hosted in Docker as `social-media-neo4j`.

- **Redis**
  - Used by:
    - user-service (caching / session-style usage).
    - feed-service (storing feeds as sorted sets).

- **Kafka + Zookeeper**
  - Event bus for post-related events.
  - Topics:
    - `post_created` – published by post-service and consumed by feed-service and search-service.

- **Elasticsearch**
  - Search index backing the search-service.

---

## Observability

- **Spring Boot Actuator**
  - Enabled in api-gateway, post-service, graph-service, feed-service, search-service and user-service.
  - Provides health, info and basic metrics endpoints under `/actuator/**`.
  - Can be scraped by external monitoring tools (Prometheus, Grafana, etc.) if needed.

- **Prometheus**
   - Metrics endpoint exposed via Actuator at `/actuator/prometheus` for each service.
   - Prometheus can scrape `http://<service-host>:<port>/actuator/prometheus`.
   - This enables central dashboards and alerting for request rate, latency and errors.

---

## Project Flow Diagram

```text
                          +-----------------------+
                          |    Client (App/Web)   |
                          +-----------+-----------+
                                      |
                           Multipart File (media +
                           caption + hashtags)
                                      |
                                      v
                 +--------------------+--------------------+
                 |    CDN / Edge (optional in prod)        |
                 +--------------------+--------------------+
                                      |
                                      v
                 +--------------------+--------------------+
                 |      API Gateway (Spring Cloud)         |
                 |      - Auth / JWT                       |
                 |      - Route to microservices           |
                 +--------------------+--------------------+
                                      |
                                      v
                          +----------+----------+
                          |   Post Service      |
                          |   - Store media     |
                          |   - Extract         |
                          |     metadata        |
                          +----------+----------+
                                     / \
                        media (file) /   \  metadata (userId,
                                     v     \ caption, tags,
                              +------+--+   \ mediaUrl...)
                              |  S3 /  |     v
                              |Storage |  +--+----------------+
                              +--------+  |  Postgres / MySQL |
                                          +-------------------+
                                                   |
                                                   | PostCreatedEvent
                                                   v
                                         +---------+---------+
                                         |      Kafka        |
                                         |  topic: post_...  |
                                         +----+---------+----+
                                              |         |
                                              |         |
                       build user feeds       |         | index posts
                                              |         |
                       +------------------+   |   +-----+----------------+
                       |   Feed Service   |   |   |   Search Service     |
                       | - Read followers |   |   | - Build search index |
                       | - Fanout to      |   |   +----------+-----------+
                       |   user feeds     |   |              |
                       +---------+--------+   |              |
                                 |            |              v
                                 v            |       +------+------+
                         +-------+------+     |       |Elasticsearch|
                         |   Redis      |     |       +-------------+
                         | (USER FEED)  |     |
                         +--------------+     |
                                             v
                                     +-------+---------+
                                     |   Neo4j         |
                                     | (Follow graph)  |
                                     +-----------------+
```

### Create Post → User Feed Flow

```text
Client
  |
  | 1. POST /api/posts  (multipart: media + caption + hashtags)
  v
API Gateway
  - Validates JWT
  - Adds X-User-Id
  |
  v
Post Service
  - Save media to S3
  - Save metadata (caption, tags, mediaUrl, userId) to DB
  - Publish PostCreatedEvent
  |
  v
Kafka  (topic: post_created)
  |
  | 2. Event consumed
  v
Feed Service
  - Call Graph Service to get followers of author
  - For each follower + author:
        add postId to Redis sorted set feed:{userId}
        score = createdAt timestamp
  |
  | 3. Client requests feed
  v
API Gateway
  - Validates JWT
  - Adds X-User-Id
  |
  v
Feed Service
  - Read top N postIds from Redis feed:{X-User-Id}
  - Fetch post details from Post Service
  - Fetch usernames from User Service
  - Return ordered feed to client
```

---

## Data Flow & Architecture Overview

### 1. Authentication and Gateway

1. Client calls `POST /api/users/register` → user-service creates a new user.
2. Client calls `POST /api/users/login` → user-service verifies credentials and returns a JWT.
3. Client calls any protected endpoint via API gateway with `Authorization: Bearer <JWT>`.
4. Gateway:
   - Validates the token.
   - Extracts `userId` claim.
   - Adds `X-User-Id` header to the request.
   - Routes to the appropriate microservice.

### 2. Follow / Unfollow (Graph Service)

1. Client calls `POST /api/graph/follow/{targetUserId}` via gateway.
2. Gateway resolves `X-User-Id` (= followerId).
3. graph-service writes `(:User {userId=followerId})-[:FOLLOWS]->(:User {userId=targetUserId})` into Neo4j.
4. Followers and following lists are fetched using paginated REST APIs.

### 3. Post Creation Flow

1. Client uploads a post:
   - `POST /api/posts` with multipart file, caption, hashtags.
2. Gateway injects `X-User-Id` (= author id).
3. post-service:
   - Stores media file via `LocalMediaStorageService`.
   - Persists post row in MySQL.
   - Resolves or creates hashtags and link rows.
   - Emits `PostCreatedEvent` to Kafka topic `post_created`.

### 4. Feed Generation (Hybrid Strategy)

1. feed-service consumes `PostCreatedEvent`.
2. For each event:
   - Calls graph-service to get followers of the author.
   - Builds the set of all target users (followers + author).
   - For each user:
     - Adds the `postId` into Redis sorted set `feed:{userId}` with score = `createdAt` (epoch millis).
3. When a client calls `GET /api/feed`:
   - Gateway injects `X-User-Id`.
   - feed-service reads the top N post IDs from `feed:{X-User-Id}` (reverse range by score).
   - Fetches post details from post-service for each ID.
   - Fetches usernames from user-service.
   - Returns a paginated list of feed items:
     - `postId`, `caption`, `mediaUrl`, `username`.

### 5. Search Indexing & Query

1. search-service also consumes `PostCreatedEvent`.
2. It transforms the event into a `PostDocument` and saves it to Elasticsearch.
3. Clients can search posts by:
   - Text: `GET /api/search?query=...`
   - Hashtag: `GET /api/search/hashtag/{tag}`

---

## Running the System

### Using Docker Compose (recommended)

From the project root:

```bash
docker compose up -d
```

This starts:

- Infrastructure:
  - `mysql`, `postgres`, `redis`, `kafka`, `zookeeper`, `neo4j`, `elasticsearch`
- Microservices:
  - `user-service` (8081)
  - `post-service` (8082)
  - `graph-service` (8083)
  - `feed-service` (8084)
  - `search-service` (8085)
  - `api-gateway` (8080)

Access points:

- Gateway: `http://localhost:8080`
- Swagger UIs:
  - user-service: `http://localhost:8081/swagger-ui.html`
  - post-service: `http://localhost:8082/swagger-ui.html`
  - graph-service: `http://localhost:8083/swagger-ui.html`
  - feed-service: `http://localhost:8084/swagger-ui.html`
  - search-service: `http://localhost:8085/swagger-ui.html`

### Running Individually (local dev)

You can still run each service directly:

```bash
cd user-service      && mvn spring-boot:run
cd post-service      && mvn spring-boot:run
cd graph-service     && mvn spring-boot:run
cd feed-service      && mvn spring-boot:run
cd search-service    && mvn spring-boot:run
cd api-gateway       && mvn spring-boot:run
```

Make sure MySQL, Redis, Neo4j, Kafka and Elasticsearch are running locally (or via Docker) and the corresponding `application.yml` values match.
