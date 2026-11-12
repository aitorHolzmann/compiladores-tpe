
## Inicio

Para esta última entrega tenemos que generar código de máquina a partir del código intermedio (Tercetos). En nuestro caso elegimos como salida el lenguaje ensamblador del procesador Intel Pentium x86 de 32 bits, utilizando la herramienta **MASM32**.

Todo el código generado se guarda en un archivo `.asm` que se divide en distintas secciones:
* **Encabezado e Includes:** directivas de arquitectura (`.386`, `.model flat, stdcall`), librerías (`masm32rt.inc`, `kernel32`, `masm32`) y prototipos externos (`printf PROTO C :VARARG`).
* **Sección `.data`:** donde se reservan e inicializan los datos estáticos del programa (variables, constantes literales y variables auxiliares del compilador).
* **Sección `.code`:** contiene las instrucciones ejecutables, marcando el inicio con la etiqueta `start:` y el fin del proceso con `invoke ExitProcess, 0` seguido de `end start`.

##  Condición Previa: Control de Errores

Antes de generar el Assembler, en el **main** de **gram.y** debemos validar que las etapas anteriores hayan finalizado sin errores. Si se cumple entonces podemos empezar a construir el **assembler** llamando a **GeneradorAssembler.generarAssembler()**. 

El metodo se encarga de contruir el archivo **.asm**. Primero agrega todos los includes, y despues empieza a armar **.data**. Este paso consiste registrar las variables para las que queremos reservar espacio, usando un nombre y la cantidad de bytes a reservar. La idea es usar nombres estandarizados en funcion de los distintos simbolos:

* **Constantes numéricas (`SHORTINT` y `SINGLEF`):** como no tienen nombre en la tabla ya que su clave es el valor literal, se les antepone el prefijo `_cte_` y se reemplazan caracteres que dan problemas con MASM:
  * El signo `-` se reemplaza por `neg_`.
  * El punto decimal `.` se reemplaza por `_`.
  * El signo `+` se reemplaza por `pos_`.
  * El exponente s se reemplaza por E
  * *Ejemplos:* `10` -> `_cte_10`, `-5` -> `_cte_neg_5`, `3.14` -> `_cte_3_14`.
  
* **Cadenas de texto:** se identifican con un contador: `_str_0`, `_str_1`, etc.

* **Variables, parámetros y atributos:** se les antepone un guión bajo `_` para evitar colisiones con palabras reservadas de ensamblador. Por ejemplo `main@a` -> `_main@a`
* **Variables auxiliares del sistema (`@`):** las variables que ya comienzan con `@` (como `@aux_print_short`) se dejan tal cual porque MASM32 admite `@` al inicio.


Para reservar el espacio tuvimos en cuenta los rangos de los tipos de datos:
* **Variables SHORTINT:** se emite `DB ?` ( 1 byte, El interrogante indica que no tenemos un valor para inicializar).
* **Variables SINGLEF:** se emite `DD ?` (4 bytes).
* **Constantes SHORTINT:** se emite `DB <valor>` (ej. `_cte_10 DB 10`).
* **Constantes SINGLEF:** se pasa por `formatearLiteralSinglef` para asegurar que tenga dígitos antes y después del punto y se emite `DD <literal>`. //Revisar
* **Cadenas CADENA:** se limpian las comillas y se emiten terminadas en byte nulo (`0`), tal como exige C: `_str_0 DB "Mensaje", 0`. //Revisar
* **Variables `COMPTIME`:** no se debe reservar espacio ya que se resuelven en compilacion.
* **Variables fijas de soporte para `printf`:** //Revisar
  ```nasm
  @aux_print_short DW ?   ; 16 bits para promocion de SHORTINT
  @aux_print_float DQ ?   ; 64 bits para promocion de SINGLEF
  ```

## Pre-registro de Auxiliares y Constantes

