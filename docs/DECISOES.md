# Registro de decisões

Cada decisão relevante do projeto, com o motivo e o que se perde com ela. Serve de material de estudo para a defesa.

---

## [2026-10-19] Escopo reduzido: aplicativo de terminal com PostgreSQL via JDBC

**Contexto:** o documento de requisitos original previa uma plataforma web (login, calendário, SM-2). O professor reavaliou os requisitos e o cronograma da disciplina pede menus de terminal, banco de dados e testes.

**Alternativas:** manter a plataforma web com Spring Boot; ou reduzir para um aplicativo de terminal em Java puro com JDBC.

**Escolha:** terminal em Java 21, PostgreSQL via JDBC, sem framework, sem login e sem SM-2.

**Motivo:** menos peças para dominar e defender, e alinhado ao cronograma. O que se perde: interface web, login e o algoritmo SM-2 ficam fora desta entrega.

**Quem decidiu:** Caio (a alinhar com Guilherme).

---

## [2026-10-20] Regra de revisão com intervalos fixos

**Contexto:** era preciso uma regra de agendamento simples de implementar, testar e explicar.

**Alternativas:** algoritmo SM-2 (intervalo adaptativo); ou intervalos fixos por etapa.

**Escolha:** etapa 0 = 1 dia, 1 = 3 dias, 2 = 7 dias, 3 = 15 dias. Card novo começa na etapa 0, com a primeira revisão no dia seguinte. Acerto avança uma etapa (a 3 permanece); erro volta à etapa 0.

**Motivo:** fácil de testar e de defender. O que se perde: não se adapta à dificuldade de cada cartão como o SM-2.

**Quem decidiu:** Caio (a regra foi proposta pelo Claude e aceita por Caio).

---

## [2026-11-09] `proxima_revisao` guardada na tabela `flashcard`

**Contexto:** a data da próxima revisão é um dado derivado (depende da etapa e da última resposta). Poderia ser calculada sempre que fosse preciso.

**Alternativas:** calcular a data a cada consulta; ou guardar a data pronta numa coluna.

**Escolha:** guardar a coluna `proxima_revisao` (DATE) em `flashcard`, com um índice.

**Motivo:** a consulta "o que vence hoje" vira um simples `WHERE proxima_revisao <= CURRENT_DATE`. O que se perde: é um dado redundante, que o service precisa manter coerente com a etapa.

**Quem decidiu:** proposta do Claude; Caio e Guilherme ainda precisam confirmar.

---

## [2026-11-09] Primeira base do código gerada de uma vez, sem revisão passo a passo

**Contexto:** o professor ia olhar o repositório no dia 09/11 e ele ainda estava vazio.

**Alternativas:** construir cada classe com explicação e confirmação, uma por vez; ou gerar uma base completa e estudá-la depois.

**Escolha:** o Claude gerou de uma vez a estrutura model/service/view/Main, o script `sql/schema.sql` e estes documentos. Os dados ficam em memória, sem JDBC por enquanto. O `CLAUDE.md`, que ainda descrevia Spring Boot e web, foi atualizado para o escopo de terminal + JDBC.

**Motivo:** havia pouco tempo. O que se perde: o combinado de explicar antes de escrever, uma unidade por vez. Para compensar, Caio estuda a base arquivo por arquivo, e as próximas funcionalidades (DAO/JDBC, revisão, histórico, testes) serão feitas passo a passo.

**Quem decidiu:** Caio.
