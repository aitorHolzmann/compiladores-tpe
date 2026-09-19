## Diseño de Compiladores I

## Compiladores e Intérpretes

Análisis Léxico

# Fases de la Compilación

Programa Análisis Salida Fuente Léxico

Análisis Léxico

Errores

Análisis Generación Sintáctico de Código

Tabla de Símbolos

# Análisis Léxico

 Lee el programa fuente.  Agrupa los caracteres en unidades llamadas tokens.

token: Secuencia de caracteres que forman una unidad significativa

Análisis Léxico

<image redacted: 53x37px, 53x36pt, ~73dpi, PNG, DEVICE_RGB, 32bpp>

# Análisis Sintáctico

# Agrupa los tokens del programa

# fuente en frases gramaticales que el

# compilador usará en las siguientes

etapas.

Análisis Léxico

## Fases de la Compilación

ANÁLISIS OPTIMIZACIONES SEMÁNTICO

Generación de Código GENERACIÓN DE GENERACIÓN DE CÓDIGO CÓDIGO DE SALIDA INTERMEDIO

Generación de Código

Análisis Léxico

# Generación de Código

Análisis Semántico  Chequea reglas que no pueden ser capturadas por la gramática, pero que pueden ser verificadas en tiempo de compilación. Estas reglas corresponden a la semántica estática del lenguaje. Generación de Código Intermedio  Se construye una representación del código fuente como un programa escrito para ser ejecutado en una máquina abstracta. Optimización  Transforma la representación actual del código en una nueva versión que logra el mismo resultado más eficientemente. Generación de Código propiamente dicho  Se traduce la representación intermedia del programa fuente al código nativo de la máquina objetivo.  El código generado efectuará el chequeo de las reglas de semántica dinámica del lenguaje, que no pudieron ser verificadas durante la compilación. Análisis Léxico

 Tabla de Símbolos

 Es una estructura de datos que contiene un registro para cada símbolo utilizado en el código fuente, con campos que contienen información relevante para cada símbolo (atributos). Sólo se registran en la Tabla aquellos símbolos para los cuales pueden existir distintos lexemas para un mismo token.

 Errores

 Cada una de las etapas del Compilador puede detectar errores que son informados al programador.  Un buen compilador no debería terminar su ejecución al detectar un error, sino que debería recuperarse y continuar con la compilación.

Análisis Léxico

# Fases de la Compilación

Programa Fuente

Análisis Léxico

Errores

**Lista de**

**Tira de**

**tokens**

**reglas**

Análisis Generación Análisis Salida Sintáctico Léxico de Código

Tabla de Símbolos

## Interacción Analizador Léxico – Analizador

## Sintáctico – Generador de Código

 Batch

Programa Fuente

 Concurrente

 Ejemplo: gcc c1 ¦ c2 ¦ c3

Análisis Léxico

Tira de Lista de Tokens Reglas

Análisis Análisis Generación Salida Léxico Sintáctico de Código

Tabla de Tabla de Símbolos Símbolos

 (AL) (AS) (GC)

## Interacción Analizador Léxico – Analizador

## Sintáctico – Generador de Código

 Monolítico

 El Generador de Código es el main El A.S. entrega una regla cuando el G.C. se la pide, y el A.L. entrega un token cuando el A.S. se lo pide

Análisis Léxico

Generación Salida de Código

t

R ex le

ul

ru tn e

eg

Análisis Sintáctico

t T n ex ok ke e tn n to

eg

Análisis Programa Fuente Léxico

## Interacción Analizador Léxico – Analizador

## Sintáctico – Generador de Código

 Monolítico

 El Parser (A.S.) es el main: El Analizador Sintáctico va pidiendo tokens al Léxico, y cuando tiene una regla le pide al G.C. que genere código para ella.

Análisis Sintáctico

Análisis Programa Generación Salida Fuente Léxico de Código

Esta es la opción que utilizaYacc

Análisis Léxico

¿Por qué un Analizador Léxico?

Análisis Léxico

## Tokens

 Identificadores

 Constantes

 Cadenas de caracteres

 Palabras reservadas

 Operadores  Comparadores  etc.

Análisis Léxico

