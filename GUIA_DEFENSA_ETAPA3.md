# Guía de Estudio y Defensa Oral — Etapa 3: Tercetos y Semántica

**Materia:** Diseño de Compiladores I / Compiladores e Intérpretes  
**Grupo:** 20 · Ingeniería de Sistemas  
**Objetivo de este documento:** Bitácora pedagógica viva donde se registran todos los hitos de implementación, fundamentos teóricos, trazas del autómata, preguntas de los alumnos y respuestas modelo para rendir con máxima solvencia la defensa oral obligatoria ante los docentes de la cátedra.

---

## 🗺️ Mapa General de la Etapa 3 en Hitos Progresivos

```text
[Hito 1] Aritmética Completa y Propagación (COMPLETADO)
    │
    ▼
[Hito 2] Sentencia de Asignación, Tema 17 (a := b = (exp)) y pout (COMPLETADO)
    │
    ▼
[Hito 3] Ámbitos, Name Mangling con ":" y Tabla de Símbolos Enriquecida (COMPLETADO)
    │
    ▼
[Hito 4] Chequeo Estático de Tipos y Tema 34: Conversión Explícita tos() (COMPLETADO)
    │
    ▼
[Hito 5] Control de Flujo y Backpatching: IF, IF-ELSE y REPEAT UNTIL (Tema 12) (COMPLETADO)
    │
    ▼
[Hito 6] Funciones, Clases y Temas Avanzados (AUTO FUNCTION, COMPTIME, IMPORT/EXPORT, Herencia Múltiple) (SIGUIENTE)
```

---

## 📌 Hito 1: Núcleo Aritmético y Propagación de Atributos

### 1.1. Objetivo Técnico
Permitir que el compilador procese cualquier expresión aritmética arbitraria con operadores binarios (`+`, `-`, `*`, `/`) y constantes negativas (enteras `SHORTINT` y flotantes `SINGLEF`), emitiendo los tercetos en el orden matemático exacto impuesto por la precedencia y asociatividad.

---

### 1.2. Decisiones de Diseño y Preguntas Conceptuales

#### ❓ Pregunta 1: ¿Por qué en `numero_negativo` seteamos tanto `sval` como `ival` en `ParserVal`?
```java
ParserVal pv = new ParserVal("-" + lexema_pos);
pv.ival = id_neg;
$$ = pv;
```
* **Explicación para la defensa:**  
  La clase `ParserVal` provista por BYacc/J simula la unión de C, conteniendo campos independientes (`ival` para enteros, `sval` para cadenas, `dval` para números de punto flotante).  
  - En la Etapa 2 solo se utilizaba `$$.ival = id_neg;` para indexar el símbolo negativo en la Tabla de Símbolos. Si dejábamos `sval` sin inicializar, su valor por defecto era `null`.
  - En la Etapa 3, los generadores de tercetos de los operadores binarios (`+`, `-`, etc.) leen los operandos como cadenas de texto a través de `$1.sval` y `$3.sval`. Si `sval` fuera `null`, una expresión como `a + -5$s` generaría un terceto corrupto: `( + , a , null )`.
  - Al inicializar explícitamente `pv.sval = "-" + lexema_pos;` y `pv.ival = id_neg;`, garantizamos que el generador de código intermedio reciba la representación textual para el terceto y que la Tabla de Símbolos conserve el puntero para los futuros chequeos de tipos.

---

#### ❓ Pregunta 2: ¿Cómo sabe el compilador en qué orden exacto formar los tercetos? (`a + b * c` vs `a * b - c / d`)
* **Explicación para la defensa:**  
  **Nosotros no escribimos ningún algoritmo de ordenamiento ni condiciones `if` para priorizar operadores.** El orden temporal de creación de tercetos es consecuencia directa del mecanismo de **reducción del autómata de pila LALR(1)**.  
  Una acción semántica `{ ... }` solo se ejecuta cuando el autómata decide hacer un **REDUCE** (desapilar el lado derecho de la producción y apilar el no terminal del lado izquierdo).

##### Caso A: `a + b * c`
1. El parser reduce `a` a `expresion` y hace shift de `+`.
2. Lee `b`, lo reduce a `termino`. En la pila queda: `expresion '+' termino`.
3. **Lookahead (símbolo de preanálisis):** El autómata mira el siguiente token sin consumirlo: es `*`.
4. Como `*` pertenece a la regla `termino : termino '*' factor`, tiene mayor precedencia que la suma. Por lo tanto, el autómata **pospone la suma** y realiza un **SHIFT** del `*`.
5. Lee `c` y lo reduce a `factor`. Pila: `expresion '+' termino '*' factor`.
6. El siguiente token es `;`.
7. **Primer Reduce:** Se reduce `termino '*' factor`. **Se ejecuta la acción de multiplicación**.  
   👉 Se genera el **Terceto 0:** `( * , b , c )` y se sintetiza `"[0]"` en `termino`.
8. **Segundo Reduce:** Ahora la pila contiene `expresion '+' termino([0])`. Al encontrar el `;`, se reduce la suma.  
   👉 Se genera el **Terceto 1:** `( + , a , [0] )`.

##### Caso B: `a * b - c / d`
1. Lee `a * b`. El lookahead es `-`.
2. Como `-` pertenece a `expresion` y no a `termino`, el producto no puede extenderse más. El autómata ejecuta inmediatamente la reducción de `termino '*' factor`.  
   👉 Se genera el **Terceto 0:** `( * , a , b )` con resultado `"[0]"`.
3. `"[0]"` asciende a `expresion`. El parser hace shift de `-`.
4. Lee `c`, shift de `/`, lee `d`. El lookahead es `;`.
5. La división tiene prioridad para cerrarse antes de la resta. Reduce `c / d`.  
   👉 Se genera el **Terceto 1:** `( / , c , d )` con resultado `"[1]"`.
6. Finalmente, con la pila en `expresion([0]) '-' termino([1])`, reduce la resta.  
   👉 Se genera el **Terceto 2:** `( - , [0] , [1] )`.

---

#### ❓ Pregunta 3: ¿Por qué en las reglas de error sintáctico NO generamos tercetos? ¿El compilador se detiene o continúa?
* **Explicación para la defensa:**  
  1. **El compilador NO se detiene:** Sigue analizando el código para encontrar y reportar al usuario todos los errores sintácticos y semánticos que existan en el archivo de una sola pasada.
  2. **No se emiten tercetos de código erróneo:** Los tercetos son una representación intermedia de instrucciones ejecutables destinadas a traducirse a código de máquina (Assembler) en el TP4. Emitir tercetos para sentencias rotas (por ejemplo `a + ;`) generaría instrucciones con operandos nulos o basura que harían fallar al generador de código posterior.
  3. **Misión de la regla de error:** Capturar la falla, emitir el mensaje con número de línea (`yyerror(...)`) y absorber el fallo para recuperar la sincronización del autómata sin romper la pila. Si se detectan errores, el compilador debe concluir reportando que la compilación fracasó, impidiendo la emisión de binarios.

---

#### ❓ Pregunta 4: ¿Por qué escribimos explícitamente `{ $$ = $1; }` en reglas unitarias como `expresion : termino` si BYacc/J lo hace por defecto?
```yacc
expresion
    : termino
        {
            $$ = $1;
        }
    ;
```
* **Explicación para la defensa:**  
  Es cierto que en Yacc, si una regla no tiene ningún bloque de acción, por defecto se ejecuta `$$ = $1;`. Sin embargo, explicitarlo obedece a tres principios fundamentales de ingeniería de compiladores:
  1. **Intención explícita en la defensa:** Demuestra al docente que el alumno conoce conscientemente cómo viajan los atributos sintetizados en el árbol y que no es una omisión accidental.
  2. **Prevención de bugs clásicos de Yacc:** En BYacc/J, si una regla no tiene acción, hace `$$ = $1;`. Pero si más adelante agregamos una sentencia de depuración como `{ System.out.println("debug"); }` y olvidamos reasignar `$$ = $1;`, Yacc **deja de hacer `$$ = $1;` automáticamente**, perdiendo el atributo y dejando `$$` nulo. Escribirlo explícitamente protege la regla ante futuras modificaciones.
  3. **Preparación para chequeos semánticos (Hito 4):** En las reglas unitarias pronto deberemos propagar o validar tipos (`$$.tipo = $1.tipo`), por lo que contar con el bloque formalmente definido facilita la extensión sin alterar la gramática.

---

### 1.3. Casos de Prueba Verificados
Archivo de prueba ejecutado: `tests/test_expresiones_aritmeticas.txt`

```pascal
programa_aritmetica

shortint a, b, c, d, res;
singlef x, y, z;
BEGIN
  res := a + b * c;
  res := a * b - c / d;
  res := a + -5$s;
  z := x * 2.5 + y;
  z := z - -1.0;
END
```

**Salida de tercetos obtenida:**
```text
0 ( * , b , c )
1 ( + , a , [0] )
2 ( := , res , [1] )
3 ( * , a , b )
4 ( / , c , d )
5 ( - , [3] , [4] )
6 ( := , res , [5] )
7 ( + , a , -5 )
8 ( := , res , [7] )
9 ( * , x , 2.5 )
10 ( + , [9] , y )
11 ( := , z , [10] )
12 ( - , z , -1.0 )
13 ( := , z , [12] )
```
*Resultado:* Cero conflictos en `y.output` (0 shift/reduce, 0 reduce/reduce) y evaluación estricta de la precedencia matemática.

