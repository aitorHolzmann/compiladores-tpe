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

### Error 3: Falta de “;” al final de las sentencias

#### Enunciado de la Cátedra
> "4. Falta de “;” al final de las sentencias." (`teoria/Errores a detectar.md`)

#### Contextos de Aparición en la Gramática
El delimitador punto y coma (`;`) es el carácter canónico de cierre y sincronización tanto en sentencias declarativas como ejecutables:
1. **Sentencias Declarativas**: Declaraciones de variables simples (`tipo`), objetos (`IDENTIFICADOR`), constantes de compilación (`COMPTIME`), clases (`CLASS ... END ;`) y funciones (`FUNCTION ... END ;`).
2. **Sentencias Ejecutables**: Asignaciones, expresiones, invocaciones a funciones/métodos y sentencias de E/S (`pout`, `ret`).

#### Reglas Implementadas en `gram.y`

**En Sentencias Declarativas:**
```yacc
declaracion_variables
    : tipo lista_variables ';'
    | tipo error ';'
        { yyerror("Falta ';' al final de la declaracion de variables"); }
    ;

declaracion_objeto
    : IDENTIFICADOR lista_identificadores ';'
    | IDENTIFICADOR lista_identificadores
        { yyerror("Falta ';' al final de la declaracion de objeto"); }
    ;

declaracion_comptime
    : COMPTIME tipo lista_identificadores ';'
    | COMPTIME tipo lista_identificadores
        { yyerror("Falta ';' al final de la declaracion comptime"); }
    ;

declaracion_clase
    : CLASS IDENTIFICADOR importacion_opcional
        BEGIN herencia_opcional miembros_clase END ';'
    | CLASS IDENTIFICADOR importacion_opcional
        BEGIN herencia_opcional miembros_clase END
        { yyerror("Falta ';' al final de la declaracion de clase"); }
    ;

declaracion_funciones
    : tipo FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END ';'
    | tipo FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END
        { yyerror("Falta ';' al final de la declaracion de funcion"); }
    | AUTO FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')'
        BEGIN sentencias_ejecutables END ';'
    | AUTO FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')'
        BEGIN sentencias_ejecutables END
        { yyerror("Falta ';' al final de la declaracion de funcion"); }
    ;
```

**En Sentencias Ejecutables:**
```yacc
sentencia_ejecutable
    : asignacion ';'
    | expresion ';'
    | sentencia_if
    | sentencia_repeat_until
    | sentencia_pout ';'
    | sentencia_ret ';'
    | error ';'
        { yyerror("Sentencia ejecutable malformada o falta ';' previo"); }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Diferenciación de Técnicas (Declarativas vs. Ejecutables):
* **En sentencias declarativas**: Para declaraciones con límites de bloque o palabras reservadas nítidas (`CLASS`, `COMPTIME`, `FUNCTION`, `AUTO`), se utiliza **Técnica A** pura (omisión del token `;`). En cambio, en `declaracion_variables`, dado que la lista de variables puede terminar con un identificador y el enunciado también exige detectar identificadores consecutivos sin coma (Error 5), se utiliza **Técnica B** mediante `tipo error ';'` para la recuperación de errores de delimitador. Esto sincroniza determinísticamente en el `;` de la siguiente sentencia sin introducir conflictos en la tabla LALR.
* **En sentencias ejecutables (Técnica B con sincronización en `;`)**: Las sentencias ejecutables (como asignaciones y expresiones aritméticas) no tienen delimitadores de inicio fijos y operan con gramáticas densas de operadores binarios. Si se omitiera el `;` entre dos sentencias ejecutables consecutivas (ej: `pout("A") pout("B");`), la segunda sentencia sería interpretada como un operando erróneo de la primera, deformando la frase gramatical. La utilización de `error ';'` desapila el estado conflictivo y sincroniza exactamente en el `;` de la sentencia subsiguiente, preservando la continuidad del compilador.

#### Casos de Prueba Verificados
* `tests/test_sin_ptocoma_decl.txt`: Falta `;` en declaración de variables $\rightarrow$ Detecta en Línea 4 `Falta ';' al final de la declaracion de variables`.
* `tests/test_sin_ptocoma_ejec.txt`: Falta `;` en sentencia ejecutable `pout` $\rightarrow$ Detecta en Línea 6 `Sentencia ejecutable malformada o falta ';' previo`.

---

### Error 4: Falta de nombre en función

#### Enunciado de la Cátedra
> "5. Falta de nombre en función." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{declaracion\_funciones} \rightarrow \text{tipo } \mathbf{FUNCTION} \text{ IDENTIFICADOR } \mathbf{(} \text{lista\_params} \mathbf{)} \dots$$
$$\text{declaracion\_funciones} \rightarrow \mathbf{AUTO} \text{ } \mathbf{FUNCTION} \text{ IDENTIFICADOR } \mathbf{(} \text{lista\_params} \mathbf{)} \dots$$

#### Reglas Implementadas en `gram.y`
```yacc
declaracion_funciones
    /* Reglas validas ... */
    | tipo FUNCTION error { yyerror("Falta nombre de funcion"); } '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END ';'
    | AUTO FUNCTION error { yyerror("Falta nombre de funcion"); } '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END ';'
    ;
```

#### Justificación Técnica y Defensa

##### 1. ¿Por qué se utiliza el token `error` y no una regla `tipo FUNCTION '('`?
* Si se intentara modelar `tipo FUNCTION '(' lista_parametros_formales ')'` mediante Técnica A pura, el autómata requeriría que el siguiente token sea obligatoriamente un paréntesis abierto `'('`. Si el desarrollador colocara un carácter espurio o un número en lugar del identificador, dicha regla no machearía.
* Mediante **Técnica B**, al situar `error` entre `FUNCTION` y `'('`, el analizador absorbe cualquier símbolo no válido situado en la posición del identificador y sincroniza de forma determinística en el paréntesis de apertura `'('`.

##### 2. Acción Semántica Intermedia Temprana:
* La acción `{ yyerror("Falta nombre de funcion"); }` se coloca inmediatamente después de `error` y antes de `'('`. De este modo, el error se emite en el momento exacto en que se detecta la anomalía (en la cabecera de la función), y no al finalizar la lectura de todo el cuerpo de la función al alcanzar el `END ;`.

##### 3. Preservación del Árbol Sintáctico Interno:
* Al sincronizar en `'('`, el autómata continúa normalmente con la reducción de `lista_parametros_formales`, las `sentencias_declarativas` internas y las `sentencias_ejecutables` del cuerpo de la función. Esto evita el "efecto dominó" de errores espurios en el interior de la función.

#### Casos de Prueba Verificados
* `tests/test_sin_nombre_funcion.txt`: Función `shortint FUNCTION (shortint a, shortint b)` sin nombre $\rightarrow$ Detecta en Línea 13 `Falta nombre de funcion` y continúa compilando el cuerpo y las funciones siguientes sin abortar.

---

### Error 5: Falta de “,” en declaración de variables

#### Enunciado de la Cátedra
> "6. Falta de “,” en declaración de variables." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{lista\_variables} \rightarrow \text{IDENTIFICADOR} \mid \text{lista\_variables } \mathbf{,} \text{ IDENTIFICADOR}$$

#### Reglas Implementadas en `gram.y`
```yacc
/* Declaracion de Variables */
declaracion_variables
    : tipo lista_variables ';'
    | tipo error ';'
        { yyerror("Falta ';' al final de la declaracion de variables"); }
    ;

/* Lista exclusiva para variables */
lista_variables
    : IDENTIFICADOR
    | lista_variables ',' IDENTIFICADOR
    | lista_variables IDENTIFICADOR   { yyerror("Falta ',' entre los identificadores"); }
    ;