## Tokens

 Los tokens se diferencian de la cadena de caracteres que representan.  La cadena de caracteres es el Lexema o valor léxico.  Existen tokens que se corresponden con un único lexema  Ejemplo: Palabra Reservada IF  Existen tokens que pueden representar lexemas diferentes  Ejemplos:  Identificador Plazo, Identificador Tasa  Constante 100, Constante 1.5E-02  Cadena de caracteres:“ Hola mundo”,“ Esto es una cadena”

Análisis Léxico

## Análisis Léxico

## Ejemplo

## if Plazo \>= 30

## thenTasa := Base + Recargo / 100

## elseTasa := Base

Análisis Léxico

## Análisis Léxico

## Ejemplo

## \[if\] \[Plazo\] \[\>=\] \[30\]

## \[then\] \[Tasa\] \[:=\] \[Base\] \[+\] \[Recargo\] \[/\] \[100\]

## \[else\] \[Tasa\] \[:=\] \[Base\]

Análisis Léxico

# Tokens

Para las implementaciones en que el Analizador Sintáctico invoca al Léxico cada vez que necesita un token:  El Análizador Léxico hace una correspondencia entre cada tipo de token y un número entero.  El Analizador Léxico entrega al Analizador Sintáctico los números enteros que corresponden a cada tipo de token detectado en el código fuente. Para las implementaciones en que el Analizador Léxico entrega al Sintáctico la lista de tokens:  La lista incluirá directamente los tokens.

Análisis Léxico

# Tokens

Identificación Tipo de token del tipo de token

| ID   | 27  |
|------|-----|
| CTE  | 28  |
| IF   | 59  |
| THEN | 60  |
| ELSE | 61  |
| +    | 70  |
| /    | 73  |
| \>=  | 80  |
| :=   | 85  |

Análisis Léxico

## Análisis Léxico

## Ejemplo

## if Plazo \>= 30

## thenTasa := Base + Recargo / 100

## elseTasa := Base

## \[if\] \[Plazo\] \[\>=\] \[30\]

## \[then\] \[Tasa\] \[:=\] \[Base\] \[+\] \[Recargo\] \[/\] \[100\]

## \[else\] \[Tasa\] \[:=\] \[Base\]

Análisis Léxico

## Análisis Léxico

## Ejemplo

## if Plazo \>= 30

## thenTasa := Base + Recargo / 100

## elseTasa := Base

## \[59\] \[Plazo\] \[\>=\] \[30\]

## \[then\] \[Tasa\] \[:=\] \[Base\] \[+\] \[Recargo\] \[/\] \[100\]

## \[else\] \[Tasa\] \[:=\] \[Base\]

Análisis Léxico

## Análisis Léxico

## Ejemplo

## if Plazo \>= 30

## thenTasa := Base + Recargo / 100

## elseTasa := Base

## \[59\] \[27\] \[\>=\] \[30\]

## \[then\] \[Tasa\] \[:=\] \[Base\] \[+\] \[Recargo\] \[/\] \[100\]

## \[else\] \[Tasa\] \[:=\] \[Base\]

Análisis Léxico

## Análisis Léxico

## Ejemplo

## if Plazo \>= 30

## thenTasa := Base + Recargo / 100

## elseTasa := Base

## \[59\] \[27\] \[80\] \[30\]

## \[then\] \[Tasa\] \[:=\] \[Base\] \[+\] \[Recargo\] \[/\] \[100\]

## \[else\] \[Tasa\] \[:=\] \[Base\]

Análisis Léxico

## Análisis Léxico

## Ejemplo

## if Plazo \>= 30

## thenTasa := Base + Recargo / 100

## elseTasa := Base

## \[59\] \[27\] \[80\] \[28\]

## \[then\] \[Tasa\] \[:=\] \[Base\] \[+\] \[Recargo\] \[/\] \[100\]

## \[else\] \[Tasa\] \[:=\] \[Base\]

Análisis Léxico

## Análisis Léxico

## Ejemplo

## if Plazo \>= 30

## thenTasa := Base + Recargo / 100

## elseTasa := Base

## \[59\] \[27\] \[80\] \[28\]

## \[60\] \[27\] \[85\] \[27\] \[70\] \[27\] \[73\] \[28\]

