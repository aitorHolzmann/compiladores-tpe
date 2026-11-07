# Name Mangling y Tabla de Símbolos

Se utiliza un `LinkedHashMap<String, Map<String, Object>>` para la tabla de símbolos. Las entradas se guardan indexadas directamente por su clave mangled (ej. `main@func@variable`) o por su lexema en caso de constantes y cadenas (`5`, `-3`, `"hola"`). Usar `LinkedHashMap` garantiza que la iteración sea determinística y predecible para los tests.

> **Nota de consulta (07/11):** En el `Tp3.pdf` el ejemplo de mangling era `simbolo:main:ambito`, pero en la consulta previa a la entrega la profesora indicó expresamente usar el separador `@` y poner el ámbito primero (`main@ambito@simbolo`).

La variable `ambitoActual` vive en `Parser`, inicia como `"main"`.
- `entrarAmbito(String nombre)` le concatena `ambitoActual + "@" + nombre`.
- `salirAmbito()` recorta el string hasta el último `@`. Si no quedan más `@`, vuelve a `"main"`. Es fundamental que ante errores de sintaxis (ej. falta de `END` o `;`) se siga saliendo del ámbito para no corromper los ámbitos siguientes.

### `TablaSimbolos.declararVariable(String nombre, String tipo, String uso, String ambito)`

Arma la clave definitiva: `clave = ambito + "@" + nombre`.
1. Primero chequea si ya existe esa clave en la tabla (`tabla_simbolos.containsKey(clave)`). Si ya está, emite error semántico de redeclaración en ese ámbito y devuelve `null`.
2. Si no está redeclarada, remueve la entrada temporal que cargó el léxico sin ámbito (`tabla_simbolos.remove(nombre)`).
3. Crea el mapa de atributos (`TIPO`, `USO`), guarda la nueva entrada en `tabla_simbolos.put(clave, atributos)` y devuelve la clave.
Ya no se guardan atributos redundantes como `LEXEMA`, `MANGLING` ni `AMBITO`:
- El nombre original se deduce en $O(1)$ con `obtenerNombre(clave)` (`clave.substring(idx + 1)`).
- El ámbito se deduce en $O(1)$ con `obtenerAmbito(clave)` (`clave.substring(0, idx)`).

### Interacción Léxico-Sintáctico y Purga Final
1. Cuando el léxico reconoce un identificador (`AS3`), lo da de alta en la tabla con `TablaSimbolos.agregarIdentificador(lexema)` con atributos vacíos y pasa el lexema al sintáctico en `yylval.sval`.
2. Si la regla es declarativa, `declararVariable` borra la entrada sin ámbito y crea la entrada mangleda con ámbito.
3. Si el identificador solo se usó en una sentencia ejecutable (o nunca se declaró), queda sin `@`. Al terminar `yyparse()`, `TablaSimbolos.purgarIdentificadoresSinDeclarar()` recorre la tabla y elimina todas las entradas que no tengan `@` y tengan atributos vacíos, dejando la tabla limpia sin tocar constantes ni cadenas (que tienen `OCURRENCIAS` y `TIPO`).

### Búsqueda con alcance: `TablaSimbolos.buscarIdentificador(String nombre, String ambito)`
Recibe el nombre/lexema y el `ambitoActual`.
En lugar de recorrer linealmente toda la tabla, hace una búsqueda directa con `containsKey`:
1. Pregunta si existe `ambito + "@" + nombre`. Si está, devuelve esa clave.
2. Si no, recorta el último `@` del ámbito y repite la consulta.
3. Si el ámbito llega a `null`, significa que no está declarada ni al alcance.
La complejidad pasa de $O(N \cdot k)$ a $O(k)$ ($O(1)$ por nivel de ámbito).

