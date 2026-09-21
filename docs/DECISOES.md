# Registro de Decisões — Cadência

> Material de estudo para a defesa. Cada entrada responde: que problema apareceu,
> o que foi considerado, o que foi escolhido, e **o que se perde com a escolha**.
> Escrito para alguém que vai reler isto em três meses — inclusive eu.

---

## [2026-09-21] Estratégia de mapeamento da herança no banco

**Contexto:** `MaterialEstudo` é uma classe abstrata com três subtipos (`Flashcard`,
`Resumo`, `Questao`). Banco relacional não tem herança — a tabela é plana. Era preciso
escolher como representar essa hierarquia em tabelas.

**Alternativas:**

- **Single Table** — uma tabela só, com todas as colunas de todos os subtipos e uma
  coluna `tipo` discriminadora. Simples e rápida (sem `JOIN`), mas toda coluna de
  subtipo precisa aceitar `NULL`: a coluna `frente` fica nula em toda linha que for
  um Resumo. Perde-se a possibilidade de declarar `NOT NULL` de verdade.
- **Table per Class** — uma tabela independente por subtipo, cada uma repetindo as
  colunas comuns (`titulo`, `criado_em`, `aula_id`). Sem `NULL` artificial, mas o
  campo comum existe em triplicata e não há uma tabela única para referenciar — a
  tabela `revisao` não teria a quem apontar sua chave estrangeira.
- **Joined Table** — tabela pai `material_estudo` com os campos comuns, mais uma
  tabela por subtipo contendo só os campos próprios, ligada ao pai por uma chave que
  é PK e FK ao mesmo tempo.

**Escolha:** Joined Table.

**Motivo:** é a única das três que preserva as duas coisas que importam aqui ao mesmo
tempo — integridade referencial (existe uma tabela `material_estudo` única para
`revisao` referenciar) e `NOT NULL` real nos campos de cada subtipo (`frente` é
obrigatória na tabela `flashcard`, e a tabela `flashcard` só tem linhas de flashcards).

**O que se perde:** toda leitura de um material concreto exige um `JOIN` entre a tabela
pai e a do subtipo. É mais lento que Single Table. Aceitamos esse custo porque o volume
de dados de um estudante individual não justifica otimizar, e porque a integridade do
modelo é o que a disciplina avalia — não o desempenho.

**Onde aparece:** `docs/CHARTER.md` §9.

---

## [2026-09-21] O pacote `dominio` não importa framework

**Contexto:** o caminho mais curto em Spring Boot é anotar as classes de domínio
diretamente com `@Entity`, `@Id`, `@Column` e usá-las como entidades de persistência.
Menos código, menos arquivos.

**Alternativas:**

- **Classes anotadas** — uma classe só serve de domínio e de entidade JPA. Zero
  duplicação. Mas a classe fica coberta de anotações de infraestrutura.
- **Domínio isolado** — o pacote `dominio` é Java puro; a persistência tem suas
  próprias classes, e algo traduz entre os dois.

**Escolha:** domínio isolado.

**Motivo:** o pacote `dominio` é onde a disciplina avalia POO. Se `Flashcard` estiver
coberta de anotações de JPA e Spring, herança e polimorfismo ficam escondidos atrás de
infraestrutura, e a classe deixa de ser apresentável numa defesa. Separando, dá para
abrir a classe na banca e mostrar o conceito sem ruído. Alinhado ao RNF06 do charter.

**O que se perde:** duplicação real entre entidade de domínio e entidade de
persistência, e o código de tradução entre as duas. É trabalho a mais, assumido de
propósito.

---

## [2026-09-21] Calendário entra no escopo da v1

**Contexto:** o `CLAUDE.md` descrevia o escopo da v1 apenas como "CRUD de disciplinas e
aulas". O charter, escrito depois e mais detalhado, deriva de OE1 e OE3 dois requisitos
de calendário: RF02 (revisões agendadas em visão de calendário) e RF07 (disciplinas e
aulas em calendário semanal e mensal). Os dois documentos divergiam.

