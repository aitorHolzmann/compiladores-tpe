# Plan de Trabajo Detallado — Etapa 4: Generación de Código Assembler (Pentium x86 / MASM32)

**Proyecto**: Compilador Grupo 20 — Diseño de Compiladores I  
**Objetivo**: Traducir el código intermedio (Tercetos agrupados por ámbito) y la Tabla de Símbolos a código **Assembler x86 de 32 bits compatible con MASM32**, implementando el mecanismo de **Variables Auxiliares** y los **4 chequeos en tiempo de ejecución asignados al Grupo 20 (`c, d, g, i`)**.

---

## 0. Reglas Estrictas de Implementación (Para el Agente Implementador)

1. **Cero Sobre-Ingeniería / Estilo Alumno de Ingeniería**:
   - Todo el generador de Assembler se implementa en **una única clase nueva**: `src/compilador/GeneradorAssembler.java`.
   - **Prohibido** crear jerarquías de clases (`Instruction`, `RegisterAllocator`, `AsmNode`, `Visitor`, `Strategy`), enumeraciones complejas, `Streams`, expresiones `lambda` (`->`), `Optional` o `record`.
   - Usar únicamente estructuras clásicas vistas en la cátedra: `StringBuilder`, `ArrayList<Terceto>`, `Map<String, ArrayList<Terceto>>`, `HashSet<Integer>`, bucles `for` tradicionales y bloques `if / else if / else`.
   - Comentarios concisos en español natural de alumno (ej. `// Hito 2: suma de enteros cortos en AL`, `// Chequeo c: overflow en suma de flotantes`).
2. **Apego 100% a la Teoría de la Cátedra (`teoria/teoria_parte4/` y `teoria/Tp04-2026.pdf`)**:
   - **Mecanismo obligatorio (`Tp04-2026.pdf`, pág. 1)**: **Variables Auxiliares (`@aux`)**. Cada terceto que produce un valor intermedio (`+`, `-`, `*`, `/`, `tos`, `CALL`) guarda su resultado en una variable auxiliar en memoria y se registra en la `TablaSimbolos` con `USO = "auxiliar"` y su `TIPO` (`SHORTINT` o `SINGLEF`).
   - **Aritmética Entera (`SHORTINT`, 8 bits con signo `[-128, 127]`)**: Se resuelve en los registros generales de la CPU (`AL`, `BL`, `CL`, `AX`).
   - **Aritmética de Punto Flotante (`SINGLEF`, 32 bits IEEE 754)**: Se resuelve obligatoriamente en la pila del **Coprocesador Matemático 80x87 (`ST(0)..ST(7)`)**.
   - **Etiquetas de Salto**: Se utiliza la **Alternativa 1 de la cátedra** (`generacion_codigo_tercetos_control.md`): pre-identificar en un conjunto (`HashSet<Integer>`) los índices destino de los tercetos `BF` y `BI` de cada ámbito, y emitir la etiqueta `Label_<ambito>_<i>:` únicamente antes de esos tercetos (incluyendo la etiqueta al final de la lista si un salto apunta a `lista.size()`).
3. **Respeto del Flujo de Compilación (`byaccj`)**:
   - Si se modifica `gram.y`, siempre compilar con:
     ```bash
     byaccj -J -v gram.y
     sed -i '1s/^/package src.compilador;\n/' ParserVal.java
     sed -i '1s/^/package src.compilador;\n/' Parser.java
     mv Parser.java ParserVal.java src/compilador/
     javac -d bin $(find src -name "*.java")
     ```
   - Verificar siempre en `y.output` que existan **0 conflictos shift/reduce y 0 conflictos reduce/reduce**.
   - El archivo `.asm` **solo debe generarse si no hubo errores léxicos, sintácticos ni semánticos** (`totalErrores == 0`).

---

## Parte I: Guía Pedagógica de Assembler x86 (De Cero)

Antes de escribir una línea de código, entendamos cómo piensa el procesador Pentium x86 y cómo se conecta con los Tercetos que ya generamos en la Etapa 3.

### 1. ¿Qué es un archivo `.asm` de MASM32?

Un programa en Assembler no tiene `while`, `if` con llaves ni expresiones anidadas: solo tiene **datos en memoria** (sección `.data`) e **instrucciones elementales ejecutadas una tras otra** (sección `.code`).

El esqueleto oficial exigido por la cátedra (`assembler_windows.pdf` y `ej_impresion.md`) es:

```nasm
.386
.model flat, stdcall
option casemap :none
include \masm32\include\masm32rt.inc
includelib \masm32\lib\kernel32.lib
includelib \masm32\lib\masm32.lib
dll_dllcrt0 PROTO C
printf PROTO C :VARARG

.data
    ; Acá se declaran TODAS las variables del programa, las constantes y las @aux

.code
    ; Acá van las subrutinas (funciones y métodos de clases)

start:
    ; Acá empieza la ejecución del ámbito "main"
    ...
    invoke ExitProcess, 0
end start
```

* **`.386`**: Habilita el conjunto de instrucciones de 32 bits del Intel 80386/Pentium.
* **`.model flat, stdcall`**: Define un espacio de memoria lineal único ("flat", sin segmentos antiguos) y la convención de llamadas estándar de Windows (`stdcall`).
* **`option casemap :none`**: Hace que el ensamblador distinga mayúsculas de minúsculas en los nombres de variables y etiquetas.
* **`printf PROTO C :VARARG`**: Nos permite usar la función `printf` de C para imprimir por consola (nuestra sentencia `POUT`).

---

### 2. La Memoria (`.data`) y nuestros Tipos de Datos del Grupo 20

En Assembler x86, las variables en `.data` no tienen "tipos de alto nivel", sino **tamaños en bytes**:

