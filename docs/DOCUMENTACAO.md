# Cadência — Documentação do projeto

Projeto Integrador de Sistemas Computacionais · Unieuro · ADS, 4º semestre · Prof. Henrique Dominges Garcia

Integrantes: Caio Andrax Portela Carvalho e Guilherme Pinheiro

> O que já está implementado em cada requisito aparece na tabela de situação do [README](../README.md).
> A especificação completa (casos de uso, regras de negócio e telas) está no [Documento de Requisitos](REQUISITOS.md).
> O roteiro e os resultados da validação com usuários estão em [VALIDACAO.md](VALIDACAO.md).

---

## 1. Introdução

### 1.1 Contexto e origem

O Cadência nasceu no Projeto Integrador do 4º semestre de Análise e Desenvolvimento de Sistemas da Unieuro, a partir de um documento de requisitos que previa uma plataforma web. Nesta etapa, o escopo foi revisado e reduzido para um aplicativo de terminal em Java com banco de dados PostgreSQL.

### 1.2 Demandante e problema

O demandante é o estudante de graduação noturna, que trabalha durante o dia, assiste cerca de 15 horas de aula por semana e não tem tempo nem método para revisar. Sem revisão, o conteúdo só é estudado na véspera da prova, com baixa retenção. Os usuários diretos são os próprios alunos da turma.

### 1.3 Proposta e escopo desta entrega

Um sistema de terminal para cadastrar disciplinas e flashcards e revisá-los em intervalos fixos de 1, 3, 7 e 15 dias. Esta entrega cobre menus de navegação, persistência no banco e testes. Interface web, login e o algoritmo SM-2 ficam fora do escopo.

---

## 2. Objetivos

### 2.1 Objetivo Geral

Desenvolver a primeira versão funcional de um sistema de linha de comando em Java, com persistência em banco de dados relacional PostgreSQL, para cadastro de disciplinas e flashcards e agendamento de revisões em intervalos fixos de 1, 3, 7 e 15 dias, a fim de ajudar estudantes de graduação noturna a revisar o conteúdo das aulas com pouco tempo disponível.

### 2.2 Objetivos Específicos

1. **Modelagem (entrada):** traduzir os requisitos em modelos conceitual, lógico e físico de banco de dados, normalizados até a 3FN, e em um mapa de navegação dos menus do terminal.
2. **Persistência (construção):** implementar o script SQL DDL e a camada DAO, com acesso ao PostgreSQL via JDBC, para gravar e consultar disciplinas, flashcards e revisões.
3. **Fluxo e regras (construção):** implementar os menus de terminal e as regras de negócio, separados nas camadas view, service e dao, com validação das entradas do usuário e cálculo da data da próxima revisão.
4. **Testes (verificação):** executar testes unitários e de integração com JUnit, para detectar falhas nas regras de revisão e na comunicação com o banco.
5. **Validação:** confrontar o sistema com a necessidade real do demandante, pedindo a colegas de turma que o usem e conferindo os critérios de aceitação das histórias de usuário.

---

## 3. Requisitos

### 3.1 Requisitos Funcionais

| ID | Requisito | Descrição |
|---|---|---|
| RF-01 | Cadastro de disciplinas | Permite cadastrar disciplinas com nome (obrigatório e único) e professor (opcional). |
| RF-02 | Cadastro de flashcards | Permite cadastrar flashcards vinculados a uma disciplina, com frente e verso obrigatórios. O card novo começa na etapa 0, com a primeira revisão no dia seguinte. |
| RF-03 | Listagem | Lista as disciplinas e os flashcards de uma disciplina, com mensagem clara quando não houver registros. |
| RF-04 | Revisão do dia | Apresenta os flashcards com revisão prevista para hoje ou antes: mostra a frente, revela o verso quando o aluno pedir e pergunta se acertou. |
| RF-05 | Agendamento por intervalos fixos | Etapa 0 = 1 dia, etapa 1 = 3 dias, etapa 2 = 7 dias, etapa 3 = 15 dias. Acerto avança uma etapa (a etapa 3 permanece); erro volta à etapa 0. |
| RF-06 | Histórico de revisões | Registra cada resposta (data e se acertou) e permite consultar o histórico de um flashcard. |
| RF-07 | Remoção com confirmação | Remove disciplinas e flashcards após confirmação (S/N). Apagar uma disciplina apaga os seus flashcards. |

### 3.2 Requisitos Não Funcionais

