---
name: yacc-error-recovery
description: >-
  Metodología para la detección y recuperación de errores sintácticos en BYacc/J y Java según la teoría de la cátedra. Usar cuando se diseñen, modifiquen o depuren reglas de error en gram.y, garantizando la estrategia dual (producciones de gramática de error vs. token error con sincronización), cero conflictos y cobertura de los 20 errores obligatorios.
---

# Detección y Recuperación de Errores Sintácticos en BYacc/J

Esta skill documenta la metodología teórica y práctica para implementar el manejo de errores sintácticos en `gram.y`, garantizando que el compilador continúe su ejecución ante fallos y respetando la regla estricta de **cero conflictos shift-reduce y reduce-reduce**.

---

## 1. Fundamentos Teóricos de Recuperación de Errores (Cátedra)

De acuerdo con las filminas de la cátedra (`analisis_sintactico_teoria.md` y `analisis_sintactico_yacc.md`), cuando el analizador sintáctico encuentra una inconsistencia con la gramática, no debe detenerse bruscamente. Debe informar el error y recuperarse para seguir analizando el resto del archivo.

Se emplean dos estrategias principales:

### Estrategia 1: Producciones de Gramática de Error (Sin token `error`)
* **Concepto**: Se agregan producciones gramaticales que contemplan explícitamente errores comunes, desvíos o ausencias previsibles.
* **Comportamiento**: Para el parser, la construcción errónea es gramaticalmente válida, pero la acción semántica asociada emite el mensaje de error:
  ```yacc
  sentencia_declarativa
      : tipo lista_identificadores ';'
      | tipo lista_identificadores
          { yyerror("Falta ';' al final de la declaracion de variables"); }
      ;
  ```
* **Ventaja**: No altera la pila bruscamente ni descarta tokens. Es ideal para fallas puntuales (falta de punto y coma, ausencia de palabras clave como `until`, `end_if`, o uso de símbolos indebidos como `:=` en lugar de `=`).

### Estrategia 2: Modo Pánico con Token `error` y Sincronización
* **Concepto**: Se utiliza el pseudo-token `error` provisto por YACC junto con un token de sincronización (generalmente `;`, `BEGIN`, `END`, `END_IF`, `UNTIL` o `')'`).
* **Comportamiento**:
  1. Al toparse con un error, YACC desapila estados hasta encontrar uno que posea una transición válida para el token `error`.
  2. Si no hay token de sincronización inmediato, descarta tokens de la entrada (`yylex()`) hasta hallar el delimitador indicado en la regla.
  3. Reanuda el análisis normal a partir de ese punto.
* **Ejemplo**:
  ```yacc
  sentencia_ejecutable
      : asignacion ';'
      | error ';'
          { yyerror("Sentencia ejecutable malformada. Se descartan tokens hasta ';'"); }
      ;
  ```
* **Cuándo usar**: Para errores mayores, expresiones aritméticas completamente deformadas o bloques ininteligibles donde una regla de ausencia no alcanza.

---

## 2. Cuidado Extremo con los Conflictos (Regla de Oro)

> [!CAUTION]
> **Nunca agregar reglas con `error` indiscriminadamente**. Un token `error` mal ubicado puede competir con reglas válidas o causar conflictos shift-reduce y reduce-reduce masivos en `y.output`.

### Pautas para Prevenir Conflictos al Agregar Errores:
1. **Colocar `error` en niveles jerárquicos altos o bien delimitados**:
   - Es preferible poner `error ';'` al nivel de `sentencia_ejecutable` o `sentencia_declarativa` antes que poner `error` dentro de cada operador de `expresion` o `termino`.
2. **Evitar ambigüedad con tokens comunes**:
   - Si una regla termina en `';'`, asegurarse de que la regla de error consuma o espere explícitamente el `';'` para sincronizar.
3. **Validar siempre con `y.output`**:
   - Cada vez que se añade una regla de error, compilar con `byaccj -J -v gram.y` y verificar que la cabecera de `y.output` indique:
     `0 shift/reduce conflicts, 0 reduce/reduce conflicts`.

---

## 3. Catálogo de Errores Obligatorios y Estrategia Recomendada

A continuación se detalla cómo abordar cada uno de los 20 errores exigidos en `teoria/Errores a detectar.md`:

