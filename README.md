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
![AWS](https://img.shields.io/badge/AWS-Rekognition-FF9900.svg)
![MinIO](https://img.shields.io/badge/MinIO-Object%20Storage-C7202C.svg)

A cloud-native microservices project for digital publishing, content distribution, and user behavior processing. The platform uses **Spring Cloud Gateway** and **Consul** for routing and service discovery, **Apache Kafka** for event-driven integration, **MinIO** and **AWS Rekognition** for media processing, and a polyglot persistence layer built on **MySQL, Redis, MongoDB, and Elasticsearch**.

---

## 🛠️ Tech Stack & Key Metrics

*   **Backend Ecosystem**: Java 17, Spring Boot 3.2, Spring Data JPA, Spring Cloud Gateway, Consul (Service Discovery).
*   **Data & Search Layer**: MySQL (Primary), MongoDB (Behavior Logs), Elasticsearch (Full-Text Search).
*   **Streaming & Caching**: Apache Kafka, Redis (ZSet, List, Pipeline).
*   **Infrastructure & Cloud**: MinIO (Static Hosting), AWS Rekognition (AI Moderation).
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


### 1. High-Concurrency Task Scheduler (Redis ZSet + Pipeline)
Built a hybrid MySQL-Redis scheduling engine. Future tasks are pushed to a Redis `ZSet` (scored by execution time). A distributed cron job fetches expired tasks and migrates them to a Ready `List`. To eliminate network RTT overhead during this migration, **Redis Pipeline** batching was introduced.

```mermaid
flowchart TB
    %% 1. Submission Flow (Sequential)
    Submit(["📥 Submit Task"]) --> AddTask["⚙️ addTask"]
    AddTask -->|"1. Persist First"| DB[("🗄️ MySQL (taskinfo)")]
    DB -->|"2. Evaluate Time"| Router{"Execution Time?"}
    
    Router -.->|"Over 5m"| Hold["Hold in DB"]
    Router -->|"Under 5m"| ZSet[("⏳ Redis ZSet (future)")]
    Router -->|"Immediate"| List[("🚀 Redis List (topic)")]

    %% 2. The 1-min Pipeline Migration (ZSet -> List) - HIGHLIGHTED
    ZSet -->|"zRangeByScore"| Refresh["🔥 refresh() Cron"]
    Refresh -->|"zRem + rPush"| List

    %% 3. The 5-min DB Sync & Recovery
    DB -.->|"Query tasks"| Reload["⚙️ reloadData() Cron"]
    Reload -.->|"Rebuild Cache"| ZSet
    Reload -.->|"Rebuild Cache"| List

    %% 4. Consumption Flow
    List -->|"lRightPop"| Poll["⚙️ poll()"]
    Poll -->|"Return"| Worker(["🚀 Execute Logic"])
    Poll -->|"Set EXECUTED"| DB

    %% 5. Cancellation Flow
    Abort(["🚫 Abort Request"]) -.-> Cancel["⚙️ cancelTask()"]
    Cancel -.->|"Set CANCELLED"| DB
    Cancel -.->|"zRemove"| ZSet
    Cancel -.->|"lRemove"| List

    %% UI Styling for Highlights
    style Refresh fill:#ffecb3,stroke:#ff8f00,stroke-width:4px,color:#d84315
    style Router fill:#fff3e0,stroke:#ff8f00,stroke-width:2px
    style ZSet fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    style List fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    style DB fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
```

### 2. Event-Driven Messaging with Kafka
Kafka decouples cross-service updates from the main request flow. Article publication events update the Elasticsearch index, article up/down events synchronize publication state, and follow events update behavior data. This keeps producers independent from downstream consumers and allows these updates to be processed asynchronously.

### 3. Static Article Generation
When an article is published, the Article service renders its content into a static HTML page with Freemarker and stores the generated file in MinIO. Readers can retrieve the pre-rendered page without rebuilding the article view from multiple database records on every request. After generation, the service also publishes a Kafka event to update the Elasticsearch index.

---

## 🚀 Quick Start

### Prerequisites
- JDK 17 & Maven 3.8+
- Docker & Docker Compose
- Consul (Port 8500)
- AWS credentials with `rekognition:DetectModerationLabels` permission for real image moderation

Configure credentials locally with `aws configure`. The Wemedia service uses `us-west-2` by default; set `AWS_REGION` to use another region. Credentials are loaded through the AWS SDK default credential chain and must not be committed to this repository.

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
