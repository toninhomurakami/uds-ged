# Arquitetura do Frontend – GED

Documento que descreve a arquitetura do frontend do sistema **GED (Gerenciamento Eletrônico de Documentos)**.

---

## 1. Visão geral

O frontend é uma **Single Page Application (SPA)** desenvolvida em **Angular 21** com **TypeScript 5.9**, consumindo a API REST do backend. Responsabilidades principais:

- Fluxo de setup inicial (primeiro acesso) e login
- Navegação autenticada com layout comum (navbar + outlet)
- CRUD de documentos (listagem, criação, edição, detalhe, exclusão, publicação/arquivamento)
- Versionamento e upload/download de arquivos (PDF, PNG, JPG)
- CRUD de usuários (apenas para role ADMIN)
- Autenticação JWT e proteção de rotas por guardas

---

## 2. Stack tecnológica

| Tecnologia        | Versão / Uso |
|-------------------|--------------|
| **Angular**       | 21.1.x       |
| **TypeScript**    | 5.9.x        |
| **RxJS**          | 7.8.x        |
| **Angular Router**| Roteamento e lazy loading de rotas |
| **Angular Forms** | Template-driven (FormsModule) e binding bidirecional |
| **Angular HttpClient** | Chamadas à API e interceptors |
| **Bootstrap**     | 5.3.8 (CSS e JS via CDN em `index.html`) |
| **Prettier**      | Formatação (printWidth 100, singleQuote) |
| **npm**           | Gerenciador de pacotes (packageManager 11.4.2) |

O projeto usa **standalone components** (sem módulos NgModule), **signals** para estado reativo e **functional guards/interceptors** (funções em vez de classes), alinhados ao estilo moderno do Angular 16+.

---

## 3. Estrutura do projeto

```
frontend/src/
├── index.html                 # HTML raiz; carrega Bootstrap (CSS + JS)
├── main.ts                    # Bootstrap da aplicação (bootstrapApplication)
├── styles.css                 # Estilos globais (complementam Bootstrap)
└── app/
    ├── app.ts                 # Componente raiz (selector app-root)
    ├── app.html               # Template: <router-outlet>
    ├── app.css
    ├── app.config.ts          # Configuração: router, HttpClient, interceptors
    ├── app.routes.ts          # Definição de rotas e guards
    ├── core/
    │   └── api.ts             # Constante API_URL (base da API)
    ├── guards/
    │   ├── auth.guard.ts      # Protege rotas que exigem login
    │   └── admin.guard.ts     # Exige role ADMIN (ex.: /app/users)
    ├── interceptors/
    │   └── auth.interceptor.ts # Adiciona header Authorization: Bearer <token>
    ├── services/
    │   ├── auth.service.ts    # Login, logout, estado do usuário (signals)
    │   ├── document.service.ts # CRUD documentos, versões, download
    │   ├── setup.service.ts   # Status do setup e criação do admin inicial
    │   └── user.service.ts   # CRUD usuários
    └── components/
        ├── layout/            # Layout autenticado: navbar + <router-outlet>
        ├── login/             # Tela de login
        ├── initial-setup/     # Tela de cadastro do primeiro admin
        ├── document-list/     # Listagem paginada, filtros e ordenação por coluna
        ├── document-create/   # Criação de documento
        ├── document-detail/   # Detalhe, edição, versões, upload/download
        └── user-list/         # Listagem e CRUD de usuários (ADMIN)
```

- **core**: configuração compartilhada (ex.: URL da API).
- **guards**: funções `CanActivateFn` que decidem se a rota pode ser ativada (login, role).
- **interceptors**: função `HttpInterceptorFn` que anexa o JWT em todas as requisições HTTP.
- **services**: serviços injetáveis (`providedIn: 'root'`) que encapsulam chamadas HTTP e estado (AuthService com signals).
- **components**: componentes standalone por funcionalidade; cada um com `.ts`, `.html` e `.css`.

---

## 4. Rotas e navegação

Definidas em `app.routes.ts`:

