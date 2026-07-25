# VAOPS GitHub Actions CI/CD Workflows

This directory contains the GitHub Actions CI/CD pipeline definitions for the **VAOPS** project. The workflows automate testing, container building, artifact management, infrastructure synchronization, and remote deployment over a secure VPN layer.

---

## Workflows Overview

### 1. Backend CI/CD (`backend-ci.yml`)
* **Trigger**: 
  * `push` or `pull_request` affecting `.github/workflows/backend-ci.yml` or `backend/**` on the `master` branch.
* **Pipeline Stages**:
  * **`test-and-build`**: Sets up Java 21 (Temurin SDK with Maven caching), executes unit & integration tests (`./mvnw test`), builds the production JAR package (`./mvnw clean package -DskipTests`), and uploads the output artifact.
  * **`docker-build-push`** *(master branch push only)*: Downloads the generated JAR artifact, builds a Docker image using Docker Buildx, tags it with `${github.sha}` and `latest`, and pushes it to Docker Hub.
  * **`deploy`** *(master branch push only)*: Establishes a secure connection via Tailscale OAuth, SSHs into the target VPS, updates `BACKEND_TAG`, pulls the latest Docker image, and triggers `docker compose` to perform a zero-downtime update for the backend container.

---

### 2. Frontend CI/CD (`frontend-cicd.yml`)
* **Trigger**:
  * `push` or `pull_request` affecting `.github/workflows/frontend-cicd.yml` or `frontend/**` on the `master` branch.
* **Pipeline Stages**:
  * **`test-and-build`**: Installs `pnpm` (v11) and Node.js 24 with pnpm lockfile caching, executes `pnpm run build` for Angular production output, and uploads the built distribution artifact.
  * **`docker-build-push`** *(master branch push only)*: Builds the container image using multi-stage Docker build, passing `NG_APP_API_URL` as a build argument, tags it with `${github.sha}` and `latest`, and pushes it to Docker Hub.
  * **`deploy`** *(master branch push only)*: Connects via Tailscale, connects to the VPS via SSH, updates `FRONTEND_TAG`, pulls the new frontend Docker image, and restarts the frontend service container.

---

### 3. Infrastructure Deployment (`infra-ci.yml`)
* **Trigger**:
  * `push` or `pull_request` affecting `.github/workflows/infra-ci.yml` or `infra/**` on the `master` branch.
* **Pipeline Stages**:
  * **`deploy`** *(master branch push only)*: 
    1. Authenticates with Tailscale.
    2. Syncs the `infra/` folder directly to `~/workspace/infra/` on the VPS using `rsync` with recursion and deletion flags.
    3. Generates `.env.prod` dynamically on the remote VPS containing database parameters.
    4. Executes `docker compose up -d` for all infrastructure components, reloads the Nginx container (`nginx -s reload`), and prunes dangling Docker images.

---

## Required GitHub Repository Secrets

To ensure these pipelines execute successfully, configure the following secrets under **Settings > Secrets and variables > Actions**:

| Secret Name | Description | Used In Workflows |
| :--- | :--- | :--- |
| `DOCKER_USERNAME` | Docker Hub account username used for registry authentication and image tagging. | `backend-ci.yml`, `frontend-cicd.yml` |
| `DOCKER_PASSWORD` | Docker Hub personal access token or password. | `backend-ci.yml`, `frontend-cicd.yml` |
| `TAILSCALE_CLIENT_ID` | OAuth Client ID generated from Tailscale admin console for secure private network connectivity. | `backend-ci.yml`, `frontend-cicd.yml`, `infra-ci.yml` |
| `TAILSCALE_SECRET` | OAuth Client Secret from Tailscale. | `backend-ci.yml`, `frontend-cicd.yml`, `infra-ci.yml` |
| `VPS_IP` | Private IP address (or Tailscale IP) of the production VPS host. | `backend-ci.yml`, `frontend-cicd.yml`, `infra-ci.yml` |
| `VPS_USERNAME` | SSH login username on the VPS (e.g., `ubuntu` or `root`). | `backend-ci.yml`, `frontend-cicd.yml`, `infra-ci.yml` |
| `SSH_PRIVATE_KEY` | SSH Private Key matching the target host's `authorized_keys`. | `backend-ci.yml`, `frontend-cicd.yml`, `infra-ci.yml` |
| `NG_APP_API_URL` | Public endpoint URL of the backend API passed as build-arg to Angular application. | `frontend-cicd.yml` |
| `DB_PORT` | PostgreSQL database port for production (e.g., `5432`). | `infra-ci.yml` |
| `DB_NAME` | Production database name. | `infra-ci.yml` |
| `POSTGRES_USER` | Production database superuser/username. | `infra-ci.yml` |
| `POSTGRES_PASSWORD` | Production database user password. | `infra-ci.yml` |
