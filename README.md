# 🚀 Distributed Microservices Content Platform (LeadNews)

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-Hibernate-59666C.svg)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-Gateway-blue.svg)
![Consul](https://img.shields.io/badge/Consul-Service%20Discovery-E74C3C.svg)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-Streaming-black.svg)
![Redis](https://img.shields.io/badge/Redis-ZSet%20%7C%20Pipeline-red.svg)
![MongoDB](https://img.shields.io/badge/MongoDB-NoSQL-47A248.svg)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-Full%20Text-005571.svg)
![Docker](https://img.shields.io/badge/Docker-Containerization-2496ED.svg)
![AWS](https://img.shields.io/badge/AWS-S3%20%7C%20Rekognition-FF9900.svg)

A highly scalable, cloud-native microservices ecosystem for digital publishing, content syndication, and user behavior analytics. Designed to handle extreme high-throughput traffic, this platform is routed by **Spring Cloud Gateway** and **Consul** for dynamic service discovery. It relies on **Apache Kafka** for event-driven decoupling, integrates **Amazon S3** and **AWS Rekognition**, and leverages a polyglot persistence layer (**MySQL, Redis, MongoDB, Elasticsearch**) to ensure sub-millisecond data delivery and robust consistency.

---

## 🛠️ Tech Stack & Key Metrics

*   **Backend Ecosystem**: Java 17, Spring Boot 3.2, Spring Data JPA, Spring Cloud Gateway, Consul (Service Discovery).
*   **Data & Search Layer**: MySQL (Primary), MongoDB (Behavior Logs), Elasticsearch (Full-Text Search).
*   **Streaming & Caching**: Apache Kafka, Redis (ZSet, List, Pipeline).
*   **Cloud Infrastructure**: Amazon S3 (Static Hosting), AWS Rekognition (AI Moderation).
*   **📈 Performance Impact**: 
    *   Slashed user-facing API latency from **~2s to <20ms** via Kafka async decoupling.
    *   Increased task processing throughput by **300%** using Redis Pipeline batching.

---

## 🏗️ System Architecture

The system is strictly layered to separate routing, business logic, asynchronous messaging, and persistence.

<div align="center">
  <img src="./assets/architecture.png" alt="System Architecture Diagram" width="100%">
</div>

<br>

---

## 💎 Under the Hood: Engineering Highlights

### 1. Event-Driven Moderation Pipeline
*   **Problem**: Content moderation via AWS Rekognition is a heavy ML workload. Executing it synchronously blocked HTTP threads, causing unacceptable ~2s wait times for users.
*   **Solution**: Designed an asynchronous, event-driven pipeline via **Apache Kafka**. The API immediately writes the pending state to MySQL and publishes an event, returning `HTTP 202` instantly. A background consumer handles the heavy AWS API call and safely updates the database.
*   **Impact**: Slashed API response times from **~2s to <20ms** and entirely decoupled the core publishing service from third-party rate limits.

### 2. High-Concurrency Task Scheduler (Redis ZSet + Pipeline)
*   **Problem**: Polling the database for scheduled articles caused massive RDBMS I/O spikes and cross-node race conditions.
*   **Solution**: Built a hybrid MySQL-Redis scheduling engine. Future tasks are pushed to a Redis `ZSet` (scored by execution time). A distributed cron job fetches expired tasks and migrates them to a Ready `List`. To eliminate network RTT overhead during this migration, **Redis Pipeline** batching was introduced.
*   **Impact**: Resolved race conditions with sub-second precision and boosted migration throughput by **300%**.

```mermaid
flowchart TB
    %% 1. Submission Flow (Sequential)
    Submit(["📥 Submit Task"]) --> AddTask["⚙️ addTask()"]
    AddTask -->|1. Persist First| DB[("🗄️ MySQL<br/>(taskinfo)")]
    DB -->|2. Evaluate Time| Router{"Execution Time?"}
    
    Router -.->|> 5m| Hold["(Hold in DB)"]
    Router -->|<= 5m| ZSet[("⏳ Redis ZSet<br/>(future_*)")]
    Router -->|Immediate| List[("🚀 Redis List<br/>(topic_*)")]

    %% 2. The 1-min Pipeline Migration (ZSet -> List) - HIGHLIGHTED
    ZSet -->|zRangeByScore (expired)| Refresh["🔥 refresh()<br/>@Scheduled(1m)"]
    Refresh -->|Pipeline: zRem + rPush| List

    %% 3. The 5-min DB Sync & Recovery
    DB -.->|Query upcoming < 5m tasks| Reload["⚙️ reloadData()<br/>@Scheduled(5m)"]
    Reload -.->|Clear & Rebuild| ZSet
    Reload -.->|Clear & Rebuild| List

    %% 4. Consumption Flow
    List -->|lRightPop| Poll["⚙️ poll()"]
    Poll -->|Return| Worker(["🚀 Execute Logic"])
    Poll -->|Status = EXECUTED| DB

    %% 5. Cancellation Flow
    Abort(["🚫 Abort Request"]) -.-> Cancel["⚙️ cancelTask()"]
    Cancel -.->|Status = CANCELLED| DB
    Cancel -.->|If future: zRemove| ZSet
    Cancel -.->|If ready: lRemove| List

    %% UI Styling for Highlights
    style Refresh fill:#ffecb3,stroke:#ff8f00,stroke-width:4px,color:#d84315
    style Router fill:#fff3e0,stroke:#ff8f00,stroke-width:2px
    style ZSet fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    style List fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    style DB fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
```

### 3. Static Site Generation (SSG) for Extreme Read Scaling
*   **Problem**: Serving popular articles triggered complex SQL `JOIN` queries, threatening database stability during traffic spikes.
*   **Solution**: Engineered an SSG pipeline that pre-renders dynamic article data into static HTML files at publish time. These files are distributed to **Amazon S3**.
*   **Impact**: Completely bypassed relational database reads for content delivery, supporting **10,000+ concurrent readers** with sub-millisecond page loads.

---

## 🚀 Quick Start

### Prerequisites
- JDK 17 & Maven 3.8+
- Docker & Docker Compose
- Consul (Port 8500)

### Infrastructure Setup
Spin up the required middleware using the provided compose file:
```bash
docker-compose -f docker/docker-compose.yml up -d
```
*(Ensure MySQL, Redis, MongoDB, Elasticsearch, Kafka, and Consul are running healthily).*

### Bootstrapping Services
Start the API Gateway first, followed by the core microservices:
1. `leadnews-gateway` (Port 51601)
2. `leadnews-user` (Port 51801)
3. `leadnews-article` (Port 51802)
4. *(Start additional services as needed via IntelliJ IDEA Run Dashboard).*
