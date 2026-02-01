# GED — Sistema de Gerenciamento Eletrônico de Documentos (Backend)

API REST em Java 21 com Spring Boot 3, JPA e MySQL para gestão de documentos com metadados, versionamento de arquivos (PDF/PNG/JPG) e autenticação JWT.

## Pré-requisitos

- Java 21
- Maven 3.9+
- MySQL 9 (ou use Docker Compose em `backend/infra`)

## Setup

1. Clone o repositório e entre na pasta do backend:
   ```bash
   cd backend/java
   ```

2. Configure o banco MySQL:
   - Crie um banco `ged` (ou use `createDatabaseIfNotExist=true` na URL).
   - Usuário: `root`, senha: `*SenhaSegur@92*` (conforme especificação).

3. Crie o diretório de armazenamento de arquivos (ou a aplicação criará na primeira escrita):
   ```bash
   mkdir -p /var/ged/data/storage
   ```
   No Windows ou para desenvolvimento local, você pode sobrescrever em `application.yml`:
   ```yaml
   ged:
     storage:
       base-path: ./data/storage
   ```

## Build

```bash
mvn clean package -DskipTests
```

O JAR executável (fat JAR) será gerado em `target/ged-1.0.0.SNAPSHOT.jar`.

## Execução local

1. **Rodar migrations (Flyway):**  
   As migrations são executadas automaticamente na subida da aplicação.

2. **Iniciar a aplicação:**
   ```bash
   mvn spring-boot:run
   ```
   Ou:
   ```bash
   java -jar target/ged-1.0.0.SNAPSHOT.jar
   ```

3. **Primeiro acesso (setup):**  
   Se não existir nenhum usuário no banco, a aplicação exibirá na console a solicitação para configurar um usuário e senha de administrador. Informe usuário e senha; o usuário será criado com role ADMIN.

## Testes

```bash
mvn test
```

Há testes unitários no service layer (DocumentService, UserService, AuthService).

## Endpoints principais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/login` | Login (body: username, password) → retorna JWT |
| GET  | `/api/documents` | Lista documentos (paginação, filtros: title, status) |
| GET  | `/api/documents/{id}` | Detalhe do documento |
| POST | `/api/documents` | Criar documento (metadados) |
| PUT  | `/api/documents/{id}` | Atualizar metadados |
| PUT  | `/api/documents/{id}/publish` | Publicar (DRAFT → PUBLISHED) |
| PUT  | `/api/documents/{id}/archive` | Arquivar |
| GET  | `/api/documents/{id}/versions` | Listar versões do arquivo |
| POST | `/api/documents/{id}/versions` | Upload (multipart file) |
| GET  | `/api/documents/{id}/download` | Download da versão atual |
| GET  | `/api/documents/{id}/versions/{versionId}/download` | Download de versão específica |
| GET  | `/api/users` | Listar usuários (ADMIN) |
| POST | `/api/users` | Criar usuário (ADMIN) |
| PUT  | `/api/users/{id}` | Atualizar usuário (ADMIN) |

## Decisões técnicas

- **Flyway** para migrations do banco (schema em `src/main/resources/db/migration/`).
- **JWT** com jjwt; secret configurável via `ged.jwt.secret` (e variável de ambiente `JWT_SECRET`).
- **Arquivos** em filesystem em `/var/ged/data/storage` (configurável por `ged.storage.base-path`).
- **Documentos DRAFT** visíveis apenas para o owner; listagem e filtros já aplicam essa regra.
- **Setup do primeiro admin** via ApplicationRunner: se não houver usuários, solicita usuário/senha na console e cria um ADMIN.

## Limitações

- Armazenamento de arquivos é local (filesystem); não há S3 ou objeto distribuído.
- Ordenação na listagem de documentos usa parâmetro `sort` (ex.: `updatedAt,desc`).
- Tamanho máximo de upload: 20MB (configurável em `spring.servlet.multipart`).