El resultado de las operaciones intermedias se debe guardar en variables auxiliares, las cuales deben tener tambien su propio espacio reservado en **.data**. Entonces debemos recorrer todos los tercetos mediante `registrarAuxiliaresYConstantes()` y agregar las variables auxiliares a la tabla de simbolos.
## Variables `@aux`
Para cada terceto de operación (`+`, `-`, `*`, `/`, `tos`) se genera un nombre único según el ámbito:
  * En `main`: `@aux0`, `@aux1`, etc.
  * En funciones/métodos: `@aux_<ambito>_<indice>` (ej. `@aux_main_f_3`).
  Y se registra en la `TablaSimbolos` con `USO = "auxiliar"` y el tipo de dato del terceto (`SHORTINT` o `SINGLEF`). De esta forma, cuando el generador recorre la tabla para emitir `.data`, ya encuentra estas variables y reserva `DB ?` (1 byte) o `DD ?` (4 bytes).

Ejemplo:
* Terceto: `3 ( + , _main@a , _main@b )` de tipo `SHORTINT`.
* Se registra `@aux3` con tipo `SHORTINT`.
* En `.data` se emite: `@aux3 DB ?`.

### COMPTIME
Para la etapa 3 implementamos plegado de constantes para variables `COMPTIME`. Esto significa que muchas operaciones se resolvieron en compilación generando valores nuevos que no estaban en la Tabla de Símbolos.

El método `registrarConstanteSiFalta()` revisa los operandos de cada terceto si detecta un literal que no está en la tabla lo da de alta automáticamente con su tipo (`SHORTINT`, `SINGLEF` o `CADENA`).  Entonces en `.data` se genera su etiqueta correspondiente (ej. `_cte_25 DB 25`).

## Sección `.code`

La sección ejecutable inicia con la directiva `.code` seguida de la etiqueta `start:`. El generador recorre  los tercetos del ámbito `main` y traduce cada operación a instrucciones x86.

### Resolución de Operandos (`resolverOperandoAsm`)
Cada operando de un terceto puede ser de dos tipos:
1. **Identificador o Constante directa:** se traduce a su nombre MASM mediante `obtenerNombreAsm()` (ej: `main@a` -> `_main@a`, `10` -> `_cte_10`).
2. **Referencia a otro terceto (`[i]`):** representa el resultado de una operación previa. Se resuelve al nombre de la variable auxiliar asociada: `@aux<i>`. Tenemos un caso especial que es asignación en expresión, si la referencia `[i]` apunta a un terceto de asignación `:=`, el valor no está en un auxiliar sino en la propia variable destino  (`operando1`) del terceto referenciado. En ese caso se toma directamente dicha variable.

### Traducción de Operaciones Aritméticas y Asignaciones



* **Operaciones Enteras (`SHORTINT` - 8 bits con signo):**
  Se resuelven usando el registro `AL` (8 bits bajos de `EAX`):
  * **Suma (`+`):** `MOV AL, op1` -> `ADD AL, op2` -> `MOV @aux, AL`
  * **Resta (`-`):** `MOV AL, op1` -> `SUB AL, op2` -> `MOV @aux, AL`
  * **Multiplicación (`*`):** `MOV AL, op1` -> `IMUL op2` -> `MOV @aux, AL` (la parte baja queda en `AL`).
  * **División (`/`):** `MOV AL, op1` -> `CBW` (extiende signo de `AL` a `AX`) -> `IDIV op2` -> `MOV @aux, AL` (el cociente queda en `AL`).
  * **Asignación (`:=`):** `MOV AL, op2` -> `MOV op1, AL`

* **Operaciones Flotantes (`SINGLEF` - 32 bits IEEE 754):**
  Se resuelven en la pila de la FPU (Coprocesador 80x87):
  * Carga: `FLD op1` (apila en `ST(0)`).
  * Operación: `FADD op2`, `FSUB op2`, `FMUL op2` o `FDIV op2`.
  * Guardado: `FSTP @aux` (o `FSTP op1` si es asignación), desapilando de la FPU.

