# Dockerfile + Docker Compose Formula (DACongCu)

Tai lieu nay giai thich cu the cong thuc da viet trong:
- `docker/backend.Dockerfile`
- `docker/frontend.Dockerfile`
- `docker/nginx/default.conf`
- `docker-compose.yml`
- `docker-compose.registry.yml`

## 1) Tong quan kien truc container

Cong thuc tach 2 service:
- `backend` (Spring Boot): xu ly API, logic, database access.
- `frontend` (Nginx): phuc vu HTML/CSS/JS va proxy `/api/*` sang backend.

Flow request:
1. Trinh duyet goi `http://localhost:8080/...` vao `frontend`.
2. Neu la static route thi Nginx tra file HTML/JS/CSS.
3. Neu la `/api/...` thi Nginx chuyen tiep sang `http://backend:8080` trong Docker network.

## 2) Cong thuc `docker/backend.Dockerfile`

```dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Giai thich theo tung dong:
- `FROM maven... AS build`: dung image co Maven + JDK 17 de build Java app.
- `WORKDIR /app`: dat thu muc lam viec ben trong container.
- `COPY pom.xml ./`: copy file dependency truoc de tan dung layer cache.
- `RUN mvn ... dependency:go-offline`: tai dependencies truoc, build lan sau nhanh hon.
- `COPY src ./src`: copy source code vao image build.
- `RUN mvn ... clean package`: dong goi thanh file jar.
- `FROM eclipse-temurin:17-jre`: runtime stage chi can JRE, image nhe hon.
- `COPY --from=build ... app.jar`: copy artifact tu stage build sang stage runtime.
- `EXPOSE 8080`: backend lang nghe port 8080 trong container.
- `ENTRYPOINT ...`: lenh khoi dong app khi container chay.

Tai sao dung multi-stage:
- Tach build tools (Maven/JDK) khoi runtime.
- Giam kich thuoc image runtime.
- Giam surface attack va build reproducible hon.

## 3) Cong thuc `docker/frontend.Dockerfile`

```dockerfile
FROM nginx:1.27-alpine

COPY docker/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY src/main/resources/static/ /usr/share/nginx/html/

EXPOSE 80
```

Giai thich:
- `FROM nginx:1.27-alpine`: web server nhe de phuc vu static.
- `COPY default.conf`: dung route/proxy tuy chinh cho app.
- `COPY static`: dua toan bo file HTML/CSS/JS vao web root cua Nginx.
- `EXPOSE 80`: frontend lang nghe port 80 trong container.

## 4) Cong thuc `docker/nginx/default.conf`

Ba nhom cau hinh chinh:

1. `location /api/ { proxy_pass http://backend:8080; }`
- Toan bo API duoc chuyen sang service `backend`.
- Trinh duyet goi cung origin `:8080` nen tranh loi CORS.

2. `location = /bill/success { try_files ... }` va cac route tuong tu
- Moi route "dep" duoc map den dung file HTML static.
- Vi frontend dang static, khong co Spring Controller o container frontend.

3. `location / { try_files $uri $uri/ =404; }`
- Fallback phuc vu file static binh thuong, neu khong co thi 404.

Luu y ky thuat:
- Ten `backend` trong `proxy_pass` la DNS name do Docker Compose network cap.
- Khong can hardcode IP.

## 5) Cong thuc `docker-compose.yml` (build local)

```yaml
services:
  backend:
    build:
      context: .
      dockerfile: docker/backend.Dockerfile
    ports:
      - "8081:8080"

  frontend:
    build:
      context: .
      dockerfile: docker/frontend.Dockerfile
    depends_on:
      - backend
    ports:
      - "8080:80"
```

Giai thich:
- `build.context: .`: build tu root project.
- `dockerfile: ...`: chi dinh file Dockerfile rieng cho moi service.
- `8081:8080` cho backend: host port 8081 -> container 8080.
- `8080:80` cho frontend: host port 8080 -> container 80.
- `depends_on`: frontend duoc tao sau backend (chi dam bao thu tu start, khong dam bao health).
- `networks`: hai service o chung bridge network de goi nhau qua ten service.

## 6) Cong thuc `docker-compose.registry.yml` (keo image theo tag)

```yaml
image: ${REGISTRY:-docker.io}/${NAMESPACE:-yourname}/dacongcu-backend:${TAG:-latest}
```

Y nghia:
- `${VAR:-default}` la syntax env-substitution cua Compose.
- `REGISTRY`: vi du `docker.io` hoac `ghcr.io`.
- `NAMESPACE`: username hoac org tren registry.
- `TAG`: version can deploy (`v1.0.0`, `latest`, `sha-xxxx`).

Vi du deploy:

```powershell
$env:REGISTRY="docker.io"
$env:NAMESPACE="manhsd2004"
$env:TAG="v1.0.0"
docker compose -f docker-compose.registry.yml up -d
```

## 7) Cong thuc tag image da ap dung

Tag strategy hien tai:
- Release tag: `v1.0.0` (immutable, uu tien cho production).
- Floating tag: `latest` (thuan tien demo, khong on dinh bang version).
- Optional commit tag: `sha-<gitsha>` (trace ve commit).

Mau image sau khi push:
- `docker.io/<namespace>/dacongcu-backend:v1.0.0`
- `docker.io/<namespace>/dacongcu-backend:latest`
- `docker.io/<namespace>/dacongcu-frontend:v1.0.0`
- `docker.io/<namespace>/dacongcu-frontend:latest`

## 8) Kiem tra nhanh sau khi up

1. Frontend: `http://localhost:8080`
2. Backend direct: `http://localhost:8081/api/v1/...`
3. API qua frontend proxy: `http://localhost:8080/api/v1/...`

Neu frontend len nhung API loi:
- Kiem tra container backend co chay khong.
- Kiem tra frontend cung network voi backend.
- Kiem tra `proxy_pass http://backend:8080;` trong Nginx config.
