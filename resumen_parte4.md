
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

La sección ejecutable inicia con `.code` seguido de la etiqueta `start:`. El generador recorre  los tercetos del ámbito `main` y traduce cada operación a instrucciones x86.

### Resolución de Operandos (`resolverOperandoAsm`)
Cada operando de un terceto puede ser de dos tipos:
1. **Identificador o Constante directa:** se traduce a su nombre MASM mediante `obtenerNombreAsm()` (ej: `main@a` -> `_main@a`, `10` -> `_cte_10`).
2. **Referencia a otro terceto (`[i]`):** representa el resultado de una operación previa. Se resuelve al nombre de la variable auxiliar asociada: `@aux<i>`. Tenemos un caso especial que es asignación en expresión, si la referencia `[i]` apunta a un terceto de asignación `:=`, el valor no está en un auxiliar sino en la propia variable destino  (`operando1`) del terceto referenciado. En ese caso se toma directamente dicha variable.

### Traducción de Operaciones Aritméticas y Asignaciones



* **Operaciones `SHORTINT` - 8 bits con signo:**
  Se resuelven usando el registro `AL` (8 bits bajos de `EAX`):
  * **Suma (`+`):** `MOV AL, op1` -> `ADD AL, op2` -> `MOV @aux, AL`
  * **Resta (`-`):** `MOV AL, op1` -> `SUB AL, op2` -> `MOV @aux, AL`
  * **Multiplicación (`*`):** `MOV AL, op1` -> `IMUL op2` -> `MOV @aux, AL` (la parte baja queda en `AL`).
  * **División (`/`):** `MOV AL, op1` -> `CBW` (extiende signo de `AL` a `AX`) -> `IDIV op2` -> `MOV @aux, AL` (el cociente queda en `AL`).
  * **Asignación (`:=`):** `MOV AL, op2` -> `MOV op1, AL`

* **Operaciones `SINGLEF` - 32 bits:**
  Se resuelven en la pila de la FPU (Coprocesador 80x87):
  * Carga: `FLD op1` (apila en `ST(0)`).
  * Operación: `FADD op2`, `FSUB op2`, `FMUL op2` o `FDIV op2`.
  * Guardado: `FSTP @aux` (o `FSTP op1` si es asignación), desapilando de la FPU.

* **Conversión Explícita `tos()`:**
  ```nasm
  FLD op1
  FISTP @aux_tos
  MOV AX, @aux_tos
  MOV @aux, AL
  ```
  1. `FLD op1`: carga el flotante a la FPU.
  2. `FISTP @aux_tos`: convierte el flotante a entero con redondeo y lo guarda en `@aux_tos` (`DW`, 16 bits), desapilando de la FPU.
  3. `MOV AX, @aux_tos` y `MOV @aux, AL`: copia los 8 bits bajos al auxiliar `SHORTINT`.

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

## Sentencias `IF / ELSE` y `REPEAT UNTIL`



Para realizar los saltos en assembler debemos indicar a que etiqueta saltar. El problema es que yo no puedo saber si un terceto de mas adelante quiere saltar a donde estoy ahora. Entonces la solucion que planteamos llega despues de emitir la seccion **.data**. Lo que hacemos es recorrer una primera vez toda la lista de tercetos y guardamos en HashSet< Integer > los indices de los tercetos a los que se quiere saltar. Entonces en la segunda pasada si etiquetas.contains(i), anteponemos Label_main_< i >.

### Comparaciones 
Cuando se procesa un terceto de comparación (`>`, `<`, `>=`, `<=`, `==`, `!=`):

* **Para enteros `SHORTINT`** se carga el primer operando en `AL` y se compara con `CMP AL, op2`:
  ```nasm
  MOV AL, _main@a
  CMP AL, _main@b
  ```
  `CMP` resta `AL - op2` y actualiza los flags del CPU (`ZF`, `SF`, `OF`). 
* **Para flotantes`SINGLEF`** el procesador no compara flotantes en los registros generales. Cargamos en la pila de la FPU y usamos `FCOMP`:
  ```nasm
  FLD _main@x
  FCOMP _main@y
  FSTSW AX
  SAHF
  ```
  * `FCOMP`: compara `ST(0)` con `op2` y **desapila** `ST(0)`, dejando la pila limpia.
  * `FSTSW AX`: guarda la palabra de estado de la FPU en `AX`.
  * `SAHF`: transfiere el byte `AH` a los flags del CPU (el bit `C0` de la FPU va a parar al *Carry Flag* `CF` y `C3` al *Zero Flag* `ZF`).

