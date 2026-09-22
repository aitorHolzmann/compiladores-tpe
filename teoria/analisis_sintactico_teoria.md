Diseño de Compiladores I Compiladores e Intérpretes

Análisis Sintáctico

## Fases de la Compilación

Programa Análisis Salida Fuente Léxico

Errores

Análisis Generación Sintáctico de Código

Tabla de Símbolos

Análisis Sintáctico

## Fases de la Compilación

Errores

**Tira de**

**tokens**

Programa Análisis Análisis Salida Fuente Sintáctico Léxico

Tabla de Símbolos

Generación de Código

Análisis Sintáctico

## Fases de la Compilación

Errores

**Tira de**

**tokens**

Programa

**Análisis**

Análisis Salida Fuente

**Sintáctico**

Léxico

Tabla de Símbolos

# \*

Generación de Código

Análisis Sintáctico

## Análisis Sintáctico

## Agrupa los tokens del programa

## fuente en frases gramaticales que el

## compilador usará en las siguientes

etapas.

Análisis Sintáctico

## Análisis Sintáctico

## Obtiene una cadena de tokens del Analizador

## Léxico, y verifica que la cadena de tokens

## presentes en la entrada pueda ser generada

mediante la gramática del lenguaje.

Análisis Sintáctico

Análisis Léxico Ejemplo

if Plazo \>= 30 then Tasa := Base + Recargo / 100 else Tasa := Base

**\[59\] \[27\] \[80\] \[28\] \[60\] \[27\] \[85\] \[27\] \[70\] \[27\]**

**\[73\] \[28\] \[61\] \[27\] \[85\] \[27\]**

IF ID \>= CTE THEN ID := ID + ID / CTE ELSE ID

**:= ID**

Análisis Sintáctico

## Análisis Sintáctico

 Las construcciones de un lenguaje de programación pueden ser descriptas mediante una gramática independiente de contexto, utilizando BNF.  Las reglas de la gramática se representan por medio de producciones.  Cada producción define un símbolo no terminal en función de símbolos terminales o tokens, y otros símbolos no terminales.

 Existe una producción que define al no terminal programa.

Análisis Sintáctico

Gramática

| 1)      | PROGRAMA  LS                             |
|---------|-------------------------------------------|
| 2)      | LS  LS ST \| ST                          |
| 3)      | ST  S                                    |
| 4)      | ST  A                                    |
| 5) S    |  if C then ST else ST                    |
| 6)      | C  E CP E                                |
| 7)      | CP  \< \| \> \| \<= \| \>= \| == \| \<\> |
| 8)      | A  id := E                               |
| 9)      | E  E + T                                 |
| 10)     | E  E – T                                 |
| 11) 12) | E T T T                                 |
| 13)     | T T / F                                  |
| 14)     | T  F                                     |
| 15)     | F  id                                    |
| 16)     | F  cte Análisis Sintáctico               |

## ¿Se podría representar

## esta gramática con un

Autómata Finito?

Análisis Sintáctico

## Análisis Sintáctico

 Conceptualmente, el Analizador Sintáctico construye un árbol de parsing.  El árbol de parsing describe la estructura sintáctica del código de entrada.  El árbol de parsing demuestra como la secuencia de tokens de entrada puede ser derivada a partir de las reglas de la gramática.

Análisis Sintáctico

## Árbol de Parsing

Tasa := Base + Recargo / 100  id := id + id / cte

A

E (8) (9)

E (13) (11)

T T

(14) (14)

F F F

(15) (15)

id := id + id  cte

Lista de reglas: 15 14 11 15 14 16 13 9 8

8\) A  id := E 9) E  E + T 10) E  E – T 11) E  T F 12) T  T\* T 13) T  T / F 14) T  F 15) F  id 16) F  cte

(16)

Análisis Sintáctico

¿El compilador construye el Árbol de Parsing?

Análisis Sintáctico

Análisis Sintáctico Implementación

##  Se requiere una estructura más compleja que

un Autómata finito.

##  Se requiere un Autómata de Pila

Análisis Sintáctico

## Estrategias de Parsing

 Parsing Ascendente (botton up)  Construye el árbol desde las hojas a la raíz

 Parsing Descendente (top down)  Construye el árbol desde la raíz a las hojas

Análisis Sintáctico

## Estrategias de Parsing

 El Parsing Descendente utiliza gramáticas LL.  El Parsing Ascendente utiliza gramáticas LR.  Los métodos ascendente y descendente más eficientes no funcionan para todas las gramáticas.  Las gramáticas LL y LR son lo bastante expresivas como para describir la mayoría de las construcciones sintácticas de los lenguajes de programación modernos.

