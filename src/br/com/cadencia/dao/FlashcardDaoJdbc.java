package br.com.cadencia.dao;

import br.com.cadencia.model.Disciplina;
import br.com.cadencia.model.Flashcard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda os flashcards na tabela "flashcard" do PostgreSQL, via JDBC.
 * Segue o mesmo padrão de DisciplinaDaoJdbc.
 */
public class FlashcardDaoJdbc implements FlashcardDao {

    // Toda leitura de flashcard traz junto a disciplina dele (JOIN),
    // porque o objeto Flashcard guarda a sua Disciplina.
    private static final String SELECT_COM_DISCIPLINA =
            "SELECT f.id_flashcard, f.frente, f.verso, f.etapa, f.proxima_revisao, "
            + "d.id_disciplina, d.nome, d.professor "
            + "FROM flashcard f JOIN disciplina d ON d.id_disciplina = f.id_disciplina ";

    private final Connection conexao;

    public FlashcardDaoJdbc(Connection conexao) {
        this.conexao = conexao;
    }

    @Override
    public Flashcard inserir(Disciplina disciplina, String frente, String verso,
                             int etapa, LocalDate proximaRevisao) {
        String sql = "INSERT INTO flashcard (id_disciplina, frente, verso, etapa, proxima_revisao) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id_flashcard";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, disciplina.getCodigo());
            comando.setString(2, frente);
            comando.setString(3, verso);
            comando.setInt(4, etapa);
            comando.setObject(5, proximaRevisao); // LocalDate vira DATE
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return new Flashcard(resultado.getInt("id_flashcard"), disciplina, frente, verso,
                        etapa, proximaRevisao);
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Não foi possível cadastrar o flashcard no banco.", e);
        }
    }

    @Override
    public List<Flashcard> listarPorDisciplina(int codigoDisciplina) {
        return listar(SELECT_COM_DISCIPLINA + "WHERE f.id_disciplina = ? ORDER BY f.id_flashcard",
                codigoDisciplina);
    }

    @Override
    public List<Flashcard> listarParaRevisar(LocalDate data) {
        // Primeiro os mais atrasados. O índice idx_flashcard_proxima_revisao acelera este WHERE.
        return listar(SELECT_COM_DISCIPLINA
                + "WHERE f.proxima_revisao <= ? ORDER BY f.proxima_revisao, f.id_flashcard", data);
    }

    @Override
    public Optional<Flashcard> buscarPorCodigo(int codigo) {
        List<Flashcard> lista = listar(SELECT_COM_DISCIPLINA + "WHERE f.id_flashcard = ?", codigo);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    @Override
    public void atualizarAgenda(Flashcard flashcard) {
        String sql = "UPDATE flashcard SET etapa = ?, proxima_revisao = ? WHERE id_flashcard = ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, flashcard.getEtapa());
            comando.setObject(2, flashcard.getProximaRevisao());
            comando.setInt(3, flashcard.getCodigo());
            comando.executeUpdate();
        } catch (SQLException e) {
            throw new AcessoDadosException("Não foi possível atualizar o flashcard.", e);
        }
    }

    @Override
    public void remover(int codigo) {
        // O histórico sai junto por causa do ON DELETE CASCADE (schema.sql).
        String sql = "DELETE FROM flashcard WHERE id_flashcard = ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, codigo);
            comando.executeUpdate();
        } catch (SQLException e) {
            throw new AcessoDadosException("Não foi possível remover o flashcard.", e);
        }
    }

    /** Roda um SELECT com um único "?" e monta a lista de flashcards. */
    private List<Flashcard> listar(String sql, Object parametro) {
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setObject(1, parametro);
            try (ResultSet resultado = comando.executeQuery()) {
                List<Flashcard> lista = new ArrayList<>();
                while (resultado.next()) {
                    lista.add(montar(resultado));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Não foi possível consultar os flashcards.", e);
        }
    }

    /** Transforma a linha atual do ResultSet num Flashcard (com a sua Disciplina). */
    private Flashcard montar(ResultSet resultado) throws SQLException {
        String professor = resultado.getString("professor");
        Disciplina disciplina = new Disciplina(
                resultado.getInt("id_disciplina"),
                resultado.getString("nome"),
                professor == null ? "" : professor);
        return new Flashcard(
                resultado.getInt("id_flashcard"),
                disciplina,
                resultado.getString("frente"),
                resultado.getString("verso"),
                resultado.getInt("etapa"),
                resultado.getObject("proxima_revisao", LocalDate.class));
    }
}