---

## 📌 Hito 2: Sentencia de Asignación, Tema 17 (`a := b = (exp)`) y Sentencia `pout`

### 2.1. Objetivo Técnico
Completar la emisión de tercetos para:
1. La sentencia de asignación tradicional: `IDENTIFICADOR ASIGNAR expresion ';'`.
2. Las asignaciones de expresiones en expresiones (**Tema 17** del Grupo 20): `IDENTIFICADOR '=' '(' expresion ')'`.
3. La sentencia de salida por consola: `POUT '(' CADENA ')'` y `POUT '(' expresion ')'`.

---

### 2.2. Decisiones de Diseño y Preguntas Conceptuales

#### ❓ Pregunta 1: ¿Cómo funciona el Tema 17 (asignación en expresión) y por qué se modela como un `factor`?
* **Regla gramatical en `gram.y`:**
  ```yacc
  /* Tema 17: Asignacion en expresion */
  unica
      : IDENTIFICADOR '=' '(' expresion ')'
          {
              int ref = $1.ival;
              String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
              int idTerceto = GeneradorTerceto.crearTerceto(":=", lexema, $4.sval);
              $$ = new ParserVal("[" + idTerceto + "]");
          }
      ;
  ```
* **Explicación para la defensa:**  
  Una asignación tradicional es una **sentencia** (no devuelve un valor utilizable por otra expresión). Sin embargo, en el Tema 17, la sintaxis `id = (exp)` es una **expresión con efecto secundario** (side-effect):
  1. **Efecto secundario:** Guarda el valor calculado en la variable local (`lexema := $4.sval`), emitiendo de inmediato el terceto de asignación.
  2. **Valor resultante:** Sintetiza hacia arriba el índice de su propio terceto `$$ = new ParserVal("[" + idTerceto + "]");`.
  3. Al ser una alternativa de `factor`, puede participar como operando en sumas, productos o asignaciones externas (por ejemplo `a := b = (2$s);` o `res := a + b = (4$s);`), garantizando que la asignación interna se ejecute **antes** de que su resultado sea consumido por la operación contenedora.

---

#### ❓ Pregunta 2: En la sentencia `a := b = (2$s);`, ¿cuántos tercetos se generan y en qué orden?
* **Explicación para la defensa:**  
  Se generan **dos tercetos**:
  ```text
  4 ( := , b , 2 )
  5 ( := , a , [4] )
  ```
  **Mecánica en el autómata LALR:**  
  1. Al llegar a `b = (2$s)`, el parser reconoce la regla `unica : IDENTIFICADOR '=' '(' expresion ')'`.  
  2. Al reducir `unica`, se ejecuta su acción semántica, creando el terceto `4 ( := , b , 2 )` y devolviendo como atributo sintetizado `[4]`.  
  3. `[4]` asciende por `factor -> termino -> expresion`.  
  4. Finalmente, se reduce la asignación externa `asignacion : a := expresion`, la cual toma el valor `$3.sval` (que es `[4]`) y emite el terceto `5 ( := , a , [4] )`.

---

#### ❓ Pregunta 3: En sentencias unarias como `pout`, ¿qué se coloca en el segundo operando del terceto?
```java
GeneradorTerceto.crearTerceto("POUT", argumento, "-");
```
* **Explicación para la defensa:**  
  Un terceto, por definición de diseño, tiene exactamente 3 componentes: `(operador, op1, op2)`.  
  Cuando una operación es unaria o de entrada/salida (como `POUT`, la conversión `tos` o un retorno `RET`), el segundo operando no existe en la semántica del lenguaje.  
  Siguiendo la convención clásica de la literatura de compiladores (Aho-Ullman / Dragon Book) y el plan de la cátedra, se utiliza el símbolo guión `"-"` (o `null`/`"_"`) para denotar explícitamente que la operación es unaria y que el segundo slot no se utiliza.

---

#### ❓ Pregunta 4: ¿Cómo se manejan las cadenas multilínea (Tema 10) al pasarlas a `pout`?
* **Explicación para la defensa:**  
  En el analizador léxico (`AS12`), cuando se reconoce el cierre de una cadena multilínea delimitada por comillas dobles `"..."`:
  1. Se eliminan los saltos de línea (`\r`, `\n`) tal como exige textualmente el enunciado del Tema 10.
  2. Se registra la cadena normalizada en la `TablaSimbolos` como constante.
  3. Se entrega su ID al token `CADENA`.
  4. En la reducción de `sentencia_pout : POUT '(' CADENA ')'`, el parser toma el lexema de la cadena desde la Tabla de Símbolos y emite el terceto `( POUT , "..." , - )`.

---

#### ❓ Pregunta 5: Cuando hacemos `a := 4;`, ¿generamos el terceto `(:=, a, 4)` y TAMBIÉN guardamos el atributo `VALOR = 4` en la Tabla de Símbolos?
* **Explicación para la defensa:**  
  **¡NO para variables comunes!** Solo se genera el terceto `( := , a , 4 )`.  
  **La diferencia fundamental entre Compilador e Intérprete:**  
  1. Un **intérprete** ejecuta el programa en tiempo real, por lo que necesita mantener en memoria el valor actualizado de cada variable a medida que lee las sentencias.  
  2. Un **compilador**, en cambio, **no ejecuta el programa**: solo lo traduce a un formato ejecutable. La instrucción `( := , a , 4 )` es una orden diferida que en el TP4 se convertirá en código Assembler (`MOV _a, 4`) para que la CPU modifique la celda de memoria en **tiempo de ejecución (Runtime)**.  
  3. En un programa real, el valor de una variable puede cambiar dinámicamente según la entrada del usuario o bifurcaciones condicionales (por ejemplo dentro de un `IF` o un bucle `REPEAT`), por lo que en tiempo de compilación es imposible predecir o fijar un único valor en la Tabla de Símbolos.  
  4. **¿Qué atributos SÍ guarda una variable común en la Tabla de Símbolos?**  
     - `LEXEMA`: nombre de la variable (`a`).  
     - `TIPO`: tipo de dato (`SHORTINT`, `SINGLEF`).  
     - `USO`: rol del identificador (`variable`, `parametro`, etc.).  
     - `AMBITO`: contexto de visibilidad (`:main`).  
  5. **La única excepción del Grupo 20: Tema 22 (`COMPTIME`):**  
     Las variables declaradas como `COMPTIME SHORTINT c;` son calculadas estáticamente por el compilador. En ese caso particular, **sí** se almacena su valor constante en la Tabla de Símbolos y **no** se genera terceto de ejecución en runtime, reemplazando la variable por su valor literal en cada lugar donde se use.

---

#### ❓ Pregunta 6: ¿Cómo evitamos generar tercetos corruptos con `null` cuando una expresión falla sintácticamente?
* **Explicación para la defensa:**  
  Cuando ocurre un error sintáctico (por ejemplo en `a := b := (x);` del Tema 17 donde se usa erróneamente `:=`), la regla de recuperación reporta el error con `yyerror(...)` pero no sintetiza ningún atributo en `$$`.  
  Si la regla contenedora (`asignacion` o un operador binario) invocara ciegamente a `crearTerceto`, recibiría un operando en `null` y emitiría instrucciones inválidas como `( := , a , null )`.  
  Para garantizar la premisa de la cátedra de **no emitir tercetos de sentencias erróneas**, las acciones semánticas incorporan guardas defensivas:
  ```java
  if ($3 != null && $3.sval != null) {
      GeneradorTerceto.crearTerceto(":=", lexema, $3.sval);
  }
  ```
  De este modo, el compilador continúa analizando el resto del archivo para encontrar más errores, pero descarta la emisión de código intermedio de las sentencias malformadas.

---

### 2.3. Caso de Prueba Verificado
Archivo de prueba: `tests/test_asignaciones_y_pout.txt`

```pascal
programa_asignaciones

shortint a, b, res;
singlef x;
BEGIN
  a := 10$s;
  b := a + 5$s;
  x := 3.14;
  a := b = (2$s);
  res := a + b = (4$s);
  pout("Resultado calculado exitosamente");
  pout(res);
  pout(a + b * 2$s);
END
```

**Salida de tercetos obtenida:**
```text
0 ( := , a , 10 )
1 ( + , a , 5 )
2 ( := , b , [1] )
3 ( := , x , 3.14 )
4 ( := , b , 2 )
5 ( := , a , [4] )
6 ( := , b , 4 )
7 ( + , a , [6] )
8 ( := , res , [7] )
9 ( POUT , "Resultado calculado exitosamente" , - )
10 ( POUT , res , - )
11 ( * , b , 2 )
12 ( + , a , [11] )
13 ( POUT , [12] , - )
```
*Validación:* Cero conflictos en BYacc/J, precedencia impecable en asignaciones encadenadas y salida de consola completa.

---

## 📌 Hito 3: Ámbitos, Name Mangling con `:` y Tabla de Símbolos Enriquecida (COMPLETADO)

### 3.1. Objetivo Técnico
1. Implementar la gestión de ámbitos jerárquicos durante el parseo (ámbito global `:main`, funciones `:main:f1`, clases `:main:ClaseA`, etc.).
2. Implementar el esquema estricto de **Name Mangling** exigido por la cátedra (`Tp3.pdf`, pág. 2) utilizando exclusivamente el separador `:`.
3. Registrar metadatos semánticos completos en la `TablaSimbolos`: `LEXEMA`, `MANGLING`, `TIPO`, `USO` y `AMBITO`.
4. Implementar los dos primeros chequeos semánticos obligatorios:
   - Detección de variable **no declarada o no visible** en el ámbito actual.
   - Detección de variable **redeclarada en el mismo ámbito** (permitiendo *shadowing* en ámbitos diferentes).
