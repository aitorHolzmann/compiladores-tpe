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
factor
unica


# Chequeo de Tipos

La semantica tiene que hacer las verificaciones de tipo. Cuando se hacen asignaciones, operaciones, y en nuestro caso tambien cuando se hace la conversion de SINGLEF a SHORTINT. El tipo se guarda tanto en la tabla de simbolos con las acciones semanticas, como desde la gramatica como informacion del terceto. 

En la regla **factor** y **numero negativo** guardamos el tipo en **pv.obj**. En el caso de las variables sabemos el tipo porque en la declaracion lo guardamos en la tabla de simbolos. Pero luego cuando hacemos operaciones y generamos tercetos, debemos verificar y conservar el tipo de la operacion, almacenandolo tambien en el pv.obj. 

Las asignaciones y comparaciones verifican que se realicen entre elementos del mismo tipo. La funcion TOS() verifica que reciba un SINGLEF.


# Control de Flujo y Backpatching

Para las estructuras de control (IF, IF-ELSE y REPEAT UNTIL) se implementó la técnica de backpatching usando una pila `Stack<Integer> pilaSaltos`.

En `condicion` se emite el terceto con el operador de comparación (`>`, `<`, `<=`, etc.) siempre que ambos operandos tengan el mismo tipo.

Tenemos que controlar las ramificaciones del codigo. Por eso cada instruccion que genera una rama fue separada en su propia regla semantica. 

El primer caso es el del IF. En nuestra implementacion las instrucciones de la rama verdadera **true** se generan a continuacion de la condicion, entonces debemos agregar en el medio un salto en caso de falso. Cuando detectamos el encabezado se genera un terceto BF con la condicion y un espacio vacio que se completa mas adelante. Ademas el indice del terceto BF se guarda en una pila del **parser** que va a ser utilizada para conocer el anidamiento de las sentencias condicionales y poder actualizar los saltos. 

Entonces si es un **if simple sin else** cuando lleguemos al **end if** significa que se generaron todos los tercetos del bloque condicional, entonces la ejecucion debe continuar a partir del proximo terceto. En ese momento podemos completar el campo vacio del terceto BF, para eso debemos tomar el tope de la pila que en este caso sera el terceto BF y completar el campo vacio con el indice del proximo terceto. 

Ahora si es un **IF ELSE** cambia un poco. Todavia debemos agregar el terceto BF porque al igual que antes los tercetos de la rama **true** se van a generar a continuacion de la condicion. Pero la diferencia está en que ahora el salto no debe llegar al final del **end if** sino al bloque **else**. La regla **inicio_else** detecta la palabra reservada **else** y tiene 2 responsabilidades. Primero tenemos que pensar que si venimos por la rama **true** debemos introducir un salto para no ejecutar el bloque falso. Entonces el ultimo terceto del bloque verdadero va a ser un nuevo terceto **BI** que ademas se agrega a la pila, porque es un salto incondicional al final del **end_if** cuya posición no conocemos todavía. Pero ademas como encontramos la posicion del **else** podemos actualizar el terceto **BF** para que haga un salto al primer terceto del bloque falso. Luego de generar todos los tercetos del bloque falso, vamos a llegar al **end_if**, en ese momento podemos tomar el terceto **bi** de la pila y actualizar su salto. 

La ultima sentencia de control es **repeat until** que itera mientras la condicion sea falsa. Este caso es mas sencillo porque no tenemos que hacer un salto a futuro sino hacia atras. Primero se lee el repeat y todo el bloque de instrucciones que se deben ejecutar. Entonces cuando leemos la palabra **repeat** debemos guardar en la pila ese inicio del primer terceto del bloque de instrucciones. Cuando llegemos al final y leamos **until** generamos un terceto **bf** con la condicion, y el salto al inicio del bloque de iteracion.

# Funciones y parametros

Queremos controlar las declaraciones e invocaciones. Para eso debemos implementar el guardado de la firma de la funcion, es decir tipo de retorno, cantidad y tipo de parametros, para luego poder verificar las invocaciones.

