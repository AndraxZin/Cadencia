package br.com.cadencia.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abre a conexão com o PostgreSQL.
 *
 * Endereço, usuário e senha vêm de variáveis de ambiente, para a senha nunca
 * ficar escrita no código (que vai para o GitHub):
 *   CADENCIA_DB_URL      (padrão: jdbc:postgresql://localhost:5432/cadencia)
 *   CADENCIA_DB_USER     (padrão: postgres)
 *   CADENCIA_DB_PASSWORD (padrão: vazio)
 */
public final class ConexaoBanco {

    private ConexaoBanco() {
        // Só tem método estático: ninguém precisa criar objeto desta classe.
    }

    /**
     * Conecta e confere se as tabelas existem.
     *
     * @throws SQLException se não houver driver, o banco estiver desligado,
     *                      a senha estiver errada ou faltar rodar o schema.sql
     */
    public static Connection abrir() throws SQLException {
        String url = variavel("CADENCIA_DB_URL", "jdbc:postgresql://localhost:5432/cadencia");
        String usuario = variavel("CADENCIA_DB_USER", "postgres");
        String senha = variavel("CADENCIA_DB_PASSWORD", "");

        // O DriverManager procura, entre os drivers do classpath, um que aceite
        // endereços "jdbc:postgresql:" (o .jar da pasta lib/).
        DriverManager.setLoginTimeout(5);
        Connection conexao = DriverManager.getConnection(url, usuario, senha);
        try {
            verificarTabelas(conexao);
        } catch (SQLException e) {
            conexao.close();
            throw new SQLException("conectou, mas as tabelas não existem: rode o sql/schema.sql", e);
        }
        return conexao;
    }

    private static void verificarTabelas(Connection conexao) throws SQLException {
        // Só queremos saber se a consulta roda: se faltar uma tabela, dá SQLException.
        try (Statement comando = conexao.createStatement()) {
            comando.executeQuery("SELECT 1 FROM disciplina, flashcard, revisao LIMIT 1").close();
        }
    }

    private static String variavel(String nome, String padrao) {
        String valor = System.getenv(nome);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }
}