| ID | Requisito | Descrição |
|---|---|---|
| RNF-01 | Paradigma | Java orientado a objetos, em camadas view/service/dao/model. As regras de negócio ficam no service, sem depender da tela nem do banco. |
| RNF-02 | Ambiente | Executa no terminal e compila com `javac`, sem framework. |
| RNF-03 | Banco de dados | PostgreSQL via JDBC. DDL com PK, FK, UNIQUE, CHECK e CREATE INDEX. Modelo normalizado até a 3FN. |
| RNF-04 | Robustez | Entradas inválidas não encerram o programa: o sistema mostra uma mensagem clara e pede de novo. |
| RNF-05 | Execução local | O banco roda na máquina do usuário, sem internet nem APIs externas. |
| RNF-06 | Disponibilidade sem banco | Se o PostgreSQL não estiver acessível, o sistema avisa e funciona com os dados em memória. |
| RNF-07 | Segurança da configuração | A senha do banco é informada por variável de ambiente e nunca fica no código-fonte. |
| RNF-08 | Usabilidade | Menus numerados, mensagens com [OK] e [ERRO], tabelas alinhadas e datas no formato dd/mm/aaaa. |

---

## 4. Histórias de usuário

### US-01 — Cadastro de flashcards

> Como estudante de graduação noturna, eu quero cadastrar flashcards em uma disciplina, para que eu possa transformar o conteúdo da aula em cartões de revisão.

Critérios de aceitação:

- O menu "Cadastrar flashcard" pede uma disciplina existente; se não houver disciplinas, exibe "Cadastre uma disciplina primeiro".
- Frente e verso são obrigatórios (se vazio, mostra mensagem e pede de novo).
- Ao salvar: etapa 0, primeira revisão no dia seguinte e a mensagem "[OK] Flashcard cadastrado".

### US-02 — Revisão do dia

> Como estudante de graduação noturna, eu quero revisar apenas os flashcards que vencem hoje, para que eu use meu pouco tempo livre no que realmente precisa ser revisado.

Critérios de aceitação:

- A opção "Revisar hoje" mostra um card por vez (a frente; o verso só quando o aluno pedir).
- O sistema pergunta se o aluno acertou (S/N). Acerto avança a etapa e agenda a próxima revisão em 1, 3, 7 ou 15 dias; erro volta à etapa 0.
- Sem cards vencidos, exibe "Nenhum flashcard para revisar hoje".

### US-03 — Histórico de revisões

> Como estudante de graduação noturna, eu quero consultar o histórico de revisões de um flashcard, para que eu identifique os conteúdos em que mais erro.

Critérios de aceitação:

- A opção "Ver histórico de um flashcard" pede a disciplina e o flashcard e então mostra o histórico.
- O histórico mostra colunas alinhadas com a data e se acertou, da revisão mais recente para a mais antiga.
- Sem revisões, exibe "Este flashcard ainda não foi revisado".

---

## 5. Modelagem de dados

A modelagem segue três níveis: conceitual (o problema, sem detalhes de banco), lógico (as tabelas) e físico (o script SQL). Os diagramas são gerados a partir dos arquivos `.mmd` da pasta `docs/diagramas`.

### 5.1 Modelo conceitual (DER)

![DER do Cadência](diagramas/der.png)

Entidades:

- **DISCIPLINA:** a matéria cadastrada pelo aluno. O nome é único e o professor é opcional.
- **FLASHCARD:** o cartão de estudo, com frente (pergunta) e verso (resposta). Guarda a etapa (0 a 3) e a data da próxima revisão.
- **REVISAO:** cada vez que o aluno estuda um cartão, com a data e hora e se acertou.

Relacionamentos:

- Uma disciplina possui zero ou mais flashcards; cada flashcard pertence a exatamente uma disciplina (1:N).
- Um flashcard é revisado zero ou mais vezes; cada revisão é de exatamente um flashcard (1:N).

`proxima_revisao` é um atributo derivado: pode ser calculado a partir da etapa e da data da última revisão. Ele é armazenado para que a busca dos cartões do dia seja uma consulta simples (ver `DECISOES.md`, 09/11).

### 5.2 Modelo lógico

![Modelo lógico do Cadência](diagramas/modelo-logico.png)

Cada relacionamento 1:N virou uma chave estrangeira no lado N: `flashcard.id_disciplina` referencia `disciplina`, e `revisao.id_flashcard` referencia `flashcard`. As duas usam `ON DELETE CASCADE`: apagar uma disciplina apaga os seus flashcards, e apagar um flashcard apaga as suas revisões.

### 5.3 Modelo físico

O script `sql/schema.sql` cria as tabelas no PostgreSQL 16 com chave primária, chave estrangeira, `UNIQUE`, `CHECK` e índices.

### 5.4 Normalização

