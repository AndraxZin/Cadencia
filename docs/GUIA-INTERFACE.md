# Guia da interface nova (para estudar antes da apresentação)

> Feito pelo Claude em 23/11/2026, numa exceção ao contrato (ver `DECISOES.md`). Leva uns 15 minutos para ler. No fim estão as 3 perguntas de banca.

## 1. O que mudou, em uma frase

Só a camada **view** mudou. As regras (service), o acesso ao banco (dao), as tabelas e os 20 testes ficaram iguais, e os testes continuaram passando. Na apresentação, isso vira argumento: "trocamos a tela inteira sem mexer em uma linha de regra ou de banco".

## 2. As três classes da view

| Classe | O que faz | Comparação de design |
|---|---|---|
| `MenuConsole` | Decide o que cada tela mostra e pergunta, e qual service chamar. | O fluxo, as telas do protótipo |
| `Tela` (nova) | Decide como tudo aparece: cores, cabeçalho, tabelas, barras e mensagens. | O design system: as cores e os estilos ficam num lugar só |
| `OpcaoMenuPrincipal` | O enum das opções: número, grupo (coluna), rótulo curto e descrição. | A arquitetura da informação do menu |

**Enum:** uma lista fechada de valores (aqui, as 9 opções do menu). Dentro dele há outro enum, `Grupo` (`ESTUDAR`, `DISCIPLINAS`, `FLASHCARDS` e `NENHUM`), que diz em que coluna cada opção aparece.

## 3. O caminho de uma opção, do número até a volta ao menu

```
iniciar()
 ├─ mostrarAbertura()        logotipo + [BANCO] Conectado + "Enter para começar"
 └─ repete:
     ├─ exibirMenu()         limpa, cabeçalho, painel do dia, opções em colunas
     ├─ lerInteiro()         lê o número digitado
     ├─ deCodigo(numero)     número → opção do enum (vazio se não existir)
     └─ executar(opcao)
          ├─ abrirTela(titulo)    limpa o terminal e mostra "CADÊNCIA · título"
          ├─ a ação da opção      ex.: revisarHoje(), que chama o FlashcardService
          └─ aguardarEnter()      "Pressione Enter para voltar ao menu..."
```

Era isso que faltava antes. A pausa no fim (`aguardarEnter`) é o que impede o menu de empurrar o resultado para cima.

## 4. Onde cada coisa da tela está no código

| Na tela | No código |
|---|---|
| "3 flashcards para revisar hoje." | `MenuConsole.mostrarPainelDoDia()` |
| Menu em três colunas | `mostrarOpcoes()` e `celula()` |
| Cabeçalho com ■ PostgreSQL | `abrirTela()` → `Tela.cabecalho()` |
| Tabelas alinhadas | `Tela.tabela()` |
| Barra da etapa `██░ 2` | `etapa()` → `Tela.barra()` |
| "em 3 dias, 26/11/2026" | `quando()` |
| "Acertou! Sobe da etapa 0 para a 1." | `explicarResultado()` |
| "Próxima revisão: amanhã (2 flashcards)." | `mostrarProximaRevisao()` |
| "Cadastrar outro nesta disciplina?" | `cadastrarFlashcardsEm()` |
| 0 para voltar | `lerCodigo()` e o campo `voltouAoMenu` |
| Erro de conexão em português | `Main.explicar()` |

## 5. Conceitos novos

- **Código ANSI:** uma sequência de texto que o terminal entende como um comando, e não como letras. `ESC[32m` quer dizer "pinte de verde", `ESC[0m` quer dizer "volte ao normal" e `ESC[2J` quer dizer "limpe a tela". `ESC` é um caractere invisível, escrito em Java como `"\u001B"`. Os códigos ficam como constantes no topo da `Tela`.
- **`--sem-cor`:** desliga os códigos ANSI. O `Main.terminalComCores()` também os desliga sozinho em dois casos:
  - quando `System.console()` devolve `null`, o que acontece quando não há um terminal de verdade (por exemplo, quando a entrada vem de um arquivo);
  - no Windows, fora do Windows Terminal, do VS Code e do IntelliJ. Ele descobre o terminal pelas variáveis de ambiente que esses programas criam, como `WT_SESSION`.
- **Expressão regular:** um molde de busca em texto. Em `Tela.larguraVisivel()`, o molde `\u001B\[[0-9;]*m` acha os códigos de cor para retirá-los antes de contar as letras. Sem isso, as colunas das tabelas sairiam tortas, porque os códigos ocupam espaço na `String` mas não aparecem na tela.
- **`StringBuilder`:** um texto que vai crescendo aos poucos (`append`). É usado para montar uma linha da tabela ou do menu pedaço por pedaço.
- **`ChronoUnit.DAYS.between(hoje, data)`:** conta quantos dias há entre duas datas. É assim que "30/11" vira "em 7 dias".
- **`DateTimeFormatter` com `Locale` pt-BR:** o formato `"EEEE"` escreve o dia da semana por extenso, em português ("segunda-feira").
- **SQLState:** um código de 5 caracteres que o banco devolve junto com cada erro. Ele é padrão, então não depende do idioma da mensagem. Exemplos: `28P01` é senha errada, `3D000` é banco que não existe e `08001` é servidor que não respondeu. O `Main.explicar()` usa um `switch` sobre esse código para escrever o motivo em português.
- **`-Dstdout.encoding=UTF-8`:** uma configuração passada ao Java na hora de rodar (está no `scripts/rodar.sh`). Sem ela, um Linux sem idioma configurado mostraria os acentos como "?".

## 6. Onde a regra NÃO está (resposta pronta para o professor)

A barra `██░`, o "em 3 dias" e a porcentagem de acerto são **apresentação**: a tela só conta e formata o que o service devolveu. A regra continua num lugar só, no `FlashcardService`:
- o acerto sobe uma etapa;
- o erro volta para a 0;
- os intervalos são 1, 3, 7 e 15 dias.

Prova disso: os testes `T01` a `T09` testam essa regra e passaram sem nenhuma mudança.

A única mudança fora da view foi tornar pública a constante `FlashcardService.ULTIMA_ETAPA`. Assim, a tela desenha a barra com 3 posições sem repetir o número 3: se um dia a regra mudar, a barra muda junto.

## 7. O que fazer antes das 19h

1. **No seu PC:** `git pull`. Depois compile e rode com `--exemplo`, como está no README.
2. Cadastre uma disciplina com acento, por exemplo "Programação", e confira se ela aparece certa na listagem.
3. Faça o roteiro da demonstração uma vez. As falas estão nas notas do slide "Demonstração".
4. **No Codespace:** antes de apresentar, rode `bash scripts/preparar-banco.sh --zerar` e depois `bash scripts/rodar.sh --exemplo`, com o painel do terminal maximizado.
5. Se o terminal mostrar `←[32m` no lugar das cores, rode de novo com `--sem-cor`.

## 8. Perguntas de banca

1. Vocês redesenharam todas as telas. Como garantem que a regra da revisão continuou a mesma?
2. Por que existe uma classe `Tela` separada do `MenuConsole`? Não seria mais simples deixar tudo junto?
3. O que acontece se o computador da apresentação não mostrar cores no terminal?
