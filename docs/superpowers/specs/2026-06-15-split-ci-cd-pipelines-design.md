# Phân chia CI/CD Pipelines: Infra và Backend

## Mục tiêu
Tách workflow hiện tại (`backend-ci.yml`) thành 2 pipelines riêng biệt để quản lý độc lập vòng đời triển khai (deployment lifecycle) cho hạ tầng (infra) và ứng dụng (backend).

## Bối cảnh
Hiện tại `backend-ci.yml` xử lý cả việc copy file `infra` lên VPS, thiết lập `.env` và deploy backend container. Việc gộp này khiến mỗi khi có thay đổi backend, toàn bộ tiến trình của `infra` lại chạy lại (kể cả khi không có thay đổi infra nào).

## Kiến trúc đề xuất

### 1. `infra-ci.yml` (Hạ tầng)
- **Mục đích:** Quản lý thay đổi cấu hình hạ tầng như database, file docker-compose, các script khởi tạo (flyway/db).
- **Trigger:**
  - `push` / `pull_request` vào thư mục `infra/**`.
- **Luồng triển khai (Jobs/Steps):**
  - **Checkout code.**
  - **Connect to Tailscale:** Kết nối VPS nội bộ.
  - **Copy thư mục `infra`:** Dùng `appleboy/scp-action` để copy thư mục `infra` từ repository lên đường dẫn `~/workspace/` trên VPS.
  - **SSH & Deploy:** 
    - Sinh ra file `.env` bằng các GitHub Secrets (DB_PORT, DB_NAME, POSTGRES_USER, POSTGRES_PASSWORD...).
    - Chạy lệnh `docker compose -f compose.prod.yml up -d --remove-orphans` để cập nhật toàn bộ stack hạ tầng.

### 2. `backend-ci.yml` (Ứng dụng)
- **Mục đích:** Quản lý vòng đời build và deploy cho ứng dụng backend (Spring Boot).
- **Trigger:**
  - `push` / `pull_request` vào thư mục `backend/**`.
- **Luồng triển khai (Jobs/Steps):**
  - **test-and-build:** Chạy unit tests và build file JAR.
  - **docker-build-push:** Đóng gói file JAR vào Docker image và push lên Docker Hub (tag theo sha và latest).
  - **deploy:** 
    - Kết nối Tailscale.
    - SSH vào VPS: Đi đến thư mục `~/workspace/infra/docker` (nơi đã có sẵn cấu hình từ pipeline infra).
    - Chỉ chạy cập nhật cho đúng service backend:
      ```bash
      docker compose -f compose.prod.yml pull backend
      docker compose -f compose.prod.yml up -d --no-deps backend
      ```
    - Không copy lại thư mục `infra` hay tạo lại file `.env`.

## Tính cô lập & Ưu điểm
- Thay đổi `backend` deploy cực nhanh do không phải xử lý và khởi động lại các file infra.
- Các biến môi trường hay cấu hình db thay đổi trong thư mục `infra` sẽ kích hoạt pipeline `infra-ci` độc lập, an toàn hơn.