### Listas de lexemas en `Object` (`ArrayList<String>`)
Los parámetros, atributos de clases, herencias, imports y exports se almacenan en atributos de tipo `ArrayList<String>` en lugar de contadores y claves indexadas:
- `PARAMETROS`: lista de lexemas de parámetros formales.
- `ATRIBUTOS`: lista de lexemas de atributos de la clase (para el acceso posicional del Tema 29).
- `HERENCIA`: lista de clases base del `EXTENDS` (para el Desambiguado Opción 1 del Tema 30).
- `IMPORTS`: lista de clases de `IMPORT FROM` (Tema 26).
- `EXPORT_TO`: lista de clases de `EXPORT TO` en atributos o métodos (Tema 26).

**Única fuente de verdad:** `PARAMETROS` y `ATRIBUTOS` guardan solo los nombres; el tipo de cada uno se consulta directamente en la tabla con `claveFuncion + "@" + param` o `claveClase + "@" + atr`. No se duplica información.


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

Empecemos con las funciones. Cuando detectamos el encabezado de una funcion, ya en ese momento estamos guardando el uso (funcion) y el tipo de retorno (puede ser auto y se define después), e iniciamos la lista de tercetos correspondiente al ámbito de la función (`GeneradorTerceto.iniciarAmbito(ambitoActual)`). Siguiendo la Opción 1 de `Tp3.pdf`, cada función posee su propia lista de tercetos en `tercetosPorAmbito` (`Map<String, ArrayList<Terceto>>`) con numeración local iniciando en 0, sin emitir pseudo-tercetos delimitadores (`FUNCION`/`FIN_FUNCION`). Luego de detectar el encabezado de la funcion, se reducen las reglas de `parametro_formal`. Cada parametro formal se declara como parametro en el ambito de la funcion y se agrega a su lista de parametros llamando a `TablaSimbolos.agregarParametro(funcMangled, nombreParam)`. Si dentro de la funcion encontramos un RET vamos a generar un terceto con el valor y el tipo, pero antes debemos controlar que coincida con el tipo de la funcion. Puede ser el caso de una funcion AUTO, si es así debemos actualizar el atributo tipo en la tabla de simbolos, pero como ademas permite varios retornos tambien hay que contemplar que ese tipo puede crecer a uno mas abarcativo. Por último, cuando reconocemos la declaracion completa de la funcion, controlamos que efectivamente haya un retorno. Si es una función común con tipo estático que no contiene ninguna sentencia `RET` explícita, generamos automáticamente un terceto de retorno por defecto (`RET`, `0$s` si es `SHORTINT` o `0.0` si es `SINGLEF`) al final de la lista del ámbito. Si es una función `AUTO`, la instrucción `RET` es obligatoria; si falta, reportamos error semántico.

Dijimos que en la tabla de simbolos los parametros formales se guardan como atributos de la funcion, vamos a explicarlo mas a detalle. El caso mas sencillo es una funcion que no declara parametros, entonces no hace falta registrar nada. Pero si necesitamos registrar parametros entonces llamamos a `agregarParametro`. Para registrar un parametro simplemente agregamos su lexema a la lista `PARAMETROS` (`ArrayList<String>`). La cantidad de parámetros es `params.size()`, y el tipo no se duplica porque ya vive en la entrada del parámetro `claveFuncion + "@" + param`.

Ya hablamos de como se declaran las funciones. Ahora toca hablar de como se invocan. El caso mas simple es invocar a una funcion que no necesite parametros. La regla primero valida que efectivamente estemos intentando invocar a una funcion y que ademas esté al alcance, despues si se verifica que efectivamente la declaracion de la funcion no tiene parámetros entonces se genera un terceto con la llamada `CALL` y el tipo de retorno, ademas al reducir la regla se guarda el tipo de retorno para que las operaciones puedan verificar la comptabilidad de tipos de las expresiones.

Para el caso de las funciones que si necesitan parametros hay que trabajar un poco mas. La regla `lista_expresiones` guarda una lista de objetos del parser, cada uno tiene el valor en `sval` y el tipo en el `obj`. Al igual que antes validamos que esté al alcance y que sea una funcion, si se cumple entonces obtengo la lista de parametros esperada de la funcion con `obtenerParametros(claveFunc)`. Si la longitud coincide con la cantidad de argumentos reales, entonces iteramos y comparamos posicion a posicion si el tipo del parámetro formal (obtenido de `claveFunc + "@" + params.get(i)`) coincide con el de la expresion real. Cuando la verificacion se cumple genero un terceto para asignar al parametro formal el valor y tipo del real (`:= , claveFormal , paramReal`). Cuando termine de recorrer toda la lista sin errores, puedo emitir el terceto con la llamada a la funcion (`CALL , claveFunc , -`).