Empecemos con las funciones. Cuando detectamos el encabezado de una funcion, ya en ese momento estamos guardando el uso (funcion) y el tipo de retorno (puede ser auto y se define después), y ademas generamos el terceto que indica el inicio de una funcion. Luego de detectar el encabezado de la funcion, se reducen las reglas de parametro_formal. Cada parametro formal se agrega como atributo de la funcion en la tabla de simbolos llamando a **TablaSimbolos.agregarParametroAFuncion(String funcionMangled, String tipoParam, String nombreParamMangled)**. Si dentro de la funcion encontramos un RET vamos a generar un terceto con el valor y el tipo, pero antes debemos controlar que coincida con el tipo de la funcion. Puede ser el caso de una funcion AUTO, si es así debemos actualizar el atributo tipo en la tabla de simbolos, pero como ademas permite varios retornos tambien hay que contemplar que ese tipo puede crecer a uno mas abarcativo. Por utlimo cuando reconocemos la declaracion completa de la funcion hay que controlar que efectivamente haya un retorno y emitir el tereceto para indicar el fin de la funcion. Si es el caso de una funcion normal entonces se contempla que no haya sentencia de ret explicita en el codigo, en ese caso la generamos nosotros con un valor por defecto (0$s si es SHORTINT y 0.0 si es SINGLEF). Si es una funcion AUTO entonces la instruccion RET es obligatoria, si no está avisa con error.

Dijimos que en la tabla de simbolos los parametros formales se guardan como atributos de la funcion, vamos a explicarlo mas a detalle. El caso mas sencillo es una funcion que no declara parametros, entonces no hace falta registrar nada. Pero si necesitamos registrar parametros entonces llamamos a **agregarParametroAFuncion**. Para registrar un parametro insertamos dos atributos que guardan su nombre y tipo. El nombre de estos atributos está formado por el numero del parametro que fue insertado. Es decir para guardar el primero vamos a insertar **PARAM_NOMBRE_1** y **PARAM_TIPO_1**. Para llevar la cuenta de cuantos parámetros hay, la funcion tiene otro atributo que es **CANT_PARAMS** que se actualiza con cada llamado al metodo. Esta implementacion favorece la busqueda posicional de atributos, que fue un tema particular asignado a nuestro grupo. 

Ya hablamos de como se declaran las funciones. Ahora toca hablar de como se invocan. El caso mas simple es invocar a una funcion que no necesite parametros. La regla primero valida que efectivamente estemos intentando invocar a una funcion y que ademas esté al alcance, despues si se verifica que efectivamente la declaracion de la funcion no tiene parámetros entonces se genera un terceto con la llamada y el tipo de retorno, ademas al reducir la regla se guarda el tipo de retorno para que las operaciones puedan verificar la comptabilidad de tipos de las expresiones.

Para el caso de las funciones que si necesitan parametros hay que trabajar un poco mas. La regla **lista_expresiones** guarda una lista de objetos del parser, cada uno tiene el valor en **svalue** y el tipo en el **obj**. Al igual que antes validamos que esté al alcanse y que sea una funcion, si se cumple entonces obtengo la cantidad de parametros esperada de la funcion con la longitud de la lista de expresiones. Si la longitud es la misma, entonces tengo que iterar y comparar posicion a posicion si el tipo esperado es el mismo que el de la expresion real, cuando la verificacion se cumple genero un terceto para asignar al parametro formal el valor y tipo del real. Cuando termine de recorrer toda la lista sin errores, puedo emitir el terceto con la llamada a la funcion 

# Comptime

Para las variables **COMPTIME** necesitabamos que en lugar de  generar tercetos sus valores se guarden en tiempo de compilacion. Entonces hay que distinguir dos casos, cuando es un **identificador** o cuando es una operacion entre variables **comptime**. Para el primer caso guardamos el valor en la **TS** como un atributo **valor_comptime**. El segundo caso se refiere al momento en que la gramatica reduce una expresion/termino/factor, entonces tenemos que guardar el resultado dentro del **parser val** como **sval**. Para saber si una expresion es **comptime** debemos preguntar que **no** contenga **[ ]** (correspondiente a un terceto) ni **:**(una variable)

Existen situaciones donde es necesario dar error en tiempo de compilacion. El primero ocurre cuando se quiere leer el valor de una variable **comptime** que todavia no fue inicializada. Tambien hay que dar error cuando se quiere operar una **comptime** con otra avariable cuyo valor se conoce en tiempo de ejecucion. Es valido operar con numeros explicitos y otras **comptime**

# Clases Herencia y Visibilidad