**Alternativas:**

- **Calendário na v1** — cumpre OE1 e OE3 como escritos no charter. É a peça de maior
  peso de interface do projeto.
- **Calendário depois** — v1 entrega listas simples; calendário vira melhoria. Entrega
  mais rápida, mas OE1 e OE3 ficariam cumpridos só pela metade em relação ao que o
  charter promete ao professor.

**Escolha:** calendário na v1.

**Motivo:** os objetivos específicos já foram entregues ao professor com o calendário
dentro. Reduzir o escopo depois de apresentar os OEs significaria defender um sistema
menor do que o prometido. Soma-se a isso que a interface é a área de maior afinidade e
interesse do autor — o Sprint 5 deixa de ser um custo e passa a ser onde o projeto
ganha diferencial.

**O que se perde:** o Sprint 4/5 fica sensivelmente maior. Um calendário semanal e
mensal responsivo é mais trabalho que uma tabela, e é trabalho que a rubrica de POO e
modelagem relacional não pontua diretamente. O risco é ele consumir tempo que faltaria
no Sprint 1, que é o que de fato vale nota. Mitigação: o calendário só começa depois do
domínio testado e da persistência fechada.

---

## [2026-09-21] Charter versionado no repositório

**Contexto:** o documento com os requisitos, o modelo de domínio e o modelo relacional
existia apenas como um arquivo `.docx` fora do Git. O repositório tinha só o `README.md`
e o `CLAUDE.md` — a modelagem inteira vivia fora do controle de versão.

**Alternativas:**

- **Manter só o `.docx`** — formato final pronto para entregar ao professor, mas
  binário: o Git não mostra o que mudou entre versões, e o arquivo pode simplesmente
  se perder.
- **Converter para Markdown e versionar** — histórico de alterações legível, e o
  documento fica ao lado do código que ele descreve.

**Escolha:** `docs/CHARTER.md` em Markdown como fonte de verdade.

**Motivo:** o charter é referenciado o tempo todo durante o desenvolvimento (números de
RF, nomes de campo, nomes de tabela). Precisa estar onde o código está, e precisa ter
histórico — quando um requisito mudar, é preciso enxergar o que mudou e quando.

**O que se perde:** a entrega formal da AC provavelmente precisa ser `.docx` ou PDF, o
que obriga a exportar o Markdown na hora de entregar. Passa a existir o risco das duas
cópias divergirem. Convenção adotada: **o Markdown é a fonte de verdade**; o `.docx` é
sempre gerado a partir dele, nunca editado direto.

---

## [2026-09-21] Decisões de DDL do esquema inicial

**Contexto:** ao traduzir o modelo relacional do charter §9 para SQL executável,
quatro detalhes não estavam decididos no papel e precisaram de escolha.

### 1. Geração da chave primária

**Alternativas:** `BIGSERIAL` (atalho histórico do PostgreSQL, muito comum em material
didático) · `BIGINT GENERATED ALWAYS AS IDENTITY` (padrão SQL:2003) · `UUID`.

**Escolha:** `IDENTITY`.

**Motivo:** é o padrão SQL atual e o recomendado pela própria documentação do
PostgreSQL. `SERIAL` cria uma sequência "solta", que pode sobreviver à coluna que a
usava e acaba desalinhada; `IDENTITY` amarra a sequência à coluna. UUID foi descartado
porque não há sistema distribuído aqui — seria complexidade sem problema que a
justifique.

**O que se perde:** `SERIAL` é o que aparece na maioria dos tutoriais, então `IDENTITY`
pode gerar a pergunta "por que diferente?" na defesa. A resposta acima cobre isso —
e a pergunta, se vier, é oportunidade, não risco.

### 2. Comportamento ao apagar (`ON DELETE`)

**Alternativas:** `CASCADE` (apagar o pai apaga os filhos) · `RESTRICT` (proíbe apagar
o pai enquanto houver filhos) · `SET NULL` (órfão com referência nula).

