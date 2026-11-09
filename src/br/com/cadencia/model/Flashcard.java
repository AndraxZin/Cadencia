package br.com.cadencia.model;

import java.time.LocalDate;

/**
 * Um cartão de estudo: a frente (pergunta) e o verso (resposta),
 * ligado a uma disciplina.
 *
 * Regra de revisão (RF-05): o cartão novo começa na etapa 0 e a primeira
 * revisão é no dia seguinte. Etapas: 0 = 1 dia, 1 = 3 dias, 2 = 7 dias, 3 = 15 dias.
 */
public class Flashcard {

    private final int codigo;
    private final Disciplina disciplina;
    private final String frente;
    private final String verso;

    // Estes dois vão mudar quando o aluno revisar o cartão (RF-04/RF-05,
    // ainda não feito). Por isso não são "final".
    private int etapa;
    private LocalDate proximaRevisao;

    public Flashcard(int codigo, Disciplina disciplina, String frente, String verso) {
        this.codigo = codigo;
        this.disciplina = disciplina;
        this.frente = frente;
        this.verso = verso;
        this.etapa = 0;
        this.proximaRevisao = LocalDate.now().plusDays(1);
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
