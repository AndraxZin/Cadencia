# Banco de dados — ambientes e migrations

O Cadência usa **dois ambientes de banco**, ambos PostgreSQL 16:

| Ambiente | Onde | Para quê |
|---|---|---|
| **local** | PostgreSQL instalado na máquina | Desenvolvimento diário e **a apresentação final** |
| **neon** | Neon (Postgres gerenciado, plano gratuito) | Ambiente remoto, acesso das duas máquinas, demonstrar deploy |

A mesma migration `V1__esquema_inicial.sql` roda nos dois sem alteração — é para isso
que existe o Flyway. Motivo da escolha em [`DECISOES.md`](DECISOES.md).

> **Por que o local continua sendo o ambiente da defesa:** o Neon depende da rede da
> sala de aula, e o plano gratuito hiberna o servidor após alguns minutos sem uso
> (*scale to zero*). A primeira consulta depois da hibernação leva alguns segundos para
> responder. Numa apresentação ao vivo, isso é um risco desnecessário. Apresente do
> local; mostre o Neon como ambiente remoto, se quiser.

---

## Ambiente local

### Criar e aplicar o esquema

```powershell
createdb cadencia
psql -d cadencia -f db/migration/V1__esquema_inicial.sql
psql -d cadencia -f db/seed.sql          # opcional: dados de demonstração
```

No **pgAdmin 4**: crie o banco `cadencia`, abra *Tools → Query Tool*, cole cada arquivo
e execute com F5 — esquema primeiro, seed depois.

### Recomeçar do zero

```powershell
dropdb cadencia
createdb cadencia
psql -d cadencia -f db/migration/V1__esquema_inicial.sql
```

O `seed.sql` pode ser reexecutado sem recriar o banco: ele começa com `TRUNCATE`.

---

## Ambiente Neon

### 1. Obter a string de conexão

No painel do Neon, projeto **Cadência** → *Connection Details*. A string tem este
formato:

```
postgresql://USUARIO:SENHA@ep-xxxx-xxxx.sa-east-1.aws.neon.tech/NOMEDOBANCO?sslmode=require
```

> ⚠️ **Essa string contém a senha do banco.** Ela nunca entra em arquivo versionado,
> nunca é colada em issue ou commit, e não vai em print de tela na apresentação. Se
> vazar, rotacione a credencial pelo painel do Neon imediatamente.
>
> O `sslmode=require` é obrigatório: o Neon recusa conexão sem TLS.

### 2. Guardar como variável de ambiente

**PowerShell — só na janela atual:**

```powershell
$env:DATABASE_URL = "postgresql://...?sslmode=require"
```

**PowerShell — persistente para o seu usuário** (some da janela atual; abra um novo
terminal depois):

```powershell
[Environment]::SetEnvironmentVariable("DATABASE_URL", "postgresql://...?sslmode=require", "User")
```

Como o projeto é desenvolvido em duas máquinas, esse passo se repete em cada uma. A
variável fica no perfil do usuário, não no repositório — que é exatamente o objetivo.

### 3. Aplicar o esquema

Não é preciso Spring Boot nem o CLI do Neon para isso. O `psql` conecta direto:

```powershell
psql $env:DATABASE_URL -f db/migration/V1__esquema_inicial.sql
psql $env:DATABASE_URL -f db/seed.sql
```

### 4. Conferir

```powershell
psql $env:DATABASE_URL -c "\dt"
```

Devem aparecer as 10 tabelas: `aula`, `disciplina`, `flashcard`, `material_estudo`,
`questao`, `resumo`, `revisao`, `sessao_estudo`, `sessao_revisao`, `usuario`.

---

## O CLI e o MCP do Neon são opcionais

O fluxo com `neon.ts` e `neon deploy` que aparece na documentação do Neon é voltado a
aplicações **Node/TypeScript** — o `neon deploy` procura uma aplicação JavaScript para
publicar. Este projeto é Maven + Spring Boot, então esse caminho não se aplica.

Para Java, o Neon é simplesmente **um PostgreSQL com endereço na internet**: basta a
string de conexão. O CLI (`npm i -g neon@latest`) continua útil para consultar o banco
pelo terminal, mas é conveniência, não requisito.

---

## Quando o Spring Boot existir (Sprint 2)

Hoje as migrations são aplicadas à mão, com `psql`. A partir do Sprint 2, o **Flyway**
faz isso sozinho quando a aplicação sobe: ele lê os arquivos de `db/migration`, verifica
numa tabela de controle quais já foram aplicados, e executa só os novos.

A separação entre os dois ambientes será feita por **perfis do Spring** — um mecanismo
que permite ter conjuntos de configuração diferentes e escolher qual vale na hora de
rodar:

```
application.properties           ← comum aos dois ambientes
application-local.properties     ← conexão local   (IGNORADO pelo Git)
application-neon.properties      ← conexão remota, lendo ${DATABASE_URL}
```

Rodando com um perfil ou outro:

```powershell
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
./mvnw spring-boot:run -Dspring-boot.run.profiles=neon
```

Os arquivos ainda não existem — serão criados no Sprint 2, com explicação. O
`.gitignore` já bloqueia `application-local.properties` desde agora, para que a
proteção exista antes da credencial.

---

## Regra das migrations

**Arquivo de migration já aplicado nunca é editado.** Se o esquema precisar mudar,
cria-se um novo arquivo:

```
db/migration/
  V1__esquema_inicial.sql     ← aplicado; não se toca mais
  V2__nome_da_mudanca.sql     ← a próxima alteração vem aqui
```

O Flyway guarda o *checksum* de cada arquivo aplicado. Editar um arquivo já executado
muda esse checksum, e o Flyway se recusa a subir — com razão: o banco de produção já
tem a versão antiga, e não há como saber o que fazer com a diferença.

É por isso que, se o professor pedir uma alteração no modelo depois da aula, a mudança
vira `V2`, e a `V1` permanece como está.