| directiva MASM | Tamaño | Nombre en cátedra | Uso en el Grupo 20 | Ejemplo en `.data` |
| :--- | :--- | :--- | :--- | :--- |
| **`DB`** (*Define Byte*) | 1 byte (8 bits) | `BYTE` / `SBYTE` | Variables, constantes y `@aux` de tipo **`SHORTINT`** (`[-128, 127]`), banderas de recursión y cadenas de texto | `_main@a DB ?`<br>`_cte_10s DB 10`<br>`_str_0 DB "Hola", 0` |
| **`DW`** (*Define Word*) | 2 bytes (16 bits) | `WORD` / `SWORD` | Auxiliar de 16 bits para conversión `tos()` y para promoción en `printf` | `@aux_tos DW ?`<br>`@aux_print_short DW ?` |
| **`DD`** (*Define Doubleword*) | 4 bytes (32 bits) | `DWORD` / `REAL4` | Variables, constantes y `@aux` de tipo **`SINGLEF`** (float de 32 bits) | `_main@f DD ?`<br>`_cte_3_5 DD 3.5` |
| **`DQ`** (*Define Quadword*) | 8 bytes (64 bits) | `QWORD` / `REAL8` | Auxiliar de 64 bits exigido por `printf` de Windows para imprimir flotantes (`printf-variables.pdf`) | `@aux_print_float DQ ?` |

> **¿Qué significa `?` en `DB ?` o `DD ?`?**  
> Significa variable **no inicializada** (reserva el espacio en memoria, igual que declarar `SHORTINT a;` en nuestro lenguaje). Las constantes en cambio llevan su valor literal (`DB 10`, `DD 3.5`).

---

### 3. Los Registros de la CPU (Para `SHORTINT` de 8 bits)

El procesador tiene 4 registros de propósito general de 32 bits (`EAX`, `EBX`, `ECX`, `EDX`). Lo genial de x86 es que los 16 bits bajos de `EAX` se llaman `AX`, y `AX` a su vez se divide en dos mitades de 8 bits: `AH` (parte alta) y `AL` (parte baja):

```text
[        EAX (32 bits)        ]
                [ AX (16 bits)]
                [ AH ] [  AL  ]  <-- AL tiene 8 bits (1 byte = exactamente un SHORTINT)
```

Como nuestro tipo entero es **`SHORTINT` (8 bits con signo, `[-128, 127]`)**:
- **`MOV AL, _main@a`**: Copia el byte de la variable `_main@a` al registro `AL`. (Recordá: en x86 **nunca** se puede hacer `MOV mem1, mem2` directamente de memoria a memoria; siempre pasamos por un registro).
- **`ADD AL, _main@b`**: Suma `AL <- AL + _main@b`.
- **`SUB AL, _main@b`**: Resta `AL <- AL - _main@b`.
- **`IMUL _main@b`** (*Integer Multiply* con signo de 8 bits): Multiplica `AL` por el operando de 8 bits y deja el resultado en **`AX`** (16 bits), pero la parte baja queda en `AL`. Además, **activa el flag de Overflow (`OF = 1`) si el resultado no entra en 1 byte con signo (`[-128, 127]`)** (`11.1.Assembler_Pentium_Sintaxis.pdf`, pág. 22).
- **`CBW` + `IDIV BL`** (*Integer Divide* con signo de 8 bits): Para dividir en 8 bits, `IDIV` exige que el dividendo esté en `AX` (16 bits) y el divisor en un registro/memoria de 8 bits (ej. `BL`). La instrucción `CBW` (*Convert Byte to Word*) extiende el signo de `AL` hacia `AH` (rellena `AH` con `00000000` si `AL` es positivo, o `11111111` si es negativo). Luego `IDIV BL` divide `AX / BL` y deja el **cociente en `AL`** (y el resto en `AH`).

---

### 4. El Coprocesador Matemático 80x87 (Para `SINGLEF` de 32 bits)

Los registros `EAX..EDX` solo entienden números enteros. Para operar con flotantes (`SINGLEF`), el Pentium tiene adentro un coprocesador llamado **80x87 (FPU)** que funciona como una **pila de 8 registros** llamados `ST(0)` (el tope de la pila) hasta `ST(7)` (`11.1.Assembler_Pentium_Sintaxis.pdf`, pág. 25):

- **`FLD op`** (*Float Load*): Empuja (`push`) un `SINGLEF` (`DD` en memoria) al tope de la pila `ST(0)`.
- **`FADD op`**: Suma `ST(0) <- ST(0) + op`.
- **`FSUB op`**: Resta `ST(0) <- ST(0) - op`.
- **`FMUL op`**: Multiplica `ST(0) <- ST(0) * op`.
- **`FDIV op`**: Divide `ST(0) <- ST(0) / op`.
- **`FSTP destino`** (*Float Store and Pop*): Guarda el valor de `ST(0)` en la variable `destino` (`DD` en memoria) y **desapila** (`pop`), dejando la pila del coprocesador limpia.
- **`FILD mem16`** (*Float Integer Load*): Toma un entero de memoria, lo convierte a punto flotante y lo apila en `ST(0)`. (¡Así resolvemos nuestra conversión implícita `SHORTINT -> SINGLEF` en asignaciones!).

---

### 5. ¿Cómo funcionan las Comparaciones y los Saltos (`IF` y `REPEAT UNTIL`)?

En Tercetos, cuando escribimos `IF (a > b) THEN ...`, nuestro compilador en Etapa 3 generó:
```text
0 ( > , main@a , main@b )
1 ( BF , [0] , [5] )
```
`BF` significa **Bifurcar por Falso**: *"Si la condición del terceto 0 NO se cumple, saltar al terceto 5"*.
Por eso en Assembler usamos la **lógica inversa** (`generacion_codigo_tercetos_control.md`, sección 4):
- Si el terceto es `>` (mayor), el `BF` debe saltar cuando sea **menor o igual**.
- Si el terceto es `==` (igual), el `BF` debe saltar cuando sea **distinto**.

