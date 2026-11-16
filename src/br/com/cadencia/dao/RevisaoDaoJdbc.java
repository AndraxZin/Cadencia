package br.com.cadencia.dao;

import br.com.cadencia.model.Revisao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda o histórico na tabela "revisao" do PostgreSQL, via JDBC.
 * Segue o mesmo padrão de DisciplinaDaoJdbc.
 */
public class RevisaoDaoJdbc implements RevisaoDao {

    private final Connection conexao;

    public RevisaoDaoJdbc(Connection conexao) {
        this.conexao = conexao;
    }

    @Override
    public Revisao inserir(int codigoFlashcard, boolean acertou) {
        // revisado_em fica com o valor padrão da tabela (CURRENT_TIMESTAMP).
        String sql = "INSERT INTO revisao (id_flashcard, acertou) VALUES (?, ?) "
                + "RETURNING id_revisao, revisado_em";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, codigoFlashcard);
            comando.setBoolean(2, acertou);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return new Revisao(resultado.getInt("id_revisao"), codigoFlashcard,
                        horaLocal(resultado), acertou);
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Não foi possível registrar a revisão.", e);
        }
    }

    @Override
    public List<Revisao> listarPorFlashcard(int codigoFlashcard) {
        String sql = "SELECT id_revisao, id_flashcard, revisado_em, acertou FROM revisao "
                + "WHERE id_flashcard = ? ORDER BY revisado_em DESC, id_revisao DESC";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, codigoFlashcard);
            try (ResultSet resultado = comando.executeQuery()) {
                List<Revisao> lista = new ArrayList<>();
                while (resultado.next()) {
                    lista.add(new Revisao(
                            resultado.getInt("id_revisao"),
                            resultado.getInt("id_flashcard"),
                            horaLocal(resultado),
                            resultado.getBoolean("acertou")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Não foi possível consultar o histórico.", e);
        }
    }

    /** TIMESTAMPTZ guarda o instante com fuso horário; aqui vira a hora local do computador. */
    private LocalDateTime horaLocal(ResultSet resultado) throws SQLException {
        OffsetDateTime instante = resultado.getObject("revisado_em", OffsetDateTime.class);
        return instante.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }
}
