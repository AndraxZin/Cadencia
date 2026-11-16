package br.com.cadencia.model;

import java.time.LocalDate;

/**
 * Um cartão de estudo: a frente (pergunta) e o verso (resposta),
 * ligado a uma disciplina.
 *
 * A etapa (0 a 3) e a data da próxima revisão mudam a cada resposta do aluno.
 * QUEM calcula os novos valores é o FlashcardService (regra de negócio);
 * esta classe só guarda os dados.
 */
public class Flashcard {

    private final int codigo;
    private final Disciplina disciplina;
    private final String frente;
    private final String verso;

    // Mudam a cada revisão, por isso não são "final".
    private int etapa;
    private LocalDate proximaRevisao;

    public Flashcard(int codigo, Disciplina disciplina, String frente, String verso,
                     int etapa, LocalDate proximaRevisao) {
        this.codigo = codigo;
        this.disciplina = disciplina;
        this.frente = frente;
        this.verso = verso;
        this.etapa = etapa;
        this.proximaRevisao = proximaRevisao;
    }

    /** Guarda a nova etapa e a nova data, já calculadas pelo FlashcardService. */
    public void reagendar(int novaEtapa, LocalDate novaData) {
        this.etapa = novaEtapa;
        this.proximaRevisao = novaData;
    }

    public int getCodigo() {
        return codigo;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public String getFrente() {
        return frente;
    }

    public String getVerso() {
        return verso;
    }

    public int getEtapa() {
        return etapa;
    }

    public LocalDate getProximaRevisao() {
        return proximaRevisao;
    }
}
