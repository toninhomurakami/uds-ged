# Arquitetura do Backend – GED

Documento que descreve a arquitetura do backend do sistema **GED (Gerenciamento Eletrônico de Documentos)**.

---

## 1. Visão geral

O backend é uma API REST desenvolvida em **Java 21** com **Spring Boot 3.3.5**, seguindo uma arquitetura em camadas e padrões consolidados. Responsabilidades principais:

- Autenticação e autorização (JWT, roles ADMIN/USER)
- CRUD de usuários (admin) e setup inicial
- CRUD de documentos com versionamento e upload de arquivos (PDF, PNG, JPG)
- Persistência em MySQL com schema versionado por Flyway

---

## 2. Stack tecnológica

| Tecnologia        | Versão / Uso |
|-------------------|--------------|
| **Java**         | 21 (LTS)     |
| **Spring Boot**  | 3.3.5        |
| **Spring Web**   | REST API     |
| **Spring Data JPA** | Repositórios |
| **Spring Security** | JWT, filtros, autorização |
| **Spring Validation** | Bean Validation (Jakarta) |
| **MySQL**        | 9 (driver `mysql-connector-j`) |
| **Flyway**       | Migrations (flyway-core + flyway-mysql) |
| **JJWT**         | 0.12.5 (geração e validação de tokens JWT) |
| **Lombok**       | Redução de boilerplate |
| **Gson**         | Serialização JSON (onde aplicável) |
| **springdoc-openapi** | 2.6.0 – documentação OpenAPI 3 e Swagger UI |
| **Maven**        | Build e dependências |

O uso de **Jakarta EE** (jakarta.servlet, jakarta.persistence, jakarta.validation) está alinhado ao Spring Boot 3 e ao Java 21.

---

## 3. Arquitetura em camadas

O código está organizado em pacotes que refletem as camadas e responsabilidades:

```
br.com.uds.tools.ged
├── GedApplication.java          # Ponto de entrada (Spring Boot)
├── config/                      # Configuração da aplicação
│   ├── JwtProperties.java       # Propriedades JWT (ged.jwt.*)
│   ├── SecurityConfig.java      # Cadeia de segurança, CORS, PasswordEncoder
│   └── StorageProperties.java   # Caminho base de armazenamento (ged.storage.*)
├── domain/                      # Entidades JPA (modelo de domínio)
│   ├── User.java
│   ├── Document.java
│   ├── DocumentVersion.java
│   ├── DocumentStatus.java
│   └── Role.java
├── dto/                         # Objetos de transferência (request/response)
│   ├── LoginRequest.java, LoginResponse.java
│   ├── UserRequest.java, UserResponse.java
│   ├── DocumentRequest.java, DocumentResponse.java, DocumentVersionResponse.java
│   ├── PageResponse.java
│   └── InitialSetupRequest.java
├── repository/                   # Acesso a dados (Spring Data JPA)
│   ├── UserRepository.java
│   ├── DocumentRepository.java
│   └── DocumentVersionRepository.java
├── service/                     # Regras de negócio
│   ├── AuthService.java
│   ├── UserService.java
│   ├── DocumentService.java
│   └── FileDownload.java        # DTO para download (bytes + nome)
├── storage/                     # Armazenamento físico de arquivos
│   └── FileStorageService.java
├── security/                    # Autenticação e identidade
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   └── UserPrincipal.java      # Principal do Spring Security
└── web.controller/             # Controllers REST e tratamento global de erros
    ├── AuthController.java
    ├── SetupController.java
    ├── UserController.java
    ├── DocumentController.java
    └── GlobalExceptionHandler.java
```

- **Web (controller)**: recebe HTTP, valida entrada (`@Valid`), delega para serviços e devolve DTOs.
- **Service**: orquestra repositórios e `FileStorageService`, aplica regras de negócio e permissões.
- **Repository**: interfaces Spring Data JPA; sem lógica de negócio.
- **Domain**: entidades JPA; representam o modelo persistido.
- **DTO**: request/response da API; desacoplam contrato da API do domínio.
- **Config**: beans de configuração (segurança, CORS, propriedades customizadas).
- **Security**: geração/validação de JWT e filtro que preenche o contexto de segurança.
- **Storage**: gravação e leitura de arquivos em disco (base-path configurável).

---

## 4. Padrões utilizados

### 4.1 Arquitetura em camadas (Layered Architecture)

Separação clara entre apresentação (web), negócio (service), persistência (repository) e domínio (domain), com fluxo unidirecional: controller → service → repository.

