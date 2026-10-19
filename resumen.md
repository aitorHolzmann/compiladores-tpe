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

Tenemos que controlar las ramificaciones del codigo. Por eso cada instruccion que genera una rama fue separada en su propia regla semantica. 

El primer caso es el del IF. En nuestra implementacion las instrucciones de la rama verdadera **true** se generan a continuacion de la condicion, entonces debemos agregar en el medio un salto en caso de falso. Cuando detectamos el encabezado se genera un terceto BF con la condicion y un espacio vacio que se completa mas adelante. Ademas el indice del terceto BF se guarda en una pila del **parser** que va a ser utilizada para conocer el anidamiento de las sentencias condicionales y poder actualizar los saltos. 

Entonces si es un **if simple sin else** cuando lleguemos al **end if** significa que se generaron todos los tercetos del bloque condicional, entonces la ejecucion debe continuar a partir del proximo terceto. En ese momento podemos completar el campo vacio del terceto BF, para eso debemos tomar el tope de la pila que en este caso sera el terceto BF y completar el campo vacio con el indice del proximo terceto. 

Ahora si es un **IF ELSE** cambia un poco. Todavia debemos agregar el terceto BF porque al igual que antes los tercetos de la rama **true** se van a generar a continuacion de la condicion. Pero la diferencia está en que ahora el salto no debe llegar al final del **end if** sino al bloque **else**. La regla **inicio_else** detecta la palabra reservada **else** y tiene 2 responsabilidades. Primero tenemos que pensar que si venimos por la rama **true** debemos introducir un salto para no ejecutar el bloque falso. Entonces el ultimo terceto del bloque verdadero va a ser un nuevo terceto **BI** que ademas se agrega a la pila, porque es un salto incondicional al final del **end_if** cuya posición no conocemos todavía. Pero ademas como encontramos la posicion del **else** podemos actualizar el terceto **BF** para que haga un salto al primer terceto del bloque falso. Luego de generar todos los tercetos del bloque falso, vamos a llegar al **end_if**, en ese momento podemos tomar el terceto **bi** de la pila y actualizar su salto. 

La ultima sentencia de control es **repeat until** que itera mientras la condicion sea falsa. Este caso es mas sencillo porque no tenemos que hacer un salto a futuro sino hacia atras. Primero se lee el repeat y todo el bloque de instrucciones que se deben ejecutar. Entonces cuando leemos la palabra **repeat** debemos guardar en la pila ese inicio del primer terceto del bloque de instrucciones. Cuando llegemos al final y leamos **until** generamos un terceto **bf** con la condicion, y el salto al inicio del bloque de iteracion.