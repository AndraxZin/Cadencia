package br.com.cadencia.view;

import br.com.cadencia.dao.AcessoDadosException;
import br.com.cadencia.model.Disciplina;
import br.com.cadencia.model.Flashcard;
import br.com.cadencia.model.Revisao;
import br.com.cadencia.service.DisciplinaService;
import br.com.cadencia.service.FlashcardService;

import java.io.Console;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

/**
 * A "view": tudo que aparece na tela e tudo que é lido do teclado.
 *
 * Esta classe NÃO guarda dados nem decide regras. Ela pergunta ao usuário,
 * chama os services e mostra o resultado (ou a mensagem de erro).
 * A aparência (cores, cabeçalho, tabelas) fica na classe Tela.
 *
 * Cada opção do menu é uma "tela": o terminal é limpo, aparece o título,
 * a opção faz o seu trabalho e espera o Enter antes de voltar ao menu.
 */
public class MenuConsole {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // "EEEE" = dia da semana por extenso, em português: "segunda-feira, 23/11/2026".
    private static final DateTimeFormatter FORMATO_DIA_DA_SEMANA =
            DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));

    private static final int LARGURA_COLUNA_MENU = 24;

    private final DisciplinaService disciplinaService;
    private final FlashcardService flashcardService;
    private final Tela tela;
    private final boolean usandoBanco;
    private final Scanner scanner;

    // Fica true quando o aluno digita 0 numa escolha: aí volta direto ao menu, sem pausa.
    private boolean voltouAoMenu;

    // O título da tela aberta agora (para limpar e reabrir a mesma tela depois de uma escolha).
    private String tituloAtual = "";

    public MenuConsole(DisciplinaService disciplinaService, FlashcardService flashcardService,
                       Tela tela, boolean usandoBanco) {
        this.disciplinaService = disciplinaService;
        this.flashcardService = flashcardService;
        this.tela = tela;
        this.usandoBanco = usandoBanco;
        this.scanner = new Scanner(System.in, charsetDoTeclado());
    }

    /**
     * No Windows o terminal usa uma codificação própria (ex.: cp850). Se o Scanner
     * usasse outra, os acentos digitados (ç, ã) chegariam errados. Com terminal
     * interativo perguntamos a codificação a ele; senão usamos o padrão do Java.
     */
    private static Charset charsetDoTeclado() {
        Console console = System.console();
        return (console != null) ? console.charset() : Charset.defaultCharset();
    }

    /**
     * Mostra a abertura e depois repete: menu, opção escolhida, volta ao menu.
     *
     * @param avisosDeAbertura o que o Main tem a dizer (ex.: "[BANCO] Conectado...")
     */
    public void iniciar(List<String> avisosDeAbertura) {
        try {
            mostrarAbertura(avisosDeAbertura);

            String erroDoMenu = null; // mensagem de opção inválida, mostrada sob o menu
            while (true) {
                exibirMenu(erroDoMenu);
                erroDoMenu = null;

                int numero = lerInteiro("Escolha uma opção: ");
                Optional<OpcaoMenuPrincipal> opcao = OpcaoMenuPrincipal.deCodigo(numero);
                if (opcao.isEmpty()) {
                    erroDoMenu = "Opção inválida. Digite um dos números do menu.";
                    continue;
                }
                if (opcao.get() == OpcaoMenuPrincipal.SAIR) {
                    break;
                }
                executar(opcao.get());
            }

            tela.limpar();
            tela.escrever(tela.marca("Até a próxima revisão!"));
            tela.pularLinha();
        } catch (NoSuchElementException e) {
            // Acontece se a entrada for fechada (Ctrl+Z / Ctrl+D): encerra sem mostrar erro feio.
            tela.pularLinha();
            tela.escrever("Entrada encerrada. Até logo!");
        }
    }

    /** Abre a tela da opção, faz o trabalho dela e espera o Enter para voltar ao menu. */
    private void executar(OpcaoMenuPrincipal opcao) {
        abrirTela(opcao.getDescricao());
        voltouAoMenu = false;
        try {
            switch (opcao) {
                case REVISAR_HOJE -> revisarHoje();
                case VER_HISTORICO -> verHistorico();
                case CADASTRAR_DISCIPLINA -> cadastrarDisciplina();
                case LISTAR_DISCIPLINAS -> listarDisciplinas();
                case REMOVER_DISCIPLINA -> removerDisciplina();
                case CADASTRAR_FLASHCARD -> cadastrarFlashcard();
                case LISTAR_FLASHCARDS -> listarFlashcards();
                case REMOVER_FLASHCARD -> removerFlashcard();
                case SAIR -> { } // tratado no laço de iniciar()
            }
        } catch (AcessoDadosException e) {
            // Problema no banco (ex.: o PostgreSQL caiu): avisa e volta ao menu (RNF-04).
            tela.erro(e.getMessage());
        }
        if (!voltouAoMenu) {
            aguardarEnter("Pressione Enter para voltar ao menu...");
        }
    }

    // ------------------------------------------------------- abertura e menu

    private void mostrarAbertura(List<String> avisos) {
        tela.limpar();
        tela.pularLinha();
        tela.logotipo();
        tela.pularLinha();
        tela.escrever(tela.destaque("Revise o que importa, no dia certo."));
        tela.escrever(tela.discreto("Flashcards com revisão espaçada: 1, 3, 7 e 15 dias."));
        tela.pularLinha();
        for (String aviso : avisos) {
            tela.mensagem(aviso);
        }
        aguardarEnter("Pressione Enter para começar...");
    }

    /** O menu principal: o painel do dia em cima e as opções em três colunas. */
    private void exibirMenu(String erro) {
        abrirTela(LocalDate.now().format(FORMATO_DIA_DA_SEMANA));
        int paraHoje = mostrarPainelDoDia();
        tela.pularLinha();
        mostrarOpcoes(paraHoje);
        tela.pularLinha();
        if (erro != null) {
            tela.erro(erro);
        }
    }

    /**
     * O resumo que abre o menu: o que fazer hoje e quanto já foi cadastrado.
     *
     * @return quantos flashcards vencem hoje (para mostrar ao lado de "Revisar hoje")
     */
    private int mostrarPainelDoDia() {
        try {
            List<Disciplina> disciplinas = disciplinaService.listar();
            List<Flashcard> todos = todosOsFlashcards();
            int paraHoje = flashcardService.listarParaRevisarHoje().size();

            if (disciplinas.isEmpty()) {
                tela.escrever(tela.destaque("Bem-vindo! Comece cadastrando uma disciplina (opção 3)."));
            } else if (todos.isEmpty()) {
                tela.escrever(tela.destaque("Agora cadastre os flashcards da disciplina (opção 6)."));
            } else if (paraHoje > 0) {
                tela.escrever(tela.alerta(quantos(paraHoje, "flashcard", "flashcards") + " para revisar hoje.")
                        + tela.discreto("  Comece pela opção 1."));
            } else {
                tela.escrever(tela.sucesso("Tudo em dia: nenhum flashcard para revisar hoje."));
                mostrarProximaRevisao(todos);
            }
            tela.escrever(tela.discreto(quantos(disciplinas.size(), "disciplina", "disciplinas")
                    + " · " + quantos(todos.size(), "flashcard", "flashcards")));
            return paraHoje;
        } catch (AcessoDadosException e) {
            tela.erro(e.getMessage());
            return 0;
        }
    }

    /** As opções em colunas (Estudar, Disciplinas, Flashcards) e o "0 Sair" embaixo. */
    private void mostrarOpcoes(int paraHoje) {
        OpcaoMenuPrincipal.Grupo[] grupos = {
            OpcaoMenuPrincipal.Grupo.ESTUDAR,
            OpcaoMenuPrincipal.Grupo.DISCIPLINAS,
            OpcaoMenuPrincipal.Grupo.FLASHCARDS,
        };

        // Primeiro monta as colunas; depois imprime linha por linha, uma célula de cada coluna.
        List<List<OpcaoMenuPrincipal>> colunas = new ArrayList<>();
        int linhas = 0;
        StringBuilder titulos = new StringBuilder();
        for (OpcaoMenuPrincipal.Grupo grupo : grupos) {
            List<OpcaoMenuPrincipal> coluna = OpcaoMenuPrincipal.doGrupo(grupo);
            colunas.add(coluna);
            linhas = Math.max(linhas, coluna.size());
            titulos.append(Tela.completar(tela.discreto(grupo.name()), LARGURA_COLUNA_MENU));
        }
        tela.escrever(titulos.toString().stripTrailing());

        for (int i = 0; i < linhas; i++) {
            StringBuilder linha = new StringBuilder();
            for (List<OpcaoMenuPrincipal> coluna : colunas) {
                String celula = (i < coluna.size()) ? celula(coluna.get(i), paraHoje) : "";
                linha.append(Tela.completar(celula, LARGURA_COLUNA_MENU));
            }
            tela.escrever(linha.toString().stripTrailing());
        }
        tela.pularLinha();
        tela.escrever(celula(OpcaoMenuPrincipal.SAIR, 0));
    }

    /** Uma opção do menu: "1  Revisar hoje (3)". */
    private String celula(OpcaoMenuPrincipal opcao, int paraHoje) {
        String texto = tela.destaque(String.valueOf(opcao.getCodigo())) + "  " + opcao.getRotulo();
        if (opcao == OpcaoMenuPrincipal.REVISAR_HOJE && paraHoje > 0) {
            texto += " " + tela.alerta("(" + paraHoje + ")");
        }
        return texto;
    }

    // ---------------------------------------------------------------- ações

    /** RF-04 e RF-05: um flashcard por tela; o aluno diz se acertou e o sistema reagenda. */
    private void revisarHoje() {
        List<Flashcard> paraRevisar = flashcardService.listarParaRevisarHoje();
        if (paraRevisar.isEmpty()) {
            tela.escrever(tela.sucesso("Nenhum flashcard para revisar hoje."));
            mostrarProximaRevisao(todosOsFlashcards());
            return;
        }

        int total = paraRevisar.size();
        int acertos = 0;
        for (int i = 0; i < total; i++) {
            Flashcard flashcard = paraRevisar.get(i);
            if (i > 0) {
                abrirTela("Revisar hoje"); // a primeira tela já foi aberta pelo executar()
            }
            tela.escrever(tela.destaque("Flashcard " + (i + 1) + " de " + total)
                    + "   " + tela.barra(i + 1, total, 20));
            tela.escrever(tela.discreto(flashcard.getDisciplina().getNome()
                    + " · etapa " + flashcard.getEtapa()));
            tela.pularLinha();

            tela.escrever(tela.destaque("PERGUNTA"));
            tela.citacao(flashcard.getFrente());
            aguardarEnter("Pense na resposta e pressione Enter para conferir...");

            tela.pularLinha();
            tela.escrever(tela.destaque("RESPOSTA"));
            tela.citacao(flashcard.getVerso());
            tela.pularLinha();

            int etapaAnterior = flashcard.getEtapa();
            boolean acertou = lerSimNao("Você acertou? (S/N): ");
            LocalDate proxima = flashcardService.registrarResposta(flashcard, acertou);
            if (acertou) {
                acertos++;
            }

            tela.pularLinha();
            String resultado = explicarResultado(acertou, etapaAnterior, flashcard.getEtapa());
            tela.escrever(acertou ? tela.sucesso(resultado) : tela.alerta(resultado));
            tela.escrever("Próxima revisão: " + quando(proxima) + ".");

            aguardarEnter(i < total - 1
                    ? "Pressione Enter para o próximo flashcard..."
                    : "Pressione Enter para ver o resumo...");
        }

        abrirTela("Revisar hoje");
        int erros = total - acertos;
        int percentual = Math.round(acertos * 100f / total);
        tela.escrever(tela.destaque("Revisão concluída: " + quantos(acertos, "acerto", "acertos")
                + " e " + quantos(erros, "erro", "erros") + "."));
        tela.escrever(tela.barra(acertos, total, 20) + "  " + percentual + "% de acerto");
        tela.pularLinha();
        mostrarProximaRevisao(todosOsFlashcards());
    }

    /** Explica, em uma frase, o que a regra das etapas fez com o flashcard. */
    private static String explicarResultado(boolean acertou, int etapaAnterior, int etapaNova) {
        if (acertou && etapaAnterior == etapaNova) {
            return "Acertou! Continua na etapa " + etapaNova + ", a última.";
        }
        if (acertou) {
            return "Acertou! Sobe da etapa " + etapaAnterior + " para a " + etapaNova + ".";
        }
        if (etapaAnterior == 0) {
            return "Errou. Continua na etapa 0.";
        }
        return "Errou. Volta da etapa " + etapaAnterior + " para a 0.";
    }

    /** RF-06: os dados do flashcard e o histórico, do mais recente para o mais antigo. */
    private void verHistorico() {
        Optional<Flashcard> escolhido = escolherFlashcard();
        if (escolhido.isEmpty()) {
            return;
        }
        Flashcard flashcard = escolhido.get();

        abrirTela("Ver histórico de um flashcard");
        tela.escrever(tela.discreto(flashcard.getDisciplina().getNome() + " · etapa ")
                + etapa(flashcard) + tela.discreto(" · próxima revisão: ")
                + quando(flashcard.getProximaRevisao()));
        tela.pularLinha();
        tela.escrever(tela.destaque("PERGUNTA"));
        tela.citacao(flashcard.getFrente());
        tela.pularLinha();
        tela.escrever(tela.destaque("RESPOSTA"));
        tela.citacao(flashcard.getVerso());
        tela.pularLinha();

        List<Revisao> historico = flashcardService.listarHistorico(flashcard);
        if (historico.isEmpty()) {
            tela.escrever("Este flashcard ainda não foi revisado.");
            return;
        }

        int acertos = 0;
        List<String[]> linhas = new ArrayList<>();
        for (Revisao revisao : historico) {
            if (revisao.isAcertou()) {
                acertos++;
            }
            linhas.add(new String[] {
                revisao.getRevisadoEm().format(FORMATO_DATA_HORA),
                revisao.isAcertou() ? tela.sucesso("Acertou") : tela.alerta("Errou"),
            });
        }
        int percentual = Math.round(acertos * 100f / historico.size());
        tela.escrever(tela.destaque("HISTÓRICO") + tela.discreto("  "
                + quantos(historico.size(), "revisão", "revisões") + " · "
                + quantos(acertos, "acerto", "acertos") + " · " + percentual + "% de acerto"));
        tela.tabela(new String[] {"DATA E HORA", "RESULTADO"}, new boolean[] {false, false}, linhas);
    }

    /** RF-01: cadastra a disciplina e oferece cadastrar os flashcards dela em seguida. */
    private void cadastrarDisciplina() {
        String nome = lerTextoObrigatorio("Nome da disciplina: ");
        String professor = lerTextoOpcional("Professor (opcional, Enter para pular): ");

        Disciplina nova;
        try {
            nova = disciplinaService.cadastrar(nome, professor);
        } catch (IllegalArgumentException e) {
            tela.erro(e.getMessage());
            return;
        }
        tela.ok("Disciplina cadastrada com o código " + nova.getCodigo() + ".");
        tela.pularLinha();

        if (lerSimNao("Cadastrar flashcards nela agora? (S/N): ")) {
            abrirTela("Cadastrar flashcard");
            cadastrarFlashcardsEm(nova);
        }
    }

    /** RF-03: as disciplinas, com quantos flashcards cada uma tem e quantos vencem hoje. */
    private void listarDisciplinas() {
        List<Disciplina> lista = disciplinaService.listar();
        if (lista.isEmpty()) {
            tela.escrever("Nenhuma disciplina cadastrada ainda. Use a opção 3 para cadastrar a primeira.");
            return;
        }
        mostrarDisciplinas(lista);
    }

    /** RF-07: remove a disciplina (e os flashcards dela) depois de confirmar. */
    private void removerDisciplina() {
        Optional<Disciplina> disciplina = escolherDisciplina();
        if (disciplina.isEmpty()) {
            return;
        }

        int quantidade = flashcardService.listarPorDisciplina(disciplina.get()).size();
        tela.pularLinha();
        tela.aviso("Isso apaga a disciplina \"" + disciplina.get().getNome() + "\", "
                + quantos(quantidade, "flashcard", "flashcards") + " e o histórico deles.");
        if (lerSimNao("Tem certeza? (S/N): ")) {
            disciplinaService.remover(disciplina.get());
            tela.ok("Disciplina removida.");
        } else {
            tela.escrever("Remoção cancelada.");
        }
    }

    /** RF-02: escolhe a disciplina e cadastra flashcards nela. */
    private void cadastrarFlashcard() {
        Optional<Disciplina> disciplina = escolherDisciplina();
        if (disciplina.isEmpty()) {
            return;
        }
        tela.pularLinha();
        cadastrarFlashcardsEm(disciplina.get());
    }

    /** Cadastra um flashcard na disciplina e pergunta se quer outro, até a resposta ser N. */
    private void cadastrarFlashcardsEm(Disciplina disciplina) {
        boolean outro = true;
        while (outro) {
            tela.escrever(tela.discreto("Novo flashcard em " + disciplina.getNome()));
            String frente = lerTextoObrigatorio("Frente (pergunta): ");
            String verso = lerTextoObrigatorio("Verso (resposta): ");
            try {
                Flashcard novo = flashcardService.cadastrar(disciplina, frente, verso);
                tela.ok("Flashcard cadastrado. Primeira revisão em "
                        + novo.getProximaRevisao().format(FORMATO_DATA) + " (amanhã).");
            } catch (IllegalArgumentException e) {
                tela.erro(e.getMessage());
            }
            tela.pularLinha();
            outro = lerSimNao("Cadastrar outro nesta disciplina? (S/N): ");
            if (outro) {
                tela.pularLinha();
            }
        }
    }

    /** RF-03: os flashcards de uma disciplina, com a etapa e a próxima revisão. */
    private void listarFlashcards() {
        Optional<Disciplina> disciplina = escolherDisciplina();
        if (disciplina.isEmpty()) {
            return;
        }

        List<Flashcard> lista = flashcardService.listarPorDisciplina(disciplina.get());
        abrirTela(tituloAtual); // limpa a lista de disciplinas: agora só interessa a escolhida
        tela.escrever(tela.destaque(disciplina.get().getNome()));
        tela.pularLinha();
        if (lista.isEmpty()) {
            tela.escrever("Esta disciplina ainda não tem flashcards.");
            return;
        }
        mostrarFlashcards(lista);
    }

    /** RF-07: remove um flashcard (e o histórico dele) depois de confirmar. */
    private void removerFlashcard() {
        Optional<Flashcard> flashcard = escolherFlashcard();
        if (flashcard.isEmpty()) {
            return;
        }

        tela.pularLinha();
        tela.aviso("Isso apaga o flashcard \"" + flashcard.get().getFrente() + "\" e o histórico dele.");
        if (lerSimNao("Tem certeza? (S/N): ")) {
            flashcardService.remover(flashcard.get());
            tela.ok("Flashcard removido.");
        } else {
            tela.escrever("Remoção cancelada.");
        }
    }

    // ------------------------------------------------------- apoio à tela

    /** Limpa o terminal e mostra o cabeçalho, com o status do banco à direita. */
    private void abrirTela(String titulo) {
        tituloAtual = titulo;
        tela.limpar();
        tela.cabecalho(titulo, usandoBanco ? "PostgreSQL" : "memória", usandoBanco);
    }

    /**
     * Mostra as disciplinas e pede o código. Vazio se não houver disciplina,
     * se o código não existir ou se o aluno digitar 0 (voltar).
     */
    private Optional<Disciplina> escolherDisciplina() {
        List<Disciplina> lista = disciplinaService.listar();
        if (lista.isEmpty()) {
            tela.escrever("Cadastre uma disciplina primeiro (opção 3).");
            return Optional.empty();
        }

        mostrarDisciplinas(lista);
        tela.pularLinha();
        int codigo = lerCodigo("Código da disciplina (0 para voltar): ");
        if (codigo == 0) {
            voltouAoMenu = true;
            return Optional.empty();
        }
        Optional<Disciplina> escolhida = disciplinaService.buscarPorCodigo(codigo);
        if (escolhida.isEmpty()) {
            tela.erro("Disciplina não encontrada.");
        }
        return escolhida;
    }

    /** Pede a disciplina, mostra os flashcards dela e pede o código do flashcard. */
    private Optional<Flashcard> escolherFlashcard() {
        Optional<Disciplina> disciplina = escolherDisciplina();
        if (disciplina.isEmpty()) {
            return Optional.empty();
        }

        List<Flashcard> lista = flashcardService.listarPorDisciplina(disciplina.get());
        abrirTela(tituloAtual); // segundo passo da escolha, numa tela limpa
        tela.escrever(tela.destaque(disciplina.get().getNome()));
        tela.pularLinha();
        if (lista.isEmpty()) {
            tela.escrever("Esta disciplina ainda não tem flashcards.");
            return Optional.empty();
        }

        mostrarFlashcards(lista);
        tela.pularLinha();
        int codigo = lerCodigo("Código do flashcard (0 para voltar): ");
        if (codigo == 0) {
            voltouAoMenu = true;
            return Optional.empty();
        }
        // Só aceita um código da lista mostrada (da disciplina escolhida).
        Optional<Flashcard> escolhido = lista.stream()
                .filter(f -> f.getCodigo() == codigo)
                .findFirst();
        if (escolhido.isEmpty()) {
            tela.erro("Flashcard não encontrado nesta disciplina.");
        }
        return escolhido;
    }

    private void mostrarDisciplinas(List<Disciplina> lista) {
        // Uma consulta só para os de hoje; depois conta quantos são de cada disciplina.
        List<Flashcard> paraHoje = flashcardService.listarParaRevisarHoje();

        List<String[]> linhas = new ArrayList<>();
        for (Disciplina d : lista) {
            int total = flashcardService.listarPorDisciplina(d).size();
            int hoje = 0;
            for (Flashcard f : paraHoje) {
                if (f.getDisciplina().getCodigo() == d.getCodigo()) {
                    hoje++;
                }
            }
            String professor = d.getProfessor().isEmpty() ? "-" : Tela.ajustar(d.getProfessor(), 24);
            linhas.add(new String[] {
                String.valueOf(d.getCodigo()),
                Tela.ajustar(d.getNome(), 36),
                professor,
                String.valueOf(total),
                (hoje > 0) ? tela.alerta(String.valueOf(hoje)) : tela.discreto("0"),
            });
        }
        tela.tabela(new String[] {"CÓD.", "DISCIPLINA", "PROFESSOR", "FLASHCARDS", "PARA HOJE"},
                new boolean[] {true, false, false, true, true}, linhas);
    }

    private void mostrarFlashcards(List<Flashcard> lista) {
        List<String[]> linhas = new ArrayList<>();
        for (Flashcard f : lista) {
            linhas.add(new String[] {
                String.valueOf(f.getCodigo()),
                etapa(f),
                quando(f.getProximaRevisao()),
                Tela.ajustar(f.getFrente(), 40),
            });
        }
        tela.tabela(new String[] {"CÓD.", "ETAPA", "PRÓXIMA REVISÃO", "FRENTE"},
                new boolean[] {true, false, false, false}, linhas);
    }

    /** A etapa como barra e número: etapa 2 = "██░ 2". */
    private String etapa(Flashcard f) {
        return tela.barra(f.getEtapa(), FlashcardService.ULTIMA_ETAPA, FlashcardService.ULTIMA_ETAPA)
                + " " + f.getEtapa();
    }

    /** A data em palavras e em número: "hoje", "amanhã, 24/11/2026", "em 7 dias, 30/11/2026". */
    private String quando(LocalDate data) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), data);
        if (dias < 0) {
            return tela.alerta("atrasado, desde " + data.format(FORMATO_DATA));
        }
        if (dias == 0) {
            return tela.alerta("hoje");
        }
        if (dias == 1) {
            return "amanhã, " + data.format(FORMATO_DATA);
        }
        return "em " + dias + " dias, " + data.format(FORMATO_DATA);
    }

    /** "Próxima revisão: amanhã, 24/11/2026 (2 flashcards)." (nada, se não houver data futura). */
    private void mostrarProximaRevisao(List<Flashcard> flashcards) {
        LocalDate hoje = LocalDate.now();
        LocalDate maisCedo = null;
        int quantidade = 0;
        for (Flashcard f : flashcards) {
            LocalDate data = f.getProximaRevisao();
            if (!data.isAfter(hoje)) {
                continue; // os de hoje já estão no "Revisar hoje"
            }
            if (maisCedo == null || data.isBefore(maisCedo)) {
                maisCedo = data;
                quantidade = 1;
            } else if (data.equals(maisCedo)) {
                quantidade++;
            }
        }
        if (maisCedo != null) {
            tela.escrever("Próxima revisão: " + quando(maisCedo) + " ("
                    + quantos(quantidade, "flashcard", "flashcards") + ").");
        }
    }

    /** Todos os flashcards, de todas as disciplinas (para o painel e a próxima revisão). */
    private List<Flashcard> todosOsFlashcards() {
        List<Flashcard> todos = new ArrayList<>();
        for (Disciplina d : disciplinaService.listar()) {
            todos.addAll(flashcardService.listarPorDisciplina(d));
        }
        return todos;
    }

    /** "1 flashcard", "3 flashcards": escolhe o singular ou o plural. */
    private static String quantos(int quantidade, String singular, String plural) {
        return quantidade + " " + (quantidade == 1 ? singular : plural);
    }

    // ------------------------------------------------- leitura do teclado

    private void aguardarEnter(String texto) {
        tela.pularLinha();
        tela.perguntar(tela.discreto(texto));
        scanner.nextLine();
    }

    /**
     * Lê uma linha e converte em número. Devolve -1 se não for um número válido
     * (-1 nunca é um código real, pois os códigos começam em 1).
     *
     * Usamos nextLine() + parseInt() em vez de nextInt(): o nextInt() deixa o
     * "Enter" no buffer e atrapalha a leitura seguinte.
     */
    private int lerInteiro(String pergunta) {
        tela.perguntar(pergunta);
        String linha = scanner.nextLine().trim();
        try {
            return Integer.parseInt(linha);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Como lerInteiro, mas Enter sem nada também vale 0 (voltar). */
    private int lerCodigo(String pergunta) {
        tela.perguntar(pergunta);
        String linha = scanner.nextLine().trim();
        if (linha.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(linha);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Repete a pergunta até o usuário digitar alguma coisa. */
    private String lerTextoObrigatorio(String pergunta) {
        while (true) {
            tela.perguntar(pergunta);
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            tela.erro("Este campo é obrigatório. Tente de novo.");
        }
    }

    private String lerTextoOpcional(String pergunta) {
        tela.perguntar(pergunta);
        return scanner.nextLine().trim();
    }

    /** Repete a pergunta até a resposta ser S ou N (maiúscula ou minúscula). */
    private boolean lerSimNao(String pergunta) {
        while (true) {
            tela.perguntar(pergunta);
            String resposta = scanner.nextLine().trim().toUpperCase();
            if (resposta.equals("S")) {
                return true;
            }
            if (resposta.equals("N")) {
                return false;
            }
            tela.erro("Responda S (sim) ou N (não).");
        }
    }
}
