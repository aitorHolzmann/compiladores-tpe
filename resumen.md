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
