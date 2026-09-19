---
name: byaccj-workflow
description: >-
  Procedimiento de compilación con BYacc/J, inyección de packages, inspección de y.output y resolución de conflictos shift-reduce y reduce-reduce. Usar cuando se modifique gram.y, se compile el proyecto o se depuren conflictos en la gramática.
---

# Workflow de BYacc/J y Resolución de Conflictos

Esta skill describe el procedimiento estándar para generar, compilar y probar el analizador sintáctico en Java utilizando BYacc/J, así como la metodología para analizar `y.output` y erradicar cualquier conflicto shift-reduce o reduce-reduce.

---

## 1. Ciclo de Construcción (Build Pipeline)

Dado que `byaccj` genera los archivos `Parser.java` y `ParserVal.java` en el directorio actual sin incluir la cláusula de paquete requerida por Java, se debe seguir la siguiente secuencia:

### Comandos Manuales
```bash
# 1. Ejecutar BYacc/J con generación en Java (-J) y reporte de estados (-v)
byaccj -J -v gram.y

# 2. Inyectar la declaración del package src.compilador en ambos archivos
sed -i '1s/^/package src.compilador;\n/' ParserVal.java
sed -i '1s/^/package src.compilador;\n/' Parser.java

# 3. Mover los archivos generados a su paquete de destino
mv Parser.java ParserVal.java src/compilador/

# 4. Compilar todos los fuentes Java
javac -d bin $(find src -name "*.java")

# 5. Ejecutar sobre un archivo de prueba
java -cp bin src.compilador.Parser tests/prueba_gramatica
```

### Script Automatizado
Se dispone de un script que ejecuta todos estos pasos y verifica automáticamente la existencia de conflictos en `y.output`:
```bash
bash .agents/skills/byaccj-workflow/scripts/build_parser.sh [archivo_de_prueba]
```

---

## 2. Diagnóstico y Solución de Conflictos en `y.output`

La cátedra prohíbe terminantemente entregar un compilador con conflictos:
> **"Eliminar TODOS LOS CONFLICTOS SHIFT-REDUCE Y REDUCE-REDUCE que se presenten al generar el Parser."**

### Tipos de Conflictos

1. **Shift-Reduce**:
   - Ocurre cuando el autómata, viendo el próximo token en la entrada (lookahead), puede decidir entre apilar ese token (`shift`) o reducir por una regla gramatical (`reduce`).
   - *Ejemplo típico*: La ambigüedad del `IF-ELSE` colgante (dangling else) o la precedencia de operadores aritméticos (`+` vs `*`).
2. **Reduce-Reduce**:
   - Ocurre cuando dos reglas de producción distintas tienen el mismo lado derecho y el autómata no puede decidir por cuál de las dos reducir con el mismo token de lookahead.
   - *Ejemplo típico*: Dos no terminales que derivan en `/* vacio */` o reglas de error redundantes que compiten entre sí.

### Metodología de Resolución Paso a Paso

1. **Abrir `y.output`**:
   - En las primeras líneas del archivo se informa si hay conflictos. Ejemplo:
     `5 shift/reduce conflicts, 0 reduce/reduce conflicts.`
2. **Buscar la palabra "conflict"**:
   - En `y.output`, buscar `/reduce` o `conflict` para hallar el número de estado problemático.
   - Inspeccionar qué reglas coinciden en ese estado y qué token desencadena la ambigüedad.
3. **Aplicar la Solución Correcta**:
   - **Precedencia y Asociatividad (`%left`, `%right`, `%nonassoc`)**:
     * Para operadores matemáticos y lógicos:
       ```yacc
       %left '+' '-'
       %left '*' '/'
       ```
     * Para desambiguar el `IF-ELSE`: BYacc/J por defecto resuelve shift/reduce a favor del shift (asocia el `ELSE` al `IF` más interno), pero si la cátedra exige cero advertencias, se puede desambiguar explícitamente mediante una pseudo-precedencia o reescribiendo la gramática en sentencias emparejadas y no emparejadas.
   - **Reescritura de la Gramática**:
     * Si el conflicto proviene de reglas de error con tokens comunes o reglas vacías ambiguas, reestructurar la producción para que el punto de decisión se postergue o sea unívoco.

