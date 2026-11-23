package br.com.cadencia.view;

import java.util.ArrayList;
import java.util.List;

/**
 * A aparência do terminal: cores, títulos, mensagens e tabelas, num lugar só.
 *
 * Funciona como um "design system" do terminal: o MenuConsole decide O QUE
 * mostrar e esta classe decide COMO aparece. Para mudar uma cor ou o estilo
 * dos títulos, basta mexer aqui, e todas as telas mudam juntas.
 *
 * As cores usam códigos ANSI: sequências de texto especiais que o terminal
 * entende como "mude a cor" (ex.: ESC[32m = verde, ESC[0m = volta ao normal).
 * Funcionam no Windows Terminal, no PowerShell e no GitHub Codespaces. Num
 * terminal antigo, que não entende esses códigos, rode com --sem-cor.
 *
 * Cada cor tem um significado fixo (cor semântica), e as mensagens também têm
 * prefixo ([OK], [ERRO], [AVISO]), para não depender só da cor.
 */
public class Tela {

    /** Largura das réguas e do cabeçalho, em caracteres. */
    public static final int LARGURA = 72;

    /** Recuo à esquerda de todo o conteúdo. */
    private static final String MARGEM = "  ";

    // "\u001B" é o caractere ESC, que abre todo código ANSI.
    private static final String NORMAL = "\u001B[0m";
    private static final String NEGRITO = "\u001B[1m";
    private static final String VERDE = "\u001B[32m";
    private static final String VERDE_CLARO = "\u001B[1;92m";
    private static final String VERMELHO = "\u001B[31m";
    private static final String AMARELO = "\u001B[33m";
    private static final String CINZA = "\u001B[90m";
    private static final String LIMPAR_TELA = "\u001B[H\u001B[2J\u001B[3J";

    // Logotipo em letras de bloco (só usa caracteres que o terminal do Windows mostra).
    private static final String[] LOGOTIPO = {
        "                         ▄▀▀▀▄",
        " ██████╗ █████╗ ██████╗ ███████╗███╗   ██╗ ██████╗██╗ █████╗",
        "██╔════╝██╔══██╗██╔══██╗██╔════╝████╗  ██║██╔════╝██║██╔══██╗",
        "██║     ███████║██║  ██║█████╗  ██╔██╗ ██║██║     ██║███████║",
        "██║     ██╔══██║██║  ██║██╔══╝  ██║╚██╗██║██║     ██║██╔══██║",
        "╚██████╗██║  ██║██████╔╝███████╗██║ ╚████║╚██████╗██║██║  ██║",
        " ╚═════╝╚═╝  ╚═╝╚═════╝ ╚══════╝╚═╝  ╚═══╝ ╚═════╝╚═╝╚═╝  ╚═╝",
    };

    private final boolean comCores;

    /** @param comCores false desliga as cores e a limpeza da tela (opção --sem-cor) */
    public Tela(boolean comCores) {
        this.comCores = comCores;
    }

    // ------------------------------------------------ estilos de texto
    // Cada método devolve o texto "pintado". Sem cores, devolve o texto igual.

    /** Verde: deu certo (ex.: [OK], acertou). */
    public String sucesso(String texto) {
        return pintar(VERDE, texto);
    }

    /** Vermelho: erro. */
    public String perigo(String texto) {
        return pintar(VERMELHO, texto);
    }

    /** Amarelo: atenção (ex.: cartões para hoje, confirmação de remoção). */
    public String alerta(String texto) {
        return pintar(AMARELO, texto);
    }

    /** Negrito: títulos e números importantes. */
    public String destaque(String texto) {
        return pintar(NEGRITO, texto);
    }

    /** Cinza: informação secundária e dicas. */
    public String discreto(String texto) {
        return pintar(CINZA, texto);
    }

    /** Verde-claro em negrito: a cor da marca Cadência. */
    public String marca(String texto) {
        return pintar(VERDE_CLARO, texto);
    }

    private String pintar(String cor, String texto) {
        return comCores ? cor + texto + NORMAL : texto;
    }

    // ------------------------------------------------ estrutura da tela

    /** Apaga o terminal, para cada tela começar limpa. Sem cores, só pula linhas. */
    public void limpar() {
        if (comCores) {
            System.out.print(LIMPAR_TELA);
            System.out.flush();
        } else {
            System.out.println();
            System.out.println();
        }
    }

    /** O nome do sistema em letras grandes (tela de abertura). */
    public void logotipo() {
        for (String linha : LOGOTIPO) {
            escrever(marca(linha));
        }
    }

    /**
     * Primeira linha de toda tela: "CADÊNCIA · título" à esquerda, o status do
     * banco à direita e uma régua embaixo.
     *
     * @param statusOk true pinta o status de verde (banco); false, de amarelo (memória)
     */
    public void cabecalho(String titulo, String status, boolean statusOk) {
        String esquerda = "CADÊNCIA · " + titulo;
        String direita = "■ " + status;
        int espacos = Math.max(2, LARGURA - esquerda.length() - direita.length());

        System.out.println(" " + marca("CADÊNCIA") + discreto(" · ") + destaque(titulo)
                + " ".repeat(espacos) + (statusOk ? sucesso(direita) : alerta(direita)));
        System.out.println(" " + discreto("─".repeat(LARGURA)));
        System.out.println();
    }

