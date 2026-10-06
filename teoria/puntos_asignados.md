Aquí tienes un mini informe con la información extraída y detallada para cada uno de los temas asignados a tu grupo (Grupo 20):

### **Tema 1: Enteros cortos (8 bits)**

* Corresponde a constantes enteras con valores en el rango de –27 a 27 – 1.


* Según la especificación, estas constantes llevarán un sufijo particular.


* En las sentencias declarativas de datos, este tipo se identifica con la palabra reservada `shortint`.



### **Tema 7: Punto Flotante de 32 bits**

* Corresponde a números reales con signo y parte exponencial.


* La parte exponencial puede estar ausente. Si está presente, el exponente debe comenzar con la letra "s".


* El signo del exponente puede estar ausente, en cuyo caso se asume que es positivo.


* La parte entera puede omitirse, pero el punto (`.`) y la parte decimal son obligatorios. Ejemplos válidos son: `1.0`, `.6`, `-1.2`, `3.0s–5`, `.2s34`.


* El rango a considerar es `1.17549435s-38 < x < 3.40282347s+38`, sus correspondientes valores negativos, o `0.0`.


* En las sentencias declarativas de datos, este tipo se identifica con la palabra `singlef`.



### **Tema 10: Cadenas multilínea**

* Son cadenas de caracteres delimitadas por comillas dobles (`"`) que pueden ocupar más de una línea.


* Al guardarse en la Tabla de Símbolos, se debe almacenar la cadena sin los saltos de línea.


* Pueden usarse en la sentencia de salida de mensajes, por ejemplo: `pout( "Hola Mundo");`.



### **Tema 12: Sentencias de Control Iterativas (repeat until)**

* La sintaxis requerida es: `Repeat <bloque_de_sentencias_ejecutables> until ( <condicion> );`.


* La `<condición>` posee la misma definición que la condición utilizada en las sentencias de selección.


* El `<bloque_de_sentencias_ejecutables>` puede contener una sola sentencia o un grupo de ellas delimitadas por `begin end`.


* **Modificación léxica:** Se deben incorporar las palabras `REPEAT` y `UNTIL` a la lista de palabras reservadas.



### **Tema 15: Comentarios de 1 línea**

* Son comentarios que deben comenzar con los caracteres `//` y finalizan con el fin de línea.



### **Tema 17: Asignaciones de expresiones en expresiones**

* Se debe permitir asignar expresiones a los términos de otras expresiones aritméticas.


* Estas nuevas asignaciones utilizarán el símbolo `=`.


* La expresión que se desea asignar debe ir obligatoriamente delimitada por paréntesis, por ejemplo: `a:=b=(2$l);` o `c := f(a=(3.0));`.


* Los paréntesis solo se utilizarán para delimitar la expresión de la asignación y **no se permite anidamiento**.



### **Tema 21: Funciones con retornos de tipos diferentes**

* El compilador debe permitir inferir el tipo retornado por funciones que se definan bajo una estructura especial.


* La sintaxis requerida es: `AUTO FUNCTION f(<lista_parámetros_formales>) Begin <cuerpo_funcion> End;`.


* Una función declarada de esta manera debe tener al menos un retorno, condición que debe validarse sintácticamente.


* **Modificación léxica:** Se debe incorporar la palabra `AUTO` a la lista de palabras reservadas.



### **Tema 22: Variables en tiempo de compilación**

* Permite declarar, dentro de las sentencias declarativas, variables que solo existirán en el programa durante la compilación.


* La estructura a utilizar es: `comptime <tipo> <lista_variaables>;`.


* **Modificación léxica:** Incorporar la palabra `comptime` a la lista de palabras reservadas.



### **Tema 26: Import/export por atributos y métodos**

* En la declaración de clases, luego de declarar un atributo o método, se puede añadir una lista de clases a las que este se exporta usando la sintaxis `export to` (ej. `integer x1 export to B, C;`).


* Asimismo, después del nombre de la clase, se puede incluir una sentencia para indicar desde qué clase(s) se importan atributos o métodos usando la sintaxis `import from` (ej. `class B import from A begin`).


* **Modificación léxica:** Se deben agregar las palabras `import`, `from`, `export` y `to` a la lista de palabras reservadas.



### **Tema 29: Acceso posicional a atributos**

* El acceso a un atributo de una clase se efectuará utilizando una notación posicional mediante índices.


* Ejemplo de uso: `a1[0$i] = 3$i;` para acceder al primer atributo, o `a1[1$i] = 3$i;` para el segundo.


* Los índices de las posiciones pueden ser constantes o variables (ej. `a1[x]`).


* **Modificación léxica:** Se deben agregar los corchetes (`[` y `]`) a la lista de símbolos reconocidos.



### **Tema 30: Herencia múltiple (Desambiguado Opción 1)**

* **Condición General (Temas 30 a 32):** Se debe permitir que una clase herede de una o más clases. Esto se logra agregando en el cuerpo de la clase la sentencia `extends` seguida de una lista de nombres de clase (ej. `extends cb, cc, cd;`).


* **Condición Específica (Tema 30):** El mecanismo particular de "Desambiguado Opción 1" será explicado en la etapa 3 del trabajo práctico.



### **Tema 34: Conversiones Explícitas de Punto Flotante a Entero**

* Se debe incorporar la posibilidad de hacer conversiones explícitas en cualquier lugar donde pueda aparecer una expresión.


* Dado que tu grupo también tiene asignado el **Tema 1 (Enteros cortos)**, la sintaxis específica que deben implementar para la conversión es: `tos (<expresión>)`.


* **Modificación léxica:** Se debe incorporar la palabra reservada `tos` a la lista.
