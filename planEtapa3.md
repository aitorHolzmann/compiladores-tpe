# Etapa 3 — Código Intermedio (Tercetos) y Análisis Semántico

## Plan de Trabajo, Nivelación Teórica y Preparación para Defensa Oral

**Grupo 20 · Diseño de Compiladores I · 2026**

---

## Parte I: Nivelación Teórica

### ¿Qué es el Código Intermedio y por qué lo usamos?

Hasta ahora nuestro compilador hace dos cosas: reconoce las palabras del lenguaje (léxico) y valida que estén organizadas según las reglas gramaticales (sintáctico). Pero **no entiende qué significan**. Cuando escribimos `a := b + c;`, el parser sabe que esa cadena "es una asignación válida", pero no sabe qué tipos tienen `a`, `b` y `c`, ni genera ninguna instrucción ejecutable.

El **código intermedio** es una representación abstracta del programa a mitad de camino entre el código fuente y el assembler final. Tiene dos ventajas clave:

1. **Independencia de la máquina**: Si cambiamos de arquitectura de CPU, solo cambiamos la última etapa (generación de assembler). El código intermedio no se toca.
2. **Facilidad de optimización**: Es más fácil aplicar optimizaciones (como "reducción simple") sobre un formato estandarizado y lineal que sobre el texto original.

> [!IMPORTANT]
> La cátedra asignó al Grupo 20 la técnica de **Tercetos**. Es la única representación válida para nuestra entrega.

---

### ¿Qué es un Terceto?

Un terceto es una estructura que representa **una única operación** con exactamente 3 componentes:

```
( Operador , Operando1 , Operando2 )
```

Cada terceto se numera secuencialmente. Cuando el resultado de un terceto se usa en otro, se lo referencia por su **índice entre corchetes** `[i]`.

**Ejemplo concreto** — la sentencia `z := a + b / 5`:

```
20 ( / , b , 5 )         ← primero la división (mayor precedencia)
21 ( + , a , [20] )       ← suma usando el resultado del terceto 20
22 ( := , z , [21] )      ← asignación del resultado al destino
```

*La clave pedagógica:* El `[20]` significa "el resultado que calculamos en el terceto número 20". En lugar de inventar variables temporales ficticias (como hacen los cuádruplos), los tercetos usan **referencias posicionales**. Esto los hace más compactos.

> [!NOTE]
> **Formato de salida exigido por la cátedra** (`Tp3.pdf`, pág. 12): Cada terceto se muestra con su número, espacios y comas exactas: `22 ( := , z , [21] )`.

---

### Diferencia: Tercetos vs. Polaca Inversa vs. Árboles Sintácticos

Esta es una **pregunta clásica de examen y defensa oral**.

| Característica | Tercetos | Polaca Inversa (RPN) | Árbol Sintáctico (AST) |
| :--- | :--- | :--- | :--- |
| **Estructura** | Lista lineal numerada | Lista lineal (pila implícita) | Estructura jerárquica en memoria |
| **Operandos** | Explícitos en cada instrucción | Implícitos (la pila los resuelve) | Nodos hoja del árbol |
| **Referencias intermedias** | Por índice `[i]` | No hay — la pila conecta todo | Por punteros hijo izq/der |
| **Saltos (IF, REPEAT)** | `BF`/`BI` con dirección numérica | `BF`/`BI` con dirección numérica | Nodos especiales en el árbol |
| **Legibilidad** | Alta (cada línea es autocontenida) | Baja (hay que simular la pila mentalmente) | Alta (visual, pero difícil de serializar) |
| **Optimización** | Fácil (se reordenan/eliminan líneas) | Difícil (las posiciones son frágiles) | Fácil (se podan ramas) |
| **Temporales** | No necesita (usa `[i]`) | No necesita (usa la pila) | No necesita (usa punteros) |

**En resumen**: Los tercetos combinan la linealidad de la polaca (fácil de recorrer para generar assembler) con la legibilidad del árbol (cada instrucción es autocontenida y explícita).

---

### Saltos y Backpatching (Parcheo)

Este es el mecanismo más importante y el que más preguntan en la defensa.