## \[61\] \[27\] \[85\] \[27\]

Análisis Léxico

## Análisis Léxico

## Ejemplo

## if Plazo \>= 30

## thenTasa := Base + Recargo / 100

## elseTasa := Base

## \[IF\] \[ID\] \[GT\] \[CTE\]

## \+’

## \[THEN\] \[ID\] \[ASIG\] \[ID\] \[‘

## \] \[ID\] \[‘/’\] \[CTE\]

## \[ELSE\] \[ID\] \[ASIG\] \[ID\]

Análisis Léxico

# Tokens

 Puesto que un token puede representar más de un lexema, el A.L. debe enviar información adicional al A.S., en forma de atributo/s. Esa información será usada por el G.C.

 Por cada token detectado, el A.L. entrega un par: \<token, atributo\>  En principio, ese atributo sería el lexema.

Análisis Léxico

## Análisis Léxico

## Ejemplo

## if Plazo \>= 30

## thenTasa := Base + Recargo / 100

## elseTasa := Base

30’ \[59, ‘IF’ \] \[27, ‘Plazo’ \] \[80, ‘\>=’ \] \[28, ‘ \] :=’\] \[27, ‘Base’ \[60, ‘THEN’ \] \[27, ‘Tasa’ \] \[85, ‘ \] \[70, ‘+’ \] \[27,’Recargo’\] \[73, ‘/’\] \[28,‘ 100’ \] :=’\] \[27,‘Base’ \[61, ‘ELSE’ \] \[27,’Tasa’\] \[85, ‘ \]

Análisis Léxico

## Análisis Léxico

## Ejemplo

## if Plazo \>= 30

## thenTasa := Base + Recargo / 100

## elseTasa := Base

**\[59, ‘IF’**

**\] \[27, ‘Plazo’**

**\] \[80, ‘\>=’**

**\] \[28, ‘30’**

**\]**

**\[60, ‘THEN’**

**\] \[27, ‘Tasa’**

**\] \[85, ‘:=’**

**\] \[27, ‘Base’**

**\] \[70, ‘+’**

**\] \[27,’Recargo’\]**

**\[73, ‘/’**

**\] \[28, ‘100’**

**\]**

**\[61, ‘ELSE’**

**\] \[27,’Tasa’\] \[85, ‘:=’**

**\] \[27,‘Base’\]**

Análisis Léxico

# Tokens

ENTONCES:

** Sólo para aquellos tokens que pueden representar más**

de un lexema, el A.L. necesita entregar un par: \<token, atributo\> En principio, ese atributo será el lexema.

** Para aquellos tokens que representan un solo lexema,**

el A.L. puede entregar sólo el token.

Análisis Léxico

## Análisis Léxico

## Ejemplo

## if Plazo \>= 30

## thenTasa := Base + Recargo / 100

## elseTasa := Base

**\[59\] \[27, ‘Plazo’**

**\] \[80\] \[28, ‘30’**

**\]**

**\[60\] \[27, ‘Tasa’**

**\] \[85\] \[27, ‘Base’**

**\] \[70\] \[27,’Recargo’\] \[73\] \[28, ‘100’**

**\]**

**\[61\] \[27,’Tasa’\] \[85\] \[27,‘Base’\]**

Análisis Léxico

## Análisis Léxico

## Ejemplo

if Plazo \>= 30 then Tasa := Base + Recargo / 100 else Tasa := Base

**\[59\] \[27, ‘Plazo’**

**\] \[80\] \[28, ‘30’**

**\]**

**\[60\] \[27, ‘Tasa’**

**\] \[85\] \[27, ‘Base’**

**\] \[70\] \[27,’Recargo’\] \[73\] \[28, ‘100’**

**\]**

**\[61\] \[27,’Tasa’\] \[85\] \[27,‘Base’\]**

** Información necesaria para el Análisis Sintáctico:**

IF ID \>= CTE THEN ID := ID + ID / CTE ELSE ID := ID

** Los atributos se necesitan durante la Generación de**

**Código**

Análisis Léxico

# Atributos de los tokens

 Lexema, nro. de línea en que aparece el token, etc.

 En la práctica, la información adicional para cada token se almacena en una Tabla de Símbolos, y el atributo entregado es el puntero o referencia a la entrada correspondiente en la Tabla de Símbolos.

Análisis Léxico

# Tabla de Símbolos

Es una estructura de datos que contiene un registro para cada identificador (y todo otro token que pueda representar más de un lexema) utilizado en el código fuente, con campos que contienen información relevante para ese símbolo (atributos).

Análisis Léxico

# Tabla de Símbolos

 Cuando el Análisis Léxico detecta un token de tipo identificador, u otro token que pueda representar más de un lexema, lo ingresa en la Tabla de Símbolos. (El alta en la Tabla de Símbolos podría hacerse durante la Generación de Código)  Durante la Generación de Código se ingresa información para los atributos de los símbolos, y se usa esa información de diversas maneras.

 Durante la Generación de Código puede ser necesario incorporar nuevas entradas a la Tabla de Símbolos.

Análisis Léxico

# Análisis Léxico: Funciones

Principales:  Reconocer tokens

 Informar errores léxicos

Secundarias:

 Eliminar comentarios

 Eliminar blancos, tabulaciones, saltos de línea  Llevar la cuenta de los saltos de línea, para correlacionar los mensajes de error con el programa fuente

Análisis Léxico

# Construcción del Analizador Léxico

##  Construir un diagrama que represente la

estructura de los tokens del programa fuente.

 Convertir el diagrama en un programa.

Análisis Léxico

# Autómata Finito

 AF = { Q , Se , d , q0 , F }  Q : Conjunto finito de estados  Se : Conjunto finito de símbolos de entrada  d : Función de transición

 q0 : Estado inicial  F : Conjunto de estados finales

Análisis Léxico

# Gramática independiente de

# Contexto

 Conjunto de símbolos terminales: los tokens.  Conjunto de no terminales.  Conjunto de producciones donde cada producción consiste de un no terminal, o lado izquierdo, una flecha y una secuencia de tokens y/o no terminales, o lado derecho.

 Designación de uno de los no terminales como símbolo de inicio.

Análisis Léxico

# Gramáticas regulares

 Todo no terminal se define en base a terminales, o en base a un NT y un terminal

A  a \| Aa (recursiva a izquierda) A  a \| aB (recursiva a derecha)

Análisis Léxico

# Diagrama de Transición de estados

 Una gramática regular se puede representar mediante un diagrama de transición de estados Dado:

T = {a,b,c} NT = {A,B,C,S}

Gramática: Diagrama de Transición de Estados

A  Sa \| Aa B  Sb \| Bb \| Ab C  Bc

S  

Análisis Léxico

a

A a

b S

b c C

B

b

# Matriz de Transición de Estados

 El diagrama de transición de estados se puede representar mediante una matriz de transición de estados

Diagrama de Transición de estados Matriz de Transición de Estados a

A

a

b S

b c

B

b

Análisis Léxico

a b c

S A B

A A B

B B C C

C

# Analizador Léxico: Construcción

##  Identificadores: cadena que comienza con una letra y

## continúa con letras y/o dígitos (las letras pueden ser

## mayúsculas o minúsculas)

##  Constantes enteras (secuencia de dígitos)

\* \* … /  Comentarios tipo C /  Operadores aritméticos + - \* /  Comparadores \> \>= \< \<= == \<\>  Palabras reservadas IF, ELSE, WHILE

Análisis Léxico

# Analizador Léxico: Construcción

 Se construye el diagrama de transición de estados que represente la estructura de cada uno de los tokens del lenguaje.

Análisis Léxico

# Análisis Léxico: Gramática

 Identificador: cadena que comienza con una letra y continúa con letras y/o dígitos (las letras pueden ser mayúsculas o minúsculas) \* ID: letra ( letra \| digito )  Constante entera: secuencia de dígitos CTE: digito +

 Reglas Léxicas:  \<ID\>  \<letra\> \| \<ID\> \<letra\> \| \<ID\>\<digito\>  \<CTE\>  \<digito\> \| \<CTE\> \<digito\>  \<letra\>  a \| b \| c \| … \| z  \<digito\>  0 \| 1 \| 2 \| … \| 9

¿Esta gramática es regular?

Análisis Léxico

# Identificadores

 Identificador: cadena que comienza con una letra y continúa con letras y/o dígitos (las letras pueden ser mayúsculas o minúsculas) l

l c – l – d 0 1 F

l: letra mayúscula o minúscula d d: dígito

Gramática:

|  0: \<Inicio\> | \<ID\>  \<Inicio\> l \| \<ID\> l \| \<ID\> d  |
|-----------------|------------------------------------------------|
|  1: \<ID\>     | \<Fin\>  \<ID\> otro                          |
|  F: \<Fin\>    | (otro es cualquier carácter distinto de l o d) |

Expresión regular: \* ID: l ( l \| d )

Análisis Léxico

# Constantes

 Constante entera: secuencia de dígitos

d

d c – d 0 2 F

Gramática:

|  0: \<Inicio\> | \<CTE\>  \<Inicio\> d \| \<CTE\> d        |
|-----------------|--------------------------------------------|
|  2: \<CTE\>    | \<Fin\>  \<CTE\> otro                     |
|  F: \<Fin\>    | (otro es cualquier carácter distinto de d) |

Expresión regular: CTE: d +

Análisis Léxico

# Comentarios

\*

… /

** Comentarios tipo C: /\***

3‘ /’ C –

0 4

‘ /’

–‘/’ C – 5

|  0: \<Inicio\> | \<PCom\>  \<Inicio\> ‘\*’                                                                                                                                        |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|  3: \<PCom\>   | \<Com\>  \<PCom\> \| \<Com\> otro1 \|                                                                                                                            |
|  4: \<Com\>    | \<PFCom\> otro2                                                                                                                                                   |
|  5: \<PFCom\>  | (otro1 es cualquier carácter distinto de ‘\*’ y otro2 cualquier carácter distinto de ‘\*’ y ‘/’ ‘\*’ ‘\*’ \<PFCom\>  \<Com\> \| \<PFCom\> \<Inicio\>  \<PFCom\> |
|  )             |                                                                                                                                                                   |

Análisis Léxico

# Operadores

** Operadores aritméticos: + -**

‘ /’

0 F

 0: \<Inicio\> \<PCom\>  \<Inicio\>‘/’

 3: \<PCom\> \<Fin\>  \<Inicio\>‘+’

 F: \<Fin\> \<PCom\> otro1

 )

