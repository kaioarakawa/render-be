# API Senac — Spring Boot + PostgreSQL + Render

Backend de exemplo para a aula de **deploy de API Java na nuvem**.

| Item | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3 |
| Banco | PostgreSQL 16 |
| Migrations | Flyway |
| Build | Maven (wrapper incluso) |
| Deploy | Render (Docker + Blueprint) |
| Docs | Swagger UI (springdoc) |

---

## 1. Estrutura do projeto

```
render-BE/
├── Dockerfile              # build multi-stage (Maven -> JRE)
├── render.yaml             # Blueprint: cria banco + web service no Render
├── docker-compose.yml      # Postgres local (+ API opcional)
├── pom.xml
└── src/main/
    ├── java/br/senac/api/
    │   ├── ApiApplication.java
    │   ├── config/         # CORS, OpenAPI, rota raiz
    │   ├── curso/          # Entity, Repository, Service, Controller, DTOs
    │   ├── aluno/          # idem, com FK para curso
    │   └── exception/      # tratamento global de erros
    └── resources/
        ├── application.yml
        └── db/migration/   # V1, V2, V3 (Flyway)
```

Cada domínio segue a mesma pilha de camadas:

```
Controller  -> recebe HTTP, valida entrada (@Valid), devolve DTO
Service     -> regra de negócio, @Transactional
Repository  -> acesso ao banco (Spring Data JPA)
Entity      -> mapeia a tabela criada pela migration
```

---

## 2. Rodar localmente

### Opção A — tudo em Docker (mais simples)

```bash
docker compose up --build
```

API em http://localhost:8080

### Opção B — banco no Docker, API na IDE

```bash
docker compose up -d db
./mvnw spring-boot:run
```

Variáveis usadas pela aplicação (todas têm default para ambiente local):

| Variável | Default | Descrição |
|---|---|---|
| `PGHOST` | `localhost` | host do Postgres |
| `PGPORT` | `5432` | porta |
| `PGDATABASE` | `senac` | nome do banco |
| `PGUSER` | `postgres` | usuário |
| `PGPASSWORD` | `postgres` | senha |
| `PORT` | `8080` | porta HTTP (o Render injeta essa variável) |

> **Porta ocupada?** Se já houver outro Postgres ou outra aplicação em `5432`/`8080`, troque o lado esquerdo do mapeamento no `docker-compose.yml` (ex.: `"5433:5432"` e `"8081:8080"`).

### Testes

```bash
./mvnw test
```

Os testes usam banco H2 em memória (perfil `test`), sem precisar de Docker.

---

## 3. Endpoints

Documentação interativa: `GET /swagger-ui.html`

### Cursos

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/cursos` | lista cursos (`?nome=java` filtra por nome) |
| GET | `/api/cursos/{id}` | busca por id |
| POST | `/api/cursos` | cria curso |
| PUT | `/api/cursos/{id}` | atualiza curso |
| DELETE | `/api/cursos/{id}` | remove curso |

### Alunos

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/alunos` | lista alunos (`?cursoId=2` filtra por curso) |
| GET | `/api/alunos/{id}` | busca por id |
| POST | `/api/alunos` | matricula aluno em um curso |
| DELETE | `/api/alunos/{id}` | remove aluno |

### Infra

| Método | Rota | Descrição |
|---|---|---|
| GET | `/` | status da aplicação |
| GET | `/actuator/health` | health check (usado pelo Render) |

### Exemplos

```bash
# listar
curl http://localhost:8080/api/cursos

# criar curso
curl -X POST http://localhost:8080/api/cursos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Docker na prática","descricao":"Containers do zero","cargaHoraria":40}'

# matricular aluno
curl -X POST http://localhost:8080/api/alunos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Ana Souza","email":"ana@exemplo.com","cursoId":2}'

# erro de validação (400)
curl -X POST http://localhost:8080/api/cursos \
  -H "Content-Type: application/json" -d '{"nome":"","cargaHoraria":0}'
```

