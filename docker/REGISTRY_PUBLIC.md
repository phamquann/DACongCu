# Public Registry Publishing (Tag Strategy)

This project is split into 2 images:
- `backend`: Spring Boot API
- `frontend`: Nginx static web + reverse proxy `/api/*` to backend

## 1) Login Registry

Docker Hub:

```powershell
docker login docker.io
```

GHCR:

```powershell
docker login ghcr.io
```

## 2) Build + Tag + Push (multiple tags)

Run in workspace root:

```powershell
./docker/publish-public.ps1 -Namespace <your-namespace> -Registry docker.io -VersionTag v1.0.0 -PushLatest -IncludeShaTag
```

Example output tags:
- `docker.io/<your-namespace>/dacongcu-backend:v1.0.0`
- `docker.io/<your-namespace>/dacongcu-backend:latest`
- `docker.io/<your-namespace>/dacongcu-backend:sha-abc1234`
- `docker.io/<your-namespace>/dacongcu-frontend:v1.0.0`
- `docker.io/<your-namespace>/dacongcu-frontend:latest`
- `docker.io/<your-namespace>/dacongcu-frontend:sha-abc1234`

## 3) Pull and Run by tag

Set env vars then run compose file for registry images:

```powershell
$env:REGISTRY = "docker.io"
$env:NAMESPACE = "<your-namespace>"
$env:TAG = "v1.0.0"
docker compose -f docker-compose.registry.yml up -d
```

Access:
- Frontend: `http://localhost:8080`
- Backend direct: `http://localhost:8081`

## Notes
- Use immutable release tags (`v1.0.0`) for stable deployments.
- Keep `latest` only as convenience tag.
- Add `-NoCache` when you need a fully clean build.