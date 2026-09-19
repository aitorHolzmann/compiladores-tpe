# Informe de Detección y Recuperación de Errores Sintácticos
**Materia:** Diseño de Compiladores I / Compiladores e Intérpretes  
**Grupo:** 20  
**Herramienta:** BYacc/J (`byaccj`)  
**Lenguaje de Implementación:** Java 21  

---

## 1. Fundamentos Teóricos y Criterios de Diseño

El analizador sintáctico tiene como objetivo no solo validar frases gramaticales correctas, sino también **detectar anomalías sintácticas, emitir mensajes descriptivos con el número de línea real y recuperarse** para continuar compilando el resto del archivo fuente sin abortar.

Para cumplir con la exigencia taxativa de la cátedra de **cero conflictos shift-reduce y reduce-reduce**, se aplican dos técnicas complementarias según la naturaleza del error:

* **Técnica A (Gramática de Error sin token `error`)**: Se modelan producciones gramaticales que contemplan desvíos predecibles (omisión de delimitadores de cierre, falta de palabras clave terminales). La reducción ejecuta la acción semántica que reporta el error sin desapilar estados ni descartar tokens.
* **Técnica B (Modo Pánico con pseudo-token `error` y Sincronización)**: Se utiliza en puntos estructurales de bifurcación o inicio donde una omisión no puede resolverse con 1 solo token de lookahead ($k=1$). El autómata entra en estado de recuperación, desapila hasta el manejador y sincroniza con el siguiente bloque sintáctico seguro.

### Formato Estándar de Reporte
Todos los errores sintácticos se emiten con el siguiente formato unificado:
```text
Error sintactico (linea X): <descripcion_clara_del_error>
```

---

## 2. Registro Detallado de Errores Contemplados

---

### Error 1: Falta de nombre de programa

#### Enunciado de la Cátedra
> "1. Falta de nombre de programa." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{programa} \rightarrow \text{IDENTIFICADOR } \text{sentencias\_declarativas } \mathbf{BEGIN} \text{ sentencias\_ejecutables } \mathbf{END}$$

#### Regla Implementada en `gram.y`
```yacc
programa
    : IDENTIFICADOR sentencias_declarativas BEGIN sentencias_ejecutables END
        { 
            if (cant_errores == 0) {
                System.out.println("Programa reconocido correctamente"); 
            } else {
                System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
            }
        }
    | error { yyerror("Falta el nombre del programa al inicio"); } sentencias_declarativas BEGIN sentencias_ejecutables END
        { 
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
        }
    ;
```

#### Justificación Técnica y Defensa
1. **¿Por qué no se utilizó Técnica A pura (`sentencias_declarativas BEGIN ...`)?**:
   - `sentencias_declarativas` puede derivar en vacío (`/* vacio */`) o comenzar con una declaración de objeto (`declaracion_objeto : IDENTIFICADOR lista_identificadores ';'`).
   - Si se omitiera el token `error` y se permitiera que el programa comience directamente con `sentencias_declarativas`, ante un token `IDENTIFICADOR` en el estado inicial, el parser no podría discernir con $k=1$ si dicho identificador es el nombre del programa o el tipo de una declaración de objeto, generando conflictos de ambigüedad.
2. **Acción Semántica Intermedia Temprana**:
   - Al colocar `{ yyerror("Falta el nombre del programa al inicio"); }` inmediatamente después de `error`, la acción se ejecuta ni bien el parser detecta la anomalía en el estado inicial (Línea 1).
   - Si la acción se ubicara al final de la producción, se dispararía recién al alcanzar el fin de archivo ($end$), reportando erróneamente la última línea del archivo en vez de la línea 1.
3. **Comportamiento en la Pila**:
   - El analizador recibe el primer token (ej: `SHORTINT` o `BEGIN`), constata que el estado 0 no tiene transición válida para ese terminal y desplaza a través de la transición `error`. La pila queda en el estado de sentencias declarativas, procesando normalmente el resto del programa sin descarte destructivo de tokens.

#### Casos de Prueba Verificados
* `tests/test_con_nombre.txt`: Programa válido $\rightarrow$ `Programa reconocido correctamente`.
* `tests/test_sin_nombre.txt`: Inicia con declaraciones sin nombre $\rightarrow$ Detecta error en Línea 1 y compila declaraciones.
* `tests/test_sin_declarativas_sin_nombre.txt`: Inicia directamente con `BEGIN` $\rightarrow$ Detecta error en Línea 1 y compila ejecutables.

---

### Error 2: Falta de delimitador de sentencias ejecutables (`begin` o `end`)

#### Enunciado de la Cátedra
> "2. Falta de delimitador de sentencias ejecutables (begin o end)." (`teoria/Errores a detectar.md`)

#### Contextos de Aparición en la Gramática
El delimitador de sentencias ejecutables interviene en dos niveles:
1. **A nivel Programa Principal (`programa`)**.
2. **A nivel Bloque de Control (`bloque`)** dentro de sentencias como `IF` y `REPEAT`.

