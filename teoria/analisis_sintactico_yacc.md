## Diseño de Compiladores I

## Compiladores e Intérpretes

Análisis Sintáctico Yacc

## Análisis Sintáctico:

## Estrategias de Parsing

 Agrupa los tokens del programa fuente en frases gramaticales que el compilador usará en las siguientes etapas.  Obtiene una cadena de tokens del Analizador Léxico, y verifica que la cadena de tokens pueda generarse mediante la gramática del lenguaje.

 Parsing Ascendente (botton up)  Construye el árbol desde las hojas a la raíz

 Parsing Descendente (top down)  Construye el árbol desde la raíz a las hojas

Análisis Sintáctico

## Gramáticas LR

 Parsing Ascendente LR(0) y SLR(1) (LR(1) Simplificado)  Intentan predecir qué reducciones aplicar sin necesidad de ver toda la entrada

 Parsers shift/reduce

 Conflictos

 LR(0)  Falla  SLR(1)  Analiza un token más para decidir qué acción tomar  LALR(1) (Lookahead LR(1))  Usan reglas más precisas que SLR(1) para resolver conflictos.

** YACC usa esta técnica**

Análisis Sintáctico

# YACC

Yet Another Compiler Compiler

## YACC

 YACC provee una herramienta general para analizar estructuralmente una entrada.

 Requiere una especificación que incluye:  Un conjunto de reglas que describen los elementos de la entrada (Gramática)  Un código a ser invocado cuando una regla es reconocida  Un Analizador Léxico que se encargue de proveer tokens

Análisis Sintáctico

## Yacc

Analizador Léxico

yylex()

Especificación nombre.y YACC

YACC

Código a ejecutar para cada regla y yyparse() otras componentes del compilador

Compilador del lenguaje de desarrollo

COMPILADOR

Análisis Sintáctico

¿Cómo se usa YACC?  Escribir una especificación YACC conteniendo la gramática. (.y por convención)  Ejemplo: gramatica.y

 Escribir un Analizador Léxico. El método o función léxica debe ser:

** int yylex()**

Nota: yyparse invoca a yylex cada vez que necesita un token.

 Ejecutar YACC con la especificación como parámetro, para generar el código fuente del parser.

**Byacc para Java: yacc –**

**J gramatica.y**

La salida es un archivo llamado Parser.java conteniendo el método yyparse()

**Byacc para C: byacc gramatica.y**

La salida es un archivo llamado y\_tab.c conteniendo la función yyparse().

 Compilar y vincular los fuentes del Analizador Sintáctico, Analizador Léxico y todo otro código creado.

## Interacción entre yyparse y el Analizador

## Léxico

main()

retorna 0 si la entrada es válida, 1 si no lo es pedir próximo token yyparse() las acciones usan y retorna el número de actualizan la token o 0 si es EOF información de la TdeS

yylval pasa la referencia a la Tabla de Símbolos

Lee caracteres de la entrada yylex() entrada

Análisis Sintáctico

## Especificación YACC

## Formato:

## declaraciones

## %%

## reg

## las gramaticales

## %%

## código

Análisis Sintáctico

## Tokens

##  El Analizador Léxico detecta un token y retorna un

número de token al parser.

##  El número de token le permite al parser identificar el

token.

##  Los números de token son definidos por YACC cuando

procesa los tokens declarados en la especificación.

##  Cada carácter ASCII es definido como un token cuyo

número es su valor ASCII (0 a 256).  Los tokens definidos por el usuario comienzan en 257.

Análisis Sintáctico

## Tokens

 En las declaraciones de la especificación, se debe incluir la lista de tokens:

Análisis Sintáctico

<image redacted: 528x246px, 528x246pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Tokens

 Si, en la especificación para YACC, se incluye: %token ID CTE …

 En byacc para Java, en el archivo Parser.Java, junto con yyparse, se generan: public final static short ID=257; public final static short CTE=258;

…

Análisis Sintáctico

<image redacted: 342x132px, 342x131pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Tokens

 Si, en la especificación para YACC, se incluye: %token ID CTE …

 En yacc para C, se generan sentencias #define para definir los números de tokens:

| #define   | ID 257  |
|-----------|---------|
| #define … | CTE 258 |

 Estas definiciones son ubicadas en el archivo y\_tab.c, junto con la función yyparse, o pueden generarse en un archivo separado, llamado y\_tab.h.  Para ello, se debe ejecutar byacc –d gramatica.y

Análisis Sintáctico

