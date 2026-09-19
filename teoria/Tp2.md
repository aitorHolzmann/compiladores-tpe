# Diseño de Compiladores I – Compiladores e Intérpretes

# Trabajo Práctico Nº 2 - 2026

Fecha de entrega: 18-09-2026

**OBJETIVO**

Construir un Parser (Analizador Sintáctico) que reciba los tokens detectados por el Analizador Léxico creado en el Trabajo Práctico Nº 1, y compruebe si el código fuente se corresponde con la sintaxis de un lenguaje con las siguientes características:

**SINTAXIS GENERAL:**

Nota: En la descripción de los temas generales y particulares puede aparecer el agregado o eliminación de algún símbolo / palabra reservada durante el Análisis Léxico.

**Programa:**

Pr ograma constituido por un nombre de programa, seguido de un bloque de sentencias declarativas, y a continuación, un bloque de sentencias ejecutables. El bloque de sentencias ejecutables estará delimitado por BEGIN y END.  Cada sentencia debe terminar con punto y coma";". El programa comenzará con un nombre, seguido por un conjunto de sentencias delimitado por‘begin‘y‘end’. Ejemplo de programa: programa\_1

… // Sentencias declarativas

**begin**

…. // Sentencias ejecutables

**end**

**Sentencias declarativas:**

 Sentencias de declaración de datos para los tipos de datos correspondientes a cada grupo según la consigna del Trabajo Práctico 1, con la siguiente sintaxis: \<tipo\> \<lista\_de\_variables\>; Donde \<tipo\> puede ser (Según tipos correspondientes a cada grupo): 

**shortint**



**ushortint**

**in**

**teger**



**utinteger**

**l**

**ongint**



**ulongint**



**singlef**



**doublef**

Las variables de la lista se separan con coma (“,”) In cluir declaración de funciones, con la siguiente sintaxis: \<tipo\> FUNCTION ID (\<parámetros\_formales\>) \<sentencias\_declarativas\_de\_la\_funcion\> // conjunto de sentencias declarativas

**BEGIN**

\<sentencias\_ejecutables\_de\_la\_funcion\> // conjunto de sentencias ejecutables

**END**

Donde:

 \< parámetros\_formales\> será una lista de identificadores precedidos por su tipo y separados por‘,’: Entonces, cada parámetro tendrá la siguiente estructura: \<tipo\> ID L as funciones deben tener, al menos, un parámetro.  \<sentencias\_declarativas\_de\_la\_funcion\> es un con junto de sentencias declarativas, incluyendo declaración de otras funciones.  \<sentencias\_e jecutables\_de\_la\_funcion\> es un conjunto de sentencias ejecutables, incluyendo sentencias de retorno, que pueden aparecer en cualquier lugar del cuerpo de sentencias ejecutables de la función, con la siguiente estructura: ret (\<retorno\>); \<retorno\> podrá ser cualquier expresión aritmética

Una función puede tener más de un retorno, y se admite la ausencia de retorno (En el Trabajo Práctico 3 se indicará qué retorna una función sin retorno explícito)

**Ejemplo:**

**Integer function f1 (integer x, doublef y)**

**integer w ;**

**integer function f11 (integer x1, integer x2)**

**begin**

x2 := x1;

**end;**

**begin**

x := f11(x,0$i);

…if (x \> 1$i) ret (x + 2$i);

**else**

**begin**

x := 2$i;

**r**

et (x\*2 $i);

**end**

**end\_if;**

**end;**

In corporar, como sentencia declarativa, la declaración de clases con la estructura que se muestra en los siguientes ejemplos:

**CLASS ca**

**BEGIN**

**INTE**