* **Conversión Explícita `tos()` (Tema 34 - Float a Shortint):**
  ```nasm
  FLD op1
  FISTP @aux_tos
  MOV AX, @aux_tos
  MOV @aux, AL
  ```
  1. `FLD op1`: carga el flotante a la FPU.
  2. `FISTP @aux_tos`: convierte el flotante a entero con redondeo y lo guarda en `@aux_tos` (`DW`, 16 bits), desapilando de la FPU.
  3. `MOV AX, @aux_tos` y `MOV @aux, AL`: copia los 8 bits bajos al auxiliar `SHORTINT`.

---

## Sentencia `POUT`

Para traducir `( POUT , op1 , - )` en `main`, consultamos el tipo de `op1` y generamos las instrucciones según corresponda:

### Caso A: Impresión de Cadenas (`CADENA`)
```nasm
invoke printf, cfm$("%s\n"), OFFSET _str_0
```
* **`OFFSET`:** obligatorio porque `printf` espera el puntero (la dirección en memoria de la cadena). Sin `OFFSET`, intentaría pasar el texto desreferenciado como un número y el programa colapsaría (*crash*).
* **`cfm$()`:** macro de MASM32 que crea la cadena de formato terminada en `0` en tiempo de ensamblado.

### Caso B: Impresión de Enteros Cortos (`SHORTINT` - 8 bits con signo)
```nasm
MOV AL, _cte_neg_5
MOVSX AX, AL
MOV @aux_print_short, AX
invoke printf, cfm$("%hd\n"), @aux_print_short
```
* **El problema:** `printf` usa `%hd` esperando un entero de 16 bits (`WORD`), no de 8 bits.
* **La solución:** cargamos el byte en `AL` y usamos **`MOVSX AX, AL`** (*Move with Sign-Extension*). Copia `AL` a `AX` y rellena `AH` replicando el bit de signo. Así `-5` conserva su valor en 16 bits sin corromperse.

### Caso C: Impresión de Flotantes (`SINGLEF` - 32 bits)
```nasm
FLD _cte_3_14
FSTP @aux_print_float
invoke printf, cfm$("%Lf\n"), @aux_print_float
```
* **El problema:** por estándar de C (`VARARG`), toda función con argumentos variables exige que los flotantes de 32 bits se promuevan a `double` de 64 bits (`DQ`). En MASM32, la cátedra usa el formato `%Lf`.
* **La solución (Coprocesador 80x87):**
  1. `FLD _cte_3_14`: carga el float de 32 bits a la FPU (`ST(0)`).
  2. `FSTP @aux_print_float`: descarga en la variable de 64 bits `@aux_print_float` (`DQ`) convirtiéndolo automáticamente por hardware a IEEE 754 de doble precisión, y desapila.
  3. `invoke printf`: lee `@aux_print_float` e imprime el flotante en consola.

---

## Sentencias de Control (`IF / ELSE` y `REPEAT UNTIL` - Tema 12)

En el Hito 3 implementamos la traducción de comparaciones y bifurcaciones condicionales (`BF`) e incondicionales (`BI`), abarcando tanto `IF / ELSE` como la sentencia iterativa `REPEAT UNTIL`.

### 1. Recolección de Etiquetas Destino (Alternativa 1 de la Cátedra)
Para no emitir etiquetas en cada instrucción, aplicamos la **Alternativa 1**:
1. Antes de emitir el código de `main`, recorremos sus tercetos y guardamos en un `HashSet<Integer> etiquetas` los índices destino:
   * Si es `BF`: el destino está en el segundo operando (`"[dest]"`).
   * Si es `BI`: el destino está en el primer operando (`"[dest]"`).
2. Durante la emisión de instrucciones, si `etiquetas.contains(i)`, anteponemos `Label_main_<i>:`.
3. Si un salto apunta a `lista.size()`, emitimos `Label_main_<size>:` justo antes de `invoke ExitProcess, 0`.