### Bifurcación por Falso (`BF`) 
El terceto `BF` salta cuando la condición previa **NO** se cumple:
* **Para `SHORTINT` :**
  * `>` $\to$ complementario $\le$ $\to$ `JLE Label_main_<dest>`
  * `>=` $\to$ complementario $<$ $\to$ `JL Label_main_<dest>`
  * `<` $\to$ complementario $\ge$ $\to$ `JGE Label_main_<dest>`
  * `<=` $\to$ complementario $>$ $\to$ `JG Label_main_<dest>`
  * `==` $\to$ complementario $\ne$ $\to$ `JNE Label_main_<dest>`
  * `!=` $\to$ complementario $=$ $\to$ `JE Label_main_<dest>`
* **Para `SINGLEF` :**
  Debido a cómo mapea la FPU sus Condition Codes a `EFLAGS` (`CF` y `ZF`), los saltos deben ser **sin signo** (*Above / Below*):
  * `>` $\to$ complementario $\le$ $\to$ `JBE Label_main_<dest>`
  * `>=` $\to$ complementario $<$ $\to$ `JB Label_main_<dest>`
  * `<` $\to$ complementario $\ge$ $\to$ `JAE Label_main_<dest>`
  * `<=` $\to$ complementario $>$ $\to$ `JA Label_main_<dest>`
  * `==` $\to$ complementario $\ne$ $\to$ `JNE Label_main_<dest>`
  * `!=` $\to$ complementario $=$ $\to$ `JE Label_main_<dest>`

### Salto Incondicional `BI`
Se traduce directamente a un salto incondicional de x86:
```nasm
JMP Label_main_<dest>
```
Se utiliza para saltar al final del bloque `IF-ELSE` desde el término de la rama `THEN`.

### Sentencia `REPEAT UNTIL` 
 `REPEAT UNTIL` se implementa como el cuerpo de sentencias seguido de la condición y un terceto `( BF , [cond] , [inicio] )`. Con la implementacion de los saltos **BF** alcanza y  no hay que hacer nada adicional

---

## Funciones, `AUTO FUNCTION`, Métodos, Objetos y Acceso Posicional

### Reserva en `.data` para Funciones y Objetos
Para permitir funciones y objetos lo primero que tenemos que hacer es reservar en **.data** las variables necesarias:
* **Variables de retorno (`funcion` y `metodo`):** cuando recorremos la Tabla de Símbolos y encontramos una entrada con uso `funcion` o `metodo`, declaramos una variable global para guardar su retorno: `_ret_<ambito> DB ?` si devuelve `SHORTINT` o `_ret_<ambito> DD ?` si devuelve `SINGLEF`. Para `AUTO FUNCTION` tambien funciona porque en la Etapa 3 ya le inferimos y asignamos su tipo de retorno en la tabla.

* **Atributos propios de cada objeto (`objeto`):** una clase puede tener atributos propios y atributos heredados con `EXTENDS`. Para saber cuáles son todos los atributos en orden posicional (`0, 1, 2...`), usamos `obtenerAtributosTotalesClase()`, que junta recursivamente los atributos de las clases padres y los de la clase hija. Entonces, por cada objeto declarado (ej. `c_hija obj`), reservamos en `.data` una variable para cada uno de sus atributos (ej. `_main_obj_atr_a DB ?`, `_main_obj_atr_b DD ?`).

* **Variables `@aux` de los `CALL`:** en el pre-registro también incluimos los tercetos `CALL`, para que cada llamada tenga su propia variable `@aux<i>` donde dejar el valor devuelto.

### Definición de Subrutinas en `.code`
En la sección **.code**, antes de la etiqueta `start:`, recorremos todos los ámbitos de `tercetosPorAmbito` que no sean `main` y emitimos cada subrutina llamando al mismo método **generarCodigoAmbito()**:
```nasm
_proc_<ambitoLimpio>:
    PUSH EBP
    MOV EBP, ESP
    ... (código de los tercetos del ámbito) ...
_fin_<ambitoLimpio>:
    MOV ESP, EBP
    POP EBP
    RET
```

**¿Cómo sabe el procesador a dónde volver cuando termina la función?**
  Cuando se ejecuta `CALL _proc_...`, el procesador automáticamente apila en el tope de la pila(`ESP`) la **dirección de retorno**. que es la instrucción que le sigue a `CALL`, y salta a la subrutina. Al entrar, `PUSH EBP` y `MOV EBP, ESP` resguardando esa dirección de retorno por si adentro de la función se invoca a otra subrutina que use la pila. Al llegar a `_fin_<ambitoLimpio>:`, `MOV ESP, EBP` y `POP EBP` restauran la pila a como estaba al entrar, dejando otra vez en el tope la dirección de retorno. Finalmente, la instrucción `RET` desapila esa dirección y la carga en el puntero de instrucciones (`EIP`), volviendo exactamente a la línea siguiente del `CALL`.