/* Lista general (clases, objetos, extends, imports) */
lista_identificadores
    : IDENTIFICADOR
    | lista_identificadores ',' IDENTIFICADOR
    ;
```

#### Justificación Técnica y Defensa (Pregunta de Examen Oral)

##### 1. Especialización de la regla (`lista_variables` vs. `lista_identificadores`):
* El enunciado de la cátedra pide textualmente *"Falta de ',' en declaración de variables"*.
* Si la producción de error `lista_identificadores IDENTIFICADOR` se agregara a la regla general `lista_identificadores`, contaminaría todas las demás producciones que la reutilizan (`IMPORT FROM`, `EXTENDS`, `EXPORT TO`, `declaracion_objeto`).
* Al crear la regla especializada `lista_variables` para `declaracion_variables` mediante **Técnica A** (Gramática de error), se aísla el comportamiento erróneo estrictamente al ámbito solicitado por el enunciado.

##### 2. Desacoplamiento estructural y Cero Conflictos:
* Al separar `lista_variables` y combinarla con la sincronización por delimitador terminal en `declaracion_variables` (`tipo error ';'`), el autómata no sufre colisiones en la tabla LALR(1):
  * Ante identificadores contiguos en una declaración válida (`shortint a b;`), el autómata reduce limpiamente por `lista_variables IDENTIFICADOR`, emitiendo la advertencia de falta de coma y cerrando la sentencia en el punto y coma (`;`).
  * Ante la omisión del punto y coma al final de una declaración de variables, el modo de recuperación con sincronización `tipo error ';'` captura la anomalía emitiendo el mensaje correspondiente.
* **Resultado**: Se garantizan **0 conflictos Shift/Reduce y 0 conflictos Reduce/Reduce** en `y.output`, utilizando únicamente las herramientas vistas en clase (Gramática de error y token `error` con sincronización).

#### Casos de Prueba Verificados
* `tests/test_sin_coma_var.txt`:
  - Línea 2: `shortint a b;` $\rightarrow$ Detecta `Error sintactico (linea 2): Falta ',' entre los identificadores`.
  - Línea 3: `singlef x, y z;` $\rightarrow$ Detecta `Error sintactico (linea 3): Falta ',' entre los identificadores`.
  - Finaliza correctamente reportando los 2 errores sintácticos.

---

### Error 6: Falta de nombre de parámetro formal en declaración de función

#### Enunciado de la Cátedra
> "7. Falta de nombre de parámetro formal en declaración de función." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{parametro\_formal} \rightarrow \text{tipo } \text{IDENTIFICADOR}$$

#### Regla Implementada en `gram.y`
```yacc
parametro_formal
    : tipo IDENTIFICADOR
    | tipo error 
        { yyerror("Falta de nombre de parametro formal en declaracion de funcion"); }
    | error IDENTIFICADOR
        { yyerror("Falta de tipo del parametro formal en declaracion de funcion"); }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Naturaleza del Parámetro Formal:
* Canónicamente, un parámetro formal es un par ordenado $\langle \text{tipo}, \text{identificador} \rangle$.
* Cuando el programador especifica el tipo (`SHORTINT` o `SINGLEF`) pero omite el nombre, el símbolo siguiente en el flujo de entrada es el delimitador de separación `,` o el delimitador de cierre `)`.

##### 2. Comportamiento en la Pila LALR(1) con `tipo error`:
* Al leer `tipo`, el autómata espera un `IDENTIFICADOR`. Ante la presencia de cualquier símbolo no correspondiente (como `,` o `)`), el parser toma la transición de recuperación `tipo error`.
* La acción semántica intermedia ejecuta inmediatamente `yyerror(...)`, reportando la línea exacta de la signatura.
* La reducción a `parametro_formal` se produce de forma limpia gracias a que `,` y `)` pertenecen al conjunto $\text{FOLLOW}(\text{parametro\_formal})$.

##### 3. Preservación del Flujo de Compilación:
* Al resolver el error a nivel de `parametro_formal`, el autómata no entra en pánico destructivo ni desapila el nombre de la función ni el paréntesis de apertura. Esto permite que los parámetros subsiguientes y el cuerpo de la función (`BEGIN ... END;`) continúen analizándose normalmente.

#### Casos de Prueba Verificados
* `tests/test_parametro_formal.txt`: Función `sumar(a, shortint)` $\rightarrow$ Detecta en Línea 6 `Falta de nombre de parametro formal en declaracion de funcion`.
* Parámetro aislado sin nombre `(shortint)` o intermedio `(shortint, singlef y)` $\rightarrow$ Detecta el error en la línea correspondiente y compila el resto de la función.

---

### Error 7: Falta de tipo de parámetro formal en declaración de función

#### Enunciado de la Cátedra
> "8. Falta de tipo de parámetro formal en declaración de función." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{parametro\_formal} \rightarrow \text{tipo } \text{IDENTIFICADOR}$$

#### Regla Implementada en `gram.y`
```yacc
parametro_formal
    : tipo IDENTIFICADOR
    | tipo error 
        { yyerror("Falta de nombre de parametro formal en declaracion de funcion"); }
    | error IDENTIFICADOR
        { yyerror("Falta de tipo del parametro formal en declaracion de funcion"); }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Mecanismo de Sincronización en `error IDENTIFICADOR`:
* En la producción `error IDENTIFICADOR`, el terminal `IDENTIFICADOR` actúa como **token de sincronización local**.
* Cuando en la cabecera de la función aparece un identificador sin su tipo previo (ej: `sumar(a, shortint b)` o `sumar(x)`), el autómata activa la rama `error`, descarta cualquier token anómalo precedente y sincroniza inmediatamente en el `IDENTIFICADOR`.
* Tras desplazar el identificador, reduce la regla emitiendo el mensaje correspondiente.

##### 2. Cero Conflictos en BYacc/J:
* Al modelar ambos desvíos en el no terminal elemental `parametro_formal`, la gramática no introduce ambigüedades. BYacc/J genera el parser con **0 conflictos Shift/Reduce y 0 conflictos Reduce/Reduce**.

##### 3. Coexistencia de Múltiples Parámetros con Error:
* Si en una signatura se combinan parámetros erróneos consecutivos (ej: `sumar(a, shortint)` donde el primero carece de tipo y el segundo carece de nombre), el analizador procesa cada uno de forma independiente mediante las reglas correspondientes, reportando ambos errores sin interferencias mutuas ni descarte del cuerpo de la función.

#### Casos de Prueba Verificados
* `tests/test_parametro_formal.txt`: Función `sumar(a, shortint)` $\rightarrow$ Detecta en Línea 6 `Falta de tipo del parametro formal en declaracion de funcion`.
* Parámetros sin tipo múltiples `(x, y)` $\rightarrow$ Detecta secuencialmente la falta de tipo en cada parámetro.

---

### Error 8: Falta de operando en expresión

#### Enunciado de la Cátedra
> "9. Falta de operando en expresión." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{expresion} \rightarrow \text{termino} \mid \text{expresion } \mathbf{+} \text{ termino} \mid \text{expresion } \mathbf{-} \text{ termino}$$
$$\text{termino} \rightarrow \text{factor} \mid \text{termino } \mathbf{*} \text{ factor} \mid \text{termino } \mathbf{/} \text{ factor}$$

#### Reglas Implementadas en `gram.y`
```yacc
expresion
    : termino
    | expresion '+' termino
    | expresion '-' termino
    | expresion '+' error { yyerror("Falta un operando en la expresion"); }
    | expresion '-' error { yyerror("Falta un operando en la expresion"); }
    | '+' termino         { yyerror("Falta un operando en la expresion"); }
    ;

