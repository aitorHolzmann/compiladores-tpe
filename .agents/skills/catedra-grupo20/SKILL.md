---
name: catedra-grupo20
description: >-
  Especificación técnica completa y casos de prueba para los temas asignados al Grupo 20 (Temas 1, 7, 10, 12, 15, 17, 21, 22, 26, 29, 30, 34). Usar como referencia de sintaxis válida, tokens reservados, rangos numéricos y casos de prueba requeridos.
---

# Especificación Técnica Cátedra — Grupo 20

Este documento reúne todas las especificaciones, reglas gramaticales, palabras reservadas y restricciones que aplican al **Grupo 20** en los Trabajos Prácticos de Compiladores.

---

## 1. Tabla Resumen de Temas Asignados

| Tema | Nombre | Palabras Reservadas / Símbolos | Regla Sintáctica / Rango |
| :--- | :--- | :--- | :--- |
| **1** | Enteros cortos (8 bits) | `SHORTINT`, sufijo `$s` | Rango: `[-128, 127]`. Valor `128` solo permitido con signo `-` sintáctico. |
| **7** | Punto flotante (32 bits) | `SINGLEF`, exponente `s` | `1.17549435s-38 < x < 3.40282347s+38`, negativos o `0.0`. Ejemplos: `1.0`, `.6`, `-1.2`, `3.0s-5`, `.2s34`. |
| **10** | Cadenas multilínea | Delimitadas por `"` | Pueden abarcar múltiples líneas. En la Tabla de Símbolos se guardan sin saltos de línea. |
| **12** | Sentencia `repeat until` | `REPEAT`, `UNTIL` | `REPEAT <bloque> UNTIL ( <condicion> );` |
| **15** | Comentarios de 1 línea | `//` | Desde `//` hasta fin de línea. |
| **17** | Asignaciones en expresiones | Símbolo `=` | Asignación a términos con `=`. Expresión obligatoriamente delimitada por `( )` **sin anidamiento**. Ej: `a := b = (2$s);`. |
| **21** | Funciones `AUTO FUNCTION` | `AUTO`, `FUNCTION` | `AUTO FUNCTION id ( <params> ) BEGIN <cuerpo> END;`. Obligatorio al menos un `RET(...)`. **No permite** sentencias declarativas de función. |
| **22** | Variables en compilación | `COMPTIME` | `COMPTIME <tipo> <lista_identificadores>;` dentro de sentencias declarativas. |
| **26** | Import / Export en clases | `IMPORT`, `FROM`, `EXPORT`, `TO` | En clase: `IMPORT FROM <lista_id>`. En atributos/métodos: `EXPORT TO <lista_id>`. |
| **29** | Acceso posicional a atributos | `[` y `]` | `obj[0$s] := valor;` o `obj[variable] := valor;`. Solo índices enteros positivos o variables. |
| **30** | Herencia múltiple | `EXTENDS` | `EXTENDS <lista_identificadores>;` en el cuerpo de la clase. |
| **34** | Conversión explícita float a entero corto | `TOS` | `tos ( <expresion> )` en cualquier lugar donde sea válida una expresión. |

---

## 2. Detalles de Implementación y Restricciones Gramaticales

### Tema 17: Asignaciones de expresiones en expresiones
- **Símbolo**: Debe ser estrictamente `=` (no `:=`). Si el alumno escribe `a := b := (2$s);`, debe emitirse un error sintáctico.
- **Paréntesis obligatorios**: La expresión asignada debe estar encerrada entre paréntesis.
- **Sin anidamiento**: No se permite anidar asignaciones dentro de la expresión encerrada entre paréntesis (ej. `a := b = (c = (2$s));` es inválido).

### Tema 21: `AUTO FUNCTION`
- Sintaxis estándar:
  ```yacc
  AUTO FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')'
      BEGIN sentencias_ejecutables_con_retorno END ';'
  ```
- **Condición excluyente**: Debe contener al menos un retorno sintáctico (`RET(...)`). Si una función `AUTO` no tiene ningún `RET`, la gramática debe rechazarla o reportar el error sintáctico.
- **Sin sentencias declarativas**: A diferencia de una función ordinaria con tipo explícito, la especificación de la cátedra para `AUTO FUNCTION` omite el bloque de sentencias declarativas locales. Si se escriben variables declarativas, debe emitirse advertencia/error de gramática de error.

### Tema 29: Acceso posicional a atributos
- Formatos válidos:
  - `IDENTIFICADOR '[' CONSTANTE ']'` (donde la constante es un entero corto positivo).
  - `IDENTIFICADOR '[' IDENTIFICADOR ']'` (donde el identificador es una variable).
- Si se ingresa una constante de punto flotante o negativa dentro de los corchetes, la gramática de error o la acción semántica debe emitir error sintáctico.

### Tema 34: Conversión explícita `tos(...)`
- Se utiliza el token `TOS` seguido de una expresión entre paréntesis:
  ```yacc
  factor : TOS '(' expresion ')' ;
  ```
- Permite forzar el casteo de valores flotantes `singlef` a enteros cortos `shortint`.

---

## 3. Matriz de Casos de Prueba Mínimos para Entrega TP2

Para asegurar la aprobación del trabajo, el directorio `tests/` debe contener casos que ejerciten:
1. **Casos Válidos**:
   - Declaraciones completas (`shortint`, `singlef`, `comptime`).
   - Clases con `import from`, `export to`, `extends` múltiple y métodos.
   - Funciones ordinarias y funciones `AUTO FUNCTION` con retorno.
   - Expresiones con asignaciones del Tema 17: `x := y = (a + 1$s);`.
   - Iteraciones `repeat until` con bloque simple y bloque `begin end`.
   - Selecciones `if-else` y `if` sin rama else, ambos cerrando con `end_if;`.
   - Accesos posicionales a atributos con constantes y variables.
   - Conversiones explícitas `tos(...)` dentro de expresiones aritméticas.
   - Constantes negativas en límites (`-128$s`).
2. **Casos con Errores Sintácticos (deben recuperarse y continuar)**:
   - Omisión de `;` al final de una asignación.
   - Omisión de `END_IF;` en un `IF`.
   - Omisión de `UNTIL` en un `REPEAT`.
   - Uso de `:=` en lugar de `=` en asignación de expresión.
   - Función `AUTO FUNCTION` sin retorno `RET(...)`.
   - Constante `shortint` positiva mayor a 127 (ej. `128$s` positivo).
   - Acceso posicional con corchetes vacíos o constante flotante.

