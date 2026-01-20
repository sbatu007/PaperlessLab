# Paperless – Semester Project
This repository contains the Paperless semester project for the course SWEN3 (BIF5).  
Sprint 3 implements the first fully integrated version of the Document Management System using:

- A REST API (Spring Boot)
- A web frontend (React)
- A PostgreSQL database
- A RabbitMQ message broker
- A worker service consuming messages (OCR worker prototype)

This README documents all critical architectural aspects, message flow, runtime instructions, validation, error handling, testing strategy, and design decisions required for the Sprint 3 mid-term code review.

> **Note:** This README reflects the final state of the project after completing Sprint 7, including batch processing, Elasticsearch integration, and full end-to-end testing.

---

# 1. Project Description

Paperless is a document management system for uploading, storing, and processing PDF documents.  
The main focus of Sprint 3 – Intermediate is:

- Implementing asynchronous communication between backend and worker (RabbitMQ)
- Processing document uploads
- Sending upload events to a queue
- Ensuring clean architecture, validation, and error handling
- Providing a frontend for upload and listing
- Passing the Sprint 3 code review

This sprint prepares the system for later sprints:
OCR (Sprint 4), Generative AI summaries (Sprint 5), Elasticsearch search (Sprint 6), Batch processing (Sprint 7).

## Out of Scope / Non-Goals
- No authentication or authorization
- No user management
- No production hardening (HTTPS, secrets management)

---

# 2. Architecture (Textual Description)

The system consists of five services, orchestrated via Docker Compose.

## 1. paperless-rest — Spring Boot Backend
- Exposes REST endpoints under `/documents`
- Stores document metadata in PostgreSQL (source of truth for all metadata)
- Handles JSON and multipart uploads
- Publishes `DocumentUploadMessage` events to RabbitMQ
- Provides DTO mapping, validation, global exception handling, and logging
- Contains service layer and entity layer

## 2. paperless-webui — React Frontend
- Allows document upload and listing
- Communicates with backend via REST
- Runs in its own container

## 3. paperless-db — PostgreSQL
- Stores document metadata (source of truth)
- Accessed through Spring Data JPA

## 4. rabbitmq — Message Broker
- Hosts queue `ocr.document.uploaded`
- Decouples backend and worker
- Messages persist even when worker is offline

## 5. ocr-worker — Spring Boot Worker
- Listens on RabbitMQ queue
- Receives `DocumentUploadMessage`
- Logs message (OCR implementation in next sprint)

### Responsibilities: OCR Worker vs Index Worker
- OCR Worker: consumes upload events, performs OCR on stored files, and emits enriched content (text, metadata).
- Index Worker: consumes OCR results and indexes filename, description, and OCR text into Elasticsearch for search.
- Indexing is triggered after OCR completion and after metadata updates (e.g., description changes).

### Elasticsearch Indexing and Search Flow
- Backend publishes `DocumentUploadMessage` → OCR Worker extracts text → Index Worker indexes into Elasticsearch.
- Indexed fields: `id`, `filename`, `description`, `ocrText`.
- Search queries use Elasticsearch scoring to return ranked results.
- Elasticsearch is used only for search; PostgreSQL remains the source of truth for persistence.

### MinIO File Lifecycle
- Uploads are stored in MinIO (S3-compatible). Metadata remains in PostgreSQL.
- Backend writes multipart files to MinIO; workers read from MinIO by object key.
- File stays in MinIO; OCR/Index workers do not delete or mutate the object.

---

# 3. Message Flow (Upload → RabbitMQ → Worker)

1. User uploads a document (REST or frontend).
2. Backend validates metadata and stores document in PostgreSQL.
3. Backend optionally stores file on disk (multipart only).
4. Backend creates a `DocumentUploadMessage`:

```json
{
  "documentId": 1,
  "filename": "example.pdf",
  "description": "Some description"
}
```

5. Backend publishes message to queue `ocr.document.uploaded`.
6. Worker receives message asynchronously.
7. Worker logs the message.
8. Stored file is available in MinIO for OCR and indexing; metadata stays in PostgreSQL.

---

# 4. Environment & Secrets
- Requires Docker and Docker Compose
- Java 21 and Node.js 20 for local development
- Copy `.env.sample` to `.env` and set `GOOGLE_API_KEY`
- Do not commit `.env` or secrets
- Frontend uses `.env` variables for API base URL; backend uses Spring `application.yml` plus `.env`

