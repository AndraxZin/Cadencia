package br.com.cadencia.model;

import java.time.LocalDateTime;

/**
 * Uma resposta do aluno a um flashcard, num certo momento (RF-06).
 * Cada revisão é uma linha do histórico do cartão.
 */
public class Revisao {

    private final int codigo;
    private final int codigoFlashcard;
    private final LocalDateTime revisadoEm;
    private final boolean acertou;

    public Revisao(int codigo, int codigoFlashcard, LocalDateTime revisadoEm, boolean acertou) {
        this.codigo = codigo;
        this.codigoFlashcard = codigoFlashcard;
        this.revisadoEm = revisadoEm;
        this.acertou = acertou;
    }

    public int getCodigo() {
        return codigo;
    }

    public int getCodigoFlashcard() {
        return codigoFlashcard;
    }

    public LocalDateTime getRevisadoEm() {
        return revisadoEm;
    }

    public boolean isAcertou() {
        return acertou;
    }
}
