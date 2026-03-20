# CodeRunner

**A high-performance, distributed online judge system for secure code execution and evaluation.**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-Streams-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Sandboxed-blue.svg)](https://www.docker.com/)

---

## Overview

CodeRunner is a production-ready code execution platform similar to LeetCode, Codeforces, or HackerRank. It provides a scalable architecture for:

- Creating programming problems with custom test cases
- Accepting user code submissions in multiple languages
- Executing code in isolated Docker containers with resource limits
- Evaluating output against expected results using custom judges

---

## System Architecture

```mermaid
flowchart TB
    subgraph Client Layer
        A[Web Client / API Consumer]
    end

    subgraph API Service
        B[coderunner-api]
        B1[Question Controller]
        B2[Submission Controller]
        B3[Question Service]
        B4[Submission Service]
    end

    subgraph Message Queue
        C[(Redis Streams)]
    end

    subgraph Worker Service
        D[coderunner-workers]
        D1[Stream Consumer]
        D2[Semaphore Pool]
        D3[Submission Dispatcher]
        D4[Judge Bootstrap Compiler]
        D5[Language Executors]
    end

    subgraph Execution Layer
        E[Docker Executor]
        F1[judge-cpp Container]
        F2[judge-java Container]
        F3[judge-python Container]
    end

    subgraph Storage
        G[(PostgreSQL)]
        H[File System - Test Cases]
    end

    A -->|REST API| B
    B1 --> B3
    B2 --> B4
    B3 -->|Save Question| G
    B3 -->|Store Test Cases| H
    B4 -->|Save Submission| G
    B4 -->|Push Message| C
    
    C -->|Consume| D1
    D1 -->|Acquire Permit| D2
    D2 -->|Dispatch| D3
    D3 -->|Bootstrap| D4
    D4 -->|Compile Judge| E
    D3 -->|Execute| D5
    D5 -->|Run in Container| E
    E --> F1
    E --> F2
    E --> F3
    
    D3 -->|Update Status| G
    D4 -->|Read Judge Source| H
```

---

## Core Components

### 1. coderunner-api

The REST API gateway that handles all client interactions.

```mermaid
flowchart LR
    subgraph Endpoints
        A[POST /submit]
        B[POST /question/create-question]
        C[GET /question/get/:id]
        D[GET /question/get-stats/:id]
    end

    subgraph Services
        E[SubmissionService]
        F[QuestionService]
    end

    subgraph Validators
        G[SubmissionValidator]
        H[QuestionValidator]
    end

    A --> E
    B --> F
    C --> F
    D --> F
    E --> G
    F --> H
```

#### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/submit` | Submit code for evaluation |
| `POST` | `/question/create-question` | Create a new programming problem |
| `GET` | `/question/get/{id}` | Retrieve question details |
| `GET` | `/question/get-stats/{id}` | Get submission statistics |

#### Submission Flow

```mermaid
sequenceDiagram
    participant Client
    participant API as coderunner-api
    participant DB as PostgreSQL
    participant Redis as Redis Stream

    Client->>API: POST /submit {code, language, questionId}
    API->>API: Validate submission
    API->>DB: Save submission (status: PENDING)
    API->>Redis: XADD submission-stream {submissionId}
    API-->>Client: 202 Accepted {submissionId}
```

---

### 2. coderunner-workers

The worker service that processes submissions asynchronously with controlled concurrency.

#### Semaphore-Based Concurrency Model

The worker uses a **semaphore-controlled thread pool** to manage concurrent executions efficiently:

```mermaid
flowchart TB
    subgraph Stream Consumer Thread
        A[Redis Stream Reader]
        B{Semaphore Available?}
        C[Acquire Permit]
        D[Read Message]
    end

    subgraph Thread Pool
        E[Worker Thread 1]
        F[Worker Thread 2]
        G[Worker Thread N]
    end

    subgraph After Execution
        H[Release Permit]
        I[ACK Message]
    end

    A --> B
    B -->|Yes| C
    C --> D
    D -->|Submit Task| E
    D -->|Submit Task| F
    D -->|Submit Task| G
    B -->|No| B
    E --> H
    F --> H
    G --> H
    H --> I
```

**How it works:**

1. A configurable `Semaphore` with N permits controls max concurrent executions
2. The reader thread **blocks** on `semaphore.acquire()` when all workers are busy
3. Each worker **releases** the permit after completing execution
4. This prevents overwhelming the system while maximizing throughput

```java
// Configuration
@Value("${spring.execution.maximum-thread-count:5}")
private int threadCount;

// Initialization
availableWorkers = new Semaphore(threadCount);
executor = new ThreadPoolExecutor(threadCount, threadCount, ...);

// Read loop
availableWorkers.acquire();  // Blocks if all workers busy
// ... read from stream ...
executor.submit(() -> processSubmission(...));

// After processing
availableWorkers.release();  // Allow next submission
```

---

### 3. Judge Bootstrap Compiler

A **lazy compilation system** that compiles custom judges on-demand with thread-safe concurrency handling.

```mermaid
flowchart TB
    A[New Submission Arrives]
    B{Judge Binary Exists?}
    C[Fast Path: Skip Compilation]
    D{Already Compiling?}
    E[Slow Path: Compile Judge]
    F[Wait for Binary]
    G[Add to Compiling Set]
    H[Invoke Docker Compiler]
    I[Remove from Set]
    J[Continue to Execution]

    A --> B
    B -->|Yes| C
    B -->|No| D
    D -->|No| G
    G --> E
    E --> H
    H --> I
    I --> J
    D -->|Yes| F
    F --> J
    C --> J
```

**Key Features:**

- **Lazy Compilation**: Judge binaries are compiled only on first submission for a question
- **Thread-Safe Locking**: Uses `ConcurrentHashMap.newKeySet()` to prevent duplicate compilations
- **Wait Mechanism**: Concurrent submissions wait for the first compilation to complete
- **Binary Caching**: Once compiled, the judge binary is reused for all future submissions

```java
// Thread-safe compilation tracking
private final Set<String> compilingQuestions = ConcurrentHashMap.newKeySet();

if (compilingQuestions.add(questionId)) {
    // First thread: compile the judge
    dockerExecutor.compileJudge(testcasesPath);
    compilingQuestions.remove(questionId);
} else {
    // Other threads: wait for binary to appear
    waitForJudgeBinary(judgeBinary);
}
```

---

### 4. Language Executors

Pluggable execution engines for different programming languages.

```mermaid
classDiagram
    class CodeExecutor {
        <<interface>>
        +getLanguage() String
        +execute(SubmissionModel) boolean
    }
    
    class CppExecutor {
        -DockerExecutor dockerExecutor
        +getLanguage() String
        +execute(SubmissionModel) boolean
    }
    
    class JavaExecutor {
        -DockerExecutor dockerExecutor
        +getLanguage() String
        +execute(SubmissionModel) boolean
    }
    
    class PythonExecutor {
        -DockerExecutor dockerExecutor
        +getLanguage() String
        +execute(SubmissionModel) boolean
    }
    
    CodeExecutor <|.. CppExecutor
    CodeExecutor <|.. JavaExecutor
    CodeExecutor <|.. PythonExecutor
```

#### Execution Pipeline

```mermaid
sequenceDiagram
    participant Dispatcher
    participant Executor as Language Executor
    participant FS as File System
    participant Docker as Docker Executor
    participant Container as Docker Container

    Dispatcher->>Executor: execute(submission)
    Executor->>FS: Create temp workspace
    Executor->>FS: Write user code to file
    Executor->>FS: Generate run.sh script
    Executor->>Docker: dockerExecute(workspace, testcases, image)
    Docker->>Container: Create container with mounts
    Docker->>Container: Start container
    Container->>Container: Execute run.sh
    Container-->>Docker: Exit code + logs
    Docker->>Docker: Remove container
    Docker-->>Executor: Result
    Executor->>FS: Cleanup workspace
    Executor-->>Dispatcher: Success/Failure
```

---

### 5. Docker Execution Environment

Secure, isolated containers for code execution.

```mermaid
flowchart LR
    subgraph Host Machine
        A[Temp Workspace]
        B[Test Cases Directory]
    end

    subgraph Docker Container
        C["/workspace (rw)"]
        D["/workspace/testcase (ro)"]
        E[run.sh]
        F[user_code.cpp]
        G[judge_program]
    end

    A -->|Mount RW| C
    B -->|Mount RO| D
    C --> E
    C --> F
    D --> G
```

#### Container Images

| Image | Base | Purpose |
|-------|------|---------|
| `judge-cpp` | `debian:bookworm-slim` | C/C++ compilation and execution |
| `judge-java` | `eclipse-temurin:21-jdk` | Java compilation and execution |
| `judge-python` | `python:3.11-slim` | Python execution |

**Security Features:**

- Non-root user (`runner`) inside containers
- Read-only mount for test cases
- No network access (can be configured)
- Resource limits via `timeout` and `ulimit`

---

## Execution Script

The `run.sh` script orchestrates compilation, execution, and judging:

```mermaid
flowchart TB
    A[Start] --> B[Compile User Code]
    B -->|Failure| C[COMPILATION_ERROR]
    B -->|Success| D[For Each Test Case]
    D --> E[Run with Limits]
    E -->|Timeout| F[TIME_LIMIT_EXCEEDED]
    E -->|Signal 139/134| G[MEMORY_LIMIT_EXCEEDED]
    E -->|Non-zero Exit| H[RUNTIME_ERROR]
    E -->|Success| I[Run Judge]
    I -->|Failure| J[WRONG_ANSWER]
    I -->|Success| K{More Tests?}
    K -->|Yes| D
    K -->|No| L[ACCEPTED]
```

**Resource Enforcement:**

```bash
# Time limit via timeout command
timeout $TIME_LIMIT ./user_program < input.txt

# Memory limit via ulimit
ulimit -v $MEMORY_LIMIT
```

---

## Data Models

```mermaid
erDiagram
    USER ||--o{ SUBMISSION : creates
    QUESTION ||--o{ SUBMISSION : has
    LANGUAGE ||--o{ SUBMISSION : uses

    USER {
        string id PK
        string username
        string email
    }

    QUESTION {
        string questionId PK
        string title
        string markdown
        string testcasesPath
        int testcasesCount
        int submissions
        int accepted
        int timeLimit
        int memoryLimit
        enum difficulty
        list topics
    }

    SUBMISSION {
        long submissionId PK
        string code
        enum status
        int timeTaken
        int memoryTaken
        long submittedAt
        string judgeMessage
    }

    LANGUAGE {
        int languageId PK
        string languageName
        double languageMultiplier
    }
```

### Submission Status Values

| Status | Description |
|--------|-------------|
| `PENDING` | Awaiting processing |
| `RUNNING` | Currently executing |
| `ACCEPTED` | All test cases passed |
| `WRONG_ANSWER` | Output mismatch |
| `TLE` | Time Limit Exceeded |
| `MLE` | Memory Limit Exceeded |
| `RUNTIME_ERROR` | Crash during execution |
| `COMPILATION_ERROR` | Failed to compile |

---

## Custom Judge System

Each question can have a custom judge program for flexible output validation.

```mermaid
flowchart LR
    A[Test Input] --> B[User Program]
    B --> C[User Output]
    C --> D[Judge Program]
    A --> D
    D -->|Exit 0| E[Correct]
    D -->|Exit 1| F[Wrong Answer]
```

**Example Judge (C++):**

```cpp
#include <bits/stdc++.h>
using namespace std;

int main(int argc, char* argv[]) {
    string testcasePath = argv[1];
    ifstream testcase(testcasePath);
    
    string expected, userOutput;
    getline(testcase, expected);
    getline(cin, userOutput);
    
    return (expected == userOutput) ? 0 : 1;
}
```

This allows for:
- Floating-point tolerance comparisons
- Multiple valid answers
- Partial scoring
- Custom output formats

---

## Technology Stack

```mermaid
mindmap
    root((CodeRunner))
        Backend
            Spring Boot 3.x
            Java 21
            Lombok
            JPA/Hibernate
        Database
            PostgreSQL
            Redis Streams
        Infrastructure
            Docker
            docker-java client
        Build
            Maven
```

---

## Project Structure

```
coderunner/
├── coderunner-api/
│   └── src/main/java/com/varshith/coderunner/
│       ├── controllers/        # REST endpoints
│       │   ├── QuestionController.java
│       │   └── SubmissionController.java
│       ├── service/            # Business logic
│       │   ├── QuestionService.java
│       │   └── SubmissionService.java
│       ├── repository/         # Data access layer
│       ├── models/             # JPA entities
│       ├── dtos/               # Request/Response objects
│       ├── helpers/            # Validators, utilities
│       └── config/             # Redis, Database config
│
├── coderunner-workers/
│   └── src/main/java/com/varshith/coderunner_workers/
│       ├── consumer/           # Redis stream consumer
│       │   └── StreamConsumer.java
│       ├── dispatcher/         # Routing & bootstrapping
│       │   ├── SubmissionDispatcher.java
│       │   └── JudgeBootstrapCompiler.java
│       ├── executors/          # Language-specific execution
│       │   ├── CodeExecutor.java (interface)
│       │   ├── CppExecutor.java
│       │   └── DockerExecutor.java
│       ├── models/             # Shared JPA entities
│       └── config/             # Docker, Redis config
│
├── judge-images/               # Docker image definitions
│   ├── cpp/Dockerfile
│   ├── java/Dockerfile
│   └── python/Dockerfile
│
└── questions/                  # Test case storage
    └── {questionId}/
        └── testcases/
            ├── input/
            ├── judge.cpp
            └── judge_program (compiled)
```

---

## Configuration

### API Service (`application.properties`)

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/coderunner
spring.datasource.username=...
spring.datasource.password=...

# Test cases storage
spring.testcases.base_path=/path/to/questions/
```

### Worker Service (`application.properties`)

```properties
# Redis Stream
spring.stream.stream_name=submission-stream
spring.stream.consumer_name=coderunner-workers

# Concurrency
spring.execution.maximum-thread-count=5
```

---

## Extending the System

### Adding a New Language

1. **Create the Executor:**

```java
@Component
public class RustExecutor implements CodeExecutor {
    
    public String getLanguage() { 
        return "rust"; 
    }
    
    public boolean execute(SubmissionModel submission) {
        // Setup workspace, generate run.sh, invoke DockerExecutor
    }
}
```

2. **Create the Docker Image:**

```dockerfile
FROM rust:1.75-slim
WORKDIR /workspace
RUN useradd -m runner
USER runner
```

3. **Create the Run Script:**

```bash
#!/bin/bash
rustc user_code.rs -o user_program
# ... execution and judging logic
```

4. **Add to Database:**

```sql
INSERT INTO language_model (language_id, language_name, language_multiplier) 
VALUES (4, 'rust', 1.0);
```

---

## Scaling

```mermaid
flowchart TB
    subgraph Load Balancer
        LB[Nginx / HAProxy]
    end

    subgraph API Instances
        A1[coderunner-api #1]
        A2[coderunner-api #2]
    end

    subgraph Redis Cluster
        R[(Redis Primary)]
    end

    subgraph Worker Pool
        W1[worker #1]
        W2[worker #2]
        W3[worker #3]
    end

    subgraph Database
        DB[(PostgreSQL)]
    end

    LB --> A1
    LB --> A2
    A1 --> R
    A2 --> R
    A1 --> DB
    A2 --> DB
    R --> W1
    R --> W2
    R --> W3
    W1 --> DB
    W2 --> DB
    W3 --> DB
```

**Horizontal Scaling:**

- **API**: Stateless, can run multiple instances behind a load balancer
- **Workers**: Redis consumer groups automatically distribute messages across workers
- **Database**: Use read replicas for query-heavy operations

---

## License

MIT License - See [LICENSE](LICENSE) for details.