# Comptime

Para las variables **COMPTIME** necesitabamos que en lugar de generar tercetos sus valores se guarden en tiempo de compilacion. Entonces hay que distinguir dos casos, cuando es un **identificador** o cuando es una operacion entre variables **comptime**. Para el primer caso guardamos el valor en la **TS** como un atributo `VALOR_COMPTIME`. El segundo caso se refiere al momento en que la gramatica reduce una expresion/termino/factor, entonces tenemos que guardar el resultado dentro del **parser val** como **sval**. Para saber si una expresion es **comptime** debemos preguntar mediante `esConstante()` que **no** empiece con `[` (un terceto de runtime) ni contenga `@` (un identificador de runtime con mangling).

Existen situaciones donde es necesario dar error en tiempo de compilacion. El primero ocurre cuando se quiere leer el valor de una variable **comptime** que todavia no fue inicializada. Tambien hay que dar error cuando se quiere operar una **comptime** con otra variable cuyo valor se conoce en tiempo de ejecucion. Es valido operar con numeros explicitos y otras **comptime**.

# Clases Herencia y Visibilidad

En la parte de POO tenemos tres temas juntos:
- **Tema 30 (Herencia multiple - Desambiguado Opcion 1):** Cuando una clase declara `EXTENDS c1, c2;`, guardamos en la tabla de símbolos la lista ordenada de padres en el atributo `HERENCIA` (`ArrayList<String>`). Al buscar un método o atributo (`buscarMiembroEnJerarquia`), primero busca en la propia clase (`main@clase@miembro`), y si no está, busca en los padres en el orden estricto de izquierda a derecha. La primera coincidencia encontrada es la que se usa.
- **Tema 26 (Import / Export):** Una clase puede indicar `IMPORT FROM c1, c2;` (guardado en `IMPORTS` como `ArrayList<String>`) y sus atributos o métodos pueden llevar `EXPORT TO c3, c4;` (guardado en `EXPORT_TO` como `ArrayList<String>`). Cuando un método de una clase `B` accede a un miembro de `A`, validamos que `B` tenga en sus imports a `A`, y que el miembro de `A` tenga en su lista de export a `B`. Si alguna de las dos cosas no se cumple, damos error semántico.
- **Tema 29 (Acceso posicional):** En lugar de usar punto para acceder a los atributos, se accede con corchetes `obj[0$s]` o `obj[idx]`. En la tabla de símbolos, cada atributo declarado en la clase se registra en la lista `ATRIBUTOS` (`ArrayList<String>`) respetando el orden de declaración. Si se accede con constante numérica, verificamos en tiempo de compilación que el índice sea válido (entre 0 y `atrs.size() - 1`). Si se accede con variable, validamos que dicha variable sea de tipo `SHORTINT`. En los tercetos generamos la asignación o lectura referenciando al objeto indexado (`main@obj[0$s]` o `main@obj[main@idx]`).

Vamos a explicar toda la historia de cómo fuimos construyendo la solución paso a paso, yendo desde lo más simple hasta las invocaciones y chequeos finales.

Las clases tienen un **encabezado_clase** que actualiza el ambito actual (`main@Clase`). A partir de ahi tenemos que identificar los atributos. Para cada atributo agregamos su nombre a la lista `ATRIBUTOS` de la clase y lo declaramos en la tabla con clave `main@Clase@nombre`. Esto nos facilita el acceso posicional a atributos del tema 29 preservando el orden de inserción.

Cuando queremos instanciar una variable del tipo de una clase, tenemos que validar que el tipo que nos pasaron realmente exista en la tabla de simbolos (`main@Clase`) y tenga **USO == nombre_clase**, sino damos un error semantico que anuncia que la clase no fue declarada. Si la declaracion es correcta, declaramos la variable con el tipo que corresponde y **USO = objeto**.