5. Emitir los tercetos utilizando los nombres mangledos (`a:main`, `a:main:sumar`) para eliminar cualquier ambigüedad en el código intermedio.

---

### 3.2. Decisiones de Diseño y Preguntas Conceptuales

#### ❓ Pregunta 1: ¿Por qué la cátedra exige Name Mangling con `:` y cómo se estructuran las claves?
* **Explicación para la defensa:**  
  En un lenguaje con funciones, procedimientos o clases, dos identificadores pueden tener el mismo nombre léxico (por ejemplo, `a` global y `a` local a una función). Si el compilador usara solo `"a"` en el código intermedio o en el Assembler final, se producirían colisiones destructivas en memoria.  
  El **Name Mangling** (decoración de nombres) transforma el identificador relativo en un nombre absoluto unívoco concatenando la ruta de ámbitos:
  - En el programa principal: `a + :main` → `a:main`.
  - En la función `sumar`: `a + :main:sumar` → `a:main:sumar`.
  - En un método de clase: `a + :main:ClaseA:metodo1` → `a:main:ClaseA:metodo1`.  
  El `Tp3.pdf` exige taxativamente utilizar `:` como separador.

---

#### ❓ Pregunta 2: ¿Cómo funciona la resolución jerárquica ascendente de identificadores? ¿Qué pasa con el *shadowing*?
* **Explicación para la defensa:**  
  El método `TablaSimbolos.buscarIdentificador(nombre, ambitoActual)` implementa la regla de visibilidad estática (alcance léxico):
  1. **Nivel local:** Primero busca el identificador con el ámbito más específico (ej. `x:main:sumar`). Si existe, lo retorna de inmediato. Esto garantiza el **shadowing**: la variable local oculta a la global con el mismo nombre.
  2. **Ascenso jerárquico:** Si no lo encuentra, recorta el último segmento del ámbito (`:sumar`) mediante `ambito.lastIndexOf(':')` y vuelve a buscar en el ámbito contenedor (`x:main`).
  3. **Límite superior:** El proceso continúa hasta agotar el ámbito raíz `:main`.
  4. **Fallo semántico:** Si no se encuentra en ningún nivel, emite el error semántico:  
     `Error semantico (linea X): Variable 'x' no declarada o no visible en el ambito actual (:main:sumar)`.

---

#### ❓ Pregunta 3: ¿Cómo interactúa el léxico con la Tabla de Símbolos enriquecida sin perder identificadores ni duplicar erróneamente?
* **Explicación para la defensa:**  
  1. En el análisis léxico (`AS3`), el autómata no conoce el contexto gramatical ni el ámbito. Solo sabe que leyó la cadena `"a"`. Si `"a"` no existe en la tabla, la inserta como entrada cruda (`{LEXEMA=a}`).
  2. Al llegar a la producción de declaración (`declaracion_variables`), el sintáctico conoce el tipo (`SHORTINT`), el uso (`variable`) y el ámbito activo (`ambitoActual`).  
     Llama a `TablaSimbolos.declararVariable(...)`:
     - Si ya existe una entrada con ese mismo `MANGLING`, detecta y reporta **redeclaración**.
     - Si no está redeclarada, busca si hay una entrada cruda del léxico para reusarla y enriquecerla con sus metadatos (`MANGLING`, `TIPO`, `USO`, `AMBITO`).
     - Si una variable con el mismo lexema se declara en otro ámbito (shadowing), se crea una **nueva entrada independiente** en la tabla con su respectivo `MANGLING` único.

---

#### ❓ Pregunta 4: ¿Por qué en `lista_variables` usamos `ArrayList<String>` en el campo `obj` de `ParserVal`?
* **Explicación para la defensa:**  
  En gramáticas LALR como la nuestra, la regla es:
  `declaracion_variables : tipo lista_variables ';'`
  Debido a que el parsing es ascendente (bottom-up), `lista_variables` se reduce **antes** de que la regla de `declaracion_variables` conozca la lista completa de identificadores.  
  Para transportar la lista de identificadores hacia arriba en el árbol:
  - En `lista_variables : IDENTIFICADOR`, instanciamos una lista `ArrayList<String>` y la guardamos en `$$.obj`.
  - En la recursión `lista_variables : lista_variables ',' IDENTIFICADOR`, agregamos cada identificador a esa misma lista.
  - Al reducir `declaracion_variables`, tomamos el tipo desde `$1.sval` y recorremos la lista `$2.obj` con un bucle `for` tradicional, registrando cada variable con su tipo y ámbito correspondiente.

---

#### ❓ Pregunta 5: ¿Por qué usamos `LinkedHashMap` en `TablaSimbolos` en lugar de un `HashMap` común si ambos se iteran con un `for`?
* **Explicación para la defensa:**  
  Un `HashMap` común no garantiza el orden en que se devuelven los elementos al iterar su `entrySet()`, ya que la secuencia depende de los buckets de hash y de los rehashes dinámicos. En la arquitectura de compiladores, el orden de inserción determinístico es crítico por dos razones:
  1. **Tema 29 (Acceso posicional a atributos de clases):** La cátedra exige acceder a los atributos de un objeto por su número ordinal (`obj[0$s]`, `obj[1$s]`), el cual mapea al **orden en que fueron declarados en el código fuente**. Con `LinkedHashMap`, los atributos se registran y consultan exactamente en su orden de declaración.
  2. **Determinismo en correcciones automáticas:** La cátedra compara las salidas volcadas de la Tabla de Símbolos contra patrones esperados. `LinkedHashMap` garantiza una salida estable, secuencial y reproducible (`1, 2, 3...`) en todas las ejecuciones.

---

#### ❓ Pregunta 6: ¿Por qué se eliminó el parámetro `linea` de `declararVariable`?
* **Explicación para la defensa:**  
  Originalmente la firma era `declararVariable(String nombre, String tipo, String uso, String ambito, int linea)`.  
  Sin embargo, el método `Parser.agregarErrorSemantico(String s)` consulta directamente la línea en curso llamando a `AnalizadorLexico.getLineaActual()`. Mantener el parámetro `linea` en la firma de `declararVariable` era código muerto y redundante. Al eliminarlo, simplificamos la interfaz pública y mantuvimos una única fuente de verdad para la línea actual.

---

#### ❓ Pregunta 7: ¿Está bien que en las clases la regla `miembros_clase` permita intercalar atributos y métodos en cualquier orden?
* **Explicación para la defensa:**  
  **Sí, es 100% intencional y está alineado con la especificación de la cátedra.**  
  En la gramática oficial de referencia (`teoria/grupo20.y`, líneas 102-112), la regla está definida recursivamente:
  ```yacc
  miembros_clase : /* vacio */ | miembros_clase miembro_clase ;
  miembro_clase  : atributo_clase | metodo_clase | herencia_opcional ;
  ```
  Al igual que en Java o C++, los atributos y métodos pueden declararse en cualquier secuencia según la necesidad del programador. La regla recursiva por izquierda en LALR(1) procesa cualquier intercalación con cero conflictos Shift/Reduce.

---

#### ❓ Pregunta 8: ¿Por qué en `declaracion_comptime` se chequea `if (id != LEXEMA_NO_ENCONTRADO)` si parece que nunca fallaría?
* **Explicación para la defensa:**  
  Es un **guard semántico indispensable**.  
  Si el programador comete un error de redeclaración en el código fuente (por ejemplo: `SHORTINT x; COMPTIME SHORTINT x;`), la llamada a `declararVariable` detecta la colisión de mangling en el mismo ámbito, emite el error semántico de redeclaración y retorna `LEXEMA_NO_ENCONTRADO` (`-1`).  
  El chequeo `if (id != TablaSimbolos.LEXEMA_NO_ENCONTRADO)` previene que el compilador intente ejecutar `TablaSimbolos.agregarAtributo(-1, "COMPTIME", "true")` sobre una declaración rechazada.

---

#### ❓ Pregunta 9: ¿Cómo se simplifica `buscarIdentificador` usando `contains(":")` en el `while` y por qué no hacen falta los `break`?
* **Explicación para la defensa:**  
  El método `buscarIdentificador` escala iterativamente en el árbol de ámbitos podando el último segmento con `lastIndexOf(':')`:
  ```java
  while (ambitoBusqueda != null && ambitoBusqueda.contains(":")) {
      ...
      int ultimoSeparador = ambitoBusqueda.lastIndexOf(':');
      ambitoBusqueda = ambitoBusqueda.substring(0, ultimoSeparador);
  }
  ```
  **El fundamento del invariante:** Todo ámbito válido en nuestra arquitectura comienza con `:` (la raíz es `:main`).  
  Cuando la búsqueda llega a la raíz `:main`, el último `:` se encuentra en el índice `0`. La operación `substring(0, 0)` produce una cadena vacía `""`.  
  En la siguiente evaluación del bucle, `"".contains(":")` evalúa a `false`, terminando el ciclo de forma totalmente natural sin requerir ningún `break` artificial ni condiciones duplicadas.

---

