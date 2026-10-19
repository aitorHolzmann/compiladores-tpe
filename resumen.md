Name Mangling
Se modificó el hashmap para que sea linkedhashmap. Antes las entradas se insertan en diferentes posiciones en cada ejecución. Ahora se insertan siempre igual, entonces es más fácil de reproducir los tests.

La variable ambitoActual vive en Parser, inicia como :main
entrarAmbito(String nombre) le concatena ambitoActual + “:” + nombre
salirAmbito() recorta el string, desde la pos 0 hasta los ultimos : sin incluir. Hay que tener en cuenta que a veces falta un end o un ; y es super importante que en esos casos de igual forma se salga del ambito.

TablaSimbolos.declararVariable(String nombre, String tipo, String uso, String ambito, int linea)

La idea es registrar en la tabla de símbolos el atributo mangling con el valor nombre + ambito. El funcionamiento consiste en primero buscar si ya fue declarada en el mismo ámbito. Tenemos que recorrer todas las entradas y preguntar si existe una que ya tenga el atributo mangling con nombre + ámbito, si es así damos error de redeclaración. 

Si no lo encuentra, entonces estamos en posición de actualizar la tabla de símbolos. Pero se vuelven a presentar dos casos. Recordemos que el léxico ejecuta la AS3 cuando cierra un identificador. Si el identificador es pepe lo va a insertar una vez, y si vuelve a encontrar otro no lo vuelve a insertar, eso en lo que respecta a la as3. Pero luego la gramática reduce la regla y tiene que declarar la variable. Entonces se le presentan dos casos a continuación: 
	
	SHORTINT a;

AUTO FUNCTION foo()
BEGIN
    SHORTINT a;
    RET 1$s;
END;

La primera a es insertada por la as3. Luego se reduce la regla y se llama a declararVariable. Este método va a buscar en la tabla de símbolos y va a encontrar la entrada para a, y mangling va a ser null, entonces lo actualiza y le agrega el ámbito actual junto con el tipo(programa, shorting, singlef, clase, auto) y uso(nombre_programa, variable, funcion, parametro, nombre_clase, atributo, metodo)

Después llega la segunda a. La as3 no va a volver a insertar, sino que devuelve la misma a. Entonces se reduce la regla y se llama a declararVariable. Vamos a volver a encontrar la entrada para a pero el mangling no va a ser null. Entonces en ese caso debemos insertar una nueva entrada en la tabla junto con los atributos.

Se llama cuando:
nombre_programa
declaracion_variables, atributo_clase: como el no terminal lista_variables o lista_identificadores tiene un objeto arraylist con los lexemas, cuando se reduce la regla y conocemos el tipo de la lista de variables, la recorremos y a cada una le agregamos sus atributos.
encabezado_funcion, inicio_auto_funcion, encabezado_clase, encabezado_metodo y encabezado_metodo_auto: ademas de actualizar la tabla de simbolos, debemos actualizar el ambitoActual
parametro_formal: antes se redujo la regla para encabezado funcion, entonces ya sabemos su ambito que es adentro de la funcion
declaracion_comptime: en este caso tiene los mismos atributos que una variable, y ademas el atributo comptime. Entonces primero declararVariable y ademas le agregamos el atributo comptime=true
Uso
Es en las partes declarativas de la gramatica que se llama a declararVariable, y el proposito es saber si es posible usar dichas variables en las partes de ejecucion. Dicha comprobacion la hace TablaSimbolos.buscarIdentificador(String nombre, String ambito). El metodo recibe el nombre/lexema de la variable y el ambitoActual. Entonces itera la tabla de simbolos buscando una entrada cuyo mangling coincida con nombre + ambitoActual. Si no encuentra ninguna coincidencia, entonces descarta el ultimo sufijo de ambitoActual y vuelve a empezar la busqueda. Si ambitoActual queda null entonces la variable no está declarada o al alcance. Cuando se encuentra la variable en la tabla de simbolos, se genera el terceto usando el atributo mangilng

Se llama cuando:
asignacion
facto
unica


Chequeo de Tipos

La semantica tiene que hacer las verificaciones de tipo. Cuando se hacen asignaciones, operaciones, y en nuestro caso tambien cuando se hace la conversion de SINGLEF a SHORTINT. El tipo se guarda tanto en la tabla de simbolos con las acciones semanticas, como desde la gramatica como informacion del terceto. 

En la regla **factor** y **numero negativo** guardamos el tipo en **pv.obj**. En el caso de las variables sabemos el tipo porque en la declaracion lo guardamos en la tabla de simbolos. Pero luego cuando hacemos operaciones y generamos tercetos, debemos verificar y conservar el tipo de la operacion, almacenandolo tambien en el pv.obj. 

Las asignaciones y comparaciones verifican que se realicen entre elementos del mismo tipo. La funcion TOS() verifica que reciba un SINGLEF.


Control de Flujo y Backpatching

Para las estructuras de control (IF, IF-ELSE y REPEAT UNTIL) se implementó la técnica de backpatching usando una pila `Stack<Integer> pilaSaltos`.

En `condicion` se emite el terceto con el operador de comparación (`>`, `<`, `<=`, etc.) siempre que ambos operandos tengan el mismo tipo.

IF simple:
1. `encabezado_if : IF '(' condicion ')'`: crea un terceto incompleto `( BF , [cond] , ? )` y apila su índice en `pilaSaltos`.
2. Al llegar a `END_IF ';'`: se desapila el índice del `BF` y se completa su destino con el próximo terceto a generar (`GeneradorTerceto.getProximoIndice()`).

IF-ELSE:
1. `encabezado_if`: crea el `BF` y lo apila.
2. `inicio_else : ELSE`: al terminar el bloque THEN, se emite un salto incondicional `( BI , ? , - )` para saltear el ELSE. Se desapila el `BF` anterior y se completa su destino con la primera instrucción del ELSE. Luego se apila el índice del `BI`.
3. Al llegar a `END_IF ';'`: se desapila el `BI` y se completa su destino con `GeneradorTerceto.getProximoIndice()`.

REPEAT UNTIL (Tema 12):
1. `inicio_repeat : REPEAT`: apila la dirección de inicio del cuerpo del ciclo (`getProximoIndice()`).
2. Al llegar a `UNTIL condicion_iteracion ';'`: como la especificación de la cátedra dice que itera hasta que la condición sea verdadera, debe volver a saltar al inicio mientras sea falsa. Se desapila la dirección de inicio y se emite `( BF , [cond] , [dir_inicio] )`. No requiere backpatching diferido porque el salto es hacia atrás y la dirección inicial ya es conocida.