**Escolha:** `CASCADE` em toda a cadeia
`usuario → disciplina → aula → material_estudo → subtipos`.

**Motivo:** são relações de composição, não de associação. Uma aula não existe fora de
uma disciplina; um flashcard não existe fora de uma aula. Manter esses registros depois
de apagar o pai produziria lixo referencial sem significado.

**O que se perde:** `CASCADE` é destrutivo e silencioso — apagar um usuário por engano
leva junto todo o material de estudo dele, sem aviso. Mitigação prevista para quando
houver tela de exclusão: confirmação explícita na aplicação antes do `DELETE`.

### 3. Cardinalidade entre `material_estudo` e `revisao`

**Contexto:** o charter não explicita se um material tem uma revisão ou várias.

**Alternativas:** 1:N, tratando `revisao` como histórico de cada revisão feita · 1:1,
tratando `revisao` como o estado atual do agendamento.

**Escolha:** 1:1, garantido por `UNIQUE (material_id)`.

**Motivo:** os campos da tabela decidem a questão. `intervalo_dias`,
`fator_facilidade`, `repeticoes` e `data_prevista` descrevem *a situação atual* de um
material no algoritmo SM-2, não um evento passado. O histórico já tem lugar próprio:
`sessao_revisao`, que guarda nota e data de cada resposta. Duas linhas de `revisao`
para o mesmo material significariam dois agendamentos concorrentes — estado
inconsistente.

**O que se perde:** o estado anterior do SM-2 não é preservado a cada atualização — só
a nota fica registrada em `sessao_revisao`. Se um dia for preciso reconstruir a
evolução do `fator_facilidade` no tempo (item de "Trabalhos Futuros": ajustar o SM-2
com dados reais de retenção), será necessária uma tabela de histórico à parte.

### 4. Ausência de índices

**Escolha:** nenhum `CREATE INDEX` no esquema inicial.

**Motivo:** o PostgreSQL já indexa `PRIMARY KEY` e `UNIQUE` automaticamente. Índices
adicionais — em `revisao.data_prevista` ou nas colunas de chave estrangeira, que o
PostgreSQL **não** indexa sozinho — provavelmente vão ajudar a fila diária (RNF04), mas
ainda não existe medição que prove isso, e índice custa espaço e torna cada `INSERT`
mais lento.

**O que se perde:** a primeira consulta pesada vai ser lenta. É aceitável: otimizar
antes de medir é adivinhação, e a correção vem em uma migration `V2` quando houver
volume real.

**Validação:** o esquema foi executado contra um PostgreSQL 16.13 real. Sete tentativas
de inserção inválida (e-mail duplicado, `dia_semana = 9`, tipo de material inexistente,
`fator_facilidade` abaixo de 1.3, nota 9, FK órfã, revisão duplicada) foram todas
recusadas pelo banco, e o `CASCADE` zerou as sete tabelas a partir de um único `DELETE`
na raiz.

---

## [2026-09-21] Formato e momento dos diagramas

**Contexto:** a trilha do charter (§10, Sprint 6) prevê "diagramas UML" como entrega.
Ao pedir um diagrama do banco, ficou evidente que "UML" estava sendo usado como termo
guarda-chuva para *qualquer* diagrama — mas DER e diagrama de classes UML são coisas
diferentes, e essa distinção é conteúdo de defesa.

**Distinção adotada:**

- **DER** (Entidade-Relacionamento) representa **tabelas**. É o modelo do banco.
- **Diagrama de classes UML** representa **classes Java**. É o modelo do código.

Os dois não coincidem de propósito: `MaterialEstudo` é *uma* classe abstrata no UML e
*quatro* tabelas no DER. Essa não-coincidência é o descasamento objeto-tabela que o
Sprint 2 vai tratar — é o motivo de o ORM existir.

**Escolha de momento:** DER agora; UML de classes no Sprint 1.

**Motivo:** o esquema do banco já existe e está testado, então o DER descreve algo
real. As classes Java ainda não existem — um UML feito agora seria desenho de intenção,
e teria que ser refeito assim que o código divergisse. Diagrama gerado a partir de
código existente não mente.

