package br.com.cadencia.view;

import br.com.cadencia.model.Disciplina;
import br.com.cadencia.model.Flashcard;
import br.com.cadencia.service.DisciplinaService;
import br.com.cadencia.service.FlashcardService;

import java.io.Console;
import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

/**
 * A "view": tudo que aparece na tela e tudo que é lido do teclado.
 *
 * Esta classe NÃO guarda dados nem decide regras. Ela pergunta ao usuário,
 * chama os services e mostra o resultado (ou a mensagem de erro).
 */
public class MenuConsole {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DisciplinaService disciplinaService;
    private final FlashcardService flashcardService;
    private final Scanner scanner;

    public MenuConsole(DisciplinaService disciplinaService, FlashcardService flashcardService) {
        this.disciplinaService = disciplinaService;
        this.flashcardService = flashcardService;
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

    /** Laço principal: mostra o menu, lê a opção e executa, até o usuário escolher Sair. */
    public void iniciar() {
        System.out.println("=== CADÊNCIA - revisão espaçada de flashcards ===");
        try {
            boolean executando = true;
            while (executando) {
                exibirMenu();
                int numero = lerInteiro("Escolha uma opção: ");
                Optional<OpcaoMenuPrincipal> opcao = OpcaoMenuPrincipal.deCodigo(numero);

                if (opcao.isEmpty()) {
                    System.out.println("[ERRO] Opção inválida. Digite um dos números do menu.");
                    continue;
                }

                switch (opcao.get()) {
                    case CADASTRAR_DISCIPLINA -> cadastrarDisciplina();
                    case LISTAR_DISCIPLINAS -> listarDisciplinas();
                    case CADASTRAR_FLASHCARD -> cadastrarFlashcard();
                    case LISTAR_FLASHCARDS -> listarFlashcards();
                    case SAIR -> {
                        executando = false;
                        System.out.println("Até a próxima revisão!");
                    }
                }
            }
        } catch (NoSuchElementException e) {
            // Acontece se a entrada for fechada (Ctrl+Z / Ctrl+D): encerra sem mostrar erro feio.
            System.out.println();
            System.out.println("Entrada encerrada. Até logo!");
        }
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("----- MENU PRINCIPAL -----");
        for (OpcaoMenuPrincipal opcao : OpcaoMenuPrincipal.values()) {
            System.out.println(opcao.getCodigo() + " - " + opcao.getDescricao());
        }
    }

    // ---------------------------------------------------------------- ações

    private void cadastrarDisciplina() {
        System.out.println();
        System.out.println("--- Cadastrar disciplina ---");
        String nome = lerTextoObrigatorio("Nome da disciplina: ");
        String professor = lerTextoOpcional("Professor (opcional, Enter para pular): ");

        try {
            Disciplina nova = disciplinaService.cadastrar(nome, professor);
            System.out.println("[OK] Disciplina cadastrada com o código " + nova.getCodigo() + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("[ERRO] " + e.getMessage());
        }
    }

    private void listarDisciplinas() {
        System.out.println();
        System.out.println("--- Disciplinas ---");
        List<Disciplina> lista = disciplinaService.listar();
        if (lista.isEmpty()) {
            System.out.println("Nenhuma disciplina cadastrada ainda.");
            return;
        }
        mostrarDisciplinas(lista);
    }

    private void cadastrarFlashcard() {
        System.out.println();
        System.out.println("--- Cadastrar flashcard ---");
        Optional<Disciplina> disciplina = escolherDisciplina();
        if (disciplina.isEmpty()) {
            return;
        }

        String frente = lerTextoObrigatorio("Frente (pergunta): ");
        String verso = lerTextoObrigatorio("Verso (resposta): ");

        try {
            Flashcard novo = flashcardService.cadastrar(disciplina.get(), frente, verso);
            System.out.println("[OK] Flashcard cadastrado. Primeira revisão em "
                    + novo.getProximaRevisao().format(FORMATO_DATA) + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("[ERRO] " + e.getMessage());
        }
    }

    private void listarFlashcards() {
        System.out.println();
        System.out.println("--- Flashcards de uma disciplina ---");
        Optional<Disciplina> disciplina = escolherDisciplina();
        if (disciplina.isEmpty()) {
            return;
        }

        List<Flashcard> lista = flashcardService.listarPorDisciplina(disciplina.get().getCodigo());
        if (lista.isEmpty()) {
            System.out.println("Esta disciplina ainda não tem flashcards.");
            return;
        }

        System.out.printf("%-6s %-6s %-14s %s%n", "Cód.", "Etapa", "Próx. revisão", "Frente");
        for (Flashcard f : lista) {
            System.out.printf("%-6d %-6d %-14s %s%n",
                    f.getCodigo(), f.getEtapa(), f.getProximaRevisao().format(FORMATO_DATA), f.getFrente());
        }
    }

    // ------------------------------------------------------- apoio à tela

    /** Mostra as disciplinas e pede o código. Vazio se não houver disciplina ou o código não existir. */
    private Optional<Disciplina> escolherDisciplina() {
        List<Disciplina> lista = disciplinaService.listar();
        if (lista.isEmpty()) {
            System.out.println("Cadastre uma disciplina primeiro.");
            return Optional.empty();
        }

        mostrarDisciplinas(lista);
        int codigo = lerInteiro("Código da disciplina: ");
        Optional<Disciplina> escolhida = disciplinaService.buscarPorCodigo(codigo);
        if (escolhida.isEmpty()) {
            System.out.println("[ERRO] Disciplina não encontrada.");
        }
        return escolhida;
    }

    private void mostrarDisciplinas(List<Disciplina> lista) {
        // A coluna "Disciplina" tem a largura do maior nome da lista
        // (no mínimo, a do próprio título), para a coluna "Professor" ficar alinhada.
        int largura = "Disciplina".length();
        for (Disciplina d : lista) {
            largura = Math.max(largura, d.getNome().length());
        }

        // Ex.: com largura 31, o formato fica "%-6s %-31s %s%n".
        // O "-" alinha o texto à esquerda e o número reserva essa quantidade de espaços.
        String formatoCabecalho = "%-6s %-" + largura + "s %s%n";
        String formatoLinha = "%-6d %-" + largura + "s %s%n";

        System.out.printf(formatoCabecalho, "Cód.", "Disciplina", "Professor");
        for (Disciplina d : lista) {
            String professor = d.getProfessor().isEmpty() ? "-" : d.getProfessor();
            System.out.printf(formatoLinha, d.getCodigo(), d.getNome(), professor);
        }
    }

    // ------------------------------------------------- leitura do teclado

    /**
     * Lê uma linha e converte em número. Devolve -1 se não for um número válido
     * (-1 nunca é um código real, pois os códigos começam em 1).
     *
     * Usamos nextLine() + parseInt() em vez de nextInt(): o nextInt() deixa o
     * "Enter" no buffer e atrapalha a leitura seguinte.
     */
    private int lerInteiro(String pergunta) {
        System.out.print(pergunta);
        String linha = scanner.nextLine().trim();
        try {
            return Integer.parseInt(linha);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Repete a pergunta até o usuário digitar alguma coisa. */
    private String lerTextoObrigatorio(String pergunta) {
        while (true) {
            System.out.print(pergunta);
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("[ERRO] Este campo é obrigatório. Tente de novo.");
        }
    }

    private String lerTextoOpcional(String pergunta) {
        System.out.print(pergunta);
        return scanner.nextLine().trim();
    }
}