### 4.2 Injeção de dependência (DI)

Uso de **constructor injection** via `@RequiredArgsConstructor` (Lombok) e `@Autowired` implícito no Spring. Controllers e serviços dependem de interfaces/implementações injetadas, o que facilita testes e troca de implementações.

### 4.3 Repository (Spring Data JPA)

Repositórios são interfaces que estendem `JpaRepository`. O Spring fornece a implementação; métodos derivados (ex.: `findByUsername`, `existsByUsername`) e `@Query` customizadas (ex.: `findAllFiltered` em `DocumentRepository`) definem o contrato de acesso a dados. A listagem de documentos usa o `Sort` do `Pageable` (sem ORDER BY fixo na query), permitindo ordenação por `title`, `status`, `owner.name` ou `updatedAt`.

### 4.4 DTO (Data Transfer Object)

Objetos em `dto/` representam o contrato da API: entrada (ex.: `LoginRequest`, `DocumentRequest`) e saída (ex.: `LoginResponse`, `DocumentResponse`, `PageResponse`). Evitam expor entidades JPA e permitem evoluir o modelo interno sem quebrar o contrato REST.

### 4.5 Filtro de requisições (Filter / Chain of Responsibility)

`JwtAuthenticationFilter` estende `OncePerRequestFilter`, atuando na cadeia de segurança do Spring. Ele lê o header `Authorization: Bearer <token>`, valida o JWT e preenche o `SecurityContext`. Rotas públicas (`/api/auth/`, `/api/setup/**`) são ignoradas via `shouldNotFilter`.

### 4.6 Configuração centralizada (Externalized Configuration)

Propriedades em `application.yml` (e `application-docker.yml` para o perfil `docker`): datasource, JPA, Flyway, JWT, storage, multipart. Valores sensíveis ou ambiente-específicos podem usar variáveis de ambiente (ex.: `JWT_SECRET`, `SPRING_PROFILES_ACTIVE`).

### 4.7 Tratamento global de exceções (Controller Advice)

`GlobalExceptionHandler` com `@RestControllerAdvice` centraliza o mapeamento de exceções para respostas HTTP:

- `IllegalArgumentException` → 400 + `message`
- `MethodArgumentNotValidException` (Bean Validation) → 400 + `message` + `errors` por campo
- `AccessDeniedException` → 403 + `message`

Controllers e serviços podem lançar exceções sem tratar manualmente em cada endpoint.

### 4.8 Princípio de responsabilidade única (SRP)

Cada classe tem um foco: controllers só orquestram chamadas e respostas; serviços contêm regras de negócio; repositórios apenas acesso a dados; `FileStorageService` apenas arquivos em disco; `JwtService` apenas tokens.

---

## 5. Uso do Spring Boot