**Escolha de formato:** Mermaid, versionado como texto, renderizado para SVG e PNG.

**Alternativas:** ferramenta gráfica (draw.io, brModelo) exportando imagem · Mermaid ·
PlantUML.

**Motivo:** o Mermaid é texto, então o Git mostra exatamente o que mudou quando uma
coluna é alterada — uma imagem binária só mostra "arquivo modificado". O GitHub também
renderiza Mermaid nativamente dentro do Markdown, sem instalar nada. SVG e PNG ficam
versionados junto para impressão e para colar em slides.

**O que se perde:** o layout é decidido pelo Mermaid, não por mim. Não dá para arrastar
uma tabela para melhorar a estética, e as linhas de relacionamento podem se cruzar mais
do que se o desenho fosse manual. Para a entrega final, se a estética pesar, dá para
redesenhar em ferramenta gráfica usando este DER como referência.

**Validação:** a sintaxe foi renderizada de verdade antes de commitar — e o teste pegou
um erro real (`PK_FK` não existe em Mermaid; a forma correta de marcar chave dupla é
`PK, FK`). Sem renderizar, o diagrama teria ido para o repositório quebrado.

---

## [2026-09-21] Manutenção do nome "Cadência" e a convenção de acento

**Contexto:** ao configurar o deploy do banco, surgiu a dúvida de trocar o nome do
projeto. O incômodo relatado foi que "no GitHub ficou estranho" e que a ligação entre o
nome e o produto não estava clara.

**Investigação:** o problema não era o nome. O `README.md` tinha como título
`# Cad-ncia` — um hífen literal no lugar do `ê`, não uma falha de codificação (o resto
do arquivo trazia "repetição" e "espaçada" corretos em UTF-8). Como o GitHub renderiza
o README em destaque no topo da página, o nome do projeto aparecia quebrado para
qualquer visitante.

**Alternativas consideradas:** manter "Cadência" · trocar por "Compasso" (sem acento,
metáfora equivalente, e o instrumento de desenho oferece símbolo gráfico pronto) ·
trocar por "Sinapse" (ângulo neurocientífico) · trocar por "Andamento".

**Escolha:** manter **Cadência**.

**Motivo:** a metáfora sustenta a tese do produto melhor que qualquer alternativa. Em
música, cadência é a sequência que resolve uma frase; no uso comum, é ritmo constante.
O sistema não promete memorizar mais — promete calcular **quando voltar**. Essa é
exatamente a ideia da repetição espaçada, e o nome a entrega numa frase. Além disso,
o charter da AC já circula com esse nome, e trocar introduziria divergência entre o
documento entregue ao professor e o repositório.

**O que se perde:** "Cadence" colide com software conhecido — a Cadence Design Systems
e o workflow engine Cadence (Uber/Temporal). Para um projeto acadêmico isso é
irrelevante, mas se um dia virar portfólio público, a busca pelo nome não será limpa.

### Convenção de acento, decidida junto

**Marca com acento, identificador técnico sem.**

| Contexto | Forma |
|---|---|
| Nome do produto, títulos, documentação | Cadência |
| Repositório no GitHub | `Cadencia` |
| Banco de dados | `cadencia` |
| Pacote Java | `br.edu.unieuro.cadencia` |

**Motivo:** um repositório chamado `Cadência` produziria a URL
`github.com/AndraxZin/Cad%C3%AAncia` — o navegador percent-encoda o `ê` e o link fica
ilegível ao ser copiado. O mesmo vale para nome de banco e pacote Java, onde caractere
acentuado é fonte de problema entre sistemas operacionais e codificações diferentes.
Separar as duas camadas elimina o atrito sem sacrificar a marca.

**Ação tomada:** o `README.md` foi reescrito com o acento correto e passou a explicar a
origem do nome logo no topo, antes de qualquer detalhe técnico — a metáfora é argumento
de defesa, não enfeite.
