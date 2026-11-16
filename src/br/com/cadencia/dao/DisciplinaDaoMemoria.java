package br.com.cadencia.dao;

import br.com.cadencia.model.Disciplina;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda as disciplinas numa lista em memória: os dados somem quando o
 * programa fecha. É usada quando o PostgreSQL não está disponível.
 *
 * "implements DisciplinaDao" = esta classe cumpre a promessa da interface.
 * "@Override" marca cada método prometido (o compilador confere).
 */
public class DisciplinaDaoMemoria implements DisciplinaDao {

    private final List<Disciplina> disciplinas = new ArrayList<>();
    private final FlashcardDaoMemoria flashcardDao; // para imitar o ON DELETE CASCADE do banco
    private int proximoCodigo = 1;

    public DisciplinaDaoMemoria(FlashcardDaoMemoria flashcardDao) {
        this.flashcardDao = flashcardDao;
    }

    @Override
    public Disciplina inserir(String nome, String professor) {
        Disciplina nova = new Disciplina(proximoCodigo++, nome, professor);
        disciplinas.add(nova);
        return nova;
    }

    @Override
    public List<Disciplina> listar() {
        return List.copyOf(disciplinas);
    }

    @Override
    public Optional<Disciplina> buscarPorCodigo(int codigo) {
        return disciplinas.stream()
                .filter(d -> d.getCodigo() == codigo)
                .findFirst();
    }

    @Override
    public void remover(int codigo) {
        disciplinas.removeIf(d -> d.getCodigo() == codigo);
        // No banco, o ON DELETE CASCADE apaga os flashcards sozinho. Aqui fazemos à mão.
        flashcardDao.removerDaDisciplina(codigo);
    }
}
