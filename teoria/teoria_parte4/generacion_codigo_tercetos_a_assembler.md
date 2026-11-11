# Generación de Código Assembler desde Tercetos (x86 / Pentium)

> **Documento de Cátedra — Resumen Especializado para Grupo 20**  
> *Materia:* Diseño de Compiladores I / Compiladores e Intérpretes  
> *Base teórica:* Extracto exclusivo de la sección Tercetos del documento `UNC.10.1.Generación de Código AS-Ass PI-Ass T-Ass.pdf` (eliminando Árbol Sintáctico y Polaca Inversa).

---

## 1. Introducción y Contexto

En el pipeline del compilador, la **Generación de Código Objeto (Assembler)** toma como entrada la representación intermedia generada en la Etapa 3 y la traduce a instrucciones de la máquina destino (en nuestro caso, arquitectura Intel x86 / Pentium de 32 bits).

```
+------------------+       +-------------------+       +-----------------------+
| Entrada:         |  ==>  | Generador de      |  ==>  | Salida:               |
| Lista de Tercetos|       | Código Assembler  |       | Código Assembler x86  |
| agrupados en     |       | (Seguimiento de   |       | (.asm ejecutable      |
| ámbitos          |       | Registros)        |       | con MASM32)           |
+------------------+       +-------------------+       +-----------------------+
```

Para expresiones aritméticas y asignaciones, la cátedra enseña **dos técnicas fundamentales**:
1. **Variables Auxiliares** (técnica básica, alto acceso a memoria).
2. **Seguimiento de Registros** (*Register Tracking*, técnica eficiente, óptima en tamaño y velocidad).

---

## 2. Técnica 1: Variables Auxiliares

### Mecánica
1. Se recorre la lista de tercetos en orden secuencial.
2. Por cada terceto que realiza una operación, se reserva una **variable auxiliar en memoria** (ej. `@aux1`, `@aux2`, ...).
3. Se genera código assembler que calcula la operación en un registro de trabajo y vuelca inmediatamente el resultado en la variable auxiliar correspondiente.
4. Se anota en el terceto el nombre de la variable auxiliar asignada, para que tercetos posteriores que lo referencien utilicen dicha variable como operando en memoria.

### Ejemplo de la Cátedra
Expresión: `z := a + b * c - d / (e + f) + 20`

Tercetos generados:
```text
10. ( *  , b    , c    )
11. ( +  , a    , [10] )
12. ( +  , e    , f    )
13. ( /  , d    , [12] )
14. ( -  , [11] , [13] )
15. ( +  , [14] , 20   )
16. ( := , z    , [15] )
```

Traducción paso a paso con Variables Auxiliares:

| Terceto con anotación | Código Assembler Generado | Comentario |
| :--- | :--- | :--- |
| `10. ( * , b , c ) @aux1` | `MOV R1, _b`<br>`MUL R1, _c`<br>`MOV @aux1, R1` | Calcula en `R1` y guarda en `@aux1` |
| `11. ( + , a , [10] ) @aux2` | `MOV R1, _a`<br>`ADD R1, @aux1`<br>`MOV @aux2, R1` | Lee `@aux1` y guarda en `@aux2` |
| `12. ( + , e , f ) @aux3` | `MOV R1, _e`<br>`ADD R1, _f`<br>`MOV @aux3, R1` | Calcula y guarda en `@aux3` |
| `13. ( / , d , [12] ) @aux4` | `MOV R1, _d`<br>`DIV R1, @aux3`<br>`MOV @aux4, R1` | Lee `@aux3` y guarda en `@aux4` |
| `14. ( - , [11] , [13] ) @aux5` | `MOV R1, @aux2`<br>`SUB R1, @aux4`<br>`MOV @aux5, R1` | Opera memoria contra memoria y guarda en `@aux5` |
| `15. ( + , [14] , 20 ) @aux6` | `MOV R1, @aux5`<br>`ADD R1, 20`<br>`MOV @aux6, R1` | Guarda en `@aux6` |
| `16. ( := , z , [15] )` | `MOV R1, @aux6`<br>`MOV _z, R1` | Asignación final a `z` |