termino
    : factor
    | termino '*' factor
    | termino '/' factor
    | termino '*' error   { yyerror("Falta un operando en la expresion"); }
    | termino '/' error   { yyerror("Falta un operando en la expresion"); }
    | '*' factor          { yyerror("Falta un operando en la expresion"); }
    | '/' factor          { yyerror("Falta un operando en la expresion"); }
    | termino IDENTIFICADOR { yyerror("Falta operador en la expresion"); }
    | termino CONSTANTE     { yyerror("Falta operador en la expresion"); }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Cobertura en los Dos Niveles de la Jerarquía Aritmética:
* Las expresiones respetan la precedencia clásica de operadores separando sumas/restas (`expresion`) de multiplicaciones/divisiones (`termino`).
* Omitir un operando en cualquier nivel (ej: `a * ;` vs `a + ;`) debe recibir idéntico tratamiento. Al incorporar reglas homólogas en `expresion` (`+`, `-`) y en `termino` (`*`, `/`), se garantiza una cobertura exhaustiva sin zonas ciegas que desemboquen en pánico destructivo.

##### 2. Falta de Operando a la Derecha (Técnica B con pseudo-token `error`):
* En producciones como `expresion '+' error` y `termino '*' error`, cuando el autómata lee un operador y no encuentra un operando válido a continuación (por encontrar el fin de sentencia `;`, un delimitador `)` o un operador duplicado), el parser desplaza `error`.
* Esto captura la anomalía localmente dentro de la expresión, ejecuta la acción semántica y reduce limpiamente la regla de error, permitiendo que el delimitador de cierre (`;`) o la estructura circundante continúen su curso normal.

##### 3. Falta de Operando a la Izquierda al Inicio de Expresión (Técnica A pura):
* Cuando una expresión comienza de forma inválida con un operador binario (`+ termino`, `* factor`, `/ factor`), el operador no tiene operando precedente.
* Dado que en este lenguaje `+`, `*` y `/` no tienen función unaria válida en la posición inicial (el único prefijo unario permitido es `-` para literales numéricos negativos), estas producciones son determinísticas en LALR(1). El autómata desplaza el operador anómalo, procesa el operando subsiguiente y reduce la regla emitiendo el mensaje correspondiente sin entrar en pánico ni requerir el token `error`.

##### 4. Operadores Consecutivos (`a + + b`):
* Ante operadores contiguos, la primera regla `expresion '+' error` absorbe el operador espurio reportando la falta del operando intermedio, y reduce de nuevo a `expresion`. El lookahead conserva el segundo operador `+`, permitiendo que el compilador procese el resto de la expresión (`+ b`) sin descartar tokens.

##### 5. Cero Conflictos en BYacc/J:
* Al no mezclar tokens `error` superpuestos entre `expresion` y `termino`, la tabla LALR(1) se genera con **0 conflictos Shift/Reduce y 0 conflictos Reduce/Reduce**.

#### Casos de Prueba Verificados
* `tests/test_expresiones_errores.txt`:
  - Línea 6: `resultado := a + ;` $\rightarrow$ Detecta `Error sintactico (linea 6): Falta un operando en la expresion`.
  - Línea 7: `resultado := a - ;` $\rightarrow$ Detecta `Error sintactico (linea 7): Falta un operando en la expresion`.
  - Línea 8: `resultado := a * ;` $\rightarrow$ Detecta `Error sintactico (linea 8): Falta un operando en la expresion`.
  - Línea 9: `resultado := a / ;` $\rightarrow$ Detecta `Error sintactico (linea 9): Falta un operando en la expresion`.
  - Línea 10: `resultado := + a;` $\rightarrow$ Detecta `Error sintactico (linea 10): Falta un operando en la expresion`.
  - Línea 11: `resultado := * a;` $\rightarrow$ Detecta `Error sintactico (linea 11): Falta un operando en la expresion`.
  - Línea 12: `resultado := / a;` $\rightarrow$ Detecta `Error sintactico (linea 12): Falta un operando en la expresion`.
  - Línea 13: `resultado := a + + b;` $\rightarrow$ Detecta `Error sintactico (linea 13): Falta un operando en la expresion`.

---

### Error 9: Falta de operador en expresión

#### Enunciado de la Cátedra
> "10. Falta de operador en expresión." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{termino} \rightarrow \text{factor} \mid \text{termino } \mathbf{*} \text{ factor} \mid \text{termino } \mathbf{/} \text{ factor}$$
Todo par de operandos contiguos en una expresión debe estar mediado por un operador aritmético binario.

#### Reglas Implementadas en `gram.y`
```yacc
termino
    : factor
    | termino '*' factor
    | termino '/' factor
    | termino '*' error   { yyerror("Falta un operando en la expresion"); }
    | termino '/' error   { yyerror("Falta un operando en la expresion"); }
    | '*' factor          { yyerror("Falta un operando en la expresion"); }
    | '/' factor          { yyerror("Falta un operando en la expresion"); }
    | termino IDENTIFICADOR { yyerror("Falta operador en la expresion"); }
    | termino CONSTANTE     { yyerror("Falta operador en la expresion"); }
    ;
```

#### Justificación Técnica y Defensa (Pregunta Típica de Examen Final)

##### 1. La Ineficacia Teórica de `expresion error termino`:
* Se podría suponer intuitivamente que la falta de operador puede capturarse con `expresion error termino`. Sin embargo, en un autómata LALR(1), cuando el programador escribe `a := b c;`, el parser lee `b` y queda en el estado de reducción de `factor`.
* Para que `b` se reduzca a `factor` y luego a `expresion`, el token siguiente (lookahead) debe pertenecer al conjunto $\text{FOLLOW}(\text{factor})$, el cual **no contiene `IDENTIFICADOR`** (solo operadores o delimitadores).
* Al ver `c` (`IDENTIFICADOR`), el autómata sufre un error de sintaxis antes de reducir `b`. El modo de recuperación por pánico desapila el estado de `b` y entra en descarte ciego de tokens, tragándose `c`, `;` y `END` en silencio.

##### 2. Solución Mediante Técnica A (Gramática de Error sin token `error`):
* Al igual que en la detección de falta de coma en declaraciones de variables (`lista_variables IDENTIFICADOR`), el compilador modela explícitamente la contigüidad de operandos mediante **Técnica A**.
* Cuando a un `termino` le sigue inmediatamente otro operando (`IDENTIFICADOR` o `CONSTANTE`) sin operador intermedio, el autómata realiza la reducción determinística por `termino IDENTIFICADOR`, emite el mensaje exacto `"Falta operador en la expresion"` y prosigue normalmente el análisis sintáctico.

##### 3. Prevención de Conflictos con el Menos Unario:
* Si se intentara escribir genéricamente `termino factor`, BYacc/J reporta `1 shift/reduce conflict on '-'`. Esto ocurre porque el terminal `-` tiene una doble naturaleza: puede ser una resta binaria (`expresion '-' termino`) o el inicio de un número negativo (`factor : numero_negativo`).
* Al explicitar los símbolos que inequívocamente inician un operando sin ambigüedad sintáctica (`IDENTIFICADOR` y `CONSTANTE`), la gramática mantiene **estrictamente 0 conflictos Shift/Reduce y 0 Reduce/Reduce**.

#### Casos de Prueba Verificados
* `tests/test_expresiones_errores.txt`:
  - Línea 14: `resultado := a b;` $\rightarrow$ Detecta `Error sintactico (linea 14): Falta operador en la expresion`.