## Tokens – yylval

##  Para pasar información del token al parser, se utiliza una

variable externa llamada yylval.

Análisis Sintáctico

## Especificación YACC

## Formato:

## declaraciones

## %%

## reg

## las gramaticales

## %%

## código

Análisis Sintáctico

## Especificación YACC

Gramática:

 Tokens, que son un conjunto de símbolos terminales  Elementos sintácticos, que son un conjunto de símbolos no terminales

 Reglas de producción  Una regla start que reduce todos los elementos de la gramática a una sola regla.

Análisis Sintáctico

## Especificación YACC

## Sección de Reglas

 Formato:

no terminal : definición {acción} ;

 Un (:) separa el lado izq. del derecho de la regla  Un (;) termina la regla. Por legibilidad, el (;) se ubica solo en una línea.  La definición consiste de cero o más nombres de terminales, tales como tokens o caracteres literales, y otros símbolos no terminales.  Cada definición puede tener una acción asociada, ubicada entre llaves.  La barra vertical (\|) permite definiciones alternativas dentro de una regla.  Los nombres de no terminales van en minúsculas y los nombres de los tokens en mayúsculas por convención.

Análisis Sintáctico

## Especificación YACC

## Sección de Reglas

 Un carácter literal se encierra entre apóstrofes (comillas simples).  La \\ tiene un significado especial, para secuencias escape: ewline ´\\n´n

eturn ´\\r´r

´´ ´ \\ comilla simple barra invertida ´\\\\´

tab ´\\t´

´\\b´ backspace orm feed ´\\f´f

´\\xxx´ carácter cuyo valor es xxx

Análisis Sintáctico

## Especificación YACC

## Sección de Reglas

Análisis Sintáctico

<image redacted: 365x422px, 364x422pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Especificación YACC

## Sección de Reglas

##  Cualquier regla de la gramática puede tener

## una acción asociada

##  Una acción es una o más sentencias en el lenguaje

de desarrollo.

Análisis Sintáctico

## Especificación YACC

## Sección de Reglas

Análisis Sintáctico

<image redacted: 510x393px, 510x393pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Especificación YACC

## Sección de Reglas

##  Cualquier regla de la gramática puede tener

## una acción asociada

##  Una acción es una o más sentencias en el lenguaje

de desarrollo.

##  Frecuentemente, la acción actúa sobre la

## información de los tokens contenida en la Tabla de

Símbolos.

Análisis Sintáctico

## Tokens – yylval

##  Para pasar información del token al parser, el Analizador

Léxico debe utilizar una variable externa llamada yylval.

Análisis Sintáctico

## Especificación YACC

## Sección de Reglas

##  El Analizador Léxico asigna a la variable externa

## yylval la referencia a la entrada en la Tabla de

## Símbolos donde se almacenó el valor léxico del

token.

##  YACC provee una notación que permite acceder

## a los valores retornados por yylex:

 La notación posicional, $n, permite acceder a la información del enésimo token en la definición de una regla.  La variable $$ permite asignar información al lado izquierdo de una regla.

Análisis Sintáctico

## Tokens – yylval

(YACC para Java)

**public class ParserVal**

**{**

 YACC genera la clase pública

**public int ival;**

ParserVal

**public double dval;**

**public String sval;**

**public Object obj;**

**public ParserVal(int val)**

**{**

**ival=val;**

**}public ParserVal(double val)**

**{**

**dval=val;**

**}public ParserVal(String val)**

**{**

**sval=val;**

**}public ParserVal(Object val)**

**{**

**=val;**

**obj**

**}**

**}//end class**

Análisis Sintáctico

## Tokens – yylval

(Yacc para Java)

 Desde el Analizador Léxico, se podrá asignar la variable yylval usando:

**yylval = new ParserVal(stringval); // lexema**

**yylval = new ParserVal(doubleval);**

**yylval = new ParserVal(integerval);**

...

**o:**

**yylval = new ParserVal(new myTypeOfObject());**

 Desde el Analizador Sintáctico, se podrá usar el valor asignado:

**acceder\_TS($1.sval)**

**$$.ival = $1.ival + $2.ival;**

**$$.dval = $1.dval - $2.dval;**

Análisis Sintáctico

## Tokens – yylval

(Yacc para C)

 En C, el tipo de yylval es int, por defecto.

##  Se puede cambiar el tipo de yylval o,

## definir una unión de tipos de datos múltiples para

## yylval:

% union { int entero;

\* char cadena; }

