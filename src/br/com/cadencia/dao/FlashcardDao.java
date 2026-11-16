package br.com.cadencia.dao;

import br.com.cadencia.model.Disciplina;
import br.com.cadencia.model.Flashcard;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * O que o sistema precisa para guardar flashcards.
 * Implementada por FlashcardDaoJdbc (PostgreSQL) e FlashcardDaoMemoria.
 */
public interface FlashcardDao {

    /** Grava o flashcard e o devolve já com o código gerado. */
    Flashcard inserir(Disciplina disciplina, String frente, String verso,
                      int etapa, LocalDate proximaRevisao);

    /** Flashcards de uma disciplina, em ordem de código. */
    List<Flashcard> listarPorDisciplina(int codigoDisciplina);

    /** Flashcards com revisão marcada para a data informada ou antes (os "vencidos"). */
    List<Flashcard> listarParaRevisar(LocalDate data);

    Optional<Flashcard> buscarPorCodigo(int codigo);

    /** Grava a etapa e a data da próxima revisão que o flashcard tem agora. */
    void atualizarAgenda(Flashcard flashcard);

    /** Remove o flashcard e o histórico dele. */
    void remover(int codigo);
}