**¡Atención a una pregunta clásica de defensa oral!**  
Los saltos en Assembler son distintos según comparemos enteros (`SHORTINT` con signo) o flotantes (`SINGLEF` en el coprocesador):
1. **Para `SHORTINT` (con signo)**: Usamos `MOV AL, op1` seguido de `CMP AL, op2`. `CMP` resta internamente `AL - op2` y actualiza los flags de la CPU (`ZF`, `SF`, `OF`). Luego usamos saltos con signo (*Greater / Less*): `JLE`, `JGE`, `JL`, `JG`, `JNE`, `JE`.
2. **Para `SINGLEF` (en 80x87)**: El coprocesador compara con `FLD op1` y `FCOMP op2`, pero **guarda el resultado en sus propios flags internos (`C3, C2, C0`), NO en los flags de la CPU**. Para que la CPU pueda saltar, hay que copiar los flags del coprocesador a `AX` (`FSTSW AX`) y pasarlos al registro `EFLAGS` (`SAHF`). Como `C0` va a parar al *Carry Flag* (`CF`), después de `SAHF` se deben usar obligatoriamente los **saltos sin signo (*Above / Below*)**: `JBE`, `JAE`, `JB`, `JA`, `JNE`, `JE` (`11.2.Assembler_Pentium_Conversiones.pdf`, pág. 6).

---

## Parte II: Plan de Implementación Paso a Paso (5 Hitos)

Cada hito a continuación está especificado con exactitud quirúrgica para ser implementado uno por uno, compilado y verificado.

---

### HITO 1: Infraestructura Base, Esqueleto `.asm`, Sección `.data` y `POUT`

#### 1.1. Objetivo Pedagógico
Lograr que el compilador tome cualquier programa válido sin errores, recorra la Tabla de Símbolos y los tercetos de `main`, y genere un archivo `<entrada>.asm` capaz de declarar todas las variables/constantes y mostrar cadenas, enteros cortos y flotantes por consola con `printf`.

#### 1.2. Archivos a Modificar / Crear
1. **`src/compilador/GeneradorTerceto.java`**:
   - Agregar un método getter simple para exponer el mapa de ámbitos al generador de Assembler:
     ```java
     public Map<String, ArrayList<Terceto>> obtenerTercetosPorAmbito() {
         return this.tercetosPorAmbito;
     }
     ```
2. **`src/compilador/TablaSimbolos.java`**:
   - Agregar un método getter simple para recorrer todas las entradas en orden:
     ```java
     public Map<String, Map<String, String>> obtenerEntradas() {
         return TablaSimbolos.tabla;
     }
     ```
3. **`src/compilador/GeneradorAssembler.java`** *(Archivo Nuevo)*:
   - Atributos de instancia:
     - `private GeneradorTerceto genTercetos;`
     - `private TablaSimbolos tablaSimbolos;`
     - `private Map<String, String> mapaNombresAsm;` (mapea la clave de la Tabla de Símbolos, ej. `"main@a"` o `"-5"` o `"\"hola\""`, a un identificador MASM32 válido y único en `.data`).
     - `private int contadorCte = 0;`
   - **Regla de nombres Assembler (`obtenerNombreAsm(String claveTS)`)**:
     - Si `claveTS` empieza con `"@"` (variables auxiliares o internas), se devuelve tal cual (MASM32 permite `@` al inicio de identificadores).
     - Si la entrada tiene `USO = "constante"`:
       - Si es `CADENA`: asignarle `_str_<contadorCte++>`.
       - Si es `SHORTINT` o `SINGLEF`: reemplazar `-` por `neg_`, `.` por `_`, `+` por `pos_` y prefijar con `_cte_` (ej. `-10` $\to$ `_cte_neg_10`, `3.5` $\to$ `_cte_3_5`). Guardar en `mapaNombresAsm`.
     - Si es variable, atributo o parámetro (`claveTS` contiene `@`, ej. `main@x`): prefijar con `_` $\to$ `_main@x` (MASM32 acepta `@` dentro de nombres de símbolos).
   - **Generación de `.data`**:
     - Recorrer `tablaSimbolos.obtenerEntradas()`:
       - **Variables (`USO` = `"variable"`, `"parametro"`, `"auxiliar"`)**:
         - **Importante (Tema 22)**: Si la entrada tiene atributo `"COMPTIME"` igual a `"SI"`, **no es necesario o bien puede declararse inicializada**, pero como sus usos ya fueron reemplazados por su valor constante en Etapa 3, si se declara con su `VALOR` o `DB ?`/`DD ?` no molesta. Mejor aún: si tiene `"COMPTIME" = "SI"`, omitirla o declararla comentada para dejar claro que es de tiempo de compilación.
         - Si `TIPO` es `"SHORTINT"`: `<nombreAsm> DB ?`
         - Si `TIPO` es `"SINGLEF"`: `<nombreAsm> DD ?`
       - **Constantes (`USO` = `"constante"`)**:
         - Si `TIPO` es `"SHORTINT"`: `<nombreAsm> DB <claveTS>`
         - Si `TIPO` es `"SINGLEF"`:
           - Reemplazar `'s'` o `'S'` por `'E'` en el literal por si tiene notación científica del Grupo 20 (ej. `1.5s+2` $\to$ `1.5E+2`).
           - Si el literal empieza con `.` (ej. `.5`), anteponer `0` (`0.5`) porque MASM32 exige dígito antes del punto en directivas `DD`.
           - Si el literal termina en `.` (ej. `3.`), agregar `0` (`3.0`).
           - Si el literal vale `0.0` o `-0.0`, emitir `<nombreAsm> DD 0.0`.
         - Si `TIPO` es `"CADENA"`:
           - Quitar las comillas externas `"..."` del lexema. Si la cadena está vacía `""`, emitir `<nombreAsm> DB 0`. Si tiene texto, emitir `<nombreAsm> DB "<texto>", 0`.
     - Declarar siempre al final de `.data` las variables de soporte fijo para impresión (`printf-variables.pdf`):
       ```nasm
       @aux_print_short DW ?
       @aux_print_float DQ ?
       ```
   - **Traducción de terceto `POUT` (`generarPout(Terceto t)`)**:
     - Determinar el tipo del `operando1` consultando `tablaSimbolos.obtenerTipo(op1)`:
       - Si es `"CADENA"`:
         ```nasm
         invoke printf, cfm$("%s\n"), OFFSET <nombreAsm(op1)>
         ```
       - Si es `"SHORTINT"` (promoción de 8 bits con signo a 16 bits según `printf-variables.pdf`):
         ```nasm
         MOV AL, <nombreAsm(op1)>
         MOVSX AX, AL
         MOV @aux_print_short, AX
         invoke printf, cfm$("%hd\n"), @aux_print_short
         ```
       - Si es `"SINGLEF"` (promoción de 32 bits `DD` a 64 bits `DQ` en coprocesador según `printf-variables.pdf` pág. 2):
         ```nasm
         FLD <nombreAsm(op1)>
         FSTP @aux_print_float
         invoke printf, cfm$("%Lf\n"), @aux_print_float
         ```