Análisis Sintáctico

## Tokens – yylval

(Yacc para C)

 Desde el Analizador Léxico, se podrá asignar la variable yylval usando:  Si se utiliza el tipo int, asignado por defecto a yylval:

** yylval = integerval;**

 Si se define una unión de tipos de datos múltiples:

** yylval.entero = integerval;**

**o**

** yylval.cadena = lexema;**

 Desde el Analizador Sintáctico, se podrá usar el valor asignado:  Si se utiliza el tipo int, asignado por defecto a yylval:

** $$ = $1;**

 Si se define una unión de tipos de datos múltiples:

** Acceder\_TS($1.cadena);**

**o**

** $$.entero = $1.entero + $2.entero;**

Análisis Sintáctico

## Acciones de las reglas

ID¨ ,  La primera acción es invocada cuando se reconoce¨ y retorna la información del token ID como el valor de la regla expr. Esta es la acción por defecto, y puede ser omitida opcionalmente.

 La segunda acción imprime la expresión completa. recuperar\_lexema accede a la tabla de símbolos para recuperar el lexema correspondiente al identificador reconocido en la primera regla.

 Si la entrada es:" , la acción de la se ( x )" gunda regla imprimirá " (x)"

Análisis Sintáctico

<image redacted: 706x80px, 706x79pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Especificación YACC

## Sección de Reglas

Acciones

 El parser generado por YACC guarda los valores de cada token en una variable de trabajo (yyval del mismo tipo que yylval).  Las variables de trabajo están disponibles para ser usadas dentro de las acciones de las reglas, y son rotuladas $1, $2, $3, etc.  La pseudo-variable $$ es el valor a ser retornado por esa invocación de la regla.  En el código real, son reemplazadas con las referencias Yacc correctas.  Se pueden ejecutar acciones después de cualquier elemento de un conjunto de tokens (no sólo al final), aunque no se recomienda.

Análisis Sintáctico

<image redacted: 224x45px, 224x45pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Especificación YACC

## Sección de Reglas

Token error

 Se puede usar en las reglas un símbolo llamado error.  No existe una regla que lo defina, ni se incluye en la declaración de tokens.  Es un token definido especialmente por Yacc que significa que cualquier entrada que no aparee ninguna de las otras reglas, apareará la que contiene error.

 Conviene utilizarlo con otro token, que sirve de carácter de sincronización. A partir del token erróneo, Yacc descartará tokens hasta encontrar ese carácter (por ej. un´;´).  Permite recuperarse de un error, y continuar con la compilación.  Se puede asociar una acción que permita informar que hubo un error, y toda la información que se desee agregar.

Análisis Sintáctico

## Especificación YACC

## Sección de Reglas

Token error

##  El analizador sintáctico podrá seguir compilando aún

cuando el código fuente contenga errores.

Informar que hubo error en asignación

Análisis Sintáctico

<image redacted: 402x229px, 401x229pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Especificación YACC

## Sección de Reglas

Gramática de error

 Se incluirán reglas que describan errores.

##  Para el analizador sintáctico, un código fuente con

esos errores será un código válido.  ¡Pero no para nuestro compilador!

Informar que hubo error en la expresión

Análisis Sintáctico

<image redacted: 437x152px, 437x151pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Especificación YACC

## Sección de Reglas: Reporte de errores

Análisis Sintáctico

<image redacted: 640x369px, 640x368pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Especificación YACC

## Sección de Reglas: Reporte de errores

Análisis Sintáctico

<image redacted: 694x384px, 694x384pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Especificación YACC

## Sección de declaraciones

 % union Declara múltiples tipos de datos para los atributos de los tokens (yylval) Ejemplo: % union { int entero;

\* char cadena; }  % token Declara los nombres de los tokens.

Si se usa union la sintaxis es:

% token \<elem. de la union que corresponde a este grupo de tokens \> lista de tokens

Análisis Sintáctico

## Especificación YACC

## Sección de declaraciones

|  % left                                                                          | Define operadores asociativos a izquierda                                                                                                   |
|-----------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
|  % right El orden de estas declaraciones Si se utiliza esta declaración, tokens. | Define operadores asociativos a derecha indica la precedencia (de menor a mayor) no es necesario declarar los operadores como               |
|  % nonassoc                                                                      | Define operadores no asociativos                                                                                                            |
|  % type a                                                                        | Declara el tipo de los no terminales, cuando se usaron diferentes tipos, y en las acciones asociadas las reglas, se hacen asignaciones a $$ |
|  % start                                                                         | Declara el símbolo de start. Por defecto es la primera regla                                                                                |
|  % prec                                                                          | Asigna precedencia a una regla Análisis Sintáctico                                                                                          |

