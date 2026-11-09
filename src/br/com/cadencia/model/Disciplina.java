package br.com.cadencia.model;

/**
 * Uma disciplina da faculdade (ex.: "Banco de Dados").
 *
 * É uma classe de MODELO: só guarda dados. Não tem regra de negócio
 * (isso fica no service) e não conversa com o usuário (isso fica na view).
 */
public class Disciplina {

    // "final" = o valor é definido no construtor e nunca mais muda.
    private final int codigo;
    private final String nome;
    private final String professor; // texto vazio ("") quando não informado

    public Disciplina(int codigo, String nome, String professor) {
        this.codigo = codigo;
        this.nome = nome;
        this.professor = professor;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getProfessor() {
        return professor;
    }
}
