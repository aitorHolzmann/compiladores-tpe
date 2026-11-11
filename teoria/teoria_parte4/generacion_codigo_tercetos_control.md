# Generación de Código Assembler para Sentencias de Control desde Tercetos

> **Documento de Cátedra — Resumen Especializado para Grupo 20**  
> *Materia:* Diseño de Compiladores I / Compiladores e Intérpretes  
> *Base teórica:* Extracto exclusivo de la sección Tercetos del documento `10.2.Generación de Código AS-Ass PI-Ass T-Ass.pdf` (descartando Árbol Sintáctico y Polaca Inversa).

---

## 1. Introducción: El Desafío de las Sentencias de Control

En las expresiones aritméticas lineales, el flujo de ejecución en Assembler es estrictamente secuencial. En cambio, en las sentencias de control (**bifurcaciones `IF`** e **iteraciones `WHILE` / `REPEAT UNTIL`**), surgen dos preguntas técnicas fundamentales:

1. **¿Dónde van las bifurcaciones?** (Instrucciones de salto condicional `BF` e incondicional `BI` en Assembler).
2. **¿Dónde van las etiquetas de destino?** (Labels simbólicos como `Label17:`, `Label25:` que marcan el punto de llegada del salto).

```
        +----------------------------------------------------+
        |                Tercetos de Control                |
        +----------------------------------------------------+
                                  |
                                  v
+-----------------------------+       +-----------------------------+
|    Saltos Condicionales     |       |    Saltos Incondicionales   |
|   ( BF , [cond] , destino ) |       |      ( BI , destino , - )   |
|             |               |       |              |              |
|             v               |       |              v              |
|  CMP reg1, reg2             |       |          JMP LabelX         |
|  J<cond_invertida> LabelX   |       +-----------------------------+
+-----------------------------+
```

---

## 2. Las 3 Alternativas de la Cátedra para Manejar Etiquetas

Para asociar las referencias de tercetos numéricas (`destino = 17`) con etiquetas Assembler (`Label17:`), la cátedra define tres estrategias de implementación:

```
+--------------------------------------------------------------------------------------------------+
| Alternativa 1 (2 Pasadas):                                                                      |
|   - Pasada 1: Recorrer tercetos buscando BF/BI y marcar en un conjunto los tercetos destino.    |
|   - Pasada 2: Generar código. Si el terceto actual está marcado, emitir "LabelX:" antes.       |
+--------------------------------------------------------------------------------------------------+
| Alternativa 2.a (1 Pasada con marcado previo):                                                   |
|   - Durante la Etapa 3 (al hacer backpatching de BF/BI), se anota la etiqueta en el terceto meta.|
|   - En una sola pasada se genera código: si el terceto tiene etiqueta asociada, se emite antes.  |
+--------------------------------------------------------------------------------------------------+
| Alternativa 2.b (1 Pasada con tercetos etiqueta):                                                |
|   - Durante la Etapa 3, se insertan pseudo-tercetos explícitos: ( LabelX , - , - ).              |
|   - En una sola pasada, al encontrar un terceto etiqueta, se emite directamente "LabelX:".      |
+--------------------------------------------------------------------------------------------------+
```

---

## 3. Sentencia IF-THEN-ELSE

### Código Fuente de Ejemplo
```text
IF ( a - b > c + 1 )
    THEN a := b + c;
    ELSE a := b - c;
```

### Lista de Tercetos
```text
 9. ...
10. ( -  , a    , b    )         ; Evalúa lado izquierdo de la condición
11. ( +  , c    , 1    )         ; Evalúa lado derecho de la condición
12. ( >  , [10] , [11] )         ; Comparación
13. ( BF , [12] , 17   )         ; Si falso, salta a la rama ELSE (terceto 17)
14. ( +  , b    , c    )         ; [RAMA THEN]
15. ( := , a    , [14] )
16. ( BI , 19   , -    )         ; Salto incondicional al final del IF (terceto 19)
17. ( -  , b    , c    )         ; [RAMA ELSE] (Destino del BF)
18. ( := , a    , [17] )
19. FUERA_DEL_IF                 ; Destino del BI
```

---

### Traducción a Assembler Paso a Paso con Seguimiento de Registros

