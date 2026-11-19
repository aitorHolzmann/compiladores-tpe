# Informe Final — TP3 y TP4

**Grupo 20**
Integrantes:
- Nicolas Benito (nbenito@alumnos.exa.unicen.edu.ar)
- Raffaeli Esteban (eraffaeli@alumnos.exa.unicen.edu.ar)
- Holzmann Aitor (aholzmann@alumnos.exa.unicen.edu.ar)

**Temas particulares asignados:** 1 (**SHORTINT**), 7 (**SINGLEF**), 10 (cadenas multilínea), 12 (**REPEAT UNTIL**), 15 (comentarios **//**), 17 (asignaciones en expresiones con **=**), 21 (**AUTO FUNCTION**), 22 (**COMPTIME**), 26 (**IMPORT FROM** / **EXPORT TO**), 29 (acceso posicional con corchetes), 30 (herencia múltiple con **EXTENDS**, Desambiguado Opción 1) y 34 (**tos()**).
**Código intermedio:** Tercetos. **Controles en tiempo de ejecución:** **c** (overflow suma float), **d** (overflow producto entero), **g** (pérdida de información en **tos()**), **i** (recursión directa o mutua).

# Introducción

En este informe documentamos las etapas 3 y 4 del compilador desarrollado en Java y BYacc/J para el Grupo 20. Partiendo del analizador léxico y sintáctico de los trabajos anteriores, incorporamos los chequeos semánticos, el manejo de ámbitos con name mangling, la generación de código intermedio por tercetos y finalmente la traducción a código Assembler x86 junto con los cuatro controles en tiempo de ejecución asignados.

# Generación de Código Intermedio y Análisis Semántico (Etapa 3)

## Name Mangling y Tabla de Símbolos

Se utiliza un **LinkedHashMap\<String, Map\<String, Object\>\>** para la tabla de símbolos (mantiene el orden de insercion al imprimirla y al armar **.data**). Las entradas se guardan indexadas directamente por su clave mangled (ej. **main@func@variable**) o por su lexema en caso de constantes y cadenas (**5**, **-3**, **"hola"**).

La variable **ambitoActual** vive en **Parser**, e inicia como **"main"**.
- **entrarAmbito(String nombre)** le concatena **ambitoActual + "@" + nombre**.
- **salirAmbito()** recorta el string hasta el último **@** (si no quedan mas vuelve a **"main"**) y limpia **pilaSaltos**.

 **TablaSimbolos.declararVariable(String nombre, String tipo, String uso, String ambito)** arma la clave de la siguiente manera: **clave = ambito + "@" + nombre**.
1. Primero chequea si ya existe esa clave en la tabla (**tabla_simbolos.containsKey(clave)**). Si ya está, emite error semántico de redeclaración en ese ámbito y devuelve **null**.
2. Si no está **redeclarada** entonces remueve la entrada que cargó el léxico en la etapa anterior, para poder insertar una entrada con más metadatos.
3. Entonces crea el mapa de atributos (**TIPO**, **USO**), guarda la nueva entrada en **tabla_simbolos.put(clave, atributos)** y devuelve la clave con el ambito incluido (**ambito@nombre**).

Ahora con tener la clave de la tabla de simbolos podemos conocer tanto el **nombre** como el **ámbito**. Para obtener el nombre tomamos lo que esta despues del ultimo **@**, y para el **ámbito** tomamos todo lo que esta antes del ultimo **@**. De eso se encargan las funciones **obtenerNombre(clave)** y **obtenerAmbito(clave)**
### Interacción Léxico-Sintáctico y Purga Final
1. Cuando el léxico reconoce un identificador (**AS3**), lo da de alta en la tabla con **TablaSimbolos.agregarIdentificador(lexema)** con atributos vacíos y pasa el lexema al sintáctico en **yylval.sval**.
2. Si la regla es declarativa, **declararVariable** borra la entrada sin ámbito y crea la entrada mangleda con ámbito.
3. Cada vez que un identificador aparece en una sentencia ejecutable, **AS3** lo vuelve a insertar sin **@** y con atributos vacios (lo mismo si nunca se declaro). Al terminar **yyparse()**, **TablaSimbolos.purgarIdentificadoresSinDeclarar()** recorre la tabla y elimina todas las entradas que no tengan **@** y tengan atributos vacíos, dejando la tabla limpia sin tocar constantes ni cadenas (que tienen **OCURRENCIAS** y **TIPO**).

Para buscar una variable teniendo en cuenta el alcance usamos **TablaSimbolos.buscarIdentificador(String nombre, String ambito)**. Recibe el lexema y el **ambitoActual**.
El flujo es el siguiente:
1. Pregunta si existe **ambito + "@" + nombre**. Si está entonces devuelve esa clave.
2. Si no está, recorta el último **@** (solo del ámbito) y repite la consulta.
3. Si el ámbito llega a **null**, significa que no está al alcance o declarada.
### Listas de lexemas en Object (ArrayList\<String\>)
Los parámetros, atributos de clases, herencias, imports y exports se almacenan en la **tabla de simbolos** como atributos de tipo **ArrayList\<String\>** :
- **PARAMETROS**
- **ATRIBUTOS**
- **HERENCIA**
- **IMPORTS**
- **EXPORT_TO**

Alcanza con guardar el nombre del identificador sin el ámbito. Sabiendo el lexema podemos buscar su entrada en la tabla de simbolos para conocer el tipo o lo que haga falta. Ademas conservamos el orden de declaracion, que nos ayuda al acceso posicional de atributos por ejemplo, o en las situaciones donde es necesario desambiguar.

## Estructura de Tercetos
Cada terceto guarda **operador**, **operando1**, **operando2** y **tipo**. En **GeneradorTerceto** los almacenamos en un **LinkedHashMap\<String, ArrayList\<Terceto\>\>** indexado por ambito (**main**, **main@f**, **main@Clase@m**), numerando cada lista desde 0.

## Chequeo de Tipos
Desde la semantica tenemos que hacer las verificaciones de tipo, cuando se hacen asignaciones, operaciones, y en nuestro caso tambien cuando se hace la conversion de **SINGLEF** a **SHORTINT**. El tipo se guarda tanto en la tabla de simbolos con las acciones semanticas, como desde la gramatica como informacion del **terceto**. 

En la regla **factor** y **numero negativo** guardamos el tipo en **pv.obj**. En el caso de las variables sabemos el tipo porque en la declaracion lo guardamos en la tabla de simbolos. Pero luego cuando hacemos operaciones y generamos **tercetos**, debemos verificar y conservar el tipo de la operacion, almacenandolo tambien en el **pv.obj.** 

Las asignaciones y comparaciones verifican que se realicen entre elementos del mismo tipo (no hay conversiones implicitas). La funcion **TOS()** verifica que reciba un **SINGLEF** y genera el terceto **( tos , op , - )** de tipo **SHORTINT**.

Tambien controlamos el atributo **USO** segun el contexto: a la izquierda de **:=** o **=** solo se aceptan **variable**, **parametro** o **atributo**, y en **factor** rechazamos nombres de funcion, metodo, clase o programa sin invocar.

Para el **Tema 17** (**ID = ( expresion )**), hacemos los mismos chequeos que en una asignacion comun, emitimos **( := , claveVar , expr )** y devolvemos su referencia **[i]** y tipo en **$$** para seguir operando. Para **pout(...)** emitimos **( POUT , op , - )** guardando el tipo si es una expresion.

## Control de Flujo y Backpatching

Para las estructuras de control **(IF, IF-ELSE y REPEAT UNTIL)** se implementó la técnica de backpatching usando una pila **Stack\<Integer\> pilaSaltos**.

En **condicion** se emite el terceto con el operador de comparación (**>**, **<**, **<=**, etc.) siempre que ambos operandos tengan el mismo tipo (guardando ese tipo en el terceto para usarlo en Assembler).

Tenemos que controlar las ramificaciones del codigo. Por eso cada instruccion que genera una rama fue separada en su propia regla semantica. 

El primer caso es el del IF. En nuestra implementacion las instrucciones de la rama verdadera **true** se generan a continuacion de la condicion, entonces debemos agregar en el medio un salto en caso de falso. Cuando detectamos el encabezado se genera un terceto BF con la condicion y un espacio vacio que se completa mas adelante. Ademas el indice del terceto BF se guarda en una pila del **parser** que va a ser utilizada para conocer el anidamiento de las sentencias condicionales y poder actualizar los saltos. 

Entonces si es un **if simple sin else** cuando lleguemos al **end if** significa que se generaron todos los tercetos del bloque condicional, entonces la ejecucion debe continuar a partir del proximo terceto. En ese momento podemos completar el campo vacio del terceto BF, para eso debemos tomar el tope de la pila que en este caso sera el terceto BF y completar el campo vacio con el indice del proximo terceto. 

Ahora si es un **IF ELSE** cambia un poco. Todavia debemos agregar el terceto BF porque al igual que antes los tercetos de la rama **true** se van a generar a continuacion de la condicion. Pero la diferencia está en que ahora el salto no debe llegar al final del **end if** sino al bloque **else**. La regla **inicio_else** detecta la palabra reservada **else** y tiene 2 responsabilidades. Primero tenemos que pensar que si venimos por la rama **true** debemos introducir un salto para no ejecutar el bloque falso. Entonces el ultimo terceto del bloque verdadero va a ser un nuevo terceto **BI** que ademas se agrega a la pila, porque es un salto incondicional al final del **end_if** cuya posición no conocemos todavía. Pero ademas como encontramos la posicion del **else** podemos actualizar el terceto **BF** para que haga un salto al primer terceto del bloque falso. Luego de generar todos los tercetos del bloque falso, vamos a llegar al **end_if**, en ese momento podemos tomar el terceto **bi** de la pila y actualizar su salto. 

La ultima sentencia de control es **repeat until** que itera mientras la condicion sea falsa. Este caso es mas sencillo porque no tenemos que hacer un salto a futuro sino hacia atras. Primero se lee el repeat y todo el bloque de instrucciones que se deben ejecutar. Entonces cuando leemos la palabra **repeat** debemos guardar en la pila ese inicio del primer terceto del bloque de instrucciones. Cuando llegemos al final y leamos **until** generamos un terceto **bf** con la condicion, y el salto al inicio del bloque de iteracion.

### Pseudocódigo del algoritmo de bifurcaciones
Resumen directo de las acciones sobre **pilaSaltos**:
```text
// IF / IF-ELSE
al reducir encabezado_if (IF '(' condicion ')'):
    idBF = crearTerceto("BF", condicion, "?")
    pilaSaltos.push(idBF)

al reducir inicio_else (ELSE):
    idBF = pilaSaltos.pop()
    idBI = crearTerceto("BI", "?", "-")
    completarSalto(idBF, proximoIndice)
    pilaSaltos.push(idBI)

al reducir END_IF ';':
    idSalto = pilaSaltos.pop()
    completarSalto(idSalto, proximoIndice)

// REPEAT UNTIL
al reducir inicio_repeat (REPEAT):
    pilaSaltos.push(proximoIndice)

al reducir UNTIL condicion ';':
    dirInicio = pilaSaltos.pop()
    crearTerceto("BF", condicion, "[" + dirInicio + "]")
```

## Funciones y parametros

Queremos controlar las declaraciones e invocaciones. Para eso debemos implementar el guardado de la firma de la funcion, es decir tipo de retorno y los parametros para conocer cantidad y tipo, y comprobarlo en las invocaciones. Además las funciones tienen su propia lista de tercetos. Lo que tenemos es un mapa con las diferentes listas de tercetos para cada ámbito. 

Empecemos con las funciones. Cuando detectamos el encabezado de una funcion, ya en ese momento estamos guardando el uso (funcion) y el tipo de retorno (puede ser auto y se define después), e iniciamos la lista de tercetos correspondiente al ámbito de la función (**GeneradorTerceto.iniciarAmbito(ambitoActual)**). Cada función posee su propia lista de tercetos con numeración que inicia en 0. Luego de detectar el encabezado de la funcion, se reducen las reglas de **parametro_formal**. Cada parametro formal se declara como parametro usando el ambito de la funcion, y también se agrega a la **lista de parametros** llamando a **TablaSimbolos.agregarParametro(funcMangled, nombreParam)** (**obtenerFuncionActualMangled()** devuelve **ambitoActual**, o **null** si estamos en **main**). 

Si dentro de la funcion encontramos un RET vamos a generar un terceto con el valor y el tipo, pero antes debemos controlar que coincida con el tipo de la funcion. Puede ser el caso de una funcion AUTO, si es así debemos actualizar el atributo tipo en la tabla de simbolos, pero como ademas permite varios retornos tambien hay que contemplar que ese tipo puede crecer a uno mas abarcativo. Por último, cuando reconocemos la declaracion completa de la funcion, controlamos que efectivamente haya un retorno. Si es una función común con tipo estático que no contiene ninguna sentencia **RET** explícita, generamos automáticamente un terceto de retorno por defecto (**RET**, **0** si es **SHORTINT** o **0.0** si es **SINGLEF**) al final de la lista del ámbito (para saber si hubo retorno usamos el flag **TIENE_RET** en la TS, y si aparece un **RET** en **main** damos error semantico). Si es una función **AUTO**, la instrucción **RET** es obligatoria si falta, reportamos error sintáctico (**yyerror**).

Dijimos que en la tabla de simbolos los parametros formales se guardan como atributos de la funcion, vamos a explicarlo mas a detalle. El caso mas sencillo es una funcion que no declara parametros, entonces no hace falta registrar nada. Pero si necesitamos registrar parametros entonces llamamos a **agregarParametro**. Para registrar un parametro simplemente agregamos su lexema a la lista **PARAMETROS** (**ArrayList\<String\>**). La cantidad de parámetros es **params.size()**, y el tipo no se duplica, sino que lo consultamos en la **tabla de simbolos** buscando **claveFuncion + "@" + param**. Los parametros tambien pueden ser de tipo clase (**Clase obj**), validando que esa clase exista.

Ya hablamos de como se declaran las funciones. Ahora toca hablar de como se invocan. El caso mas simple es invocar a una funcion que no necesite parametros. La regla primero valida que efectivamente estemos intentando invocar a una funcion(preguntamos por el uso) y que ademas esté al alcance, despues si se verifica que efectivamente la declaracion de la funcion no tiene parámetros entonces se genera un terceto con la llamada **CALL** y el tipo de retorno, ademas al reducir la regla se guarda el tipo de retorno para que las operaciones puedan verificar la compatibilidad de tipos de las expresiones.

Para el caso de las funciones que si necesitan parametros hay que trabajar un poco mas. La regla **lista_expresiones** guarda una lista de objetos del parser, cada uno tiene el valor en **sval** y el tipo en el **obj**. Al igual que antes validamos que esté al alcance y que sea una funcion, si se cumple entonces obtengo la lista de parametros esperada de la funcion con **obtenerParametros(claveFunc)**. Si la longitud coincide con la cantidad de argumentos reales, entonces iteramos y comparamos posicion a posicion si el tipo del parámetro formal (obtenido de **claveFunc + "@" + params.get(i)**) coincide con el de la expresion real. Cuando la verificacion se cumple genero un terceto para asignar al parametro formal el valor y tipo del real **( := , claveFormal , paramReal )**. Cuando termine de recorrer toda la lista sin errores, puedo emitir el terceto con la llamada a la funcion **( CALL , claveFunc , - )**.

## Comptime
Para las variables **COMPTIME** necesitabamos que en lugar de generar tercetos sus valores se guarden en tiempo de compilacion. Entonces hay que distinguir dos casos, cuando es un **identificador** o cuando es una operacion entre variables **comptime**. Para el primer caso guardamos el valor en la **TS** como un atributo **VALOR_COMPTIME**. El segundo caso se refiere al momento en que la gramatica reduce una expresion/termino/factor, entonces tenemos que guardar el resultado dentro del **parser val** como **sval**, y las reglas de mas arriba pueden distinguir si están trabajando con el resultado de una operacion entre **comptime** apoyandose en **esConstante()**. Es facil distinguir, si el **sval**  empieza con **[** (es un terceto), si contiene **@** ( es un identificador con mangling), si no tiene nada de eso es una constante.

Existen situaciones donde es necesario dar error en tiempo de compilacion. El primero ocurre cuando se quiere leer el valor de una variable **comptime** que todavia no fue inicializada. Tambien hay que dar error cuando se quiere asignar a una **comptime** una expresion que dependa de variables de tiempo de ejecucion (en cambio, usar una **comptime** ya inicializada en una expresion con variables de runtime si es valido porque se reemplaza por su constante). Cuando ambos operandos son constantes o **comptime**, **evaluarOperacionEstatica** resuelve la cuenta en compilacion controlando division por cero y rangos.

## Clases Herencia y Visibilidad

En la parte de POO tenemos tres temas juntos:
- **Tema 30 (Herencia multiple):** Cuando una clase declara **EXTENDS c1, c2;**. Guardamos en la tabla de símbolos la lista ordenada de padres en el atributo **HERENCIA** (**ArrayList\<String\>**). Al buscar un método o atributo llamamos a **buscarMiembroEnJerarquia**. Primero busca en la propia clase y si no está entonces busca en los padres en el orden que fueron declarados por el **EXTENDS**. La primera coincidencia encontrada es la que se usa.
- **Tema 26 (Import / Export):** Cuando una clase indica **IMPORT FROM c1, c2;** guardamos en el atributo **IMPORTS** como **ArrayList\<String\>**. Del otro lado las clases tienen que exportar sus atributos o métodos, pueden llevar **EXPORT TO c3, c4;** (guardado en el atributo **EXPORT_TO** como **ArrayList\<String\>**). Cuando un método de una clase **B** accede a un miembro de **A**, validamos que **B** tenga en sus imports a **A**, y que el miembro de **A** tenga en su lista de export a **B**. Si alguna de las dos cosas no se cumple, damos error semántico.
- **Tema 29 (Acceso posicional):** En lugar de usar punto para acceder a los atributos, se accede con corchetes **obj[0$s]** o **obj[idx]**. En la tabla de símbolos, cada atributo declarado en la clase se registra en la lista **ATRIBUTOS** (**ArrayList\<String\>**) respetando el orden de declaración. Si se accede con constante numérica, verificamos en tiempo de compilación que el índice sea válido (entre 0 y el **tamaño de ATRIBUTOS**). Si se accede con variable, validamos que dicha variable sea de tipo **SHORTINT**. En los tercetos generamos la asignación o lectura referenciando al objeto indexado (**main@obj[0$s]** o **main@obj[main@idx]**).

Vamos a explicar la ejecucion completa paso a paso, de lo mas simple a lo mas complejo.

Las clases tienen un **encabezado_clase** que actualiza el ambito actual (**main@Clase**). A partir de ahi tenemos que identificar los atributos. Para cada atributo agregamos su nombre a la lista **ATRIBUTOS** de la clase y lo declaramos en la tabla con clave **main@Clase@nombre**. 

Cuando queremos instanciar una variable del tipo de una clase, tenemos que validar que el tipo que nos pasaron realmente exista en la tabla de simbolos (**main@Clase**) y tenga **USO == nombre_clase**, sino damos un error semantico que anuncia que la clase no fue declarada. Si la declaracion es correcta, declaramos la variable con el tipo que corresponde y **USO = objeto**.

Ademas tenemos que contemplar la herencia multiple. La sintaxis es **EXTENDS lista_identificadores**, entonces al reducir la cláusula **EXTENDS** recorremos la lista y validamos tres condiciones por cada identificador:
1. Que exista en la tabla de símbolos y su uso sea **nombre_clase** (si no existe, error de clase base no declarada).
2. Que no introduzca ciclos de herencia directos ni transitivos, chequeando con **TablaSimbolos.hayCicloHerencia(nomClase, padre)** (si hay ciclo, error semántico de herencia circular).
3. Que no esté duplicado en la misma lista **EXTENDS** de la clase actual.

Solo los padres que pasan todas las validaciones se agregan a **padresValidos** y se registran en el atributo **HERENCIA** (**ArrayList\<String\>**) de la clase (siempre que la clase actual no haya sido una redeclaración inválida, controlado por la bandera **claseActualValida**). Esto no es para compilar con errores (si hay errores no se genera Assembler al final), sino para que el parser pueda seguir leyendo el archivo sin entrar en un bucle infinito de herencia al buscar miembros mas abajo.

Es importante registrar las herencias en orden porque luego para desambiguar los casos donde se necesita recuperar un metodo o atributo que está presente en 2 herencias tenemos que recorrer en orden y tomar el primero. La busqueda la realiza **TablaSimbolos.buscarMiembroEnJerarquia**, que primero consulta si existe **main@Clase@miembro**. Si no lo encuentra, toma la lista **HERENCIA** en orden, y por cada padre vuelve a realizar la misma búsqueda recursiva.

### Import / Export 
Una clase puede decir desde qué otras clases importa miembros, y un atributo o método de una clase puede decir a quién se exporta

Acá también tenemos dos momentos: el registro y el chequeo estático.
**En la declaración**:
- Cuando se reduce **importacion_opcional** (**IMPORT FROM lista_identificadores**), verificamos que las clases que se intentan importar existan en la tabla de símbolos. Si no existen, tiramos error semántico. Si existen, llamamos a **registrarImports**, que guarda el **ArrayList\<String\>** directo en el atributo **IMPORTS** de la clase **main@B**. La clase de la que se quiera importar debe haber sido declarada anteriormente.
- Cuando se reduce **exportacion_opcional** (**EXPORT TO lista_identificadores**), guardamos en la entrada del atributo o método la lista **EXPORT_TO** con las clases autorizadas. En este caso en la declaración solo guardamos la lista de nombres autorizados. El chequeo real de visibilidad lo hacemos al momento de usarse, para eso el compilador mira en qué ámbito estamos actualmente mediante **obtenerClaseDeAmbito(ambitoActual)**. Si estamos adentro de un método de la clase **B**, y el objeto al que estamos accediendo es de la clase **A**:
	
	1. ¿La clase **B** declaró que importa a **A**?  
	   Consultamos **obtenerImports("main@B")**. Si **A** no está en la lista de importados, emitimos: **error B no importa A**
	2. **¿Ese atributo o método de A tiene permiso para B?**  
	   Llamamos a **TablaSimbolos.estaExportadoA(claveMiembro, "B")**. Si el miembro no tiene a **B** en su lista **EXPORT_TO**, emitimos: **error A no exporta a B**

Si ambas cosas están bien, el acceso se permite. Si estamos adentro de la misma clase dueña del objeto, no se pide import/export porque el acceso es interno.

### Acceso posicional a atributos

Nos falta ver cómo se usan los atributos con notación posicional. 

La regla **acceso_posicional** puede venir con constante entera (**CTE_SHORTINT**) o con variable. En ambos casos:
1. Valida que el identificador del objeto exista y tenga **USO == OBJETO** o **USO == PARAMETRO**.
2. **Si el índice es una constante (1$s) entonces**  parseamos el número del índice y chequeamos que sea mayor o igual a 0 y menor a la cantidad maxima de atributos totales (obtenidos con **obtenerAtributosTotalesClase**, que junta primero los heredados en orden de **EXTENDS** y despues los propios).
   - Si el rango es correcto, obtenemos la clave absoluta del atributo en **atrsTotales.get(idx)** y consultamos su tipo con **obtenerTipo(claveAtr)**.
   - Aplicamos el chequeo de visibilidad contra la clase dueña del atributo.
   - Sintetizamos el operando como **claveObj + "[" + idx + "$s]"** y le asociamos su tipo (**SHORTINT** o **SINGLEF**).
3. **Si el índice es variable (b[idx]):**
   - Validamos que la variable **idx** esté declarada y sea de tipo **SHORTINT**. 
   - Sintetizamos el operando como **claveObj + "[" + claveIdx + "]"** y tomamos como tipo el del primer atributo de **obtenerAtributosTotalesClase**.

Cuando esto se usa del lado izquierdo de una asignación tenemos que comparar el tipo del atributo con el tipo de la expresión. Si ambos coinciden, emitimos el terceto de asignación: **( := , main@b[0$s] , 25 )**.

Y si se usa adentro de una expresión aritmética (**r := b[0$s] + 1$s;**), el acceso posicional sintetiza su valor y tipo, generando el terceto de suma y luego la asignación sin ninguna diferencia respecto a una variable común.

### Invocación de métodos (invocacion_metodo)

Para cerrar el circuito, la invocación de métodos (**obj.metodo()** o **obj.metodo(params)**) junta todos los elementos que explicamos:
1. Valida que **obj** sea un objeto o parámetro de tipo clase.
2. Resuelve el método llamando a **buscarMiembroEnJerarquia** (aplicando la herencia múltiple y el Desambiguado Opción 1) y valida que su **USO** sea **"metodo"**.
3. Verifica la visibilidad con **IMPORT FROM** y **EXPORT TO**.
4. Si tiene parámetros, compara la cantidad recibida con la esperada (**params.size()**), chequea que coincidan los tipos uno a uno y emite los tercetos de copia de parámetros **( := , paramFormal , paramReal )**.
5. Finalmente emite el terceto de llamada **( CALL , claveMetodo , claveObj )** (guardando el objeto receptor para la Etapa 4) y sintetiza el tipo de retorno para que pueda usarse en expresiones o asignaciones.

## Uso de notación posicional en Yacc ($$ y $n)

En **gram.y** usamos **$$** y **$1..$n** sobre **ParserVal** para pasar datos hacia arriba en las reducciones:
- **Referencias y valores en sval:** propagamos nombres de identificadores, literales constantes o referencias a tercetos (**[i]**) desde **factor**, **termino**, **expresion**, **unica**, **conversion_tos**, **acceso_posicional** y **condicion**.
- **Tipos en obj:** cada expresión guarda su tipo (**SHORTINT**, **SINGLEF** o nombre de clase) en **pv.obj** para chequear compatibilidad en operaciones, asignaciones, comparaciones, parámetros y **RET**.
- **Listas en obj:** en **lista_variables**, **lista_identificadores**, **exportacion_opcional** y **lista_expresiones** acumulamos un **ArrayList** en **$1.obj** y lo devolvemos en **$$** para declarar variables/atributos, registrar **IMPORT** / **EXPORT** / **EXTENDS** o validar argumentos reales.

## Nuevos errores considerados

Además de los errores léxicos y sintácticos de las etapas previas, incorporamos:
- **Alcance y declaraciones:** variable, función, clase o método no declarado/no visible, redeclaración en el mismo ámbito, uso incorrecto de símbolos según contexto (ej. asignar a una función o invocar un atributo como método).
- **Tipos y conversiones:** operaciones, asignaciones, comparaciones o parámetros con tipos incompatibles sin **tos()**; uso de **tos()** sobre un **SHORTINT**; **RET** fuera de función o con tipo distinto al declarado; cantidad incorrecta de argumentos.
- **Temas particulares:** **COMPTIME** leída sin inicializar, asignada desde runtime, o con división por cero/fuera de rango en compilación; clase en **IMPORT FROM** o **EXTENDS** inexistente, duplicada o con herencia circular; acceso a miembro no importado/no exportado; índice posicional fuera de rango o no **SHORTINT**.
- **Tiempo de ejecución (TP4):** overflow en suma **SINGLEF** (**c**), overflow en producto **SHORTINT** (**d**), pérdida de información en **tos()** (**g**) y recursión directa o mutua (**i**).

# Generación de Código Assembler (Etapa 4)

Para esta última entrega tenemos que generar código de máquina a partir del código intermedio (Tercetos). En nuestro caso elegimos como salida el lenguaje ensamblador del procesador Intel Pentium x86 de 32 bits, utilizando la herramienta **MASM32**.

Todo el código generado se guarda en un archivo **.asm** que se divide en distintas secciones:
* **Encabezado e Includes:** directivas de arquitectura (**.386**, **.model flat, stdcall**, **option casemap :none**), librerías (**masm32rt.inc**, **kernel32**, **masm32**) y prototipos externos (**dll_dllcrt0 PROTO C** y **printf PROTO C :VARARG**).
* **Sección .data:** donde se reservan e inicializan los datos estáticos del programa (variables, constantes literales y variables auxiliares del compilador).
* **Sección .code:** contiene las instrucciones ejecutables, marcando el inicio con la etiqueta **start:** y el fin del proceso con **invoke ExitProcess, 0** seguido de **end start**.

##  Condición Previa: Control de Errores

Antes de generar el Assembler, en el **main** de **gram.y** debemos validar que las etapas anteriores hayan finalizado sin errores. Si se cumple entonces podemos empezar a construir el **assembler** llamando a **GeneradorAssembler.generarAssembler()**. 

El metodo se encarga de construir el archivo **.asm**. Primero agrega todos los includes, y despues empieza a armar **.data**. Este paso consiste registrar las variables para las que queremos reservar espacio, usando un nombre y la cantidad de bytes a reservar. La idea es usar nombres estandarizados en funcion de los distintos simbolos:

* **Constantes numéricas (SHORTINT y SINGLEF):** como no tienen nombre en la tabla ya que su clave es el valor literal, se les antepone el prefijo **_cte_** y se reemplazan caracteres que dan problemas con MASM:
  * El signo **-** se reemplaza por **neg_**.
  * El punto decimal **.** se reemplaza por **_**.
  * El signo **+** se reemplaza por **pos_**.
  * *Ejemplos:* **10** -> **_cte_10**, **-5** -> **_cte_neg_5**, **3.14** -> **_cte_3_14**.
  
* **Cadenas de texto:** se identifican con un contador: **_str_0**, **_str_1**, etc.

* **Variables, parámetros y atributos:** se les antepone un guión bajo **_** para evitar colisiones con palabras reservadas de ensamblador y se reemplazan los **@** por **_**. Por ejemplo **main@a** -> **_main_a**
* **Variables auxiliares del sistema (@):** las variables que ya comienzan con **@** (como **@aux_print_short**) se dejan tal cual.


Para reservar el espacio tuvimos en cuenta los rangos de los tipos de datos:
* **Variables SHORTINT:** se emite **DB ?** ( 1 byte, El interrogante indica que no tenemos un valor para inicializar).
* **Variables SINGLEF:** se emite **DD ?** (4 bytes).
* **Constantes SHORTINT:** se emite **DB \<valor\>** (ej. **_cte_10 DB 10**).
* **Constantes SINGLEF:** se pasa por **formatearLiteralSinglef** para cambiar **s** por **E** y asegurar que tenga dígitos antes y después del punto y se emite **DD \<literal\>**.
* **Cadenas CADENA:** se limpian las comillas y se emiten terminadas en byte nulo (**0**), tal como exige C: **_str_0 DB "Mensaje", 0** (si está vacía emite **DB 0**).
* **Variables COMPTIME:** no se debe reservar espacio ya que se resuelven en compilacion.
* **Variables fijas de soporte para printf y conversiones:**
  ```nasm
  @aux_print_short DW ?   ; 16 bits para promocion de SHORTINT
  @aux_print_float DQ ?   ; 64 bits para promocion de SINGLEF
  @aux_tos DW ?           ; 16 bits para conversion tos() y RET mixto
  ```

## Pre-registro de Auxiliares y Constantes

El resultado de las operaciones intermedias se debe guardar en variables auxiliares, las cuales deben tener tambien su propio espacio reservado en **.data**. Entonces debemos recorrer todos los tercetos mediante **registrarAuxiliaresYConstantes()** y agregar las variables auxiliares a la tabla de simbolos.

### Variables @aux
Para cada terceto de operación (**+**, **-**, **\***, **/**, **tos**, **CALL**) se genera un nombre único según el ámbito:
  * En **main**: **@aux0**, **@aux1**, etc.
  * En funciones/métodos: **@aux_\<ambito\>_\<indice\>** (ej. **@aux_main_f_3**).
  Y se registra en la **TablaSimbolos** con **USO = "auxiliar"** y el tipo de dato del terceto (**SHORTINT** o **SINGLEF**). De esta forma, cuando el generador recorre la tabla para emitir **.data**, ya encuentra estas variables y reserva **DB ?** (1 byte) o **DD ?** (4 bytes).

Ejemplo:
* Terceto: **3 ( + , main@a , main@b )** de tipo **SHORTINT**.
* Se registra **@aux3** con tipo **SHORTINT**.
* En **.data** se emite: **@aux3 DB ?**.

### COMPTIME
Para la etapa 3 implementamos plegado de constantes para variables **COMPTIME**. Esto significa que muchas operaciones se resolvieron en compilación generando valores nuevos que no estaban en la Tabla de Símbolos.

El método **registrarConstanteSiFalta()** revisa los operandos de cada terceto si detecta un literal que no está en la tabla lo da de alta automáticamente con su tipo (**SHORTINT**, **SINGLEF** o **CADENA**).  Entonces en **.data** se genera su etiqueta correspondiente (ej. **_cte_25 DB 25**).

## Sección .code

La sección ejecutable inicia con **.code**, primero emite las subrutinas de funciones y métodos (**_proc_...**) y después la etiqueta **start:** con los tercetos de **main**, traduciendo cada operación a instrucciones x86.

### Resolución de Operandos (resolverOperandoAsm)
Cada operando de un terceto puede ser de dos tipos:
1. **Identificador o Constante directa:** se traduce a su nombre MASM mediante **obtenerNombreAsm()** (ej: **main@a** -> **_main_a**, **10** -> **_cte_10**).
2. **Referencia a otro terceto ([i]):** representa el resultado de una operación previa. Se resuelve al nombre de la variable auxiliar asociada: **@aux\<i\>**. Tenemos un caso especial que es asignación en expresión, si la referencia **[i]** apunta a un terceto de asignación **:=**, el valor no está en un auxiliar sino en la propia variable destino  (**operando1**) del terceto referenciado. En ese caso se toma directamente dicha variable.

### Traducción de Operaciones Aritméticas y Asignaciones



* **Operaciones SHORTINT - 8 bits con signo:**
  Se resuelven usando el registro **AL** (8 bits bajos de **EAX**):
  * **Suma (+):** **MOV AL, op1** -> **ADD AL, op2** -> **MOV @aux, AL**
  * **Resta (-):** **MOV AL, op1** -> **SUB AL, op2** -> **MOV @aux, AL**
  * **Multiplicación (\*):** **MOV AL, op1** -> **IMUL op2** -> **MOV @aux, AL** (la parte baja queda en **AL**; con el chequeo **d** de overflow después de **IMUL**).
  * **División (/):** **MOV AL, op1** -> **CBW** (extiende signo de **AL** a **AX**) -> **IDIV op2** -> **MOV @aux, AL** (el cociente queda en **AL**).
  * **Asignación (:=):** **MOV AL, op2** -> **MOV op1, AL**

* **Operaciones SINGLEF - 32 bits:**
  Se resuelven en la pila de la FPU (Coprocesador 80x87):
  * Carga: **FLD op1** (apila en **ST(0)**).
  * Operación: **FADD op2** (con chequeo **c** de overflow), **FSUB op2**, **FMUL op2** o **FDIV op2**.
  * Guardado: **FSTP @aux** (o **FSTP op1** si es asignación), desapilando de la FPU.

* **Conversión Explícita tos():** (además de los controles del chequeo **g** detallados más abajo)
  ```nasm
  FLD op1
  FISTP @aux_tos
  MOV AX, @aux_tos
  MOV @aux, AL
  ```
  1. **FLD op1**: carga el flotante a la FPU.
  2. **FISTP @aux_tos**: convierte el flotante a entero con redondeo y lo guarda en **@aux_tos** (**DW**, 16 bits), desapilando de la FPU.
  3. **MOV AX, @aux_tos** y **MOV @aux, AL**: copia los 8 bits bajos al auxiliar **SHORTINT**.

## Sentencia POUT

Para traducir **( POUT , op1 , - )** en cualquier ámbito, consultamos el tipo de **op1** y generamos las instrucciones según corresponda:

### Impresión de CADENA
```nasm
invoke printf, cfm$("%s\n"), OFFSET _str_0
```
**printf** espera el puntero (la dirección en memoria de la cadena). Sin **OFFSET**, intentaría pasar el texto desreferenciado como un número y el programa fallaría. **cfm$()** MASM32 crea la cadena de formato terminada en **0** en tiempo de ensamblado.

### Impresión de SHORTINT
```nasm
MOV AL, _cte_neg_5
MOVSX AX, AL
MOV @aux_print_short, AX
invoke printf, cfm$("%hd\n"), @aux_print_short
```
**printf** usa **%hd** esperando un entero de 16 bits (**WORD**), no de 8 bits. Por eso cargamos el byte en **AL** y usamos **MOVSX AX, AL**. Copia **AL** a **AX** y rellena **AH** replicando el bit de signo. Así **-5** conserva su valor en 16 bits.

### Impresión de SINGLEF
```nasm
FLD _cte_3_14
FSTP @aux_print_float
invoke printf, cfm$("%Lf\n"), @aux_print_float
```
Como es una función con argumentos variables se exige trabajar con 64bits, pero nuestro flotantes usan 32. Entonces **FLD _cte_3_14** carga el float de 32 bits a la FPU (**ST(0)**) y **FSTP @aux_print_float** descarga en la variable de 64 bits **@aux_print_float** (**DQ**) convirtiéndolo automáticamente.

## Sentencias IF / ELSE y REPEAT UNTIL



Para realizar los saltos en assembler debemos indicar a que etiqueta saltar. El problema es que yo no puedo saber si un terceto de mas adelante quiere saltar a donde estoy ahora. Entonces la solucion que planteamos llega despues de emitir la seccion **.data**. Lo que hacemos es recorrer una primera vez toda la lista de tercetos y guardamos en **HashSet\<Integer\>** los indices de los tercetos a los que se quiere saltar. Entonces en la segunda pasada si **etiquetas.contains(i)**, anteponemos **Label_\<ambitoLimpio\>_\<i\>** (y al terminar el bucle revisamos también **listaTercetos.size()** por si un **IF** es la última sentencia del ámbito).

### Comparaciones 
Cuando se procesa un terceto de comparación (**>**, **<**, **>=**, **<=**, **==**, **!=**) tomamos su tipo desde **t.getTipo()**:

* **Para enteros SHORTINT** se carga el primer operando en **AL** y se compara con **CMP AL, op2**:
  ```nasm
  MOV AL, _main_a
  CMP AL, _main_b
  ```
  **CMP** resta **AL - op2** y actualiza los flags del CPU (**ZF**, **SF**, **OF**). 
* **Para flotantes SINGLEF** el procesador no compara flotantes en los registros generales. Cargamos en la pila de la FPU y usamos **FCOMP**:
  ```nasm
  FLD _main_x
  FCOMP _main_y
  FSTSW AX
  SAHF
  ```
  * **FCOMP**: compara **ST(0)** con **op2** y **desapila** **ST(0)**, dejando la pila limpia.
  * **FSTSW AX**: guarda la palabra de estado de la FPU en **AX**.
  * **SAHF**: transfiere el byte **AH** a los flags del CPU (el bit **C0** de la FPU va a parar al *Carry Flag* **CF** y **C3** al *Zero Flag* **ZF**).

### Bifurcación por Falso (BF) 
El terceto **BF** salta cuando la condición previa **NO** se cumple:
* **Para SHORTINT :**
  * **>** $\to$ complementario $\le$ $\to$ **JLE Label_\<ambito\>_\<dest\>**
  * **>=** $\to$ complementario $<$ $\to$ **JL Label_\<ambito\>_\<dest\>**
  * **<** $\to$ complementario $\ge$ $\to$ **JGE Label_\<ambito\>_\<dest\>**
  * **<=** $\to$ complementario $>$ $\to$ **JG Label_\<ambito\>_\<dest\>**
  * **==** $\to$ complementario $\ne$ $\to$ **JNE Label_\<ambito\>_\<dest\>**
  * **!=** $\to$ complementario $=$ $\to$ **JE Label_\<ambito\>_\<dest\>**
* **Para SINGLEF :**
  Debido a cómo mapea la FPU sus Condition Codes a **EFLAGS** (**CF** y **ZF**), los saltos deben ser **sin signo** (*Above / Below*):
  * **>** $\to$ complementario $\le$ $\to$ **JBE Label_\<ambito\>_\<dest\>**
  * **>=** $\to$ complementario $<$ $\to$ **JB Label_\<ambito\>_\<dest\>**
  * **<** $\to$ complementario $\ge$ $\to$ **JAE Label_\<ambito\>_\<dest\>**
  * **<=** $\to$ complementario $>$ $\to$ **JA Label_\<ambito\>_\<dest\>**
  * **==** $\to$ complementario $\ne$ $\to$ **JNE Label_\<ambito\>_\<dest\>**
  * **!=** $\to$ complementario $=$ $\to$ **JE Label_\<ambito\>_\<dest\>**

### Salto Incondicional BI
Se traduce directamente a un salto incondicional de x86:
```nasm
JMP Label_<ambito>_<dest>
```
Se utiliza para saltar al final del bloque **IF-ELSE** desde el término de la rama **THEN**.

### Sentencia REPEAT UNTIL 
 **REPEAT UNTIL** se implementa como el cuerpo de sentencias seguido de la condición y un terceto **( BF , [cond] , [inicio] )**. Con la implementacion de los saltos **BF** alcanza y  no hay que hacer nada adicional


## Funciones, AUTO FUNCTION, Métodos, Objetos y Acceso Posicional

### Reserva en .data para Funciones y Objetos
Para permitir funciones y objetos lo primero que tenemos que hacer es reservar en **.data** las variables necesarias:
* **Variables de retorno (funcion y metodo):** cuando recorremos la Tabla de Símbolos y encontramos una entrada con uso **funcion** o **metodo**, declaramos una variable global para guardar su retorno: **_ret_\<ambito\> DB ?** si devuelve **SHORTINT** o **_ret_\<ambito\> DD ?** si devuelve **SINGLEF**. Para **AUTO FUNCTION** tambien funciona porque en la Etapa 3 ya le inferimos y asignamos su tipo de retorno en la tabla.

* **Atributos propios de cada objeto (objeto y parámetro de tipo clase):** una clase puede tener atributos propios y atributos heredados con **EXTENDS**. Para saber cuáles son todos los atributos en orden posicional (**0, 1, 2...**), usamos **obtenerAtributosTotalesClase()**, que junta recursivamente los atributos de las clases padres y los de la clase hija. Entonces, por cada objeto declarado (ej. **c_hija obj**) o parámetro formal de tipo clase (ej. **c_base1 obj_b1**), reservamos en **.data** una variable para cada uno de sus atributos (ej. **_main_obj_atr_a DB ?**, **_main_obj_atr_b DD ?**). Cuando se pasa un objeto como parámetro, el terceto **:=** copia atributo por atributo desde el objeto real al formal.

* **Variables @aux de los CALL:** en el pre-registro también incluimos los tercetos **CALL**, para que cada llamada tenga su propia variable **@aux\<i\>** donde dejar el valor devuelto.

### Definición de Subrutinas en .code
En la sección **.code**, antes de la etiqueta **start:**, recorremos todos los ámbitos de **tercetosPorAmbito** que no sean **main** y emitimos cada subrutina llamando al mismo método **generarCodigoAmbito()** (sumando en el prólogo y epílogo el control de **@activa_...** del Chequeo **i**):
```nasm
_proc_<ambitoLimpio>:
    PUSH EBP
    MOV EBP, ESP
    ... (código de los tercetos del ámbito) ...
_fin_<ambitoLimpio>:
    MOV ESP, EBP
    POP EBP
    RET
```

**¿Cómo sabe el procesador a dónde volver cuando termina la función?**
  Cuando se ejecuta **CALL _proc_...**, el procesador automáticamente apila en el tope de la pila(**ESP**) la **dirección de retorno**. que es la instrucción que le sigue a **CALL**, y salta a la subrutina. Al entrar, **PUSH EBP** y **MOV EBP, ESP** resguardando esa dirección de retorno por si adentro de la función se invoca a otra subrutina que use la pila. Al llegar a **_fin_\<ambitoLimpio\>:**, **MOV ESP, EBP** y **POP EBP** restauran la pila a como estaba al entrar, dejando otra vez en el tope la dirección de retorno. Finalmente, la instrucción **RET** desapila esa dirección y la carga en el puntero de instrucciones (**EIP**), volviendo exactamente a la línea siguiente del **CALL**.

### Traducción del Terceto RET
Cuando dentro de una función o método encontramos el terceto **( RET , op1 , - )** movemos el valor de **op1** a la variable de retorno de esa subrutina (**_ret_\<ambitoLimpio\>**), usando **MOV AL** si es **SHORTINT** o **FLD** / **FSTP** si es **SINGLEF** (si es una **AUTO FUNCTION** que infirió **SINGLEF** pero este **RET** devuelve **SHORTINT**, lo convertimos a float con **CBW**, **MOV @aux_tos, AX**, **FILD @aux_tos** y **FSTP _ret_...**). Despues hacemos **JMP _fin_\<ambitoLimpio\>** para saltar al epílogo y ejecutar el **RET**.

```nasm
MOV AL, @aux_main_duplicar_0
MOV _ret_main_duplicar, AL
JMP _fin_main_duplicar
```

### Traducción del Terceto CALL
Antes de llegar al terceto **CALL**, en la Etapa 3 ya se emitieron los tercetos **:=** que copian cada argumento real en la variable del parámetro formal (ej. **_main_duplicar_num := _main_a**). Cuando aparece el terceto **( CALL , claveSub , claveObj )**, tenemos dos casos:

#### Llamada a FUNCTION o AUTO FUNCTION
Se distingue porque el segundo operando del terceto es **"-"**. Lo único que hacemos es:
1. Emitir **CALL _proc_\<subLimpio\>**.
2. Cuando vuelve, copiar el resultado desde **_ret_\<subLimpio\>** a la variable auxiliar **@aux\<i\>** asociada al terceto **CALL**:
```nasm
CALL _proc_main_duplicar
MOV AL, _ret_main_duplicar
MOV @aux18, AL
```

#### Llamada a obj.metodo()
Este caso es un poco mas complejo. La subrutina del metodo de la clase se genero con las variables de **clase**. Pero en realidad cuando ejecutemos el metodo tenemos que modificar los valores de las **instancias**. Entonces la solucion es guardar el nombre del objeto (**main@obj**) en el segundo operando del terceto **CALL**: **( CALL , main@c_base_a@sumar_base , main@obj )**. Así al traducir el **CALL** hacemos tres pasos:
  1. **Antes del CALL:** copiamos todos los atributos del objeto (**_main_obj_\<atr\>**) hacia las variables de atributo de la clase (**_main_\<clase\>_\<atr\>**).
  2. **Llamada:** ejecutamos **CALL _proc_main_c_base_a_sumar_base** que trabaja con las variables de clase.
  3. **Después del CALL:** copiamos de vuelta las variables de la clase hacia las variables del objeto **_main_obj_\<atr\>** y guardamos **_ret_...** en el **@aux\<i\>** del terceto.

### Acceso posicional a atributos 
Para resolver el acceso posicional (**obj[0$s] := 15$s;** o **obj[a] := 25$s;**), buscamos la lista ordenada de atributos de la clase del objeto con **obtenerAtributosTotalesClase()** y separamos en dos casos:

* **Caso indice constante (obj[0$s]):**
  Como conocemos el número de posición en tiempo de compilación, buscamos directamente qué atributo está en ese índice **k** dentro de la lista de la clase y lo reemplazamos directamente por la variable de ese atributo del objeto (tanto en escritura como en lectura mediante **resolverAccesoPosicional**). Ej:
  ```nasm
  MOV AL, _cte_15
  MOV _main_obj_atr_a, AL
  ```

* **Caso indice con variable (obj[a]):**
  Como el valor de la variable **a** recién se conoce en tiempo de ejecución, en la asignación generamos comparaciones en Assembler (**CMP AL, k**) para cada posición **k** compatible de la clase. Si **a** vale **k**, asigna en ese atributo; si no, salta con **JNE** al siguiente:
  ```nasm
  MOV AL, _main_a
  CMP AL, 0
  JNE Label_skip_pos_main_3_0
  MOV AL, _cte_25
  MOV _main_obj_atr_a, AL
  Label_skip_pos_main_3_0:
  MOV AL, _main_a
  CMP AL, 2
  JNE Label_skip_pos_main_3_2
  MOV AL, _cte_25
  MOV _main_obj_atr_propio, AL
  Label_skip_pos_main_3_2:
  ```

## Controles en Tiempo de Ejecución 

Nos tocaron 4 chequeos en tiempo de ejecución **c, d, g e i**. Cuando ocurre cualquiera de estos errores, el programa salta a una rutina al final de **.code**, imprime el mensaje correspondiente con **printf** y corta la ejecución con **invoke ExitProcess, 1**.

Para poder hacer las comparaciones declaramos en **.data** las constantes límite (**@max_singlef DD 3.40282347E+38**, **@min_shortint_f DD -128.0**, **@max_shortint_f DD 127.0**), los 4 mensajes de error (**_msg_err_...**) y una bandera **@activa_\<ambito\> DB 0** por cada función o método.

### Chequeo c: Overflow en sumas de punto flotante
Verificar que la suma no pueda pasarse del límite de 32 bits (**3.40282347E+38**). Después de **FADD op2**:
1. Duplicamos el tope (**FLD ST(0)**) y le aplicamos valor absoluto (**FABS**) para cubrir también sumas negativas muy grandes.
2. Comparamos **|resultado|** contra **@max_singlef** con **FCOMP @max_singlef**, pasamos los flags con **FSTSW AX** + **SAHF** y si es mayor saltamos con **JA _err_overflow_float**.
3. Si está en rango, recién ahí guardamos el valor en **@aux** con **FSTP**.

```nasm
FLD _main_f1
FADD _main_f2
FLD ST(0)
FABS
FCOMP @max_singlef
FSTSW AX
SAHF
JA _err_overflow_float
FSTP @aux9
```

### Chequeo d: Overflow en productos de enteros
La instrucción **IMUL op2** de 8 bits deja el producto con signo en **AX** (16 bits), pero nuestro tipo **SHORTINT** entra solo en **AL** (**[-128, 127]**). Cuando el resultado no entra en 1 byte con signo, el procesador activa automáticamente el **Overflow Flag (OF = 1)**. Entonces alcanza con agregar **JO _err_overflow_entero** justo después de **IMUL**:

```nasm
MOV AL, _main_a
IMUL _main_b
JO _err_overflow_entero
MOV @aux3, AL
```

### Chequeo g: Pérdida de información en conversión tos()
Al pasar un **SINGLEF** a **SHORTINT** con **tos(op1)** se pierde información en dos casos: si se sale del rango **[-128, 127]** o si tiene parte decimal distinta de cero (ej. **10.5**).
1. **Control de rango:** cargamos **op1** y comparamos con **FCOMP @max_shortint_f** (**JA _err_perdida_info**) y con **FCOMP @min_shortint_f** (**JB _err_perdida_info**).
2. **Control de decimales:** convertimos a entero de 16 bits en **@aux_tos** con **FISTP @aux_tos**, lo volvemos a subir como flotante con **FILD @aux_tos**, apilamos el **op1** original con **FLD op1** y los comparamos desapilando ambos con **FCOMPP**. Si son distintos (**JNE _err_perdida_info**), tenía decimales y hubo pérdida de información.
3. Si pasó los dos controles, movemos **AX** desde **@aux_tos** y guardamos **AL** en **@aux**.

### Chequeo i: Recursión en invocaciones de funciones
El lenguaje prohíbe tanto la recursión directa (**f -> f**) como la mutua (**f -> g -> f**). Para detectarlo en runtime sin tocar la pila de llamadas:
* En **.data** declaramos **@activa_\<subrutina\> DB 0** para cada función o método.
* Al entrar a **_proc_\<subrutina\>:**, comparamos si ya vale **1** (**CMP @activa_..., 1** + **JE _err_recursion**); si estaba en **0**, la ponemos en **1**.
* Al salir por **_fin_\<subrutina\>:**, justo antes de restaurar **EBP** y hacer **RET**, la volvemos a poner en **0** (**MOV @activa_..., 0**).

## Cierre y Guardado del Archivo

Al terminar el recorrido de los tercetos de **main**:
1. Cerramos el programa normal con **invoke ExitProcess, 0**, dejamos las 4 etiquetas de error (**_err_overflow_float**, **_err_overflow_entero**, **_err_perdida_info**, **_err_recursion**) que imprimen el mensaje y terminan con **invoke ExitProcess, 1**, y cerramos el archivo con **end start**.
2. Invocamos **GeneradorAssembler.guardarArchivo(ruta, asm)**, que toma el nombre del archivo fuente (ej. **test.txt**), le cambia la extensión a **.asm** (**test.asm**) y lo graba en disco en UTF-8.

# Modificaciones respecto a etapas anteriores

Respecto a lo entregado en las etapas 1 y 2, realizamos los siguientes cambios sobre el léxico, la tabla de símbolos y la gramática:

- **Rediseño de TablaSimbolos:** pasó de **Map\<Integer, Map\<String, String\>\>** (indexada por ID numérico) a **LinkedHashMap\<String, Map\<String, Object\>\>** indexada por clave **String** (mangled o lexema). Esto permitió guardar listas (**ArrayList**) y flags booleanos (**COMPTIME**, **TIENE_RET**), y que **yylval** pase directamente el **String** en **sval**.
- **Acciones semánticas del léxico:** **AS6**, **AS8**, **AS9**, **AS10** y **AS14** ahora registran el atributo **TIPO** al dar de alta cada constante. En **AS12** se agregó la limpieza de saltos de línea y el alta de la cadena en la TS con **TIPO = "CADENA"**.
- **Ajustes en gram.y y salidas:** agregamos reglas intermedias (**encabezado_funcion**, **inicio_auto_funcion**, **encabezado_clase**, **encabezado_if**, **inicio_else**, **inicio_repeat**) para abrir/cerrar ámbitos y hacer backpatching. Sumamos **parametro_formal : IDENTIFICADOR IDENTIFICADOR** para pasar objetos por parámetro. Además consolidamos la salida mostrando errores, TS, tercetos y **.asm** (dejando los logs de tokens y reglas de TP1/TP2 solo con el flag **-v**)


# Conclusiones

Al integrar las cuatro etapas logramos un compilador completo que valida los programas del lenguaje asignado al grupo 20. Detecta los errores léxicos, sintácticos y semánticos. Y genera el código Assembler a partir de los tercetos generados como código intermedio.

