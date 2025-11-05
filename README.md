# 📚 Book Recommendation System - Backend

Backend cho hệ thống gợi ý sách: cung cấp REST API cho xác thực, quản lý người dùng, sách, bookmark, rating và dịch vụ gợi ý. Ứng dụng dùng Spring Boot, lưu dữ liệu vào database và lưu file (cover, ảnh) lên MinIO (S3-compatible).

## Mô tả ngắn

Ứng dụng chịu trách nhiệm xử lý business logic và REST API cho frontend/mobile. Nó quản lý authentication (JWT), upload file qua MinIO, lưu/đọc dữ liệu từ database và tích hợp module recommendation.

## ⚙️ Công nghệ sử dụng

| Công nghệ | Vai trò |
|---|---|
| Java 17+ | Ngôn ngữ chính |
| Spring Boot | Framework REST / DI / Security |
| Maven (./mvnw) | Build & dependency management |
| MinIO (S3-compatible) | Object storage cho file uploads |
| PostgreSQL | Quan hệ dữ liệu |
| Docker | Chạy MinIO / containerize backend |

## 🧩 Kiến trúc tổng quan

- Backend: REST API, authentication (JWT), upload/download file (MinIO), database persistence (JPA/Hibernate), và tích hợp với recommendation module.
- MinIO: lưu ảnh/cover và file tĩnh.
- Database: lưu user, book, rating, history, recommendation data.

## 🧭 Yêu cầu hệ thống

- Java 17+
- Maven (sử dụng `./mvnw` wrapper có sẵn)
- Docker (để chạy MinIO nhanh trong môi trường dev)
- MySQL hoặc PostgreSQL (hoặc RDBMS tương thích)

## 🔧 Cấu hình ứng dụng

Ứng dụng đọc cấu hình từ `src/main/resources/application.yml`. Bạn có thể override bằng biến môi trường khi chạy container.

Ví dụ minh hoạ `application.yml` (dùng placeholder để thay bằng giá trị thật):

```yaml
spring:
  datasource:
    url: ${DB_URL}          # e.g. jdbc:mysql://localhost:3306/book_recsys
    username: ${DB_USER}
    password: ${DB_PASS}

server:
  port: ${SERVER_PORT:8080}

minio:
  url: ${MINIO_URL}        # http://localhost:9000
  accessKey: ${MINIO_ACCESS_KEY}
  secretKey: ${MINIO_SECRET_KEY}
  bucket: ${MINIO_BUCKET}

# JWT, mail, cloudinary, etc.
security:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: ${JWT_EXP_MS:3600000}
```

Gợi ý: trên macOS/zsh bạn có thể export các biến trước khi chạy:

```bash
export DB_URL=jdbc:mysql://localhost:3306/book_recsys
export DB_USER=your_user
export DB_PASS=your_pass
export MINIO_URL=http://localhost:9000
export MINIO_ACCESS_KEY=admin
export MINIO_SECRET_KEY=12345678
export MINIO_BUCKET=book-recsys-bucket
export JWT_SECRET=some_long_secret
```

### Chạy MinIO (dev)
Chạy MinIO bằng Docker (dev):

```bash
docker run -d --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e MINIO_ROOT_USER=admin \
  -e MINIO_ROOT_PASSWORD=12345678 \
  -v ~/minio_data:/data \
  quay.io/minio/minio server /data --console-address ":9001"
```

Console: http://localhost:9001 (user/password theo env trên)

## 🏗️ Build & Run

Sử dụng Maven wrapper (Linux / macOS - zsh):

```bash
# Biên dịch và package
./mvnw clean package -DskipTests

# Chạy jar
java -jar target/*.jar

# Hoặc chạy trực tiếp trong dev
./mvnw spring-boot:run
```

Trong IDE: chạy `com.bookrecommend.book_recommend_be.BookRecommendationSystemApplication`.

## 🗄️ Khởi tạo Database

Nếu có `init_db.sql` ở gốc repo, import bằng MySQL CLI / psql:

MySQL:
```bash
mysql -u root -p < init_db.sql
```

Postgres:
```bash
psql -U postgres -d your_db -f init_db.sql
```

Nếu dùng Hibernate DDL auto, schema có thể được tạo tự động theo cấu hình.

## 🔌 Endpoints chính

Những nhóm API chính (xem code để biết đường dẫn và payload chi tiết):

- /auth — đăng ký, đăng nhập, refresh token, reset password
- /users — quản lý người dùng, profile
- /books — CRUD sách, truy vấn sách
- /genres — danh mục/genre
- /recommendations — lấy gợi ý, cập nhật, chạy job gợi ý
- /ratings — rating sách
- /bookmarks — bookmark của user
- /history — lịch sử đọc

## 🐳 Dockerize

Ví dụ `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build image:

```bash
docker build -t book-recsys-backend:latest .
```

Run (truyền biến môi trường để override cấu hình):

```bash
docker run -d --name backend -p 8080:8080 \
  -e DB_URL=jdbc:mysql://<host>:<port>/<db> \
  -e DB_USER=<user> \
  -e DB_PASS=<pass> \
  -e MINIO_URL=http://<minio-host>:9000 \
  -e MINIO_ACCESS_KEY=<ak> \
  -e MINIO_SECRET_KEY=<sk> \
  book-recsys-backend:latest
```

Tip: dùng Docker Compose để chạy MinIO + DB + backend cùng lúc (tôi có thể tạo file `docker-compose.yml` nếu cần).

## 🔒 Bảo mật & Lưu ý

- Không commit secrets (DB password, MinIO keys, JWT secret) vào git.
- Dùng biến môi trường, vault hoặc secret manager cho production.
- Dùng HTTPS, rotate keys định kỳ, và bật CORS/CSRF phù hợp nếu cần.

## 🩺 Troubleshooting (những lỗi thường gặp)

| Vấn đề | Nguyên nhân thường gặp | Hướng xử lý |
|---|---|---|
| 500 / DB connection error | Sai URL / user / password, DB chưa chạy | Kiểm tra `DB_URL`, credentials, và DB service |
| Lỗi upload file | MinIO không reachable, bucket chưa có | Kiểm tra `MINIO_URL`/creds, tạo bucket |
| 401 Unauthorized | JWT missing/expired hoặc secret sai | Kiểm tra header Authorization, `JWT_SECRET` |
| Build fail | Dependency/Java version mismatch | Kiểm tra Java version (`java -version`) và `pom.xml` |

## 📁 Cấu trúc thư mục (tóm tắt)

```text
src/
  main/
    java/
      com/bookrecommend/book_recommend_be/
        BookRecommendationSystemApplication.java
        config/
        controller/
        dto/
        exceptions/
        model/
        repository/
        security/
        service/
    resources/
      application.yml
      templates/
      static/
test/
init_db.sql
pom.xml
Dockerfile
```

## 🤝 Liên hệ / Đóng góp

- Owner / Team: `Wjbu`
- Mọi thay đổi xin gửi Pull Request vào branch `main` hoặc mở Issue để thảo luận.
- Khi tạo issue/PR, mô tả rõ: bước reproduce, logs, và môi trường (Java version, OS).

---

Cảm ơn! 👍