En la parte de POO tenemos tres temas juntos:
- **Tema 30 (Herencia multiple - Desambiguado Opcion 1):** Cuando una clase declara `EXTENDS c1, c2;`, guardamos en la tabla de símbolos la lista ordenada de padres (`HEREDA_0`, `HEREDA_1`, etc.). Al buscar un método o atributo (`buscarMiembroEnJerarquia`), primero busca en la propia clase (`:main:clase`), y si no está, busca en los padres en el orden estricto de izquierda a derecha. La primera coincidencia encontrada es la que se usa.
- **Tema 26 (Import / Export):** Una clase puede indicar `IMPORT FROM c1, c2;` y sus atributos o métodos pueden llevar `EXPORT TO c3, c4;`. Cuando un método de una clase `B` accede a un miembro de `A`, validamos que `B` tenga en sus imports a `A`, y que el miembro de `A` tenga en su lista de export a `B`. Si alguna de las dos cosas no se cumple, damos error semántico.
- **Tema 29 (Acceso posicional):** En lugar de usar punto para acceder a los atributos, se accede con corchetes `obj[0$s]` o `obj[idx]`. En la tabla de símbolos, cada atributo declarado en la clase se registra con su posición (`ATR_NOMBRE_0`, `ATR_TIPO_0`, etc.). Si se accede con constante numérica, verificamos en tiempo de compilación que el índice sea válido (entre 0 y la cantidad de atributos menos uno). Si se accede con variable, validamos que dicha variable sea de tipo `SHORTINT`. En los tercetos generamos la asignación o lectura referenciando al objeto indexado.

Vamos a explicar toda la historia de cómo fuimos construyendo la solución paso a paso, yendo desde lo más simple hasta las invocaciones y chequeos finales.

Las clases tienen un **encabezado_clase** que actualiza el ambito actual. A partir de ahi tenemos que identificar los atributos. Los atributos de las clases se guardan con la misma estrategia que usamos en las funciones. Para cada atributo atributo vamos a agregar 2 campos a la entrada de la **TS** asociada a la funcion, **ATR_NOMBRE_i** y **ATR_TIPO_i** y actualiza el contador **CANT_ATRIBUTOS**. Nos facilita el acceso posicional a atributos del tema 29.   

Cuando queremos instanciar una variable del tipo de una clase, tenemos que validar que el tipo que nos pasaron realmente exista en la tabla de simbolos y tenga **USO == nombre_clase**, sino damos un error semantico que anuncia que la clase no fue declarada. Si la declaracion es correcta, declaramos la variable con el tipo que corresponde y **USO = objeto**.

Ademas tenemos que contemplar la herencia multiple. La sintaxis es **EXTENDS lista_identificadores**, entonces lo primero es recorrer la lista y validar que los identificadores existan y sean **nombre_clase**. Luego de validar agregamos mas atributos a la funcion en la **TS**, **HEREDA_i** por cada herencia. 

Es importante registrar las herencias en orden porque luego para desambiguar los casos donde se necesita recuperar un metodo o atributo que está presente en 2 herencias tenemos que recorrer en orden y tomar el primero. La busqueda la realiza **TablaSimbolos.registrarHerencia**, que primero recorre la **TS** buscando el atributo/metodo con el mismo ambito de la funcion. Si no lo encuentra entonces toma todas las herencias en orden, y por cada una vuelve a realizar la misma busqueda recursiva. En ese momento podría suceder un ciclo de herencias donde A herede de B y viceverza, para evitarlo llevamos un conjunto de visitados.


## 4. Import / Export (Tema 26) y cuándo se chequea la visibilidad

El **Tema 26** introduce el control de visibilidad entre clases:
- Una clase puede decir desde qué otras clases importa miembros: `class B import from A begin ... end;`
- Y un atributo o método de una clase puede decir a quién se exporta: `shortint x export to B, C;` o `shortint m() begin ... end export to B;`

Acá también tenemos dos momentos: el registro y el chequeo estático.

### En la declaración:
- Cuando se reduce `importacion_opcional` (`IMPORT FROM lista_identificadores`), verificamos que las clases que se intentan importar existan en la tabla de símbolos. Si no existen, tiramos error semántico. Si existen, llamamos a `registrarImports`, que guarda `IMPORT_0`, `IMPORT_1`, etc., en la clase cliente.
- Cuando se reduce `exportacion_opcional` (`EXPORT TO lista_identificadores`), guardamos en la entrada del atributo o método un string `EXPORT_TO` con las clases autorizadas (ej. `"B,C"`).  
  **Un detalle muy importante de diseño:** en `export to` no podemos exigir que las clases destino ya estén declaradas antes en el archivo. ¿Por qué? Porque en un código normal como el que da la cátedra en el enunciado, la clase `A` se declara primero y exporta a `B`, pero `B` recién se escribe más abajo. Si exigiéramos que `B` exista antes, tendríamos un error de huevo o la gallina. Por eso, en la declaración solo guardamos la lista de nombres autorizados, y el chequeo real de visibilidad lo hacemos en el punto de uso.

