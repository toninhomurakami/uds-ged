# Arquitetura do sistema GED

Visão geral da arquitetura do **GED (Gerenciamento Eletrônico de Documentos)** e de como o sistema é disponibilizado com Docker.

---

## Backend

API REST em **Java 21** com **Spring Boot 3**, em camadas (controllers, services, repositories, domain, DTOs). Utiliza JWT para autenticação stateless, Spring Data JPA e Flyway sobre MySQL, e um serviço dedicado para armazenamento de arquivos em disco. Segurança (CORS, roles ADMIN/USER) e tratamento global de exceções estão centralizados na configuração e no `GlobalExceptionHandler`.

**Detalhes:** [ARQUITETURA_BACKEND.md](ARQUITETURA_BACKEND.md)

---

## Frontend

SPA em **Angular 21** com **TypeScript 5.9**, componentes standalone e estado reativo com signals no `AuthService`. Guards e interceptor funcionais protegem rotas e anexam o token JWT nas requisições. Serviços encapsulam o acesso à API; a UI usa Bootstrap 5 e formulários template-driven. A listagem de documentos permite ordenação clicando nos cabeçalhos das colunas (Título, Status, Proprietário, Atualizado).

**Detalhes:** [ARQUITETURA_FRONTEND.md](ARQUITETURA_FRONTEND.md)

---

## Docker e disponibilização

A aplicação pode ser executada inteiramente em **Docker** por meio do Compose em `infra/docker-compose.yml`, que sobe três serviços: **MySQL 9** (banco), **backend** (Spring Boot na porta 8080) e **frontend** (servido por nginx na porta 80).

Os scripts **`run.bat`** (Windows) e **`run.sh`** (Linux/macOS) na raiz do projeto executam `docker-compose -f infra/docker-compose.yml up --build`, construindo as imagens e subindo todos os containers. Com isso, o sistema fica disponível de forma integrada em **http://localhost**: o frontend é acessado nesse endereço e consome a API do backend em http://localhost:8080. Não é necessário instalar Java, Node ou MySQL na máquina; basta ter Docker e Docker Compose instalados.

Mais informações sobre a infraestrutura (pré-requisitos, volumes, parar ambiente): [../infra/README.md](../infra/README.md).