| Nro | Error Exigido | Técnica Recomendada | Producción / Enfoque Sugerido |
| :--- | :--- | :--- | :--- |
| **1** | Falta de nombre de programa | Gramática de error | `programa : error sentencias_declarativas BEGIN sentencias_ejecutables END { yyerror("Falta nombre de programa"); };` |
| **2** | Falta de delimitador `begin` o `end` en programa | Gramática de error | Contemplar `programa` sin `BEGIN` o sin `END`, emitiendo `yyerror("Falta delimitador BEGIN/END en el programa");`. |
| **3** | Falta de `;` al final de sentencias | Gramática de error | Agregar regla en `sentencia_ejecutable` o `declaracion_variables` sin `;` emitiendo `yyerror("Falta ';' al final de la sentencia");`. |
| **4** | Falta de nombre en función | Gramática de error | `tipo FUNCTION '(' lista_params ')' ... { yyerror("Falta el identificador de la funcion"); }` |
| **5** | Falta de `,` en lista de variables | Gramática de error | `lista_identificadores : lista_identificadores IDENTIFICADOR { yyerror("Falta ',' entre identificadores"); }` |
| **6** | Falta de nombre de parámetro formal | Gramática de error | `parametro_formal : tipo { yyerror("Falta nombre del parametro formal"); }` |
| **7** | Falta de tipo de parámetro formal | Gramática de error | `parametro_formal : IDENTIFICADOR { yyerror("Falta tipo del parametro formal"); }` |
| **8** | Falta de operando en expresión | Gramática de error / error | `expresion '+' error { yyerror("Falta operando derecho en la expresion"); }` |
| **9** | Falta de operador en expresión | Gramática de error | Contemplar factores contiguos sin operador intermedio emitiendo mensaje descriptivo. |
| **10** | Falta de argumento en `pout` | Gramática de error | `POUT '(' ')' { yyerror("Falta argumento en sentencia pout"); }` |
| **11** | Falta de `(` o `)` en condición | Gramática de error | En `sentencia_if` y `repeat_until`, contemplar `IF condicion` o `IF '(' condicion` emitiendo error de paréntesis. |
| **12** | Falta de cuerpo en iteraciones | Gramática de error | `REPEAT UNTIL '(' condicion ')' ';' { yyerror("Falta cuerpo de sentencias en la iteracion REPEAT"); }` |
| **13** | Falta de `END_IF` | Gramática de error | `IF '(' condicion ')' bloque ';' { yyerror("Falta 'END_IF' para cerrar la seleccion"); }` |
| **14** | Tema 12: Falta `UNTIL` | Gramática de error | `REPEAT bloque '(' condicion ')' ';' { yyerror("Falta la palabra clave UNTIL"); }` |
| **15** | Temas 17 y 18: Uso de `:=` donde debe ir `=` | Gramática de error | `unica : IDENTIFICADOR ASIGNAR '(' expresion ')' { yyerror("Debe utilizarse '=' para asignacion en expresion en lugar de ':='"); }` |
| **16** | Tema 21: Ausencia de retorno en función | Gramática sintáctica | En `AUTO FUNCTION`, definir que el bloque ejecutable deba contener obligatoriamente una producción derivada que garantice al menos un `RET(...)`. Si no lo contiene, emitir error sintáctico. |
| **17** | Tema 22: Falta de tipo en `comptime` | Gramática de error | `COMPTIME lista_identificadores ';' { yyerror("Falta el tipo de dato en la declaracion comptime"); }` |
| **18** | Tema 26: Ausencia de `to` o `from` | Gramática de error | Contemplar `IMPORT lista_id` (sin `FROM`) o `EXPORT lista_id` (sin `TO`), emitiendo el mensaje correspondiente. |
| **19** | Temas 30 a 32: Falta lista tras `extends` | Gramática de error | `EXTENDS ';' { yyerror("Falta lista de clases a heredar luego de 'extends'"); }` |
| **20** | Constantes fuera de rango y negativas | Acción Semántica en Yacc | En la reducción de constantes positivas o con signo `-`, parsear el lexema y chequear contra límites `ValorMaximoInt` (127), `ValorMinimoInt` (-128) y rangos float de `AnalizadorLexico`. |

---

## 4. Estructura de Salida de Errores

En el código de soporte de `gram.y`:
```java
void yyerror(String s) {
    System.out.println("Error sintactico (linea " + AnalizadorLexico.getLineaActual() + "): " + s);
}
```
Esto asegura el cumplimiento exacto del formato pedido en las filminas del Trabajo Práctico Nº 2.

