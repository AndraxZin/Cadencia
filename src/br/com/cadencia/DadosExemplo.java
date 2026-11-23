package br.com.cadencia;

import br.com.cadencia.dao.DisciplinaDao;
import br.com.cadencia.dao.FlashcardDao;
import br.com.cadencia.dao.RevisaoDao;
import br.com.cadencia.model.Disciplina;
import br.com.cadencia.model.Flashcard;

import java.time.LocalDate;

/**
 * Dados de exemplo para demonstrar o sistema (java ... Main --exemplo).
 *
 * Cria 2 disciplinas e 5 flashcards; 3 deles já vencem hoje, para a opção
 * "Revisar hoje" ter o que mostrar (um cartão novo só vence amanhã).
 * Só carrega se ainda não houver nenhuma disciplina. Devolve uma frase dizendo
 * o que aconteceu, que o Main mostra na tela de abertura.
 *
 * Usa os DAOs direto, sem passar pelo service, porque precisa criar cartões
 * já "vencidos", algo que a regra normal de cadastro não permite.
 */
public final class DadosExemplo {

    private DadosExemplo() {
        // Só tem método estático.
    }

    public static String carregar(DisciplinaDao disciplinaDao, FlashcardDao flashcardDao, RevisaoDao revisaoDao) {
        if (!disciplinaDao.listar().isEmpty()) {
            return "[EXEMPLO] Já existem disciplinas: os dados de exemplo não foram carregados.";
        }

        LocalDate hoje = LocalDate.now();
        Disciplina banco = disciplinaDao.inserir("Banco de Dados", "");
        Disciplina poo = disciplinaDao.inserir("Programação Orientada a Objetos", "");

        flashcardDao.inserir(banco, "O que é uma chave primária?",
                "A coluna que identifica cada linha da tabela, sem repetir.", 0, hoje);
        Flashcard chaveEstrangeira = flashcardDao.inserir(banco, "O que é uma chave estrangeira?",
                "A coluna que aponta para a chave primária de outra tabela.", 1, hoje);
        revisaoDao.inserir(chaveEstrangeira.getCodigo(), true); // um acerto no histórico
        flashcardDao.inserir(poo, "O que é um objeto?",
                "Uma instância de uma classe, criada com new.", 2, hoje);
        flashcardDao.inserir(poo, "O que é uma interface?",
                "Uma lista de métodos prometidos, sem o corpo.", 0, hoje.plusDays(1));
        flashcardDao.inserir(banco, "O que diz a 3FN?",
                "Nenhum atributo comum depende de outro atributo comum.", 3, hoje.plusDays(15));

        return "[EXEMPLO] Carregados 2 disciplinas e 5 flashcards (3 para revisar hoje).";
    }
}
