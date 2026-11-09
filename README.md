# Cadência

Aplicativo de **terminal** (Java 21) para estudar com **flashcards** e **revisão espaçada**, com banco de dados **PostgreSQL** acessado por **JDBC**.

Projeto Integrador de Sistemas Computacionais · 4º semestre de Análise e Desenvolvimento de Sistemas · Unieuro (2026.2)

| | |
|---|---|
| **Integrantes** | Caio Andrax Portela Carvalho · Guilherme Pinheiro |
| **Professor** | Henrique Dominges Garcia |

## O que o sistema faz

O aluno cadastra **disciplinas** e **flashcards** (frente = pergunta, verso = resposta). O sistema agenda a revisão de cada cartão em intervalos fixos de **1, 3, 7 e 15 dias**: se o aluno acerta, o cartão avança uma etapa; se erra, volta ao começo.

## Situação atual (09/11/2026)

A base do programa está pronta e roda no terminal. Por enquanto os dados ficam **em memória** (somem ao fechar o programa). A ligação com o PostgreSQL é o próximo passo; o script das tabelas (`sql/schema.sql`) já está pronto e testado.

| Requisito | Descrição | Situação |
|---|---|---|
| RF-01 | Cadastro de disciplinas | Feito (dados em memória) |
| RF-02 | Cadastro de flashcards | Feito (dados em memória) |
| RF-03 | Listagem de disciplinas e de flashcards | Feito (dados em memória) |
| RF-04 | Revisão do dia | A fazer |
| RF-05 | Agendamento em 1, 3, 7 e 15 dias | Parcial: o cartão já nasce na etapa 0 com revisão no dia seguinte; falta avançar as etapas |
| RF-06 | Histórico de revisões | A fazer |
| RF-07 | Remoção com confirmação | A fazer |
| — | Persistência no PostgreSQL (DAO + JDBC) | A fazer |
| — | Testes com JUnit | A fazer |

Os requisitos completos estão em [`docs/DOCUMENTACAO.md`](docs/DOCUMENTACAO.md) e as decisões de projeto em [`docs/DECISOES.md`](docs/DECISOES.md).

## Como o código está organizado

```
src/br/com/cadencia/
├── Main.java        ponto de entrada: monta as peças e inicia o menu
├── model/           dados: Disciplina, Flashcard
├── service/         regras de negócio: DisciplinaService, FlashcardService
└── view/            tela e teclado: MenuConsole, OpcaoMenuPrincipal
sql/schema.sql       criação das tabelas no PostgreSQL
docs/                documentação e registro de decisões
```

Fluxo: o usuário digita na **view**, que chama o **service** (onde ficam as regras), que usa o **model** (os dados). Na próxima etapa entra a camada **dao** (JDBC) entre o service e o banco.

## Como executar

Requisito: JDK 21. Na pasta do projeto (PowerShell):

```powershell
javac -encoding UTF-8 -d bin -sourcepath src src\br\com\cadencia\Main.java
java -cp bin br.com.cadencia.Main
```

## Banco de dados

Cria o banco e as tabelas (testado no PostgreSQL 16):

```powershell
psql -U postgres -c "CREATE DATABASE cadencia;"
psql -U postgres -d cadencia -f sql/schema.sql
```

## Próximos passos

1. Camada DAO com JDBC, para gravar no PostgreSQL
2. Revisão do dia e agendamento (RF-04 e RF-05)
3. Histórico (RF-06) e remoção (RF-07)
4. Testes com JUnit
5. Diagramas: DER, modelo lógico e mapa de navegação