- **1FN:** todos os atributos são atômicos. Não há listas dentro de uma coluna: as revisões ficam em tabela própria, uma por linha.
- **2FN:** cada tabela tem chave primária de uma coluna só, então nenhum atributo depende de parte da chave.
- **3FN:** nenhum atributo comum depende de outro atributo comum da mesma tabela. `proxima_revisao` não é determinada só pela `etapa` (dois cartões na mesma etapa podem ter datas diferentes); ela depende também da data da última revisão, que está em outra tabela. A redundância em relação ao histórico é intencional e está registrada em `DECISOES.md`.

---

## 6. Arquitetura

### 6.1 Camadas

O código fica em `src/br/com/cadencia`, separado em camadas. Cada camada tem uma responsabilidade só:

- **view** (`MenuConsole`, `OpcaoMenuPrincipal`): mostra os menus e lê o teclado. Não decide regra nenhuma.
- **service** (`DisciplinaService`, `FlashcardService`): as regras de negócio, como "nome não repete" e "acerto sobe uma etapa".
- **dao** (`DisciplinaDao`, `FlashcardDao`, `RevisaoDao`): guarda e busca os dados. Cada DAO é uma interface com duas implementações: `...DaoJdbc`, que usa o PostgreSQL via JDBC, e `...DaoMemoria`, que usa listas em memória. `ConexaoBanco` abre a conexão.
- **model** (`Disciplina`, `Flashcard`, `Revisao`): os dados de cada entidade.
- **Main**: tenta conectar ao PostgreSQL. Se conseguir, entrega aos services os DAOs JDBC; se não, avisa e entrega os DAOs em memória.

Como os services só conhecem as interfaces dos DAOs, as mesmas regras funcionam com o banco e com a memória (polimorfismo).

### 6.2 Diagrama de classes

![Diagrama de classes do Cadência](diagramas/classes.png)

### 6.3 Diagrama de sequência: revisar os flashcards do dia

![Diagrama de sequência da revisão do dia](diagramas/sequencia-revisar-hoje.png)

### 6.4 Diagrama de implantação

![Diagrama de implantação do Cadência](diagramas/implantacao.png)

O programa e o banco rodam no computador do estudante. O programa fala com o PostgreSQL pelo driver JDBC (`lib/postgresql-42.7.4.jar`), por uma conexão TCP local na porta 5432.

---

## 7. Mapa de navegação

![Mapa de navegação dos menus](diagramas/mapa-navegacao.png)

Ao abrir, o programa informa se está usando o banco ou a memória e mostra o menu principal. Cada opção pede os dados de que precisa, mostra o resultado e volta ao menu. A opção 0 encerra o programa.

---

## 8. Testes

### 8.1 Estratégia

- **Testes unitários** (`FlashcardServiceTest`, `DisciplinaServiceTest`): testam as regras dos services usando os DAOs em memória. Rodam sem banco e em milissegundos.
- **Testes de integração** (`DaoJdbcIntegracaoTest`): testam os DAOs JDBC com o PostgreSQL de verdade. Cada teste roda dentro de uma transação desfeita no fim (rollback), então o banco fica como estava. Sem PostgreSQL disponível, esses testes são pulados, em vez de falhar.

Os testes usam JUnit 5 (`lib/junit-platform-console-standalone-1.9.1.jar`) e ficam na pasta `test`.

### 8.2 Como executar

Na pasta do projeto (PowerShell):

```powershell
javac -encoding UTF-8 -d bin-test -cp lib\junit-platform-console-standalone-1.9.1.jar -sourcepath "src;test" test\br\com\cadencia\service\FlashcardServiceTest.java test\br\com\cadencia\service\DisciplinaServiceTest.java test\br\com\cadencia\dao\DaoJdbcIntegracaoTest.java
$env:CADENCIA_DB_PASSWORD = "sua_senha"
java -jar lib\junit-platform-console-standalone-1.9.1.jar execute --class-path "bin-test;lib\postgresql-42.7.4.jar" --select-package br.com.cadencia --details=tree
```

### 8.3 Casos de teste e resultados

Execução em 16/11/2026, com Java 21 e PostgreSQL 16: **20 testes, 20 aprovados, 0 falhas**. Sem o PostgreSQL disponível, 14 são aprovados e os 6 de integração são pulados.