| Terceto Evaluado | Código Assembler Generado | Estado de Registros (`R1 R2 R3 R4`) | Justificación Técnica |
| :--- | :--- | :---: | :--- |
| `10. ( - , a , b )` | `MOV R1, _a`<br>`SUB R1, _b` | `O L L L` | Situación 1: calcula `a - b` en `R1`. |
| `11. ( + , c , 1 )` | `MOV R2, _c`<br>`ADD R2, 1` | `O O L L` | Como `R1` está ocupado, toma `R2` para `c + 1`. |
| `12. ( > , [10] , [11] )` | `CMP R1, R2` | **`L L L L`** | **CRÍTICO:** Compara los registros y actualiza los flags de la CPU (`EFLAGS`). **Ambos registros se liberan de inmediato.** |
| `13. ( BF , [12] , 17 )` | `JLE Label17` | `L L L L` | **Condición Invertida:** Si la condición original era `>`, el salto por falso (`BF`) salta si es `<=` (`JLE`). |
| `14. ( + , b , c )` | `MOV R1, _b`<br>`ADD R1, _c` | `O L L L` | Rama THEN: arranca con registros libres. |
| `15. ( := , a , [14] )` | `MOV _a, R1` | `L L L L` | Asignación final de la rama THEN; libera `R1`. |
| `16. ( BI , 19 , - )` | `JMP Label19` | `L L L L` | Salto incondicional para no ejecutar la rama ELSE. |
| **Punto de Entrada ELSE** | **`Label17:`** | `L L L L` | Se emite la etiqueta de destino del `BF`. |
| `17. ( - , b , c )` | `MOV R1, _b`<br>`SUB R1, _c` | `O L L L` | Rama ELSE: calcula en `R1`. |
| `18. ( := , a , [17] )` | `MOV _a, R1` | `L L L L` | Asignación final de la rama ELSE; libera `R1`. |
| **Fin del IF** | **`Label19:`** | `L L L L` | Se emite la etiqueta de convergencia final. |

### Bloque Assembler Limpio Resultante (x86)
```asm
    MOV R1, _a
    SUB R1, _b
    MOV R2, _c
    ADD R2, 1
    CMP R1, R2
    JLE Label17        ; Salto por falso si (a - b) <= (c + 1)
    
    ; --- Rama THEN ---
    MOV R1, _b
    ADD R1, _c
    MOV _a, R1
    JMP Label19        ; Salteo de la rama ELSE
    
Label17:
    ; --- Rama ELSE ---
    MOV R1, _b
    SUB R1, _c
    MOV _a, R1

Label19:
    ; --- Continuación del programa ---
```

---

## 4. Sentencias Iterativas: Bucle WHILE

### Código Fuente de Ejemplo
```text
WHILE ( a + b <= c - d ) {
    a := b + c;
    c := d - 3;
}
```

### Lista de Tercetos
```text
24. ...
25. ( +  , a    , b    )         ; [INICIO DEL BUCLE] Evalúa lado izq. condición
26. ( -  , c    , d    )         ; Evalúa lado der. condición
27. ( <= , [25] , [26] )         ; Comparación
28. ( BF , [27] , 34   )         ; Si falso, sale del bucle (salta a terceto 34)
29. ( +  , b    , c    )         ; [CUERPO DEL BUCLE]
30. ( := , a    , [29] )
31. ( -  , d    , 3    )
32. ( := , c    , [31] )
33. ( BI , 25   , -    )         ; Re-itera: salta al inicio de la condición (terceto 25)
34. SENT_SIG_AL_WHILE            ; Destino de salida del bucle
```

---

### Traducción a Assembler con Seguimiento de Registros

| Terceto Evaluado | Código Assembler Generado | Estado de Registros | Justificación Técnica |
| :--- | :--- | :---: | :--- |
| **Inicio del Bucle** | **`Label25:`** | `L L L L` | Etiqueta para re-evaluar la condición en cada vuelta. |
| `25. ( + , a , b )` | `MOV R1, _a`<br>`ADD R1, _b` | `O L L L` | Evalúa lado izquierdo en `R1`. |
| `26. ( - , c , d )` | `MOV R2, _c`<br>`SUB R2, _d` | `O O L L` | Evalúa lado derecho en `R2`. |
| `27. ( <= , [25] , [26] )` | `CMP R1, R2` | **`L L L L`** | Compara y **libera ambos registros**. |
| `28. ( BF , [27] , 34 )` | `JG Label34` | `L L L L` | **Condición Invertida:** Si la condición era `<=`, el `BF` salta con `>` (`JG`) para salir. |
| `29. ( + , b , c )` | `MOV R1, _b`<br>`ADD R1, _c` | `O L L L` | Cuerpo del bucle. |
| `30. ( := , a , [29] )` | `MOV _a, R1` | `L L L L` | Asigna y libera `R1`. |
| `31. ( - , d , 3 )` | `MOV R1, _d`<br>`SUB R1, 3` | `O L L L` | Segunda sentencia del cuerpo. |
| `32. ( := , c , [31] )` | `MOV _c, R1` | `L L L L` | Asigna y libera `R1`. |
| `33. ( BI , 25 , - )` | `JMP Label25` | `L L L L` | Salto incondicional al inicio del ciclo. |
| **Salida del Bucle** | **`Label34:`** | `L L L L` | Etiqueta de escape al cumplirse la condición de salida. |

