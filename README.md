# Cadência

**Plataforma de estudo ativo por repetição espaçada, para quem estuda à noite e trabalha de dia.**

![Java 21](https://img.shields.io/badge/Java-21-C74634?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Licença](https://img.shields.io/badge/uso-acadêmico-6B7280?style=flat-square)

---

## De onde vem o nome

Em música, **cadência** é a sequência que resolve uma frase — o ponto onde a tensão
fecha e o trecho encontra seu lugar. No uso comum, é o ritmo constante de quem mantém
o passo.

Este projeto não é sobre memorizar mais. É sobre **quando voltar**.

A repetição espaçada parte de uma constatação incômoda: reler o mesmo conteúdo cinco
vezes numa noite produz menos retenção do que revê-lo cinco vezes ao longo de um mês.
O que fixa não é o esforço concentrado — é o **intervalo**. O algoritmo SM-2 calcula
esse intervalo para cada material, a partir de quanto você acertou da última vez.

O sistema define o ritmo. Daí o nome.

---

## O problema

O estudante noturno assiste cerca de **15 horas de aula por semana** e chega em casa
sem tempo nem método para revisar. A revisão acaba acontecendo na véspera da prova, de
forma concentrada e passiva — o que produz retenção baixa justamente nas disciplinas
cumulativas, onde o conteúdo da semana 3 é pré-requisito do da semana 10.

O Cadência ataca isso em três movimentos:

| | |
|---|---|
| **Fatiar** | O conteúdo de cada aula vira unidades de estudo ativo: flashcards, questões e resumos |
| **Agendar** | Cada unidade recebe uma data de revisão calculada pelo SM-2, conforme seu desempenho |
| **Priorizar** | A sessão diária monta uma fila em que o que você mais erra aparece primeiro |

O resultado é uma fila curta e específica por dia, em vez de uma pilha intimidante na
véspera.

---

## Estado do projeto

Desenvolvimento em sprints. O que está marcado é o que existe no repositório — não o
que está planejado.

| Sprint | Entrega | Estado |
|---|---|---|
| 0 — Fundação | Repositório, documentação, esquema do banco | 🟡 em andamento |
| 1 — Domínio | Classes do modelo em Java puro, com testes | ⬜ |
| 2 — Persistência | Migrations Flyway, entidades JPA, repositórios | ⬜ |
| 3 — Regra de negócio | Motor SM-2, serviço de sessão, fila priorizada | ⬜ |
| 4 — Camada web | Controllers, rotas, formulários | ⬜ |
| 5 — Interface | Thymeleaf + Tailwind CSS | ⬜ |
| 6 — Fechamento | Seed, diagramas, relatório | ⬜ |
| 7 — Fase 2 | Tokens de acesso para agentes de IA | ⬜ |

**Pronto até aqui:** o esquema completo do banco, validado contra PostgreSQL 16; dados
de demonstração; o diagrama entidade-relacionamento; e o charter com requisitos e
modelo de domínio.

**Ainda não existe:** código Java. O Sprint 1 é o próximo.

---

## Arquitetura

```
br.edu.unieuro.cadencia
  ├── dominio       ← POO pura. Sem Spring, sem JPA, sem anotação de framework.
  ├── repositorio   ← interfaces de persistência
  ├── servico       ← regras de negócio e orquestração
  ├── web           ← controllers
  └── config
```

**O pacote `dominio` não importa nada de framework.** É uma regra deliberada: é ali
que herança e polimorfismo precisam estar visíveis, sem anotações de infraestrutura
no meio. O custo assumido é alguma duplicação entre entidades de domínio e de
persistência — documentado em [`docs/DECISOES.md`](docs/DECISOES.md).

---

## Modelo de dados

Dez tabelas. A herança de `MaterialEstudo` é mapeada pela estratégia **Joined Table**
— tabela pai com os campos comuns, uma tabela por subtipo com os campos próprios.

```
usuario ──< disciplina ──< aula ──< material_estudo
                                          │
                            ┌─────────────┼─────────────┐
                        flashcard      resumo       questao

material_estudo ──── revisao ──< sessao_revisao >── sessao_estudo
```

Diagrama completo, cardinalidades e rastreabilidade requisito→tabela em
[`docs/MODELO-ER.md`](docs/MODELO-ER.md).

### Rodando o banco

Pré-requisito: PostgreSQL 16.

```bash
createdb cadencia
psql -d cadencia -f db/migration/V1__esquema_inicial.sql
psql -d cadencia -f db/seed.sql          # opcional: dados de demonstração
```

No **pgAdmin 4**: crie o banco `cadencia`, abra *Tools → Query Tool*, cole cada arquivo
e execute com F5 — primeiro o esquema, depois o seed. O seed pode ser reexecutado
quantas vezes quiser.

---

## Documentação

| Documento | Conteúdo |
|---|---|
| [`docs/CHARTER.md`](docs/CHARTER.md) | Requisitos, objetivos, modelo de domínio e trilha de sprints |
| [`docs/MODELO-ER.md`](docs/MODELO-ER.md) | Diagrama entidade-relacionamento e leitura das cardinalidades |
| [`docs/DECISOES.md`](docs/DECISOES.md) | Registro de decisões técnicas, com alternativas e custos |
| [`CLAUDE.md`](CLAUDE.md) | Contrato de trabalho do projeto |

---

## Contexto acadêmico

Projeto Integrador de Sistemas Computacionais — 4º semestre de Análise e
Desenvolvimento de Sistemas, Centro Universitário Unieuro, 2026.2.
Orientação: Prof. Henrique Dominges Garcia.

Autor: **Caio Andrax Portela Carvalho** — RA 093793

A disciplina avalia demonstração de Programação Orientada a Objetos e modelagem
relacional. As decisões deste repositório priorizam clareza conceitual sobre
quantidade de funcionalidades — um domínio pequeno e bem modelado vale mais que um
sistema grande e confuso.