### Balance de la Técnica
* **Resultado**: **20 instrucciones assembler** + **6 variables auxiliares** creadas en el segmento de datos `.data`.
* **Problema**: Gran cantidad de instrucciones `MOV` redundantes entre registros y memoria RAM, lo que degrada fuertemente el rendimiento y engrosa el binario.

---

## 3. Técnica 2: Seguimiento de Registros (*Register Tracking*)

### Mecánica
1. Previo a generar código para un terceto, el compilador consulta una **Tabla de Ocupación de Registros** para saber qué registro está disponible (`L` = Libre, `O` = Ocupado).
2. Se genera código realizando la operación directamente sobre los registros de la CPU.
3. El registro donde queda almacenado el resultado se marca como `Ocupado` (`O`).
4. Se anota en el terceto la **identidad del registro** que contiene su resultado.
5. Cuando un terceto posterior consume una referencia `[i]`, el registro anotado en `[i]` puede ser liberado (`L`) si ya no tiene otros usos pendientes.

### Tabla de Ocupación de Registros Inicial
```text
  R1   R2   R3   R4
+----+----+----+----+
| L  | L  | L  | L  |
+----+----+----+----+
```

### Ejemplo Paso a Paso (Misma expresión `z := a + b * c - d / (e + f) + 20`)

#### Paso 1: `10. ( * , b , c )`
* Hay registros libres: se toma `R1` (pasa a `O`).
* Código generado:
  ```asm
  MOV R1, _b
  MUL R1, _c
  ```
* Terceto anotado: `10. ( * , b , c ) -> R1`
* Tabla de registros:
  ```text
    R1   R2   R3   R4
  |  O |  L |  L |  L |
  ```

#### Paso 2: `11. ( + , a , [10] )`
* El segundo operando `[10]` está en `R1`.
* Operación conmutativa: se opera directamente sobre `R1` sumando la variable `_a`.
* Código generado:
  ```asm
  ADD R1, _a
  ```
* Terceto anotado: `11. ( + , a , [10] ) -> R1`
* Tabla de registros: `R1` sigue ocupado con el resultado.
  ```text
    R1   R2   R3   R4
  |  O |  L |  L |  L |
  ```

#### Paso 3: `12. ( + , e , f )`
* Operación entre dos variables. Como `R1` está ocupado (guarda el resultado parcial de `[11]`), se busca otro registro libre: se toma `R2` (pasa a `O`).
* Código generado:
  ```asm
  MOV R2, _e
  ADD R2, _f
  ```
* Terceto anotado: `12. ( + , e , f ) -> R2`
* Tabla de registros:
  ```text
    R1   R2   R3   R4
  |  O |  O |  L |  L |
  ```

#### Paso 4: `13. ( / , d , [12] )`
* Operación no conmutativa: `_d / R2`. No se puede hacer `DIV R2, _d` porque dividiría al revés.
* Se ocupa un nuevo registro libre `R3` para el numerador `_d`.
* Se realiza la división `R3 / R2`.
* Como `R2` ya fue consumido y no se vuelve a usar, **se libera `R2`** (`R2` pasa a `L`).
* Código generado:
  ```asm
  MOV R3, _d
  DIV R3, R2
  ```
* Terceto anotado: `13. ( / , d , [12] ) -> R3`
* Tabla de registros:
  ```text
    R1   R2   R3   R4
  |  O |  L |  O |  L |
  ```

#### Paso 5: `14. ( - , [11] , [13] )`
* Operación entre dos registros: `[11]` está en `R1` y `[13]` está en `R3`.
* Se resta sobre el primer registro: `SUB R1, R3`.
* Como `R3` ya entregó su resultado, **se libera `R3`** (`R3` pasa a `L`).
* Código generado:
  ```asm
  SUB R1, R3
  ```
* Terceto anotado: `14. ( - , [11] , [13] ) -> R1`
* Tabla de registros:
  ```text
    R1   R2   R3   R4
  |  O |  L |  L |  L |
  ```

#### Paso 6: `15. ( + , [14] , 20 )`
* Operación entre registro `R1` y constante `20`.
* Se opera directamente sobre `R1`.
* Código generado:
  ```asm
  ADD R1, 20
  ```