Análisis Léxico

\* /

3‘\*’ C –

‘‘\*’ +’,‘-’,

‘\*’ \| \| \<Inicio\>‘-’ \| \<Inicio\>

‘\*’ (otro1 es cualquier carácter distinto de )

# Comparadores

** Comparadores: \> \>= \< \<= == \<\>**

‘ \>’

0 7 F

##  En Autómatas Finitos la representación debería ser

## diferente:

‘ \>’ c 0 7 F

c: cualquier carácter

Análisis Léxico

‘ =‘

otro

‘ =‘ F1‘ \>’ 0 7

F2 otro

## Blancos, tabulaciones, saltos de línea

 Eliminar blancos, tabulaciones, saltos de línea

Blanco, tab, nl

0

 No se entregan al Analizador Sintáctico ¿Nunca?

Análisis Léxico

## Autómata para el Analizador Léxico

Análisis Léxico l

1

d

d l c – l – d 2 c – d d 3‘\*’ c –

‘‘\*’ Blanco, tab, nl /’

0 F 4‘\*’ c –‘‘\*’ /’

‘\*’ –‘/’ c –

5‘‘\*’ +’,‘-’,

‘\*’

‘ \>’,‘=‘

‘ \<’

c –‘\>’–‘=’ 6‘ \>’‘ =‘ 7 c –‘=’‘ c –‘=’ =‘

