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

---

## [2026-11-16] Documento de Requisitos no formato da disciplina de Engenharia de Requisitos

**Contexto:** o Caio pediu que os requisitos do Cadência seguissem a formatação do documento de requisitos usado na disciplina de Engenharia de Requisitos.

**Alternativas:** manter só a seção 3 do `DOCUMENTACAO.md`; ou criar um documento próprio, no formato do modelo.

**Escolha:** `docs/REQUISITOS.md` (fonte) e `docs/Cadencia-Documento-de-Requisitos.pdf`, com capa, visão do negócio, escopo positivo e negativo, atores, 7 casos de uso detalhados, diagrama UML de casos de uso (PlantUML), requisitos funcionais e não funcionais, regras de negócio, telas do terminal, especificação de telas e considerações finais. O `DOCUMENTACAO.md` continua no formato do guia do professor Henrique e aponta para o novo documento. Os identificadores seguem o padrão do projeto (RF-01, RN-01) para casar com o resto da documentação.

**Motivo:** a especificação fica completa e serve de base para a matriz de rastreabilidade e para os testes. O que se perde: são dois documentos que precisam continuar coerentes. Junto com isso, o RF-03 passou a se chamar "Listagem" (não existe busca por texto) e entraram o RNF-06 (funciona em memória sem banco), o RNF-07 (senha em variável de ambiente) e o RNF-08 (usabilidade). O diagrama de casos de uso foi feito pelo Claude; se o Guilherme já tiver o dele, ele substitui este.

**Quem decidiu:** Caio, dentro da exceção ao contrato de 16/11.

---

## [2026-11-16] Testes com JUnit: unitários em memória e de integração com o PostgreSQL

**Contexto:** a documentação promete testes unitários e de integração com JUnit (objetivo 4), e o professor vai avaliar se o que foi prometido foi cumprido.

**Alternativas:** tirar a promessa do documento; testar só os services; ou testar os services (unitários) e os DAOs JDBC (integração).

**Escolha:** 14 testes unitários dos services usando os DAOs em memória (rodam sem banco) e 6 de integração dos DAOs JDBC com o PostgreSQL. Cada teste de integração roda numa transação desfeita no fim (rollback), e todos são pulados se não houver banco. O `.jar` do JUnit (`junit-platform-console-standalone-1.9.1.jar`) veio do pacote `junit5` do Ubuntu, porque o site oficial (Maven Central) estava bloqueado no ambiente do Claude. A versão oficial funciona igual.

**Motivo:** a interface do DAO deixa testar as regras sem banco, e o rollback mantém o banco limpo. O que se perde: mais um `.jar` no repositório. Um teste análogo ficou para o Caio escrever (regra 6 do contrato): "Erro na etapa 2 volta para a etapa 0".

**Quem decidiu:** Caio.

---

## [2026-11-16] Validação com colegas de turma

**Contexto:** a documentação promete validar o sistema com colegas (objetivo 5), e isso conta na parte extensionista.

**Alternativas:** tirar a promessa; ou aplicar um roteiro curto com 2 ou 3 colegas antes do congelamento do documento (23/11).

**Escolha:** aplicar o roteiro de `docs/VALIDACAO.md` (uma tarefa por história de usuário, mais 3 perguntas), registrando os participantes sem nome completo.

**Motivo:** é rápido e mostra o sistema sendo usado pelo público-alvo. O que se perde: o resultado depende de os colegas terem tempo durante a semana.

**Quem decidiu:** Caio.

---

## [2026-11-16] Diagramas e matriz gerados em lote (exceção ao contrato)

**Contexto:** o professor congela o documento na segunda, 23/11, e o Cronograma pede mapa de navegação, diagramas de classes e de sequência, e matriz de rastreabilidade.

**Alternativas:** um documento por vez, com explicação e confirmação (regra 2); ou todos de uma vez.

**Escolha:** o Claude gerou de uma vez o mapa de navegação, os diagramas de classes, de sequência (revisão do dia) e de implantação, a matriz de rastreabilidade e as seções 6 a 10 da documentação, além de corrigir o critério da US-03 para bater com o sistema.

**Motivo:** prazo. O que se perde: o Caio revisa e estuda depois. **Antes da apresentação**, conferir cada diagrama contra o sistema rodando.

**Quem decidiu:** Caio.

---

## [2026-11-19] Demonstração pelo GitHub Codespaces, com scripts de preparo do banco

**Contexto:** a apresentação deve ser no notebook do professor, sem garantia de que ele tenha Java, PostgreSQL ou a senha do banco. Se o sistema não abrir com o banco, a promessa do RNF-03 fica sem demonstração.

**Alternativas:** instalar tudo no notebook na hora; levar um notebook próprio; demonstrar só em memória; ou rodar num ambiente na nuvem que abre pelo navegador.

**Escolha:** GitHub Codespaces. O `.devcontainer/devcontainer.json` cria um Ubuntu 24.04 com Java 21 e roda o `scripts/preparar-banco.sh`, que instala e liga o PostgreSQL, cria o usuário `cadencia` com senha sorteada em `~/.cadencia-env` (fora do repositório, mantendo o RNF-07), cria o banco e as tabelas e compila. O `scripts/rodar.sh` liga o banco se precisar e abre o sistema. Com `--zerar`, o banco é recriado antes da apresentação. Os scripts foram testados no ambiente do Claude (Ubuntu 24.04, PostgreSQL 16); o Codespace em si precisa ser testado pelo Caio num ensaio.