### 2. Comparaciones en CPU vs. Coprocesador FPU 80x87
Cuando se procesa un terceto de comparación (`>`, `<`, `>=`, `<=`, `==`, `!=`):
* **Para enteros (`SHORTINT` - 8 bits con signo):**
  Se carga el primer operando en `AL` y se compara con `CMP AL, op2`:
  ```nasm
  MOV AL, _main@a
  CMP AL, _main@b
  ```
  `CMP` resta `AL - op2` y actualiza los flags del CPU (`ZF`, `SF`, `OF`).
* **Para flotantes (`SINGLEF` - 32 bits IEEE 754):**
  El procesador no compara flotantes en los registros generales. Cargamos en la pila de la FPU y usamos `FCOMP`:
  ```nasm
  FLD _main@x
  FCOMP _main@y
  FSTSW AX
  SAHF
  ```
  * `FCOMP`: compara `ST(0)` con `op2` y **desapila** `ST(0)`, dejando la pila limpia.
  * `FSTSW AX`: guarda la palabra de estado de la FPU en `AX`.
  * `SAHF`: transfiere el byte `AH` a los flags del CPU (el bit `C0` de la FPU va a parar al *Carry Flag* `CF` y `C3` al *Zero Flag* `ZF`).

### 3. Bifurcación por Falso (`BF`) y Lógica Invertida
El terceto `BF` salta cuando la condición previa **NO** se cumple:
* **Para `SHORTINT` (saltos con signo tras `CMP`):**
  * `>` $\to$ complementario $\le$ $\to$ `JLE Label_main_<dest>`
  * `>=` $\to$ complementario $<$ $\to$ `JL Label_main_<dest>`
  * `<` $\to$ complementario $\ge$ $\to$ `JGE Label_main_<dest>`
  * `<=` $\to$ complementario $>$ $\to$ `JG Label_main_<dest>`
  * `==` $\to$ complementario $\ne$ $\to$ `JNE Label_main_<dest>`
  * `!=` $\to$ complementario $=$ $\to$ `JE Label_main_<dest>`
* **Para `SINGLEF` (saltos sin signo tras `SAHF`):**
  Debido a cómo mapea la FPU sus Condition Codes a `EFLAGS` (`CF` y `ZF`), los saltos deben ser **sin signo** (*Above / Below*):
  * `>` $\to$ complementario $\le$ $\to$ `JBE Label_main_<dest>`
  * `>=` $\to$ complementario $<$ $\to$ `JB Label_main_<dest>`
  * `<` $\to$ complementario $\ge$ $\to$ `JAE Label_main_<dest>`
  * `<=` $\to$ complementario $>$ $\to$ `JA Label_main_<dest>`
  * `==` $\to$ complementario $\ne$ $\to$ `JNE Label_main_<dest>`
  * `!=` $\to$ complementario $=$ $\to$ `JE Label_main_<dest>`

### 4. Salto Incondicional (`BI`)
Se traduce directamente a un salto incondicional de x86:
```nasm
JMP Label_main_<dest>
```
Se utiliza para saltar al final del bloque `IF-ELSE` desde el término de la rama `THEN`.

### 5. Sentencia `REPEAT UNTIL` (Tema 12)
No requiere ninguna lógica adicional en el generador de Assembler. En la Etapa 3, `REPEAT UNTIL` se implementa como el cuerpo de sentencias seguido de la condición y un terceto `( BF , [cond] , [inicio] )`. Por ende, el mecanismo de `BF` salta automáticamente hacia atrás a la etiqueta de inicio (`Label_main_<inicio>`) mientras la condición de salida sea falsa.

---

## Cierre y Guardado del Archivo

Al terminar el recorrido de los tercetos:
1. Cerramos la sección ejecutable con:
   ```nasm
   invoke ExitProcess, 0
   end start
   ```
2. Invocamos `GeneradorAssembler.guardarArchivo(ruta, asm)`, que toma el nombre del archivo fuente (ej. `test.txt`), le cambia la extensión a `.asm` (`test.asm`) y lo graba en disco en UTF-8.
