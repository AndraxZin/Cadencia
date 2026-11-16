package br.com.cadencia.dao;

import br.com.cadencia.model.Revisao;

import java.util.List;

/**
 * O que o sistema precisa para guardar o histórico de revisões.
 * Implementada por RevisaoDaoJdbc (PostgreSQL) e RevisaoDaoMemoria.
 */
public interface RevisaoDao {

    /** Registra uma resposta do aluno, com a data e hora atuais. */
    Revisao inserir(int codigoFlashcard, boolean acertou);

    /** Histórico de um flashcard, da revisão mais recente para a mais antiga. */
    List<Revisao> listarPorFlashcard(int codigoFlashcard);
}