---

# 5. How to Run the System (Docker Compose)

## Requirements
- Docker
- Docker Compose

## Environment variables (.env)

For the GenAI (Gemini) summarization you need to provide a Google API key and (optionally) override the default GenAI settings.

1. Copy the template:

```bash
cp .env.sample .env
```

2. Edit `.env` and set at least `GOOGLE_API_KEY`:

```env
GOOGLE_API_KEY=

# Gemini API Base URL (Standard)
GENAI_BASE_URL=https://generativelanguage.googleapis.com/v1beta

# Modell
GENAI_MODEL=gemini-2.5-flash-lite

# Antwortlänge (ausreichend für Bullet Summary)
GENAI_MAX_OUTPUT_TOKENS=250

GENAI_TEMPERATURE=0.4

# GenAI Debug Logging
# Loggt Prompt-Länge + Vorschau
GENAI_LOG_REQUEST=true

# Loggt Antwort-Vorschau
GENAI_LOG_RESPONSE=true
```

> **Security note:** Never commit your `.env` file or API keys to git.


Start all services:

```bash
docker compose up --build
```

This starts:
- REST backend
- React frontend
- PostgreSQL
- RabbitMQ
- OCR worker
- Index worker
- MinIO
- Batch worker

To stop:
```bash
docker compose down
```

Health checks (inside Docker network):
- Backend: `http://paperless-backend:8081/actuator/health`
- OCR worker: `http://ocr-worker:8080/actuator/health`
- Index worker: `http://index-worker:8080/actuator/health`
- Batch worker: `http://batch-worker:8080/actuator/health`
- Docker Compose health checks rely on these Actuator endpoints.

---

# 6. Ports & URLs

| Service | URL / Port | Description |
|--------|------------|-------------|
| Frontend | http://localhost | Web UI |
| Backend (REST) | http://localhost:8081 | REST API |
| PostgreSQL | 5432 | Database |
| RabbitMQ UI | http://localhost:15672 | Queue Dashboard |
| OCR Worker | — | Listens to queue only |
| Batch Worker | — | Runs on internal port 8080 (health) |
| MinIO UI | http://localhost:9001 | Object storage console |
| MinIO S3 | http://localhost:9000 | S3-compatible API |

RabbitMQ credentials:
- **User:** paperless
- **Password:** secret

---

# 7. REST API Documentation

Base path: `/documents`

## GET /documents
Returns all documents.

## GET /documents/{id}
Returns a single document.  
404 if not found.

## POST /documents (JSON)
Stores metadata + sends queue message.

Example:
```json
{
  "filename": "file.pdf",
  "description": "text"
}
```

## POST /documents/upload (Multipart)
**Fields:**
- `file` (required)
- `description` (optional)

Stores file, metadata, sends queue message.

## PUT /documents/{id}
Updates description only.  
Description max length = **2000 characters**.

## DELETE /documents/{id}
Deletes document by ID.

## GET /documents/search?query={text}
Performs full-text search via Elasticsearch on filename, description, and OCR text.  
Returns ranked results by Elasticsearch score.

Example response:
```json
[
  {
    "id": 1,
    "filename": "invoice.pdf",
    "description": "March invoice",
    "score": 2.13
  }
]
```

---

# 8. Validation Rules

Using Jakarta Bean Validation:

- **filename** → must not be blank
- **description** → max 2000 chars

Invalid requests produce structured JSON field errors:

```json
{
  "description": "is max 2000 characters"
}
```

---

# 9. Error Handling (GlobalExceptionHandler)

Global exception handling ensures uniform JSON error responses.

Handled cases:
- `IllegalArgumentException` → 400
- `MethodArgumentNotValidException` → 400
- `MaxUploadSizeExceededException` → 413
- `NotFoundException` → 404
- `Exception` → 500

Example:
```json
{
  "error": "Invalid filename"
}
```

---

# 10. Testing Strategy

Sprint 3 requires automated tests.  
The project contains a complete test suite:

## Repository Tests
- Basic persistence test for `DocumentRepository`

## Service Tests
- `DocumentServiceMessagingTest` → checks message publishing
- `DocumentServiceUpdateDescriptionTest` → update & validation logic
- `DocumentServiceSignatureTest` → ensures service layer does not use DTOs

## Controller Tests
Using MockMvc:
- JSON POST
- Multipart upload
- Validation
- Correct DTO response (no entity leakage)

## Worker Tests
- Ensures worker can consume queue messages