#### ❓ Pregunta 10: ¿Por qué guardamos `ival` en `ParserVal` en las reglas de `factor`? ¿Cuándo se usa?
* **Explicación para la defensa:**  
  En `factor : IDENTIFICADOR`, `CTE_SHORTINT`, `CTE_SINGLEF` y `numero_negativo`:
  1. **Uso inmediato:** En `numero_negativo : '-' CTE_SHORTINT`, se utiliza `$2.ival` para consultar la Tabla de Símbolos y convertir la constante a su versión negativa con `convertirANegativo`.
  2. **Cimiento para el Hito 4 (Chequeo Estático de Tipos):** En operaciones binarias (`expresion '+' termino`), el compilador necesita validar la compatibilidad de tipos entre operandos. Si `factor` descartara el `ival` y solo retornara el texto en `sval`, el analizador de tipos tendría que volver a iterar y buscar en toda la tabla. Al propagar `pv.ival = idVar;`, las reglas superiores tienen acceso directo en $O(1)$ a los metadatos semánticos (`TablaSimbolos.obtenerAtributo($1.ival, TablaSimbolos.TIPO)`).

---

#### ❓ Pregunta 11: ¿Por qué en `encabezado_funcion` se declara la función ANTES de invocar a `entrarAmbito`?
* **Explicación para la defensa:**  
  Si invocáramos `entrarAmbito("sumar")` antes de registrar la función en la Tabla de Símbolos:
  - La función se declararía con el mangling `sumar:main:sumar` (dentro de su propio ámbito local).
  - Como consecuencia, ¡la función viviría encerrada dentro de sí misma y desde el programa principal `:main` nadie podría verla ni invocarla!  
  El orden estricto de una compilación de una pasada es:
  1. Declarar el símbolo de la función en el ámbito contenedor actual (`:main`).
  2. Descender al nuevo ámbito (`:main:sumar`) para que los parámetros formales y las variables locales queden contenidos en él.

---

#### ❓ Pregunta 12: ¿Por qué en `lista_variables` usamos recursión por izquierda y qué riesgo tendría escribirla por derecha?
* **Explicación para la defensa:**  
  - **Recursión por izquierda (`lista_variables ',' IDENTIFICADOR`):** En un autómata ascendente (bottom-up / LALR), cada vez que se lee una coma y un identificador, el parser ejecuta un `Reduce` inmediato. La pila del autómata se mantiene en tamaño constante $O(1)$ sin importar si se declaran 3 o 10.000 variables.
  - **Recursión por derecha (`IDENTIFICADOR ',' lista_variables`):** El autómata no puede reducir hasta conocer el final de la lista. Está obligado a hacer `Shift` de todos los identificadores y comas, consumiendo memoria proporcional a la cantidad de variables ($O(N)$ en la pila). Si la lista es extensa, provoca un **desbordamiento de la pila del parser** (`StackOverflow`). Por eso en Yacc las listas siempre se definen por izquierda.

---

#### ❓ Pregunta 13: ¿Por qué en BYacc/J asignamos `$$ = new ParserVal();` en lugar de `$$ = null;` ante un error semántico o sintáctico?
* **Explicación para la defensa:**  
  1. **El peligro de propagar un `ParserVal` válido con el lexema crudo:** Si una variable no está declarada y se devuelve un `ParserVal` con el lexema crudo y referencia del léxico, las operaciones superiores (`expresion '+' termino`, `asignacion`) ven operandos no nulos y generan tercetos inválidos con variables inexistentes (ej. `( + , z , 5 )`).
  2. **El motivo técnico de no usar `null` de Java:** En BYacc/J, antes de ejecutar la acción semántica de una regla con longitud mayor a cero, el autómata ejecuta:
     ```java
     yyval = dup_yyval(val_peek(yym - 1));
     ```
     Donde `dup_yyval(val)` accede directamente a `val.ival`, `val.sval`, etc. Si una acción semántica previa asignara `$$ = null;`, el valor apilado en la pila semántica sería `null`, provocando un `NullPointerException` en la siguiente reducción que intente duplicarlo.
  3. **La solución canónica:** Asignar una instancia vacía: `$$ = new ParserVal();`. Un `ParserVal` instanciado por defecto tiene `sval = null`. De esta forma, la pila del autómata nunca contiene referencias nulas (evitando el fallo en `dup_yyval`), pero todas las guardas superiores (`if ($3 != null && $3.sval != null)`) detectan que el valor sintetizado no es computable y bloquean limpiamente la creación de tercetos corruptos.

---

#### ❓ Pregunta 14: En operaciones binarias (`+`, `-`, `*`, `/`), ¿por qué es indispensable el bloque `else { $$ = new ParserVal(); }`?
* **Explicación para la defensa:**  
  En BYacc/J, antes de ejecutar la acción semántica de una regla, el autómata asigna por defecto `$$ = $1;`.  
  Si el bloque `if ($1 != null && $1.sval != null && $3 != null && $3.sval != null)` evalúa a `false` (porque `$3` o `$1` provinieron de un operando inválido o no declarado) y no colocamos un bloque `else`:
  - `$$` conserva intacto el valor de `$1` (`expresion` o `termino`).
  - Esto enmascara silenciosamente el fallo y transforma una operación rota (como `a + <error>`) en únicamente el operando izquierdo `a`.  
  - En consecuencia, una asignación como `resultado := a + ;` reduciría a `resultado := a;` y emitiría un terceto espurio.  
  Colocar explícitamente `else { $$ = new ParserVal(); }` (así como en las reglas de recuperación de error sintáctico) garantiza que la expresión resultante quede anulada (`sval = null`), impidiendo que la sentencia contenedora emita código intermedio.

---

#### ❓ Pregunta 15: En `CTE_SHORTINT`, ¿por qué se sigue ejecutando el resto de la acción tras el `if` de error de rango?
* **Explicación para la defensa:**  
  El bloque `if (valor > AnalizadorLexico.ValorMaximoInt) { yyerror(...); }` no contiene `return` ni `System.exit()`.  
  Esto responde a la exigencia de **recuperación no fatal de errores** de la cátedra: el compilador debe informar el error sintáctico con su línea real, pero debe continuar analizando el resto del programa para descubrir y listar todos los errores del archivo en una sola pasada. Al continuar y sintetizar la constante, el parser puede seguir validando la estructura sintáctica de la sentencia sin abortar prematuramente.

---

### 3.3. Casos de Prueba Verificados

#### Caso 1: Ámbitos y Shadowing (`tests/test_hito3_ambitos.txt`)
```pascal
programa_ambitos

shortint a, b;

shortint FUNCTION sumar(shortint x)
  shortint a, res;
BEGIN
  a := 5$s;
  res := a + x;
  RET(res);
END;

BEGIN
  a := 10$s;
  b := a;
  pout(b);
END
```

**Salida obtenida:**
```text
Programa reconocido correctamente
=== TABLA DE SIMBOLOS ===
1: {USO=nombre_programa, TIPO=programa, MANGLING=programa_ambitos:main, AMBITO=:main, LEXEMA=programa_ambitos}
2: {USO=variable, TIPO=SHORTINT, MANGLING=a:main, AMBITO=:main, LEXEMA=a}
3: {USO=variable, TIPO=SHORTINT, MANGLING=b:main, AMBITO=:main, LEXEMA=b}
4: {USO=funcion, TIPO=SHORTINT, MANGLING=sumar:main, AMBITO=:main, LEXEMA=sumar}
5: {USO=parametro, TIPO=SHORTINT, MANGLING=x:main:sumar, AMBITO=:main:sumar, LEXEMA=x}
6: {USO=variable, TIPO=SHORTINT, MANGLING=res:main:sumar, AMBITO=:main:sumar, LEXEMA=res}
7: {USO=variable, TIPO=SHORTINT, MANGLING=a:main:sumar, AMBITO=:main:sumar, LEXEMA=a}
8: {LEXEMA=5, OCURRENCIAS=1}
9: {LEXEMA=10, OCURRENCIAS=1}

----------------------------------------
         CODIGO INTERMEDIO (TERCETOS)   
----------------------------------------
0 ( := , a:main:sumar , 5 )
1 ( + , a:main:sumar , x:main:sumar )
2 ( := , res:main:sumar , [1] )
3 ( := , a:main , 10 )
4 ( := , b:main , a:main )
5 ( POUT , b:main , - )
----------------------------------------
```
*Validación:* Coexistencia de `a:main` y `a:main:sumar` sin colisión; tercetos emitidos con nombres mangledos exactos.

---

#### Caso 2: Chequeos Semánticos Obligatorios y Supresión de Tercetos Inválidos (`tests/test_hito3_errores_semanticos.txt`)
```pascal
programa_errores_semanticos

shortint a, b;
shortint a; // Error semantico: redeclaracion en el mismo ambito

BEGIN
  a := 10$s;
  z := 20$s; // Error semantico: z no declarada
  b := z + 5$s; // Error semantico: z no declarada
END
```

**Salida obtenida:**
```text
Error semantico (linea 4): Redeclaracion de 'a' en el ambito :main
Error semantico (linea 8): Variable 'z' no declarada o no visible en el ambito actual (:main)
Error semantico (linea 9): Variable 'z' no declarada o no visible en el ambito actual (:main)
Compilacion finalizada con 0 error(es) sintactico(s) y 3 error(es) semantico(s)

----------------------------------------
         CODIGO INTERMEDIO (TERCETOS)   
----------------------------------------
0 ( := , a:main , 10 )
----------------------------------------
```
*Validación:* Detección taxativa de redeclaraciones en el mismo ámbito y variables no declaradas. Se reportan los 3 errores con su número de línea real y **únicamente** se emite el terceto de la sentencia válida (`0 ( := , a:main , 10 )`), suprimiendo completamente la creación de tercetos para `z := 20$s` y `b := z + 5$s`.

