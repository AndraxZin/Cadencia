package br.com.cadencia;

import br.com.cadencia.dao.ConexaoBanco;
import br.com.cadencia.dao.DisciplinaDao;
import br.com.cadencia.dao.DisciplinaDaoJdbc;
import br.com.cadencia.dao.DisciplinaDaoMemoria;
import br.com.cadencia.dao.FlashcardDao;
import br.com.cadencia.dao.FlashcardDaoJdbc;
import br.com.cadencia.dao.FlashcardDaoMemoria;
import br.com.cadencia.dao.RevisaoDao;
import br.com.cadencia.dao.RevisaoDaoJdbc;
import br.com.cadencia.dao.RevisaoDaoMemoria;
import br.com.cadencia.service.DisciplinaService;
import br.com.cadencia.service.FlashcardService;
import br.com.cadencia.view.MenuConsole;
import br.com.cadencia.view.Tela;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ponto de entrada do programa. Só monta as peças e inicia o menu.
 *
 * 1. Tenta conectar ao PostgreSQL. Conseguiu: usa os DAOs JDBC (dados gravados no banco).
 *    Não conseguiu: avisa e usa os DAOs em memória (os dados somem ao sair).
 * 2. Entrega os DAOs aos services e os services à view.
 *
 * Os services recebem as INTERFACES (DisciplinaDao...), então funcionam igual
 * nos dois casos.
 *
 * Opções (em qualquer ordem):
 *   --exemplo   carrega dados para demonstração, se o banco estiver vazio
 *   --sem-cor   desliga as cores, para terminais que não as entendem
 */
public class Main {

    public static void main(String[] args) {
        List<String> opcoes = List.of(args);
        List<String> avisos = new ArrayList<>(); // mostrados na tela de abertura
        Connection conexao = null;
        boolean usandoBanco;
        DisciplinaDao disciplinaDao;
        FlashcardDao flashcardDao;
        RevisaoDao revisaoDao;

        try {
            conexao = ConexaoBanco.abrir();
            disciplinaDao = new DisciplinaDaoJdbc(conexao);
            flashcardDao = new FlashcardDaoJdbc(conexao);
            revisaoDao = new RevisaoDaoJdbc(conexao);
            usandoBanco = true;
            avisos.add("[BANCO] Conectado ao PostgreSQL: os dados ficam gravados.");
        } catch (SQLException e) {
            usandoBanco = false;
            avisos.add("[AVISO] Sem PostgreSQL: " + explicar(e));
            avisos.add("[AVISO] Usando memória: os dados somem quando o programa fechar.");
            RevisaoDaoMemoria revisaoMemoria = new RevisaoDaoMemoria();
            FlashcardDaoMemoria flashcardMemoria = new FlashcardDaoMemoria(revisaoMemoria);
            disciplinaDao = new DisciplinaDaoMemoria(flashcardMemoria);
            flashcardDao = flashcardMemoria;
            revisaoDao = revisaoMemoria;
        }

        if (opcoes.contains("--exemplo")) {
            avisos.add(DadosExemplo.carregar(disciplinaDao, flashcardDao, revisaoDao));
        }

        DisciplinaService disciplinaService = new DisciplinaService(disciplinaDao);
        FlashcardService flashcardService = new FlashcardService(flashcardDao, revisaoDao);

        Tela tela = new Tela(terminalComCores(opcoes));

        MenuConsole menu = new MenuConsole(disciplinaService, flashcardService, tela, usandoBanco);
        menu.iniciar(avisos);

        fechar(conexao);
    }

    /**
     * Traduz os erros de conexão mais comuns para uma frase clara, em português.
     * O SQLState é um código padrão de 5 letras que o banco devolve junto com o erro.
     */
    private static String explicar(SQLException e) {
        String mensagem = e.getMessage();
        if (mensagem != null && mensagem.startsWith("No suitable driver")) {
            return "o driver JDBC não está no classpath (rode com lib/postgresql-42.7.4.jar).";
        }
        String estado = (e.getSQLState() == null) ? "" : e.getSQLState();
        return switch (estado) {
            case "08004" -> "a senha do banco não foi informada (defina a variável CADENCIA_DB_PASSWORD).";
            case "28P01" -> "o banco recusou a senha (confira a variável CADENCIA_DB_PASSWORD).";
            case "3D000" -> "o banco \"cadencia\" não existe (crie o banco e rode o sql/schema.sql).";
            case "08001" -> "o servidor PostgreSQL não respondeu na porta 5432 (ele está ligado?).";
            default -> mensagem;
        };
    }

    /**
     * Decide se a tela usa cores. Sem terminal (System.console() == null) ou com
     * --sem-cor: não. No Windows, só liga nos terminais que com certeza entendem os
     * códigos ANSI: o Windows Terminal (WT_SESSION), o do VS Code (TERM_PROGRAM) e o
     * do IntelliJ (TERMINAL_EMULATOR). No console antigo, os códigos viram lixo na tela.
     */
    private static boolean terminalComCores(List<String> opcoes) {
        if (opcoes.contains("--sem-cor") || System.console() == null) {
            return false;
        }
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        return !windows
                || System.getenv("WT_SESSION") != null
                || System.getenv("TERM_PROGRAM") != null
                || System.getenv("TERMINAL_EMULATOR") != null;
    }

    private static void fechar(Connection conexao) {
        if (conexao == null) {
            return;
        }
        try {
            conexao.close();
        } catch (SQLException e) {
            // O programa já está terminando: não há o que fazer além de seguir.
        }
    }
}
