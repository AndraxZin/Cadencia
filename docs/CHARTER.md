# Cadência — Charter do Projeto

**Plataforma de estudo ativo por repetição espaçada**

Centro Universitário Unieuro — Grupo Educacional Ceuma
Projeto Integrador de Sistemas Computacionais
Análise e Desenvolvimento de Sistemas — 4º Semestre — 2026.2

Caio Andrax Portela Carvalho — RA 093793
Prof. Henrique Dominges Garcia
Brasília – DF, 2026

> **Fonte de verdade.** Este arquivo Markdown é o original. O `.docx` entregue na AC
> é gerado a partir daqui — nunca editado diretamente. Ver `docs/DECISOES.md`.

---

# PARTE I — DOCUMENTAÇÃO ACADÊMICA

*Conteúdo destinado à entrega da Avaliação Continuada (AC): objetivos, requisitos e
escopo do projeto.*

## 1. Introdução

Cadência é uma plataforma de estudo ativo por repetição espaçada, desenvolvida como
Projeto Integrador de Sistemas Computacionais do 4º semestre do curso de Análise e
Desenvolvimento de Sistemas do Centro Universitário Unieuro, sob orientação do Prof.
Henrique Dominges Garcia, no período letivo 2026.2.

O projeto nasce de um problema real enfrentado por estudantes do turno noturno: a
rotina de aula seguida imediatamente por jornada de trabalho deixa pouco tempo para
revisão do conteúdo estudado, levando a uma prática de estudo concentrada apenas na
véspera das avaliações — o que produz baixa retenção, especialmente em disciplinas de
conteúdo cumulativo.

A proposta do Cadência é organizar o material de cada aula em unidades de estudo ativo
(flashcards, questões e resumos) e agendar automaticamente as revisões de cada unidade
por meio do algoritmo de repetição espaçada SM-2, apresentando ao aluno, a cada dia,
uma fila de revisão já priorizada pelo seu próprio desempenho.

## 2. Ficha do Projeto

| Item | Definição |
|---|---|
| **Integrantes** | Caio Andrax Portela Carvalho — RA 093793 |
| **Tema do projeto** | Plataforma web de estudo ativo que converte material de aula em unidades revisáveis e agenda revisões por repetição espaçada |
| **Público-alvo** | Estudantes de graduação noturna que trabalham em período integral, em cursos de tecnologia com alta carga teórica acumulada |
| **Problema a ser resolvido** | O aluno noturno assiste cerca de 15h de aula por semana e chega em casa sem tempo nem método para revisar. A revisão só acontece na véspera da prova, de forma concentrada e passiva, produzindo retenção baixa em disciplinas cumulativas |
| **Proposta de solução** | Sistema que organiza o conteúdo por disciplina e aula, permite fatiá-lo em unidades de estudo ativo, agenda a próxima revisão de cada unidade via SM-2, e prioriza na sessão diária o que o aluno mais erra |
| **Tecnologia OO proposta** | Java 21 · Spring Boot 3.x · Maven · JUnit 5 · IDE: IntelliJ IDEA Community Edition |
| **SGBD relacional proposto** | PostgreSQL 16 · versionamento de esquema com Flyway |
| **Repositório Git** | https://github.com/AndraxZin/Cadencia |

### Pontos a confirmar com o professor

- [ ] A linguagem OO é livre ou existe exigência de Java/C#?
- [ ] O trabalho é individual ou em grupo? Quantos integrantes?
- [ ] Há exigência de front-end próprio ou uma API basta?
- [ ] Existe rubrica de avaliação publicada?

## 3. Objetivo Geral

Desenvolver uma plataforma web de estudo ativo, capaz de organizar o conteúdo de aulas
em unidades revisáveis e agendar revisões por repetição espaçada, destinada a
estudantes de graduação noturna, a fim de aumentar a retenção de conteúdo mesmo com
pouco tempo disponível para estudar.

## 4. Objetivos Específicos

- **OE1** — Implementar um motor de agendamento de revisões baseado no algoritmo SM-2,
  apresentado ao aluno como uma visão de calendário, permitindo controle claro de
  quando cada conteúdo deve ser revisado.
- **OE2** — Desenvolver um sistema de cadastro e autenticação de usuários, com
  armazenamento seguro de senha (hash, nunca texto puro) via Spring Security,
  garantindo que cada aluno acesse apenas o seu próprio conteúdo.
- **OE3** — Implementar o cadastro de disciplinas e aulas em uma visão de calendário
  semanal e mensal, permitindo ao aluno organizar seu conteúdo conforme a grade
  curricular.