Análisis Sintáctico

## Estrategias de Parsing

 Los Analizadores Sintácticos que se implementan manualmente utilizan con frecuencia gramáticas LL (descendente)  Los Analizadores Sintácticos para la clase más extendida de gramáticas LR (ascendente), se construyen generalmente mediante herramientas.

Análisis Sintáctico

Parsing Ascendente Gramáticas LR

 Va del programa a la hipótesis  El programa se lee de izquierda a derecha (L).  Las reglas se leen de derecha a izquierda (R): el lado derecho se reemplaza por el izquierdo.  Estrategia: Reducción

Análisis Sintáctico

Parsing Ascendente Ejemplo

precio := costo1 \* 1.5 + costo2 \* 1.2  id := id \* cte + id \* cte

**Gramática Árbol de Parsing**

1\) A  id := E 2) E  E + T 3) E T F 4) T T\* 5) T  F 6) F  id 7) F  cte

Lista de Reglas: 6 5 7 4 3 6 5 7 4 2 1

A

E (2) (1)

E

(3)

T T (4) (4)

T T

(5) (5) F F F F

(6) (7) (6) (7) \* \* id := id + id cte cte

Análisis Sintáctico

Parsing Ascendente Ejemplo

Lista de Reglas: 6 5 7 4 3 6 5 7 4 2 1 id := id \* cte + id \* cte

6 id := F \* cte + id \* cte

5 id := T \* cte + id \* cte

7 id := T \* F + id \* cte

4 id := T + id \* cte

3 id := E + id \* cte

6 id := E + F \* cte

5 id := E + T \* cte

7 id := E + T \* F

4 id := E + T

2 id := E

**1 A**

1\) A  id := E 2) E  E + T 3) E T 4) T T\*F 5) T  F 6) F  id 7) F  cte

Análisis Sintáctico

Parsing Descendente Gramáticas LL

 Va de la hipótesis al programa  El programa se lee de izquierda a derecha (L).  Las reglas se leen de izquierda a derecha (L): el lado izquierdo se reemplaza por el derecho.  Estrategia: Expansión

Análisis Sintáctico

Parsing Descendente Ejemplo

precio := costo1 \* 1.5 + costo2 \* 1.2  id := id \* cte + id \* cte

**Gramática Árbol de Parsing**

1\) A  id := E 2) E  E + T 3) E T F 4) T T\* 5) T  F 6) F  id 7) F  cte

Lista de Reglas: 1 2 3 4 5 6 7 4 5 6 7

A (1)

:= E id (2)

\+ T E (4) (3) \* T F T (4) (5) (7)

F \* cte T F (6) (5) (7) id F cte

(6) id

Análisis Sintáctico

Parsing Descendente Ejemplo

Lista de Reglas: 1 2 3 4 5 6 7 4 5 6 7 A

1 id := E

2 id := E + T

3 id := T + T

4 id := T \* F + T

5 id := F \* F + T

6 id := id \* F + T

7 id := id \* cte + T

4 id := id \* cte + T \* F

5 id := id \* cte + F \* F

6 id := id \* cte + id \* F

7 id := id \* cte + id \* cte

1\) A  id := E 2) E  E + T 3) E T F 4) T T\* 5) T  F 6) F  id 7) F  cte

Análisis Sintáctico

## Parsing Descendente

 Predice el programa.

##  Sólo puede construir el programa deseado, si

se elige la secuencia correcta de reglas.  Otras secuencias dan otros programas.

##  El algoritmo debe permitir determinar la

secuencia correcta.

Análisis Sintáctico

## Fases de la Compilación

Programa Fuente

Errores

**Lista de**

**Tira de**

**tokens**

**reglas**

Análisis Generación Análisis Salida Sintáctico Léxico de Código

Tabla de Símbolos

Análisis Sintáctico

## Fases de la Compilación

Programa Fuente

**Errores**

**Lista de**

**Tira de**

**tokens**

**reglas**

Análisis Generación Análisis Salida Sintáctico Léxico de Código

Tabla de Símbolos

Análisis Sintáctico

Análisis Sintáctico Errores

 Ante un error, el Análisis Sintáctico se detiene.

 Para continuar con la compilación, se deben usar técnicas de recuperación: Modo pánico Producciones de error

Recuperación a nivel de frase (Requiere producciones de error) Corrección global

Análisis Sintáctico

¿Preguntas?

<image redacted: 210x290px, 210x290pt, ~72dpi, JPG, DEVICE_RGB, 32bpp>