4. **`gram.y` (método `main`)**:
   - Al finalizar `par.yyparse()`, si `totalErrores == 0`, instanciar `GeneradorAssembler`, generar el archivo `.asm` en la misma ruta del archivo fuente cambiando/agregando extensión `.asm`, e imprimir por consola el código Assembler generado (o el mensaje indicando la ruta del `.asm` generado).

---

### HITO 2: Variables Auxiliares (`@aux`), Expresiones Aritméticas, Asignaciones y `tos()` (Tema 34)

#### 2.1. Objetivo Pedagógico
Entender cómo se desarman expresiones compuestas usando **Variables Auxiliares (`@aux`)**, cómo operan los enteros cortos en `AL`/`AX`, cómo operan los flotantes en la pila `ST(0)` del coprocesador 80x87, cómo se hace la conversión implícita `SHORTINT -> SINGLEF` en `:=` y cómo funciona la conversión explícita `tos(expr)` (`SINGLEF -> SHORTINT`).

#### 2.2. Ajuste Clave en `gram.y`: Plegado de Constantes solo en `COMPTIME` (Tema 22)
- **Por qué es necesario**: En la Etapa 3 (`gram.y`, líneas 747, 776, 814, 843 y 1295), cualquier suma/producto entre constantes literales (como `a := 100$s * 2$s;` o `a := tos(10.5);`) se resolvía estáticamente en Java sin emitir tercetos. En la Etapa 4 necesitamos que las sentencias ejecutables normales **emitan los tercetos `+`, `-`, `*`, `/`, `tos`** para que se traduzcan a Assembler y puedan disparar los **chequeos en tiempo de ejecución `c`, `d`, `g`** pedidos por la cátedra.
- **Cómo implementarlo limpiamente**:
  - En `gram.y`, agregar un atributo booleano `boolean enComptime = false;`.
  - En la regla `declaracion_comptime`, poner un marcador o activar `enComptime = true` antes de `expresion` y `enComptime = false` al terminar la declaración `COMPTIME`.
  - En `expresion + termino`, `expresion - termino`, `termino * factor`, `termino / factor` y `conversion_tos`, cambiar la condición `if (esConstante($1.sval) && esConstante($3.sval))` por `if (enComptime && esConstante($1.sval) && esConstante($3.sval))` (y `if (enComptime && esConstante($3.sval))` en `conversion_tos`).
  - Así el **Tema 22 (`COMPTIME`)** sigue evaluándose 100% en compilación sin generar tercetos de runtime, mientras que el resto del programa genera tercetos reales para Assembler.

#### 2.3. Mecanismo de Variables Auxiliares (`@aux`) en `GeneradorAssembler.java`
1. **Pre-registro antes de emitir `.data` (`registrarVariablesAuxiliares()`)**:
   - Recorrer todos los ámbitos en `tercetosPorAmbito`. Para cada terceto con índice `i` en el ámbito `amb`:
   - Si el operador es `"+"`, `"-"`, `"*"`, `"/"`, `"tos"` o `"CALL"`:
     - Construir el nombre de la variable auxiliar: `@aux_<ambLimpio>_<i>` (donde `<ambLimpio>` reemplaza `@` por `_`, ej. `@aux_main_0`).
     - Registrar esa variable en `tablaSimbolos` usando `tablaSimbolos.agregarSimbolo(nombreAux, t.getTipo())` y `tablaSimbolos.agregarAtributo(nombreAux, "USO", "auxiliar")`.
     - De esta forma, la variable auxiliar **aparece en la Tabla de Símbolos** (como pide `Tp04-2026.pdf`) y se declara automáticamente en `.data` (`DB ?` si es `SHORTINT`, `DD ?` si es `SINGLEF`).
2. **Resolución de operandos de un terceto (`resolverOperando(String op, String ambitoActual)`)**:
   - Si `op` es una referencia a otro terceto (`"[k]"`): devolver `@aux_<ambLimpio>_<k>`.
   - Si no, devolver `obtenerNombreAsm(op)`.

#### 2.4. Plantillas de Traducción de Tercetos Aritméticos y Asignación
Sea `op1 = resolverOperando(t.getOperando1())`, `op2 = resolverOperando(t.getOperando2())`, `aux = "@aux_" + ambLimpio + "_" + i`:

1. **Operaciones `SHORTINT` (`t.getTipo().equals("SHORTINT")`)**:
   - **Suma (`+`)**:
     ```nasm
     MOV AL, <op1>
     ADD AL, <op2>
     MOV <aux>, AL
     ```
   - **Resta (`-`)**:
     ```nasm
     MOV AL, <op1>
     SUB AL, <op2>
     MOV <aux>, AL
     ```
   - **Multiplicación (`*`)**:
     ```nasm
     MOV AL, <op1>
     IMUL <op2>
     ; (En Hito 5 acá se inserta JO _err_overflow_entero)
     MOV <aux>, AL
     ```
   - **División (`/`)**:
     ```nasm
     MOV AL, <op1>
     CBW
     MOV BL, <op2>
     IDIV BL
     MOV <aux>, AL
     ```
2. **Operaciones `SINGLEF` (`t.getTipo().equals("SINGLEF")`)**:
   - **Suma (`+`)**:
     ```nasm
     FLD <op1>
     FADD <op2>
     FSTP <aux>
     ; (En Hito 5 acá se inserta el chequeo de overflow flotante sobre <aux>)
     ```
   - **Resta (`-`)**:
     ```nasm
     FLD <op1>
     FSUB <op2>
     FSTP <aux>
     ```
   - **Multiplicación (`*`)**:
     ```nasm
     FLD <op1>
     FMUL <op2>
     FSTP <aux>
     ```
   - **División (`/`)**:
     ```nasm
     FLD <op1>
     FDIV <op2>
     FSTP <aux>
     ```
3. **Conversión explícita `tos` (`SINGLEF -> SHORTINT`, Tema 34)**:
   - Necesitamos una variable auxiliar de 16 bits en `.data`: `@aux_tos DW ?`.
   - En el Hito 2 (versión base sin el chequeo de pérdida de información que completaremos en el Hito 5) o usando truncamiento hacia cero:
     ```nasm
     FLD <op1>
     FISTP @aux_tos
     MOV AX, @aux_tos
     MOV <aux>, AL
     ```
4. **Asignación (`:=`)**:
   - Determinar `tipoDest` (del `operando1`) y `tipoOrig` (del `operando2`, ya sea desde la TS o desde `lista.get(idx).getTipo()` si es `[idx]`):
   - **Caso 1: `SHORTINT := SHORTINT`**:
     ```nasm
     MOV AL, <op2>
     MOV <op1>, AL
     ```
   - **Caso 2: `SINGLEF := SINGLEF`**:
     ```nasm
     FLD <op2>
     FSTP <op1>
     ```
   - **Caso 3: Conversión implícita `SINGLEF := SHORTINT` (`Tp04-2026.pdf` y Tema 34)**:
     - El procesador 80x87 no tiene `FILD` para 8 bits directo (solo 16, 32 o 64 bits). Por lo tanto, promovemos el `SHORTINT` de 8 bits a 16 bits con signo en `@aux_tos DW ?` y lo cargamos con `FILD`:
     ```nasm
     MOV AL, <op2>
     CBW
     MOV @aux_tos, AX
     FILD @aux_tos
     FSTP <op1>
     ```

---

### HITO 3: Sentencias de Control (`IF / ELSE` y `REPEAT UNTIL` - Tema 12)

#### 3.1. Objetivo Pedagógico
Comprender cómo los tercetos de comparación (`>`, `<`, `>=`, `<=`, `==`, `!=`) y las bifurcaciones (`BF`, `BI`) se transforman en etiquetas (`Label_...`) y saltos condicionales/incondicionales en Assembler, tanto en la ALU (para `SHORTINT`) como en el coprocesador 80x87 (para `SINGLEF`).

#### 3.2. Recolección de Etiquetas Destino (Alternativa 1 de la Cátedra)
Antes de traducir la lista de tercetos `lista` de un ámbito `amb`:
1. Crear `HashSet<Integer> etiquetas = new HashSet<Integer>();`.
2. Recorrer `lista`:
   - Si `op.equals("BF")`: extraer el número `dest` de `t.getOperando2()` (ej. `"[12]"` $\to$ `12`) y hacer `etiquetas.add(dest)`.
   - Si `op.equals("BI")`: extraer el número `dest` de `t.getOperando1()` (ej. `"[15]"` $\to$ `15`) y hacer `etiquetas.add(dest)`.
3. Durante la recorrida de generación de código (`for (int i = 0; i < lista.size(); i++)`):
   - Si `etiquetas.contains(i)`: emitir `Label_<ambLimpio>_<i>:` antes del código del terceto `i`.
4. Al terminar el `for`:
   - Si `etiquetas.contains(lista.size())`: emitir `Label_<ambLimpio>_<lista.size()>:`.

#### 3.3. Traducción de Comparaciones y `BF` / `BI`
En `GeneradorAssembler`, guardar en una variable `String ultimoOperadorComp = "";` y `String ultimoTipoComp = "";` cuando se procesa un terceto de comparación (`>`, `<`, `>=`, `<=`, `==`, `!=`):

1. **Cuando el terceto `i` es un comparador (`>`, `<`, `>=`, `<=`, `==`, `!=`)**:
   - Guardar `ultimoOperadorComp = t.getOperador();`
   - Determinar si los operandos son `SHORTINT` o `SINGLEF` (si alguno es `SINGLEF`, la comparación se hace en `SINGLEF`):
   - **Si ambos son `SHORTINT`**:
     ```nasm
     MOV AL, <op1>
     CMP AL, <op2>
     ```
   - **Si son `SINGLEF`** (según `11.2.Assembler_Pentium_Conversiones.pdf`, pág. 6):
     ```nasm
     FLD <op1>
     FCOMP <op2>
     FSTSW AX
     SAHF
     ```
     *(Nota: si uno de los dos operandos fuera `SHORTINT` y el otro `SINGLEF`, primero se convierte el `SHORTINT` a float con `MOV AL, op` / `CBW` / `MOV @aux_tos, AX` / `FILD @aux_tos` / `FSTP @aux_conv_float`).*

