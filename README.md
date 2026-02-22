# 🚀 Social Media Backend – Microservices Architecture

This project is a scalable, event-driven social media backend system inspired by Instagram.

It demonstrates how to design and implement a distributed system using:

- Java 21
- Spring Boot 3
- MySQL (currently for user-service)
- Apache Kafka (planned)
- PostgreSQL / Neo4j / Elasticsearch (planned services)
- Redis
- Docker

The system is designed to handle:
- Media uploads
- Feed generation
- Search indexing
- Social graph relationships
- High-throughput event processing

The architecture follows modern backend engineering principles including:
- Microservices architecture
- Event-driven communication
- Horizontal scalability
- Asynchronous processing
- Distributed data management

## Services
- API Gateway
- User Service
- Post Service
- Feed Service
- Search Service
- Graph Service

## Tech Stack (Current)
- Java 21
- Spring Boot 3
- MySQL (user-service)
- Apache Kafka (planned)
- Neo4j (planned)
- Elasticsearch (planned)
- Redis
- Docker

## Run
Each service can be started individually using:
mvn spring-boot:run

