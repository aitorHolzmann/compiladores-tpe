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
[Hito 4] Chequeo Estático de Tipos y Tema 34: Conversión Explícita tos() (SIGUIENTE)
    │
    ▼
[Hito 5] Control de Flujo y Backpatching: IF, IF-ELSE y REPEAT UNTIL (Tema 12)
    │
    ▼
[Hito 6] Funciones, Clases y Temas Avanzados (AUTO FUNCTION, COMPTIME, IMPORT/EXPORT, Herencia Múltiple)
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

#### Caso 2: Chequeos Semánticos Obligatorios (`tests/test_hito3_errores_semanticos.txt`)
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
```
*Validación:* Detección taxativa de redeclaraciones en el mismo ámbito y variables no declaradas, reportando número de línea real y bloqueando la emisión de tercetos erróneos.

---

## 📌 Hito 4 (SIGUIENTE): Chequeo Estático de Tipos y Tema 34: Conversión Explícita `tos()`

### 4.1. Objetivo Técnico
1. Propagar el tipo (`SHORTINT` o `SINGLEF`) a través de atributos sintetizados (`$$.sval` y campo `tipo`) en `expresion`, `termino` y `factor`.
2. Validar compatibilidad de tipos en operaciones aritméticas binarias (`+`, `-`, `*`, `/`):
   - Prohibido operar `SHORTINT` con `SINGLEF` sin conversión explícita `tos()`.
3. Validar compatibilidad en sentencias de asignación (`:=`):
   - Asignar `SHORTINT` a `SINGLEF`: permitido mediante conversión implícita (generar terceto de conversión).
   - Asignar `SINGLEF` a `SHORTINT`: prohibido sin conversión explícita `tos()` (error semántico).
4. Implementar el **Tema 34**: función intrínseca `tos(expresion)` que convierte de `SINGLEF` a `SHORTINT` emitiendo el terceto `( tos , [i] , - )`.