Respostas de erro seguem o formato:

```json
{
  "timestamp": "2026-10-19T12:00:00Z",
  "status": 400,
  "mensagem": "Erro de validação",
  "campos": { "nome": "nome é obrigatório" }
}
```

---

## 4. Migrations (Flyway)

Arquivos em `src/main/resources/db/migration/`:

| Arquivo | O que faz |
|---|---|
| `V1__criar_tabela_cursos.sql` | cria `cursos` + índice |
| `V2__criar_tabela_alunos.sql` | cria `alunos` com FK para `cursos` |
| `V3__inserir_dados_iniciais.sql` | dados de exemplo (seed) |

Regras importantes:

1. Nome do arquivo: `V<numero>__<descricao>.sql` (dois underscores).
2. **Migration aplicada nunca é editada.** Para mudar algo, crie `V4__...sql`.
3. O Flyway cria a tabela `flyway_schema_history` e registra cada versão + checksum.
4. `spring.jpa.hibernate.ddl-auto=validate`: o Hibernate **não** cria tabelas, apenas confere se as entidades batem com o schema criado pelas migrations. Se divergir, a aplicação não sobe — é o comportamento desejado em produção.

Checar o que foi aplicado:

```sql
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
```

---

## 5. Deploy no Render

### Pré-requisitos
- Código em um repositório no GitHub.
- Conta gratuita em https://render.com.

### Caminho A — Blueprint (usa o `render.yaml`, recomendado)

1. Suba o projeto no GitHub:
   ```bash
   git init && git add . && git commit -m "feat: api senac"
   git branch -M main
   git remote add origin https://github.com/<usuario>/<repo>.git
   git push -u origin main
   ```
2. No Render: **New → Blueprint** → conecte o repositório → **Apply**.
3. O Render lê o `render.yaml` e cria dois recursos:
   - `senac-db` (PostgreSQL)
   - `senac-api` (web service Docker), já com as variáveis `PG*` preenchidas a partir do banco.
4. Acompanhe os logs. O deploy termina quando aparece `Started ApiApplication`.
5. Teste: `https://senac-api.onrender.com/api/cursos`.

### Caminho B — manual (bom para explicar cada peça em aula)

1. **New → PostgreSQL** → nome `senac-db`, plano Free → Create.
2. **New → Web Service** → conecte o repositório → Runtime **Docker**.
3. Em **Environment**, adicione as variáveis copiando os dados da aba *Connections* do banco:
   `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`.
4. **Health Check Path**: `/actuator/health`.
5. Create Web Service.

### Pontos de atenção (plano Free)

- O serviço **hiberna após ~15 min sem tráfego**; a primeira requisição depois disso demora ~50s. Normal.
- O banco Free **expira em 30 dias** — para aula, suficiente; para projeto real, plano pago.
- Banco e serviço web precisam estar na **mesma região** (aqui: `oregon`).
- Use o **Internal Database URL** (host interno) — é mais rápido e não exige SSL. Para conectar de fora (DBeaver, pgAdmin), use a URL externa e acrescente `?sslmode=require`.
- A porta é definida pelo Render via variável `PORT`; por isso `server.port=${PORT:8080}`.

### Erros comuns

| Sintoma no log | Causa | Correção |
|---|---|---|
| `Connection to localhost:5432 refused` | variáveis `PG*` não configuradas | conferir Environment do serviço |
| `Schema-validation: missing table [cursos]` | migrations não rodaram | conferir `spring.flyway.enabled` e a pasta `db/migration` |
| `Validate failed: migration checksum mismatch` | migration já aplicada foi editada | criar nova migration `V4__...` e reverter a edição |
| Deploy ok mas health check falha | path errado | usar `/actuator/health` |
| `no main manifest attribute` | jar errado copiado no Dockerfile | conferir `COPY --from=build /app/target/*.jar` |
