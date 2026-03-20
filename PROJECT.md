# CodeRunner - Distributed Code Execution System

A scalable online judge system for executing and evaluating user-submitted code against test cases, similar to platforms like LeetCode or Codeforces.

---

## Overview

CodeRunner is a distributed system consisting of two main Spring Boot services that work together to provide secure, sandboxed code execution:

| Component | Purpose |
|-----------|---------|
| **coderunner-api** | REST API for questions and submissions |
| **coderunner-workers** | Async workers that execute code in Docker containers |

---

## Architecture

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│                 │      │                 │      │                 │
│   Client/User   │─────▶│  coderunner-api │─────▶│  Redis Stream   │
│                 │      │                 │      │                 │
└─────────────────┘      └─────────────────┘      └────────┬────────┘
                                                           │
                                                           ▼
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│                 │      │                 │      │                 │
│    Database     │◀─────│ coderunner-     │◀─────│ Stream Consumer │
│   (PostgreSQL)  │      │   workers       │      │                 │
└─────────────────┘      └─────────────────┘      └─────────────────┘
                                │
                                ▼
                         ┌─────────────────┐
                         │                 │
                         │ Docker Container│
                         │  (Sandboxed)    │
                         └─────────────────┘
```

---

## Component Details

### 1. coderunner-api

The API service handles all external requests and manages the flow of data.

#### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/question/create-question` | Create a new coding problem |
| `GET` | `/question/get/{id}` | Retrieve question details |
| `GET` | `/question/get-stats/{id}` | Get submission statistics |
| `POST` | `/submit` | Submit code for evaluation |

#### Key Features

**Question Management**
- Create questions with title, description (markdown), difficulty, time/memory limits
- Upload test cases as a ZIP file (extracted and stored on filesystem)
- Track submission count and acceptance rate per question

**Submission Processing**
1. Validate the submission (user, question, language exist)
2. Save submission to database with `PENDING` status
3. Push submission ID to Redis Stream for async processing
4. Return immediately (non-blocking)

---

### 2. coderunner-workers

The worker service processes submissions from the queue and executes code securely.

#### Processing Pipeline

```
Stream Consumer → Dispatcher → Language Executor → Docker Executor → Update DB
```

**Stream Consumer**
- Creates a Redis consumer group on startup
- Continuously polls the `submission-stream` for new messages
- Acknowledges messages after successful processing

**Submission Dispatcher**
- Fetches full submission data from database
- Routes to appropriate language executor based on submission language
- Supports multiple languages through the `CodeExecutor` interface

**Language Executors**
- Each language has its own executor implementation (e.g., `CppExecutor`)
- Prepares the execution environment:
  - Creates temporary workspace directory
  - Copies test cases from question's testcase path
  - Writes user code to file
  - Generates execution script with time/memory limits

**Docker Executor**
- Mounts the workspace into a Docker container
- Executes the `run.sh` script inside an isolated environment
- Captures output and cleans up container after execution

---

## Data Models

### Question
```
QuestionModel
├── questionId (UUID)
├── title
├── markdown (problem description)
├── testcasesPath (filesystem location)
├── testcasesCount
├── timeLimit (milliseconds)
├── memoryLimit (KB)
├── difficulty (EASY, MEDIUM, HARD)
├── topics (list of tags)
├── submissions (count)
└── accepted (count)
```

### Submission
```
SubmissionModel
├── submissionId (auto-generated)
├── language → LanguageModel
├── question → QuestionModel
├── user → UserModel
├── code (source code text)
├── status (PENDING, RUNNING, ACCEPTED, WRONG_ANSWER, TLE, MLE, RUNTIME_ERROR, COMPILATION_ERROR)
├── timeTaken
├── memoryTaken
├── submittedAt (timestamp)
└── judgeMessage
```

### Language
```
LanguageModel
├── languageId
├── languageName (e.g., "cpp", "python")
└── languageMultiplier (for time limit adjustments)
```

---

## Code Execution Flow

1. **Compilation**: User code is compiled with optimizations
   ```bash
   g++ user_code.cpp -O2 -std=c++17 -o user_program
   ```

2. **Execution**: Code runs against each test case with limits
   ```bash
   timeout $TIME_LIMIT bash -c "ulimit -v $MEMORY_LIMIT; ./user_program < input.txt > output.txt"
   ```

3. **Judging**: Custom judge validates output
   ```bash
   ./judge_program input.txt < user_output.txt
   ```

4. **Verdict**: Returns one of:
   - `ACCEPTED` - All test cases passed
   - `WRONG_ANSWER` - Output doesn't match expected
   - `TIME_LIMIT_EXCEEDED` - Execution took too long
   - `MEMORY_LIMIT_EXCEEDED` - Used too much memory
   - `RUNTIME_ERROR` - Program crashed
   - `COMPILATION_ERROR` - Code failed to compile

---

## Technology Stack

| Layer | Technology |
|-------|------------|
| API Framework | Spring Boot |
| Database | PostgreSQL (via JPA/Hibernate) |
| Message Queue | Redis Streams |
| Containerization | Docker (docker-java client) |
| Build Tool | Maven |

---

## Configuration

### API Service
- Database connection settings
- Redis connection (`localhost:6379`)
- Testcase base path for filesystem storage

### Worker Service
- Redis stream name and consumer group
- Docker image names for each language (e.g., `judge-cpp`)
- Worker workspace directory for temporary files

---

## Extending the System

### Adding a New Language

1. Create a new executor class implementing `CodeExecutor`:
   ```java
   @Component
   public class PythonExecutor implements CodeExecutor {
       public String getLanguage() { return "python"; }
       public boolean execute(SubmissionModel submission) { ... }
   }
   ```

2. Create a corresponding `run.sh` script in resources
3. Build a Docker image with the language runtime
4. Add the language entry to the database

### Scaling Workers

Workers use Redis consumer groups, allowing horizontal scaling:
- Each worker gets a unique consumer name
- Messages are distributed across workers
- Failed messages can be re-processed

---

## Directory Structure

```
coderunner/
├── coderunner-api/
│   └── src/main/java/com/varshith/coderunner/
│       ├── controllers/     # REST endpoints
│       ├── service/         # Business logic
│       ├── repository/      # Database access
│       ├── models/          # JPA entities
│       ├── dtos/            # Request/Response objects
│       ├── helpers/         # Validators, file utilities
│       └── config/          # Redis, Database config
│
└── coderunner-workers/
    └── src/main/java/com/varshith/coderunner_workers/
        ├── consumer/        # Redis stream consumer
        ├── dispatcher/      # Routes to executors
        ├── executors/       # Language-specific execution
        ├── models/          # Shared JPA entities
        ├── repository/      # Database access
        └── config/          # Docker, Redis, Database config
```

---

## Security Features

- **Docker Isolation**: User code runs in isolated containers
- **Resource Limits**: Time and memory limits enforced via `timeout` and `ulimit`
- **No Network Access**: Containers can be configured without network access
- **Temporary Workspaces**: Each submission gets a fresh, isolated workspace
- **Input Validation**: All inputs validated before processing
