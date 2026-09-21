-- =============================================================================
-- Cadência — Dados de demonstração (seed)
--
-- Popula o banco com um cenário realista de um aluno do 4º semestre, para que
-- as consultas tenham o que retornar durante uma apresentação.
--
-- NÃO é parte do esquema. Roda DEPOIS de V1__esquema_inicial.sql, e só em
-- ambiente de desenvolvimento — nunca em produção.
--
-- COMO USAR NO pgAdmin 4:
--   Query Tool, no banco "cadencia", cole e execute (F5).
--   Pode rodar quantas vezes quiser: o TRUNCATE abaixo limpa antes de inserir.
-- =============================================================================


-- -----------------------------------------------------------------------------
-- Limpeza — deixa o seed repetível
--
-- TRUNCATE apaga todas as linhas de uma vez (mais rápido que DELETE, que apaga
-- linha a linha). CASCADE estende a limpeza às tabelas que dependem destas.
-- RESTART IDENTITY zera os contadores das chaves primárias, para os ids
-- recomeçarem do 1 a cada execução.
-- -----------------------------------------------------------------------------
TRUNCATE TABLE usuario RESTART IDENTITY CASCADE;


-- -----------------------------------------------------------------------------
-- 1. Usuário
--
-- A senha abaixo é um hash BCrypt de exemplo. O sistema nunca guarda a senha
-- em texto puro (RNF01) — nem mesmo no seed.
-- -----------------------------------------------------------------------------
INSERT INTO usuario (nome, email, senha_hash) VALUES
  ('Caio Andrax', 'caio@cadencia.dev', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');


-- -----------------------------------------------------------------------------
-- 2. Disciplinas — a grade do 4º semestre
--
-- Repare que NÃO escrevemos "usuario_id = 1". Buscamos o id pelo e-mail com uma
-- subconsulta. Motivo: a chave primária é gerada pelo banco (IDENTITY), então
-- fixar o número no script é frágil — se a ordem de inserção mudar, o vínculo
-- aponta para o usuário errado, e em silêncio. Buscar pelo dado real é seguro.
-- -----------------------------------------------------------------------------
INSERT INTO disciplina (usuario_id, nome, professor, dia_semana) VALUES
  ((SELECT id FROM usuario WHERE email = 'caio@cadencia.dev'), 'Projeto Integrador de Sistemas Computacionais', 'Henrique Dominges Garcia', 3),
  ((SELECT id FROM usuario WHERE email = 'caio@cadencia.dev'), 'Banco de Dados II',                              'Marcia Tavares',           1),
  ((SELECT id FROM usuario WHERE email = 'caio@cadencia.dev'), 'Programação Orientada a Objetos',                'Rafael Lima',              2),
  ((SELECT id FROM usuario WHERE email = 'caio@cadencia.dev'), 'Engenharia de Software',                         'Patricia Nunes',           4),
  ((SELECT id FROM usuario WHERE email = 'caio@cadencia.dev'), 'Redes de Computadores',                           'Anderson Melo',            5);


-- -----------------------------------------------------------------------------
-- 3. Aulas
--
-- Datas RELATIVAS a hoje (CURRENT_DATE), não fixas. Assim o cenário continua
-- fazendo sentido em qualquer dia que você rodar o seed — aulas no passado,
-- revisões atrasadas e revisões futuras sempre nas posições certas.
-- -----------------------------------------------------------------------------
INSERT INTO aula (disciplina_id, data, titulo, duracao_min) VALUES
  ((SELECT id FROM disciplina WHERE nome = 'Projeto Integrador de Sistemas Computacionais'), CURRENT_DATE - 14, 'Levantamento de requisitos e escopo',   100),
  ((SELECT id FROM disciplina WHERE nome = 'Projeto Integrador de Sistemas Computacionais'), CURRENT_DATE -  7, 'Modelagem do domínio',                  100),
  ((SELECT id FROM disciplina WHERE nome = 'Projeto Integrador de Sistemas Computacionais'), CURRENT_DATE,      'Modelo relacional na prática',          100),

  ((SELECT id FROM disciplina WHERE nome = 'Banco de Dados II'), CURRENT_DATE - 12, 'Normalização até a 3FN',               100),
  ((SELECT id FROM disciplina WHERE nome = 'Banco de Dados II'), CURRENT_DATE -  5, 'Chaves, índices e integridade',        100),

  ((SELECT id FROM disciplina WHERE nome = 'Programação Orientada a Objetos'), CURRENT_DATE - 11, 'Herança e classes abstratas', 100),
  ((SELECT id FROM disciplina WHERE nome = 'Programação Orientada a Objetos'), CURRENT_DATE -  4, 'Polimorfismo e interfaces',   100),

  ((SELECT id FROM disciplina WHERE nome = 'Engenharia de Software'), CURRENT_DATE - 9, 'Requisitos funcionais e não funcionais', 100),

  ((SELECT id FROM disciplina WHERE nome = 'Redes de Computadores'), CURRENT_DATE - 8, 'Modelo OSI e encapsulamento', 100);


-- =============================================================================
-- 4. Materiais de estudo — a herança com dados de verdade
--
-- Cada material nasce em DUAS etapas, porque a herança é Joined Table:
--   (a) uma linha em material_estudo, com os campos comuns
--   (b) uma linha na tabela do subtipo, com os campos próprios
--
-- É exatamente isso que o JPA vai fazer por baixo dos panos no Sprint 2 —
-- aqui está explícito.
-- =============================================================================

-- --- (a) campos comuns -------------------------------------------------------
INSERT INTO material_estudo (aula_id, tipo, titulo) VALUES
  ((SELECT id FROM aula WHERE titulo = 'Modelo relacional na prática'),   'FLASHCARD', 'Estratégias de mapeamento de herança'),
  ((SELECT id FROM aula WHERE titulo = 'Modelo relacional na prática'),   'FLASHCARD', 'Diferença entre PK e FK'),
  ((SELECT id FROM aula WHERE titulo = 'Modelo relacional na prática'),   'RESUMO',    'Joined Table: quando usar'),
  ((SELECT id FROM aula WHERE titulo = 'Modelo relacional na prática'),   'QUESTAO',   'Cardinalidade em relacionamento N:N'),

  ((SELECT id FROM aula WHERE titulo = 'Chaves, índices e integridade'),  'FLASHCARD', 'O que o índice custa'),
  ((SELECT id FROM aula WHERE titulo = 'Chaves, índices e integridade'),  'QUESTAO',   'ON DELETE CASCADE vs RESTRICT'),
  ((SELECT id FROM aula WHERE titulo = 'Normalização até a 3FN'),         'RESUMO',    'As três primeiras formas normais'),

  ((SELECT id FROM aula WHERE titulo = 'Herança e classes abstratas'),    'FLASHCARD', 'Classe abstrata x interface'),
  ((SELECT id FROM aula WHERE titulo = 'Polimorfismo e interfaces'),      'FLASHCARD', 'O que é polimorfismo'),
  ((SELECT id FROM aula WHERE titulo = 'Polimorfismo e interfaces'),      'QUESTAO',   'Sobrecarga x sobrescrita'),

  ((SELECT id FROM aula WHERE titulo = 'Requisitos funcionais e não funcionais'), 'RESUMO',    'RF x RNF: como diferenciar'),
  ((SELECT id FROM aula WHERE titulo = 'Modelo OSI e encapsulamento'),             'FLASHCARD', 'As sete camadas do OSI');

-- --- (b) campos próprios: FLASHCARD -----------------------------------------
INSERT INTO flashcard (material_id, frente, verso) VALUES
  ((SELECT id FROM material_estudo WHERE titulo = 'Estratégias de mapeamento de herança'),
   'Quais são as três estratégias de mapear herança em banco relacional?',
   'Single Table (uma tabela com coluna discriminadora), Table per Class (uma tabela por subtipo, repetindo campos comuns) e Joined Table (tabela pai + uma por subtipo, ligadas por PK/FK).'),

  ((SELECT id FROM material_estudo WHERE titulo = 'Diferença entre PK e FK'),
   'Qual a diferença entre chave primária e chave estrangeira?',
   'A PK identifica unicamente uma linha dentro da própria tabela. A FK aponta para a PK de outra tabela, criando o vínculo e garantindo que o destino exista.'),

  ((SELECT id FROM material_estudo WHERE titulo = 'O que o índice custa'),
   'Por que não criar índice em toda coluna?',
   'Índice ocupa espaço em disco e precisa ser atualizado a cada INSERT, UPDATE e DELETE — acelera a leitura às custas da escrita. Só compensa onde há consulta frequente comprovada.'),

  ((SELECT id FROM material_estudo WHERE titulo = 'Classe abstrata x interface'),
   'Quando usar classe abstrata em vez de interface?',
   'Classe abstrata quando há estado (atributos) e comportamento comum a compartilhar entre as filhas. Interface quando se quer apenas definir um contrato, sem implementação nem estado.'),

  ((SELECT id FROM material_estudo WHERE titulo = 'O que é polimorfismo'),
   'O que é polimorfismo, em uma frase?',
   'É chamar o mesmo método em objetos de tipos diferentes e cada um responder à sua maneira, sem que quem chama precise saber o tipo concreto.'),

  ((SELECT id FROM material_estudo WHERE titulo = 'As sete camadas do OSI'),
   'Quais são as sete camadas do modelo OSI, de baixo para cima?',
   'Física, Enlace, Rede, Transporte, Sessão, Apresentação e Aplicação.');

-- --- (b) campos próprios: RESUMO --------------------------------------------
INSERT INTO resumo (material_id, corpo_md) VALUES
  ((SELECT id FROM material_estudo WHERE titulo = 'Joined Table: quando usar'),
   E'## Joined Table\n\nTabela pai com os campos comuns, uma tabela por subtipo com os campos próprios.\nA ligação é uma coluna que é **PK e FK ao mesmo tempo**.\n\n**A favor:** permite NOT NULL real nos campos de cada subtipo; nenhuma coluna nula por design; existe uma tabela única para outras tabelas referenciarem.\n\n**Contra:** toda leitura de um subtipo exige JOIN com a tabela pai.'),

  ((SELECT id FROM material_estudo WHERE titulo = 'As três primeiras formas normais'),
   E'## Formas normais\n\n**1FN** — todo atributo é atômico: nada de lista dentro de uma coluna.\n\n**2FN** — está na 1FN e todo atributo não-chave depende da chave *inteira* (só importa quando a chave é composta).\n\n**3FN** — está na 2FN e nenhum atributo não-chave depende de outro atributo não-chave (sem dependência transitiva).'),

  ((SELECT id FROM material_estudo WHERE titulo = 'RF x RNF: como diferenciar'),
   E'## Requisito funcional x não funcional\n\n**RF** descreve o que o sistema *faz*: "o sistema deve permitir o cadastro de disciplinas".\n\n**RNF** descreve *como* ele faz, ou sob que restrição: desempenho, segurança, usabilidade, portabilidade.\n\nTeste prático: se dá para escrever um caso de teste que passa ou falha observando uma ação, é RF. Se é uma qualidade que atravessa o sistema todo, é RNF.');

-- --- (b) campos próprios: QUESTAO -------------------------------------------
INSERT INTO questao (material_id, enunciado, gabarito, nivel) VALUES
  ((SELECT id FROM material_estudo WHERE titulo = 'Cardinalidade em relacionamento N:N'),
   'Como se representa um relacionamento N:N em um banco relacional, e o que acontece quando esse relacionamento tem atributos próprios?',
   'Cria-se uma terceira tabela (associativa) com FKs para as duas tabelas originais. Quando o relacionamento tem atributos próprios, eles viram colunas dessa tabela associativa — como nota e respondida_em em sessao_revisao.',
   'MEDIO'),

  ((SELECT id FROM material_estudo WHERE titulo = 'ON DELETE CASCADE vs RESTRICT'),
   'Qual a diferença prática entre ON DELETE CASCADE e ON DELETE RESTRICT, e como escolher entre os dois?',
   'CASCADE apaga automaticamente as linhas filhas junto com a pai. RESTRICT impede apagar a pai enquanto houver filhas. Escolhe-se CASCADE quando a relação é de composição (a filha não faz sentido sozinha) e RESTRICT quando a filha tem existência própria.',
   'MEDIO'),

  ((SELECT id FROM material_estudo WHERE titulo = 'Sobrecarga x sobrescrita'),
   'Diferencie sobrecarga (overload) de sobrescrita (override).',
   'Sobrecarga é declarar, na mesma classe, vários métodos de mesmo nome com listas de parâmetros diferentes — resolvida em tempo de compilação. Sobrescrita é a classe filha redefinir um método herdado, mantendo a mesma assinatura — resolvida em tempo de execução, e é o que produz polimorfismo.',
   'FACIL');


-- =============================================================================
-- 5. Revisões — o estado do SM-2
--
-- Os três estados que a fila diária precisa distinguir (RF10):
--   ATRASADA  — data_prevista já passou
--   PARA HOJE — data_prevista = hoje
--   FUTURA    — ainda não chegou a hora
--
-- Repare no fator_facilidade: quanto mais baixo, mais o aluno erra aquele
-- material, e mais cedo ele volta. É isso que significa "fila priorizada
-- pelo desempenho".
-- =============================================================================

-- --- ATRASADAS: o aluno errou e não revisou. Prioridade máxima na fila. ------
INSERT INTO revisao (material_id, data_prevista, intervalo_dias, fator_facilidade, repeticoes, ultima_nota) VALUES
  ((SELECT id FROM material_estudo WHERE titulo = 'Sobrecarga x sobrescrita'),        CURRENT_DATE - 3, 1, 1.70, 1, 2),
  ((SELECT id FROM material_estudo WHERE titulo = 'As sete camadas do OSI'),          CURRENT_DATE - 2, 1, 1.96, 1, 3),
  ((SELECT id FROM material_estudo WHERE titulo = 'ON DELETE CASCADE vs RESTRICT'),   CURRENT_DATE - 1, 1, 1.30, 2, 1);

-- --- PARA HOJE ---------------------------------------------------------------
INSERT INTO revisao (material_id, data_prevista, intervalo_dias, fator_facilidade, repeticoes, ultima_nota) VALUES
  ((SELECT id FROM material_estudo WHERE titulo = 'Estratégias de mapeamento de herança'), CURRENT_DATE, 6, 2.50, 2, 4),
  ((SELECT id FROM material_estudo WHERE titulo = 'O que é polimorfismo'),                 CURRENT_DATE, 3, 2.36, 2, 4),
  ((SELECT id FROM material_estudo WHERE titulo = 'As três primeiras formas normais'),     CURRENT_DATE, 1, 2.18, 1, 3);

-- --- FUTURAS: já dominadas, intervalo longo ----------------------------------
INSERT INTO revisao (material_id, data_prevista, intervalo_dias, fator_facilidade, repeticoes, ultima_nota) VALUES
  ((SELECT id FROM material_estudo WHERE titulo = 'Diferença entre PK e FK'),        CURRENT_DATE +  6, 6,  2.60, 3, 5),
  ((SELECT id FROM material_estudo WHERE titulo = 'Classe abstrata x interface'),    CURRENT_DATE + 15, 15, 2.70, 4, 5),
  ((SELECT id FROM material_estudo WHERE titulo = 'Joined Table: quando usar'),      CURRENT_DATE +  2, 3,  2.50, 2, 4),
  ((SELECT id FROM material_estudo WHERE titulo = 'O que o índice custa'),           CURRENT_DATE +  9, 10, 2.60, 3, 5);

-- Os dois materiais restantes ficam SEM revisão de propósito: material recém
-- criado que o aluno ainda não estudou nenhuma vez. A fila precisa saber lidar
-- com isso — é o caso de "nunca revisado".


-- =============================================================================
-- 6. Sessões de estudo e respostas — o histórico e o N:N
--
-- Quatro dias seguidos de estudo, terminando ontem. Serve para demonstrar a
-- sequência de dias estudados (RF12) e o painel de progresso (RF11).
-- =============================================================================

INSERT INTO sessao_estudo (usuario_id, iniciada_em, finalizada_em) VALUES
  ((SELECT id FROM usuario WHERE email = 'caio@cadencia.dev'), CURRENT_DATE - 4 + TIME '20:10', CURRENT_DATE - 4 + TIME '20:31'),
  ((SELECT id FROM usuario WHERE email = 'caio@cadencia.dev'), CURRENT_DATE - 3 + TIME '21:05', CURRENT_DATE - 3 + TIME '21:22'),
  ((SELECT id FROM usuario WHERE email = 'caio@cadencia.dev'), CURRENT_DATE - 2 + TIME '19:48', CURRENT_DATE - 2 + TIME '20:14'),
  ((SELECT id FROM usuario WHERE email = 'caio@cadencia.dev'), CURRENT_DATE - 1 + TIME '22:00', CURRENT_DATE - 1 + TIME '22:19'),
  -- Sessão de HOJE, ainda aberta: finalizada_em fica NULL.
  ((SELECT id FROM usuario WHERE email = 'caio@cadencia.dev'), now(), NULL);


-- Respostas dadas. A mesma revisão aparece em sessões diferentes ao longo do
-- tempo — é isso que torna a relação N:N, e é por isso que a nota mora AQUI e
-- não na tabela revisao: cada encontro teve a sua.
INSERT INTO sessao_revisao (sessao_id, revisao_id, nota, respondida_em)
SELECT s.id, r.id, v.nota, s.iniciada_em + INTERVAL '4 minutes'
FROM (VALUES
    (1, 'Estratégias de mapeamento de herança', 3),
    (1, 'Diferença entre PK e FK',              4),
    (1, 'Sobrecarga x sobrescrita',             2),
    (2, 'Estratégias de mapeamento de herança', 4),
    (2, 'O que é polimorfismo',                 3),
    (2, 'As sete camadas do OSI',               3),
    (3, 'Classe abstrata x interface',          5),
    (3, 'O que o índice custa',                 5),
    (3, 'ON DELETE CASCADE vs RESTRICT',        1),
    (4, 'Joined Table: quando usar',            4),
    (4, 'As três primeiras formas normais',     3),
    (4, 'Diferença entre PK e FK',              5)
  ) AS v(sessao_num, material_titulo, nota)
JOIN sessao_estudo s  ON s.id = v.sessao_num
JOIN material_estudo m ON m.titulo = v.material_titulo
JOIN revisao r         ON r.material_id = m.id;


-- =============================================================================
-- 7. Conferência — o que entrou
-- =============================================================================
SELECT 'usuario'          AS tabela, count(*) FROM usuario
UNION ALL SELECT 'disciplina',      count(*) FROM disciplina
UNION ALL SELECT 'aula',            count(*) FROM aula
UNION ALL SELECT 'material_estudo', count(*) FROM material_estudo
UNION ALL SELECT '  flashcard',     count(*) FROM flashcard
UNION ALL SELECT '  resumo',        count(*) FROM resumo
UNION ALL SELECT '  questao',       count(*) FROM questao
UNION ALL SELECT 'revisao',         count(*) FROM revisao
UNION ALL SELECT 'sessao_estudo',   count(*) FROM sessao_estudo
UNION ALL SELECT 'sessao_revisao',  count(*) FROM sessao_revisao
ORDER BY 1;
