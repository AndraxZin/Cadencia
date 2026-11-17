package br.com.cadencia.service;

import br.com.cadencia.dao.DisciplinaDaoMemoria;
import br.com.cadencia.dao.FlashcardDaoMemoria;
import br.com.cadencia.dao.RevisaoDaoMemoria;
import br.com.cadencia.model.Disciplina;
import br.com.cadencia.model.Flashcard;
import br.com.cadencia.model.Revisao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes UNITÁRIOS das regras de flashcard e de revisão (RF-02, RF-04, RF-05 e RF-06).
 *
 * Usam os DAOs em memória: rodam sem banco e em milissegundos.
 * Cada método com @Test é um teste. O @BeforeEach roda antes de CADA teste,
 * então todo teste começa com um cenário novo e limpo.
 * assertEquals(esperado, obtido) falha o teste se os dois forem diferentes.
 */
class FlashcardServiceTest {

    private FlashcardService service;
    private Disciplina banco;

    @BeforeEach
    void prepararCenario() {
        RevisaoDaoMemoria revisaoDao = new RevisaoDaoMemoria();
        FlashcardDaoMemoria flashcardDao = new FlashcardDaoMemoria(revisaoDao);
        DisciplinaDaoMemoria disciplinaDao = new DisciplinaDaoMemoria(flashcardDao);
        service = new FlashcardService(flashcardDao, revisaoDao);
        banco = new DisciplinaService(disciplinaDao).cadastrar("Banco de Dados", "");
    }

    // ------------------------------------------------ regra das etapas (RF-05)

    @Test
    @DisplayName("Acerto na etapa 0 vai para a etapa 1")
    void acertoNaEtapa0VaiParaEtapa1() {
        assertEquals(1, FlashcardService.proximaEtapa(0, true));
    }

    @Test
    @DisplayName("Acerto na etapa 3 continua na etapa 3")
    void acertoNaUltimaEtapaContinuaNela() {
        assertEquals(3, FlashcardService.proximaEtapa(3, true));
    }

    // >>> Caio: escreva aqui o teste "Erro na etapa 2 volta para a etapa 0",
    //     seguindo o modelo do primeiro teste acima.

    @Test
    @DisplayName("Intervalos das etapas: 1, 3, 7 e 15 dias")
    void intervalosDasEtapas() {
        assertEquals(1, FlashcardService.intervaloEmDias(0));
        assertEquals(3, FlashcardService.intervaloEmDias(1));
        assertEquals(7, FlashcardService.intervaloEmDias(2));
        assertEquals(15, FlashcardService.intervaloEmDias(3));
    }

    // ------------------------------------------------------- cadastro (RF-02)

    @Test
    @DisplayName("Flashcard novo nasce na etapa 0, com revisão amanhã")
    void flashcardNovoNasceNaEtapa0ComRevisaoAmanha() {
        Flashcard novo = service.cadastrar(banco, "O que é chave primária?", "Identifica cada linha.");
        assertEquals(0, novo.getEtapa());
        assertEquals(LocalDate.now().plusDays(1), novo.getProximaRevisao());
    }

    @Test
    @DisplayName("Frente vazia é recusada")
    void frenteVaziaERecusada() {
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(banco, "   ", "Resposta"));
    }

    // ------------------------------------------------- revisão do dia (RF-04)

    @Test
    @DisplayName("Cartão novo não entra na revisão de hoje")
    void cartaoNovoNaoEntraNaRevisaoDeHoje() {
        service.cadastrar(banco, "Pergunta", "Resposta");
        assertTrue(service.listarParaRevisarHoje().isEmpty());
    }

    // ------------------------------------- registrar a resposta (RF-05 e RF-06)

    @Test
    @DisplayName("Acertar: sobe uma etapa, agenda em 3 dias e grava no histórico")
    void acertarSobeEtapaEGravaNoHistorico() {
        Flashcard cartao = service.cadastrar(banco, "Pergunta", "Resposta");

        LocalDate proxima = service.registrarResposta(cartao, true);

        assertEquals(1, cartao.getEtapa());
        assertEquals(LocalDate.now().plusDays(3), proxima);
        List<Revisao> historico = service.listarHistorico(cartao);
        assertEquals(1, historico.size());
        assertTrue(historico.get(0).isAcertou());
    }

    @Test
    @DisplayName("Errar: volta para a etapa 0 e agenda para amanhã")
    void errarVoltaParaEtapa0EAgendaParaAmanha() {
        Flashcard cartao = service.cadastrar(banco, "Pergunta", "Resposta");
        service.registrarResposta(cartao, true); // agora está na etapa 1

        LocalDate proxima = service.registrarResposta(cartao, false);

        assertEquals(0, cartao.getEtapa());
        assertEquals(LocalDate.now().plusDays(1), proxima);
        assertEquals(2, service.listarHistorico(cartao).size());
    }

    @Test
    @DisplayName("Histórico vem da revisão mais recente para a mais antiga")
    void historicoDoMaisRecenteParaOMaisAntigo() {
        Flashcard cartao = service.cadastrar(banco, "Pergunta", "Resposta");
        service.registrarResposta(cartao, true);
        service.registrarResposta(cartao, false);

        List<Revisao> historico = service.listarHistorico(cartao);

        assertFalse(historico.get(0).isAcertou()); // o erro foi a última resposta
        assertTrue(historico.get(1).isAcertou());
    }
}
