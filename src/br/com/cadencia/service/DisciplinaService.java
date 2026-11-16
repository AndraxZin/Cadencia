package br.com.cadencia.service;

import br.com.cadencia.dao.DisciplinaDao;
import br.com.cadencia.model.Disciplina;

import java.util.List;
import java.util.Optional;

/**
 * Regras de negócio das disciplinas (RF-01, RF-03 e RF-07).
 *
 * Não sabe se os dados ficam no PostgreSQL ou em memória: só conversa com a
 * interface DisciplinaDao, que o Main escolhe e entrega pronta.
 */
public class DisciplinaService {

    private static final int TAMANHO_MAXIMO = 100; // VARCHAR(100) na tabela

    private final DisciplinaDao disciplinaDao;

    public DisciplinaService(DisciplinaDao disciplinaDao) {
        this.disciplinaDao = disciplinaDao;
    }

    /**
     * Cadastra uma disciplina. O nome é obrigatório e não pode repetir
     * (sem diferenciar maiúscula de minúscula: "banco de dados" = "Banco de Dados").
     *
     * @throws IllegalArgumentException se o nome estiver vazio, longo demais ou já existir
     */
    public Disciplina cadastrar(String nome, String professor) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da disciplina é obrigatório.");
        }
        String nomeLimpo = nome.trim();
        String professorLimpo = (professor == null) ? "" : professor.trim();

        if (nomeLimpo.length() > TAMANHO_MAXIMO || professorLimpo.length() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("Nome e professor podem ter no máximo "
                    + TAMANHO_MAXIMO + " caracteres.");
        }

        boolean jaExiste = disciplinaDao.listar().stream()
                .anyMatch(d -> d.getNome().equalsIgnoreCase(nomeLimpo));
        if (jaExiste) {
            throw new IllegalArgumentException(
                    "Já existe uma disciplina chamada \"" + nomeLimpo + "\".");
        }

        return disciplinaDao.inserir(nomeLimpo, professorLimpo);
    }

    public List<Disciplina> listar() {
        return disciplinaDao.listar();
    }

    /** Procura pelo código. Optional = "pode ter achado ou não" (evita devolver null). */
    public Optional<Disciplina> buscarPorCodigo(int codigo) {
        return disciplinaDao.buscarPorCodigo(codigo);
    }

    /**
     * RF-07: remove a disciplina; os flashcards dela e o histórico vão junto.
     * A confirmação (S/N) é pedida pela tela antes de chamar este método.
     */
    public void remover(Disciplina disciplina) {
        disciplinaDao.remover(disciplina.getCodigo());
    }
}