‘ =‘ 8

Análisis Léxico

# Matriz de Transición de Estados

|     |     |     |     |     |     |     |     |     |     | otro | BL     | $   |
|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|------|--------|-----|
|     | l   | d   | /   | \*  | +   | -   | =   | \<  | \>  |      | tab nl |     |
| 0   | 1   | 2   | 3   | F   | F   | F   | 8   | 6   | 7   | F    | 0      | F   |
| 1   | 1   | 1   | F   | F   | F   | F   | F   | F   | F   | F    | F      | F   |
| 2   | F   | 2   | F   | F   | F   | F   | F   | F   | F   | F    | F      | F   |
| 3   | F   | F   | F   | 4   | F   | F   | F   | F   | F   | F    | F      | F   |
| 4   | 4   | 4   | 4   | 5   | 4   | 4   | 4   | 4   | 4   | 4    | 4      | F   |
| 5   | 4   | 4   | 0   | 5   | 4   | 4   | 4   | 4   | 4   | 4    | 4      | F   |
| 6   | F   | F   | F   | F   | F   | F   | F   | F   | F   | F    | F      | F   |
| 7   | F   | F   | F   | F   | F   | F   | F   | F   | F   | F    | F      | F   |
| 8   | F   | F   | F   | F   | F   | F   | F   | F   | F   | F    | F      | F   |

