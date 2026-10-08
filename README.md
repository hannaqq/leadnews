# 🚀 Distributed Microservices Content Platform (LeadNews)

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.15-brightgreen.svg)
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

A cloud-native microservices project for open news publishing, content distribution, and user behavior processing. Readers can apply to become creators; approved applicants receive a Wemedia account for submitting and scheduling articles. The platform uses **Spring Cloud Gateway** and **Consul** for routing and service discovery, **Apache Kafka** for event-driven integration, **MinIO** and **AWS Rekognition** for media processing, and a polyglot persistence layer built on **MySQL, Redis, MongoDB, and Elasticsearch**.

---

## 🛠️ Tech Stack & Key Metrics

*   **Backend Ecosystem**: Java 17, Spring Boot 3.5, Spring Data JPA, Spring Cloud Gateway, Consul (Service Discovery).
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
Kafka decouples cross-service article updates from the main request flow. Article publication events update the Elasticsearch index, while article up/down events synchronize publication state. User reactions and creator follows are synchronous, transactional operations owned directly by the Behavior service.

### 3. Static Article Generation
When an article is published, the Article service renders its content into a static HTML page with Freemarker and stores the generated file in MinIO. Readers can retrieve the pre-rendered page without rebuilding the article view from multiple database records on every request. After generation, the service also publishes a Kafka event to update the Elasticsearch index.

---

## 🚀 Backend Quick Start

This repository contains the backend services only. The frontend build artifacts and machine-specific Nginx configuration are not included.

### Prerequisites

- JDK 17 and Maven 3.8+
- Docker and Docker Compose
- MySQL with the required LeadNews schemas and tables
- `MYSQL_PASSWORD` set in the local environment; `MYSQL_USERNAME` defaults to `root`

AWS credentials with `rekognition:DetectModerationLabels` permission are optional and are required only for real image moderation. Configure them locally with `aws configure`. The Wemedia service uses `us-west-2` by default; set `AWS_REGION` to use another region. Credentials are loaded through the AWS SDK default credential chain and must not be committed to this repository.

### Start Infrastructure

From the repository root, start Consul, Redis, ZooKeeper, Kafka, Elasticsearch, MongoDB, and MinIO:

```bash
docker compose up -d
```

Verify the containers before starting the applications:

```bash
docker compose ps
```

MySQL is not included in the Compose file and must be started separately. The files under `scripts/database` are incremental migrations, not a complete baseline schema.

### Start Applications

After the infrastructure is ready, start the seven business services:

1. `leadnews-schedule` (51701)
2. `leadnews-user` (51801)
3. `leadnews-article` (51802)
4. `leadnews-wemedia` (51803)
5. `leadnews-search` (51804)
6. `leadnews-behavior` (51805)
7. `leadnews-admin` (51806)

Then start the three gateways:

1. `leadnews-app-gateway` (51601)
2. `leadnews-wemedia-gateway` (51602)
3. `leadnews-admin-gateway` (51603)

The applications register with Consul at `http://localhost:8500`. Starting a gateway before its downstream services is possible, but requests will fail until the required services have registered and become healthy.

### Optional Frontend Proxy

When using a separately obtained frontend build, Nginx can serve its static assets and reverse-proxy API requests to the three gateways listed above. The frontend assets and Nginx configuration are intentionally excluded because they are external, machine-specific resources. The backend can be exercised directly through the gateway ports without Nginx.
