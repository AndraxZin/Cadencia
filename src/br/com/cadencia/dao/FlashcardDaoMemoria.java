package br.com.cadencia.dao;

import br.com.cadencia.model.Disciplina;
import br.com.cadencia.model.Flashcard;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Guarda os flashcards numa lista em memória (usada sem PostgreSQL).
 */
public class FlashcardDaoMemoria implements FlashcardDao {

    private final List<Flashcard> flashcards = new ArrayList<>();
    private final RevisaoDaoMemoria revisaoDao; // para imitar o ON DELETE CASCADE do banco
    private int proximoCodigo = 1;

    public FlashcardDaoMemoria(RevisaoDaoMemoria revisaoDao) {
        this.revisaoDao = revisaoDao;
    }

    @Override
    public Flashcard inserir(Disciplina disciplina, String frente, String verso,
                             int etapa, LocalDate proximaRevisao) {
        Flashcard novo = new Flashcard(proximoCodigo++, disciplina, frente, verso, etapa, proximaRevisao);
        flashcards.add(novo);
        return novo;
    }

    @Override
    public List<Flashcard> listarPorDisciplina(int codigoDisciplina) {
        return flashcards.stream()
                .filter(f -> f.getDisciplina().getCodigo() == codigoDisciplina)
                .toList();
    }

    @Override
    public List<Flashcard> listarParaRevisar(LocalDate data) {
        // Mesma ordem do banco: primeiro os mais atrasados.
        return flashcards.stream()
                .filter(f -> !f.getProximaRevisao().isAfter(data))
                .sorted(Comparator.comparing(Flashcard::getProximaRevisao)
                        .thenComparing(Flashcard::getCodigo))
                .toList();
    }

    @Override
    public Optional<Flashcard> buscarPorCodigo(int codigo) {
        return flashcards.stream()
                .filter(f -> f.getCodigo() == codigo)
                .findFirst();
    }

    @Override
    public void atualizarAgenda(Flashcard flashcard) {
        // Troca o cartão guardado pela versão atualizada (mesmo código).
        for (int i = 0; i < flashcards.size(); i++) {
            if (flashcards.get(i).getCodigo() == flashcard.getCodigo()) {
                flashcards.set(i, flashcard);
                return;
            }
        }
    }

    @Override
    public void remover(int codigo) {
        flashcards.removeIf(f -> f.getCodigo() == codigo);
        revisaoDao.removerDoFlashcard(codigo); // o histórico vai junto
    }

    /** Remove todos os flashcards de uma disciplina (chamado ao remover a disciplina). */
    void removerDaDisciplina(int codigoDisciplina) {
        List<Integer> codigos = listarPorDisciplina(codigoDisciplina).stream()
                .map(Flashcard::getCodigo)
                .toList();
        for (int codigo : codigos) {
            remover(codigo);
        }
    }
}