## Especificación YACC

## Sección de declaraciones

##  Puede contener código en el lenguaje de desarrollo

## para declarar variables, tipos, etc. %{

## declaraciones en lenguaje de desarrollo

## %}

##  Cualquier cosa entre %{ y %} es copiada

directamente al archivo generado por YACC.

Análisis Sintáctico

<image redacted: 336x153px, 336x152pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Especificación YACC

## Sección de código

##  La sección de código es opcional, pero puede

## contener cualquier código en el lenguaje de

## desarrollo provisto por el usuario (incluso el código

del Analizador Léxico).

Análisis Sintáctico

## Especificación YACC

## Sección de código

Análisis Sintáctico

<image redacted: 330x375px, 329x374pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Especificación YACC

##  La mínima especificación YACC consiste en una

## sección de reglas precedidas por una declaración de

los tokens usados en la gramática.

Análisis Sintáctico

## Autómata de Pila

 Los autómatas finitos son suficientes para el Analizador Léxico.

 Los A.F. no son suficientes para un Analizador Sintáctico, porque son incapaces de recordar el estado anterior.  YACC genera un autómata de pila.

Análisis Sintáctico

## Autómata de Pila

 Tiene un número finito de estados, una función de transición, una entrada, y está equipado con una pila.  La función de transición trabaja sobre el estado actual, el elemento en el tope de la pila, y el token de entrada actual, produciendo un nuevo estado.  Permite a YACC reconocer gramáticas LALR (LookAhead Left Recursive) o LR(1).

Análisis Sintáctico

## Autómata de Pila

 El estado actual es siempre el del tope de la pila.  Inicialmente, la pila contiene sólo el estado 0 y no se ha leído ningún token.  El autómata tiene 4 acciones disponibles:

** shift,**

** reduce,**

** accept,**

 y error.

Análisis Sintáctico

## Autómata de Pila

 De acuerdo con el estado actual, el parser decide si necesita un token para decidir la próxima acción.  Si necesita un token y no lo tiene, llama a yylex para obtener el próximo token.  La acción puede provocar que se apilen y desapilen estados en la pila, y que el token sea procesado o no.

Análisis Sintáctico

## Autómata de Pila

 Una acción shift, se ejecuta a partir del token leído.  Ejemplo: En un estado, puede haber una acción: IF shift 34

 Si el token leído es IF, el estado 34 se convertirá en el estado actual (en el tope de la pila).  Se pide un nuevo token.

Análisis Sintáctico

## Autómata de Pila

 Una acción reduce evita que la pila crezca sin límites.  Se ejecuta cuando el parser ha visto el lado derecho de una regla y está listo para reemplazar el lado derecho por el izquierdo.  Las acciones reduce son asociadas con reglas de la gramática. La acción:

. reduce 18

se refiere a la regla 18 de la gramática.

 Hace que se desapilen tantos estados como símbolos tenga la regla del lado derecho.

Análisis Sintáctico

## Autómata de Pila

 Al desapilar estados, luego de un reduce, queda descubierto el estado en que el parser estaba al comenzar a procesar la regla.  Con este estado y el símbolo del lado izquierdo de la regla, se ejecuta un shift de un nuevo estado a la pila, pero sin leer un nuevo token.

 Esta acción se llama goto.  Ejemplo: A goto 20 A es el lado izquierdo de la regla después del reduce, y 20 será el nuevo estado actual.

Análisis Sintáctico

## Autómata de Pila

##  La acción accept indica que el parser ha visto la

## entrada completa y que ésta cumple con la

especificación.

## Esta acción aparece sólo cuando el token leído es la

marca de fin de archivo.

##  La acción error representa un lugar donde el parser

## no puede continuar el proceso de acuerdo con la

especificación.

Análisis Sintáctico

## Ejemplo: Especificación YACC

## %token A B C

## %%

| lista : | inicio ; | fin |
|---------|----------|-----|
| inicio  | : ;      | A B |
| fin     | :        | C   |

Análisis Sintáctico

## Ejemplo: Autómata

state 0 $accept : \_lista $end

A shift 3

. error

lista goto 1 inicio goto 2

state 1 $accept : lista\_$end

$end accept . error

state 2 lista : inicio\_fin

C shift 5