**El problema**: Al parsear un `IF` o un `REPEAT UNTIL`, necesitamos insertar un terceto de salto (`BF` = Branch if False, `BI` = Branch Incondicional). Pero como el parser lee de izquierda a derecha (bottom-up / LALR), cuando leemos la condición del `IF`, **todavía no sabemos cuántas líneas ocupará el cuerpo** y por lo tanto no sabemos a dónde debe saltar.

**La solución (Backpatching)**:

1. Generamos el terceto **incompleto**: `13 ( BF , [12] , _ )` (el `_` es el destino desconocido).
2. **Apilamos** el índice `13` en una `Stack<Integer>` (pila de saltos).
3. Seguimos compilando el cuerpo del bloque.
4. Cuando la gramática reduce la regla completa y sabemos el destino final, hacemos un **pop** de la pila y **parcheamos** el `_` con el número de terceto correcto.

#### Ejemplo: IF simple

```
Código fuente:                      Tercetos generados:
IF (a > b)                          10 ( > , a , b )
   x := 1$s;                       11 ( BF , [10] , _ )    ← incompleto, apilamos 11
END_IF;                             12 ( := , x , 1$s )
                                    Al reducir END_IF: desapilamos 11, parcheamos _ → 13
                                    Resultado: 11 ( BF , [10] , 13 )
```

#### Ejemplo: IF-ELSE

```
10 ( > , a , b )
11 ( BF , [10] , _ )        ← apilamos 11
12 ( := , x , 1$s )         ← rama TRUE
13 ( BI , _ , _ )            ← salto incondicional, apilamos 13
   Al ver ELSE: desapilamos 11, parcheamos _ → 14
14 ( := , x , 2$s )         ← rama FALSE
   Al reducir END_IF: desapilamos 13, parcheamos _ → 15
```

#### Ejemplo: REPEAT UNTIL (Tema 12)

```
Código fuente:                      Tercetos generados:
REPEAT                              Guardamos posición inicio = 10
   x := x + 1$s;                   10 ( + , x , 1$s )
                                    11 ( := , x , [10] )
UNTIL (x > 5$s);                    12 ( > , x , 5$s )
                                    13 ( BF , [12] , 10 )   ← si condición FALSA, volver al inicio
```

En `REPEAT UNTIL`, la condición se evalúa **al final**. Si es FALSA, se vuelve al inicio del bloque. Si es VERDADERA, se sale del bucle (cae al siguiente terceto).

> [!TIP]
> A diferencia del `IF`, en el `REPEAT UNTIL` no necesitamos backpatching porque al momento de generar el `BF` **ya conocemos la dirección de destino** (la guardamos al inicio del bloque).

---

### Name Mangling y Gestión de Ámbitos

Para evitar colisiones cuando hay variables con el mismo nombre en distintos ámbitos, la cátedra pide renombrar los identificadores concatenando los contextos con **dos puntos `:`**.

> [!CAUTION]
> El `Tp3.pdf` (pág. 2) exige **explícitamente** usar `:` como separador. Ejemplo textual del PDF: `x:main:aa:aaa`. Usar `@`, `_` o cualquier otro separador sería un error grave de consigna.

**Reglas de mangling:**
- Variable `a` declarada en el programa principal → `a:main`
- Variable `a` declarada en la función `f1` → `a:main:f1`
- Parámetro `x` de función `f1` anidada en el main → `x:main:f1`
- Variable `a` en función `aaa` anidada en `aa` → `a:main:aa:aaa`

**Implementación conceptual:**
- Se mantiene una variable global `String ambitoActual` en el parser.
- Al entrar a una función, clase o bloque, se concatena: `ambitoActual = ambitoActual + ":" + nombreNuevo`.
- Al salir, se recorta el último segmento.
- Al registrar una variable en la Tabla de Símbolos, la clave pasa a ser `nombre:ambito`.

---

## Parte II: Plan de Trabajo por Fases

> [!IMPORTANT]
> Todas las implementaciones deben respetar estrictamente el estilo de "código natural de alumno" (sin lambdas, sin streams, sin records, sin librerías externas). Clases simples, bucles `for`/`while`, y `ArrayList`/`Stack` de Java estándar.

---

### Fase 1 — Infraestructura de Tercetos y Estructuras Base

**Objetivo conceptual:** Crear las clases Java básicas para almacenar y numerar tercetos.

