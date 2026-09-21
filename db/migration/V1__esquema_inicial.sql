-- =============================================================================
-- Cadência — Esquema inicial
-- PostgreSQL 16
--
-- Referência: docs/CHARTER.md §9 (Modelo Relacional)
--
-- COMO USAR NO pgAdmin 4:
--   1. Crie um banco chamado "cadencia" (botão direito em Databases > Create)
--   2. Selecione esse banco e abra Tools > Query Tool
--   3. Cole este arquivo inteiro e execute (F5)
--
-- O nome do arquivo (V1__esquema_inicial.sql) segue a convenção do Flyway,
-- a ferramenta que versiona o esquema (RNF07). Mas o conteúdo é SQL puro:
-- roda igual no pgAdmin, no psql ou pelo Flyway. Um arquivo, dois usos.
--
-- A ORDEM DAS TABELAS IMPORTA: uma tabela só pode referenciar (FOREIGN KEY)
-- outra que já exista. Por isso a criação segue de "pai" para "filho".
-- =============================================================================


-- -----------------------------------------------------------------------------
-- usuario — a raiz de tudo. Todo dado do sistema pertence a algum usuário.
-- -----------------------------------------------------------------------------
CREATE TABLE usuario (
    id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    senha_hash  VARCHAR(255) NOT NULL,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- RF03/RNF01: dois usuários não podem ter o mesmo e-mail. Essa restrição
    -- vive NO BANCO, não só na aplicação: mesmo que alguém insira dados por
    -- fora do sistema, a regra continua valendo.
    CONSTRAINT uq_usuario_email UNIQUE (email)
);

COMMENT ON TABLE  usuario            IS 'Aluno que usa a plataforma. Raiz de propriedade de todos os dados (RF04).';
COMMENT ON COLUMN usuario.senha_hash IS 'Hash da senha (RNF01). NUNCA a senha em texto puro.';


-- -----------------------------------------------------------------------------
-- disciplina — matéria da grade curricular do aluno. 1 usuário : N disciplinas
-- -----------------------------------------------------------------------------
CREATE TABLE disciplina (
    id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id  BIGINT       NOT NULL,
    nome        VARCHAR(120) NOT NULL,
    professor   VARCHAR(120),              -- opcional: nem toda disciplina tem professor cadastrado
    dia_semana  SMALLINT     NOT NULL,

    CONSTRAINT fk_disciplina_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
        ON DELETE CASCADE,

    -- Dia da semana guardado como número (1=segunda ... 7=domingo) em vez de
    -- texto livre. O CHECK faz o banco recusar qualquer valor fora da faixa —
    -- é impossível existir uma disciplina no "dia 9".
    CONSTRAINT ck_disciplina_dia_semana CHECK (dia_semana BETWEEN 1 AND 7)
);

COMMENT ON TABLE  disciplina            IS 'Matéria da grade do aluno (RF05).';
COMMENT ON COLUMN disciplina.dia_semana IS '1=segunda, 2=terça, 3=quarta, 4=quinta, 5=sexta, 6=sábado, 7=domingo (ISO-8601).';


