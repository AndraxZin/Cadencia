package br.com.cadencia.dao;

/**
 * Erro ao ler ou gravar dados (ex.: o banco caiu no meio do uso).
 *
 * As classes JDBC transformam a SQLException (que o Java obriga a tratar)
 * nesta exceção. Assim o service e a view não precisam conhecer JDBC.
 * A view mostra a mensagem e o programa continua rodando (RNF-04).
 */
public class AcessoDadosException extends RuntimeException {

    public AcessoDadosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