Análisis Léxico

¿Qué significa llegar al estado final?

Análisis Léxico

# Analizador Léxico: Tokens

##  Identificadores (cadena que comienza con una letra y

## continúa con letras y/o dígitos)

##  Constantes enteras (secuencia de dígitos)

\* \* … /  Comentarios tipo C /  Operadores aritméticos + - \* /  Comparadores \> \>= \< \<= == \<\>  Palabras reservadas (secuencia de letras mayúsculas)

Análisis Léxico

¿Y las palabras reservadas?

**Autómata**

Análisis Léxico

# Palabras Reservadas

 Palabra reservada: Secuencia de letras mayúsculas

l l: letra mayúscula o minúscula d: dígito

l c – l – d 0 1 F

**Acción**

**Semántica**

d

 ¿Identificador o palabra reservada?

Análisis Léxico

# Acciones Semánticas

 Fragmentos de código en el lenguaje del compilador.  Se asocian a las transiciones o a las expresiones regulares que definen tokens.  Impiden el crecimiento del número de estados del autómata.

Análisis Léxico

# Identificadores y Palabras Reservadas

l

l c – l – d 0 1 F

**A.S. 1**

d

 Acción Semántica 1:  Devolver a la entrada el último carácter leído  Buscar en la TPR  Si está, devolver la Palabra Reservada  Si no está,  Buscar en la TS  Si está, devolver ID + PuntTS  Si no está,  Alta en la TS  Devolver ID + PuntTS

Análisis Léxico

Almacenamiento de las

palabras reservadas:  Tabla de palabras reservadas  Tabla de Símbolos

# Identificadores y Palabras Reservadas

l

l c – l – d 0 1 F

**A.S. 1**

d

 Acción Semántica 1:  Devolver a la entrada el último carácter leído  Buscar en la TS  Si está,  Si es PR, devolver la Palabra Reservada  Si no, Devolver ID + Punt TS  Si no está,  Alta en la TS  Devolver ID + Punt TS

Análisis Léxico

Almacenamiento de las

palabras reservadas:  Tabla de Símbolos

# Acciones Semánticas

l

**A.S. 3**

| l      | c – l – d |
|--------|-----------|
| 0      | 1 F       |
| A.S. 2 | A.S. 1    |

**A.S. 3**

d

 Acción Semántica 2:

 Inicializar string (se reserva la máxima longitud permitida para identificadores)  Agregar letra al string

 Acción Semántica 3:

 Agregar letra o dígito al string

Análisis Léxico

# Constantes - Acciones Semánticas

d

**A.S. 5**

| d      | c – d  |
|--------|--------|
| 0      | 2 F    |
| A.S. 4 | A.S. 6 |

 Acción Semántica 4:

 Inicializar string para la constante  Agregar dígito al string

 Acción Semántica 5:

 Agregar dígito al string

 Acción Semántica 6:

 Devolver a la entrada el último carácter leído

 Verificar rango de la constante  Alta en la TS

 Devolver CTE + Punt TS

Análisis Léxico

# Constantes

 Formas de reconocerlas

 Un solo tipo de token (CTE)  Diferentes tipos de tokens (CTEINT, CTEFL)

 Almacenamiento

 Tabla de Símbolos

 Tabla de Constantes y Literales

Análisis Léxico