| GER a; INTEGER m(integer | // declaración de atributo pa) |
|--------------------------|--------------------------------|
| BEGIN …                  | // declaración de método       |

**CLASS cb**

| SINGLEF b; //     | declaración de atributo      |
|-------------------|------------------------------|
| SINGLEF a; //     | declaración de atributo      |
| SINGLEF n(INTEGER | pb) // declaración de método |
| BEGIN // … END    | declaración de método        |

…Incorporar, como sentencia declarativa, la declaración de objetos de una clase determinada: ca a1, a2; cb b1, b2, b3

// LÉXICO: Incorporar, a la lista de palabras reservadas, la palabra CLASS

**Sentencias ejecutables:**

A signaciones donde el lado izquierdo puede ser un identificador, y el lado derecho una expresión aritmética. Los operandos de las expresiones aritméticas pueden ser variables, constantes, invocaciones a función o método, referencias a atributos u otras expresiones aritméticas. No se debe permitir anidamiento de expresiones con paréntesis.

L as invocaciones a función tendrán el siguiente formato: ID(\<parametros\_reales\>) Cada parámetro real puede ser una expresión aritmética, variable o constante: Ejemplos de asignaciones:

a := b\*2 $i; z := f1(a, b) + j; w := a / f3(a+b,10$i) + f4(1.2);

’ L as referencias a atributos o métodos de un objeto se escribirán utilizando el‘.como se indica en los ejemplos: a1.a = 3\_i; b1.b = 1.2; b2.b = b1.a; x := b1.m(1$i); y := b1.n(2$i) + w;

(todos los chequeos semánticos para estas referencias, se efectuarán en las etapas siguientes

 Cláusula de selección (if). Cada rama de la selección será un bloque de sentencias. La estructura de la selección será, entonces: if (\<condicion\>) \<bloque\_de\_sent\_ejecutables\> else \<bloque\_de\_sent\_ejecutables\> end\_if ; El bloque para el else puede estar ausente. La condición será una comparación entre expresiones aritméticas, variables, invocaciones a función o constantes, y debe escribirse entre“(““)”. El bloque de sentencias ejecutables puede estar constituido por una sola sentencia, o un conjunto de sentencias ejecutables delimitadas por begin end.

 Sentencia de salida de mensajes por pantalla. El formato será pout(\<cadena\>);

o

pout(\<expresion\>); Ejemplos:

**pout(‘Hola mundo’); //Tema 9**

**pout(“Hola //Tema 10**

M undo”);

pout(a+ 5.0\*b); //Todos los temas

**TEMAS PARTICULARES**

Nota: La semántica de cada tema particular, se explicará y resolverá en las etapas 3 y 4 del trabajo práctico.

**Temas 11 a 14: Sentencias de Control Iterativas**

**T**

**ema 11: while repeat**

while ( \<condicion\> ) repeat \<bloque\_de\_sentencias\_ejecutables\> ; \<condición\> tendrá la misma definición que la condición de las sentencias de selección. \<bloque\_de\_sentencias\_ejecutables\> podrá contener una sentencia, o un grupo de sentencias ejecutables delimitadas por begin end. // LÉXICO: Incorporar, a la lista de palabras reservadas, las palabras WHILE y REPEAT.

**T**

**ema 12: repeat until**

Repeat \<bloque\_de\_sentencias\_ejecutables\> until ( \<condicion\> ); \<condición\> tendrá la misma definición que la condición de las sentencias de selección. \<bloque\_de\_sentencias\_ejecutables\> podrá contener una sentencia, o un grupo de sentencias ejecutables delimitadas por begin end. // LÉXICO: Incorporar, a la lista de palabras reservadas, las palabras REPEAT y UNTIL.

**T**

**ema 13: repeat while**

repeat \<bloque\_de\_sentencias\_ejecutables\> while ( \<condicion\> ); \<condición\> tendrá la misma definición que la condición de las sentencias de selección. \<bloque\_de\_sentencias\_ejecutables\> podrá contener una sentencia, o un grupo de sentencias ejecutables delimitadas por begin end. Incorporar, a la lista de palabras reservadas, las palabras REPEAT y while.

**T**

**ema 14:**

From ID := \<valor\_inicial\> to \<valor\_final\> by \<valor\_actualización\> repeat \< bloque\_de\_sentencias\_ejecutables \> ; \<valor\_inicial\>, \<valor\_final\> y \<valor\_actualización\> serán constantes \<bloque\_de\_sentencias\_ejecutables\> podrá contener una sentencia, o un grupo de sentencias ejecutables delimitadas por begin end.

// LÉXICO: Incorporar, a la lista de palabras reservadas, las palabras FROM, TO, BY y REPEAT.

**Temas 17 y 18: Asignaciones en expresiones**

**T**

**ema 17: Asignaciones de expresiones en expresiones**

Se debe permitir asignar expresiones a los términos de expresiones aritméticas. Estas nuevas asignaciones, utilizarán el símbolo‘=’,y la expresión a asignar debe ir delimitada por paréntesis.

Nota: Se usarán paréntesis solamente para delimitar la expresión para este tipo de asignaciones, pero sin anidamiento

Ejemplos válidos:

a:=b=

| (2$l);               | c := f(a=(3.0)); | a:=b=(c+d)        | (c/3$ul); | a:=b=(2$ui+d); |
|----------------------|------------------|-------------------|-----------|----------------|
| if(a= (3$ui)==3$ui)… | if(a=(b)!=3$ul)… | if(a=(b+c)==3.1)… |           | a:=b+c=(4$s);  |

a:=b:=

|          | (2$l);        | c := f(a:=(3.0)); | a:=b:=(c+d) | (c/3$ul);    | a=b=(2$ui+d);  |
|----------|---------------|-------------------|-------------|--------------|----------------|
|          |               | if(a=b!=3$ul)…    | if(a:=      |              |                |
| if(a:= … | (3$ui)==3$ui) |                   |             | (b+c)==3.1)… | a:=b+c:=(4$s); |

**T**

**ema 18: Asignaciones de identificadores o constantes en expresiones**

Se debe permitir asignar identificadores o constantes a los términos de expresiones aritméticas. Estas nuevas asignaciones, utilizarán el símbolo‘=’.. Ejemplos válidos: (sólo a modo aclarativo, se subraya la asignación que se llevará a cabo en cada caso)

| a:=b=2$l;                             | c := f(a=3.0);  | a:=b=c+d         |              |
|---------------------------------------|-----------------|------------------|--------------|
| if(a=3$ui==3$ui)… Ejemplos inválidos: | if(a=b!=3$ul)…  | if(a=b+c==3.1)…  | a:=b+c=4$s;  |
| a:=b:=2$l;                            | c := f(a:=3.0); | a:=b:=c+d        |              |
| if(a:=3$ui==3$ui)…                    | if(a:=b!=3$ul)… | if(a:=b+c==3.1)… | a:=b+c:=4$s; |

**Temas 19 a 21: Funciones con invocaciones y retornos con características especiales**

**T**

ema 19: Orden de evaluación de parámetros reales obligatorio (con asignaciones en expresiones) El programador debe, para cada invocación, indicar el orden en el cual quiere que se evalúe y se realice la asignación de los parámetros reales a los formales. Las invocaciones tendrán la siguiente estructura: ID ( \<lista\_parámetros\_reales\> ) \[\<lista\_constantes\>\] Donde:

\<lista\_parámetros\_reales\> será una lista de expresiones separadas por coma \<lista\_constantes\> será una lista de constantes separadas por coma Por ejemplo: a := f(x,y,z, …) \[1,2,3,...\] b := f2(a=1$i,b+3,5$i)\[2,3,1\]

// LÉXICO: Incorporar, a la lista de símbolos reconocidos, los corchetes‘\[‘y‘\]’.

**T**

ema 20: Orden de evaluación de parámetros reales opcional (con asignaciones en expresiones)

El programador puede, para cada invocación, indicar el orden en el cual quiere que se evalúe y se realice la asignación de los parámetros reales a los formales. Las invocaciones tendrán la siguiente estructura: ID ( \<lista\_parámetros\_reales\> ) \[\<lista\_constantes\>\] // \[\<lista\_constantes\>\] es opcional Donde:

\<lista\_parámetros\_reales\> será una lista de expresiones separadas por coma \<lista\_constantes\> será una lista de constantes separadas por coma Por ejemplo: a := f(x,y,z, …) \[1,2,3,...\]; b := f2(a=1$i,b+3,5$i)\[2,3,1\]; c := f2(a,b,c);

Incorporar, a la lista de símbolos reconocidos, los corchetes‘\[‘y‘\]’.

**T**

**ema 21: Funciones con retornos de tipos diferetnes**

Se debe permitir inferir el tipo retornado por funciones que se definan con la siguiente estructura: AUTO FUNCTION f(\<lista\_parámetros\_formales\>)

**Begin**

\<cuerpo\_funcion\>

**End;**

// LÉXICO: Incorporar, a la lista de palabras reservadas, la palabra auto. Una función declarada con esta estructura debe tener, al menos, un retorno. Esta condición debe ser chequeada sintácticamente.

La semántica de esta funcionalidad se explicará en las etapas siguientes.

**Temas 22 y 23: Declaraciones especiales**

**T**

**ema 22: Variables en tiempo de compilación**

Permitir, dentro de las sentencias declarativas, la declaración de variables que sólo existen en el programa durante la compilación. Para ello, se utilizará la siguiente estructura: comptime \<tipo\> \<lista\_variaables\>; // LÉXICO: Incorporar, a la lista de palabras reservadas, la palabra comptime. La semántica de esta funcionalidad se explicará en las etapas siguientes.

**T**

**ema 23: Enumeraciones**

Incorporar la posibilidad de definir tipos que establezcan, en su definición, una lista de valores de alguno de los tipos básicos del lenguaje. La definición del tipo tendrá la siguiente estructura: Typedef ID = \[ \<lista\_valores\> \]; Se debe permitir la declaración de variables del tipo definido por el usuario

Por ejemplo: Typedef pares\_1d = \[2$i,4$i,6$i,8$i\]; //define el tipo pares\_1d Pares\_1d a, b, c; // Declara las variables a, b y c del tipo pares\_1d // LÉXICO: Incorporar, a la lista de palabras reservadas, la palabra typedef, y a la lista de símbolos aceptados, los corchetes..

**Temas 24 al 27: Clases/Objetos**

**T**

**ema 24: Asignación por estructura en objetos con código**

En la declaración de las clases se debe incluir, luego del nombre de la clase, un código que permitirá asignar objetos de clases diferentes, siempre que posean el mismo código (Esto será chequeado en la etapa siguiente) Por lo tanto, la declaración de una clase, para este tema se realizará como se indica en el siguiente ejemplo:

CLASS ca c10

**BEGIN**

**INTE**

| GER a; INTEGER m(integer | // declaración de atributo pa) |
|--------------------------|--------------------------------|
| BEGIN …                  | // declaración de método       |

CLASS cb c11

| SINGLEF b; //     | declaración de atributo      |
|-------------------|------------------------------|
| SINGLEF a; //     | declaración de atributo      |
| SINGLEF n(INTEGER | pb) // declaración de método |
| BEGIN // … END    | declaración de método        |

**T**

**ema 25: Asignación por estructura en objetos sin código**

La declaración de las clases siguiendo la estructura indicada en la descripción de sentencias declarativas general: La semántica de este tema se presentará en la etapa siguiente.

**T**

**ema 26: import/export por atributos y métodos**

En la declaración de las clases se puede incluir, luego de la declaración de un atributo o de un método, una lista de clases a las que ese atributo o método se exporta. En la declaración de las clases, luego del nombre de la clase, se puede incluir una sentencia indicando de qué clase/s se importan atributos o métodos. Por lo tanto, la declaración de una clase, para este tema se realizará como se indica en el siguiente ejemplo:

**class A begin**

**in**

**teger x1 export to B, C;**

**singlef x2 export to C;**

**in**

**teger m1(integer p) begin**

**…end; export to B;**

**end;**

**class B import from A begin**

**in**

**teger x3; export to C;**

**in**

teger m2(A a1) begin a1.x1 ;=1 $i; a1.m1(1$i);

**end;**

**end;**

**class C import from A, B begin**

**in**

teger m3(A a1, B b2) begin a1.x2 := 2$i; b2.x3 := 3$i;

**end;**

**end;**

// LÉXICO: Agregar a la lista de palabras reservadas, las palabras import, from, export y to.

**T**

**ema 27: Clases Amigas**

En la declaración de una clase se puede incluir, precediendo a la declaración de un atributo o de un método, la palabra private, y agregar, en el cuerpo de la clase, una lista de clases y funciones amigas, como se indica en el siguiente ejemplo:.

**CLASS ca**

**BEGIN**

**Priv**

ate INTEGER a; // declaración de atributo privado

**Private INTEGER m(integer pa)**

| BEGIN … END;                                      | // declaración de método privado                         |
|---------------------------------------------------|----------------------------------------------------------|
| Friend cb; //                                     | declara a la clase cb como amiga                         |
| Friend f; // END; Esta funcionalidad se explicará | declara a la función f como amiga en la etapa siguiente. |

// LÉXICO: Agregar a la lista de palabras reservadas, las palabras friend y private;

**Temas 28 y 29: Acceso a atributos**

**T**

**ema 28: Acceso tradicional a atributos**

El acceso a un atributo de una clase se efectuará como se indicó en la declaración general para sentencias ejecutables. Es decir utilizando el punto. Por ejemplo:

ca a1;

**class ca begin**

a1.x = 3$i;

**integer x;**

a1.z = 3$i;

**integer z;**

**end;**

**T**

**ema 29: Acceso posicional a atributos**

El acceso a un atributo de una clase, para este tema, se efectuará con una notación posicional. Por ejemplo:

ca a1;

**class ca begin**

a1\[0$i\] = 3$i; // accede al atributo x

**integer x;**

a1\[1$i\] = 3$i; // accede al atributo z

**integer z;**

**end;**

Los índices pueden ser constantes o variables a1\[0$i\] o a1\[x\] // LÉXICO: Agregar a la lista de símbolos reconocidos, los corchetes.

**Temas 30 a 32: Herencia múltiple**

**Para los 3 temas:**

En la declaración de una clase se puede incluir, en el cuerpo de la clase, una o más clases de las cuales esta clase hereda, mediante una sentencia extend seguida de una lista de nombres de clase, como se indica en el ejemplo:

**class ca**

**begin**

**integer x;**

**integer z;**

extends cb, cc, cd; // la clase ca hereda de cb, cc, cd

**end;**

**Tema 30: Desambiguado Opción 1**

Se explicará en la etapa 3

**Tema 31: Desambiguado con prefijado**

En el acceso a un atributo de una clase, ya sea tradicional o posicional, se debe permitir indicar el nombre de la clase a la que pertenece el atributo o método heredado. Por ejemplo:

**Tema 32: Desambiguado Opción 3**

Se explicará en la etapa 3

**Temas 33 a 35: Conversiones**

T

**ema 33: Conversiones Explícitas de Entero a Punto Flotante**

Se debe incorporar en todo lugar donde pueda aparecer una expresión, la posibilidad de utilizar la siguiente sintaxis:

// LÉXICO: Incorporar a la lista de palabras reservadas, la palabra tosf o todf según corresponda.

T

**ema 34: Conversiones Explícitas de Punto Flotante a Entero**

Se debe incorporar en todo lugar donde pueda aparecer una expresión, la posibilidad de utilizar la siguiente sintaxis:

// LÉXICO: Incorporar a la lista de palabras reservadas, la palabra tos, tous, toi, toui, tol o toul según corresponda.

T

**ema 35: Conversiones Implícitas**

Se explicará y resolverá en trabajos prácticos 3 y 4.

ca a1; a1.x a1.cb.x

a1.cc.x

o

a1\[0$i\] a1.cb\[1$i\]

tosf (\<expresión\>) // para grupos que tienen asignado el tema 7 todf (\<expresión\>) // para grupos que tienen asignado el tema 8

tos (\<expresión\>) // para grupos que tienen asignado el tema 1 tous (\<expresión\>) // para grupos que tienen asignado el tema 2 toi (\<expresión\>) // para grupos que tienen asignado el tema 3 toui (\<expresión\>) // para grupos que tienen asignado el tema 4 tol (\<expresión\>) // para grupos que tienen asignado el tema 5 toul (\<expresión\>) // para grupos que tienen asignado el tema 6

**SALIDA DEL COMPILADOR**

El programa deberá leer un código fuente escrito en el lenguaje descripto, y deberá generar como salida:

**T**

**okens detectados por el Analizador Léxico**

**E**

**structuras sintácticas detectadas en el código fuente. Por ejemplo:**

Asignación Sentencia WHILE Sentencia IF etc. (Indicando nro. de línea para cada estructura)

**Err**

ores léxicos y sintácticos presentes en el código fuente, indicando: nro. de línea y descripción del

**error. Por ejemplo:**

Línea 24: Error: Constante de tipo integer fuera del rango permitido. Línea 43: Error: Falta paréntesis de cierre para la condición de la sentencia IF. Línea 52: Warning: El identificador identificadormuylargo fue truncado a: identificadormu



**Contenidos de la Tabla de símbolos**

**CONSIDERACIONES GENERALES**

a) Utilizar YACC u otra herramienta similar para construir el Parser. b) Adaptar el Analizador Léxico del Trabajo Práctico 1 para convertirlo en el método o función integer yylex() (o el nombre que el Parser generado requiera). Tener en cuenta que el léxico deberá devolver al parser, en cada invocación, un token. Para los identificadores, constantes y cadenas, deberá devolver además, la referencia a la entrada de la Tabla de Símbolos donde se ha registrado dicho símbolo, utilizando yylval para hacerlo. c) Para aquellos tipos de datos que permitan valores negativos (int, long, float, dfloat) durante el Análisis Sintáctico se deberán detectar constantes negativas, modificando la tabla de símbolos según corresponda. Será necesario volver a controlar el rango de las constantes, ya que un valor aceptado para una constante por el Analizador Léxico, que desconoce su signo, podría estar fuera de rango si la constante es positiva.  E jemplo: Las constantes de tipo integer pueden tomar valores desde –32768 a 32767. El Léxico aceptará la constante 32768 como válida, pero si se trata de una constante positiva, estará fuera de rango. d) Cuando se detecte un error, la compilación debe continuar. e) Conflictos: Eliminar TODOS LOS CONFLICTOS SHIFT-REDUCE Y REDUCE-REDUCE que se presenten al generar el Parser.

**FORMA DE ENTREGA**

Se deberá presentar: a) Código fuente completo y ejecutable, incluyendo librerías del lenguaje si fuera necesario para la ejecución b) Informe  Contenidos indicados en el enunciado del Trabajo Práctico 1 D escripción del proceso de desarrollo del Analizador Sintáctico: problemas surgidos (y soluciones adoptadas) en el proceso de construcción de la gramática, manejo de errores, solución de conflictos shift reduce y reduce-reduce, etc. Li sta de no terminales usados en la gramática con una breve descripción para cada uno. Li sta de errores léxicos y sintácticos considerados por el compilador.  Conclusiones. c) Casos de prueba que contemplen todas las estructuras válidas del lenguaje. Incluir casos con errores sintácticos.