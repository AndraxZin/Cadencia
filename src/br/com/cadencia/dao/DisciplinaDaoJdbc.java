package br.com.cadencia.dao;

import br.com.cadencia.model.Disciplina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda as disciplinas na tabela "disciplina" do PostgreSQL, via JDBC.
 *
 * Padrão de cada método:
 *   1. PreparedStatement: o comando SQL com "?" no lugar dos valores.
 *      Os valores entram com setString/setInt, nunca colados no texto do SQL
 *      (isso evita SQL injection: um nome com aspas não quebra o comando).
 *   2. executeQuery (SELECT/RETURNING) devolve um ResultSet, que é lido linha a linha;
 *      executeUpdate (DELETE/UPDATE) devolve só quantas linhas mudaram.
 *   3. try-with-resources: o "try (...)" fecha o comando e o resultado sozinho no fim.
 *   4. SQLException vira AcessoDadosException (o resto do sistema não conhece JDBC).
 */
public class DisciplinaDaoJdbc implements DisciplinaDao {

    private final Connection conexao;

    public DisciplinaDaoJdbc(Connection conexao) {
        this.conexao = conexao;
    }

    @Override
    public Disciplina inserir(String nome, String professor) {
        // RETURNING devolve o código que o banco gerou (GENERATED ALWAYS AS IDENTITY).
        String sql = "INSERT INTO disciplina (nome, professor) VALUES (?, ?) RETURNING id_disciplina";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, nome);
            comando.setString(2, professor.isEmpty() ? null : professor); // vazio vira NULL na tabela
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return new Disciplina(resultado.getInt("id_disciplina"), nome, professor);
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Não foi possível cadastrar a disciplina no banco.", e);
        }
    }

    @Override
    public List<Disciplina> listar() {
        String sql = "SELECT id_disciplina, nome, professor FROM disciplina ORDER BY id_disciplina";
        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            List<Disciplina> lista = new ArrayList<>();
            while (resultado.next()) {
                lista.add(montar(resultado));
            }
            return lista;
        } catch (SQLException e) {
            throw new AcessoDadosException("Não foi possível listar as disciplinas.", e);
        }
    }

    @Override
    public Optional<Disciplina> buscarPorCodigo(int codigo) {
        String sql = "SELECT id_disciplina, nome, professor FROM disciplina WHERE id_disciplina = ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, codigo);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(montar(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("Não foi possível buscar a disciplina.", e);
        }
    }

    @Override
    public void remover(int codigo) {
        // Os flashcards e o histórico saem junto por causa do ON DELETE CASCADE (schema.sql).
        String sql = "DELETE FROM disciplina WHERE id_disciplina = ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, codigo);
            comando.executeUpdate();
        } catch (SQLException e) {
            throw new AcessoDadosException("Não foi possível remover a disciplina.", e);
        }
    }

    /** Transforma a linha atual do ResultSet num objeto Disciplina (NULL vira ""). */
    private Disciplina montar(ResultSet resultado) throws SQLException {
        String professor = resultado.getString("professor");
        return new Disciplina(
                resultado.getInt("id_disciplina"),
                resultado.getString("nome"),
                professor == null ? "" : professor);
    }
}
