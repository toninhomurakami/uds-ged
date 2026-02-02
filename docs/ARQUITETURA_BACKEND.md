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

O código está organizado em pacotes que refletem as camadas e responsabilidades. O fluxo de chamadas é: **controller → facade → service(s) → repository**.

```
br.com.uds.tools.ged
├── GedApplication.java          # Ponto de entrada (Spring Boot)
├── config/                      # Configuração da aplicação
│   ├── JwtProperties.java       # Propriedades JWT (ged.jwt.*)
│   ├── OpenApiConfig.java       # Configuração OpenAPI/Swagger
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
├── repository/                  # Acesso a dados (Spring Data JPA)
│   ├── UserRepository.java
│   ├── DocumentRepository.java
│   └── DocumentVersionRepository.java
├── facade/                      # Camada intermediária entre controller e services
│   ├── UserFacade.java
│   ├── DocumentFacade.java
│   ├── FileStorageFacade.java
│   └── impl/
│       ├── UserFacadeImpl.java
│       ├── DocumentFacadeImpl.java
│       └── FileStorageFacadeImpl.java
├── service/                     # Regras de negócio (uma classe por caso de uso)
│   ├── AuthService.java
│   ├── FileDownload.java        # DTO para download (bytes + nome)
│   ├── user/                    # Serviços de usuário
│   │   ├── AbstractUserService.java
│   │   ├── CountUserService.java, FindAllUserService.java, FindByIdUserService.java
│   │   ├── GetByUsernameUserService.java
│   │   ├── CreateUserService.java, CreateInitialAdminUserService.java
│   │   ├── UpdateUserService.java, DeleteByIdUserService.java
│   │   └── ...
│   ├── document/                # Serviços de documento
│   │   ├── AbstractDocumentService.java
│   │   ├── FindAllDocumentService.java, FindByIdDocumentService.java
│   │   ├── CreateDocumentService.java, UpdateDocumentService.java, DeleteByIdDocumentService.java
│   │   ├── PublishDocumentService.java, ArchiveDocumentService.java
│   │   ├── ListVersionsDocumentService.java, UploadVersionDocumentService.java
│   │   ├── DownloadCurrentDocumentService.java, DownloadVersionDocumentService.java
│   │   └── ...
│   └── storage/file/            # Serviços de armazenamento físico
│       ├── AbstractStorageService.java
│       ├── PersistFileStoreService.java
│       └── DeleteByFileKeyStorageService.java
├── security/                    # Autenticação e identidade
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   └── UserPrincipal.java       # Principal do Spring Security
└── web.controller/              # Controllers REST (um controller por recurso/ação)
    ├── AuthController.java
    ├── SetupController.java
    ├── GlobalExceptionHandler.java
    ├── user/                    # CRUD de usuários (ADMIN)
    │   ├── CreateUserController.java, ListUserController.java, GetUserController.java
    │   ├── UpdateUserController.java, DeleteUserController.java
    │   └── ...
    └── document/                # CRUD e versionamento de documentos
        ├── CreateDocumentController.java, ListDocumentController.java, GetDocumentController.java
        ├── UpdateDocumentController.java, DeleteDocumentController.java
        ├── DownloadDocumentController.java, UploadDocumentController.java
        └── ...
```

- **Web (controller)**: recebe HTTP, valida entrada (`@Valid`), delega para a **facade** e devolve DTOs. Cada controller é responsável por uma ação ou conjunto pequeno de endpoints (ex.: `CreateUserController` apenas POST de usuário).
- **Facade**: camada intermediária que orquestra um ou mais **services**; expõe uma API estável para os controllers e centraliza transações (`@Transactional`) quando necessário. Controllers dependem das interfaces (ex.: `UserFacade`, `DocumentFacade`), não dos services diretamente.
- **Service**: classes pequenas, uma por caso de uso (ex.: `FindByIdUserService`, `CreateDocumentService`). Estendem classes abstratas (`AbstractUserService`, `AbstractDocumentService`) para reutilizar mapeamento para DTO e acesso ao usuário corrente. Orquestram repositórios e, no caso de documentos, a `FileStorageFacade` para arquivos.
- **Repository**: interfaces Spring Data JPA; sem lógica de negócio.
- **Domain**: entidades JPA; representam o modelo persistido.
- **DTO**: request/response da API; desacoplam contrato da API do domínio.
- **Config**: beans de configuração (segurança, CORS, OpenAPI, propriedades customizadas).
- **Security**: geração/validação de JWT e filtro que preenche o contexto de segurança.
- **Storage (service.storage.file + FileStorageFacade)**: gravação e leitura de arquivos em disco (base-path configurável); a facade abstrai os serviços de persistência e exclusão por `fileKey`.

---

## 4. Padrões utilizados

### 4.1 Arquitetura em camadas (Layered Architecture)

Separação clara entre apresentação (web), facade, negócio (service), persistência (repository) e domínio (domain), com fluxo unidirecional: **controller → facade → service → repository**. A camada **facade** desacopla os controllers dos serviços concretos e centraliza orquestração e transações.

### 4.2 Injeção de dependência (DI)

