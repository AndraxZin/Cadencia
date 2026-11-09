package br.com.cadencia.service;

import br.com.cadencia.model.Disciplina;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negócio das disciplinas (RF-01 e RF-03).
 *
 * Por enquanto os dados ficam numa lista em memória: somem quando o programa
 * fecha. Na próxima etapa esta lista será trocada pelo banco PostgreSQL (JDBC).
 */
public class DisciplinaService {

    private final List<Disciplina> disciplinas = new ArrayList<>();
    private int proximoCodigo = 1;

    /**
     * Cadastra uma disciplina. O nome é obrigatório e não pode repetir.
     *
     * @throws IllegalArgumentException se o nome estiver vazio ou já existir
     */
    public Disciplina cadastrar(String nome, String professor) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da disciplina é obrigatório.");
        }
        String nomeLimpo = nome.trim();

        boolean jaExiste = disciplinas.stream()
                .anyMatch(d -> d.getNome().equalsIgnoreCase(nomeLimpo));
        if (jaExiste) {
            throw new IllegalArgumentException(
                    "Já existe uma disciplina chamada \"" + nomeLimpo + "\".");
        }

        String professorLimpo = (professor == null) ? "" : professor.trim();
        Disciplina nova = new Disciplina(proximoCodigo++, nomeLimpo, professorLimpo);
        disciplinas.add(nova);
        return nova;
    }

    /** Devolve uma cópia da lista: quem chamar não consegue alterar a lista original. */
    public List<Disciplina> listar() {
        return List.copyOf(disciplinas);
    }

    /** Procura pelo código. Optional = "pode ter achado ou não" (evita devolver null). */
    public Optional<Disciplina> buscarPorCodigo(int codigo) {
        return disciplinas.stream()
                .filter(d -> d.getCodigo() == codigo)
                .findFirst();
    }
}
