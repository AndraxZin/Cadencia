package br.com.cadencia.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * As opções do menu principal.
 *
 * Um enum é uma lista fechada de valores. Usar enum em vez de "números soltos"
 * evita erro de digitação: o compilador reclama se a opção não existir.
 *
 * Cada opção tem um número, um grupo (a coluna onde aparece no menu), um rótulo
 * curto (o texto dentro da coluna) e uma descrição completa (o título da tela).
 * O que o aluno faz todo dia (revisar) vem primeiro: opção 1.
 */
public enum OpcaoMenuPrincipal {

    REVISAR_HOJE(1, Grupo.ESTUDAR, "Revisar hoje", "Revisar hoje"),
    VER_HISTORICO(2, Grupo.ESTUDAR, "Ver histórico", "Ver histórico de um flashcard"),
    CADASTRAR_DISCIPLINA(3, Grupo.DISCIPLINAS, "Cadastrar", "Cadastrar disciplina"),
    LISTAR_DISCIPLINAS(4, Grupo.DISCIPLINAS, "Listar", "Listar disciplinas"),
    REMOVER_DISCIPLINA(5, Grupo.DISCIPLINAS, "Remover", "Remover disciplina"),
    CADASTRAR_FLASHCARD(6, Grupo.FLASHCARDS, "Cadastrar", "Cadastrar flashcard"),
    LISTAR_FLASHCARDS(7, Grupo.FLASHCARDS, "Listar", "Listar flashcards de uma disciplina"),
    REMOVER_FLASHCARD(8, Grupo.FLASHCARDS, "Remover", "Remover flashcard"),
    SAIR(0, Grupo.NENHUM, "Sair", "Sair");

    /** As colunas do menu. NENHUM = fora das colunas (o "0 Sair", embaixo). */
    public enum Grupo { ESTUDAR, DISCIPLINAS, FLASHCARDS, NENHUM }

    private final int codigo;
    private final Grupo grupo;
    private final String rotulo;
    private final String descricao;

    OpcaoMenuPrincipal(int codigo, Grupo grupo, String rotulo, String descricao) {
        this.codigo = codigo;
        this.grupo = grupo;
        this.rotulo = rotulo;
        this.descricao = descricao;
    }

    public int getCodigo() {
        return codigo;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getDescricao() {
        return descricao;
    }

    /** As opções de um grupo, na ordem em que aparecem na coluna. */
    public static List<OpcaoMenuPrincipal> doGrupo(Grupo grupo) {
        List<OpcaoMenuPrincipal> opcoes = new ArrayList<>();
        for (OpcaoMenuPrincipal opcao : values()) {
            if (opcao.grupo == grupo) {
                opcoes.add(opcao);
            }
        }
        return opcoes;
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