---

## 📌 Hito 4: Chequeo Estático de Tipos y Tema 34: Conversión Explícita `tos()`

### 4.1. Objetivo Técnico
1. **Asociar tipos en la Tabla de Símbolos:** Garantizar que todas las constantes (`CTE_SHORTINT`, `CTE_SINGLEF` y sus variantes negativas) registren su atributo `TIPO` en `TablaSimbolos` (`"SHORTINT"` o `"SINGLEF"`).
2. **Propagación de tipos en el autómata LALR(1):** Propagar el tipo resultante a través de atributos sintetizados utilizando el campo `obj` de `ParserVal` en `factor`, `termino` y `expresion`.
3. **Validación estricta en operaciones aritméticas binarias (`+`, `-`, `*`, `/`):**
   - Operaciones homogéneas (`SHORTINT op SHORTINT` o `SINGLEF op SINGLEF`): válidas, emiten terceto con el tipo resultante.
   - Operaciones heterogéneas (`SHORTINT op SINGLEF` o viceversa): prohibidas sin conversión explícita previa; emiten error semántico y bloquean la emisión del terceto.
4. **Validación estricta en sentencias de asignación (`:=` y Tema 17 `= (exp)`):**
   - Variable y expresión del mismo tipo: válidas, emiten terceto de asignación.
   - Asignar `SINGLEF` a `SHORTINT`: error semántico específico indicando que requiere la conversión explícita `tos`.
   - Asignar `SHORTINT` a `SINGLEF`: error semántico por tipos incompatibles (el Grupo 20 tiene asignado el Tema 34 y no el Tema 35 de conversiones implícitas).
5. **Implementación del Tema 34 (`tos`):**
   - Reconocer la función intrínseca `tos(expresion)`.
   - Validar que la expresión interna sea de tipo `SINGLEF`.
   - Emitir el terceto de conversión `( tos , op , - )` y sintetizar hacia arriba el tipo `"SHORTINT"`.
6. **Validación en comparaciones (`condicion`):**
   - Prohibir comparaciones entre operandos de distinto tipo sin conversión explícita `tos`.

---

### 4.2. Decisiones de Diseño y Preguntas Conceptuales

#### ❓ Pregunta 1: ¿Por qué el chequeo de tipos se realiza durante la reducción de BYacc/J y no en una pasada posterior sobre un AST?
* **Explicación para la defensa:**  
  1. **Enfoque arquitectónico de la cátedra:** El enunciado del TP3 establece textualmente: *"Para Tercetos y Árbol Sintáctico, los chequeos de tipos se deben efectuar durante la generación de código intermedio."*  
  2. **Traducción dirigida por la sintaxis (Syntax-Directed Translation - SDT) en una sola pasada:** Nuestro compilador no construye un Árbol de Sintaxis Abstracta (AST) explícito en memoria, sino que traduce directamente el flujo de tokens a una secuencia de tercetos a medida que el autómata de pila LALR(1) reduce cada producción.
  3. **Invariante en la reducción:** Cada vez que el parser reduce una regla como `expresion : expresion '+' termino`, tiene en el tope de la pila semántica los atributos de los hijos (`$1` y `$3`). Verificar los tipos en ese instante exacto permite detener inmediatamente la creación de código intermedio inválido antes de que el terceto se incorpore a la lista global.

---

#### ❓ Pregunta 2: ¿Cómo viaja la información de tipos por la pila de BYacc/J si `ParserVal` no tiene un campo `tipo` nativo?
* **Explicación para la defensa:**  
  La clase `ParserVal` provista por BYacc/J simula la unión de C mediante cuatro campos:
  - `ival` (entero): usado para índices a la Tabla de Símbolos.
  - `dval` (flotante).
  - `sval` (String): usado para transportar la referencia textual del operando (`"a:main"`, `"5"`, `"[0]"`).
  - `obj` (`Object`): campo genérico para estructuras arbitrarias.
  
  **Nuestra solución:** Utilizamos el campo `pv.obj` para transportar la cadena con el tipo canónico (`"SHORTINT"` o `"SINGLEF"`):
  - En `factor : IDENTIFICADOR` se consulta la Tabla de Símbolos y se asigna `pv.obj = tipo`.
  - En `factor : CTE_SHORTINT` y `numero_negativo` se asigna `pv.obj = "SHORTINT"`.
  - En `factor : CTE_SINGLEF` y `numero_negativo` se asigna `pv.obj = "SINGLEF"`.
  - En las reglas unitarias (`termino : factor`, `expresion : termino`), BYacc/J ejecuta `$$ = $1;`, lo que propaga automáticamente la referencia completa con su `obj` intacto sin costo de memoria adicional.

---

#### ❓ Pregunta 3: ¿Por qué nuestro compilador rechaza asignar un entero a un flotante (`singlef := shortint`) sin conversión si en otros lenguajes es válido?
* **Explicación para la defensa:**  
  Esto responde a la estricta **especificación de temas particulares de la cátedra**:
  - El **Tema 35** define las *Conversiones Implícitas*, donde el compilador inserta automáticamente instrucciones de conversión de entero a flotante.
  - El **Tema 33** define *Conversiones Explícitas de Entero a Flotante* (`tosf` / `todf`).
  - Nuestro grupo (Grupo 20) tiene asignado **únicamente el Tema 34: Conversiones Explícitas de Flotante a Entero (`tos`)**.  
  El `Tp3.pdf` indica para el Tema 34 que *"Sólo se permitirán operaciones (expresiones aritméticas o comparaciones) con operandos de distinto tipo, si en el código se incluye la conversión explícita [...]. Para asignaciones, sólo se permitirá asignar una expresión de punto flotante a una variable de tipo entero, si se incluye la conversión correspondiente"*.  
  Al no tener asignado el Tema 35, cualquier cruce de tipos fuera de lo reglado por el Tema 34 es rechazado por el sistema de tipos.

---

#### ❓ Pregunta 4: ¿Qué sucede ante un error de tipos y por qué NO es un "error silencioso"? ¿Cómo se evitan los errores en cascada?
* **Explicación para la defensa:**  
  1. **El error SIEMPRE se reporta:** Si los operandos de una suma son `SHORTINT` y `SINGLEF`, la acción semántica emite inmediatamente:  
     `Error semantico (linea X): Incompatibilidad de tipos en operacion '+': no se puede operar SHORTINT con SINGLEF (requiere conversion explicita 'tos')`.
  2. **Supresión de errores en cascada:** Tras reportar el error, la regla ejecuta `$$ = new ParserVal();`. Una instancia vacía de `ParserVal` tiene sus atributos `sval = null` y `obj = null`.  
  3. **Protección en niveles superiores:** Las reglas contenedoras (por ejemplo, una asignación `a := a + x;`) chequean `if ($3 != null && $3.sval != null && $3.obj != null)`. Al detectar que el hijo proviene de una falla ya informada:
     - No emiten un segundo mensaje redundante ("error en asignación").
     - No generan tercetos con operandos basura o referencias corruptas.
     - Continúan analizando el resto del código fuente para encontrar otros posibles errores en el programa.

---

#### ❓ Pregunta 5: ¿Cómo se implementa la función intrínseca `tos()` y qué tercetos genera?
* **Explicación para la defensa:**  
  La regla en `gram.y` es:
  ```yacc
  conversion_tos : TOS '(' expresion ')'
  ```
  1. **Chequeo semántico:** Se valida que `$3.obj` sea exactamente `"SINGLEF"`. Si el programador intenta aplicar `tos` sobre un `SHORTINT`, el compilador reporta:  
     `Error semantico (linea X): La funcion de conversion 'tos' requiere una expresion de tipo SINGLEF (se recibio: SHORTINT)`.
  2. **Emisión del terceto:** Si es válido, se emite el terceto:  
     `[i] ( tos , $3.sval , - )` con tipo asociado `"SHORTINT"`.
  3. **Sintetización del tipo:** La regla asigna `$$.sval = "[" + i + "]"` y `$$.obj = "SHORTINT"`. De este modo, cualquier operación que envuelva al `tos()` (como `tos(z) + 5$s`) ve al operando como un entero corto legítimo.

---

#### ❓ Pregunta 6: ¿Por qué ninguna Acción Semántica (AS) léxica guarda el tipo de los identificadores si en `factor : IDENTIFICADOR` lo consultamos en la Tabla de Símbolos?
* **Explicación para la defensa:**  
  1. **El Léxico carece de memoria contextual:** Cuando el autómata finito determinista del analizador léxico procesa los caracteres `a`, `b`, etc., ejecuta la acción `AS3` para cerrar el token de identificador. En ese momento, el léxico ignora completamente si esa palabra representará una variable de tipo `SHORTINT`, un `SINGLEF`, una función o una clase.
  2. **El Sintáctico como fuente de verdad de tipos:** El tipo de un identificador se conoce únicamente cuando el autómata ascendente reduce una producción declarativa, como `declaracion_variables : tipo lista_variables ';'`. En esa acción semántica sintáctica se ejecuta `TablaSimbolos.declararVariable(nombreVar, tipoDato, "variable", ambitoActual)`, y es allí donde se registra `atributos.put(TIPO, tipo)` en la entrada correspondiente de la tabla.
  3. Por lo tanto, cuando las sentencias ejecutables reducen `factor : IDENTIFICADOR`, la variable ya cuenta con su metadato `TIPO` cargado y listo para ser consultado en $O(1)$.