| ID | Teste | Tipo | Requisito | Resultado |
|---|---|---|---|---|
| T01 | Acerto na etapa 0 vai para a etapa 1 | Unitário | RF-05 | Aprovado |
| T02 | Acerto na etapa 3 continua na etapa 3 | Unitário | RF-05 | Aprovado |
| T03 | Intervalos das etapas: 1, 3, 7 e 15 dias | Unitário | RF-05 | Aprovado |
| T04 | Flashcard novo nasce na etapa 0, com revisão amanhã | Unitário | RF-02 | Aprovado |
| T05 | Frente vazia é recusada | Unitário | RF-02 | Aprovado |
| T06 | Cartão novo não entra na revisão de hoje | Unitário | RF-04 | Aprovado |
| T07 | Acertar: sobe uma etapa, agenda em 3 dias e grava no histórico | Unitário | RF-05, RF-06 | Aprovado |
| T08 | Errar: volta para a etapa 0 e agenda para amanhã | Unitário | RF-05, RF-06 | Aprovado |
| T09 | Histórico vem da revisão mais recente para a mais antiga | Unitário | RF-06 | Aprovado |
| T10 | Cadastra a disciplina e tira os espaços das pontas | Unitário | RF-01, RF-03 | Aprovado |
| T11 | Nome repetido é recusado, mesmo com maiúsculas diferentes | Unitário | RF-01 | Aprovado |
| T12 | Nome vazio é recusado | Unitário | RF-01 | Aprovado |
| T13 | Nome com mais de 100 caracteres é recusado | Unitário | RF-01 | Aprovado |
| T14 | Remover a disciplina remove também os flashcards dela | Unitário | RF-07 | Aprovado |
| T15 | Grava a disciplina e a encontra pelo código gerado pelo banco | Integração | RF-01, RNF-03 | Aprovado |
| T16 | Flashcard vencido entra na revisão do dia; o de amanhã não | Integração | RF-04, RNF-03 | Aprovado |
| T17 | Atualizar a agenda grava a nova etapa e a nova data | Integração | RF-05, RNF-03 | Aprovado |
| T18 | Revisão gravada aparece no histórico do flashcard | Integração | RF-06, RNF-03 | Aprovado |
| T19 | Remover a disciplina apaga flashcards e histórico (ON DELETE CASCADE) | Integração | RF-07, RNF-03 | Aprovado |
| T20 | O banco recusa nome de disciplina repetido (UNIQUE) | Integração | RF-01, RNF-03 | Aprovado |

---

## 9. Validação com usuários

Para conferir se o sistema atende o demandante, 2 ou 3 colegas de turma (estudantes do período noturno, o público do Cadência) usam o sistema sozinhos e fazem uma tarefa para cada história de usuário. O aplicador observa e anota se cada critério de aceitação foi cumprido. O roteiro, a ficha e os resultados estão em [VALIDACAO.md](VALIDACAO.md).

---

## 10. Matriz de rastreabilidade

Liga cada requisito à história de usuário, ao caso de uso, ao código, aos testes e ao passo da demonstração em que ele aparece.

| Requisito | História | Caso de uso | Onde está no código | Testes | Demonstração |
|---|---|---|---|---|---|
| RF-01 Cadastro de disciplinas | — | CU01 | `DisciplinaService.cadastrar`, `DisciplinaDao.inserir` | T10 a T13, T15, T20 | passo 2 |
| RF-02 Cadastro de flashcards | US-01 | CU02 | `FlashcardService.cadastrar`, `FlashcardDao.inserir` | T04, T05 | passo 3 |
| RF-03 Listagem | — | CU03 | `DisciplinaService.listar`, `FlashcardService.listarPorDisciplina` | T10 | passo 4 |
| RF-04 Revisão do dia | US-02 | CU04 | `FlashcardService.listarParaRevisarHoje`, `MenuConsole.revisarHoje` | T06, T16 | passo 5 |
| RF-05 Intervalos fixos | US-02 | CU04 | `FlashcardService.proximaEtapa`, `intervaloEmDias`, `registrarResposta` | T01 a T03, T07, T08, T17 | passos 5 e 6 |
| RF-06 Histórico | US-03 | CU05 | `FlashcardService.listarHistorico`, `RevisaoDao` | T07 a T09, T18 | passo 7 |
| RF-07 Remoção com confirmação | — | CU06, CU07 | `DisciplinaService.remover`, `FlashcardService.remover`, `ON DELETE CASCADE` | T14, T19 | passo 8 |
| RNF-03 PostgreSQL via JDBC | — | todos | `dao/...DaoJdbc`, `ConexaoBanco`, `sql/schema.sql` | T15 a T20 | passos 1 e 9 |
| RNF-04 Robustez | — | todos | `MenuConsole` (`lerInteiro`, `lerSimNao`, tratamento de `AcessoDadosException`) | — (verificado na demonstração) | passo 2 |
| RNF-06 Sem banco, em memória | — | todos | `Main`, `...DaoMemoria` | T01 a T14 (usam a memória) | plano B |

Passos da demonstração: 1 abrir com o banco; 2 cadastrar disciplina e tentar um nome repetido; 3 cadastrar flashcard; 4 listar; 5 revisar hoje; 6 listar de novo e ver as novas datas; 7 ver o histórico; 8 remover uma disciplina; 9 fechar e abrir de novo para ver os dados no banco.