Ademas tenemos que contemplar la herencia multiple. La sintaxis es **EXTENDS lista_identificadores**, entonces al reducir la cláusula `EXTENDS` recorremos la lista y validamos tres condiciones por cada identificador:
1. Que exista en la tabla de símbolos y su uso sea **nombre_clase** (si no existe, error de clase base no declarada).
2. Que no introduzca ciclos de herencia directos ni transitivos, chequeando con `TablaSimbolos.hayCicloHerencia(nomClase, padre)` (si hay ciclo, error semántico de herencia circular).
3. Que no esté duplicado en la misma lista `EXTENDS` de la clase actual.

Solo los padres que pasan todas las validaciones se agregan a `padresValidos` y se registran en el atributo `HERENCIA` (`ArrayList<String>`) de la clase (siempre que la clase actual no haya sido una redeclaración inválida, controlado por la bandera `claseActualValida`).

Es importante registrar las herencias en orden porque luego para desambiguar los casos donde se necesita recuperar un metodo o atributo que está presente en 2 herencias tenemos que recorrer en orden y tomar el primero (Desambiguado Opción 1 del Tema 30). La busqueda la realiza `TablaSimbolos.buscarMiembroEnJerarquia`, que primero consulta si existe `main@Clase@miembro`. Si no lo encuentra, toma la lista `HERENCIA` en orden, y por cada padre vuelve a realizar la misma búsqueda recursiva. Dado que cualquier ciclo fue detectado y rechazado estrictamente en tiempo de compilación al procesar `EXTENDS`, el grafo de herencia en la tabla de símbolos es estrictamente un grafo acíclico dirigido (DAG) finito, por lo que la búsqueda recursiva termina siempre en pasos finitos sin necesidad de utilizar conjuntos auxiliares (`visitados`) en tiempo de ejecución.


## 4. Import / Export (Tema 26) y cuándo se chequea la visibilidad

El **Tema 26** introduce el control de visibilidad entre clases:
- Una clase puede decir desde qué otras clases importa miembros: `class B import from A begin ... end;`
- Y un atributo o método de una clase puede decir a quién se exporta: `shortint x export to B, C;` o `shortint m() begin ... end export to B;`

Acá también tenemos dos momentos: el registro y el chequeo estático.

### En la declaración:
- Cuando se reduce `importacion_opcional` (`IMPORT FROM lista_identificadores`), verificamos que las clases que se intentan importar existan en la tabla de símbolos. Si no existen, tiramos error semántico. Si existen, llamamos a `registrarImports`, que guarda el `ArrayList<String>` directo en el atributo `IMPORTS` de la clase `main@B`.
- Cuando se reduce `exportacion_opcional` (`EXPORT TO lista_identificadores`), guardamos en la entrada del atributo o método la lista `EXPORT_TO` con las clases autorizadas.  
  **Un detalle muy importante de diseño:** en `export to` no podemos exigir que las clases destino ya estén declaradas antes en el archivo. ¿Por qué? Porque en un código normal como el que da la cátedra en el enunciado, la clase `A` se declara primero y exporta a `B`, pero `B` recién se escribe más abajo. Si exigiéramos que `B` exista antes, tendríamos un error de huevo o la gallina. Por eso, en la declaración solo guardamos la lista de nombres autorizados, y el chequeo real de visibilidad lo hacemos en el punto de uso.

### En el punto de uso (Chequeo estático de visibilidad):
¿Cuándo se chequea? Cada vez que se intenta acceder a un atributo o invocar un método de otra clase.  
El compilador mira en qué ámbito estamos actualmente mediante `obtenerClaseDeAmbito(ambitoActual)`. Si estamos adentro de un método de la clase `B`, y el objeto al que estamos accediendo es de la clase `A`:
1. **Primer chequeo:** ¿La clase `B` declaró que importa a `A`?  
   Consultamos `obtenerImports("main@B")`. Si `A` no está en la lista de importados, emitimos:  
   `Error semantico (linea X): La clase 'B' no importa a la clase 'A'`
