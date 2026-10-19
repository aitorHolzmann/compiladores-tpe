#!/usr/bin/env bash
set -e

# Script de compilación y verificación de gramática BYacc/J
# Diseñado para Compiladores I (Grupo 20)

WORKSPACE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
cd "$WORKSPACE_DIR"

echo "=== [1/4] Generando Parser con BYacc/J ==="
byaccj -J -v gram.y

# Chequear conflictos en y.output
if grep -i "conflict" y.output | grep -v "0 shift/reduce conflicts, 0 reduce/reduce conflicts" > /dev/null 2>&1; then
    echo "⚠️ ADVERTENCIA: Se detectaron conflictos en y.output:"
    grep -i "conflict" y.output | head -n 5
else
    echo "✅ Gramática limpia: 0 conflictos reportados."
fi

echo "=== [2/4] Configurando paquetes Java ==="
sed -i '1s/^/package src.compilador;\n/' ParserVal.java
sed -i '1s/^/package src.compilador;\n/' Parser.java
mv Parser.java ParserVal.java src/compilador/

echo "=== [3/4] Compilando fuentes Java ==="
mkdir -p bin
javac -d bin $(find src -name "*.java")
echo "✅ Compilación exitosa."

if [ -n "$1" ]; then
    echo "=== [4/4] Ejecutando caso de prueba: $1 ==="
    java -cp bin src.compilador.Parser "$1"
fi