-- -----------------------------------------------------------------------------
-- aula — encontro de uma disciplina numa data. 1 disciplina : N aulas
-- -----------------------------------------------------------------------------
CREATE TABLE aula (
    id             BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    disciplina_id  BIGINT       NOT NULL,
    data           DATE         NOT NULL,
    titulo         VARCHAR(200) NOT NULL,
    duracao_min    INTEGER,

    CONSTRAINT fk_aula_disciplina
        FOREIGN KEY (disciplina_id) REFERENCES disciplina (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_aula_duracao CHECK (duracao_min IS NULL OR duracao_min > 0)
);

COMMENT ON TABLE aula IS 'Encontro de uma disciplina em uma data (RF06). Alimenta o calendário semanal/mensal (RF07).';


-- =============================================================================
-- HERANÇA — estratégia Joined Table
--
-- No Java, MaterialEstudo é uma classe abstrata com três filhas: Flashcard,
-- Resumo e Questao. Banco relacional não tem herança — a tabela é plana.
--
-- A estratégia escolhida (ver docs/DECISOES.md) resolve assim:
--   - material_estudo  guarda o que é COMUM aos três (id, titulo, aula, data)
--   - flashcard/resumo/questao guardam só o que é PRÓPRIO de cada um
--   - a ligação é feita por uma coluna que é PRIMARY KEY e FOREIGN KEY ao mesmo
--     tempo: ela identifica a linha E aponta para o pai
--
-- O ganho: cada campo de subtipo pode ser NOT NULL DE VERDADE. Numa tabela
-- única, a coluna "frente" teria que aceitar NULL, porque um Resumo não tem
-- frente — e aí o banco não conseguiria garantir que todo flashcard tem uma.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- material_estudo — a tabela PAI da hierarquia. 1 aula : N materiais
-- -----------------------------------------------------------------------------
CREATE TABLE material_estudo (
    id         BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    aula_id    BIGINT       NOT NULL,
    tipo       VARCHAR(20)  NOT NULL,
    titulo     VARCHAR(200) NOT NULL,
    criado_em  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_material_aula
        FOREIGN KEY (aula_id) REFERENCES aula (id)
        ON DELETE CASCADE,

    -- Coluna DISCRIMINADORA: diz qual é o subtipo sem precisar consultar as três
    -- tabelas filhas. Estritamente falando ela é redundante (dá para descobrir o
    -- tipo vendo em qual tabela filha a linha existe), mas evita três JOINs numa
    -- listagem simples. É uma troca consciente de normalização por praticidade.
    CONSTRAINT ck_material_tipo CHECK (tipo IN ('FLASHCARD', 'RESUMO', 'QUESTAO'))
);

COMMENT ON TABLE  material_estudo      IS 'Tabela PAI da herança. Campos comuns a Flashcard, Resumo e Questao (RF08).';
COMMENT ON COLUMN material_estudo.tipo IS 'Discriminador: indica em qual tabela filha está o restante dos dados.';


-- -----------------------------------------------------------------------------
-- flashcard — subtipo. Pergunta na frente, resposta no verso.
-- -----------------------------------------------------------------------------
CREATE TABLE flashcard (
    -- material_id é PK e FK ao mesmo tempo: isso garante 1 linha de flashcard
    -- para no máximo 1 material, e nenhum flashcard órfão.
    material_id  BIGINT PRIMARY KEY,
    frente       TEXT   NOT NULL,
    verso        TEXT   NOT NULL,

    CONSTRAINT fk_flashcard_material
        FOREIGN KEY (material_id) REFERENCES material_estudo (id)
        ON DELETE CASCADE
);

COMMENT ON TABLE flashcard IS 'Subtipo de material_estudo. Campos próprios: frente e verso.';


-- -----------------------------------------------------------------------------
-- resumo — subtipo. Texto corrido em Markdown.
-- -----------------------------------------------------------------------------
CREATE TABLE resumo (
    material_id  BIGINT PRIMARY KEY,
    corpo_md     TEXT   NOT NULL,

    CONSTRAINT fk_resumo_material
        FOREIGN KEY (material_id) REFERENCES material_estudo (id)
        ON DELETE CASCADE
);

COMMENT ON TABLE  resumo          IS 'Subtipo de material_estudo. Campo próprio: corpo em Markdown.';
COMMENT ON COLUMN resumo.corpo_md IS 'Texto em Markdown, renderizado na exibição.';


-- -----------------------------------------------------------------------------
-- questao — subtipo. Enunciado com gabarito e nível de dificuldade.
-- -----------------------------------------------------------------------------
CREATE TABLE questao (
    material_id  BIGINT      PRIMARY KEY,
    enunciado    TEXT        NOT NULL,
    gabarito     TEXT        NOT NULL,
    nivel        VARCHAR(10) NOT NULL,

    CONSTRAINT fk_questao_material
        FOREIGN KEY (material_id) REFERENCES material_estudo (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_questao_nivel CHECK (nivel IN ('FACIL', 'MEDIO', 'DIFICIL'))
);

COMMENT ON TABLE questao IS 'Subtipo de material_estudo. Campos próprios: enunciado, gabarito e nível.';


-- =============================================================================
-- AGENDAMENTO — o estado do SM-2
-- =============================================================================

-- -----------------------------------------------------------------------------
-- revisao — o ESTADO ATUAL de agendamento de um material.
--
-- Atenção, isto não é histórico: cada material tem UMA linha aqui, que é
-- atualizada a cada resposta. O histórico de respostas mora em sessao_revisao.
-- -----------------------------------------------------------------------------
CREATE TABLE revisao (
    id                BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    material_id       BIGINT       NOT NULL,
    data_prevista     DATE         NOT NULL,
    intervalo_dias    INTEGER      NOT NULL DEFAULT 0,
    fator_facilidade  NUMERIC(4,2) NOT NULL DEFAULT 2.50,
    repeticoes        INTEGER      NOT NULL DEFAULT 0,
    ultima_nota       SMALLINT,

    CONSTRAINT fk_revisao_material
        FOREIGN KEY (material_id) REFERENCES material_estudo (id)
        ON DELETE CASCADE,

    -- 1 material : 1 revisao. Ver nota acima e docs/DECISOES.md.
    CONSTRAINT uq_revisao_material UNIQUE (material_id),

    -- Regras do próprio SM-2, aplicadas pelo banco:
    CONSTRAINT ck_revisao_fator      CHECK (fator_facilidade >= 1.30),
    CONSTRAINT ck_revisao_intervalo  CHECK (intervalo_dias >= 0),
    CONSTRAINT ck_revisao_repeticoes CHECK (repeticoes >= 0),
    CONSTRAINT ck_revisao_nota       CHECK (ultima_nota IS NULL OR ultima_nota BETWEEN 0 AND 5)
);

COMMENT ON TABLE  revisao                  IS 'Estado atual do agendamento SM-2 de um material (RF01). Uma linha por material.';
COMMENT ON COLUMN revisao.fator_facilidade IS 'Fator E do SM-2. Começa em 2.5 e nunca desce de 1.3 — piso do algoritmo.';
COMMENT ON COLUMN revisao.ultima_nota      IS 'Última autoavaliação, 0 a 5 na escala do SM-2. NULL enquanto nunca revisado.';
COMMENT ON COLUMN revisao.data_prevista    IS 'Quando o material deve reaparecer. Alimenta o calendário de revisões (RF02).';


-- -----------------------------------------------------------------------------
-- sessao_estudo — uma rodada de estudo do aluno. 1 usuário : N sessões
-- -----------------------------------------------------------------------------
CREATE TABLE sessao_estudo (
    id             BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id     BIGINT      NOT NULL,
    iniciada_em    TIMESTAMPTZ NOT NULL DEFAULT now(),
    finalizada_em  TIMESTAMPTZ,              -- NULL = sessão ainda em andamento

    CONSTRAINT fk_sessao_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_sessao_ordem_datas
        CHECK (finalizada_em IS NULL OR finalizada_em >= iniciada_em)
);

COMMENT ON TABLE  sessao_estudo               IS 'Rodada diária de estudo (RF10). Base da sequência de dias estudados (RF12).';
COMMENT ON COLUMN sessao_estudo.finalizada_em IS 'NULL enquanto a sessão está aberta.';


-- -----------------------------------------------------------------------------
-- sessao_revisao — N:N entre sessões e revisões, COM ATRIBUTOS PRÓPRIOS.
--
-- Uma sessão passa por várias revisões; uma revisão reaparece em várias sessões
-- ao longo do semestre. A tabela do meio não serve só para ligar: ela guarda o
-- que aconteceu naquele encontro específico — que nota o aluno deu e quando.
-- É aqui que mora o HISTÓRICO.
-- -----------------------------------------------------------------------------
CREATE TABLE sessao_revisao (
    sessao_id      BIGINT      NOT NULL,
    revisao_id     BIGINT      NOT NULL,
    nota           SMALLINT    NOT NULL,
    respondida_em  TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Chave primária COMPOSTA: a identidade da linha são as duas colunas juntas.
    -- Efeito colateral útil: a mesma revisão não pode ser respondida duas vezes
    -- dentro da mesma sessão.
    CONSTRAINT pk_sessao_revisao PRIMARY KEY (sessao_id, revisao_id),

    CONSTRAINT fk_sessao_revisao_sessao
        FOREIGN KEY (sessao_id) REFERENCES sessao_estudo (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sessao_revisao_revisao
        FOREIGN KEY (revisao_id) REFERENCES revisao (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_sessao_revisao_nota CHECK (nota BETWEEN 0 AND 5)
);

COMMENT ON TABLE  sessao_revisao      IS 'Associativa N:N entre sessao_estudo e revisao, com atributos próprios. Guarda o histórico de respostas.';
COMMENT ON COLUMN sessao_revisao.nota IS 'Autoavaliação de 0 a 5 na escala do SM-2. Entrada do recálculo do agendamento.';


-- =============================================================================
-- NOTA SOBRE ÍNDICES
--
-- Não há CREATE INDEX neste arquivo de propósito. O PostgreSQL cria índice
-- sozinho para PRIMARY KEY e UNIQUE, mas NÃO para FOREIGN KEY.
--
-- Índices em colunas como revisao.data_prevista ou aula.disciplina_id
-- provavelmente vão ajudar o desempenho da fila diária (RNF04) — mas ainda não
-- existe medição que prove isso, e índice tem custo: ocupa espaço e torna cada
-- INSERT mais lento. Ficam para quando houver volume real e um problema medido.
-- =============================================================================
