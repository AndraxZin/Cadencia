package br.com.cadencia.view;

import java.util.Optional;

/**
 * As opções do menu principal.
 *
 * Um enum é uma lista fechada de valores. Usar enum em vez de "números soltos"
 * evita erro de digitação: o compilador reclama se a opção não existir.
 * A ordem das constantes é a ordem em que o menu aparece na tela.
 */
public enum OpcaoMenuPrincipal {

    CADASTRAR_DISCIPLINA(1, "Cadastrar disciplina"),
    LISTAR_DISCIPLINAS(2, "Listar disciplinas"),
    CADASTRAR_FLASHCARD(3, "Cadastrar flashcard"),
    LISTAR_FLASHCARDS(4, "Listar flashcards de uma disciplina"),
    SAIR(0, "Sair");

    private final int codigo;
    private final String descricao;

    OpcaoMenuPrincipal(int codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Converte o número digitado na opção correspondente (vazio se não existir). */
    public static Optional<OpcaoMenuPrincipal> deCodigo(int codigo) {
        for (OpcaoMenuPrincipal opcao : values()) {
            if (opcao.codigo == codigo) {
                return Optional.of(opcao);
            }
        }
        return Optional.empty();
    }
}