### Traducción del Terceto `RET`
Cuando dentro de una función o método encontramos el terceto `( RET , op1 , - )` movemos el valor de `op1` a la variable de retorno de esa subrutina (`_ret_<ambitoLimpio>`), usando `MOV AL` si es `SHORTINT` o `FLD` / `FSTP` si es `SINGLEF`. Despues hacemos `JMP _fin_<ambitoLimpio>` para saltar al epílogo y ejecutar el `RET`.

```nasm
MOV AL, @aux_main_duplicar_0
MOV _ret_main_duplicar, AL
JMP _fin_main_duplicar
```

### Traducción del Terceto `CALL`
Antes de llegar al terceto `CALL`, en la Etapa 3 ya se emitieron los tercetos `:=` que copian cada argumento real en la variable del parámetro formal (ej. `_main_duplicar_num := _main_a`). Cuando aparece el terceto `( CALL , claveSub , claveObj )`, tenemos dos casos:

#### Llamada a `FUNCTION` o `AUTO FUNCTION`
Se distingue porque el segundo operando del terceto es `"-"`. Lo único que hacemos es:
1. Emitir `CALL _proc_<subLimpio>`.
2. Cuando vuelve, copiar el resultado desde `_ret_<subLimpio>` a la variable auxiliar `@aux<i>` asociada al terceto `CALL`:
```nasm
CALL _proc_main_duplicar
MOV AL, _ret_main_duplicar
MOV @aux18, AL
```

#### Llamada a `obj.metodo()`
Este caso es un poco mas complejo. La subrutina del metodo de la clase se genero con las variables de **clase**. Pero en realidad cuando ejecutemos el metodo tenemos que modificar los valores de las **instancias**. Entonces la solucion es guardar el nombre del objeto (`main@obj`) en el segundo operando del terceto `CALL`: `( CALL , main@c_base_a@sumar_base , main@obj )`. Así al traducir el `CALL` hacemos tres pasos:
  1. **Antes del `CALL`:** copiamos todos los atributos del objeto (`_main_obj_<atr>`) hacia las variables de atributo de la clase (`_main_<clase>_<atr>`).
  2. **Llamada:** ejecutamos `CALL _proc_main_c_base_a_sumar_base` que trabaja con las variables de clase.
  3. **Después del `CALL`:** copiamos de vuelta las variables de la clase hacia las variables del objeto `_main_obj_<atr>` y guardamos `_ret_...` en el `@aux<i>` del terceto.

### Acceso Posicional a Atributos (`obj[i]`)
Para resolver el acceso posicional del Tema 29 (`obj[0$s] := 15$s;` o `obj[a] := 25$s;`), buscamos la lista ordenada de atributos de la clase del objeto con `obtenerAtributosTotalesClase()` y separamos en dos casos:

* **Caso 1: Índice constante (`obj[0$s]`):**
  Como conocemos el número de posición en tiempo de compilación, buscamos directamente qué atributo está en ese índice `k` dentro de la lista de la clase (por ejemplo, el índice `0` es `atr_a`) y lo reemplazamos directamente por la variable de ese atributo del objeto (`_main_obj_atr_a`).
  ```nasm
  MOV AL, _cte_15
  MOV _main_obj_atr_a, AL
  ```

* **Caso 2: Índice con variable (`obj[a]`):**
  Como el valor de la variable `a` recién se conoce en tiempo de ejecución, generamos comparaciones en Assembler (`CMP AL, k`) para cada posición `k` compatible de la clase. Si `a` vale `k`, asigna en ese atributo; si no, salta con `JNE` al siguiente:
  ```nasm
  MOV AL, _main_a
  CMP AL, 0
  JNE Label_skip_pos_main_3_0
  MOV AL, _cte_25
  MOV _main_obj_atr_a, AL
  Label_skip_pos_main_3_0:
  MOV AL, _main_a
  CMP AL, 2
  JNE Label_skip_pos_main_3_2
  MOV AL, _cte_25
  MOV _main_obj_atr_propio, AL
  Label_skip_pos_main_3_2:
  ```

---

## Controles en Tiempo de Ejecución (Grupo 20: c, d, g, i)

