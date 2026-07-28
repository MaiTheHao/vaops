# VAOPS Required Environment Secrets

To ensure the GitHub Actions CI/CD pipelines execute successfully, configure the following secrets under **Settings > Secrets and variables > Actions**:

## GitHub Repository Secrets

| Secret Name | Description | Used In Workflows |
| :--- | :--- | :--- |
| `DOCKER_USERNAME` | Docker Hub account username used for registry authentication and image tagging. | `backend-cicd.yml`, `frontend-cicd.yml` |
| `DOCKER_PASSWORD` | Docker Hub personal access token or password. | `backend-cicd.yml`, `frontend-cicd.yml` |
| `TAILSCALE_CLIENT_ID` | OAuth Client ID generated from Tailscale admin console for secure private network connectivity. | `backend-cicd.yml`, `frontend-cicd.yml`, `infra-cicd.yml` |
| `TAILSCALE_SECRET` | OAuth Client Secret from Tailscale. | `backend-cicd.yml`, `frontend-cicd.yml`, `infra-cicd.yml` |
| `VPS_IP` | Private IP address (or Tailscale IP) of the production VPS host. | `backend-cicd.yml`, `frontend-cicd.yml`, `infra-cicd.yml` |
| `VPS_USERNAME` | SSH login username on the VPS (e.g., `ubuntu` or `root`). | `backend-cicd.yml`, `frontend-cicd.yml`, `infra-cicd.yml` |
| `SSH_PRIVATE_KEY` | SSH Private Key matching the target host's `authorized_keys`. | `backend-cicd.yml`, `frontend-cicd.yml`, `infra-cicd.yml` |
| `NG_APP_API_URL` | Public endpoint URL of the backend API passed as build-arg to Angular application. | `frontend-cicd.yml` |
| `DB_PORT` | PostgreSQL database port for production (e.g., `5432`). | `infra-cicd.yml` |
| `DB_NAME` | Production database name. | `infra-cicd.yml` |
| `POSTGRES_USER` | Production database superuser/username. | `infra-cicd.yml` |
| `POSTGRES_PASSWORD` | Production database user password. | `infra-cicd.yml` |
| `BE_CORS_ALLOWED_ORIGINS` | Allowed origins configuration for backend CORS (e.g., `https://vaops.id.vn`). | `infra-cicd.yml` |
| `VAOPS_AUTH_JWT_ACCESS_SECRET` | Secret key used for signing JWT access tokens in backend authentication. | `infra-cicd.yml` |
| `VAOPS_AUTH_JWT_REFRESH_SECRET` | Secret key used for signing JWT refresh tokens in backend authentication. | `infra-cicd.yml` |