| Rota            | Componente          | Guardas    | Descrição |
|-----------------|---------------------|------------|-----------|
| `''`            | InitialSetupComponent| —          | Setup inicial (primeiro admin); redireciona se já configurado |
| `login`         | LoginComponent      | —          | Login; redireciona para `/app/documents` se já logado |
| `app`           | LayoutComponent     | authGuard  | Área autenticada: navbar + filhos |
| `app` (default) | —                   | —          | Redirect para `app/documents` |
| `app/documents` | DocumentListComponent | —        | Lista de documentos |
| `app/documents/new` | DocumentCreateComponent | —     | Novo documento |
| `app/documents/:id` | DocumentDetailComponent | —     | Detalhe/edição de documento |
| `app/users`     | UserListComponent   | authGuard, adminGuard | Lista de usuários (ADMIN) |
| `**`            | —                   | —          | Redirect para `''` |

- **authGuard**: verifica `AuthService.isLoggedIn()`; se falso, redireciona para `/login`.
- **adminGuard**: verifica `AuthService.isAdmin()`; se falso, redireciona para `/app/documents`.
- Layout com **children**: todas as rotas sob `app` compartilham o mesmo layout (navbar + conteúdo no `<router-outlet>`).

---

## 5. Padrões utilizados

### 5.1 Componentes standalone

Cada componente é autocontido com `standalone: true` e declara apenas os `imports` necessários (CommonModule, FormsModule, RouterLink, etc.), sem depender de NgModules. Facilita tree-shaking e manutenção.

### 5.2 Serviços como camada de acesso à API

Os **services** concentram as chamadas HTTP (HttpClient) e expõem métodos que retornam `Observable<T>`. Os componentes subscrevem e atualizam a UI (signals ou propriedades). Contratos da API são representados por interfaces TypeScript (ex.: `Document`, `PageResponse`, `LoginResponse`).

### 5.3 Estado reativo com Signals (Angular 21)

O **AuthService** usa **signals** (`signal`, `computed`) para token e usuário: `token()`, `user()`, `isLoggedIn`, `isAdmin`. A UI e os guards reagem a essas leituras. O estado é persistido em `localStorage` e reidratado na inicialização.

### 5.4 Guards funcionais (CanActivateFn)

`authGuard` e `adminGuard` são funções que usam `inject()` para obter `AuthService` e `Router`, e retornam `true`/`false` ou redirecionam. Padrão recomendado no Angular moderno em vez de classes.

### 5.5 Interceptor funcional (HttpInterceptorFn)

`authInterceptor` usa `inject(AuthService)` para obter o token e clona a requisição com `Authorization: Bearer <token>`. Aplicado globalmente via `provideHttpClient(withInterceptors([authInterceptor]))` em `app.config.ts`.

### 5.6 Layout + rotas filhas

**LayoutComponent** fornece a estrutura comum (navbar com links e botão Sair); o conteúdo varia conforme a rota filha (`<router-outlet />`). Evita repetir navbar em cada tela autenticada.

### 5.7 Tratamento de erros na UI

Componentes tratam erros nas subscrições (callback `error`) e exibem mensagens ao usuário (ex.: `err?.error?.message` ou texto fixo). Não há um interceptador global de erro HTTP; cada fluxo decide a mensagem.

### 5.8 Configuração centralizada da API

A base da URL da API está em `core/api.ts` (`API_URL`). Serviços importam essa constante; alterar o ambiente implica apenas um ponto de configuração (ou uso de environment/arquivo de config no build).

---

## 6. Uso do Angular

- **Bootstrap da aplicação**: `main.ts` chama `bootstrapApplication(App, appConfig)`; a configuração (router, HttpClient, interceptors) está em `app.config.ts` com `provideRouter`, `provideHttpClient`, `provideBrowserGlobalErrorListeners`.
- **Roteamento**: `RouterOutlet` no componente raiz e no layout; `RouterLink` e `RouterLinkActive` nos links da navbar.
- **Formulários**: uso de `FormsModule` com `ngModel`, `ngModelChange`, validação em template e submissão com `(ngSubmit)`.
- **HttpClient**: injetado nos serviços; requisições com GET/POST/PUT/DELETE, parâmetros de query (`HttpParams`), `responseType: 'blob'` e leitura de `Content-Disposition` para downloads.
- **Pipes**: `date` (formato brasileiro em listas/detalhe) e `async` quando aplicável; uso de `NgClass` para classes condicionais (ex.: status do documento).
- **Strict mode**: TypeScript e Angular com opções strict (strictTemplates, strictInjectionParameters, etc.) em `tsconfig.json` e `angularCompilerOptions`.

---

## 7. Autenticação e autorização

