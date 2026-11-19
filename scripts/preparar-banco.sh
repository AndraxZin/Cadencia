#!/usr/bin/env bash
# Prepara o PostgreSQL para o Cadência em Linux (GitHub Codespaces, Ubuntu, Debian).
#
# Uso:
#   bash scripts/preparar-banco.sh           primeira vez: instala, liga e cria o banco
#   bash scripts/preparar-banco.sh --zerar   apaga e recria o banco (antes de apresentar)
#
# A senha do banco é sorteada na primeira vez e fica em ~/.cadencia-env,
# FORA do repositório (RNF-07: a senha nunca vai para o GitHub).
set -euo pipefail
cd "$(dirname "$0")/.."

ARQUIVO_SENHA="$HOME/.cadencia-env"

# Roda um comando como o usuário "postgres" (o administrador do banco).
# O "cd /tmp" evita um aviso inofensivo sobre a pasta atual.
como_postgres() { (cd /tmp && sudo -u postgres "$@"); }

echo "[1/5] PostgreSQL instalado?"
if ! command -v psql >/dev/null 2>&1; then
  echo "      Não. Instalando (leva 1 ou 2 minutos)..."
  sudo apt-get update -qq
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -qq postgresql >/dev/null
fi

echo "[2/5] Ligando o servidor PostgreSQL..."
sudo service postgresql start >/dev/null

echo "[3/5] Criando o usuário 'cadencia' do banco..."
if [ ! -f "$ARQUIVO_SENHA" ]; then
  SENHA=$(head -c 16 /dev/urandom | od -An -tx1 | tr -d ' \n')
  printf 'export CADENCIA_DB_USER=cadencia\nexport CADENCIA_DB_PASSWORD=%s\n' "$SENHA" > "$ARQUIVO_SENHA"
  chmod 600 "$ARQUIVO_SENHA"
fi
# shellcheck disable=SC1090
source "$ARQUIVO_SENHA"
como_postgres psql -q -v ON_ERROR_STOP=1 <<SQL
DO \$\$ BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'cadencia') THEN
    CREATE ROLE cadencia LOGIN;
  END IF;
END \$\$;
ALTER ROLE cadencia PASSWORD '$CADENCIA_DB_PASSWORD';
SQL

echo "[4/5] Criando o banco 'cadencia' e as tabelas..."
if [ "${1:-}" = "--zerar" ]; then
  como_postgres dropdb --if-exists cadencia
  echo "      (banco antigo apagado)"
fi
if ! como_postgres psql -tAc "SELECT 1 FROM pg_database WHERE datname = 'cadencia'" | grep -q 1; then
  como_postgres createdb -O cadencia cadencia
  PGPASSWORD="$CADENCIA_DB_PASSWORD" psql -h localhost -U cadencia -d cadencia -q -f sql/schema.sql
elif PGPASSWORD="$CADENCIA_DB_PASSWORD" psql -h localhost -U cadencia -d cadencia -tAc "SELECT 1 FROM disciplina LIMIT 1" >/dev/null 2>&1; then
  echo "      (o banco já existia: nada foi apagado)"
else
  echo "      ATENÇÃO: o banco 'cadencia' já existe, mas o usuário cadencia não consegue usar as tabelas."
  echo "      Rode de novo com:  bash scripts/preparar-banco.sh --zerar"
fi

# Faz todo terminal novo já conhecer a senha.
grep -qs 'cadencia-env' "$HOME/.bashrc" || echo '[ -f ~/.cadencia-env ] && source ~/.cadencia-env' >> "$HOME/.bashrc"

echo "[5/5] Compilando o Cadência..."
javac -encoding UTF-8 -d bin -sourcepath src src/br/com/cadencia/Main.java

echo
echo "Pronto. Para abrir o sistema:  bash scripts/rodar.sh --exemplo"