# Constantes

 Si mis constantes enteras pueden tomar valores entre: - -- - 32768 y 32767:

# ¿Cómo reconozco constantes

negativas?

**Autómata**

Análisis Léxico

# Comparadores

‘‘ =‘ \>’

0 7 F

otro

 ¿Qué diferencia hay entre una y otra transición?

Análisis Léxico

‘ =‘

F 7 El carácter‘=‘

7 F El carácter‘otro‘n otro

• ¿Cómo se distingue una situación de la otra?

• Solución 1:

‘ \>’

0 7 F

• Solución 2: ACCIONES SEMÁNTICAS

• Solución para expresiones regulares: Definir la expresión

correspondiente a la cadena más larga primero. Análisis Léxico

se consume.

o se consume.

‘ =‘ 10 otro

otro

# Acciones semánticas

 ¿Cómo se asocian las acciones semánticas con las transiciones?

**Matriz de Transición de Estados**

(Contiene la “letra chica” del Análisis Léxico)

Análisis Léxico

# Matriz de Transición de Estados

l d / \* + - = \< \>

2 3 F F F 8 6 7 F 0 F 0 1 AS2 AS4

1 F F F F 1 1 AS3 AS3 AS1 AS1 AS1 AS1

2 F 2 F F F F F F F F F F

3 F F F 4 F F F F F F F F

4 4 4 4 5 4 4 4 4 4 4 4 F

5 4 4 0 5 4 4 4 4 4 4 4 F

6 F F F F F F F F F F F F

7 F F F F F F F F F F F F

8 F F F F F F F F F F F F

Análisis Léxico

otro BL tab nl $

F F F F F F AS1 AS1 AS1 AS1 AS1 AS1

# Programación

 Para cada carácter leído, mapear con las columnas de la matriz

 l  columna 0

 d  columna 1

‘  /’ columna 2

 …

 Si el carácter leído no corresponde a ninguna columna, debe “ ” informarse Carácter Inválido

Análisis Léxico

# Programación

 Definir matriz de estados

1,-  nuevo\_estado \[9\]\[12\] = {1,2,3,- 1,8,6,…}

Análisis Léxico

# Programación

 Definir matriz de acciones semánticas

 accion\_sem \[9\]\[12\] = {AS2, AS4, … }

Implementación: Matriz de punteros a función o equivalente

Análisis Léxico

# Programación

 Código para el Análisis Léxico:  Dados:

 estado: estado actual

 entrada: símbolo leído

 Acciones:

 accion\_sem\[estado\]\[entrada\] ejecuta la acción semántica  estado = nuevo\_estado\[estado\]\[entrada\] actualiza el estado actual

Análisis Léxico

# Errores Léxicos

 En el mapeo de los símbolos de entrada, se pueden detectar caracteres inválidos.

 Si en un estado del autómata, el carácter de entrada no coincide con ninguno de los arcos de salida, se trata de un error.

 Constante fuera de rango.  etc.

Análisis Léxico

# Errores Léxicos

## Acciones frente a un error

 Modo pánico: descartar caracteres hasta que aparezca un token conocido

 Ej. Constante de punto flotante mal construida. Consecuencias:

 Inserción / Borrado / Reemplazo  Borrar un carácter de la entrada. Ej.:  Insertar un carácter omitido. Ej.:  Sustituir un carácter por otro. Ej.:  Transponer dos caracteres adyacentes. Ej.:“ Consecuencias:

Análisis Léxico

 Se informa como Error

 El token se descarta

“ :==“

“ :”

“ :+”

= :”

 Se informa como Warning  El token (modificado) se entrega como token válido

# Fases de la Compilación

Errores

**Tira de**

**tokens**

Programa Análisis Análisis Salida Fuente Sintáctico Léxico

Tabla de Símbolos

Análisis Léxico

Generación de Código

# Fases de la Compilación

Errores

**Tira de**

**tokens**

Programa

**Análisis**

Análisis Salida Fuente

**Sintáctico**

Léxico

Tabla de Símbolos

Análisis Léxico

# \*

Generación de Código

¿Preguntas?

<image redacted: 210x290px, 210x290pt, ~72dpi, JPG, DEVICE_RGB, 32bpp>