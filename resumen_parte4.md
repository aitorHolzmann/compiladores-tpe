
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


## 4. Sección `.code`: Traducción de Tercetos y la Sentencia `POUT`

En el Hito 1, nos concentramos en traducir la sentencia `POUT` dentro del ámbito raíz (`main`). Recorremos los tercetos de `main` obtenidos con `GeneradorTerceto.obtenerTercetosPorAmbito()`.

Cuando encontramos un terceto `( POUT , op1 , - )`, consultamos el tipo de `op1` y generamos las instrucciones correspondientes:

### Caso A: Impresión de Cadenas (`CADENA`)
```nasm
invoke printf, cfm$("%s\n"), OFFSET _str_0
```
* **`OFFSET`:** es obligatorio porque `printf` espera el puntero (la dirección en memoria de la cadena). Sin `OFFSET`, intentaría pasar el texto desreferenciado como un número y el programa colapsaría (*crash*).
* **`cfm$()`:** macro de MASM32 que crea la cadena de formato terminada en `0` en tiempo de ensamblado.

### Caso B: Impresión de Enteros Cortos (`SHORTINT` - 8 bits con signo)
```nasm
MOV AL, _cte_neg_5
MOVSX AX, AL
MOV @aux_print_short, AX
invoke printf, cfm$("%hd\n"), @aux_print_short
```
* **El problema:** `printf` no tiene un modificador estándar de 8 bits en este runtime; usa `%hd` esperando un entero de 16 bits (`WORD`).
* **La solución:** cargamos el byte en `AL` y usamos **`MOVSX AX, AL`** (*Move with Sign-Extension*). Esta instrucción copia `AL` a `AX` y rellena la parte alta `AH` replicando el bit de signo. Si `AL` vale `-5`, `AX` conserva exactamente el valor `-5` en 16 bits. (Si usáramos `MOVZX` o no limpiáramos `AH`, rellenaría con ceros o basura convirtiendo un negativo en un positivo enorme).

### Caso C: Impresión de Flotantes (`SINGLEF` - 32 bits)
```nasm
FLD _cte_3_14
FSTP @aux_print_float
invoke printf, cfm$("%Lf\n"), @aux_print_float
```
* **El problema:** por estándar de C (`VARARG`), toda función con argumentos variables exige que los flotantes de 32 bits se promuevan a `double` de 64 bits. En MASM32, la cátedra utiliza el formato `%Lf` que lee una variable de 64 bits (`DQ`).
* **La solución (Coprocesador 80x87):**
  1. `FLD _cte_3_14`: carga el float de 32 bits al tope de la pila de la FPU (`ST(0)`). Internamente, la FPU lo expande gratis a 80 bits sin perder precisión.
  2. `FSTP @aux_print_float`: descarga el valor en la variable de 64 bits `@aux_print_float` (`DQ`) convirtiéndolo automáticamente por hardware a formato IEEE 754 de doble precisión, y desapila (*pop*) para dejar la FPU limpia.
  3. `invoke printf`: lee `@aux_print_float` e imprime el flotante en consola.

---

## 5. Cierre y Guardado del Archivo

Al terminar el recorrido de los tercetos:
1. Cerramos la sección ejecutable con:
   ```nasm
   invoke ExitProcess, 0
   end start
   ```
2. Invocamos `GeneradorAssembler.guardarArchivo(ruta, asm)`, que toma el nombre del archivo fuente (por ejemplo `test_etapa4_hito1.txt`), le cambia la extensión a `.asm` (`test_etapa4_hito1.asm`) y lo graba en disco con codificación UTF-8.

## Inicio

Para esta última entrega tenemos que generar código de máquina a partir del código intermedio (Tercetos). En nuestro caso elegimos como salida el lenguaje ensamblador del procesador Intel Pentium x86 de 32 bits, utilizando la herramienta **MASM32**.

Todo el código generado se guarda en un archivo `.asm` que se divide en distintas secciones:
* **Encabezado e Includes:** directivas de arquitectura (`.386`, `.model flat, stdcall`), librerías (`masm32rt.inc`, `kernel32`, `masm32`) y prototipos externos (`printf PROTO C :VARARG`).
* **Sección `.data`:** donde se reservan e inicializan los datos estáticos del programa (variables, constantes literales y variables auxiliares del compilador).
* **Sección `.code`:** contiene las instrucciones ejecutables, marcando el inicio con la etiqueta `start:` y el fin del proceso con `invoke ExitProcess, 0` seguido de `end start`.

##  .data

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


## Sentencia POUT
//Se ataco solo lo del pout. Hay que entender mejor como funciona la impresion.

En el Hito 1, nos concentramos en traducir la sentencia `POUT` dentro del ámbito raíz (`main`). Recorremos los tercetos de `main` obtenidos con `GeneradorTerceto.obtenerTercetosPorAmbito()`.

Cuando encontramos un terceto `( POUT , op1 , - )`, consultamos el tipo de `op1` y generamos las instrucciones correspondientes:

### Impresión de`CADENA`
```nasm
invoke printf, cfm$("%s\n"), OFFSET _str_0
```
`OFFSET` es obligatorio porque `printf` espera el puntero (la dirección en memoria de la cadena). Sin `OFFSET`, intentaría pasar el texto desreferenciado como un número y el programa colapsaría (*crash*).`cfm$()` es un macro de MASM32 que crea la cadena de formato terminada en `0` en tiempo de ensamblado.
### Impresión de `SHORTINT`
```nasm
MOV AL, _cte_neg_5
MOVSX AX, AL
MOV @aux_print_short, AX
invoke printf, cfm$("%hd\n"), @aux_print_short
```
`printf` espera un entero de 16 bits (`WORD`). Entonces cargamos el byte en `AL` y usamos **`MOVSX AX, AL`** (*Move with Sign-Extension*). Esta instrucción copia `AL` a `AX` y rellena la parte alta `AH` replicando el bit de signo. (Si usáramos `MOVZX` o no limpiáramos `AH`, rellenaría con ceros o basura convirtiendo un negativo en un positivo enorme).

### Impresión de`SINGLEF`
```nasm
FLD _cte_3_14
FSTP @aux_print_float
invoke printf, cfm$("%Lf\n"), @aux_print_float
```
* **El problema:** por estándar de C (`VARARG`), toda función con argumentos variables exige que los flotantes de 32 bits se promuevan a `double` de 64 bits. En MASM32, la cátedra utiliza el formato `%Lf` que lee una variable de 64 bits (`DQ`).
* **La solución (Coprocesador 80x87):**
  1. `FLD _cte_3_14`: carga el float de 32 bits al tope de la pila de la FPU (`ST(0)`). Internamente, la FPU lo expande gratis a 80 bits sin perder precisión.
  2. `FSTP @aux_print_float`: descarga el valor en la variable de 64 bits `@aux_print_float` (`DQ`) convirtiéndolo automáticamente por hardware a formato IEEE 754 de doble precisión, y desapila (*pop*) para dejar la FPU limpia.
  3. `invoke printf`: lee `@aux_print_float` e imprime el flotante en consola.

---

## 5. Cierre y Guardado del Archivo

Al terminar el recorrido de los tercetos:
1. Cerramos la sección ejecutable con:
   ```nasm
   invoke ExitProcess, 0
   end start
   ```
2. Invocamos `GeneradorAssembler.guardarArchivo(ruta, asm)`, que toma el nombre del archivo fuente (por ejemplo `test_etapa4_hito1.txt`), le cambia la extensión a `.asm` (`test_etapa4_hito1.asm`) y lo graba en disco con codificación UTF-8.