---

#### ❓ Pregunta 7: ¿Qué ocurre en la sentencia de asignación si `tipoExp` llega en `null`?
* **Explicación para la defensa:**  
  En la acción semántica de `asignacion : IDENTIFICADOR ASIGNAR expresion`:
  ```java
  String tipoExp = (String) $3.obj;
  if (tipoExp != null) {
      // Comparación y generación de terceto :=
  }
  ```
  1. **Supresión de código intermedio:** Si `tipoExp` es `null`, la guarda `if (tipoExp != null)` evalúa a `false` y el compilador **no genera ningún terceto de asignación**.
  2. **Causa del valor `null`:** Ocurre exclusivamente cuando la subexpresión del lado derecho falló previamente durante el análisis (por ejemplo, operó tipos incompatibles o utilizó una variable no declarada) y sintetizó una instancia vacía `new ParserVal()` con sus campos en `null`.
  3. **Invariante:** Esto garantiza que la máquina intermedia jamás reciba una instrucción `:=` con una expresión que no haya superado satisfactoriamente el análisis semántico de tipos.

---

#### ❓ Pregunta 8: En las comparaciones (`condicion`), ¿qué caso provocaría que un operando tenga tipo `null` y qué significa?
* **Explicación para la defensa:**  
  Consideremos una sentencia de selección como:
  ```pascal
  IF (a + z > 10$s)
  ```
  Donde la variable `z` no fue declarada en el código fuente:
  1. Al reducir la subexpresión `a + z`, se detecta que `z` no existe, se reporta inmediatamente el error `Variable 'z' no declarada` y la suma sintetiza `new ParserVal()`, dejando `sval = null` y `obj = null`.
  2. Al llegar a la reducción de `condicion : expresion comparador expresion`, el lado izquierdo tiene su tipo en `null`.
  3. **Manejo defensivo:** La guarda `if (tipo1 != null && tipo2 != null)` previene que el comparador intente cotejar `null` contra `"SHORTINT"`, lo que emitiría un **error falso en cascada** (*"Tipos incompatibles: null y SHORTINT"*). La condición entiende que el error de base ya fue reportado en la hoja del árbol y se abstiene de duplicar advertencias.

---

#### ❓ Pregunta 9: ¿Por qué la regla `condicion` debe emitir un terceto de comparación y cómo se consumirá en el Hito 5 con el salto condicional `BF`?
* **Explicación para la defensa:**  
  1. **Semántica de control en tercetos:** En la teoría de la cátedra (*Resumen Generación código intermedio.pdf*, diapositiva *"Selección – Tercetos"*), la evaluación de una condición `(a > 0)` debe producir una instrucción de comparación en código intermedio:
     ```text
     1. ( > , a , 0 )       // Terceto generado por 'condicion'
     2. ( BF , [1] , 6 )    // Salto condicional generado por el 'IF'
     ```
  2. **Rol de la condición:** La regla `condicion` genera el terceto relacional (`>`, `<`, `<=`, etc.) y sintetiza su referencia numérica (`[1]`) en `$$`.
  3. **Consumo por el salto condicional:** La sentencia de control (`IF` o `REPEAT`) toma esa referencia como su primer operando para generar el terceto `BF` (Branch False), indicando: *"si el resultado de la comparación [1] es falso, bifurcar hacia la dirección de destino"*.
  4. En el Hito 4 dejamos establecido el chequeo estático de tipos en la comparación, sentando la base para incorporar la emisión del terceto relacional y el backpatching en el Hito 5.

---

### 4.3. Casos de Prueba Verificados

#### Caso 1: Tipos Válidos y Conversión `tos()` (`tests/test_hito4_tipos_ok.txt`)
```pascal
programa_tipos_ok

shortint a, b, c;
singlef x, y, z;

BEGIN
  // Operaciones homogeneas shortint
  a := 10$s;
  b := 20$s;
  c := a + b * 2$s;
  pout(c);

  // Operaciones homogeneas singlef
  x := 1.5;
  y := 2.5;
  z := x + y * 3.0;
  pout(z);

  // Tema 34: Conversion explicita tos() de singlef a shortint
  a := tos(x);
  b := tos(x + y);
  c := tos(z) + 5$s;
  pout(c);

  // Tema 17: Asignacion en expresion con tipos compatibles
  a := b = (30$s);
  x := y = (10.0);
END
```

**Salida obtenida:**
```text
Programa reconocido correctamente
=== TABLA DE SIMBOLOS ===
1: {USO=nombre_programa, TIPO=programa, MANGLING=programa_tipos_ok:main, AMBITO=:main, LEXEMA=programa_tipos_ok}
2: {USO=variable, TIPO=SHORTINT, MANGLING=a:main, AMBITO=:main, LEXEMA=a}
3: {USO=variable, TIPO=SHORTINT, MANGLING=b:main, AMBITO=:main, LEXEMA=b}
4: {USO=variable, TIPO=SHORTINT, MANGLING=c:main, AMBITO=:main, LEXEMA=c}
5: {USO=variable, TIPO=SINGLEF, MANGLING=x:main, AMBITO=:main, LEXEMA=x}
6: {USO=variable, TIPO=SINGLEF, MANGLING=y:main, AMBITO=:main, LEXEMA=y}
7: {USO=variable, TIPO=SINGLEF, MANGLING=z:main, AMBITO=:main, LEXEMA=z}
8: {TIPO=SHORTINT, LEXEMA=10, OCURRENCIAS=1}
9: {TIPO=SHORTINT, LEXEMA=20, OCURRENCIAS=1}
10: {TIPO=SHORTINT, LEXEMA=2, OCURRENCIAS=1}
11: {TIPO=SINGLEF, LEXEMA=1.5, OCURRENCIAS=1}
12: {TIPO=SINGLEF, LEXEMA=2.5, OCURRENCIAS=1}
13: {TIPO=SINGLEF, LEXEMA=3.0, OCURRENCIAS=1}
14: {TIPO=SHORTINT, LEXEMA=5, OCURRENCIAS=1}
15: {TIPO=SHORTINT, LEXEMA=30, OCURRENCIAS=1}
16: {TIPO=SINGLEF, LEXEMA=10.0, OCURRENCIAS=1}

----------------------------------------
         CODIGO INTERMEDIO (TERCETOS)   
----------------------------------------
0 ( := , a:main , 10 )
1 ( := , b:main , 20 )
2 ( * , b:main , 2 )
3 ( + , a:main , [2] )
4 ( := , c:main , [3] )
5 ( POUT , c:main , - )
6 ( := , x:main , 1.5 )
7 ( := , y:main , 2.5 )
8 ( * , y:main , 3.0 )
9 ( + , x:main , [8] )
10 ( := , z:main , [9] )
11 ( POUT , z:main , - )
12 ( tos , x:main , - )
13 ( := , a:main , [12] )
14 ( + , x:main , y:main )
15 ( tos , [14] , - )
16 ( := , b:main , [15] )
17 ( tos , z:main , - )
18 ( + , [17] , 5 )
19 ( := , c:main , [18] )
20 ( POUT , c:main , - )
21 ( := , b:main , 30 )
22 ( := , a:main , [21] )
23 ( := , y:main , 10.0 )
24 ( := , x:main , [23] )
----------------------------------------
```
*Validación:* Todas las constantes registran `TIPO` en TS; los tercetos de `tos` (`12`, `15`, `17`) convierten de `SINGLEF` a `SHORTINT` y permiten su posterior operación con constantes y variables enteras.

---

#### Caso 2: Detección de Errores de Tipo y Supresión de Tercetos (`tests/test_hito4_tipos_error.txt`)
```pascal
programa_tipos_error

shortint a, b;
singlef x, y;

BEGIN
  // Error 1: Operacion binaria entre shortint y singlef sin conversion
  a := a + x;

  // Error 2: Operacion binaria entre singlef y shortint sin conversion
  x := y * b;

  // Error 3: Asignacion de singlef a shortint sin tos()
  a := x;

  // Error 4: Asignacion de shortint a singlef
  x := a;

  // Error 5: Uso indebido de tos() sobre una expresion de tipo shortint
  b := tos(a + 5$s);

  // Error 6: Comparacion entre tipos incompatibles sin conversion
  IF (a > x)
    pout("Error de tipos");
  END_IF;
END
```

**Salida obtenida:**
```text
Error semantico (linea 8): Incompatibilidad de tipos en operacion '+': no se puede operar SHORTINT con SINGLEF (requiere conversion explicita 'tos')
Error semantico (linea 11): Incompatibilidad de tipos en operacion '*': no se puede operar SINGLEF con SHORTINT (requiere conversion explicita 'tos')
Error semantico (linea 14): Incompatibilidad de tipos en la asignacion: no se puede asignar SINGLEF a la variable SHORTINT 'a' sin conversion explicita 'tos'
Error semantico (linea 17): Incompatibilidad de tipos en la asignacion: no se puede asignar SHORTINT a la variable SINGLEF 'x'
Error semantico (linea 20): La funcion de conversion 'tos' requiere una expresion de tipo SINGLEF (se recibio: SHORTINT)
Error semantico (linea 23): Incompatibilidad de tipos en comparacion: no se puede comparar SHORTINT con SINGLEF (requiere conversion explicita 'tos')
Compilacion finalizada con 0 error(es) sintactico(s) y 6 error(es) semantico(s)

----------------------------------------
         CODIGO INTERMEDIO (TERCETOS)   
----------------------------------------
0 ( + , a:main , 5 )
1 ( POUT , "Error de tipos" , - )
----------------------------------------
```
*Validación:* El compilador detecta y reporta taxativamente los 6 errores semánticos con sus números de línea reales. Se suprimen por completo las 5 sentencias de asignación inválidas y jamás se emite código intermedio con tipos corruptos.