### En el punto de uso (Chequeo estático de visibilidad):
¿Cuándo se chequea? Cada vez que se intenta acceder a un atributo o invocar un método de otra clase.  
El compilador mira en qué ámbito estamos actualmente mediante `obtenerClaseDeAmbito(ambitoActual)`. Si estamos adentro de un método de la clase `B`, y el objeto al que estamos accediendo es de la clase `A`:
1. **Primer chequeo:** ¿La clase `B` declaró que importa a `A`?  
   Consultamos `obtenerImports("B:main")`. Si `A` no está en la lista de importados, emitimos:  
   `Error semantico (linea X): La clase 'B' no importa a la clase 'A'`
2. **Segundo chequeo:** ¿Ese atributo o método específico de `A` tiene permiso para `B`?  
   Llamamos a `TablaSimbolos.estaExportadoA(idMiembro, "B")`. Si el miembro no tiene a `B` en su lista `EXPORT_TO`, emitimos:  
   `Error semantico (linea X): El miembro 'x' de la clase 'A' no esta exportado a la clase 'B'`

Si ambas cosas están bien, el acceso se permite. Si estamos adentro de la misma clase dueña del objeto, no se pide import/export porque el acceso es interno.

## 5. Acceso posicional a atributos (Tema 29) y generación de código

Ahora que ya vimos cómo se guardan los atributos, cómo se hereda y cómo se controla la visibilidad, podemos ver cómo se usan los atributos con notación posicional:
`b[0$s] := 25$s;` o `d[idx] := 42$s;`

La regla `acceso_posicional` puede venir con constante entera (`CTE_SHORTINT`) o con variable (`IDENTIFICADOR`). En ambos casos:
1. Valida que el identificador del objeto exista y sea efectivamente un objeto (o un parámetro formal de tipo clase, como cuando se pasa `c_base obj` a un método).
2. **Si el índice es constante (`0$s`, `1$s`):**
   - Parseamos el número del índice.
   - Chequeamos el rango estático: tiene que ser mayor o igual a 0 y estrictamente menor a `CANT_ATRIBUTOS`. Si alguien pone `b[5$s]` en una clase que tiene 2 atributos, tiramos inmediatamente:  
     `Error semantico (linea X): Indice posicional [5$s] fuera de rango para la clase 'c_base' (cantidad de atributos: 2)`
   - Si el rango es correcto, obtenemos de la tabla de símbolos el nombre real y el tipo de ese atributo (`ATR_NOMBRE_idx`, `ATR_TIPO_idx`).
   - Aplicamos el chequeo de visibilidad (Tema 26) que explicamos antes.
   - Sintetizamos el operando como `b:main[0$s]` y le asociamos su tipo (`SHORTINT` o `SINGLEF`).
3. **Si el índice es variable (`b[idx]`):**
   - Validamos que la variable `idx` esté declarada.
   - Validamos que sea de tipo `SHORTINT`. Si le pasan un `SINGLEF`, tiramos error semántico porque los índices deben ser enteros.
   - Sintetizamos el operando como `b:main[idx:main]`.

Cuando esto se usa del lado izquierdo de una asignación (`acceso_posicional := expresion`):
- Comparamos el tipo del atributo con el tipo de la expresión.
- Si ambos coinciden, emitimos el terceto de asignación: `( := , b:main[0$s] , 25 )`.
- Si hay incompatibilidad (por ejemplo asignar un float a un entero sin `tos()`), tiramos el error semántico de tipos correspondiente.

Y si se usa adentro de una expresión aritmética (`r := b[0$s] + 1$s;`), el acceso posicional sintetiza su valor y tipo, generando el terceto de suma y luego la asignación sin ninguna diferencia respecto a una variable común.

## 6. Invocación de métodos (`invocacion_metodo`)

Para cerrar el circuito, la invocación de métodos (`obj.metodo()` o `obj.metodo(params)`) junta todos los elementos que explicamos:
1. Valida que `obj` sea un objeto o parámetro de tipo clase.
2. Resuelve el método llamando a `buscarMiembroEnJerarquia` (aplicando la herencia múltiple y el Desambiguado Opción 1).
3. Verifica la visibilidad con `IMPORT FROM` y `EXPORT TO`.
4. Si tiene parámetros, compara la cantidad recibida con la esperada (`CANT_PARAMS`), chequea que coincidan los tipos uno a uno y emite los tercetos de copia de parámetros `( := , paramFormal , paramReal )`.
5. Finalmente emite el terceto de llamada `( CALL , metodoMangled , - )` y sintetiza el tipo de retorno para que pueda usarse en expresiones o asignaciones.