    /** Escreve uma linha com o recuo padrão. */
    public void escrever(String texto) {
        System.out.println(MARGEM + texto);
    }

    public void pularLinha() {
        System.out.println();
    }

    /** Mostra uma pergunta e deixa o cursor na mesma linha, esperando a resposta. */
    public void perguntar(String pergunta) {
        System.out.print(MARGEM + pergunta);
    }

    /** Um texto longo (frente ou verso de um flashcard), com uma barra à esquerda e quebra de linha. */
    public void citacao(String texto) {
        for (String linha : quebrar(texto, LARGURA - 6)) {
            escrever(discreto("│ ") + linha);
        }
    }

    // ------------------------------------------------ mensagens

    public void ok(String mensagem) {
        escrever(sucesso("[OK] " + mensagem));
    }

    public void erro(String mensagem) {
        escrever(perigo("[ERRO] " + mensagem));
    }

    public void aviso(String mensagem) {
        escrever(alerta("[AVISO] " + mensagem));
    }

    /** Escreve um aviso já pronto (ex.: "[BANCO] Conectado..."), na cor do prefixo. */
    public void mensagem(String texto) {
        if (texto.startsWith("[BANCO]") || texto.startsWith("[OK]")) {
            escrever(sucesso(texto));
        } else if (texto.startsWith("[AVISO]")) {
            escrever(alerta(texto));
        } else if (texto.startsWith("[ERRO]")) {
            escrever(perigo(texto));
        } else {
            escrever(texto);
        }
    }

    // ------------------------------------------------ tabelas e barras

    /**
     * Imprime uma tabela com as colunas alinhadas: títulos, uma régua e as linhas.
     * A largura de cada coluna é a do maior texto dela.
     *
     * @param direita true nas colunas de número, que ficam alinhadas à direita
     */
    public void tabela(String[] titulos, boolean[] direita, List<String[]> linhas) {
        int[] largura = new int[titulos.length];
        for (int c = 0; c < titulos.length; c++) {
            largura[c] = titulos[c].length();
            for (String[] linha : linhas) {
                largura[c] = Math.max(largura[c], larguraVisivel(linha[c]));
            }
        }

        StringBuilder cabecalho = new StringBuilder();
        StringBuilder regua = new StringBuilder();
        for (int c = 0; c < titulos.length; c++) {
            cabecalho.append(alinhar(titulos[c], largura[c], direita[c])).append("  ");
            regua.append("─".repeat(largura[c])).append("  ");
        }
        escrever(destaque(cabecalho.toString().stripTrailing()));
        escrever(discreto(regua.toString().stripTrailing()));

        for (String[] linha : linhas) {
            StringBuilder texto = new StringBuilder();
            for (int c = 0; c < titulos.length; c++) {
                texto.append(alinhar(linha[c], largura[c], direita[c])).append("  ");
            }
            escrever(texto.toString().stripTrailing());
        }
    }

    /** Barra de progresso: ex. barra(2, 3, 3) = "██░". */
    public String barra(int feitos, int total, int tamanho) {
        int cheios = (total == 0) ? 0 : feitos * tamanho / total;
        return sucesso("█".repeat(cheios)) + discreto("░".repeat(tamanho - cheios));
    }

    // ------------------------------------------------ apoio (texto puro)

    /** Corta um texto longo e termina com "...", para caber na coluna. */
    public static String ajustar(String texto, int tamanhoMaximo) {
        if (texto.length() <= tamanhoMaximo) {
            return texto;
        }
        return texto.substring(0, tamanhoMaximo - 3) + "...";
    }

    /** Completa o texto com espaços até a largura pedida (para montar colunas). */
    public static String completar(String texto, int largura) {
        return alinhar(texto, largura, false);
    }

    /** Quebra um texto em linhas de até "largura" caracteres, sem cortar palavras. */
    public static List<String> quebrar(String texto, int largura) {
        List<String> linhas = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        for (String palavra : texto.split(" ")) {
            if (atual.length() > 0 && atual.length() + 1 + palavra.length() > largura) {
                linhas.add(atual.toString());
                atual = new StringBuilder();
            }
            if (atual.length() > 0) {
                atual.append(' ');
            }
            atual.append(palavra);
        }
        linhas.add(atual.toString());
        return linhas;
    }

    /**
     * Quantos caracteres aparecem de fato na tela. Os códigos de cor ocupam
     * espaço na String mas não aparecem, então são retirados antes de contar.
     * O padrão "\u001B\\[[0-9;]*m" é uma expressão regular (um molde de busca)
     * que casa com qualquer código de cor.
     */
    private static int larguraVisivel(String texto) {
        return texto.replaceAll("\u001B\\[[0-9;]*m", "").length();
    }

    private static String alinhar(String texto, int largura, boolean direita) {
        String espacos = " ".repeat(Math.max(0, largura - larguraVisivel(texto)));
        return direita ? espacos + texto : texto + espacos;
    }
}
