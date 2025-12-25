# Paperless – Semester Project
This repository contains the Paperless semester project for the course SWEN3 (BIF5).  
Sprint 3 implements the first fully integrated version of the Document Management System using:

- A REST API (Spring Boot)
- A web frontend (React)
- A PostgreSQL database
- A RabbitMQ message broker
- A worker service consuming messages (OCR worker prototype)

This README documents all critical architectural aspects, message flow, runtime instructions, validation, error handling, testing strategy, and design decisions required for the Sprint 3 mid-term code review.

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

---

# 2. Architecture (Textual Description)

The system consists of five services, orchestrated via Docker Compose.

## 1. paperless-rest — Spring Boot Backend
- Exposes REST endpoints under `/documents`
- Stores document metadata in PostgreSQL
- Handles JSON and multipart uploads
- Publishes `DocumentUploadMessage` events to RabbitMQ
- Provides DTO mapping, validation, global exception handling, and logging
- Contains service layer and entity layer

## 2. paperless-webui — React Frontend
- Allows document upload and listing
- Communicates with backend via REST
- Runs in its own container

## 3. paperless-db — PostgreSQL
- Stores document metadata
- Accessed through Spring Data JPA

## 4. rabbitmq — Message Broker
- Hosts queue `ocr.document.uploaded`
- Decouples backend and worker
- Messages persist even when worker is offline

## 5. ocr-worker — Spring Boot Worker
- Listens on RabbitMQ queue
- Receives `DocumentUploadMessage`
- Logs message (OCR implementation in next sprint)

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


---

# 4. How to Run the System (Docker Compose)

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

---

# 5. Ports & URLs

| Service | URL / Port | Description |
|--------|------------|-------------|
| Frontend | http://localhost | Web UI |
| Backend (REST) | http://localhost:8081 | REST API |
| PostgreSQL | 5432 | Database |
| RabbitMQ UI | http://localhost:15672 | Queue Dashboard |
| OCR Worker | — | Listens to queue only |

RabbitMQ credentials:
- **User:** paperless
- **Password:** secret

---

# 6. REST API Documentation

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

---

# 7. Validation Rules

Using Jakarta Bean Validation:

- **filename** → must not be blank
- **description** → max 2000 chars

Invalid requests produce structured JSON field errors:

```json
{
  "description": "must be at most 2000 characters"
}
```

---

# 8. Error Handling (GlobalExceptionHandler)

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

# 9. Testing Strategy

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

Run tests:
```bash
cd backend
./mvnw test
```

---

# 10. Design Decisions (Critical Aspects)

## Loose Coupling
- REST layer uses DTOs
- Service layer uses entities
- Worker is fully decoupled (messaging only)

## Asynchronous Architecture
- Upload returns immediately
- Worker processes in background

## Scalability
- Worker can be horizontally scaled

## Reliability
- RabbitMQ stores messages
- Worker may be offline

## Predictable Error Handling
- Centralized exception management
- Clear JSON structures

---

# 11. Project Structure

```bash
Paperless/
│
├── backend/                   # Spring Boot REST API
│   ├── src/main/java
│   ├── src/test/java
│   └── Dockerfile
│
├── ocr-worker/                # Spring Boot RabbitMQ consumer
│   └── Dockerfile
│
├── frontend/                  # React UI
│   └── Dockerfile
│
├── docker-compose.yml         # Orchestration
└── README.md                  # This file```

---

# 13. Running Tests

```bash
cd backend
./mvnw test
```

---

# 12. Authors
Batuhan Saimler
Jansen Wu