2. **Segundo chequeo:** ¿Ese atributo o método específico de `A` tiene permiso para `B`?  
   Llamamos a `TablaSimbolos.estaExportadoA(claveMiembro, "B")`. Si el miembro no tiene a `B` en su lista `EXPORT_TO`, emitimos:  
   `Error semantico (linea X): El miembro 'x' de la clase 'A' no esta exportado a la clase 'B'`

Si ambas cosas están bien, el acceso se permite. Si estamos adentro de la misma clase dueña del objeto, no se pide import/export porque el acceso es interno.

## 5. Acceso posicional a atributos (Tema 29) y generación de código

Ahora que ya vimos cómo se guardan los atributos, cómo se hereda y cómo se controla la visibilidad, podemos ver cómo se usan los atributos con notación posicional:
`b[0$s] := 25$s;` o `d[idx] := 42$s;`

La regla `acceso_posicional` puede venir con constante entera (`CTE_SHORTINT`) o con variable (`IDENTIFICADOR`). En ambos casos:
1. Valida que el identificador del objeto exista y sea efectivamente un objeto (o un parámetro formal de tipo clase, como cuando se pasa `c_base obj` a un método).
2. **Si el índice es constante (`0$s`, `1$s`):**
   - Parseamos el número del índice.
   - Chequeamos el rango estático: tiene que ser mayor o igual a 0 y estrictamente menor a `atrs.size()`. Si alguien pone `b[5$s]` en una clase que tiene 2 atributos, tiramos inmediatamente:  
     `Error semantico (linea X): Indice posicional [5$s] fuera de rango para la clase 'c_base' (cantidad de atributos: 2)`
   - Si el rango es correcto, obtenemos el nombre léxico del atributo en esa posición con `atrs.get(idx)` y consultamos su tipo en la tabla con `obtenerTipo(claseClave + "@" + atrs.get(idx))`.
   - Aplicamos el chequeo de visibilidad (Tema 26) que explicamos antes.
   - Sintetizamos el operando como `claveObj + "[" + idx + "$s]"` y le asociamos su tipo (`SHORTINT` o `SINGLEF`).
3. **Si el índice es variable (`b[idx]`):**
   - Validamos que la variable `idx` esté declarada.
   - Validamos que sea de tipo `SHORTINT`. Si le pasan un `SINGLEF`, tiramos error semántico porque los índices deben ser enteros.
   - Sintetizamos el operando como `claveObj + "[" + claveIdx + "]"`.

Cuando esto se usa del lado izquierdo de una asignación (`acceso_posicional := expresion`):
- Comparamos el tipo del atributo con el tipo de la expresión.
- Si ambos coinciden, emitimos el terceto de asignación: `( := , main@b[0$s] , 25 )`.
- Si hay incompatibilidad (por ejemplo asignar un float a un entero sin `tos()`), tiramos el error semántico de tipos correspondiente.

Y si se usa adentro de una expresión aritmética (`r := b[0$s] + 1$s;`), el acceso posicional sintetiza su valor y tipo, generando el terceto de suma y luego la asignación sin ninguna diferencia respecto a una variable común.

## 6. Invocación de métodos (`invocacion_metodo`)

Para cerrar el circuito, la invocación de métodos (`obj.metodo()` o `obj.metodo(params)`) junta todos los elementos que explicamos:
1. Valida que `obj` sea un objeto o parámetro de tipo clase.
2. Resuelve el método llamando a `buscarMiembroEnJerarquia` (aplicando la herencia múltiple y el Desambiguado Opción 1).
3. Verifica la visibilidad con `IMPORT FROM` y `EXPORT TO`.
4. Si tiene parámetros, compara la cantidad recibida con la esperada (`params.size()`), chequea que coincidan los tipos uno a uno y emite los tercetos de copia de parámetros `( := , paramFormal , paramReal )`.
5. Finalmente emite el terceto de llamada `( CALL , claveMetodo , - )` y sintetiza el tipo de retorno para que pueda usarse en expresiones o asignaciones.
