# Plano de Aula — API Java com Spring Boot e Deploy no Render

**Curso:** Desenvolvimento de Sistemas / Técnico em Informática
**Componente:** Programação Back-end com Java
**Carga horária:** 4 horas-aula (4 × 50 min), aplicável em 1 encontro ou 2 de 2h
**Modalidade:** presencial com laboratório (adaptável a remoto síncrono)
**Projeto de apoio:** este repositório (`render-BE`)

---

## 1. Ementa do encontro

Construção de uma API REST em Java com Spring Boot, persistência em PostgreSQL com versionamento de banco via Flyway, empacotamento em container Docker e publicação em ambiente de nuvem (Render) com integração contínua a partir do GitHub.

## 2. Objetivo geral

Ao final da aula, o aluno será capaz de publicar uma API REST Java funcional na internet, conectada a um banco de dados gerenciado, com o schema versionado por migrations.

## 3. Objetivos específicos

Ao final da aula, o aluno deverá ser capaz de:

1. Identificar as camadas de uma aplicação Spring Boot (Controller, Service, Repository, Entity) e a responsabilidade de cada uma.
2. Implementar endpoints REST com os verbos GET, POST, PUT e DELETE, retornando os status HTTP adequados.
3. Validar dados de entrada com Bean Validation e tratar erros de forma centralizada.
4. Criar e aplicar migrations com Flyway, explicando por que o schema não deve ser gerado pelo `ddl-auto`.
5. Externalizar configuração em variáveis de ambiente, separando o ambiente local do de produção.
6. Empacotar a aplicação em uma imagem Docker multi-stage.
7. Realizar o deploy no Render a partir de um repositório GitHub e diagnosticar falhas pelos logs.

## 4. Pré-requisitos dos alunos

- Lógica de programação e POO em Java.
- Noções de SQL (CREATE TABLE, INSERT, SELECT).
- Conceito de cliente/servidor e de requisição HTTP.
- Git básico (`add`, `commit`, `push`).

## 5. Recursos necessários

| Recurso | Observação |
|---|---|
| JDK 21 | Temurin ou Microsoft Build |
| IntelliJ IDEA Community ou VS Code + Extension Pack for Java | — |
| Docker Desktop | para o Postgres local |
| Git + conta no GitHub | repositório público |
| Conta no Render (plano Free) | criar **antes** da aula, não exige cartão |
| Postman, Insomnia ou `curl` | testar os endpoints |
| Projetor / compartilhamento de tela | — |

> **Preparação prévia do docente:** peça que os alunos criem as contas (GitHub + Render) e instalem JDK, IDE e Docker no encontro anterior. Instalação em sala consome a aula inteira.

---

## 6. Roteiro da aula

### Momento 1 — Contextualização (20 min)

- Pergunta disparadora: *"Seu TCC roda na sua máquina. Como o professor, o cliente ou o app mobile do colega acessa essa API?"*
- Conceitos: localhost × servidor público, o que é PaaS, por que não usar a própria máquina.
- Apresentação do produto final: abrir a URL `https://...onrender.com/api/cursos` já publicada pelo docente.
- **Entregável visível desde o começo:** o aluno sabe exatamente onde vai chegar.

### Momento 2 — Anatomia do projeto Spring Boot (40 min)

- Abrir o projeto na IDE e percorrer a estrutura de pastas.
- Explicar as camadas com o domínio `curso`:

| Classe | Papel | Analogia |
|---|---|---|
| `CursoController` | recebe a requisição HTTP | atendente do balcão |
| `CursoService` | aplica a regra de negócio | setor que resolve |
| `CursoRepository` | conversa com o banco | arquivo/almoxarifado |
| `Curso` (Entity) | representa a linha da tabela | a ficha |
| `CursoDTO` | formato de entrada e saída | formulário preenchido |

- Rodar a aplicação localmente (`docker compose up -d db` + `./mvnw spring-boot:run`).
- Demonstração: `GET /api/cursos` no navegador e no Postman; abrir `/swagger-ui.html`.
- **Atividade guiada (15 min):** cada aluno executa os 5 endpoints de curso e registra o status HTTP de cada resposta (200, 201, 204, 400, 404).

### Momento 3 — Banco de dados e migrations (50 min)