- **Login**: formulário envia credenciais para `POST /api/auth/login`; em sucesso, o backend devolve token e dados do usuário. O **AuthService** grava token e usuário em `localStorage` e atualiza os signals.
- **Persistência**: chaves `ged_token` e `ged_user` em `localStorage`; ao recarregar a página, o serviço reidrata o estado a partir delas.
- **Envio do token**: o **authInterceptor** adiciona `Authorization: Bearer <token>` em toda requisição HTTP; rotas públicas (login, setup) também recebem o header se houver token, mas o backend ignora para esses endpoints.
- **Proteção de rotas**: `authGuard` bloqueia acesso a `/app/**` se não logado; `adminGuard` bloqueia `/app/users` se não for ADMIN.
- **UI condicional**: o layout usa `auth.isAdmin()` para exibir ou ocultar o link "Usuários" e exibe `auth.user()?.username` e `role` na navbar.
- **Logout**: limpa `localStorage` e signals e redireciona para `/login`.

---

## 8. Comunicação com o backend

- **Base URL**: `http://localhost:8080/api` (constante em `core/api.ts`). Em produção ou Docker, pode ser alterada por variável de ambiente ou build.
- **Formato**: JSON para request e response; upload de arquivo via `FormData` (`multipart/form-data`).
- **Download**: requisições com `responseType: 'blob'` e `observe: 'response'` para ler o header `Content-Disposition` e obter o nome do arquivo; criação de link temporário ou objeto URL para download no navegador.
- **Paginação**: listagem de documentos usa parâmetros `page`, `size`, `sort` e opcionalmente `title`, `status`; resposta no formato `PageResponse<Document>` (content, totalElements, totalPages, etc.).
- **Ordenação**: na lista de documentos, os cabeçalhos das colunas (Título, Status, Proprietário, Atualizado) são clicáveis; ao clicar, a lista é reordenada (ascendente/descendente) e o parâmetro `sort` é enviado à API; a coluna e a direção atuais são indicadas por ícone (▲/▼).
- **Tratamento de erro**: em respostas 401/403, o backend pode retornar JSON com `message`; componentes e serviços usam `err?.error?.message` quando disponível.
- **Documentação da API**: o backend expõe a documentação OpenAPI/Swagger em **http://localhost:8080/swagger-ui/index.html** (com o backend em execução). Nessa interface é possível consultar todos os endpoints, schemas e testar requisições (incluindo autenticação JWT via botão "Authorize").

---

## 9. UI e estilos

- **Bootstrap 5.3.8**: carregado via CDN em `index.html` (CSS e JS). Componentes usam classes como `container-fluid`, `navbar`, `btn`, `form-control`, `table`, `modal`, `badge`, etc.
- **Estilos globais**: `styles.css` complementa o Bootstrap (ex.: `.cursor-pointer`, `.navbar .nav-link.active`).
- **Estilos por componente**: cada componente pode ter arquivo `.css` próprio (styleUrl no decorator); escopo é do componente.
- **Acessibilidade**: uso de labels, botões e estrutura semântica; navbar responsiva com toggler para mobile.
- **Formatação de dados**: datas com pipe `date` (ex.: `dd/MM/yyyy HH:mm:ss`); status de documento com cores por classe (DRAFT: amarelo, PUBLISHED: verde, ARCHIVED: vermelho).

---

## 10. Build e testes

- **Build**: `ng build` (produção por padrão, com output hashing e budgets configurados em `angular.json`); `ng build --configuration development` para desenvolvimento (source maps, sem otimização agressiva).
- **Servidor de desenvolvimento**: `ng serve`; a aplicação consome a API em `http://localhost:8080/api` (configurável).
- **Testes**: script `ng test` (Karma/Jasmine configurados pelo Angular CLI); schematics do projeto definem `skipTests: true` para novos componentes/guards/services, mas a estrutura suporta testes unitários e de integração.
- **Prettier**: configuração no `package.json` (printWidth 100, singleQuote, parser angular para HTML) para formatação consistente.

---

## 11. Resumo

O frontend do GED é uma SPA em **Angular 21** com **TypeScript 5.9**, usando **componentes standalone**, **signals** no AuthService, **guards e interceptors funcionais**, e **Bootstrap 5** para a UI. A arquitetura separa claramente rotas, guards, interceptors, serviços (acesso à API e estado de autenticação) e componentes de tela, com configuração centralizada da API e proteção de rotas por login e role. A comunicação com o backend é REST (JSON e multipart para upload/download), com token JWT enviado via interceptor e estado de login persistido em localStorage e reidratado ao carregar a aplicação.