## Integration Tests (Backend)
- Uses Testcontainers for PostgreSQL, RabbitMQ, Elasticsearch
- Run with Maven Failsafe (`*IT.java` suffix)
- Requires Docker daemon running
- Coverage includes end-to-end upload flow with queue consumption and search index verification.

Run all (unit + integration):
```bash
cd Paperless/backend
./mvnw verify
```

Unit only:
```bash
cd Paperless/backend
./mvnw test
```

Integration only:
```bash
cd Paperless/backend
./mvnw failsafe:integration-test failsafe:verify
```

---

# 11. Batch Processing Guide
The batch worker imports XML access logs into PostgreSQL on a fixed schedule.

## Configuration
- Input directory: `batch-worker/input` (mounted to `/app/input`)
- Archive directory: `batch-worker/archive` (mounted to `/app/archive`)
- Interval: `BATCH_FIXED_DELAY_MS` (default 60000 ms)
- Initial delay: `BATCH_INITIAL_DELAY_MS` (default 5000 ms)
- Database: shared Postgres from Docker Compose

## Run in Docker Compose
```bash
docker compose up batch-worker --build
```
Starts automatically with `docker compose up --build`.

## Local Development Run
```bash
cd batch-worker
./mvnw spring-boot:run \
  -DBATCH_INPUT_DIR=./input \
  -DBATCH_ARCHIVE_DIR=./archive \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/paperless \
  -Dspring.datasource.username=paperless_user \
  -Dspring.datasource.password=secret
```

## Processing Steps
1. Place XML files in `batch-worker/input`.
2. Worker polls, parses, and persists.
3. Success → file moves to `batch-worker/archive`; failure → logged and file stays.
4. On failure, the file remains in the input directory and will be retried on the next scheduled poll.

Health (container internal): `http://localhost:8080/actuator/health`.

## Data Model and Aggregation Logic
- Input XML contains access log entries (document ID, timestamp).
- Worker aggregates access counts per document and per day.
- Aggregated results are persisted in PostgreSQL (document-day totals).
- Each processed XML file is archived after successful persistence.

---

# 12. Project Structure
```bash
SWEN3/
│
├── .github/
│   └── workflows/
│       └── maven-build.yml        # GitHub Actions CI pipeline (build & test)
│
├── Paperless/
│   │
│   ├── backend/                   # Spring Boot REST backend (paperless-backend)
│   │   ├── src/main/java
│   │   ├── src/test/java
│   │   ├── src/main/resources
│   │   ├── src/test/resources
│   │   └── Dockerfile
│   │
│   ├── frontend/                  # React web frontend
│   │   ├── src/
│   │   ├── public/
│   │   └── Dockerfile
│   │
│   ├── ocr-worker/                # Spring Boot OCR worker service
│   │   ├── src/main/java
│   │   ├── src/main/resources
│   │   └── Dockerfile
│   │
│   ├── index-worker/              # Spring Boot Elasticsearch indexing worker
│   │   ├── src/main/java
│   │   ├── src/main/resources
│   │   └── Dockerfile
│   │
│   ├── batch-worker/              # Batch processing service (Sprint 7)
│   │   ├── src/main/java
│   │   ├── src/main/resources
│   │   ├── input/                 # XML input directory
│   │   ├── archive/               # Archived processed XML files
│   │   └── Dockerfile
│   │
│   ├── data/                      # Persistent data volumes (Docker)
│   │
│   ├── uploads/                   # Uploaded documents (local volume)
│   │
│   ├── .env                       # Local environment configuration (not committed)
│   ├── .env.sample                # Environment variable template
│   ├── docker-compose.yml         # Service orchestration
│   └── .gitignore
│
├── README.md                      # Main project documentation
├── semester-project.pdf           # Assignment specification
├── semester-project-architecture.png  # Architecture diagram
├── .gitattributes
└── .gitignore
```

---

# 13. Troubleshooting
- Docker not running → Testcontainers/integration tests fail.
- Port conflicts (8081, 5432, 15672, 9000/9001) → stop other services or remap in `docker-compose.yml`.
- RabbitMQ auth errors → check `.env` user/password match compose.
- MinIO not reachable → ensure `MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD` in `.env`.

---

# 14. CI Pipeline
- GitHub Actions workflow at `.github/workflows/maven-build.yml`
- Runs Maven build and tests on push/pull requests

---

# 15. Authors
Jansen Wu
