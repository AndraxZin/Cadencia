package br.com.cadencia.service;

import br.com.cadencia.dao.DisciplinaDaoMemoria;
import br.com.cadencia.dao.FlashcardDaoMemoria;
import br.com.cadencia.dao.RevisaoDaoMemoria;
import br.com.cadencia.model.Disciplina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes UNITÁRIOS das regras de disciplina (RF-01, RF-03 e RF-07), com DAOs em memória.
 * assertThrows confere que o código lança o erro esperado (a regra recusou).
 */
class DisciplinaServiceTest {

    private DisciplinaService disciplinaService;
    private FlashcardService flashcardService;

    @BeforeEach
    void prepararCenario() {
        RevisaoDaoMemoria revisaoDao = new RevisaoDaoMemoria();
        FlashcardDaoMemoria flashcardDao = new FlashcardDaoMemoria(revisaoDao);
        disciplinaService = new DisciplinaService(new DisciplinaDaoMemoria(flashcardDao));
        flashcardService = new FlashcardService(flashcardDao, revisaoDao);
    }

    @Test
    @DisplayName("Cadastra a disciplina e tira os espaços das pontas")
    void cadastraDisciplina() {
        Disciplina nova = disciplinaService.cadastrar("  Banco de Dados  ", "Prof. Exemplo");

        assertEquals("Banco de Dados", nova.getNome());
        assertEquals(1, disciplinaService.listar().size());
    }

    @Test
    @DisplayName("Nome repetido é recusado, mesmo com maiúsculas diferentes")
    void nomeRepetidoERecusado() {
        disciplinaService.cadastrar("Banco de Dados", "");

        assertThrows(IllegalArgumentException.class,
                () -> disciplinaService.cadastrar("banco de dados", ""));
    }

    @Test
    @DisplayName("Nome vazio é recusado")
    void nomeVazioERecusado() {
        assertThrows(IllegalArgumentException.class, () -> disciplinaService.cadastrar("   ", ""));
    }

    @Test
    @DisplayName("Nome com mais de 100 caracteres é recusado")
    void nomeLongoERecusado() {
        String nomeLongo = "a".repeat(101);

        assertThrows(IllegalArgumentException.class, () -> disciplinaService.cadastrar(nomeLongo, ""));
    }

    @Test
    @DisplayName("Remover a disciplina remove também os flashcards dela")
    void removerDisciplinaRemoveOsFlashcards() {
        Disciplina disciplina = disciplinaService.cadastrar("Banco de Dados", "");
        flashcardService.cadastrar(disciplina, "Pergunta", "Resposta");

        disciplinaService.remover(disciplina);

        assertTrue(disciplinaService.listar().isEmpty());
        assertTrue(flashcardService.listarPorDisciplina(disciplina).isEmpty());
    }
}