#### Reglas Implementadas en `gram.y`

**En `programa`:**
```yacc
programa
    : IDENTIFICADOR sentencias_declarativas BEGIN sentencias_ejecutables END
        { /* ... */ }
    | error { yyerror("Falta el nombre del programa al inicio"); } sentencias_declarativas BEGIN sentencias_ejecutables END
        { /* ... */ }
    | IDENTIFICADOR sentencias_declarativas error { yyerror("Falta el delimitador BEGIN de sentencias ejecutables"); } sentencias_ejecutables END
        {
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
        }
    | IDENTIFICADOR sentencias_declarativas BEGIN sentencias_ejecutables
        {
            yyerror("Falta el delimitador END al final del programa");
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s)");
        }
    ;
```

**En `bloque`:**
```yacc
bloque
    : BEGIN sentencias_ejecutables END
    | sentencia_ejecutable
    | BEGIN sentencias_ejecutables
        { yyerror("Falta el delimitador END en el bloque de sentencias"); }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Falta de `BEGIN` en `programa`:
* **Problema de diseño evitado**: Intentar modelar `IDENTIFICADOR sentencias_declarativas sentencias_ejecutables END` sin token `error` produce `1 shift/reduce conflict on IDENTIFICADOR`. Esto se debe a que tanto una declaración de objeto como una sentencia ejecutable (asignación, llamada a método) inician con `IDENTIFICADOR`.
* **Solución**: Se utiliza el token de recuperación `error` con acción semántica temprana. Cuando finalizan las declarativas y no aparece `BEGIN`, el autómata dispara el error y sincroniza con el bloque de `sentencias_ejecutables`.

##### 2. Falta de `END` en `programa`:
* Se implementa mediante **Técnica A** pura: `IDENTIFICADOR sentencias_declarativas BEGIN sentencias_ejecutables`.
* Al alcanzar el fin de archivo (`$end`), el autómata no encuentra el token terminal `END`. Como `$end` y `END` son mutuamente excluyentes, el parser reduce directamente por esta producción de error reportando la línea final del archivo con 0 conflictos.

##### 3. Falta de `END` en `bloque`:
* Se implementa mediante **Técnica A** pura: `BEGIN sentencias_ejecutables`.
* Cuando un bloque se abre con `BEGIN`, pero el programador omite `END` antes de las palabras de cierre de estructura (`END_IF`, `ELSE` o `UNTIL`), el lookahead detecta dichos delimitadores y reduce la regla emitiendo el mensaje correspondiente sin entrar en pánico.

##### 4. ¿Por qué NO se contempla la falta de `BEGIN` en `bloque`? (Pregunta Típica de Examen)
Existen dos fundamentos determinantes:
1. **Definición Semántica del Lenguaje**:
   Por diseño de la cátedra, la producción canónica es:
   $$\text{bloque} \rightarrow \mathbf{BEGIN} \text{ sentencias\_ejecutables } \mathbf{END} \mid \text{sentencia\_ejecutable}$$
   Una sentencia ejecutable individual (ej: `IF (cond) x := 1$s; END_IF;`) **no requiere `BEGIN`**. Para el compilador, una sentencia aislada sin `BEGIN` ni `END` es gramaticalmente válida y no constituye un error.
2. **Limitación Teórica del Modelo LALR(1) (Imposibilidad Matemática)**:
   Si intentáramos agregar una regla para capturar un bloque que omitió `BEGIN` pero puso `END`:
   ```yacc
   bloque : sentencias_ejecutables END { yyerror("Falta BEGIN..."); }  /* ⚠️ ERROR FATAL */
   ```
   BYacc/J reporta inmediatamente **24 conflictos shift-reduce**.
   *Causa:* `sentencias_ejecutables` puede ser vacía (`/* vacio */`). Ante cualquier token inicial de sentencia (`IDENTIFICADOR`, `IF`, `POUT`, etc.), el autómata con lookahead de 1 símbolo ($k=1$) no puede determinar si ese token conforma una `sentencia_ejecutable` simple (que no espera `END`) o el inicio de una secuencia dentro de `sentencias_ejecutables END`. El token `END` se encuentra a múltiples tokens de distancia en el flujo de entrada, inaccesible para un analizador LALR(1).

#### Casos de Prueba Verificados
* `tests/test_sin_begin_prog.txt`: Falta `BEGIN` en programa $\rightarrow$ Detecta en Línea 4 `Falta el delimitador BEGIN de sentencias ejecutables`.
* `tests/test_sin_end_prog.txt`: Falta `END` en programa $\rightarrow$ Detecta en Línea 6 `Falta el delimitador END al final del programa`.
* `tests/test_sin_end_bloque.txt`: Falta `END` en bloque de `IF` $\rightarrow$ Detecta en Línea 8 `Falta el delimitador END en el bloque de sentencias`.
* `tests/test_sin_end_repeat.txt`: Falta `END` en bloque de `REPEAT` $\rightarrow$ Detecta en Línea 8 `Falta el delimitador END en el bloque de sentencias`.

---