2. **Cuando el terceto `i` es `BF` (`( BF , [cond] , [dest] )`)**:
   - El salto se realiza por **condición invertida** hacia `Label_<ambLimpio>_<dest>`:

   | Operador en Terceto | Condición Falsa | Salto `SHORTINT` (con signo) | Salto `SINGLEF` (80x87 + `SAHF`) |
   | :---: | :---: | :---: | :---: |
   | `>` | $\le$ | `JLE Label_...` | `JBE Label_...` |
   | `>=` | $<$ | `JL Label_...` | `JB Label_...` |
   | `<` | $\ge$ | `JGE Label_...` | `JAE Label_...` |
   | `<=` | $>$ | `JG Label_...` | `JA Label_...` |
   | `==` | $\ne$ | `JNE Label_...` | `JNE Label_...` |
   | `!=` | $=$ | `JE Label_...` | `JE Label_...` |

3. **Cuando el terceto `i` es `BI` (`( BI , [dest] , _ )`)**:
   ```nasm
   JMP Label_<ambLimpio>_<dest>
   ```

> **¿Y qué pasa con `REPEAT ... UNTIL (condicion)` (Tema 12)?**  
> ¡Ya está resuelto automáticamente! En la Etapa 3 (`gram.y`, línea 489), `REPEAT UNTIL` genera exactamente una comparación seguida de `( BF , [cond] , [inicio_repeat] )` (salta al inicio mientras la condición de salida sea falsa). Por lo tanto, el mismo mecanismo de `BF` e identificación de etiquetas hace funcionar `IF`, `IF-ELSE` y `REPEAT UNTIL` sin escribir ni una regla especial.

---

### HITO 4: Funciones, `AUTO FUNCTION` (Tema 21), Métodos, Objetos y Acceso Posicional (Temas 26, 29, 30)

#### 4.1. Objetivo Pedagógico
Entender cómo se estructuran las subrutinas en `.code` (`PROC` / etiquetas + `RET`), cómo se pasan parámetros y valores de retorno en el esquema estático de la cátedra, y cómo se representan en memoria los atributos de objetos (incluyendo herencia `EXTENDS` y acceso posicional `obj[i]`).

#### 4.2. Subrutinas (`FUNCTION`, `AUTO FUNCTION` y Métodos de Clase)
En `GeneradorTerceto`, el mapa `tercetosPorAmbito` tiene una entrada `"main"` y una entrada por cada función o método (ej. `"main@duplicar"`, `"main@c_base_a@metodo_a"`):

1. **Ubicación en `.code`**:
   - Antes de la etiqueta `start:`, recorrer todos los ámbitos de `tercetosPorAmbito` cuyo nombre **no** sea `"main"`.
   - Para cada ámbito `amb`, emitir la etiqueta de la subrutina `_proc_<ambLimpio>:` seguida de su prólogo de pila (recomendado en `printf-variables.pdf`, pág. 3, para que `invoke printf` dentro de funciones no corrompa la dirección de retorno):
     ```nasm
     _proc_<ambLimpio>:
         PUSH EBP
         MOV EBP, ESP
         ; ... tercetos del ámbito ...
     _fin_<ambLimpio>:
         MOV ESP, EBP
         POP EBP
         RET
     ```
2. **Retorno de Función (`RET`)**:
   - Para cada función en la Tabla de Símbolos (`USO = "funcion"`), declarar en `.data` una variable de retorno `@ret_<ambLimpio>` (`DB ?` si `TIPO` es `SHORTINT`, `DD ?` si es `SINGLEF`).
   - Cuando aparece un terceto `( RET , val , _ )` dentro del ámbito `amb`:
     - Si el tipo de la función es `SHORTINT` (y si `val` es `SHORTINT`):
       ```nasm
       MOV AL, <op1>
       MOV @ret_<ambLimpio>, AL
       JMP _fin_<ambLimpio>
       ```
     - Si el tipo de la función es `SINGLEF`:
       ```nasm
       FLD <op1>
       FSTP @ret_<ambLimpio>
       JMP _fin_<ambLimpio>
       ```
3. **Invocación (`CALL`)**:
   - En la Etapa 3 (`gram.y`, líneas 989 y 1179), antes del terceto `CALL` ya se emitió un terceto `:=` que copia el argumento real en el parámetro formal (`main@func@param := arg`).
   - Cuando aparece `( CALL , claveFunc , param )` en el terceto `i` del ámbito actual:
     - Si `claveFunc` es una función (`USO = "funcion"`):
       ```nasm
       CALL _proc_<claveFuncLimpia>
       ; Copiar @ret_<claveFuncLimpia> a la variable auxiliar @aux_<ambActual>_<i>
       ```
     - Si `claveFunc` es una invocación a método de objeto (`"main@obj.metodo"`, es decir contiene `.`):
       - Separar la instancia (`main@obj`) y el nombre del método (`metodo`).
       - Buscar la clase de `main@obj` (`tablaSimbolos.obtenerTipo("main@obj")`) y resolver en qué clase (`main@Clase` o ancestro por `EXTENDS`) está definido `metodo` para obtener la etiqueta del procedimiento `_proc_<ambitoMetodoLimpio>`.
       - **Sincronización de atributos del objeto (`this`)**:
         - Antes de `CALL _proc_<ambitoMetodoLimpio>`: copiar los atributos de la instancia `_main@obj@<atr>` a las variables de atributo de la clase `_main@<Clase>@<atr>` que usa el cuerpo del método.
         - Ejecutar `CALL _proc_<ambitoMetodoLimpio>`.
         - Después de `CALL`: copiar de vuelta las variables `_main@<Clase>@<atr>` hacia `_main@obj@<atr>` (por si el método modificó atributos del objeto).