- **OE4** — Possibilitar a criação manual de flashcards, questões e resumos vinculados
  a uma aula, organizados em cards de acesso rápido que retomam o estudo exatamente de
  onde o aluno parou.
- **OE5** — Disponibilizar uma sessão diária de estudo com fila de revisões priorizada
  pelo desempenho do aluno, acompanhada de um painel de progresso por disciplina e de
  sinais visuais de incentivo, como sequência de dias estudados.
- **OE6** — Implementar um sistema de tokens de acesso para agentes de IA, com escopo
  restrito a espaços específicos (disciplina ou aula) e revogação individual,
  permitindo que assistentes externos leiam ou organizem o conteúdo do aluno sem
  comprometer a conta principal.

> **Observação:** os objetivos OE1 a OE5 constituem a base funcional da plataforma e
> serão desenvolvidos primeiro (Fase 1). O OE6 depende dessa base já implementada e
> será tratado em uma segunda etapa (Fase 2).

## 5. Requisitos

Cada requisito funcional referencia o Objetivo Específico do qual foi derivado,
mantendo a rastreabilidade entre o que se pretende (OE) e o que o sistema precisa fazer
para cumprir isso (RF).

### 5.1 Requisitos Funcionais

| Código | Descrição | Origem |
|---|---|---|
| RF01 | O sistema deve calcular automaticamente a próxima data de revisão de cada material de estudo, com base no algoritmo SM-2 e no desempenho informado pelo aluno | OE1 |
| RF02 | O sistema deve exibir as revisões agendadas em uma visão de calendário | OE1 |
| RF03 | O sistema deve permitir que o aluno crie uma conta e efetue login de forma segura | OE2 |
| RF04 | O sistema deve garantir que cada aluno acesse apenas o conteúdo vinculado à sua própria conta | OE2 |
| RF05 | O sistema deve permitir o cadastro de disciplinas, com nome, professor e dia da semana | OE3 |
| RF06 | O sistema deve permitir o cadastro de aulas vinculadas a uma disciplina, com data, título e duração | OE3 |
| RF07 | O sistema deve exibir disciplinas e aulas em uma visão de calendário semanal e mensal | OE3 |
| RF08 | O sistema deve permitir a criação manual de flashcards, questões e resumos vinculados a uma aula | OE4 |
| RF09 | O sistema deve apresentar o conteúdo de cada disciplina em cards que retomam o estudo de onde o aluno parou | OE4 |
| RF10 | O sistema deve montar diariamente uma fila de revisões priorizada pelo desempenho do aluno | OE5 |
| RF11 | O sistema deve exibir um painel de progresso por disciplina | OE5 |
| RF12 | O sistema deve exibir sinais visuais de incentivo, como sequência de dias estudados | OE5 |
| RF13 *(Fase 2)* | O sistema deve permitir a emissão de tokens de acesso para agentes de IA, restritos a uma disciplina ou aula específica | OE6 |
| RF14 *(Fase 2)* | O sistema deve permitir a revogação individual de um token de acesso, sem afetar os demais nem a conta principal | OE6 |

### 5.2 Requisitos Não Funcionais

| Código | Categoria | Descrição |
|---|---|---|
| RNF01 | Segurança | As senhas dos usuários devem ser armazenadas com hash (nunca em texto puro), via Spring Security |
| RNF02 | Segurança | Tokens de acesso para agentes de IA (Fase 2) nunca devem coincidir com a senha do usuário |
| RNF03 | Usabilidade | A interface deve ser responsiva, funcionando adequadamente em desktop e em dispositivos móveis |
| RNF04 | Desempenho | A fila de revisões diária deve ser calculada e exibida em tempo aceitável, mesmo com acúmulo de materiais |
| RNF05 | Portabilidade | O sistema deve rodar em Java 21 com Spring Boot, compatível com qualquer ambiente que suporte a JVM |
| RNF06 | Manutenibilidade | As regras de negócio (domínio) devem ser independentes de frameworks, facilitando testes e manutenção |
| RNF07 | Confiabilidade | O esquema do banco de dados deve ser versionado via Flyway, garantindo migrações rastreáveis e reversíveis |

## 6. Escopo e Fases

### Fase 1 — v1 (OE1 a OE5, entrega e defesa deste semestre)

- Motor de agendamento por repetição espaçada (SM-2), com visão de calendário
- Cadastro e autenticação segura de usuários
- Cadastro de disciplinas e aulas, em calendário semanal e mensal
- Criação manual de flashcards, questões e resumos, com retomada de onde o aluno parou
- Sessão diária de estudo com fila priorizada, painel de progresso e incentivo visual

### Fase 2 — planejada (OE6, depois da Fase 1 pronta)

