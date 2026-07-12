---
name: project-context
description: Build a concise mental model of a software project by retrieving only the minimum relevant context required for downstream agents. Optimized for architecture understanding, module relationships, dependency discovery, and targeted implementation lookup.
---

# Project Context

## Purpose

Build a concise, high-quality mental model of a project before analysis, implementation, debugging, or review.

## Known Project Reference

When analyzing or building the project context for this workspace, leverage the following static system specifications directly:

- **Developer / Author**: Mai Thế Hào dev (only developer) - [GitHub Profile](https://github.com/MaiTheHao)
- **Project Purpose**: Study, research, and aim towards practical applications.
- **Main Documentation Directory**: [/docs](file:///home/maithehao/Workspace/projects/vaops/docs)
- **CI/CD Workflows**: Located in [workflows](file:///home/maithehao/Workspace/projects/vaops/.github/workflows)
- **Technology Stack & Architecture**:
  - **Backend**: Spring Boot 4.x modular monolith. Build configuration: [pom.xml](file:///home/maithehao/Workspace/projects/vaops/backend/pom.xml).
  - **Frontend**: Angular 21.

This skill does **not** answer user questions.

Its only responsibility is to retrieve the minimum relevant project context required by downstream agents.

Always optimize for:

- Minimal context usage
- High retrieval precision
- Low token consumption
- Fast project understanding

---

## Core Principles

- Metadata First, Source Code Last.
- Retrieve information, not files.
- Never inspect implementation unless required.
- Stop retrieval as soon as sufficient context has been collected.
- Every retrieved artifact must directly contribute to the requested knowledge.
- Prefer official documentation over inferred implementation.
- Keep the output concise, structured, and deterministic.

---

## Retrieval Priority

Always retrieve context in this order.

**P0 — Project Metadata**

- README
- Documentation
- Architecture docs
- ADR
- Build files
- Workspace configuration

↓

**P1 — Repository Structure**

- Folder hierarchy
- Module layout
- Package organization
- Naming conventions

↓

**P2 — Public Contracts**

- Controllers
- Public APIs
- Interfaces
- DTOs
- Configuration

↓

**P3 — Implementation**

- Services
- Use Cases
- Repositories
- Algorithms

↓

**P4 — Supporting Assets**

- Tests
- Migrations
- Benchmarks
- Historical documents

Implementation is always the last resort.

---

## Workflow

```
Question
    │
    ▼
Intent Classification
    │
    ▼
Information Gap Analysis
    │
    ▼
Select Retrieval Strategy
    │
    ▼
Retrieve Minimum Context
    │
    ▼
Validate
    │
    ▼
Return PROJECT CONTEXT
```

---

## Retrieval Strategies

Select one or more strategies based on the request.

- Project Discovery
- Architecture Discovery
- Structure Discovery
- Relationship Discovery
- Configuration Discovery
- Behavior Discovery
- Implementation Discovery

Only retrieve information required by the selected strategy.

---

## Stop Conditions

Immediately stop retrieval when:

- The requested information has been found.
- Additional files provide no new information.
- Implementation is unnecessary.
- The downstream agent has enough context to continue.

Never continue retrieving "just in case."

---

## Output Format

Always return:

```text
PROJECT CONTEXT

Purpose
Architecture
Technology Stack
Relevant Modules
Relationships
Relevant Symbols
Relevant Artifacts
Key Findings
```

Do not answer the original question.

Return context only.