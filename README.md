# AI-Powered GitHub Code Review Chatbot

> **"ChatGPT for reviewing GitHub repositories"** — A full-stack AI Code Review application built with **Java, Spring Boot, Google Gemini API, PostgreSQL + pgvector, GitHub OAuth / API, and React**.

---

## Features

- 🔐 **JWT Authentication & User Management**: Secure signup, login, password encryption via BCrypt, and user profile management.
- 🐙 **GitHub Integration**: Connect GitHub via OAuth or Personal Access Token (PAT). Browse private and public repositories with language badges, stars, and update timestamps.
- 📥 **Background Repository Importer & Chunker**: Recursively imports repository trees, filters unwanted/binary files, and chunks code with precise start/end line tracking.
- 🧠 **Vector RAG Engine**: Generates semantic embeddings with Google Gemini and performs cosine similarity search across codebase chunks.
- 📊 **Structured AI Code Review**: Analyzes code across 7 core dimensions:
  1. **Architecture & Patterns**
  2. **Code Quality**
  3. **Efficiency** (Algorithmic bottlenecks, N+1 query risks, unnecessary memory/loops, caching opportunities)
  4. **Security** (Vulnerabilities, authentication, input validation)
  5. **Maintainability** (Modularity, coupling, naming)
  6. **Testing** (Test coverage, mocks, assertions)
  7. **Documentation** (README, inline docs)
- ⚖️ **Evidence vs Recommendation**: Strict distinction between observed code evidence and actionable recommendations.
- 🗂️ **Interactive Code Reference Cards**: Clickable cards (e.g. `[ 📄 JwtAuthFilter.java (Lines 24–58) ]`) that open the built-in **Code Viewer Modal** highlighting the exact lines.
- 💬 **Conversational AI Chat**: Dual-pane split view allowing developers to ask follow-up questions to Gemini with repository context and citations.
- 📈 **Dashboard & Persistence**: View stats (Repositories Reviewed, AI Questions Asked, Average Score) and reopen past reviews.

---

## Architecture Overview

```text
code review/
├── backend/
│   ├── mvnw / mvnw.cmd
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/codereview/
│       │   ├── auth/          # Authentication, JWT utilities, UserPrincipal
│       │   ├── user/          # User entities, services, dashboard stats
│       │   ├── github/        # GitHub REST client, OAuth service, repo fetcher
│       │   ├── repository/    # Importer, file filter, indexer, chunks
│       │   ├── ai/            # Gemini client, structured prompt review generator
│       │   ├── embedding/     # Vector embeddings & cosine similarity search
│       │   ├── review/        # Review entities, findings, scoring services
│       │   ├── chat/          # RAG conversational chat engine
│       │   ├── security/      # SecurityConfig & filters
│       │   └── config/        # Async, CORS, Jackson configurations
│       └── main/resources/
│           └── application.yml
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── components/
│       │   ├── review/        # ScoreCard, Gauge, Efficiency, CategoryTabs, ReferenceCards
│       │   ├── chat/          # ChatInterface, MessageItem, SuggestedPrompts
│       │   ├── code/          # CodeViewerModal with line highlighting
│       │   └── layout/        # Sidebar, Navbar, AppLayout
│       ├── pages/             # Dashboard, Repositories, Review, Profile, Login, Signup
│       └── services/          # API services
└── docker-compose.yml         # PostgreSQL + pgvector container
```

---

## Quick Start Guide

### 1. Start Database (Docker)
```bash
docker-compose up -d
```

### 2. Configure Environment Variables
Set your Google Gemini API key and GitHub credentials (optional for OAuth):
```bash
# Windows PowerShell
$env:GEMINI_API_KEY="your_gemini_api_key"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/codereview_db"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgrespassword"

# Optional GitHub OAuth
$env:GITHUB_CLIENT_ID="your_client_id"
$env:GITHUB_CLIENT_SECRET="your_client_secret"
```
*(Note: You can also configure your Gemini API Key directly in the UI Settings / Profile page!)*

### 3. Run Backend (Spring Boot)
```bash
cd backend
./mvnw.cmd spring-boot:run
```
Backend runs at `http://localhost:8080`.

### 4. Run Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```
Frontend runs at `http://localhost:5173`.

---

## API Endpoints

- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Authenticate & obtain JWT
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/profile` - Update user profile & custom Gemini key
- `PUT /api/users/password` - Change password
- `GET /api/dashboard/stats` - Get user stats & review counts
- `GET /api/github/connect` - GitHub OAuth authorization URL
- `POST /api/github/connect-token` - Connect GitHub via Personal Access Token
- `GET /api/github/repositories` - Fetch user's GitHub repositories
- `POST /api/repositories/import` - Import repository & trigger background review
- `GET /api/repositories` - List user repositories
- `GET /api/repositories/{id}/status` - Live status & progress of analysis job
- `GET /api/repositories/{id}/file` - Fetch single file content with line numbers
- `GET /api/reviews/{id}` - Get full structured review with 7-category breakdown
- `POST /api/reviews/{id}/chat` - Send conversational question with RAG code retrieval
- `GET /api/conversations/{id}` - Retrieve conversation message history
