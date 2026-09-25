# LeadNews – Cloud-Native Microservices Content Platform

A robust, highly scalable Spring Cloud microservices platform tailored for content publishing, Trust & Safety (T&S) moderation, delayed task scheduling, and real-time search. Designed with modern US enterprise architectural patterns, focusing on security, data integrity, and containerized deployment.

## 🚀 Engineering Highlights

- **Cloud-Native Infrastructure & Service Discovery**
  Migrated from legacy registries to **HashiCorp Consul** for service discovery and configuration management. The entire infrastructure stack (Consul, Redis, Kafka, Zookeeper, Elasticsearch, MongoDB, MinIO) is fully containerized and orchestrated via `docker-compose` for rapid, reproducible local development.
- **Trust & Safety (T&S) Content Moderation Pipeline**
  Engineered an asynchronous, highly decoupled content moderation pipeline for User-Generated Content (UGC). Replaced legacy, CPU-intensive local OCR engines (Tesseract) with a Cloud-Native architecture integrating the **AWS Rekognition Java SDK** to detect explicit images and extract embedded text for bad-word filtering, drastically reducing manual review overhead.
- **Data Integrity & API Type Purity**
  Identified and resolved a severe JavaScript 53-bit integer precision loss vulnerability affecting 19-digit Snowflake IDs (e.g., `articleId`, `authorId`). Instead of global interceptors that violate RESTful JSON type semantics, implemented a targeted AST-aware declarative serialization strategy (`@JsonSerialize`) to preserve backend type safety while guaranteeing flawless frontend interactions.
- **Zero-Trust API Gateway & Context Propagation**
  Hardened the Spring Cloud Gateway JWT authentication filters. Prevented HTTP Header Injection attacks by strictly sanitizing downstream headers (`httpHeaders.set("userId")`), ensuring secure, tamper-proof identity propagation across the microservice mesh.
- **State-Machine Concurrency Control (Delayed Tasks)**
  Resolved critical race conditions in the `leadnews-schedule` delayed task queue using a state-machine conditional update (`SCHEDULED → EXECUTED` or `CANCELLED`). Utilized SQL-style guards to prevent execute/cancel concurrent overwrites.

## 🛠 Tech Stack

- **Backend Foundation:** Java 11, Spring Boot 2.6, Spring Cloud 2021
- **Service Mesh & Config:** HashiCorp Consul, Spring Cloud Gateway, OpenFeign
- **Storage & Databases:** MySQL 8, MongoDB (Search History)
- **Object Storage:** AWS S3 API compatibility via MinIO
- **Message Broker & Async:** Apache Kafka, Redis (ZSet + List for delayed queues)
- **Search Engine:** Elasticsearch 7.x
- **AI & Moderation:** AWS Rekognition (Java SDK)

## 📦 Modules

| Module | Description |
|---|---|
| `leadnews-gateway` | Global entry point, Auth Filters, CORS, and Routing (`app`, `wemedia`, `admin`) |
| `leadnews-service/leadnews-user` | User identity, profiles, and authentication |
| `leadnews-service/leadnews-article` | Article management, storage, and static HTML generation |
| `leadnews-service/leadnews-wemedia` | Creator platform, content submission, AWS Rekognition T&S moderation |
| `leadnews-service/leadnews-schedule` | High-performance delayed task scheduling (Redis + State Machine) |
| `leadnews-service/leadnews-search` | Elasticsearch indexing and Mongo-backed user search history |
| `leadnews-service/leadnews-behavior` | User behavior tracking (likes, follows, reads) |

## 🚦 Quick Start

### 1. Start Infrastructure (Docker Compose)
All necessary infrastructure dependencies are bundled in the root directory.
```bash
docker-compose up -d
```
*This will spin up Consul (port 8500), Redis, Kafka, Zookeeper, Elasticsearch, MongoDB, and MinIO.*

### 2. Configure Local Database
Ensure you have a local MySQL instance running on `localhost:3306`. (MySQL is excluded from docker-compose to prevent conflicts with existing local dev environments).

### 3. Build & Run
Compile the project skipping tests:
```bash
mvn clean install -DskipTests
```
Launch the microservices via your IDE. They will automatically register with the local Consul cluster at `http://localhost:8500`.

## 🧪 Testing Concurrency (Schedule Service)

**1) JUnit Concurrency Test (DB assertions)**
```bash
mvn -pl leadnews-service/leadnews-schedule -Dtest=TaskRaceConditionStressTest test
```

**2) Python HTTP Race Script**
Start `leadnews-schedule` (port `51701`), then run:
```bash
python scripts/task_race_stress.py --tasks 200 --workers 8
```
Expect: `cancel_success + poll_success == tasks` (each task is finalized exactly once).
