# Infraestrutura GED

Dockerfile e Docker Compose para subir o **frontend**, o **backend** e o **MySQL 9**.

## Pré-requisitos

- Docker e Docker Compose instalados

## Build do backend (Maven)

A partir da raiz do projeto:

```bash
cd backend/java
mvn clean package -DskipTests
```

O JAR será gerado em `backend/java/target/ged-1.0.0.SNAPSHOT.jar`.

## Subir ambiente (frontend + backend + banco)

A partir da **raiz do projeto** (pasta que contém `backend/`, `frontend/` e `infra/`):

```bash
docker-compose -f infra/docker-compose.yml up -d --build
```

Ou, a partir da pasta `infra`:

```bash
cd infra
docker-compose up -d --build
```

(Se usar o segundo comando, o `context` no `docker-compose.yml` deve apontar para `..`.)

- **Frontend:** http://localhost
- **Backend:** http://localhost:8080
- **MySQL:** localhost:3306 (root / *SenhaSegur@92*)

## Migrations

As migrations (Flyway) rodam automaticamente quando o backend sobe. Não é necessário rodar manualmente.

## Executar testes

A partir da raiz do projeto:

```bash
cd backend/java
mvn test
```

## Parar ambiente

```bash
docker-compose -f infra/docker-compose.yml down
```

## Onde ficam os dados no disco

Os volumes no `docker-compose.yml` usam **caminhos absolutos no host**:

| Conteúdo        | Caminho no host (bind mount)   | Caminho no container      |
|-----------------|--------------------------------|----------------------------|
| Banco MySQL     | `/var/ged/data/mysql`          | `/var/lib/mysql`          |
| Arquivos do GED | `/var/ged/data/storage`        | `/var/ged/data/storage`   |

O serviço **frontend** não utiliza volumes; é stateless.

Em ambiente Linux, os dados ficam em `/var/ged/data/mysql` e `/var/ged/data/storage` no próprio host. Com Docker Desktop no Windows, esses caminhos referem-se ao sistema de arquivos da VM do Docker (por exemplo, no WSL2: `\\wsl$\docker-desktop-data\...`). Para persistir em uma pasta do projeto (ex.: `data/mysql` e `data/storage` na raiz), altere no `docker-compose.yml` os volumes para caminhos relativos, por exemplo: `../data/mysql:/var/lib/mysql` e `../data/storage:/var/ged/data/storage`.