- Sistema de tokens de acesso para agentes de IA, escopados por disciplina ou aula, com
  revogação individual

**Por que o OE6 é Fase 2, não corte:** diferente de uma simples geração de conteúdo por
IA, o sistema de tokens tem substância técnica real — modela autorização, escopo e
revogação, conceitos de segurança de verdade. O motivo de vir depois é estrutural: um
token só pode ser escopado a uma disciplina ou aula que já exista, então depende da
Fase 1 estar pronta. É também a peça de maior risco do projeto — separá-la evita que
ela atrase a entrega do que já é avaliado.

### Fora do projeto (não implementado neste trabalho)

- Hospedagem própria de transcrição de áudio ou inferência de IA no servidor do Cadência
- Aplicativo mobile nativo
- Compartilhamento de baralhos entre usuários

---

# PARTE II — DOCUMENTAÇÃO TÉCNICA DE APOIO

*Material de referência para o desenvolvimento: decisões de arquitetura, modelo de
dados e plano de sprints. Não é conteúdo formal da AC, mas embasa as decisões tomadas
nela.*

## 7. Nota de Implementação — Assistente de Importação da Agenda (OE3)

Ideia de apoio ao OE3: usar uma IA para ajudar o aluno a montar a agenda inicial
(disciplinas e aulas) a partir de uma imagem ou PDF da grade horária.

**Duas permissões separadas, não uma só:**

- **Propor** — a IA lê o documento e sugere disciplinas/aulas extraídas, preenchendo o
  formato de dados já definido no modelo
- **Criar** — só o aluno, revisando a proposta, confirma e efetiva a criação. Nenhuma
  disciplina nasce sem essa confirmação

Essa separação existe porque dar à IA o poder de criar sozinha é o ponto de maior risco
de erro silencioso (duplicata, letra borrada interpretada errada). Separando *propor*
de *criar*, o erro fica visível antes de virar dado real.

**Características do prompt usado na extração** (a escrever na implementação):

- Tarefa única e estreita — só extrair, nunca "ajudar a organizar os estudos" de forma
  aberta
- Schema de saída exato, batendo com os campos de `Disciplina` e `Aula`
- Regra explícita: campo incerto fica marcado como incerto, nunca preenchido por
  suposição
- Um exemplo de entrada/saída embutido no próprio prompt

Esse processo roda uma única vez por semestre (a grade não muda) e reaproveita o canal
de acesso do OE6 depois de a Fase 2 estar pronta. Antes disso, o cadastro pode ser
feito manualmente pelo aluno via formulário — o que já cumpre o OE3 sozinho.

## 8. Modelo de Domínio (onde a POO aparece)

`MaterialEstudo` — **classe abstrata**.
Atributos: `id`, `titulo`, `aula`, `criadoEm`.
Métodos polimórficos: `renderizar()`, `tipo()`.

- `Flashcard` (herda `MaterialEstudo`) — `frente`, `verso`
- `Resumo` (herda `MaterialEstudo`) — `corpoMd`
- `Questao` (herda `MaterialEstudo`) — `enunciado`, `gabarito`, `nivel`

### Os quatro pilares, concretamente

**Encapsulamento** — `Revisao` guarda `intervaloDias`, `fatorFacilidade` e `repeticoes`
como campos privados. A única porta de escrita é `registrarResposta(int nota)`, que
aplica a regra do SM-2 e recalcula tudo internamente.

**Herança** — `Flashcard`, `Resumo` e `Questao` compartilham identidade, título, vínculo
com a aula e data de criação, que moram em `MaterialEstudo` e não se repetem três vezes.

**Polimorfismo** — a sessão de estudo percorre uma `List<MaterialEstudo>` e chama
`renderizar()` sem saber o tipo concreto. Adicionar um quarto tipo depois não exige
tocar na sessão.

**Abstração** — a interface `AgendadorRevisao` declara
`proximaRevisao(Revisao r, int nota)`. `AgendadorSM2` é a implementação real; trocar de
algoritmo é trocar uma linha de configuração.

**Composição** — `SessaoEstudo` **tem** uma lista de `Revisao`. Não **é** uma lista de
revisões — distinção que vale ponto na defesa.

> **Nota para a Fase 2:** o OE6 vai exigir uma nova classe de domínio para representar
> o token de acesso e seu escopo. Ainda não desenhada — fica para quando chegarmos lá.

## 9. Modelo Relacional

