-- =====================================================================
-- Cadência - modelo físico (DDL) para PostgreSQL
-- Testado no PostgreSQL 16.15.
--
-- Como executar (cria as tabelas num banco que já exista):
--   psql -U postgres -c "CREATE DATABASE cadencia;"
--   psql -U postgres -d cadencia -f sql/schema.sql
--
-- Para recriar do zero (APAGA OS DADOS!), rode antes:
--   DROP TABLE IF EXISTS revisao, flashcard, disciplina;
--
-- Relacionamentos (todos 1:N):
--   disciplina 1 --- N flashcard 1 --- N revisao
-- =====================================================================

-- ---------------------------------------------------------------------
-- DISCIPLINA: matéria da faculdade (RF-01).
-- O nome é único. (A comparação sem diferenciar maiúscula/minúscula
-- é feita no DisciplinaService, antes de chegar aqui.)
-- ---------------------------------------------------------------------
CREATE TABLE disciplina (
    id_disciplina INT GENERATED ALWAYS AS IDENTITY,
    nome          VARCHAR(100) NOT NULL,
    professor     VARCHAR(100),
    CONSTRAINT pk_disciplina      PRIMARY KEY (id_disciplina),
    CONSTRAINT uk_disciplina_nome UNIQUE (nome)
);

-- ---------------------------------------------------------------------
-- FLASHCARD: cartão de estudo de uma disciplina (RF-02).
-- etapa vai de 0 a 3 e define o intervalo da próxima revisão:
--   0 = 1 dia | 1 = 3 dias | 2 = 7 dias | 3 = 15 dias (RF-05).
-- proxima_revisao é um dado DERIVADO (poderia ser calculado a partir do
-- histórico). Guardamos a data pronta para a consulta "o que vence hoje"
-- ser um simples WHERE. Decisão registrada em docs/DECISOES.md.
-- Apagar a disciplina apaga seus flashcards (RF-07).
-- ---------------------------------------------------------------------
CREATE TABLE flashcard (
    id_flashcard    INT GENERATED ALWAYS AS IDENTITY,
    id_disciplina   INT      NOT NULL,
    frente          TEXT     NOT NULL,
    verso           TEXT     NOT NULL,
    etapa           SMALLINT NOT NULL DEFAULT 0,
    proxima_revisao DATE     NOT NULL,
    CONSTRAINT pk_flashcard PRIMARY KEY (id_flashcard),
    CONSTRAINT fk_flashcard_disciplina FOREIGN KEY (id_disciplina)
        REFERENCES disciplina (id_disciplina) ON DELETE CASCADE,
    CONSTRAINT chk_flashcard_etapa CHECK (etapa BETWEEN 0 AND 3)
);

-- ---------------------------------------------------------------------
-- REVISAO: histórico. Cada resposta do aluno vira uma linha (RF-06).
-- Apagar o flashcard apaga o seu histórico.
-- ---------------------------------------------------------------------
CREATE TABLE revisao (
    id_revisao   INT GENERATED ALWAYS AS IDENTITY,
    id_flashcard INT         NOT NULL,
    revisado_em  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acertou      BOOLEAN     NOT NULL,
    CONSTRAINT pk_revisao PRIMARY KEY (id_revisao),
    CONSTRAINT fk_revisao_flashcard FOREIGN KEY (id_flashcard)
        REFERENCES flashcard (id_flashcard) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- ÍNDICES: aceleram os JOINs por chave estrangeira e a busca da revisão do dia.
-- ---------------------------------------------------------------------
CREATE INDEX idx_flashcard_disciplina      ON flashcard (id_disciplina);
CREATE INDEX idx_flashcard_proxima_revisao ON flashcard (proxima_revisao);
CREATE INDEX idx_revisao_flashcard         ON revisao (id_flashcard);
