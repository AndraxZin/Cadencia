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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ponto de entrada do programa. Só monta as peças e inicia o menu.
 *
 * 1. Tenta conectar ao PostgreSQL. Conseguiu: usa os DAOs JDBC (dados gravados no banco).
 *    Não conseguiu: avisa e usa os DAOs em memória (os dados somem ao sair).
 * 2. Entrega os DAOs aos services e os services à view.
 *
 * Os services recebem as INTERFACES (DisciplinaDao...), então funcionam igual
 * nos dois casos. Rodando com "--exemplo", carrega dados para demonstração.
 */
public class Main {

    public static void main(String[] args) {
        Connection conexao = null;
        DisciplinaDao disciplinaDao;
        FlashcardDao flashcardDao;
        RevisaoDao revisaoDao;

        try {
            conexao = ConexaoBanco.abrir();
            disciplinaDao = new DisciplinaDaoJdbc(conexao);
            flashcardDao = new FlashcardDaoJdbc(conexao);
            revisaoDao = new RevisaoDaoJdbc(conexao);
            System.out.println("[BANCO] Conectado ao PostgreSQL: os dados ficam gravados.");
        } catch (SQLException e) {
            System.out.println("[AVISO] Sem PostgreSQL: " + explicar(e));
            System.out.println("[AVISO] Usando memória: os dados somem quando o programa fechar.");
            RevisaoDaoMemoria revisaoMemoria = new RevisaoDaoMemoria();
            FlashcardDaoMemoria flashcardMemoria = new FlashcardDaoMemoria(revisaoMemoria);
            disciplinaDao = new DisciplinaDaoMemoria(flashcardMemoria);
            flashcardDao = flashcardMemoria;
            revisaoDao = revisaoMemoria;
        }

        if (args.length > 0 && args[0].equals("--exemplo")) {
            DadosExemplo.carregar(disciplinaDao, flashcardDao, revisaoDao);
        }

        DisciplinaService disciplinaService = new DisciplinaService(disciplinaDao);
        FlashcardService flashcardService = new FlashcardService(flashcardDao, revisaoDao);

        MenuConsole menu = new MenuConsole(disciplinaService, flashcardService);
        menu.iniciar();

        fechar(conexao);
    }

    /** Traduz o erro mais comum (driver fora do classpath) para uma frase clara. */
    private static String explicar(SQLException e) {
        String mensagem = e.getMessage();
        if (mensagem != null && mensagem.startsWith("No suitable driver")) {
            return "o driver JDBC não está no classpath (rode com lib/postgresql-42.7.4.jar).";
        }
        return mensagem;
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