* Terceto anotado: `15. ( + , [14] , 20 ) -> R1`
* Tabla de registros:
  ```text
    R1   R2   R3   R4
  |  O |  L |  L |  L |
  ```

#### Paso 7: `16. ( := , z , [15] )`
* Asignación: el valor a asignar está en `R1`.
* Se mueve de `R1` a la variable destino `_z`.
* Al finalizar la sentencia, **se libera `R1`** (`R1` pasa a `L`).
* Código generado:
  ```asm
  MOV _z, R1
  ```
* Tabla de registros final: todos libres.
  ```text
    R1   R2   R3   R4
  |  L |  L |  L |  L |
  ```

### Comparativa Final
```text
+-----------------------+-----------------------------+-----------------------------+
| Métrica               | Con Variables Auxiliares    | Con Seguimiento de Regs     |
+-----------------------+-----------------------------+-----------------------------+
| Instrucciones ASM     | 20 instrucciones            | 10 instrucciones (-50%)     |
| Variables Auxiliares  | 6 variables (@aux1..@aux6)  | 0 variables (0 bytes RAM)   |
| Accesos a Memoria RAM | Múltiples escrituras/lecturas| Solo lectura de vars base   |
| Eficiencia de Código  | Lento y abultado            | Código más chico y rápido   |
+-----------------------+-----------------------------+-----------------------------+
```

---

## 4. Matriz de Decisiones del Algoritmo (Reglas de la Cátedra)

Al recorrer los tercetos de expresiones y asignaciones, el compilador debe aplicar estrictamente las siguientes **situaciones canónicas**:

### Operaciones Binarias Aritméticas `( OP , operando1 , operando2 )`

#### Situación 1: Operación entre 2 variables y/o constantes `( OP , vble1 , vble2 )`
1. Ocupar un registro libre (ej. `R1`).
2. Generar código sobre ese registro:
   ```asm
   MOV R1, vble1
   OP  R1, vble2
   ```
3. Anotar `R1` en el terceto. `R1` queda **Ocupado**.

#### Situación 2: Operación entre un registro y una variable o constante `( OP , reg , vble )`
1. Generar código directamente sobre el registro que ya tiene el acumulado:
   ```asm
   OP reg, vble
   ```
2. La tabla de registros queda sin cambios (`reg` sigue **Ocupado**).
3. Anotar `reg` en el terceto.

#### Situación 3: Operación entre dos registros `( OP , reg1 , reg2 )`
1. Generar código sobre el primer registro:
   ```asm
   OP reg1, reg2
   ```
2. **Liberar el segundo registro** (`reg2` pasa a **Libre**).
3. El resultado queda en `reg1` (**Ocupado**).
4. Anotar `reg1` en el terceto.

#### Situación 4: Operación entre una variable (o constante) y un registro `( OP , vble , reg )`
Depende de la conmutatividad del operador:

* **Situación 4.a: Operación conmutativa (`+`, `*`)**:
  * Como `vble + reg == reg + vble`, no hace falta mover a un registro nuevo.
  * Se genera código sobre el registro ya existente:
    ```asm
    OP reg, vble
    ```
  * La tabla no cambia (`reg` sigue **Ocupado**). Anotar `reg` en el terceto.

* **Situación 4.b: Operación NO conmutativa (`-`, `/`)**:
  * El minuendo/dividendo `vble` debe estar en el registro destino antes de operar.
  1. Ocupar un nuevo registro (ej. `reg_nuevo`).
  2. Mover la variable al nuevo registro:
     ```asm
     MOV reg_nuevo, vble
     OP  reg_nuevo, reg
     ```
  3. **Liberar el registro anterior** (`reg` pasa a **Libre**).
  4. Anotar `reg_nuevo` en el terceto (`reg_nuevo` queda **Ocupado**).

---

### Asignaciones `( := , destino , origen )`

#### Asignación — Situación a: Valor a asignar está en un registro `( := , vble , reg )`
1. Generar código moviendo el registro a la variable destino:
   ```asm
   MOV vble, reg
   ```
