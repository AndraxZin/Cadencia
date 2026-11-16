package br.com.cadencia.dao;

import br.com.cadencia.model.Revisao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Guarda o histórico de revisões numa lista em memória (usada sem PostgreSQL).
 */
public class RevisaoDaoMemoria implements RevisaoDao {

    private final List<Revisao> revisoes = new ArrayList<>();
    private int proximoCodigo = 1;

    @Override
    public Revisao inserir(int codigoFlashcard, boolean acertou) {
        Revisao nova = new Revisao(proximoCodigo++, codigoFlashcard, LocalDateTime.now(), acertou);
        revisoes.add(nova);
        return nova;
    }

    @Override
    public List<Revisao> listarPorFlashcard(int codigoFlashcard) {
        return revisoes.stream()
                .filter(r -> r.getCodigoFlashcard() == codigoFlashcard)
                .sorted(Comparator.comparing(Revisao::getRevisadoEm)
                        .thenComparing(Revisao::getCodigo)
                        .reversed())
                .toList();
    }

    /** Apaga o histórico de um flashcard (chamado ao remover o flashcard). */
    void removerDoFlashcard(int codigoFlashcard) {
        revisoes.removeIf(r -> r.getCodigoFlashcard() == codigoFlashcard);
    }
}
