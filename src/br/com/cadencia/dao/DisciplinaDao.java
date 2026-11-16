package br.com.cadencia.dao;

import br.com.cadencia.model.Disciplina;

import java.util.List;
import java.util.Optional;

/**
 * O que o sistema precisa para guardar disciplinas, sem dizer ONDE guarda.
 *
 * Uma interface é uma lista de métodos prometidos, sem o corpo. Duas classes
 * cumprem a promessa: DisciplinaDaoJdbc (PostgreSQL) e DisciplinaDaoMemoria
 * (lista em memória). O service só conhece esta interface, então funciona igual
 * com qualquer uma das duas (polimorfismo). Quem escolhe qual usar é o Main.
 */
public interface DisciplinaDao {

    /** Grava a disciplina e a devolve já com o código gerado. */
    Disciplina inserir(String nome, String professor);

    /** Todas as disciplinas, em ordem de código. */
    List<Disciplina> listar();

    Optional<Disciplina> buscarPorCodigo(int codigo);

    /** Remove a disciplina, os flashcards dela e o histórico desses flashcards. */
    void remover(int codigo);
}
