package br.com.cadencia.service;

import br.com.cadencia.dao.FlashcardDao;
import br.com.cadencia.dao.RevisaoDao;
import br.com.cadencia.model.Disciplina;
import br.com.cadencia.model.Flashcard;
import br.com.cadencia.model.Revisao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negócio dos flashcards e da revisão espaçada (RF-02 a RF-07).
 *
 * Intervalos fixos (RF-05): etapa 0 = 1 dia, 1 = 3 dias, 2 = 7 dias, 3 = 15 dias.
 * Acertou: sobe uma etapa (a 3 é a última: fica nela). Errou: volta para a etapa 0.
 */
public class FlashcardService {

    // A posição no vetor é a etapa: INTERVALO_EM_DIAS[2] = 7 dias.
    private static final int[] INTERVALO_EM_DIAS = {1, 3, 7, 15};
    // Pública para a tela desenhar a barra de etapas sem repetir o número 3.
    public static final int ULTIMA_ETAPA = INTERVALO_EM_DIAS.length - 1;

    private final FlashcardDao flashcardDao;
    private final RevisaoDao revisaoDao;

    public FlashcardService(FlashcardDao flashcardDao, RevisaoDao revisaoDao) {
        this.flashcardDao = flashcardDao;
        this.revisaoDao = revisaoDao;
    }

    /**
     * Cadastra um flashcard numa disciplina. Frente e verso são obrigatórios.
     * O cartão nasce na etapa 0, com a primeira revisão amanhã (RF-02).
     *
     * @throws IllegalArgumentException se faltar disciplina, frente ou verso
     */
    public Flashcard cadastrar(Disciplina disciplina, String frente, String verso) {
        if (disciplina == null) {
            throw new IllegalArgumentException("Escolha uma disciplina para o flashcard.");
        }
        if (frente == null || frente.isBlank()) {
            throw new IllegalArgumentException("A frente do flashcard é obrigatória.");
        }
        if (verso == null || verso.isBlank()) {
            throw new IllegalArgumentException("O verso do flashcard é obrigatório.");
        }

        LocalDate primeiraRevisao = LocalDate.now().plusDays(intervaloEmDias(0));
        return flashcardDao.inserir(disciplina, frente.trim(), verso.trim(), 0, primeiraRevisao);
    }

    /** RF-03: flashcards de uma disciplina, na ordem em que foram cadastrados. */
    public List<Flashcard> listarPorDisciplina(Disciplina disciplina) {
        return flashcardDao.listarPorDisciplina(disciplina.getCodigo());
    }

    public Optional<Flashcard> buscarPorCodigo(int codigo) {
        return flashcardDao.buscarPorCodigo(codigo);
    }

    /** RF-04: cartões com revisão marcada para hoje ou para um dia que já passou. */
    public List<Flashcard> listarParaRevisarHoje() {
        return flashcardDao.listarParaRevisar(LocalDate.now());
    }

    /**
     * RF-05 e RF-06: registra a resposta do aluno e agenda a próxima revisão.
     *
     * @return a data da próxima revisão
     */
    public LocalDate registrarResposta(Flashcard flashcard, boolean acertou) {
        int novaEtapa = proximaEtapa(flashcard.getEtapa(), acertou);
        LocalDate novaData = LocalDate.now().plusDays(intervaloEmDias(novaEtapa));

        flashcard.reagendar(novaEtapa, novaData);
        flashcardDao.atualizarAgenda(flashcard);   // grava a nova agenda do cartão
        revisaoDao.inserir(flashcard.getCodigo(), acertou); // grava a resposta no histórico
        return novaData;
    }

    /** RF-06: histórico de um flashcard, da revisão mais recente para a mais antiga. */
    public List<Revisao> listarHistorico(Flashcard flashcard) {
        return revisaoDao.listarPorFlashcard(flashcard.getCodigo());
    }

    /**
     * RF-07: remove o flashcard e o histórico dele.
     * A confirmação (S/N) é pedida pela tela antes de chamar este método.
     */
    public void remover(Flashcard flashcard) {
        flashcardDao.remover(flashcard.getCodigo());
    }

    // ------------------------------------------------------------------
    // A regra das etapas fica em métodos separados ("static": não dependem
    // de nenhum objeto) para poder ser testada sozinha, sem banco (JUnit).
    // ------------------------------------------------------------------

    /** Acertou: sobe uma etapa, até a última. Errou: volta para a etapa 0. */
    public static int proximaEtapa(int etapaAtual, boolean acertou) {
        if (!acertou) {
            return 0;
        }
        return Math.min(etapaAtual + 1, ULTIMA_ETAPA);
    }

    /** Quantos dias até a próxima revisão para cada etapa (1, 3, 7 ou 15). */
    public static int intervaloEmDias(int etapa) {
        return INTERVALO_EM_DIAS[etapa];
    }
}
