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

---

## [2026-11-16] Diagramas de modelagem em Mermaid, preto e branco

**Contexto:** o guia de modelagem do professor exige o DER e o modelo lógico com fundo branco, linhas pretas e sem cores.

**Alternativas:** desenhar à mão no brModelo ou no draw.io (precisa de computador); ou gerar o diagrama a partir de texto com Mermaid.

**Escolha:** Mermaid. Os arquivos-fonte `.mmd` ficam em `docs/diagramas`, e as imagens `.png` são geradas a partir deles.

**Motivo:** o texto fica versionado no Git e pode ser editado até pelo celular; o desenho de exemplo do guia segue o mesmo estilo. O que se perde: o posicionamento das caixas é automático, sem controle fino do layout.

**Quem decidiu:** proposta do Claude, aceita pelo Caio junto com a exceção abaixo.

---

## [2026-11-16] Seção de modelagem subida antes da confirmação de entendimento (exceção ao contrato)

**Contexto:** aula de 16/11 e pedido do professor para acelerar a documentação.

**Alternativas:** esperar o Caio confirmar o entendimento do DER (regra 3 do contrato) ou subir já.

**Escolha:** subir o DER, o modelo lógico, a seção 5 da documentação (incluindo a análise de normalização) e o PDF antes da confirmação, a pedido do Caio.

**Motivo:** ganhar tempo. O que se perde: este trecho entrou sem revisão passo a passo. **Revisar antes da apresentação**, principalmente a seção 5.4 (normalização) e a decisão sobre `proxima_revisao`, que ainda precisa da confirmação do Caio e do Guilherme.

**Quem decidiu:** Caio.

---

## [2026-11-16] Camada DAO com interface e duas implementações (PostgreSQL e memória)

**Contexto:** o professor pediu a comunicação com o banco. O laboratório pode não ter PostgreSQL instalado, e o sistema precisa abrir mesmo assim.

**Alternativas:** classes DAO só com JDBC (o sistema não abre sem banco); ou uma interface por entidade (`DisciplinaDao`, `FlashcardDao`, `RevisaoDao`) com duas implementações, JDBC e memória.

**Escolha:** interface + duas implementações. O `Main` tenta conectar; se conseguir usa as classes `...DaoJdbc`, senão avisa e usa as `...DaoMemoria`. Os services só conhecem as interfaces.

**Motivo:** o sistema sempre abre, e é polimorfismo na prática (o mesmo service funciona com qualquer implementação). Também prepara os testes: unitários com a memória, de integração com o banco. O que se perde: há duas versões de cada DAO para manter, e a memória precisa imitar o `ON DELETE CASCADE` à mão.

**Quem decidiu:** Caio.

---

## [2026-11-16] Driver JDBC do PostgreSQL dentro do projeto

**Contexto:** sem Maven, o `.jar` do driver precisa estar no classpath.

**Alternativas:** cada um baixa o driver em cada máquina; ou o arquivo fica no repositório.

**Escolha:** `lib/postgresql-42.7.4.jar` (driver oficial, baixado das releases do projeto pgjdbc no GitHub) versionado no Git.

**Motivo:** funciona nas duas máquinas e no laboratório sem baixar nada. O que se perde: um arquivo binário de cerca de 1 MB no repositório, e atualizar a versão é manual.

**Quem decidiu:** Caio.

---

## [2026-11-16] Senha do banco em variável de ambiente

**Contexto:** a conexão precisa de usuário e senha, e o repositório é enviado ao GitHub.

**Alternativas:** escrever a senha no código; arquivo de configuração fora do Git; variáveis de ambiente.

**Escolha:** variáveis `CADENCIA_DB_PASSWORD`, `CADENCIA_DB_USER` e `CADENCIA_DB_URL`, com padrões para usuário e endereço.

**Motivo:** a senha nunca vai para o GitHub e não precisa de código para ler arquivo. O que se perde: é preciso definir a variável no terminal antes de rodar.

**Quem decidiu:** proposta do Claude, dentro da exceção abaixo.

---

## [2026-11-16] Código restante gerado de uma vez, sem revisão passo a passo (exceção ao contrato)

**Contexto:** aula de 16/11; o Caio pediu para completar o código conforme os requisitos e subir para mostrar ao professor no mesmo dia.

**Alternativas:** seguir o contrato (explicar, confirmar e o Caio escrever parte, uma unidade por vez); ou gerar tudo agora.

**Escolha:** o Claude gerou de uma vez a camada `dao` (interfaces, JDBC, memória e `ConexaoBanco`), o model `Revisao`, as regras de revisão no `FlashcardService` (RF-04, RF-05, RF-06), a remoção (RF-07), as telas novas no `MenuConsole`, o `DadosExemplo` e o driver em `lib/`. Tudo foi testado com PostgreSQL 16 e em modo memória antes de subir.

**Motivo:** prazo. O que se perde: o Caio ainda não escreveu nem revisou este código. **Estudar antes da apresentação**, nesta ordem: `FlashcardService` (regra das etapas), `DisciplinaDaoJdbc` (padrão JDBC), `Main` (escolha banco ou memória), `MenuConsole.revisarHoje`.

**Quem decidiu:** Caio.