---

## 📌 Hito 5: Control de Flujo y Backpatching: IF, IF-ELSE y REPEAT UNTIL (Tema 12)

### 5.1. Objetivo Técnico
Dotar al compilador de la capacidad de traducir estructuras de control de bifurcación (`IF`, `IF-ELSE`) y de iteración (`REPEAT UNTIL` - Tema 12) a código intermedio en tercetos con resolución automática de saltos hacia adelante y hacia atrás.
1. **Emisión de comparaciones:** Generar tercetos con el operador relacional exacto (`>`, `<`, `>=`, `<=`, `==`, `!=`) al reducir la regla `condicion`.
2. **Bifurcaciones condicionales e incondicionales:** Emitir `BF` (Branch False) y `BI` (Branch Incondicional).
3. **Backpatching sistemático:** Resolver destinos futuros de bifurcaciones mediante una pila de saltos LIFO (`Stack<Integer>`) y acciones semánticas intermedias en BYacc/J garantizando 0 conflictos Shift/Reduce y Reduce/Reduce.
4. **Semántica de REPEAT UNTIL:** Salto condicional `BF` hacia atrás a la primera instrucción del cuerpo del ciclo mientras la condición permanezca falsa.

---

### 5.2. Decisiones de Diseño y Preguntas Conceptuales para la Defensa Oral

#### ❓ Pregunta 1: ¿Por qué utilizamos una pila LIFO (`Stack<Integer>`) para almacenar las direcciones de salto en lugar de simples variables enteras?
* **Explicación para la defensa:**  
  Las estructuras de control en cualquier lenguaje estructurado admiten **anidamiento arbitrario** (un `IF` dentro de otro `IF`, un `REPEAT` dentro de un `IF`, o un `IF-ELSE` dentro del `ELSE` de otro `IF`).
  - Si usáramos variables escalares (como `int ultimoSalto`), un `IF` interno sobrescribiría la dirección del `IF` contenedor, haciendo imposible completar el salto exterior cuando se cierre el bloque externo.
  - Al emplear una estructura **LIFO (Last-In, First-Out)** con `Stack<Integer>`, cada bloque apila su propio terceto incompleto (`BF` o `BI`) y, al cerrarse, desapila exactamente la referencia que le corresponde a su nivel sintáctico.
  - Gracias a la pila, el anidamiento se resuelve de manera limpia, predecible y sin interferencias entre ámbitos o estructuras de control adyacentes.

---

#### ❓ Pregunta 2: ¿Cómo funciona el algoritmo de Backpatching paso a paso en un `IF-ELSE`?
```pascal
IF (condicion)
  bloque_then;
ELSE
  bloque_else;
END_IF;
```
* **Explicación para la defensa (alineada con las diapositivas 46 a 55 de `sentencias-control.pdf`):**  
  1. **Fin de la condición:** Se reduce la regla `encabezado_if : IF '(' condicion ')'`. En este punto no sabemos cuántas instrucciones tendrá `bloque_then`, por lo que emitimos un salto condicional con destino desconocido:  
     `idBF = GeneradorTerceto.crearTerceto("BF", [cond], "?");`  
     Se apila `idBF` en `pilaSaltos`.
  2. **Lectura del token `ELSE`:** El autómata reduce la regla auxiliar `inicio_else : ELSE`. En este instante:
     - El `bloque_then` ya terminó de emitir sus tercetos.
     - Para que el flujo que ejecutó el `THEN` no ingrese al `ELSE`, debemos emitir un salto incondicional:  
       `idBI = GeneradorTerceto.crearTerceto("BI", "?", "-");`
     - Desapilamos el `idBF` anterior de la pila. Como la siguiente instrucción a generar será la primera del `bloque_else`, el destino del `BF` es exactamente `GeneradorTerceto.getProximoIndice()`.
     - Ejecutamos `completarSalto(idBF, getProximoIndice())`, parchando el operando 2 del `BF`.
     - Finalmente, apilamos `idBI` en `pilaSaltos`.
  3. **Cierre con `END_IF ';'`:** Al reducir `resto_if : inicio_else bloque END_IF ';'`:
     - El `bloque_else` ya culminó.
     - Desapilamos `idBI` de `pilaSaltos`.
     - Completamos el operando 1 del `BI` con `getProximoIndice()` (la primera instrucción que sigue fuera del `IF-ELSE`).
     - ¡El parcheo queda completado en una sola pasada!

---

#### ❓ Pregunta 3: ¿Por qué en la sentencia `REPEAT UNTIL` (Tema 12) la bifurcación es `BF` y NO requiere backpatching diferido?
* **Explicación para la defensa:**  
  1. **Semántica de REPEAT UNTIL:** La especificación de la cátedra dice textualmente: *"El bloque de sentencias ejecutables se ejecutará hasta que la condición sea verdadera"*. Por lo tanto, el ciclo debe iterar mientras la condición sea **FALSA**, y salir cuando sea **VERDADERA**.
  2. **Bifurcación por falso:** El terceto a emitir al evaluar la condición al final del ciclo es:  
     `( BF , [cond] , [dir_inicio] )`
  3. **Cero backpatching diferido:** A diferencia del `IF` (cuyo salto es hacia adelante hacia código no generado aún), en `REPEAT UNTIL` el salto es **hacia atrás**. La dirección del inicio del bloque fue registrada previamente al leer la palabra clave `REPEAT` (`dirInicio = getProximoIndice()`). Cuando el parser alcanza el `UNTIL (condicion);`, ya conoce perfectamente el índice de destino `[dirInicio]`, por lo que emite el terceto completo inmediatamente sin dejar huecos pendientes.

---

#### ❓ Pregunta 4: ¿Por qué definimos reglas explícitas como `encabezado_if`, `inicio_else` e `inicio_repeat` en lugar de incrustar acciones semánticas en medio de las producciones existentes?
* **Explicación para la defensa:**  
  En Yacc / BYacc/J, colocar una acción semántica en medio de una regla:
  ```yacc
  // Acción incrustada (antipatrón riesgoso)
  sentencia_if : IF '(' condicion ')' { accion; } bloque resto_if ;
  ```
  fuerza al generador LALR a inventar una producción epsilon anónima oculta (`$$1 : /* vacio */ { accion; }`).
  - Si existen reglas de error o producciones que comparten prefijos con la regla (por ejemplo `IF condicion ')'` o `REPEAT UNTIL`), las producciones vacías anónimas son la causa número uno de **conflictos Shift/Reduce y Reduce/Reduce**.
  - Al crear no terminales explícitos (`encabezado_if`, `inicio_else`, `inicio_repeat`), controlamos conscientemente el autómata de estados finitos, permitiendo que `byaccj -J -v gram.y` mantenga **0 conflictos** en `y.output` de manera determinista.

---

#### ❓ Pregunta 5: ¿Qué sucede ante un error sintáctico o semántico dentro de una condición o bloque?
* **Explicación para la defensa:**  
  1. **Condición con error de tipos:** Si se intenta comparar tipos incompatibles (ej. `IF (a > x)` donde `a` es `SHORTINT` y `x` es `SINGLEF`), la regla `condicion` emite el error semántico correspondiente y sintetiza un `ParserVal()` vacío (`.sval == null`). La regla `encabezado_if` comprueba `$3 != null && $3.sval != null` antes de crear el `BF` y antes de apilar nada.
  2. **Pilas a salvo de EmptyStackException:** En `resto_if` y en las reglas de error de `REPEAT`, siempre se valida `if (!pilaSaltos.isEmpty())` antes de hacer `pop()`. Si hubo un error sintáctico que impidió apilar el inicio, el compilador reporta el error sintáctico, absorbe el fallo y jamás se cuelga por excepción en tiempo de compilación.

---

#### ❓ Pregunta 6: Si una sentencia `REPEAT UNTIL` tiene un error sintáctico (ej. falta el cuerpo o la condición), ¿qué ocurre con los tercetos ya emitidos dentro del bloque? ¿Por qué no se hace rollback?
* **Explicación para la defensa:**  
  1. **En `inicio_repeat`:** Únicamente se apiló el índice numérico en `pilaSaltos`; no se generó ninguna instrucción intermedia.
  2. **En el cuerpo (`bloque`):** Las sentencias internas válidas ya redujeron y emitieron tercetos de manera secuencial en una sola pasada (*single-pass*). Al detectarse el error sintáctico al cierre del bucle, simplemente se cancela la emisión del terceto `( BF , ... )`.
  3. **No se hace rollback porque la compilación ya está abortada:** En la arquitectura de compiladores, si se detecta al menos un error sintáctico o semántico (`cant_errores > 0 || cant_errores_semanticos > 0`), **la etapa posterior de generación de código Assembler (TP4) queda terminantemente cancelada**. Los tercetos quedan en memoria exclusivamente como traza de depuración del parser, pero jamás llegarán a traducirse a código ejecutable. Implementar un mecanismo de marcha atrás (*rollback*) complejizaría la tabla de símbolos y los índices de tercetos sin ninguna utilidad práctica.