- Problema a resolver: *"O schema da minha máquina é igual ao do servidor? E o do colega?"*
- `ddl-auto: validate` × `update`: por que produção nunca usa `update`.
- Abrir `V1`, `V2` e `V3`; explicar a convenção `V<n>__<descricao>.sql`.
- Mostrar a tabela `flyway_schema_history` no banco e o conceito de checksum.
- Regra de ouro: **migration aplicada é imutável** — toda mudança vira uma nova versão.
- **Atividade prática (25 min):** criar a migration `V4__adicionar_telefone_em_alunos.sql`:

  ```sql
  ALTER TABLE alunos ADD COLUMN telefone VARCHAR(20);
  ```

  Em seguida, adicionar o campo na entidade `Aluno`, no `AlunoDTO` e reiniciar a aplicação.
  Depois, provocar o erro de propósito: editar a `V4` já aplicada e reiniciar → analisar a mensagem
  `Validate failed: migration checksum mismatch` e discutir por que o Flyway age assim.

### Momento 4 — Containerização e deploy (60 min)

- Ler o `Dockerfile` linha a linha: por que dois estágios (build pesado × runtime enxuto), por que copiar o `pom.xml` antes do `src` (cache de camadas).
- Externalização de configuração: `application.yml` lendo `PGHOST`, `PGUSER`, `PORT`...; por que segredo não entra no Git.
- **Prática em conjunto, passo a passo:**
  1. `git init`, commit e push do projeto para o GitHub.
  2. Render → **New → Blueprint** → selecionar o repositório → **Apply**.
  3. Acompanhar o log de build ao vivo e identificar as fases: `docker build` → `mvn package` → `Started ApiApplication`.
  4. Verificar no log do primeiro boot as linhas do Flyway aplicando `V1`, `V2` e `V3`.
  5. Abrir a URL pública e executar um `POST /api/cursos` pelo Postman apontando para produção.
- Discutir as limitações do plano Free: hibernação após 15 min, cold start, banco com 30 dias de validade.

### Momento 5 — Depuração e fechamento (30 min)

- **Laboratório de erros provocados** (o docente quebra, a turma diagnostica pelo log):

| Erro provocado | Sintoma | Diagnóstico esperado |
|---|---|---|
| Remover a variável `PGHOST` no Render | `Connection to localhost:5432 refused` | configuração de ambiente ausente |
| Health check apontando para `/health` | deploy "unhealthy" | path correto é `/actuator/health` |
| Entidade com campo que não existe na tabela | `Schema-validation: missing column` | faltou criar a migration |

- Retomada dos objetivos: cada aluno mostra a própria URL pública funcionando.
- Ponte para a próxima aula: autenticação com Spring Security + JWT; consumo da API pelo front-end.

---

## 7. Metodologia

Aula expositiva dialogada alternada com prática em laboratório (*live coding* acompanhado). O código base é fornecido pronto e funcionando; o aluno modifica, quebra e conserta, em vez de digitar o projeto do zero — isso concentra o tempo nos conceitos de persistência e deploy, e não em erros de digitação.

## 8. Avaliação

| Instrumento | Peso | Critério |
|---|---|---|
| Lista de status HTTP (Momento 2) | 10% | identificou corretamente 200/201/204/400/404 |
| Migration `V4` aplicada (Momento 3) | 25% | nomenclatura correta, entidade sincronizada, aplicação sobe |
| API publicada e acessível (Momento 4) | 40% | URL pública respondendo com dados do banco |
| Diagnóstico dos erros (Momento 5) | 15% | associou o sintoma no log à causa |
| Participação e colaboração | 10% | — |

**Critério de aprovação do encontro:** entregar a URL pública com `GET /api/cursos` retornando 200 e um registro criado pelo próprio aluno via `POST`.

### Atividade extraclasse (entrega na aula seguinte)

Criar um terceiro domínio — `Professor` — contendo:
1. migration `V5__criar_tabela_professores.sql`;
2. entidade, repository, service e controller com CRUD completo;
3. validação dos campos e tratamento de erro 404;
4. deploy da alteração via `git push` (deploy automático);
5. entrega: link do repositório + URL pública do endpoint `/api/professores`.

## 9. Mapa de riscos da aula

| Risco | Probabilidade | Plano B |
|---|---|---|
| Internet do laboratório instável | alta | imagens Docker e dependências Maven pré-baixadas em cache |
| Aluno sem conta no Render | média | deploy em duplas |
| Build lento no Render (~5 min) | alta | usar a janela de espera para a discussão sobre plano Free e cold start |
| Docker não instalado na máquina | média | usar o banco Postgres compartilhado do laboratório |
| Serviço Free hibernado na demonstração | alta | abrir a URL 2 min antes para "acordar" o serviço |

## 10. Referências

- Spring Boot — https://docs.spring.io/spring-boot/index.html
- Spring Data JPA — https://docs.spring.io/spring-data/jpa/reference/
- Flyway — https://documentation.red-gate.com/flyway
- Render (Docker e Blueprints) — https://render.com/docs
- Docker — https://docs.docker.com/get-started/
- Richardson Maturity Model (níveis de maturidade REST) — https://martinfowler.com/articles/richardsonMaturityModel.html
