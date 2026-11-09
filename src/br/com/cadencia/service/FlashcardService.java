package br.com.cadencia.service;

import br.com.cadencia.model.Disciplina;
import br.com.cadencia.model.Flashcard;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio dos flashcards (RF-02 e RF-03).
 *
 * Assim como DisciplinaService, guarda os dados em memória por enquanto.
 */
public class FlashcardService {

    private final List<Flashcard> flashcards = new ArrayList<>();
    private int proximoCodigo = 1;

    /**
     * Cadastra um flashcard numa disciplina. Frente e verso são obrigatórios.
     * O cartão nasce na etapa 0, com primeira revisão no dia seguinte
     * (quem garante isso é o construtor de Flashcard).
     *
     * @throws IllegalArgumentException se faltar disciplina, frente ou verso
     */
    public Flashcard cadastrar(Disciplina disciplina, String frente, String verso) {
        if (disciplina == null) {
            throw new IllegalArgumentException("Escolha uma disciplina para o flashcard.");
        }
        if (frente == null || frente.isBlank()) {
            throw new IllegalArgumentException("A frente do flashcard é obrigatória.");
        }
        if (verso == null || verso.isBlank()) {
            throw new IllegalArgumentException("O verso do flashcard é obrigatório.");
        }

        Flashcard novo = new Flashcard(proximoCodigo++, disciplina, frente.trim(), verso.trim());
        flashcards.add(novo);
        return novo;
    }

    /** Flashcards de uma disciplina, na ordem em que foram cadastrados. */
    public List<Flashcard> listarPorDisciplina(int codigoDisciplina) {
        return flashcards.stream()
                .filter(f -> f.getDisciplina().getCodigo() == codigoDisciplina)
                .toList();
    }
}