**Componentes a diseñar:**
- `class Terceto`: Atributos `String operador`, `String operando1`, `String operando2`, `String tipo`. Constructor simple. Método `toString()` con el formato exigido.
- `class GeneradorTercetos`: Contiene un `ArrayList<Terceto>` estático accesible desde las acciones de `gram.y`. Métodos: `crearTerceto(op, op1, op2)` (devuelve el índice), `obtenerTerceto(indice)`, `parchear(indice, destino)`.
- `Stack<Integer> pilaSaltos`: Para backpatching.

**Validaciones semánticas:** Ninguna (fase puramente estructural).

**Casos de prueba:**
- Instanciación manual de `(+, 2, 3)` y volcado por consola verificando formato `0 ( + , 2 , 3 )`.

---

### Fase 2 — Name Mangling y Gestión de Ámbitos en la Tabla de Símbolos

**Objetivo conceptual:** Enriquecer la Tabla de Símbolos para soportar name mangling con `:` y registrar tipo y uso de cada identificador.

**Componentes a diseñar:**
- Modificar `TablaSimbolos.java` para que soporte claves mangledas (`variable:main:funcion`).
- Agregar atributos `"TIPO"` (shortint, singlef, clase, funcion...) y `"USO"` (variable, funcion, parametro, nombre_programa, nombre_clase) a las entradas.
- Variable global en `gram.y`: `static String ambitoActual = "";` que se actualiza al entrar/salir de funciones, clases y bloques.
- Acciones semánticas en las producciones de `sentencias_declarativas` para registrar tipo y uso.

**Validaciones semánticas:**
- **Error:** Variable no declarada o no visible (búsqueda en el scope actual y superiores).
- **Error:** Variable/función/clase redeclarada en el mismo ámbito.

**Casos de prueba:**
- Declarar `SHORTINT a;` global y `SHORTINT a;` en una función. Verificar en la TS que coexistan como `a:main` y `a:main:f1`.
- Intentar usar una variable no declarada → debe reportar error semántico.

---

### Fase 3 — Tercetos para Expresiones Aritméticas y Asignaciones

**Objetivo conceptual:** Generar tercetos respetando la precedencia de operadores que ya resuelve el autómata LALR.

**Componentes a diseñar:**
- Usar atributos sintetizados en Yacc (`$$.ival = indice_terceto;`) para propagar el índice del terceto generado hacia arriba en el árbol de derivación.
- Acciones semánticas en `expresion`, `termino` y `factor`:
  - `expresion : expresion '+' termino` → `$$.ival = crearTerceto("+", ref($1), ref($3));`
  - `termino : termino '*' factor` → `$$.ival = crearTerceto("*", ref($1), ref($3));`
  - `asignacion : IDENTIFICADOR ASIGNAR expresion` → `crearTerceto(":=", lexema($1), ref($3));`
- Función auxiliar `ref(i)` que devuelve `"[i]"` si es un índice de terceto, o el lexema si es un factor terminal.

**Validaciones semánticas:**
- Propagación del tipo del resultado (para chequeos posteriores).

**Casos de prueba:**
- `a := 2$s + 3$s * 4$s;` → Debe generar el terceto de `*` antes que el de `+` (la precedencia la da el autómata LALR, no nosotros).
- `z := a + b / 5$s;` → Verificar el orden correcto de tercetos.

---

### Fase 4 — Chequeo de Tipos y Conversiones Explícitas (Tema 34 — `tos()`)

**Objetivo conceptual:** Verificar estáticamente la compatibilidad de tipos durante las reducciones gramaticales.

**Componentes a diseñar:**
- Enriquecer el mecanismo de atributos sintetizados para propagar el tipo (`SHORTINT` o `SINGLEF`) junto con el índice del terceto.
- Acción semántica para `factor : TOS '(' expresion ')'` → genera terceto `(tos, ref($3), _)` y el tipo resultante es `SHORTINT`.

**Validaciones semánticas:**
- **Error semántico:** Operar con `SHORTINT` y `SINGLEF` sin conversión explícita `tos()`.
- **Error semántico:** Asignar `SINGLEF` a variable `SHORTINT` sin `tos()`.
- **Válido:** Asignar `SHORTINT` a variable `SINGLEF` (con conversión implícita a generar, según `Tp3.pdf`).

