package br.com.cadencia;

import br.com.cadencia.service.DisciplinaService;
import br.com.cadencia.service.FlashcardService;
import br.com.cadencia.view.MenuConsole;

/**
 * Ponto de entrada do programa. Só monta as peças e inicia o menu.
 *
 * Quem cria os services aqui (e os entrega à view) é o Main: assim a view não
 * precisa saber COMO os dados são guardados, só chama os services.
 */
public class Main {

    public static void main(String[] args) {
        DisciplinaService disciplinaService = new DisciplinaService();
        FlashcardService flashcardService = new FlashcardService();

        MenuConsole menu = new MenuConsole(disciplinaService, flashcardService);
        menu.iniciar();
    }
}
