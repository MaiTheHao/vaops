# Frontend CI/CD Pipeline & NG_APP Environment Variable Integration Design

## Overview
This specification outlines the completed CI/CD pipeline for the Angular frontend application (`frontend-cicd.yml`), Dockerfile configuration with fallback environment variables, and Docker Compose integration for both development (`compose.dev.yml`) and production (`compose.prod.yml`).

## Problem Statement
Angular applications compiled using `@ngx-env/builder` perform build-time environment variable substitution for keys prefixed with `NG_APP_`. In Dockerized environments and CI/CD pipelines, `NG_APP_API_URL` needs a reliable default value (`http://localhost:8080/api`) while allowing dynamic overrides via GitHub Actions secrets (`${{ secrets.NG_APP_API_URL }}`) during Docker build steps.

## Proposed Design & Changes

### 1. `frontend/Dockerfile`
- Declare `ARG NG_APP_API_URL=http://localhost:8080/api` with default value.
- Export `ENV NG_APP_API_URL=${NG_APP_API_URL}` before running `pnpm run build`.
- Maintain multi-stage build structure using `node:24-alpine` for building and `nginxinc/nginx-unprivileged:1.27-alpine` for serving static assets on port 8080.

### 2. `.github/workflows/frontend-cicd.yml`
Implement a 3-stage GitHub Actions pipeline mirroring `backend-ci.yml`:
- **Triggers**:
  - `push` on `master` branch (paths: `frontend/**`, `.github/workflows/frontend-cicd.yml`).
  - `pull_request` on `master` branch (paths: `frontend/**`, `.github/workflows/frontend-cicd.yml`).
- **Job 1: `test-and-build`**
  - Checkout code.
  - Setup Node.js & pnpm.
  - Run `pnpm install --frozen-lockfile`.
  - Run tests and Angular build.
- **Job 2: `docker-build-push`** (Push to `master` only)
  - Login to Docker Hub using secrets (`DOCKER_USERNAME`, `DOCKER_PASSWORD`).
  - Use `docker/build-push-action@v5` with `--build-arg NG_APP_API_URL=${{ secrets.NG_APP_API_URL || 'http://localhost:8080/api' }}`.
  - Push tags `${{ secrets.DOCKER_USERNAME }}/vaops-frontend:${{ github.sha }}` and `:latest`.
- **Job 3: `deploy`** (Push to `master` only)
  - Connect via Tailscale VPN.
  - Execute SSH commands on remote VPS to pull and recreate the `frontend` container in `compose.prod.yml`.

### 3. `infra/docker/compose.dev.yml`
- Add `frontend` service definition with local build context (`../../frontend`).
- Support build argument `NG_APP_API_URL: ${NG_APP_API_URL:-http://localhost:8080/api}`.

### 4. `infra/docker/compose.prod.yml`
- Update `frontend` image reference to support dynamic tagging via `FRONTEND_TAG`: `c4fdocker/vaops-frontend:${FRONTEND_TAG:-latest}`.

## Verification Plan
1. Validate syntax of `.github/workflows/frontend-cicd.yml` and `Dockerfile`.
2. Test Docker build locally using `docker build -t vaops-frontend:test ./frontend`.
3. Verify environment variable substitution during `pnpm build`.