**Casos de prueba:**
- `SHORTINT a; a := tos(3.14);` → Válido. Genera terceto `(tos, 3.14, _)` + `(:=, a:main, [i])`.
- `SHORTINT a; a := 3.14;` → Error semántico: tipos incompatibles.
- `SINGLEF x; SHORTINT y; x := y + 1$s;` → Válido si se genera conversión implícita.

---

### Fase 5 — Sentencias de Control: IF/ELSE y REPEAT UNTIL (Tema 12)

**Objetivo conceptual:** Implementar backpatching para estructuras de control.

**Componentes a diseñar:**
- **Producciones "marcador"** vacías en `gram.y` para capturar el índice actual:
  ```yacc
  marcador_salto : /* vacío */ { $$.ival = GeneradorTercetos.proximoIndice(); } ;
  ```
- Modificar `sentencia_if` para insertar marcadores antes del bloque y antes del ELSE.
- Modificar `sentencia_repeat_until` para guardar el índice de inicio antes del bloque.
- Rutina `GeneradorTercetos.parchear(indice, destino)` para rellenar el campo incompleto.

**Validaciones semánticas:**
- La condición debe ser resultado de un operador relacional (no una expresión aritmética sola).

**Casos de prueba:**
- `IF (a > b) x := 1$s; END_IF;` → Verificar que `BF` apunte al terceto posterior al bloque.
- `IF (a > b) x := 1$s; ELSE x := 2$s; END_IF;` → Verificar `BF` y `BI`.
- `REPEAT x := x + 1$s; UNTIL (x > 5$s);` → Verificar que `BF` apunte al inicio del bloque.
- **Anidamiento**: `IF` dentro de `REPEAT UNTIL` → Verificar que la pila no mezcle índices.

> [!WARNING]
> Los marcadores vacíos pueden generar conflictos shift/reduce si se colocan incorrectamente. Siempre verificar con `byaccj -J -v gram.y` después de agregar marcadores.

---

### Fase 6 — Funciones, AUTO FUNCTION (Tema 21) y Retornos

**Objetivo conceptual:** Generar tercetos para funciones y resolver la inferencia de tipo en `AUTO FUNCTION`.

**Componentes a diseñar:**
- Tercetos especiales: `(CALL, nombre_funcion, cant_params)`, `(RET, ref_expresion, _)`.
- Registrar en la TS: tipo de retorno, nombre y tipo de parámetros.
- Para `pout`: terceto `(POUT, referencia_cadena_o_expresion, _)`.
- Para `AUTO FUNCTION`: al reducir `sentencia_ret : RET '(' expresion ')'`, extraer el tipo de la expresión y propagarlo a la entrada de la función en la TS.

**Validaciones semánticas:**
- **Tema 21:** Si `AUTO FUNCTION` tiene múltiples `RET`, verificar que todos tengan el mismo tipo sintetizado. Si difieren, error semántico.
- **Error:** Cantidad o tipo de parámetros de llamada no coinciden con la declaración.
- **Error:** Invocar una función no declarada.
- Ya validado sintácticamente: `AUTO FUNCTION` sin `RET` (lo maneja `cant_retornos_auto`).

**Casos de prueba:**
- `AUTO FUNCTION test(SHORTINT x) BEGIN RET(x + 1$s); END;` → Infiere tipo `SHORTINT`.
- `AUTO FUNCTION test2() BEGIN RET(2$s); RET(3.0); END;` → Error: tipos de retorno inconsistentes.
- Llamada `test(5$s)` → Genera tercetos de paso de parámetro + `CALL`.

---

### Fase 7 — Semántica de Clases y Objetos (Temas 26, 29, 30)

**Objetivo conceptual:** Implementar chequeos de visibilidad, herencia y acceso posicional.

**Componentes a diseñar:**
- Enriquecer la TS para clases: lista de atributos (ordenados por posición), lista de métodos, lista de padres (`EXTENDS`), listas de import/export.
- Terceto de acceso posicional: `(ACCESO, objeto, indice)` → resuelve al atributo en la posición indicada.
- Mangling para clases: `x1:main:ClaseA`, métodos: `m1:main:ClaseA`.