#### 4.3. Atributos de Objetos, Herencia (`EXTENDS`, Tema 30) y Acceso Posicional (`obj[i]`, Tema 29)
En `test_etapa3_integrador.txt` y en general en el Grupo 20, los objetos pueden acceder a sus atributos por nombre (`obj.x`) o por posición (`obj[0$s]` o `obj[idx]`, Tema 29):

1. **Lista completa de atributos de una clase (incluyendo heredados)**:
   - Agregar o actualizar en `TablaSimbolos.java` un método `obtenerTodosLosAtributosClase(String claveClase)` que devuelva en orden los atributos propios de la clase y, si tiene `ANCESTROS` (`EXTENDS`), los atributos de sus clases base (sin duplicar nombres).
2. **Declaración en `.data`**:
   - Para cada variable de instancia de clase (`USO = "variable"` cuyo `TIPO` empieza con `"main@"`):
     - Para cada atributo `atr` de su clase (incluyendo heredados), asegurar que exista en `.data` la variable `_<claveObj>@<nombreAtr>` (`DB ?` o `DD ?` según el tipo del atributo).
3. **Resolución de `obj[indice]` en `GeneradorAssembler`**:
   - Cuando un operando de terceto tiene formato `claveObj[indice]` (ej. `main@obj_deriv[0$s]` o `main@obj_deriv[main@a]`):
   - Obtener la lista ordenada de atributos de la clase de `claveObj`: `atr_0, atr_1, ..., atr_{n-1}`.
   - **Caso A: `indice` es una constante entera (`0`, `1`, ...)** $\to$ se resuelve directamente al nombre Assembler del atributo k-ésimo: `_<claveObj>@<nombreAtr_k>`.
   - **Caso B: `indice` es una variable (`main@a`)**:
     - Si el operando está del **lado derecho** (lectura): antes de la instrucción que lo usa, emitir una secuencia simple de comparación (`CMP _main@a, 0` $\to$ `JE`, `CMP _main@a, 1` $\to$ `JE`...) que copie el atributo correspondiente a una variable temporal `@aux_pos_<i_terceto>` y usar `@aux_pos_<i_terceto>` como operando.
     - Si el operando está del **lado izquierdo** de `:=` (escritura `obj[main@a] := valor`): después de evaluar `valor`, comparar `_main@a` con `0, 1, ...` y mover el valor a `_<claveObj>@<nombreAtr_k>`.
     - *(¡Esto evita aritmética de punteros compleja y funciona incluso si la clase mezcla atributos `SHORTINT` de 1 byte y `SINGLEF` de 4 bytes!).*

---

### HITO 5: Los 4 Controles en Tiempo de Ejecución del Grupo 20 (`c, d, g, i`) y Defensa

#### 5.1. Objetivo Pedagógico
Según la tabla de la página 2 de `Tp04-2026.pdf`, al **GRUPO 20** le corresponden los chequeos **`c, d, g, i`**. Si cualquiera de estos errores ocurre durante la ejecución del `.exe`, el programa debe mostrar un mensaje claro por pantalla y terminar inmediatamente (`invoke ExitProcess, 1`).

#### 5.2. Mensajes y Rutinas de Error al Final de `.code`
En `.data`:
```nasm
_msg_err_ovf_float   DB "Error en tiempo de ejecucion: Overflow en suma de punto flotante (SINGLEF)", 0
_msg_err_ovf_entero  DB "Error en tiempo de ejecucion: Overflow en producto de enteros (SHORTINT)", 0
_msg_err_perdida_tos DB "Error en tiempo de ejecucion: Perdida de informacion en conversion tos()", 0
_msg_err_recursion   DB "Error en tiempo de ejecucion: Recursion mutua o directa no permitida en funciones", 0
@max_singlef         DD 3.40282347E+38
@min_shortint_f      DD -128.0
@max_shortint_f      DD 127.0
```
Al final de `.code` (después de `invoke ExitProcess, 0` de `start:`):
```nasm
_err_overflow_float:
    invoke printf, cfm$("%s\n"), OFFSET _msg_err_ovf_float
    invoke ExitProcess, 1

_err_overflow_entero:
    invoke printf, cfm$("%s\n"), OFFSET _msg_err_ovf_entero
    invoke ExitProcess, 1

_err_perdida_info:
    invoke printf, cfm$("%s\n"), OFFSET _msg_err_perdida_tos
    invoke ExitProcess, 1

_err_recursion:
    invoke printf, cfm$("%s\n"), OFFSET _msg_err_recursion
    invoke ExitProcess, 1
```

#### 5.3. Detalle Exacto de cada Chequeo del Grupo 20

##### Chequeo `c`: Overflow en sumas de datos de punto flotante (`SINGLEF`)
* **Fundamento Teórico**: Los registros `ST(0)..ST(7)` del coprocesador 80x87 tienen **80 bits** de precisión interna. Cuando sumamos dos `SINGLEF` con `FADD`, en `ST(0)` el número puede superar el máximo de 32 bits (`3.40282347E+38`) sin que el procesador aborte solo.
* **Implementación**: En cada terceto `+` de tipo `SINGLEF`, antes de guardar en `<aux>` (o comparando en la FPU antes de `FSTP <aux>`):
  ```nasm
  FLD <op1>
  FADD <op2>
  FLD ST(0)               ; Duplicamos el resultado en el tope ST(0)
  FABS                    ; Tomamos valor absoluto |resultado| en ST(0)
  FCOMP @max_singlef      ; Comparamos |resultado| con 3.40282347E+38 y desapilamos la copia
  FSTSW AX
  SAHF
  JA _err_overflow_float  ; Si |resultado| > max_singlef, saltar al error (salto sin signo JA!)
  FSTP <aux>              ; Si está en rango, guardamos el resultado en la variable auxiliar
  ```