Por consigna, al **Grupo 20** le tocaron los 4 chequeos en tiempo de ejecución **`c`, `d`, `g` e `i`**. Cuando ocurre cualquiera de estos errores, el programa salta a una rutina al final de `.code`, imprime el mensaje correspondiente con `printf` y corta la ejecución con `invoke ExitProcess, 1`.

Para poder hacer las comparaciones declaramos en `.data` las constantes límite (`@max_singlef DD 3.40282347E+38`, `@min_shortint_f DD -128.0`, `@max_shortint_f DD 127.0`), los 4 mensajes de error (`_msg_err_...`) y una bandera `@activa_<ambito> DB 0` por cada función o método.

### Chequeo `c`: Overflow en sumas de punto flotante (`SINGLEF`)
Los registros `ST(0)..ST(7)` de la FPU trabajan internamente con 80 bits, así que una suma puede pasarse del límite de 32 bits (`3.40282347E+38`) antes de bajar a memoria. Después de `FADD op2`:
1. Duplicamos el tope (`FLD ST(0)`) y le aplicamos valor absoluto (`FABS`) para cubrir también sumas negativas muy grandes.
2. Comparamos `|resultado|` contra `@max_singlef` con `FCOMP @max_singlef` (desapila la copia), pasamos los flags con `FSTSW AX` + `SAHF` y si es mayor saltamos con `JA _err_overflow_float`.
3. Si está en rango, recién ahí guardamos el valor en `@aux` con `FSTP`.

```nasm
FLD _main_f1
FADD _main_f2
FLD ST(0)
FABS
FCOMP @max_singlef
FSTSW AX
SAHF
JA _err_overflow_float
FSTP @aux9
```

### Chequeo `d`: Overflow en productos de enteros (`SHORTINT`)
La instrucción `IMUL op2` de 8 bits deja el producto con signo en `AX` (16 bits), pero nuestro tipo `SHORTINT` entra solo en `AL` (`[-128, 127]`). Cuando el resultado no entra en 1 byte con signo, el procesador activa automáticamente el **Overflow Flag (`OF = 1`)**. Entonces alcanza con agregar `JO _err_overflow_entero` justo después de `IMUL`:

```nasm
MOV AL, _main_a
IMUL _main_b
JO _err_overflow_entero
MOV @aux3, AL
```

### Chequeo `g`: Pérdida de información en conversión `tos()`
Al pasar un `SINGLEF` a `SHORTINT` con `tos(op1)` se pierde información en dos casos: si se sale del rango `[-128, 127]` o si tiene parte decimal distinta de cero (ej. `10.5`).
1. **Control de rango:** cargamos `op1` y comparamos con `FCOMP @max_shortint_f` (`JA _err_perdida_info`) y con `FCOMP @min_shortint_f` (`JB _err_perdida_info`).
2. **Control de decimales:** convertimos a entero de 16 bits en `@aux_tos` con `FISTP @aux_tos`, lo volvemos a subir como flotante con `FILD @aux_tos`, apilamos el `op1` original con `FLD op1` y los comparamos desapilando ambos con `FCOMPP`. Si son distintos (`JNE _err_perdida_info`), tenía decimales y hubo pérdida de información.
3. Si pasó los dos controles, movemos `AX` desde `@aux_tos` y guardamos `AL` en `@aux`.

### Chequeo `i`: Recursión en invocaciones de funciones
El lenguaje prohíbe tanto la recursión directa (`f -> f`) como la mutua (`f -> g -> f`). Para detectarlo en runtime sin tocar la pila de llamadas:
* En `.data` declaramos `@activa_<subrutina> DB 0` para cada función o método.
* Al entrar a `_proc_<subrutina>:`, comparamos si ya vale `1` (`CMP @activa_..., 1` + `JE _err_recursion`); si estaba en `0`, la ponemos en `1`.
* Al salir por `_fin_<subrutina>:`, justo antes de restaurar `EBP` y hacer `RET`, la volvemos a poner en `0` (`MOV @activa_..., 0`).

---

## Cierre y Guardado del Archivo

Al terminar el recorrido de los tercetos de `main`:
1. Cerramos el programa normal con `invoke ExitProcess, 0`, dejamos las 4 etiquetas de error (`_err_overflow_float`, `_err_overflow_entero`, `_err_perdida_info`, `_err_recursion`) que imprimen el mensaje y terminan con `invoke ExitProcess, 1`, y cerramos el archivo con `end start`.
2. Invocamos `GeneradorAssembler.guardarArchivo(ruta, asm)`, que toma el nombre del archivo fuente (ej. `test.txt`), le cambia la extensión a `.asm` (`test.asm`) y lo graba en disco en UTF-8.