**Motivo:** o ambiente é sempre o mesmo, no ensaio e na apresentação. O que se perde: depende de internet na sala e de entrar na conta do GitHub no notebook. Plano B: modo memória com `--exemplo`. Plano C: vídeo do ensaio com o banco.

**Quem decidiu:** Caio.

---

## [2026-11-23] Interface do terminal redesenhada só na camada view (exceção ao contrato)

**Contexto:** ao rodar com o banco no PC pessoal, a listagem de disciplinas sumia: o menu era impresso logo embaixo e empurrava o resultado para cima. Além disso, o professor avalia a apresentação como a venda de um produto, e as telas eram texto corrido, sem hierarquia, com a opção mais usada (Revisar hoje) escondida no número 5.

**Alternativas:** (a) só colocar uma pausa depois de cada opção; (b) redesenhar todas as telas mexendo só na view; (c) trocar a interface de terminal por outra tecnologia (fora do escopo).

**Escolha:** (b). Cada opção virou uma tela: limpa o terminal, mostra um cabeçalho com o status do banco e espera o Enter para voltar. Também entraram:
- tela de abertura com logotipo;
- painel do dia no menu;
- tabelas com colunas novas: flashcards e "para hoje" por disciplina, a etapa em barra e a próxima revisão em palavras;
- revisão com um cartão por tela, barra de progresso, a regra explicada a cada resposta e resumo com a % de acerto;
- histórico com a pergunta, a resposta e o desempenho;
- cadastro em sequência ("Cadastrar flashcards nela agora?" e "Cadastrar outro?");
- 0 para voltar nas escolhas;
- erros de conexão explicados em português.

A aparência ficou numa classe nova, `Tela`.

**Motivo:** resolve o problema visto e deixa o sistema apresentável sem mudar nenhuma regra, o banco ou os testes. Os 20 testes passaram sem nenhuma alteração, o que também prova a separação em camadas. O que se perde: o código da view foi gerado pelo Claude sem a revisão do Caio, por causa do prazo (regra 10). **Antes da apresentação**, ler o `docs/GUIA-INTERFACE.md` e rodar o roteiro da demonstração uma vez.

**Quem decidiu:** Caio (abriu a exceção e escolheu o menu e as cores, nas entradas abaixo).

---

## [2026-11-23] Menu agrupado, com "Revisar hoje" como opção 1

**Contexto:** o menu antigo seguia a ordem do cadastro (1 Cadastrar disciplina ... 5 Revisar hoje), mas o que o aluno faz todo dia é revisar.

**Alternativas:** manter a numeração e mudar só o visual; ou reorganizar por uso e por assunto.

**Escolha:** o menu foi reorganizado em três grupos, mostrados em colunas, mais o 0 Sair:
- ESTUDAR: 1 Revisar hoje e 2 Ver histórico;
- DISCIPLINAS: 3 Cadastrar, 4 Listar e 5 Remover;
- FLASHCARDS: 6 Cadastrar, 7 Listar e 8 Remover.

O enum `OpcaoMenuPrincipal` ganhou o grupo (outro enum, `Grupo`), o rótulo curto e a descrição.

**Motivo:** o que se faz todo dia vem primeiro, e o agrupamento por assunto deixa o menu mais rápido de ler. O que se perde: a numeração antiga. Por isso foram atualizados os casos de uso, o mapa de navegação, o roteiro da demonstração e os slides.

**Quem decidiu:** Caio.

---

## [2026-11-23] Cores com significado, desligáveis com --sem-cor, e UTF-8 no Linux

**Contexto:** cores ajudam a ler rápido, mas dependem de o terminal entender os códigos ANSI. Num teste em Linux sem idioma configurado, os acentos e o logotipo apareceram como "?".

**Alternativas:** só texto; cores sempre; ou cores com uma opção para desligar.

**Escolha:**
- Cada cor tem um significado fixo: verde para sucesso, vermelho para erro, amarelo para atenção e cinza para dica.
- Os prefixos [OK], [ERRO] e [AVISO] continuam.
- Sem terminal (saída redirecionada) ou com `--sem-cor`, o sistema não usa cores nem limpa a tela.
- No Windows, as cores só ligam nos terminais que com certeza entendem ANSI: Windows Terminal, VS Code e IntelliJ. O `Main` descobre isso pelas variáveis de ambiente que esses terminais criam (`WT_SESSION`, `TERM_PROGRAM` e `TERMINAL_EMULATOR`). No console antigo, o sistema entra sozinho no modo sem cores.
- No `scripts/rodar.sh` (Linux e Codespaces), o Java roda com `-Dstdout.encoding=UTF-8`.

**Motivo:** o prefixo garante que a mensagem não dependa só da cor, o que também é uma questão de acessibilidade. Com o UTF-8, tela e teclado se entendem em qualquer Linux. O que se perde: num terminal que entende ANSI mas não está na lista, as cores ficam desligadas; e num terminal estranho que diga entender e não entenda, é preciso usar o `--sem-cor`.

**Quem decidiu:** Caio.