### Bloque Assembler Limpio Resultante (x86)
```asm
Label25:
    ; --- Evaluación de la Condición ---
    MOV R1, _a
    ADD R1, _b
    MOV R2, _c
    SUB R2, _d
    CMP R1, R2
    JG Label34         ; Sale del bucle si (a + b) > (c - d)
    
    ; --- Cuerpo del Bucle ---
    MOV R1, _b
    ADD R1, _c
    MOV _a, R1
    
    MOV R1, _d
    SUB R1, 3
    MOV _c, R1
    
    JMP Label25        ; Próxima iteración

Label34:
    ; --- Continuación luego del WHILE ---
```

---

## 5. Particularidad del Grupo 20: Iteración `REPEAT ... UNTIL` (Tema 12)

En nuestro compilador del Grupo 20, la estructura asignada es `REPEAT <bloque> UNTIL ( <condicion> );`.

A diferencia de un `WHILE` (bucle con condición previa), el `REPEAT UNTIL` es un **bucle con condición posterior**:
1. El cuerpo se ejecuta **al menos una vez**.
2. Al final del bloque se evalúa la condición.
3. Se repite **MIENTRAS SEA FALSO** (es decir, sale cuando la condición es verdadera).

### Mapeo de Tercetos en `REPEAT UNTIL`:
```text
10. ...
11. [Primer terceto del cuerpo del REPEAT]   <--- Destino del salto iterativo (Label11)
    ... (sentencias del cuerpo) ...
20. ( > , op1 , op2 )                       ; Evaluación de la condición
21. ( BF , [20] , 11 )                      ; Si es FALSO, vuelve a iterar a Label11
22. [Sentencia siguiente al REPEAT]         ; Si es VERDADERO, continúa secuencialmente
```
> **Diferencia clave con WHILE**: En `REPEAT UNTIL`, no hay `BI` hacia atrás ni etiqueta de escape adelante: el salto condicional `BF` apunta directamente al inicio del cuerpo (`Label11:`), logrando un bucle más compacto y eficiente.

---

## 6. Tabla Maestra de Traducción de Operadores (`BF` y `EFLAGS`)

Dado que el terceto es `BF` (*Branch on False*), la instrucción de salto en Assembler debe verificar la **condición complementaria (negada)**:

| Operador en el Terceto | Significado | Condición de Salto por Falso (`BF`) | Mnemónico x86 Pentium |
| :---: | :---: | :---: | :---: |
| `>` | Mayor | Menor o Igual | `JLE` (*Jump Less or Equal*) |
| `<` | Menor | Mayor o Igual | `JGE` (*Jump Greater or Equal*) |
| `>=` | Mayor o Igual | Menor estricto | `JL` (*Jump Less*) |
| `<=` | Menor o Igual | Mayor estricto | `JG` (*Jump Greater*) |
| `==` | Igual | Distinto | `JNE` (*Jump Not Equal*) |
| `!=` | Distinto | Igual | `JE` (*Jump Equal*) |

---

## 7. Preguntas Clave para la Defensa Oral (Cátedra)

### ¿Por qué en un `CMP` se liberan todos los registros (`L-L-L-L`) antes de emitir el salto condicional?
> *"Porque la instrucción `CMP R1, R2` realiza una resta interna que no altera los operandos pero actualiza el registro de estado de la CPU (`EFLAGS` / `FLAGS`). El resultado lógico de la comparación ya reside en los flags del procesador (`ZF`, `SF`, `OF`, `CF`), por lo que los registros de datos ya no son necesarios y deben quedar libres para las sentencias que siguen en cada rama."*

### ¿Por qué un terceto `( > , a , b )` seguido de `( BF , ... )` genera `JLE` y no `JG`?
> *"Porque el terceto es `BF` (Branch on False). Si la condición original era `a > b`, la bifurcación debe tomarse cuando dicha condición NO se cumpla. El complemento lógico de `>` es `<=`, por lo que en Assembler la instrucción correspondiente tras el `CMP` es `JLE` (Jump if Less or Equal)."*

### ¿Cómo garantiza el algoritmo que no queden registros ocupados al bifurcar o entrar a una etiqueta?
> *"Porque cada sentencia o expresión aritmética asigna su resultado final a una variable en memoria antes de una bifurcación o fin de bloque, liberando sus registros. Al ingresar a cualquier etiqueta (`Label`), el estado de ocupación de registros es canónicamente `L-L-L-L`, garantizando que ninguna rama dependa de registros cargados en una rama alternativa divergente."*
