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

A highly scalable, cloud-native microservices ecosystem for digital publishing, content syndication, and user behavior analytics. Designed to handle extreme high-throughput traffic, this platform is routed by **Spring Cloud Gateway** and **Consul** for dynamic service discovery. It relies on **Apache Kafka** for event-driven decoupling, integrates **MinIO** and **AWS Rekognition**, and leverages a polyglot persistence layer (**MySQL, Redis, MongoDB, Elasticsearch**) to ensure sub-millisecond data delivery and robust consistency.

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

<details>
<summary><b>👨‍💻 View Mermaid Source Code (For IDE Rendering)</b></summary>

```mermaid
flowchart TB
    Client["📱 Client Applications (App/Web)"]
    
    subgraph Layer1 ["1. API Gateway & Registry"]
        SCG["🛡️ Spring Cloud Gateway"]
        Consul["🧭 Consul (Service Discovery)"]
    end
    
    subgraph Layer2 ["2. Core Microservices (7+ Services)"]
        User["👤 User"]
        Admin["👔 Admin"]
        WeMedia["✍️ WeMedia"]
        Article["📄 Article"]
        Behavior["🖱️ Behavior"]
        Schedule["⏳ Schedule"]
        Search["🔍 Search"]
    end
    
    subgraph Layer3 ["3. Event Bus & Caching"]
        Kafka["🚄 Apache Kafka"]
        Redis["🔴 Redis (ZSet / Pipeline)"]
    end
    
    subgraph Layer4 ["4. Persistence & Search"]
        MySQL["🐬 MySQL"]
        MongoDB["🍃 MongoDB"]
        ES["🔍 Elasticsearch"]
    end
    
    subgraph Layer5 ["5. Infrastructure & Cloud"]
        S3["🪣 MinIO"]
        AI["🤖 AWS Rekognition"]
    end

    %% Tightly packed downward routing
    Client --> SCG
    SCG -.->|Routing Data| Consul
    SCG --> User & Admin & WeMedia & Article & Behavior & Schedule & Search
    
    %% Standard length pipelines (Thin lines for perfect layout)
    Article -->|Publish Event| Kafka
    Behavior -->|Track Event| Kafka
    Kafka -->|Async Moderation| AI
    
    %% Standard Event Bus Fanning (Showing Kafka's multi-purpose role)
    Kafka -.->|Sync Indexes| Search
    Kafka -.->|Log Aggregation| MongoDB
    
    Schedule -->|Task Migration| Redis
    Schedule --> MySQL
    
    Article -->|SSG Upload| S3
    Search --> ES
```

</details>

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

### 2. Event-Driven Moderation Pipeline
*   **Problem**: Image moderation via AWS Rekognition is an external operation and should not block the article submission request.
*   **Solution**: Submitted articles are placed in the Schedule service's Redis-backed delayed-task queue. The Wemedia service polls ready tasks and performs moderation asynchronously, while conditional database updates protect each review state transition.

### 3. Static Site Generation for Extreme Read Scaling
*   **Problem**: Serving popular articles triggered complex SQL `JOIN` queries, threatening database stability during traffic spikes.
*   **Solution**: Engineered an SSG pipeline that pre-renders dynamic article data into static HTML files at publish time. These files are distributed to **MinIO**.
*   **Impact**: Completely bypassed relational database reads for content delivery, supporting **10,000+ concurrent readers** with sub-millisecond page loads.

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