##### Chequeo `d`: Overflow en productos de enteros (`SHORTINT`)
* **Fundamento Teórico** (`11.1.Assembler_Pentium_Sintaxis.pdf`, pág. 22): La instrucción `IMUL op8` multiplica `AL * op8` con signo, deja el resultado en `AX`, y **pone `CF = OF = 1` si el resultado con signo no cabe en `AL` (`[-128, 127]`)**.
* **Implementación**: En cada terceto `*` de tipo `SHORTINT`:
  ```nasm
  MOV AL, <op1>
  IMUL <op2>
  JO _err_overflow_entero ; Jump if Overflow (OF == 1)
  MOV <aux>, AL
  ```

##### Chequeo `g`: Pérdida de información en la conversión de punto flotante a entero (`tos()`, Tema 34)
* **Fundamento Teórico**: Al convertir un `SINGLEF` (`op1`) a `SHORTINT` (8 bits con signo `[-128, 127]`) mediante `tos(op1)`, se pierde información en **dos situaciones**:
  1. **Fuera de rango de `SHORTINT`**: El flotante es mayor que `127.0` o menor que `-128.0`.
  2. **Tiene parte fraccionaria no nula**: Por ejemplo `tos(10.5)`. Si se convierte a entero da `10` (o `11`), que no es igual a `10.5`.
* **Implementación en 80x87**:
  ```nasm
  ; 1) Chequeo de rango [-128.0, 127.0]
  FLD <op1>
  FCOMP @max_shortint_f
  FSTSW AX
  SAHF
  JA _err_perdida_info      ; op1 > 127.0
  FLD <op1>
  FCOMP @min_shortint_f
  FSTSW AX
  SAHF
  JB _err_perdida_info      ; op1 < -128.0

  ; 2) Chequeo de parte fraccionaria (¿op1 == entero(op1)?)
  FLD <op1>
  FISTP @aux_tos            ; Convierte a entero de 16 bits en @aux_tos
  FILD @aux_tos             ; Vuelve a cargar ese entero como float en ST(0)
  FLD <op1>                 ; Carga el float original en ST(0)
  FCOMPP                    ; Compara ambos y desapila los dos (ST(0) y ST(1))
  FSTSW AX
  SAHF
  JNE _err_perdida_info     ; Si difieren, tenía decimales -> pérdida de información

  ; 3) Guardar resultado válido en <aux> (SHORTINT de 8 bits)
  MOV AX, @aux_tos
  MOV <aux>, AL
  ```

##### Chequeo `i`: Recursión en invocaciones de funciones
* **Fundamento Teórico**: El lenguaje no permite invocaciones recursivas (ni directas `f -> f`, ni mutuas indirectas `f -> g -> f`).
* **Implementación**:
  - Por cada función o método `<ambLimpio>`, declarar en `.data` una bandera de 1 byte inicializada en `0`:
    ```nasm
    @activa_<ambLimpio> DB 0
    ```
  - En el prólogo de `_proc_<ambLimpio>:` (justo al entrar a la función):
    ```nasm
    CMP @activa_<ambLimpio>, 1
    JE _err_recursion
    MOV @activa_<ambLimpio>, 1
    ```
  - En el epílogo `_fin_<ambLimpio>:` (justo antes de `RET`):
    ```nasm
    MOV @activa_<ambLimpio>, 0
    ```
  *(Nota para la defensa: usar una bandera de activación por función detecta **tanto la recursión directa como la recursión mutua indirecta `A -> B -> A`** en tiempo de ejecución, con solo 1 byte por función).*

---

## Parte III: Tabla Resumen de Progresión y Entregables por Hito

| Hito | Qué aprendés de Assembler | Qué implementa el Agente | Prueba de Verificación |
| :--- | :--- | :--- | :--- |
| **Hito 1** | Estructura `.asm`, secciones `.data` y `.code`, directivas `DB/DW/DD/DQ` y cómo invocar `printf`. | `GeneradorAssembler.java` base, volcado de TS a `.data`, traducción de `POUT`, enganche en `gram.y`. | `tests/test_etapa4_hito1.txt` (declaraciones y `POUT` de cadenas, `SHORTINT` y `SINGLEF`). |
| **Hito 2** | Registros `AL/AX/BL`, instrucciones `MOV/ADD/SUB/IMUL/CBW/IDIV`, pila `ST(0)` del 80x87 (`FLD/FADD/FSUB/FMUL/FDIV/FSTP/FILD`) y variables `@aux`. | Registro de `@aux` en TS, aritmética `SHORTINT` y `SINGLEF`, asignaciones `:=`, `tos()` y ajuste de `enComptime` en `gram.y`. | `tests/test_etapa4_hito2.txt` (expresiones mixtas, `COMPTIME`, `= (expr)` y `tos()`). |
| **Hito 3** | Flags de CPU vs Flags del 80x87 (`FSTSW AX` + `SAHF`), saltos con signo (`JLE..JG`) vs sin signo (`JBE..JA`), etiquetas. | Recolección de etiquetas destino (Alternativa 1) y traducción de comparadores, `BF` y `BI` (`IF/ELSE` y `REPEAT UNTIL`). | `tests/test_etapa4_hito3.txt` (`IF`, `IF-ELSE` anidados y ciclos `REPEAT UNTIL`). |
| **Hito 4** | Subrutinas (`CALL`/`RET`), prólogo/epílogo `EBP/ESP`, pasaje estático de parámetros y despacho de atributos posicionales. | Generación de procedimientos por ámbito, `@ret_...`, llamada a métodos de clase y acceso posicional `obj[i]`. | `tests/test_etapa3_integrador.txt` completo generando `.asm` de punta a punta. |
| **Hito 5** | Detección de overflow con `JO` y con `FABS`/`FCOMP`, truncamiento en FPU y banderas de activación. | Chequeos runtime **`c`, `d`, `g`, `i`**, casos de prueba de fallo runtime y bitácora `GUIA_DEFENSA_ETAPA4.md`. | Suite de 4 tests runtime (`c, d, g, i`) + caso integrador final. |
