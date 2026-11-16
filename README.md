# Cadência

Aplicativo de **terminal** (Java 21) para estudar com **flashcards** e **revisão espaçada**, com banco de dados **PostgreSQL** acessado por **JDBC**.

Projeto Integrador de Sistemas Computacionais · 4º semestre de Análise e Desenvolvimento de Sistemas · Unieuro (2026.2)

| | |
|---|---|
| **Integrantes** | Caio Andrax Portela Carvalho · Guilherme Pinheiro |
| **Professor** | Henrique Dominges Garcia |

## O que o sistema faz

O aluno cadastra **disciplinas** e **flashcards** (frente = pergunta, verso = resposta). O sistema agenda a revisão de cada cartão em intervalos fixos de **1, 3, 7 e 15 dias**: se o aluno acerta, o cartão avança uma etapa; se erra, volta ao começo. Cada resposta fica no histórico.

## Situação atual (16/11/2026)

| Requisito | Descrição | Situação |
|---|---|---|
| RF-01 | Cadastro de disciplinas | Feito |
| RF-02 | Cadastro de flashcards | Feito |
| RF-03 | Listagem de disciplinas e de flashcards | Feito |
| RF-04 | Revisão do dia | Feito |
| RF-05 | Agendamento em 1, 3, 7 e 15 dias | Feito |
| RF-06 | Histórico de revisões | Feito |
| RF-07 | Remoção com confirmação | Feito |
| — | Persistência no PostgreSQL (DAO + JDBC) | Feito, com modo memória quando o banco não está disponível |
| — | Testes com JUnit | A fazer |

Os requisitos completos estão em [`docs/DOCUMENTACAO.md`](docs/DOCUMENTACAO.md) e as decisões de projeto em [`docs/DECISOES.md`](docs/DECISOES.md).

## Como o código está organizado

```
src/br/com/cadencia/
├── Main.java          ponto de entrada: escolhe banco ou memória e monta as peças
├── DadosExemplo.java  dados para demonstração (opção --exemplo)
├── model/             dados: Disciplina, Flashcard, Revisao
├── service/           regras de negócio: DisciplinaService, FlashcardService
├── dao/               acesso aos dados
│     ├── DisciplinaDao, FlashcardDao, RevisaoDao      interfaces (o que precisa ser guardado)
│     ├── ...DaoJdbc + ConexaoBanco                    implementação com PostgreSQL (JDBC)
│     └── ...DaoMemoria                                implementação em memória
└── view/              tela e teclado: MenuConsole, OpcaoMenuPrincipal
lib/postgresql-42.7.4.jar   driver JDBC oficial do PostgreSQL
sql/schema.sql              criação das tabelas
docs/                       documentação, diagramas e registro de decisões
```

Fluxo: o usuário digita na **view**, que chama o **service** (onde ficam as regras), que usa um **dao** (que grava no banco ou na memória). O service só conhece as interfaces do dao, então funciona igual nos dois casos.

## Como executar

Requisito: JDK 17 ou mais novo (o projeto usa Java 21). Na pasta do projeto, compile:

```powershell
javac -encoding UTF-8 -d bin -sourcepath src src\br\com\cadencia\Main.java
```

### Sem banco (dados em memória)

```powershell
java -cp bin br.com.cadencia.Main --exemplo
```

O `--exemplo` carrega 2 disciplinas e 5 flashcards, 3 deles para revisar hoje (um cartão novo só vence amanhã). Sem ele, o sistema começa vazio.

### Com PostgreSQL

1. Crie o banco e as tabelas (uma vez só):

   ```powershell
   psql -U postgres -c "CREATE DATABASE cadencia;"
   psql -U postgres -d cadencia -f sql\schema.sql
   ```

2. Informe a senha do usuário `postgres` e rode com o driver no classpath:

   ```powershell
   $env:CADENCIA_DB_PASSWORD = "sua_senha"
   java -cp "bin;lib\postgresql-42.7.4.jar" br.com.cadencia.Main --exemplo
   ```

Na abertura o programa mostra `[BANCO] Conectado ao PostgreSQL` ou, se algo faltar, `[AVISO] Sem PostgreSQL: <motivo>` e segue em memória.

Outras variáveis (opcionais): `CADENCIA_DB_URL` (padrão `jdbc:postgresql://localhost:5432/cadencia`) e `CADENCIA_DB_USER` (padrão `postgres`). A senha nunca fica escrita no código.

No Linux, macOS ou GitHub Codespaces, troque `\` por `/` e use `:` no lugar de `;` no `-cp`.

## Próximos passos

1. Testes com JUnit: unitários da regra das etapas e de integração dos DAOs JDBC
2. Diagramas restantes: casos de uso, mapa de navegação, classes e sequência
3. Validação com colegas de turma