* Finaliza la compilación del archivo reconociendo normalmente las sentencias válidas circundantes (`resultado := a + b * c;`).

---

### Error 10: Falta argumento en sentencia pout

#### Enunciado de la Cátedra
> "11. Falta argumento en sentencia pout." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{sentencia\_pout} \rightarrow \mathbf{POUT} \;\; \mathbf{(} \;\; \text{CADENA} \;\; \mathbf{)} \mid \mathbf{POUT} \;\; \mathbf{(} \;\; \text{expresion} \;\; \mathbf{)}$$

#### Reglas Implementadas en `gram.y`
```yacc
sentencia_pout
    : POUT '(' CADENA ')'
    | POUT '(' expresion ')'
    | POUT '(' ')'
        { yyerror("Falta argumento en sentencia pout"); }
    ;
```

#### Justificación Técnica y Defensa
1. **Modelado Mediante Técnica A (Gramática de Error)**:
   - Cuando el desarrollador escribe la llamada `pout();`, los delimitadores sintácticos de invocación (`(` y `)`) están presentes, pero la lista de argumentos se encuentra vacía.
   - Al modelar explícitamente `POUT '(' ')'`, el autómata reconoce la omisión de forma inmediata y determinística en la reducción, reportando la línea exacta sin descartar tokens ni entrar en modo pánico.
2. **Cero Conflictos**:
   - Como una sentencia ejecutable `sentencia_pout` culmina con un punto y coma (`;`), la reducción de `POUT '(' ')'` no colisiona con ninguna otra regla de la gramática. Se mantiene la tabla LALR(1) con **0 conflictos Shift/Reduce y 0 Reduce/Reduce**.

#### Casos de Prueba Verificados
* `tests/test_ctrl_pout_errores.txt`:
  - Línea 6: `pout();` $\rightarrow$ Detecta `Error sintactico (linea 6): Falta argumento en sentencia pout`.
  - Línea 7: `pout("Mensaje correcto");` $\rightarrow$ Reconocido válidamente sin falsos positivos.

---

### Error 11: Falta de paréntesis de apertura y/o cierre en condición de selecciones e iteraciones

#### Enunciado de la Cátedra
> "12. Falta de paréntesis de apertura y/o cierre en condición de selecciones e iteraciones." (`teoria/Errores a detectar.md`)

#### Contextos de Aparición en la Gramática
El delimitador de condición afecta a dos estructuras de control:
1. **Selección (`sentencia_if`)**: Requiere `IF '(' condicion ')' bloque ...`
2. **Iteración (`sentencia_repeat_until`)**: Requiere `REPEAT bloque UNTIL '(' condicion ')' ';'`

#### Reglas Implementadas en `gram.y`

**En Selecciones (`sentencia_if` y `resto_if`):**
```yacc
sentencia_if
    : IF '(' condicion ')' bloque resto_if
    | IF condicion ')' bloque resto_if
        { yyerror("Falta '(' de apertura en condicion de seleccion"); }
    | IF error bloque resto_if
        { yyerror("Condicion de seleccion malformada o error en parentesis"); }
    ;

resto_if
    : END_IF ';'
    | ELSE bloque END_IF ';'
    ;
```

**En Iteraciones (`sentencia_repeat_until`):**
```yacc
sentencia_repeat_until
    : REPEAT bloque UNTIL '(' condicion ')' ';'
    | REPEAT bloque UNTIL condicion ')' ';'
        { yyerror("Falta '(' de apertura en condicion de iteracion"); }
    | REPEAT bloque UNTIL '(' condicion ';'
        { yyerror("Falta ')' de cierre en condicion de iteracion"); }
    | REPEAT bloque UNTIL condicion ';'
        { yyerror("Faltan parentesis en condicion de iteracion"); }
    ;
```

#### Justificación Técnica y Defensa (Preguntas Críticas de Examen Final)

##### 1. ¿Por qué se factorizó `resto_if`?
* La estructura canónica del `IF` admite dos formas: con cláusula alternativa (`ELSE bloque END_IF ';'`) o sin ella (`END_IF ';'`).
* Ambas comparten el 80% de su estructura: `IF ... condicion ... bloque`.
* Si no se factorizara la cola común, cada regla de error para la condición debería duplicarse textualmente para ambas variantes. Esto genera riesgo de asimetría y código redundante.
* La factorización mediante `resto_if` es una transformación formal estándar ($A \rightarrow \alpha \beta_1 \mid \alpha \beta_2 \iff A \rightarrow \alpha B$) que compacta el autómata y garantiza cobertura uniforme en ambas ramas con **0 conflictos**.

##### 2. Eliminación de Conflictos Reduce/Reduce (El problema de las acciones intermedias):
* Si se intentara colocar una acción semántica intermedia como `IF { yyerror("Falta '('"); } condicion ...`, Yacc genera producciones vacías ($\epsilon$-producciones) invisibles.
* Si coexisten múltiples reglas con acciones intermedias inmediatamente después de `IF` o de `UNTIL`, al llegar el primer símbolo de `condicion`, el parser LALR(1) sufre **conflictos reduce/reduce irresolubles** (ambigüedad entre qué acción vacía reducir).
* Al trasladar las acciones al final de las reglas correspondientes, se eliminan por completo los 14 conflictos reduce/reduce.

##### 3. Diferenciación Teórica entre `REPEAT UNTIL` e `IF`:
* **En `REPEAT UNTIL` (Técnica A determinística)**:
  La condición está delimitada al final por un punto y coma (`;`). Dado que `;` es un token terminal rígido que **nunca puede formar parte de una condición ni de una expresión**, el autómata sabe con lookahead 1 ($k=1$) cuándo termina la condición, permitiendo detectar la falta de `(`, la falta de `)` o la falta de ambos de forma puramente determinística sin entrar en pánico.
* **En `sentencia_if` (Técnica B con sincronización en `bloque`)**:
  A diferencia del `REPEAT`, en el `IF` la condición es seguida inmediatamente por un `bloque`, el cual puede comenzar con una sentencia ejecutable simple (ej: una asignación `x := 2$s;`).
  Si se intentara modelar la falta de `)` con Técnica A pura (`IF '(' condicion bloque`), ante un identificador `x` después de la condición, un analizador con lookahead 1 no puede discernir si `x` es un error de operando en la expresión o la primera variable del bloque. Esto generaría 12 conflictos Shift/Reduce.
  Al utilizar `IF error bloque resto_if`, la gramática delega la recuperación ante la falta de `)` o condiciones malformadas a la sincronización con el inicio del `bloque`, emitiendo el mensaje descriptivo y permitiendo que el cuerpo del `IF` y el `resto_if` continúen compilándose sin abortar.

#### Casos de Prueba Verificados
* `tests/test_ctrl_pout_errores.txt`:
  - Línea 11: `IF x > y) ...` $\rightarrow$ Detecta `Error sintactico (linea 11): Falta '(' de apertura en condicion de seleccion`.
  - Línea 17: `IF x > y) ... ELSE ...` $\rightarrow$ Detecta `Error sintactico (linea 17): Falta '(' de apertura en condicion de seleccion` en estructura con `ELSE`.
  - Línea 21: `UNTIL x > y);` $\rightarrow$ Detecta `Error sintactico (linea 21): Falta '(' de apertura en condicion de iteracion`.
  - Línea 25: `UNTIL (x > y;` $\rightarrow$ Detecta `Error sintactico (linea 25): Falta ')' de cierre en condicion de iteracion`.
  - Línea 29: `UNTIL x > y;` $\rightarrow$ Detecta `Error sintactico (linea 29): Faltan parentesis en condicion de iteracion`.
  - Línea 33: `UNTIL (x > y);` $\rightarrow$ Reconocido normalmente sin errores sintácticos.