**Validaciones semánticas:**
- **Tema 26 (IMPORT/EXPORT):** Al acceder a `obj.atributo` o `obj.metodo()`, verificar que el atributo/método esté declarado en `EXPORT TO` de la clase origen, y que la clase destino esté en `IMPORT FROM`. Si no se encuentra localmente, buscar en las clases importadas.
- **Tema 29 (Acceso Posicional):** `obj[0$s]` → verificar que el índice sea constante `SHORTINT` positiva o identificador. Mapear al atributo en esa posición (orden de declaración).
- **Tema 30 (Herencia Múltiple — Desambiguado Opción 1):** Si un atributo/método no está en la clase del objeto, buscar en las clases heredadas **en el orden de la lista** de `EXTENDS`. El primero encontrado gana.

**Casos de prueba:**
- Clase A con `x1 EXPORT TO B`, clase B con `IMPORT FROM A` → `a1.x1` desde B es válido.
- `obj[0$s] := 10$s;` → Válido si la clase tiene al menos 1 atributo.
- `obj[-1$s] := 10$s;` → Error semántico (índice negativo).
- Herencia: clase C `EXTENDS A, B;` donde tanto A como B tienen atributo `x` → Se usa el de A (primera en la lista).

---

### Fase 8 — COMPTIME (Tema 22), Asignaciones en Expresión (Tema 17) y Salida Final

**Objetivo conceptual:** Resolver variables estáticas, asignaciones embebidas y generar la salida completa.

**Componentes a diseñar:**
- **Tema 22 (COMPTIME):** Marcar en la TS con un flag `"COMPTIME" = "true"`. Las variables COMPTIME solo se asignan con valores conocidos en compilación. No generan tercetos de runtime (se reemplazan por su valor constante en los tercetos que las usen).
- **Tema 17 (Asignación en expresión):** `a := b = (2$s);` genera:
  - Terceto `(:=, b:main, 2$s)` para la asignación interna.
  - Terceto `(:=, a:main, [anterior])` para la asignación externa.
- **Salida:** Volcado del código intermedio completo a consola (y opcionalmente archivo). Formato: cada terceto en una línea con su número.
- **Errores:** Mostrar todos los errores (léxicos, sintácticos y semánticos) juntos, con descripción y línea.
- **Tabla de Símbolos:** Imprimir contenido final enriquecido (con tipo, uso, ámbito, etc.).

**Validaciones semánticas:**
- **Tema 22:** Las variables COMPTIME solo se asignan con expresiones evaluables estáticamente (constantes u otras variables COMPTIME). Error si se asignan con llamadas a funciones o variables de runtime.
- **Tema 17:** No se permite anidamiento de asignaciones en expresión.

**Casos de prueba:**
- `COMPTIME SHORTINT x; x := 3$s;` → Válido. `x` se reemplaza por `3$s` donde se use.
- `a := b = (2$s);` → Genera 2 tercetos en el orden correcto.
- Ejecutar programa completo y verificar que la salida no tenga tercetos con destinos vacíos (`_`) ni saltos a `-1`.

---

## Parte III: Propuestas de Actualización de Documentación y Skills

### Cambios en `AGENTS.md`

Agregar al final del documento las siguientes secciones:

**Sección 9: Etapa 3 — Generación de Código Intermedio por Tercetos**
- Técnica asignada: Tercetos. Estructura dinámica (`ArrayList<Terceto>`).
- Los tercetos se imprimen con formato exacto de la cátedra: `N ( op , op1 , op2 )`.
- Backpatching: se implementa con `Stack<Integer>` y producciones "marcador" vacías en Yacc.
- Name Mangling: separador `:` estrictamente. Ejemplo: `variable:main:funcion`.

**Sección 10: Chequeos Semánticos Obligatorios**
- Lista de errores semánticos a detectar (variables no declaradas, redeclaración, tipos incompatibles, etc.).
- Referencia a las reglas de alcance y visibilidad del `Tp3.pdf`.
- Formato de errores semánticos: `Error semantico (linea X): <descripcion>`.

**Sección 11: Temas del Grupo 20 en Etapa 3**
- Tema 34: Chequeo de tipos con conversión `tos()`. Solo se permiten operaciones entre tipos distintos con conversión explícita.
- Tema 21: Inferencia de tipo en `AUTO FUNCTION` vía atributos sintetizados del `RET`.
- Tema 22: Variables `COMPTIME` evaluadas estáticamente.
- Tema 26: Chequeos de visibilidad `IMPORT FROM` / `EXPORT TO`.
- Tema 29: Resolución de acceso posicional al atributo correcto.
- Tema 30: Herencia múltiple con desambiguado por orden de lista.

