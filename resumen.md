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


Chequeo de Tipos y Tema 34 (tos)
Para este hito la idea principal es validar que las operaciones y asignaciones se hagan entre tipos compatibles, y permitir la conversión explícita de flotante a entero corto mediante la función tos(). En nuestro compilador solo existen dos tipos primitivos: SHORTINT y SINGLEF.

Infraestructura:
1. En Terceto.java se agregó el atributo tipo (con getter y setter), y en GeneradorTerceto se agregó la sobrecarga crearTerceto(op, arg1, arg2, tipo). El toString() del terceto sigue imprimiendo igual (op, arg1, arg2) para respetar la salida exacta de la cátedra.
2. Para transportar el tipo hacia arriba en el árbol sintáctico usamos el campo obj de ParserVal. En cada reducción de factor, termino y expresion guardamos ahí el string del tipo ("SHORTINT" o "SINGLEF").
3. En la tabla de símbolos, nos aseguramos de que las constantes (CTE_SHORTINT, CTE_SINGLEF y las constantes negativas) tengan seteado su atributo TIPO. Los identificadores ya lo tenían guardado desde que se ejecutó declararVariable en las sentencias declarativas.

¿Cómo se propaga el tipo desde las hojas (factor)?
- IDENTIFICADOR: se busca la variable en la tabla con buscarIdentificador y se saca su atributo TIPO. Se setea pv.obj = tipo.
- CTE_SHORTINT y negativo shortint: se agrega TIPO="SHORTINT" en la tabla y pv.obj = "SHORTINT".
- CTE_SINGLEF y negativo singlef: se agrega TIPO="SINGLEF" en la tabla y pv.obj = "SINGLEF".
- Reglas unitarias (termino : factor, expresion : termino): como hacen $$ = $1, el ParserVal pasa hacia arriba intacto con su sval, ival y obj.

Casos que se presentan en las operaciones y asignaciones:

1. Operaciones binarias (+, -, *, /)
Se recupera tipo1 de $1.obj y tipo2 de $3.obj.
- Caso tipos iguales (SHORTINT con SHORTINT, o SINGLEF con SINGLEF): es válido. Se crea el terceto con la operación y el tipo resultante, y se sintetiza el nuevo terceto [i] con pv.obj = tipo.
- Caso tipos distintos (SHORTINT con SINGLEF o al revés): es inválido. La cátedra exige que los tipos coincidan. Se emite un error semántico avisando la incompatibilidad y que se requiere conversión explícita 'tos'. En $$ se asigna new ParserVal() (con sval y obj en null) para que las reglas de arriba no generen tercetos con operaciones rotas.
- Caso alguno es null: significa que el operando ya falló previamente más abajo (por ejemplo una variable no declarada). No se genera terceto ni se emite un nuevo error para evitar errores en cascada.

2. Conversión explícita tos(expresion) (Tema 34)
La función intrínseca tos convierte una expresión de punto flotante a entero corto.
- Si la expresión interna es SINGLEF: es válido. Se genera el terceto ( tos , op , - ) con tipo SHORTINT. Hacia arriba sintetiza el terceto [i] y pv.obj = "SHORTINT", por lo que a partir de ahí se comporta como un entero corto y puede sumarse o asignarse a un shortint.
- Si la expresión interna NO es SINGLEF (por ejemplo si le pasás un SHORTINT): se emite un error semántico avisando que tos requiere una expresión de tipo SINGLEF, y se devuelve new ParserVal().

3. Asignación (variable := expresion y Tema 17: variable = (expresion))
Se busca la variable receptora en la tabla de símbolos y se obtiene su tipoVar. Luego se mira el tipoExp de la expresión del lado derecho:
- Caso tipos iguales: la asignación es válida y se genera el terceto :=.
- Caso variable SHORTINT y expresión SINGLEF: da error semántico avisando que no se puede asignar SINGLEF a SHORTINT sin la conversión explícita tos.
- Caso variable SINGLEF y expresión SHORTINT: da error semántico avisando que son incompatibles. Hay que recordar que nuestro grupo tiene el Tema 34 (conversión explícita) y no el Tema 35 (conversión implícita), por lo que el compilador no convierte automáticamente de entero a flotante.
- Caso tipoExp es null o hubo error en la expresión: no se genera el terceto de asignación y no se emiten errores redundantes.

4. Comparaciones (condicion: expresion comparador expresion)
Se comprueba que ambos operandos tengan el mismo tipo. Si se intenta comparar un SHORTINT con un SINGLEF directamente sin usar tos, se emite error semántico de incompatibilidad de tipos en la comparación.
