#!/usr/bin/env bash
# Abre o Cadência em Linux (GitHub Codespaces, Ubuntu, Debian), usando o PostgreSQL.
#
# Uso:
#   bash scripts/rodar.sh             abre o sistema
#   bash scripts/rodar.sh --exemplo   abre com os dados de exemplo (se o banco estiver vazio)
#   bash scripts/rodar.sh --sem-cor   abre sem cores (para terminais que mostram códigos estranhos)
#
# Antes, uma vez só: bash scripts/preparar-banco.sh
set -euo pipefail
cd "$(dirname "$0")/.."

# Senha e usuário do banco, criados pelo preparar-banco.sh (fora do repositório).
# shellcheck disable=SC1090
[ -f "$HOME/.cadencia-env" ] && source "$HOME/.cadencia-env"

# Liga o servidor, caso esteja desligado. O "-n" não deixa o sudo pedir senha.
sudo -n service postgresql start >/dev/null 2>&1 || true

javac -encoding UTF-8 -d bin -sourcepath src src/br/com/cadencia/Main.java

# -Dstdout.encoding=UTF-8: num Linux sem idioma configurado, o Java escreveria os
# acentos e as letras do logotipo como "?". Com UTF-8, tela e teclado se entendem.
java -Dstdout.encoding=UTF-8 -cp "bin:lib/postgresql-42.7.4.jar" br.com.cadencia.Main "$@"
