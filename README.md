# uds-ged

Projeto para Gestão Eletrônica de Documentos.

---

## Uso rápido

### Pré-requisitos

- **Docker** e **Docker Compose** instalados na máquina.

### Subir o sistema

Use um dos scripts na raiz do projeto para carregar banco de dados, backend e frontend em containers:

| Plataforma | Comando |
|------------|---------|
| **Linux / macOS** | `./run.sh` |
| **Windows** | `run.bat` |

O script executa `docker-compose -f infra/docker-compose.yml up --build`, que sobe:

- **MySQL** (porta 3306) – banco de dados
- **Backend** (porta 8080) – API REST (Spring Boot)
- **Frontend** (porta 80) – interface web (Angular)

### Primeiro acesso

1. Aguarde os containers subirem (a primeira vez pode demorar por causa do build).
2. No navegador, acesse: **http://localhost**
3. Como não há usuários ainda, será exibida a tela de **cadastro do primeiro administrador**. Preencha Nome, Login e Senha e conclua o setup.
4. Em seguida, faça **login** com o usuário criado.

Para o uso completo do sistema (login, cadastro de usuários, documentos, versionamento, etc.), **recomenda-se a leitura do [Guia do Usuário](docs/GUIA_USUARIO.md)** em `docs/`. O guia traz telas, passos e orientações para todas as funcionalidades.

---

## Documentação

A documentação do projeto está na pasta [`docs/`](docs/):

| Documento | Descrição |
|-----------|-----------|
| [ARQUITETURA.md](docs/ARQUITETURA.md) | Visão geral da arquitetura do sistema |
| [ARQUITETURA_BACKEND.md](docs/ARQUITETURA_BACKEND.md) | Arquitetura e detalhes do backend (Java/Spring) |
| [ARQUITETURA_FRONTEND.md](docs/ARQUITETURA_FRONTEND.md) | Arquitetura e detalhes do frontend (Angular) |
| **[GUIA_USUARIO.md](docs/GUIA_USUARIO.md)** | **Guia do usuário – uso do sistema (recomendado)** |
| [GUIA_USUARIO.html](docs/GUIA_USUARIO.html) | Guia do usuário em HTML |