. error

fin goto 4

state 3 inicio : A\_B

B shift 6

. error

state 4 lista : inicio fin\_ (1)

. reduce 1

state 5 fin : C\_ (3)

. reduce 3

state 6 inicio : A B\_ (2)

. reduce 2

Análisis Sintáctico

## Autómata de Pila

 Para obtener el autómata, se debe ejecutar yacc con la opción –v

**yacc –**

| J –v gramatica.y     | (byacc para Java) |
|----------------------|-------------------|
| byacc –v gramatica.y | (byacc para C)    |

## y.output

Análisis Sintáctico

## Conflictos en gramáticas Yacc

 Para la regla: ´ ´ sent : IF cond sent (´ )´ ´ ´ cond sent ELSE sent (´ )´ \| IF

;

 Con la entrada:

IF ( C1 ) IF ( C2 ) S1 ELSE S2

 El parser puede reconocer:

| IF ( C1  | )    | o IF | ( C1 )                      |
|----------|------|------|-----------------------------|
| {        |      | {    |                             |
| IF (     | C2 ) |      | IF ( C2 )                   |
| S1       |      |      | S1                          |
| }        |      |      | ELSE                        |
| ELSE     |      |      | S2                          |
| S2       |      | }    |                             |
| (Reduce) |      |      | (Shift) Análisis Sintáctico |

## Conflictos en gramáticas Yacc

Análisis Sintáctico

<image redacted: 467x212px, 467x211pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

<image redacted: 488x82px, 487x82pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Conflictos en gramáticas Yacc

## y.output

Análisis Sintáctico

<image redacted: 380x401px, 380x401pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Conflictos en gramáticas Yacc

Análisis Sintáctico

<image redacted: 322x381px, 322x380pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

<image redacted: 495x83px, 495x83pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Conflictos en gramáticas Yacc

## y.output

Análisis Sintáctico

<image redacted: 452x251px, 451x251pt, ~72dpi, PNG, DEVICE_RGB, 32bpp>

## Conflictos en gramáticas Yacc

 Ante la regla ´ ´

\- expr : expr expr  La entrada:

expr – expr – expr Puede ser reconocida como:

(expr – expr) – expr (Asociatividad a izquierda)

**reduce**

O

expr – (expr – expr) (Asociatividad a derecha)

**shift**

Análisis Sintáctico

## Conflictos en gramáticas Yacc

#  Soluciones:

##  REESCRIBIR LA GRAMÁTICA

##  Modificar la precedencia / asociatividad de

## los operadores

##  Modificar la precedencia de las reglas

##  Reglas de desambiguación de la herramienta

Análisis Sintáctico

## Solución de conflictos:

## Asociatividad / Precedencia

´ ´ ´

\- ´+ %left

´ ´ ´\* %left /´

%%

…

´ ´= asig : ID expr ´ ´+ expr : expr expr ´ ´

\- expr \| expr ´ ´\* expr \| expr ´ /´ expr \| expr ´ ´ ´ ´\* - expr %prec \| \| ID

;

Análisis Sintáctico

## Conflictos en gramáticas Yacc

 Si ni el símbolo de entrada ni la regla tienen precedencia y asociatividad:

 En un conflicto shift-reduce, la acción por defecto es el shift

 En un conflicto reduce-reduce, el defecto es reducir por la primera regla (en la especificación Yacc)

Análisis Sintáctico

## Reglas de desambiguación

 Se consideran las precedencias y asociatividades para aquellos tokens y literales que las tengan.  Cuando hay un conflicto reduce-reduce o un conflicto shift-reduce y, ni el símbolo de entrada ni la regla tienen precedencia y asociatividad, entonces se usan las dos reglas de desambiguación descriptas anteriormente, y los conflictos son reportados.  Si hay un conflicto shift-reduce, y tanto la regla de la gramática como el carácter de entrada tienen precedencia y asociatividad asociadas con ellos, el conflicto se resuelve en favor de la acción (shift o reduce) asociada con la precedencia más alta. Si las precedencias son iguales, se usa la asociatividad.

 La asociatividad a izquierda implica reduce;  La asociatividad a derecha implica shift;  La no asociatividad implica error.

Análisis Sintáctico

## Conflictos en gramáticas Yacc

##  REESCRIBIR LA GRAMÁTICA

Análisis Sintáctico

¿Preguntas?

<image redacted: 210x290px, 210x290pt, ~72dpi, JPG, DEVICE_RGB, 32bpp>