### Skills Nuevos a Crear

| Skill | Archivo | Contenido |
| :--- | :--- | :--- |
| **semantica-tercetos** | `.agents/skills/semantica-tercetos/SKILL.md` | Guía de diseño para la clase `Terceto` y `GeneradorTercetos`. Formato de salida. Ejemplos de tercetos para operaciones aritméticas, `BF`, `BI`, `:=`, `CALL`, `RET`, `POUT`, `tos`. Uso de atributos sintetizados (`$$`). |
| **yacc-backpatching** | `.agents/skills/yacc-backpatching/SKILL.md` | Técnicas para insertar marcadores vacíos en Yacc sin generar conflictos. Uso de `Stack<Integer>` para parcheo. Ejemplos detallados para IF, IF-ELSE y REPEAT UNTIL. |
| **verificaciones-semanticas-grupo20** | `.agents/skills/verificaciones-semanticas-grupo20/SKILL.md` | Catálogo de todos los errores semánticos a detectar. Reglas de name mangling con `:`. Inferencia ascendente en AUTO FUNCTION. Reglas de visibilidad IMPORT/EXPORT. Resolución de acceso posicional. Desambiguado por orden en herencia múltiple. |

### Skills Existentes a Actualizar

| Skill | Cambio propuesto |
| :--- | :--- |
| `catedra-grupo20` | Agregar sección "Etapa 3: Semántica y Tercetos" con los requisitos específicos por tema. Actualizar la tabla de casos de prueba. |
| `compiladores-docente-tutor` | Agregar preguntas de defensa oral de Etapa 3. Incorporar explicaciones de tercetos y backpatching a las "Preguntas Típicas de Examen". |
| `byaccj-workflow` | Agregar nota sobre producciones "marcador" y cómo verificar que no generen conflictos. |

---

## Parte IV: Preguntas Clave para la Defensa Oral

### Sobre Código Intermedio y Tercetos

1. **"¿Qué es un terceto y en qué se diferencia de un cuádruplo?"**
   > Un terceto tiene 3 campos: `(operador, op1, op2)`. Un cuádruplo agrega un cuarto campo para el resultado temporal. Los tercetos no necesitan ese campo porque **el resultado es la propia línea** — se referencia con `[i]`. Esto los hace más compactos pero más sensibles a reordenamientos.

2. **"¿Por qué la cátedra les pide tercetos y no polaca inversa?"**
   > Son técnicas equivalentes en poder expresivo. La diferencia es organizativa: los tercetos hacen explícitas las dependencias entre operaciones (con los `[i]`), facilitando la lectura y la optimización. La polaca usa una pila implícita, lo que la hace más compacta pero difícil de rastrear para sentencias complejas.

3. **"Si tienen la sentencia `a := b + c * d;`, ¿cuántos tercetos generan y en qué orden?"**
   > Tres tercetos. Primero `( * , c , d )`, luego `( + , b , [0] )`, finalmente `( := , a , [1] )`. El orden lo dicta la precedencia del autómata LALR: el parser reduce `c * d` antes que la suma porque `*` tiene mayor precedencia declarada con `%left`.

### Sobre Backpatching

4. **"¿Cómo saben a dónde salta el `BF` de un IF si todavía no leyeron el cuerpo?"**
   > Usamos backpatching. Dejamos el destino vacío, guardamos el índice del terceto incompleto en una pila (`Stack<Integer>`), y cuando el parser reduce la regla completa (`END_IF` o `ELSE`), desapilamos y parcheamos con el índice del siguiente terceto libre.

5. **"En un IF-ELSE anidado dentro de un REPEAT UNTIL, ¿cómo garantizan que la pila de saltos no mezcle índices?"**
   > La pila es LIFO. Cada estructura push-ea su propio índice al entrar y lo pop-ea al cerrar. Como el parser es bottom-up, las estructuras internas se reducen completamente antes que las externas. El IF interno se cierra (pop) antes de que el REPEAT haga su propio pop.

6. **"¿Necesitan backpatching para el REPEAT UNTIL?"**
   > No necesariamente. En `REPEAT UNTIL`, la condición se evalúa **al final** del bloque. Al generar el `BF`, ya conocemos la dirección de destino (el inicio del bloque que guardamos al principio). No queda nada "incompleto".