2. **Liberar el registro** (`reg` pasa a **Libre**).

#### Asignación — Situación b: Valor a asignar es directo desde variable o constante `( := , vble1 , vble2 )`
En la arquitectura x86 no es válida la instrucción memoria a memoria (`MOV mem, mem`).
1. Ocupar un registro temporal libre (ej. `R1`).
2. Generar código usando el registro intermedio:
   ```asm
   MOV R1, vble2
   MOV vble1, R1
   ```
3. **Liberar el registro** (`R1` pasa a **Libre**).

---

## 5. Reglas de Capacidad, Derrame a Memoria y Mapeo a x86 Real

### 1. Cantidad Máxima de Registros Necesarios
El número teórico de registros requeridos en el peor caso depende de los niveles de precedencia que se pueden anidar en la expresión:
* Con **2 niveles** (`+` y `-`, `*` y `/`) $\to$ **2 registros** simultáneos.
* Con **3 niveles** (incorporando potencia u operadores unarios) $\to$ **3 registros**.
* Con **4 niveles** (incorporando comparaciones `<` y `>`) $\to$ **4 registros**.
* Con **5 niveles** (incorporando `AND` y `OR` booleanos) $\to$ **5 registros**.

### 2. Derrame a Memoria (*Spilling*)
Si en una expresión compleja se requiere un registro libre y todos los registros disponibles de la CPU están ocupados:
1. Se selecciona un registro ocupado para desalojar.
2. Se genera una variable auxiliar en memoria (`@aux_spill`).
3. Se emite un `MOV @aux_spill, reg` para guardar su valor transitorio.
4. Se marca el registro como **Libre**, se utiliza para la nueva operación y, cuando se necesite el valor desalojado, se lee desde `@aux_spill`.

### 3. Consideraciones Específicas para Intel Pentium (x86 32-bit)
En la arquitectura x86 de 32 bits, los registros generales son: `EAX`, `EBX`, `ECX`, `EDX`.

> **Recomendación de la Cátedra**:
> * Los registros `EAX` y `EDX` tienen funciones dedicadas obligatorias en la arquitectura x86 (por ejemplo, `MUL`/`IMUL` y `DIV`/`IDIV` utilizan implícitamente el par `EDX:EAX`, y las llamadas a funciones retornan su resultado en `EAX`).
> * Por lo tanto, el algoritmo de asignación de registros debe **postergar el uso de `EAX` y `EDX`**, comenzando prioritariamente con `EBX` y `ECX` para operaciones intermedias generales. Así se evita tener que desalojar registros cuando surja una multiplicación o división.

---

## 6. Preguntas Típicas de Defensa Oral (Cátedra)

### ¿Por qué elegimos Seguimiento de Registros sobre Variables Auxiliares?
> *"Porque reduce a la mitad la cantidad de instrucciones generadas (elimina los `MOV` de ida y vuelta a memoria), no consume espacio en el segmento de datos (`.data`) y aprovecha la alta velocidad de los registros internos de la CPU."*

### En la Situación 4.b (`vble - reg`), ¿por qué se necesita un registro nuevo en lugar de operar sobre el que ya teníamos?
> *"Porque la resta y la división no son conmutativas: `vble - reg != reg - vble`. Si hiciéramos `SUB reg, vble`, estaríamos calculando `reg - vble`. Para respetar la semántica, debemos cargar `vble` en un nuevo registro libre y restarle el registro previo, liberando luego este último."*

### ¿Qué información se le agrega a cada Terceto durante la Generación de Código?
> *"A cada terceto se le anota el registro de CPU en el cual queda disponible su resultado intermedio (o la variable auxiliar si hubo derrame / spilling). Así, los tercetos posteriores que referencian a este mediante `[i]` saben exactamente de qué registro leer el operando."*

### ¿Por qué en la asignación `a := b` se utiliza un registro intermedio si son dos variables?
> *"Porque la arquitectura Intel x86 no soporta instrucciones con dos operandos de memoria (`MOV mem, mem` es una instrucción ilegal). Es obligatorio pasar por un registro de la CPU: `MOV EAX, _b` seguido de `MOV _a, EAX`."*