---

### Error 12: Falta de cuerpo en iteraciones

#### Enunciado de la Cátedra
> "13. Falta de cuerpo en iteraciones." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{sentencia\_repeat\_until} \rightarrow \mathbf{REPEAT} \;\; \text{bloque} \;\; \mathbf{UNTIL} \;\; \mathbf{(} \;\; \text{condicion} \;\; \mathbf{)} \;\; \mathbf{;}$$
En la estructura de iteración `REPEAT UNTIL`, la palabra reservada `REPEAT` debe ir seguida obligatoriamente por un cuerpo de sentencias (`bloque`).

#### Reglas Implementadas en `gram.y`
```yacc
/* Tema 12: Repeat-Until */
sentencia_repeat_until
    : REPEAT bloque UNTIL '(' condicion ')' ';'
    | REPEAT bloque UNTIL condicion ')' ';'
        { yyerror("Falta '(' de apertura en condicion de iteracion"); }
    | REPEAT bloque UNTIL '(' condicion ';'
        { yyerror("Falta ')' de cierre en condicion de iteracion"); }
    | REPEAT bloque UNTIL condicion ';'
        { yyerror("Faltan parentesis en condicion de iteracion"); }
    | REPEAT UNTIL '(' condicion ')' ';'
        { yyerror("Falta el cuerpo de la iteracion REPEAT"); }
    | REPEAT UNTIL condicion ')' ';'
        { yyerror("Falta el cuerpo de la iteracion REPEAT"); }
    | REPEAT UNTIL '(' condicion ';'
        { yyerror("Falta el cuerpo de la iteracion REPEAT"); }
    | REPEAT UNTIL condicion ';'
        { yyerror("Falta el cuerpo de la iteracion REPEAT"); }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Modelado Mediante Técnica A Pura (Gramática de Error):
* Cuando el programador escribe `REPEAT` seguido inmediatamente por `UNTIL` (sin sentencias ejecutables ni bloque intermedio), se produce la omisión del cuerpo de la iteración.
* Al modelar directamente `REPEAT UNTIL ...`, el autómata LALR(1) detecta la contigüidad de las dos palabras clave de forma inmediata y determinística sin desapilar estados ni entrar en modo de pánico destructivo.

##### 2. Prevención del Falso Positivo de *"Sentencia ejecutable malformada o falta ';' previo"*:
* Sin estas reglas explícitas, tras leer `REPEAT` el autómata espera un `bloque`. Al recibir `UNTIL` (que no pertenece a $\text{FIRST}(\text{bloque})$), el parser colapsaría en pánico, desapilando el `REPEAT` y cayendo en la regla genérica de captura `sentencia_ejecutable : error ';'`. Esto emitía un mensaje confuso de delimitador previo en lugar de diagnosticar la ausencia del cuerpo del bucle.
* Con la producción de error especializada, el compilador reporta con precisión: `"Falta el cuerpo de la iteracion REPEAT"`.

##### 3. Cobertura ante Combinación de Errores (Cuerpo y Paréntesis):
* Se contemplaron las variantes donde la ausencia del cuerpo coincide con la omisión del paréntesis de apertura `(`, del paréntesis de cierre `)` o de ambos en la condición del `UNTIL`.
* En cualquiera de estas combinaciones, el autómata absorbe la anomalía emitiendo el mensaje correspondiente y cierra la sentencia en el punto y coma (`;`), preservando el flujo de compilación para el resto del programa.

##### 4. Cero Conflictos en BYacc/J:
* Al no existir solapamiento de tokens ni ambigüedad entre `bloque` y `UNTIL`, la gramática se mantiene estrictamente en **0 conflictos Shift/Reduce y 0 Reduce/Reduce**.

#### Casos de Prueba Verificados
* `tests/test_repeat_until.txt`:
  - Línea 6: `REPEAT UNTIL (x > b);` $\rightarrow$ Detecta `Error sintactico (linea 6): Falta el cuerpo de la iteracion REPEAT`.
  - Líneas 8 a 13: `REPEAT BEGIN ... END UNTIL (x <= bloque);` $\rightarrow$ Iteración con cuerpo completo reconocida válidamente sin falsos positivos.
* Variantes combinadas (`REPEAT UNTIL x > b);`, `REPEAT UNTIL (x > b;`, `REPEAT UNTIL x > b;`):
  - Todas detectan la falta de cuerpo en la iteración y continúan compilando las sentencias subsiguientes.

---

### Error 13: Falta de end_if

#### Enunciado de la Cátedra
> "14. Falta de end_if" (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{sentencia\_if} \rightarrow \mathbf{IF} \;\; \mathbf{(} \;\; \text{condicion} \;\; \mathbf{)} \;\; \text{bloque} \;\; \text{resto\_if}$$
$$\text{resto\_if} \rightarrow \mathbf{END\_IF} \;\; \mathbf{;} \mid \mathbf{ELSE} \;\; \text{bloque} \;\; \mathbf{END\_IF} \;\; \mathbf{;}$$

#### Reglas Implementadas en `gram.y`
```yacc
resto_if
    : END_IF ';'
    | ELSE bloque END_IF ';'
    | ';'
        { yyerror("Falta palabra clave END_IF al final de la sentencia IF"); }
    | ELSE bloque ';'
        { yyerror("Falta palabra clave END_IF al final de la sentencia IF"); }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Factorización y Aislamiento de la Cola en `resto_if`:
* Al modularizar la terminación del `IF` en `resto_if`, la regla de error para la omisión de `END_IF` se aplica de forma simétrica e idéntica tanto a la variante simple del `IF` como a la variante con cláusula alternativa `ELSE`.

##### 2. Modelado Mediante Técnica A (Gramática de Error):
* Cuando el programador finaliza el cuerpo del `IF` (o el cuerpo del `ELSE`) y coloca el delimitador terminal punto y coma (`;`) omitiendo la palabra clave `END_IF` (ej: `IF (c) BEGIN ... END;` o `IF (c) x := 1; ;`), el autómata reduce limpiamente por `resto_if : ';'`, emite el diagnóstico preciso `"Falta palabra clave END_IF al final de la sentencia IF"` y prosigue normalmente el análisis sintáctico.

##### 3. Eliminación de Conflictos Shift/Reduce con el Bloque:
* Si se intentara usar el token `error` en `resto_if` (`resto_if : error ';'`), el autómata LALR(1) sufre un conflicto Shift/Reduce con la recuperación de sentencias ejecutables dentro del `bloque`.
* Al modelar la omisión directamente sobre el token terminal `;` mediante Técnica A, se evitan colisiones con la recuperación interna del bloque, manteniendo **0 conflictos Shift/Reduce y 0 Reduce/Reduce** en BYacc/J.

#### Casos de Prueba Verificados
* `tests/test_end_if_until.txt`:
  - Línea 9: `IF (x > y) BEGIN ... END;` $\rightarrow$ Detecta `Error sintactico (linea 9): Falta palabra clave END_IF al final de la sentencia IF`.
  - Línea 15: `IF (x > y) ... ELSE ... ;` $\rightarrow$ Detecta `Error sintactico (linea 15): Falta palabra clave END_IF al final de la sentencia IF` en estructura con `ELSE`.

---

### Error 14: Falta de until (Tema 12)

#### Enunciado de la Cátedra
> "15. Tema 12: Falta until." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{sentencia\_repeat\_until} \rightarrow \mathbf{REPEAT} \;\; \text{bloque} \;\; \mathbf{UNTIL} \;\; \mathbf{(} \;\; \text{condicion} \;\; \mathbf{)} \;\; \mathbf{;}$$

#### Reglas Implementadas en `gram.y`
```yacc
/* Tema 12: Repeat-Until */
sentencia_repeat_until
    : REPEAT bloque UNTIL condicion_iteracion ';'
    | REPEAT UNTIL condicion_iteracion ';'
        { yyerror("Falta el cuerpo de la iteracion REPEAT"); }
    | REPEAT bloque '(' condicion ')' ';'
        { yyerror("Falta palabra clave UNTIL en la iteracion REPEAT"); }
    | REPEAT '(' condicion ')' ';'
        { yyerror("Falta palabra clave UNTIL y cuerpo en la iteracion REPEAT"); }
    | REPEAT bloque '(' condicion ';'
        { yyerror("Falta palabra clave UNTIL y ')' en la iteracion REPEAT"); }
    | REPEAT '(' condicion ';'
        { yyerror("Falta palabra clave UNTIL, cuerpo y ')' en la iteracion REPEAT"); }
    ;

condicion_iteracion
    : '(' condicion ')'
    | condicion ')'
        { yyerror("Falta '(' de apertura en condicion de iteracion"); }
    | '(' condicion
        { yyerror("Falta ')' de cierre en condicion de iteracion"); }
    | condicion
        { yyerror("Faltan parentesis en condicion de iteracion"); }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Modularización Elegante de Permutaciones (`condicion_iteracion`):
* En lugar de multiplicar combinatoriamente cada variación de paréntesis por cada variante de `UNTIL` y de `bloque` (lo que requeriría más de 16 reglas redundantes), se desacopló el no terminal auxiliar `condicion_iteracion`.
* Este módulo encapsula las 4 combinaciones posibles de delimitación de la condición (canónica, sin `(`, sin `)` y sin ambos) emitiendo sus propios diagnósticos específicos.
* Gracias a esto, `REPEAT bloque UNTIL condicion_iteracion ';'` y `REPEAT UNTIL condicion_iteracion ';'` quedan expresadas de forma concisa y matemáticamente rigurosa.

##### 2. Detección Determinística de la Omisión de `UNTIL`:
* Cuando el programador omite la palabra clave `UNTIL`, tras el cuerpo del `REPEAT` aparece directamente la condición parentizada (ej: `REPEAT x := y; (x > b);`).
* Dado que en este lenguaje ninguna sentencia ejecutable simple ni bloque puede comenzar con el terminal `'('` (las expresiones no permiten paréntesis anidados), el token `'('` actúa como un **delimitador inequívoco y determinístico**.
* El parser LALR(1) reduce el `bloque`, lee `'('` y transiciona determinísticamente por las producciones de omisión de `UNTIL`, emitiendo el mensaje correspondiente sin entrar en pánico destructivo.

##### 3. Cobertura de Doble Omisión (Falta de UNTIL y Falta de Cuerpo):
* Si el programador omite tanto el cuerpo como la palabra clave `UNTIL` (ej: `REPEAT (x > b);`), la regla especializada captura ambas anomalías de forma simultánea, reportando: `"Falta palabra clave UNTIL y cuerpo en la iteracion REPEAT"`.

##### 4. Cero Conflictos en BYacc/J:
* Al no superponerse el conjunto $\text{FIRST}(\text{condicion parentizada})$ con $\text{FIRST}(\text{sentencia\_ejecutable})$, la gramática completa mantiene **0 conflictos Shift/Reduce y 0 Reduce/Reduce**.

#### Casos de Prueba Verificados
* `tests/test_end_if_until.txt`:
  - Línea 19: `REPEAT x := y; (x > b);` $\rightarrow$ Detecta `Error sintactico (linea 19): Falta palabra clave UNTIL en la iteracion REPEAT`.
  - Línea 22: `REPEAT (x > b);` $\rightarrow$ Detecta `Error sintactico (linea 22): Falta palabra clave UNTIL y cuerpo en la iteracion REPEAT`.
  - Línea 26: `REPEAT x := y; (x > b;` $\rightarrow$ Detecta `Error sintactico (linea 26): Falta palabra clave UNTIL y ')' en la iteracion REPEAT`.
  - Línea 30: `REPEAT x := y; UNTIL (x > b);` $\rightarrow$ Iteración canónica válida reconocida sin falsos positivos.

---

### Error 15: Uso del símbolo de asignación normal (':=') donde debe usarse '=' (Tema 17)

#### Enunciado de la Cátedra
> "16. Temas 17: Uso del símbolo de asignación normal (‘:=’) donde debe usarse ‘=’." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{factor} \rightarrow \text{unica}$$
$$\text{unica} \rightarrow \mathbf{IDENTIFICADOR} \;\; \mathbf{=} \;\; \mathbf{(} \;\; \text{expresion} \;\; \mathbf{)}$$

#### Regla Implementada en `gram.y`
```yacc
/* Tema 17: Asignacion en expresion */
unica
    : IDENTIFICADOR '=' '(' expresion ')'
    | IDENTIFICADOR ASIGNAR '(' expresion ')'
        { yyerror("Uso del simbolo ':=' donde debe usarse '=' en asignacion dentro de expresion"); }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Distinción Semántico-Sintáctica de Asignaciones:
* En este lenguaje, la asignación como sentencia independiente utiliza el operador tradicional `:=` (ej: `x := 5$s;`).
* Para el **Tema 17 (Asignaciones de expresiones en expresiones)**, la especificación exige estrictamente el símbolo `=` para asignar valores a términos dentro de una expresión, requiriendo además que la subexpresión asignada esté delimitada por paréntesis sin anidamiento (ej: `a := b = (x);` o `c := a + b = (y);`).

##### 2. Implementación Mediante Técnica A Pura (Gramática de Error):
* Se añade una alternativa explícita en la producción `unica` que contempla la presencia errónea del operador de asignación estándar (`:=`) en lugar del operador igual (`=`).
* Al ser una producción específica de error, el autómata LALR(1) reduce la construcción directamente al encontrar la secuencia errónea, emitiendo el diagnóstico exacto sin desapilar estados ni descartar tokens.

##### 3. Resolución Crítica del Token en BYacc/J (`ASIGNAR` vs `':='`):
* En BYacc/J y Yacc, los caracteres simples delimitados por comillas simples (`'='`, `'+'`, `';'`) son tratados como tokens con su correspondiente valor numérico ASCII (por ejemplo, `'='` equivale a 61).
* Sin embargo, una secuencia de múltiples caracteres como `':='` no es un carácter simple. Si se escribe literalmente `':='` en la gramática, BYacc/J genera un nuevo identificador de token anónimo con código numérico superior a 285.
* Por su parte, el analizador léxico (`AnalizadorLexico`), al escanear la secuencia `:=`, consulta la tabla de palabras reservadas y emite el token `ASIGNAR` (código numérico 267).
* Por esta razón técnica fundamental, la producción de error en `gram.y` debe utilizar taxativamente el terminal simbólico `%token ASIGNAR 267`. De lo contrario, existiría un desacople entre el código emitido por el léxico (267) y el esperado por el parser (286), lo que provocaría que la regla no matchee y el compilador caiga en un pánico genérico.

##### 4. Ausencia de Conflictos con la Sentencia de Asignación:
* En la gramática, un `IDENTIFICADOR` seguido de `ASIGNAR` también puede iniciar una `asignacion` a nivel sentencia (`asignacion : IDENTIFICADOR ASIGNAR expresion`).
* No obstante, en `unica : IDENTIFICADOR ASIGNAR '(' expresion ')'`, el operador `ASIGNAR` está seguido inmediatamente de forma obligatoria por el delimitador `'('`. En contraste, las expresiones estándar del lenguaje no permiten paréntesis arbitrarios alrededor de la expresión completa ni en factores simples (no hay `factor : '(' expresion ')'`).
* Por lo tanto, el autómata LALR(1) resuelve determinísticamente mediante el símbolo de anticipación sin introducir ambigüedades, manteniendo **0 conflictos Shift/Reduce y 0 Reduce/Reduce** en BYacc/J.

#### Casos de Prueba Verificados
* `tests/test_unica_asignar.txt`:
  - Línea 6: `a := b = (x);` $\rightarrow$ Asignación dentro de expresión con `=` reconocida como válida.
  - Línea 7: `c := a + b = (y);` $\rightarrow$ Asignación dentro de término aritmético con `=` reconocida como válida.
  - Línea 10: `a := b := (x);` $\rightarrow$ Detecta con precisión:  
    `Error sintactico (linea 11): Uso del simbolo ':=' donde debe usarse '=' en asignacion dentro de expresion`.

---

### Error 16: Ausencia de retorno en una función (Tema 21)

#### Enunciado de la Cátedra
> "17. Tema 21: Ausencia de retorno en una función." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{declaracion\_funciones} \rightarrow \mathbf{AUTO} \;\; \mathbf{FUNCTION} \;\; \mathbf{IDENTIFICADOR} \;\; \mathbf{(} \;\; \text{lista\_parametros\_formales} \;\; \mathbf{)} \;\; \mathbf{BEGIN} \;\; \text{sentencias\_ejecutables} \;\; \mathbf{END} \;\; \mathbf{;}$$

#### Reglas Implementadas en `gram.y`
```yacc
encabezado_auto_funcion
    : AUTO FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')'
        { cant_retornos_auto = 0; }
    ;

declaracion_funciones
    /* ... funciones ordinarias ... */

    /* Tema 21: AUTO FUNCTION */
    | encabezado_auto_funcion BEGIN sentencias_ejecutables END ';'
        {
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
        }
    | AUTO FUNCTION error { yyerror("Falta nombre de funcion"); } '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END ';'
    | encabezado_auto_funcion BEGIN sentencias_ejecutables END
        {
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
            yyerror("Falta ';' al final de la declaracion de funcion");
        }
    ;

sentencia_ret
    : RET '(' expresion ')'
        {
            if (cant_retornos_auto >= 0) {
                cant_retornos_auto++;
            }
        }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Imposibilidad de Modelado Puramente BNF en LALR(1) con Lookahead $k=1$:
* Si se intentara modelar la presencia obligatoria de un retorno bifurcando las producciones de la gramática libre de contexto en `sentencias_con_retorno` y `sentencias_sin_retorno`, el autómata LALR(1) enfrentaría un problema insalvable con $k=1$:
  - Al procesar la primera sentencia de un cuerpo (por ejemplo, una asignación `x := 1;`), el analizador no puede prever si 5 sentencias más adelante existirá un `RET` o si una rama condicional contendrá uno.
  - Ambas ramas derivarían por producciones distintas pero compartiendo idénticos prefijos viables, generando de inmediato **conflictos Shift/Reduce y Reduce/Reduce masivos**.
* Además, el requisito de que una función contenga al menos un retorno es una **restricción contextual dependiente de la traducción dirigida por la sintaxis** (exactamente análogo al chequeo de rango numérico en `factor : CONSTANTE`).

##### 2. Traducción Dirigida por la Sintaxis (Acciones Semánticas del Parser):
* Siguiendo el estándar de diseño de compiladores en Yacc, la restricción se modela asociando acciones a las reducciones sintácticas del propio parser:
  - Al reducir `encabezado_auto_funcion`, se inicializa `cant_retornos_auto = 0`, marcando que el cuerpo analizado corresponde a una función `AUTO`.
  - Cada vez que el autómata reduce cualquier producción `sentencia_ret : RET '(' expresion ')'`, incrementa el contador `cant_retornos_auto++`.
  - Al completar la reducción del cuerpo de la función en `END ';'`, se comprueba si `cant_retornos_auto == 0`. Si no se registró ningún retorno, se emite el error sintáctico unificado.

##### 3. Cobertura Completa ante Retornos en Raíz o en Estructuras Anidadas:
* Gracias a este diseño, no importa en qué nivel de profundidad jerárquica se encuentre el `RET`:
  - En la raíz de las sentencias ejecutables de la función.
  - En una rama de selección simple (`IF (cond) RET(x); END_IF;`).
  - En ambas ramas de una selección alternativa (`IF-ELSE`).
  - Dentro de un bloque anidado `BEGIN ... RET(x); ... END`.
  - Dentro de una iteración `REPEAT UNTIL`.
* En cualquiera de estos casos, la reducción gramatical de `sentencia_ret` es alcanzada y el contador se incrementa, validando la función sin falsos positivos.

##### 4. Determinismo Secuencial y Ausencia de Conflictos:
* A diferencia de las funciones comunes, la especificación para `AUTO FUNCTION` no incluye sentencias declarativas locales (`sentencias_declarativas`), por lo que no es posible declarar funciones anidadas dentro de una función `AUTO`.
* Esto garantiza que el análisis de cuerpos de funciones `AUTO` sea estrictamente secuencial, permitiendo el uso de una variable de estado entera `cant_retornos_auto` sin necesidad de estructuras complejas.
* El encabezado modularizado `encabezado_auto_funcion` mantiene **0 conflictos Shift/Reduce y 0 Reduce/Reduce** en BYacc/J.

#### Casos de Prueba Verificados
* `tests/test_auto_retorno.txt`:
  - Líneas 6-10 (`f_raiz`): Retorno en la raíz $\rightarrow$ Reconocida válidamente sin errores.
  - Líneas 13-21 (`f_if_else`): Retornos dentro de ramas `IF` y `ELSE` $\rightarrow$ Reconocida válidamente.
  - Líneas 24-33 (`f_bloque`): Retorno dentro de bloque anidado `BEGIN END` $\rightarrow$ Reconocida válidamente.
  - Líneas 36-39 (`f_normal_sin_ret`): Función común sin retorno $\rightarrow$ Válida (ausencia permitida en funciones ordinarias).
  - Línea 43 (`f_error_sin_ret`): Función `AUTO` sin retorno $\rightarrow$ Detecta:  
    `Error sintactico (linea 43): Ausencia de sentencia de retorno 'RET' en funcion AUTO`.
  - Línea 51 (`f_error_if_sin_ret`): Función `AUTO` con `IF` pero sin sentencias `RET` $\rightarrow$ Detecta:  
    `Error sintactico (linea 51): Ausencia de sentencia de retorno 'RET' en funcion AUTO`.

---

### Error 17: Falta de tipo en la declaración de variables en tiempo de compilación (Tema 22)

#### Enunciado de la Cátedra
> "18. Tema 22: Falta de tipo en la declaración de variables en tiempo de compilación" (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{declaracion\_comptime} \rightarrow \mathbf{COMPTIME} \;\; \text{tipo} \;\; \text{lista\_identificadores} \;\; \mathbf{;}$$

#### Reglas Implementadas en `gram.y`
```yacc
/* COMPTIME (Tema 22) */
declaracion_comptime
    : COMPTIME tipo lista_identificadores ';'
    | COMPTIME tipo lista_identificadores
        { yyerror("Falta ';' al final de la declaracion comptime"); }
    | COMPTIME lista_identificadores ';'
        { yyerror("Falta el tipo de dato en la declaracion comptime"); }
    | COMPTIME lista_identificadores
        {
            yyerror("Falta el tipo de dato en la declaracion comptime");
            yyerror("Falta ';' al final de la declaracion comptime");
        }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Modelado Mediante Técnica A Pura (Gramática de Error):
* La regla contempla de forma explícita la omisión del tipo tras la palabra clave `COMPTIME` (ej: `comptime x, y;` en lugar de `comptime shortint x, y;`).
* La reducción ejecuta directamente la acción semántica diagnóstica sin perturbar la pila de análisis ni descartar los identificadores declarados.

##### 2. Disyunción de Conjuntos FIRST:
* En este lenguaje, el no terminal `tipo` deriva únicamente en palabras reservadas (`SHORTINT` o `SINGLEF`), mientras que `lista_identificadores` deriva en `IDENTIFICADOR`:
  $$\text{FIRST}(\text{tipo}) = \{ \mathbf{SHORTINT}, \mathbf{SINGLEF} \}$$
  $$\text{FIRST}(\text{lista\_identificadores}) = \{ \mathbf{IDENTIFICADOR} \}$$
* Al ser conjuntos totalmente disjuntos ($\text{FIRST}(\text{tipo}) \cap \text{FIRST}(\text{lista\_identificadores}) = \emptyset$), el autómata LALR(1) con 1 solo token de anticipación distingue determinísticamente si tras `COMPTIME` se encuentra el tipo o si se omitió y comenzó la lista de identificadores.

##### 3. Combinación con Omisión de Punto y Coma:
* Se contempló la doble anomalía donde además de omitir el tipo se olvida el delimitador terminal punto y coma (`;`), emitiendo ambos diagnósticos de forma sucesiva sin desfasar el reconocimiento del siguiente bloque declarativo.
* La gramática mantiene estrictamente **0 conflictos Shift/Reduce y 0 Reduce/Reduce**.

#### Casos de Prueba Verificados
* `tests/test_comptime_sin_tipo.txt`:
  - Línea 7: `comptime x, y;` $\rightarrow$ Detecta `Error sintactico (linea 7): Falta el tipo de dato en la declaracion comptime`.
  - Línea 12: `comptime z` (sin `;` previo a `BEGIN`) $\rightarrow$ Detecta falta de tipo y falta de `;` al final de la declaración.

---

### Error 18: Ausencia de TO o de FROM en declaraciones EXPORT e IMPORT (Tema 26)

#### Enunciado de la Cátedra
> "19. Tema 26: Ausencia de to o de from en las declaración export e import respectivamente." (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{importacion\_opcional} \rightarrow \mathbf{IMPORT} \;\; \mathbf{FROM} \;\; \text{lista\_identificadores}$$
$$\text{exportacion\_opcional} \rightarrow \mathbf{EXPORT} \;\; \mathbf{TO} \;\; \text{lista\_identificadores}$$

#### Reglas Implementadas en `gram.y`
```yacc
importacion_opcional
    : /* vacio */
    | IMPORT FROM lista_identificadores
    | IMPORT lista_identificadores
        { yyerror("Falta la palabra clave 'FROM' en la declaracion IMPORT"); }
    ;

exportacion_opcional
    : /* vacio */
    | EXPORT TO lista_identificadores
    | EXPORT lista_identificadores
        { yyerror("Falta la palabra clave 'TO' en la declaracion EXPORT"); }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Modelado Mediante Técnica A Pura (Gramática de Error):
* Para `IMPORT`: si el programador escribe `class hija import base` omitiendo la palabra clave obligatoria `FROM`, la regla alternativa captura directamente la presencia de `IMPORT` seguido de la lista de identificadores.
* Para `EXPORT`: tanto en atributos como en métodos de clases (`shortint x export base;`), si se omite la palabra clave obligatoria `TO`, la regla alternativa absorbe la anomalía emitiendo el mensaje correspondiente.

##### 2. Determinismo en las Bifurcaciones de Lookahead:
* Tras desplazar `IMPORT`, el siguiente token solo puede ser `FROM` (camino canónico) o un `IDENTIFICADOR` (camino de error).
* Tras desplazar `EXPORT`, el siguiente token solo puede ser `TO` (camino canónico) o un `IDENTIFICADOR` (camino de error).
* Al no existir ambigüedad léxica ni solapamiento entre las palabras reservadas (`FROM`, `TO`) y los nombres de clases/paquetes (`IDENTIFICADOR`), el autómata LALR(1) decide la transición sin ningún conflicto.

##### 3. Cobertura Integral en Clases, Atributos y Métodos:
* La modularización de `exportacion_opcional` permite que el error se detecte de forma homogénea tanto al declarar atributos de instancia como al definir métodos de clase (sean ordinarios o de tipo `AUTO`), manteniendo **0 conflictos Shift/Reduce y 0 Reduce/Reduce**.

#### Casos de Prueba Verificados
* `tests/test_clases_import_export_extends.txt`:
  - Línea 21: `class errimport import base` $\rightarrow$ Detecta  
    `Error sintactico (linea 21): Falta la palabra clave 'FROM' en la declaracion IMPORT`.
  - Línea 35: `shortint c export hija;` (en atributo) $\rightarrow$ Detecta  
    `Error sintactico (linea 35): Falta la palabra clave 'TO' en la declaracion EXPORT`.
  - Línea 39: `END export hija;` (en método) $\rightarrow$ Detecta  
    `Error sintactico (linea 39): Falta la palabra clave 'TO' en la declaracion EXPORT`.

---

### Error 19: Ausencia de nombre o lista de clases después de EXTENDS (Tema 30)

#### Enunciado de la Cátedra
> "20. Tema 30: Ausencia de nombre o lista de clases después de extends" (`teoria/Errores a detectar.md`)

#### Estructura Canónica del Lenguaje
$$\text{herencia\_opcional} \rightarrow \mathbf{EXTENDS} \;\; \text{lista\_identificadores} \;\; \mathbf{;}$$

#### Reglas Implementadas en `gram.y`
```yacc
herencia_opcional
    : /* vacio */
    | EXTENDS lista_identificadores ';'
    | EXTENDS ';'
        { yyerror("Falta nombre o lista de clases a heredar luego de 'EXTENDS'"); }
    ;
```

#### Justificación Técnica y Defensa

##### 1. Modelado Mediante Técnica A Pura (Gramática de Error):
* Cuando el programador coloca la cláusula de herencia pero omite los nombres de las clases padre a extender (ej: `extends;`), el delimitador terminal punto y coma (`;`) aparece inmediatamente tras la palabra clave `EXTENDS`.
* Al modelar directamente `EXTENDS ';'`, el analizador reduce de forma inmediata y reporta el diagnóstico exacto.

##### 2. Preservación del Bloque de Miembros de Clase:
* Al absorber el error de forma limpia en `herencia_opcional`, la pila del parser no descarta tokens ni entra en modo de pánico.
* Esto asegura que el parser continúe de inmediato analizando los atributos y métodos subsiguientes de la clase (`miembros_clase`) sin emitir falsos positivos secundarios.

##### 3. Cero Conflictos en BYacc/J:
* Tras leer `EXTENDS`, si el lookahead es `IDENTIFICADOR` se procesa la lista de clases; si el lookahead es `;`, se toma la rama de error.
* La gramática completa mantiene estrictamente **0 conflictos Shift/Reduce y 0 Reduce/Reduce**.

#### Casos de Prueba Verificados
* `tests/test_clases_import_export_extends.txt`:
  - Línea 28: `extends;` dentro del cuerpo de la clase $\rightarrow$ Detecta  
    `Error sintactico (linea 28): Falta nombre o lista de clases a heredar luego de 'EXTENDS'`.