7. **"Muéstrenme la regla Yacc del IF. ¿Dónde exactamente generan el terceto de salto?"**
   > En la producción del IF, justo después de evaluar la condición y antes del bloque, tenemos una producción "marcador" vacía que captura el índice actual, genera el terceto `(BF, [cond], _)` incompleto y apila el índice. Luego, en la reducción de `END_IF`, desapilamos y parcheamos.

### Sobre Tipos y Conversiones

8. **"Si tienen `a := b = c;` del Tema 17, ¿cuántos tercetos generan?"**
   > Dos. Primero la asignación interna: `( := , b , c )`. Luego la asignación externa que usa el resultado: `( := , a , [anterior] )`.

9. **"¿Cómo manejan la conversión `tos()` en el código intermedio?"**
   > Generamos un terceto unario `( tos , ref_expresion , _ )`. El tipo resultante pasa a ser `SHORTINT`. Si el argumento ya era `SHORTINT`, no es un error pero tampoco es necesaria la conversión.

10. **"Si una AUTO FUNCTION tiene `RET(2$s)` en el IF y `RET(3.0)` en el ELSE, ¿qué pasa?"**
    > Error semántico: los tipos de retorno son inconsistentes (`SHORTINT` vs `SINGLEF`). Lo detectamos al reducir cada `sentencia_ret` — comparamos el tipo de la expresión contra el tipo inferido previo. Si no coincide, reportamos el error.

### Sobre Ámbitos y Name Mangling

11. **"Veo `a:main:f1` en la Tabla de Símbolos. ¿Qué significa?"**
    > Es el name mangling. Indica que la variable `a` fue declarada en la función `f1`, que a su vez está en el ámbito global `main`. Nos permite tener otra `a:main` global sin colisión.

12. **"¿Cómo resuelven la visibilidad? Si uso una variable `x` dentro de una función, ¿buscan solo en el ámbito local?"**
    > No. Primero buscamos `x:main:funcion` (ámbito local). Si no existe, buscamos `x:main` (ámbito contenedor). Subimos iterativamente. Si no la encontramos en ningún nivel, reportamos error de variable no declarada.

### Sobre Clases y Temas Específicos

13. **"Para la herencia múltiple del Tema 30, si A y B ambas tienen atributo `x`, ¿cuál usan?"**
    > El de A, porque se declaró primero en la lista `EXTENDS A, B;`. Nuestro Desambiguado Opción 1 dice que buscamos en el orden de la lista de herencia y el primero encontrado gana.

14. **"El Tema 29 permite `obj[0$s]`. ¿Cómo sabe el compilador cuál es el atributo en la posición 0?"**
    > Los atributos de la clase se registran en la TS en orden de declaración. El índice `0$s` mapea al primer atributo declarado, `1$s` al segundo, etc. En compilación, verificamos que el índice no exceda la cantidad de atributos.

15. **"¿Cómo difiere una variable COMPTIME de una normal?"**
    > Una variable COMPTIME se evalúa completamente en tiempo de compilación. No genera tercetos de asignación en runtime. Donde se la use, se reemplaza directamente por su valor constante. Solo se puede asignar con expresiones evaluables estáticamente.

---

## Parte V: Preguntas Abiertas a Validar con la Cátedra

> [!WARNING]
> Los siguientes puntos no están completamente especificados en el material disponible. Confirmar con el JTP antes de implementar.

1. **COMPTIME (Tema 22):** ¿Las variables COMPTIME deben pre-evaluarse y reemplazarse por constantes en los tercetos (Constant Folding), o simplemente se marcan y se tratan como variables normales con restricciones semánticas?

2. **Acceso Posicional + Herencia (Tema 29 + 30):** Si hay herencia múltiple, ¿el "índice 0" del acceso posicional se refiere al primer atributo de la clase actual o incluye los heredados? ¿Se aplanan los atributos?

3. **Tema 30 (Desambiguado Opción 1):** `puntos_asignados.md` dice que "será explicado en la etapa 3". ¿Hay apuntes de clase adicionales con la especificación exacta?

4. **Errores semánticos y continuación:** Si se detecta un error semántico grave (ej. tipo incompatible), ¿se debe generar un terceto "inválido" para continuar la compilación, o se omite la generación de tercetos para esa sentencia y se sigue con la siguiente?
