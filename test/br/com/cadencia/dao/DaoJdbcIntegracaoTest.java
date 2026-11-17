package br.com.cadencia.dao;

import br.com.cadencia.model.Disciplina;
import br.com.cadencia.model.Flashcard;
import br.com.cadencia.model.Revisao;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testes de INTEGRAÇÃO: os DAOs JDBC conversando com o PostgreSQL de verdade
 * (usa as mesmas variáveis de ambiente do programa).
 *
 * Cada teste roda dentro de uma transação que é desfeita no fim (rollback):
 * o banco fica exatamente como estava. Sem PostgreSQL disponível, os testes
 * são pulados (assumeTrue), em vez de falhar.
 */
class DaoJdbcIntegracaoTest {

    private static Connection conexao;

    private DisciplinaDaoJdbc disciplinaDao;
    private FlashcardDaoJdbc flashcardDao;
    private RevisaoDaoJdbc revisaoDao;

    @BeforeAll
    static void conectar() {
        try {
            conexao = ConexaoBanco.abrir();
        } catch (SQLException e) {
            conexao = null;
        }
    }

    @AfterAll
    static void desconectar() throws SQLException {
        if (conexao != null) {
            conexao.close();
        }
    }

    @BeforeEach
    void iniciarTransacao() throws SQLException {
        assumeTrue(conexao != null, "PostgreSQL indisponível: testes de integração pulados.");
        conexao.setAutoCommit(false); // daqui em diante, nada é gravado até um commit
        disciplinaDao = new DisciplinaDaoJdbc(conexao);
        flashcardDao = new FlashcardDaoJdbc(conexao);
        revisaoDao = new RevisaoDaoJdbc(conexao);
    }

    @AfterEach
    void desfazerTransacao() throws SQLException {
        if (conexao != null) {
            conexao.rollback(); // desfaz tudo o que o teste gravou
            conexao.setAutoCommit(true);
        }
    }

    /** Nome que não existe no banco, para não colidir com dados reais. */
    private static String nomeUnico() {
        return "Teste " + System.nanoTime();
    }

    @Test
    @DisplayName("Grava a disciplina e a encontra pelo código gerado pelo banco")
    void gravaEBuscaDisciplina() {
        String nome = nomeUnico();
        Disciplina nova = disciplinaDao.inserir(nome, "");

        Optional<Disciplina> lida = disciplinaDao.buscarPorCodigo(nova.getCodigo());

        assertTrue(lida.isPresent());
        assertEquals(nome, lida.get().getNome());
        assertEquals("", lida.get().getProfessor()); // NULL na tabela vira "" no Java
    }

    @Test
    @DisplayName("Flashcard vencido entra na revisão do dia; o de amanhã não")
    void revisaoDoDiaUsaAData() {
        Disciplina disciplina = disciplinaDao.inserir(nomeUnico(), "");
        Flashcard vencido = flashcardDao.inserir(disciplina, "P1", "R1", 0, LocalDate.now());
        Flashcard deAmanha = flashcardDao.inserir(disciplina, "P2", "R2", 0, LocalDate.now().plusDays(1));

        List<Integer> codigos = flashcardDao.listarParaRevisar(LocalDate.now()).stream()
                .map(Flashcard::getCodigo)
                .toList();

        assertTrue(codigos.contains(vencido.getCodigo()));
        assertFalse(codigos.contains(deAmanha.getCodigo()));
    }

    @Test
    @DisplayName("Atualizar a agenda grava a nova etapa e a nova data")
    void atualizarAgendaGravaEtapaEData() {
        Disciplina disciplina = disciplinaDao.inserir(nomeUnico(), "");
        Flashcard cartao = flashcardDao.inserir(disciplina, "P", "R", 0, LocalDate.now());

        cartao.reagendar(2, LocalDate.now().plusDays(7));
        flashcardDao.atualizarAgenda(cartao);

        Flashcard lido = flashcardDao.buscarPorCodigo(cartao.getCodigo()).orElseThrow();
        assertEquals(2, lido.getEtapa());
        assertEquals(LocalDate.now().plusDays(7), lido.getProximaRevisao());
    }

    @Test
    @DisplayName("Revisão gravada aparece no histórico do flashcard")
    void revisaoGravadaApareceNoHistorico() {
        Disciplina disciplina = disciplinaDao.inserir(nomeUnico(), "");
        Flashcard cartao = flashcardDao.inserir(disciplina, "P", "R", 0, LocalDate.now());

        revisaoDao.inserir(cartao.getCodigo(), true);

        List<Revisao> historico = revisaoDao.listarPorFlashcard(cartao.getCodigo());
        assertEquals(1, historico.size());
        assertTrue(historico.get(0).isAcertou());
    }

    @Test
    @DisplayName("Remover a disciplina apaga flashcards e histórico (ON DELETE CASCADE)")
    void removerDisciplinaApagaEmCascata() {
        Disciplina disciplina = disciplinaDao.inserir(nomeUnico(), "");
        Flashcard cartao = flashcardDao.inserir(disciplina, "P", "R", 0, LocalDate.now());
        revisaoDao.inserir(cartao.getCodigo(), false);

        disciplinaDao.remover(disciplina.getCodigo());

        assertTrue(flashcardDao.buscarPorCodigo(cartao.getCodigo()).isEmpty());
        assertTrue(revisaoDao.listarPorFlashcard(cartao.getCodigo()).isEmpty());
    }

    @Test
    @DisplayName("O banco recusa nome de disciplina repetido (UNIQUE)")
    void bancoRecusaNomeRepetido() {
        String nome = nomeUnico();
        disciplinaDao.inserir(nome, "");

        assertThrows(AcessoDadosException.class, () -> disciplinaDao.inserir(nome, ""));
    }
}
