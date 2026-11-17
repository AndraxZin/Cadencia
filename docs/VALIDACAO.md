# Cadência — Validação com usuários

Objetivo: conferir, com estudantes da turma (o público do Cadência), se o sistema cumpre os critérios de aceitação das histórias de usuário (seção 4 da [documentação](DOCUMENTACAO.md)).

## Como aplicar (cerca de 10 minutos por pessoa)

1. Abra o sistema com os dados de exemplo, para haver cartões a revisar hoje:
   `java -cp bin br.com.cadencia.Main --exemplo` (ou com o banco, conforme o README).
2. Peça autorização ao colega e explique que quem está sendo testado é o sistema, não ele.
3. Leia uma tarefa por vez. O colega usa o sistema sozinho, só com o que aparece na tela.
4. Observe e marque cada critério. Só ajude se ele travar, e anote que precisou de ajuda.
5. No fim, faça as 3 perguntas.

Para preservar a privacidade, registre os participantes como "Participante 1, 2, 3", sem nome completo.

## Tarefas e critérios

**T1 — Cadastrar um flashcard (US-01)**
"Cadastre a disciplina Engenharia de Software e um flashcard nela."
- [ ] O sistema pede uma disciplina existente antes do flashcard.
- [ ] Frente e verso vazios são recusados e pedidos de novo.
- [ ] Ao salvar, aparece "[OK] Flashcard cadastrado" com a data da primeira revisão (amanhã).

**T2 — Revisar os flashcards de hoje (US-02)**
"Revise os flashcards de hoje."
- [ ] Aparece um cartão por vez, só a frente.
- [ ] O verso só aparece depois do Enter.
- [ ] O sistema pergunta se acertou (S/N) e informa a próxima revisão.
- [ ] Revisando de novo, aparece "Nenhum flashcard para revisar hoje".

**T3 — Ver o histórico (US-03)**
"Veja o histórico do cartão 'O que é uma chave estrangeira?'."
- [ ] O histórico mostra data, hora e resultado em colunas alinhadas.
- [ ] A revisão mais recente aparece primeiro.

## Perguntas finais

1. De 1 a 5, quão fácil foi usar o sistema?
2. Você usaria o Cadência para estudar para as provas? (sim, não, talvez)
3. O que você mudaria?

## Resultados

Data da aplicação: ____/11/2026 · Aplicador: ______________

| Participante | T1 | T2 | T3 | Precisou de ajuda? | Facilidade (1 a 5) | Usaria? | Sugestão |
|---|---|---|---|---|---|---|---|
| Participante 1 | | | | | | | |
| Participante 2 | | | | | | | |
| Participante 3 | | | | | | | |

Em T1, T2 e T3, anote "cumpriu" quando todos os critérios da tarefa foram marcados, ou diga qual critério falhou.

**Conclusão:** (preencher depois da aplicação: os critérios foram cumpridos? que melhorias os colegas sugeriram?)