| Tabela | Campos principais | Chaves e relacionamentos |
|---|---|---|
| `usuario` | id, nome, email, senha_hash, criado_em | PK: id · email único |
| `disciplina` | id, usuario_id, nome, professor, dia_semana | PK: id · FK: usuario_id → usuario |
| `aula` | id, disciplina_id, data, titulo, duracao_min | PK: id · FK: disciplina_id → disciplina |
| `material_estudo` | id, aula_id, tipo, titulo, criado_em | PK: id · FK: aula_id → aula |
| `flashcard` | material_id, frente, verso | PK/FK: material_id → material_estudo |
| `resumo` | material_id, corpo_md | PK/FK: material_id → material_estudo |
| `questao` | material_id, enunciado, gabarito, nivel | PK/FK: material_id → material_estudo |
| `revisao` | id, material_id, data_prevista, intervalo_dias, fator_facilidade, repeticoes, ultima_nota | PK: id · FK: material_id → material_estudo |
| `sessao_estudo` | id, usuario_id, iniciada_em, finalizada_em | PK: id · FK: usuario_id → usuario |
| `sessao_revisao` | sessao_id, revisao_id, nota, respondida_em | PK composta (sessao_id, revisao_id) · FK para sessao_estudo e revisao |

**Herança não existe em banco relacional.** O esquema acima usa a estratégia **Joined
Table** (tabela pai + uma tabela por subtipo, ligadas por PK/FK) em vez de Single Table
ou Table per Class — preserva integridade referencial e permite `NOT NULL` real nos
campos de cada subtipo. Escolher uma estratégia e justificar é conteúdo de defesa.

**Relacionamentos presentes** — 1:N (usuário→disciplinas, disciplina→aulas,
aula→materiais); N:N (sessões ↔ revisões, via `sessao_revisao` com atributos próprios);
Herança (`material_estudo` → três subtipos).

> **Nota para a Fase 2:** o OE6 vai exigir uma tabela adicional para tokens de acesso,
> ligada a `usuario` e escopada a `disciplina` ou `aula`. Desenho pendente, deixado
> para a implementação da Fase 2.

## 10. Trilha de Desenvolvimento

| Sprint | Entrega | O que ensina |
|---|---|---|
| **0 — Fundação** | Repositório, JDK 21, IntelliJ, PostgreSQL, projeto Spring Initializr, primeiro commit | Maven, estrutura de projeto Java, o que um framework faz e o que não faz |
| **1 — Domínio** | Todas as classes do modelo, sem banco e sem web. Só objetos e testes | Classes, herança, classes abstratas, interfaces, polimorfismo, encapsulamento, JUnit 5 |
| **2 — Persistência** | Migrations Flyway, entidades JPA, mapeamento da herança, repositórios | ORM, descasamento objeto-tabela, `@Inheritance(JOINED)`, transações |
| **3 — Regra de negócio** | Implementação do SM-2, serviço de sessão, fila priorizada | Separação domínio/infraestrutura, testes de algoritmo, injeção de dependência |
| **4 — Camada web** | Controllers, rotas, formulários, tratamento de erro | MVC, ciclo de requisição HTTP, validação, códigos de status |
| **5 — Interface** | Thymeleaf + Tailwind CSS | Design system, hierarquia tipográfica, acessibilidade, responsividade |
| **6 — Fechamento** | Seed de dados, README, diagramas UML, relatório, apresentação | Documentação técnica, engenharia reversa de diagrama |
| **7 — Fase 2** | Sistema de tokens de acesso para agentes de IA (OE6) | Autenticação por token vs. sessão, menor privilégio, API para máquina |

O **Sprint 1 é o que vale mais nota** na disciplina — não convém apressá-lo. O Sprint 5
é o de maior afinidade com design de interface. O Sprint 7 só começa depois que os
Sprints 0–6 estiverem fechados e entregues; não faz parte da AC atual.

## 11. Comandos Iniciais

```bash
# depois de criar o repo vazio no GitHub
git clone https://github.com/AndraxZin/Cadencia.git
cd Cadencia

git add README.md
git commit -m "docs: charter do projeto e ficha da AC"
git push origin main
```

## 12. Trabalhos Futuros

- Uso do canal de acesso do OE6 para que assistentes de IA externos transcrevam aulas e
  gravem resumos diretamente no espaço do aluno, sem que o Cadência hospede transcrição
  ou inferência própria
- Permitir que a IA crie uma disciplina nova fora do semeamento inicial (ex.: troca de
  curso no meio do semestre) — hoje a criação de estrutura é sempre confirmada
  manualmente pelo aluno
- Importação e exportação no formato Anki (`.apkg`)
- Compartilhamento de baralhos entre colegas de turma
- Aplicativo mobile para revisar no deslocamento
- Ajuste do SM-2 com base em dados reais de retenção do próprio usuário

---

*Documento vivo. Última revisão: setembro de 2026.*