- **Starters**: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation` fornecem configuração e dependências coerentes (servlet, JPA, segurança, Bean Validation).
- **Autoconfiguração**: DataSource, JPA/Hibernate, Flyway, segurança e servidor embutido são configurados com base em `application.yml` e no classpath, reduzindo código de configuração.
- **Perfis**: perfil `docker` (`application-docker.yml`) permite ajustar URL do banco e demais configs para o ambiente containerizado.
- **Beans de configuração**: `SecurityConfig` define `SecurityFilterChain`, CORS, `PasswordEncoder`; propriedades customizadas são lidas via `@ConfigurationProperties` (ex.: `JwtProperties`, `StorageProperties`).

---

## 6. Segurança

- **Stateless**: sessão não é mantida no servidor; cada requisição protegida deve enviar o JWT no header `Authorization: Bearer <token>`.
- **JWT**: tokens assinados (HMAC) contêm subject (username), `userId` e `role`; expiração configurável (`ged.jwt.expiration-ms`). Em caso de token inválido/expirado, o filtro responde 401 com corpo JSON.
- **Rotas públicas**: `/api/auth/login` e `/api/setup/**` não passam pelo filtro JWT e estão com `permitAll()`.
- **Autorização**: `/api/users/**` exige `ADMIN`; documentos (GET/POST/PUT/DELETE em `/api/documents` e `/api/documents/**`) exigem usuário autenticado; regras adicionais (ex.: dono do documento) são aplicadas nos serviços.
- **CORS**: configurado para permitir origens, métodos e headers necessários ao frontend; header `Content-Disposition` exposto para download com nome de arquivo.
- **Senhas**: armazenadas com BCrypt via `PasswordEncoder` definido em `SecurityConfig`.
- **CSRF**: desabilitado por se tratar de API stateless com JWT.

---

## 7. Persistência

- **JPA/Hibernate**: entidades em `domain/` com mapeamento ORM; `ddl-auto: validate` em produção para não alterar o schema (este fica a cargo do Flyway).
- **Flyway**: migrations em `src/main/resources/db/migration/` (ex.: `V1__schema.sql`) criam tabelas (`users`, `documents`, `document_tags`, `document_versions`) e índices. O schema é versionado e aplicado na subida da aplicação.
- **MySQL**: driver `mysql-connector-j`; URL, usuário e senha configuráveis; em testes, H2 em memória com `MODE=MySQL` permite rodar as mesmas migrations nos testes de repositório.
- **Transações**: serviços que alteram dados usam `@Transactional`; repositórios herdam o suporte transacional do Spring Data JPA.
- **open-in-view: false**: lazy loading fora de transação não é mantido; evita N+1 e deixa o uso de lazy explícito nos serviços.

---

## 8. Armazenamento de arquivos

- **FileStorageService** (`storage/`): grava arquivos enviados (multipart) em diretório configurável (`ged.storage.base-path`). Estrutura de chave: `documentId/versionId/nomeDoArquivo.ext`. Suporta extensões permitidas (ex.: .pdf, .png, .jpg, .jpeg); validação de extensão e sanitização de nome para evitar path traversal.
- **Integração com domínio**: `DocumentVersion` guarda `fileKey`; o serviço de documentos orquestra criação de versão, chamada ao `FileStorageService` e atualização do `fileKey` após o upload. Download usa o mesmo serviço para resolver path e stream de bytes.
- **Perfil docker**: o caminho de storage pode ser mapeado para volume no container (ex.: `/var/ged/data/storage`).

---

## 9. API REST – convenções

- **Base path**: `/api`.
- **Subcaminhos**: `/api/auth` (login), `/api/setup` (status e criação do admin inicial), `/api/users` (CRUD de usuários, ADMIN), `/api/documents` (CRUD e versionamento de documentos).
- **Verbos HTTP**: GET (consulta), POST (criação/login), PUT (atualização), DELETE (remoção).
- **Formato**: JSON para request e response; validação com Bean Validation (`@Valid`, anotações em DTOs).
- **Paginação**: listagem de documentos usa `Pageable` e resposta paginada (ex.: `PageResponse`).
- **Ordenação**: a listagem de documentos aceita o parâmetro `sort` (ex.: `title,asc`, `updatedAt,desc`); propriedades suportadas: `title`, `status`, `owner.name`, `updatedAt`.
- **Multipart**: upload de versões de documento via `multipart/form-data`; limites configurados em `spring.servlet.multipart`.
- **Documentação OpenAPI/Swagger**: springdoc-openapi expõe o spec em `/v3/api-docs` e a interface Swagger UI em `/swagger-ui.html` (ou `/swagger-ui/index.html`). A autenticação JWT pode ser informada na UI via botão "Authorize" para testar endpoints protegidos.

---

## 10. Testes

- **Testes de unidade**: serviços (ex.: `AuthService`, `UserService`, `DocumentService`), `JwtService`, `JwtAuthenticationFilter`, `FileStorageService`, e controllers (ex.: `SetupController`) com mocks (Mockito) e, quando aplicável, `@WebMvcTest` ou contexto mínimo.
- **Testes de integração de repositório**: `UserRepositoryTest`, `DocumentRepositoryTest`, `DocumentVersionRepositoryTest` com `@SpringBootTest`, H2 em memória (MODE=MySQL) e Flyway aplicando as migrations de `db/migration/`, garantindo que o schema e os repositórios funcionem em conjunto.
- **Configuração de teste**: `src/test/resources/application.yml` define DataSource H2 e dialect para os testes que precisam de banco; nenhuma alteração no código de produção é necessária para rodar esses testes.

---

## 11. Resumo

O backend do GED utiliza **Java 21** e **Spring Boot 3** em uma arquitetura em camadas (web, service, repository, domain, dto, config, security, storage), com padrões como **Repository**, **DTO**, **DI**, **Filter** para JWT e **Controller Advice** para erros. A segurança é **stateless** com **JWT**; a persistência usa **JPA** e **Flyway** sobre **MySQL**; o armazenamento de arquivos é isolado em um serviço dedicado e configurável. A combinação de Spring Boot, propriedades externalizadas e testes com H2/Flyway permite evoluir o sistema mantendo clareza e testabilidade.