---

#### ❓ Pregunta 7: ¿Por qué en las reglas de error de `REPEAT UNTIL` no usamos directamente el no terminal `condicion_iteracion` en vez de reglas explícitas con `'('`? ¿Qué es un ancla sintáctica (*anchor token*)?
* **Explicación para la defensa:**  
  1. **Aparición de 8 conflictos Shift/Reduce en `y.output`:** Si reemplazamos `inicio_repeat bloque '(' condicion ')' ';'` por `inicio_repeat bloque condicion_iteracion ';'`, BYacc/J reporta inmediatamente 8 conflictos en el estado 191.
  2. **Causa técnica del conflicto:** La regla de error `condicion_iteracion : condicion` contempla una condición *sin paréntesis*. Como una `condicion` puede arrancar con identificadores o números, y las sentencias dentro de `BEGIN sentencias_ejecutables` también arrancan con identificadores o números, si falta la palabra clave `UNTIL`, el autómata LALR(1) con 1 solo token de preanálisis no puede decidir si un identificador `x` es la siguiente sentencia del bloque (hacer *shift*) o si es la condición del bucle sin `UNTIL` ni paréntesis (hacer *reduce*).
  3. **El concepto de ancla sintáctica (*anchor token*):** Cuando está presente la palabra clave `UNTIL`, esta actúa como un ancla inequívoca que avisa al autómata que comienza la condición. Pero cuando **falta `UNTIL`**, el autómata necesita imperiosamente el delimitador `'('` para separar el cuerpo de la condición. Esas reglas con `'('` explícito no son duplicación accidental, sino **anclas sintácticas indispensables para garantizar los 0 conflictos exigidos por la cátedra**.

---

#### ❓ Pregunta 8: ¿Por qué no se unifica la regla sintáctica de condición entre paréntesis `( condicion )` para el `IF` y el `REPEAT UNTIL` en un solo no terminal?
* **Explicación para la defensa:**  
  1. **Problema del lookahead y delimitador posterior:** En `REPEAT UNTIL`, la condición está al final de la sentencia y termina con `;`, permitiendo al parser un punto de sincronización seguro aun si falta el `)`. En cambio, en el `IF`, la condición está en el medio y lo que sigue es directamente el cuerpo/bloque sin punto y coma intermedio; relajar los paréntesis en esa posición generaría colisiones Shift/Reduce con las sentencias del bloque.
  2. **Criterio de contextualidad de la cátedra:** Se evalúa positivamente que los mensajes sean informativos y específicos al contexto (*"Falta '(' de apertura en condicion de seleccion"* vs. *"Falta '(' de apertura en condicion de iteracion"*).

---

### 5.3. Casos de Prueba Verificados

#### Caso 1: Estructuras de Control y Anidamiento Válidos (`tests/test_hito5_control_ok.txt`)
```pascal
programa_control_ok

shortint a, b, c;
singlef x, y, z;

BEGIN
  a := 5$s;
  b := 10$s;
  c := 0$s;
  x := 1.0;
  y := 2.0;
  z := 0.0;

  // 1. IF simple
  IF (a > 10$s)
    c := a + 1$s;
  END_IF;

  // 2. IF-ELSE
  IF (x <= y)
    z := x * 2.0;
  ELSE
    z := y * 3.0;
  END_IF;

  // 3. IF anidados dentro de IF-ELSE
  IF (a == b)
    IF (c != 0$s)
      a := 1$s;
    ELSE
      a := 2$s;
    END_IF;
  ELSE
    a := 3$s;
  END_IF;

  // 4. REPEAT UNTIL sentencia simple
  REPEAT
    a := a + 1$s;
  UNTIL (a >= 20$s);

  // 5. REPEAT UNTIL con BEGIN ... END
  REPEAT
  BEGIN
    x := x + 1.0;
    z := z - 1.0;
  END
  UNTIL (x > 10.0);

  // 6. REPEAT conteniendo IF anidado
  REPEAT
    IF (a < 30$s)
      a := a + 2$s;
    END_IF;
  UNTIL (a == 30$s);

  pout("Fin de control de flujo ok");
END
```

**Salida obtenida:**
```text
Programa reconocido correctamente
----------------------------------------
         CODIGO INTERMEDIO (TERCETOS)   
----------------------------------------
0 ( := , a:main , 5 )
1 ( := , b:main , 10 )
2 ( := , c:main , 0 )
3 ( := , x:main , 1.0 )
4 ( := , y:main , 2.0 )
5 ( := , z:main , 0.0 )
6 ( > , a:main , 10 )
7 ( BF , [6] , [10] )
8 ( + , a:main , 1 )
9 ( := , c:main , [8] )
10 ( <= , x:main , y:main )
11 ( BF , [10] , [15] )
12 ( * , x:main , 2.0 )
13 ( := , z:main , [12] )
14 ( BI , [17] , - )
15 ( * , y:main , 3.0 )
16 ( := , z:main , [15] )
17 ( == , a:main , b:main )
18 ( BF , [17] , [25] )
19 ( != , c:main , 0 )
20 ( BF , [19] , [23] )
21 ( := , a:main , 1 )
22 ( BI , [24] , - )
23 ( := , a:main , 2 )
24 ( BI , [26] , - )
25 ( := , a:main , 3 )
26 ( + , a:main , 1 )
27 ( := , a:main , [26] )
28 ( >= , a:main , 20 )
29 ( BF , [28] , [26] )
30 ( + , x:main , 1.0 )
31 ( := , x:main , [30] )
32 ( - , z:main , 1.0 )
33 ( := , z:main , [32] )
34 ( > , x:main , 10.0 )
35 ( BF , [34] , [30] )
36 ( < , a:main , 30 )
37 ( BF , [36] , [40] )
38 ( + , a:main , 2 )
39 ( := , a:main , [38] )
40 ( == , a:main , 30 )
41 ( BF , [40] , [36] )
42 ( POUT , "Fin de control de flujo ok" , - )
----------------------------------------
```
*Validación:*
- Terceto 7 (`BF`) salta a `[10]` (fin del IF simple).
- Terceto 11 (`BF`) salta a `[15]` (inicio de la rama ELSE).
- Terceto 14 (`BI`) salta a `[17]` (salida del IF-ELSE).
- Tercetos 18, 20, 22 y 24 resuelven a la perfección el doble anidamiento de `IF-ELSE` anidado.
- Tercetos 29 y 35 (`BF`) saltan hacia atrás a `[26]` y `[30]` respectivamente.
- Tercetos 36 a 41 resuelven el bucle `REPEAT` conteniendo un `IF` anidado, saltando hacia atrás a `[36]`.

---

#### Caso 2: Manejo de Errores y Recuperación Sintáctica/Semántica (`tests/test_hito5_control_error.txt`)
```pascal
programa_control_error

shortint a, b;
singlef x, y;

BEGIN
  // Error 1: Incompatibilidad de tipos en condicion IF
  IF (a > x)
    a := 1$s;
  END_IF;

  // Error 2: Falta '(' en condicion IF
  IF a == b)
    a := 2$s;
  END_IF;

  // Error 3: Falta END_IF en IF-ELSE
  IF (a == b)
    a := 3$s;
  ELSE
    a := 4$s;
  ;

  // Error 4: Falta cuerpo en REPEAT UNTIL
  REPEAT
  UNTIL (a == 0$s);

  // Error 5: Incompatibilidad de tipos en condicion REPEAT UNTIL
  REPEAT
    a := a + 1$s;
  UNTIL (a == y);

  pout("Fin de control con errores");
END
```

**Salida obtenida:**
```text
Error semantico (linea 8): Incompatibilidad de tipos en comparacion: no se puede comparar SHORTINT con SINGLEF (requiere conversion explicita 'tos')
Error sintactico (linea 15): Falta '(' de apertura en condicion de seleccion
Error sintactico (linea 22): Falta palabra clave END_IF al final de la sentencia IF
Error sintactico (linea 26): Falta el cuerpo de la iteracion REPEAT
Error semantico (linea 31): Incompatibilidad de tipos en comparacion: no se puede comparar SHORTINT con SINGLEF (requiere conversion explicita 'tos')
Compilacion finalizada con 3 error(es) sintactico(s) y 2 error(es) semantico(s)
```
*Validación:* Cero excepciones `EmptyStackException`, detección taxativa con número de línea real y recuperación de sincronización limpia.

---

## 📌 Hito 6 (SIGUIENTE): Funciones, Clases y Temas Avanzados
### 6.1. Alcance
- **Tema 21:** Funciones inferidas `AUTO FUNCTION` (verificación de al menos un `RET` sintáctico, inferencia del tipo de retorno a partir del terceto sintetizado por `RET`).
- **Tema 22:** Variables `COMPTIME` evaluadas estáticamente (supresión de tercetos en tiempo de ejecución).
- **Tema 26:** Modificadores `IMPORT FROM` y `EXPORT TO` en clases (chequeo de visibilidad semántica de miembros).
- **Tema 29:** Acceso posicional a atributos de clases (`obj[0$s]`).
- **Tema 30:** Herencia múltiple de clases (`EXTENDS` múltiple con desambiguación por orden de declaración).