Uso de **constructor injection** via `@RequiredArgsConstructor` (Lombok) e `@Autowired` implícito no Spring. Controllers dependem das **interfaces de facade** (ex.: `UserFacade`, `DocumentFacade`); facades dependem dos **services** concretos (ex.: `FindByIdUserService`, `CreateDocumentService`); services dependem dos **repositories**. Essa cadeia facilita testes (mocks nas interfaces) e troca de implementações.

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

### 4.8 Facade

As **facades** (`UserFacade`, `DocumentFacade`, `FileStorageFacade`) expõem uma API estável para os controllers e delegam para um ou mais services. Reduzem o acoplamento entre web e regras de negócio e concentram transações (`@Transactional`) e orquestração (ex.: exclusão de documento com remoção de arquivos em disco em `DocumentFacadeImpl.deleteById`).

### 4.9 Princípio de responsabilidade única (SRP)

Cada classe tem um foco: **controllers** tratam um recurso/ação (ex.: `CreateUserController` só POST de usuário); **facades** orquestram services e transações; **services** implementam um caso de uso (ex.: `FindByIdUserService` só busca por ID); repositórios apenas acesso a dados; serviços de storage apenas arquivos em disco; `JwtService` apenas tokens.

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

- **FileStorageFacade** (`facade/`): interface que expõe `store`, `deleteByFileKey` e `resolve`; a implementação delega para os services de storage (`PersistFileStoreService`, `DeleteByFileKeyStorageService`) e usa `StorageProperties` para o caminho base.
- **Services de storage** (`service/storage/file/`): **PersistFileStoreService** grava arquivos enviados (multipart) em diretório configurável (`ged.storage.base-path`). Estrutura de chave: `documentId/versionId/nomeDoArquivo.ext`. Suporta extensões permitidas (ex.: .pdf, .png, .jpg, .jpeg); validação de extensão e sanitização de nome em `AbstractStorageService`. **DeleteByFileKeyStorageService** remove o arquivo físico referenciado pelo `fileKey`.
- **Integração com domínio**: `DocumentVersion` guarda `fileKey`; a facade de documentos orquestra criação de versão (via `UploadVersionDocumentService`), chamada ao `FileStorageFacade.store` e atualização do `fileKey`. Download usa `FileStorageFacade.resolve` para obter o path e ler os bytes.
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

- **Testes de unidade de services**: cada service (ex.: `CountUserService`, `CreateDocumentService`, `FindByIdDocumentService`, `PersistFileStoreService`) é testado com `@ExtendWith(MockitoExtension.class)`, mocks dos repositórios e, quando usam `SecurityContextHolder`, configuração de `UserPrincipal` em `@BeforeEach`. Serviços de documento que estendem `AbstractDocumentService` usam `@MockitoSettings(strictness = Strictness.LENIENT)` quando alguns testes não utilizam o contexto de segurança.
- **Testes de unidade de facades**: `UserFacadeImplTest`, `DocumentFacadeImplTest`, `FileStorageFacadeImplTest` verificam que as facades delegam corretamente para os services injetados.
- **Testes de unidade de controllers**: cada controller (ex.: `CreateUserController`, `ListDocumentController`, `SetupController`) é testado com `@WebMvcTest`, `@AutoConfigureMockMvc(addFilters = false)` e `@MockBean` para a facade e para `JwtService` (necessário ao subir o contexto). Requisições são simuladas com `MockMvc` e respostas verificadas com `jsonPath` e status HTTP.
- **Testes de serviços e filtros**: `AuthService`, `JwtService`, `JwtAuthenticationFilter` continuam com testes dedicados; configuração mínima ou mocks conforme o caso.
- **Testes de integração de repositório**: `UserRepositoryTest`, `DocumentRepositoryTest`, `DocumentVersionRepositoryTest` com `@SpringBootTest`, H2 em memória (MODE=MySQL) e Flyway aplicando as migrations de `db/migration/`, garantindo que o schema e os repositórios funcionem em conjunto.
- **Configuração de teste**: `src/test/resources/application.yml` define DataSource H2 e dialect para os testes que precisam de banco; nenhuma alteração no código de produção é necessária para rodar esses testes.

---

## 11. Resumo

O backend do GED utiliza **Java 21** e **Spring Boot 3** em uma arquitetura em camadas (web, **facade**, service, repository, domain, dto, config, security, storage), com padrões como **Facade**, **Repository**, **DTO**, **DI**, **Filter** para JWT e **Controller Advice** para erros. Os **controllers** e **services** foram refatorados em classes menores (um controller por recurso/ação, um service por caso de uso); a camada **facade** orquestra os services e centraliza transações. A segurança é **stateless** com **JWT**; a persistência usa **JPA** e **Flyway** sobre **MySQL**; o armazenamento de arquivos é abstraído pela **FileStorageFacade** e implementado por services em `service/storage/file/`. A combinação de Spring Boot, propriedades externalizadas e testes (unitários de service, facade e controller, além de integração de repositório com H2/Flyway) permite evoluir o sistema mantendo clareza e testabilidade.
