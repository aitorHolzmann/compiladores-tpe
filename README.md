# TPE - Compiladores

Compilador hecho en Java. El analizador léxico lo armamos a mano y el sintáctico lo generamos con byacc/j a partir de `gram.y`.

## Cómo ejecutarlo

Requisito: Java 11 o superior.

```
java -jar compilador.jar <archivo_a_compilar>
```

Por ejemplo:

```
java -jar compilador.jar tests/test_con_nombre.txt
```

- Se compila **un archivo por ejecución**, pasado como parámetro.
- El `.jar` es autocontenido: las tablas del analizador léxico (matriz de transición de estados, matriz de acciones semánticas y palabras reservadas) están empaquetadas adentro como *resources*. Se puede ejecutar desde cualquier carpeta, sin ningún otro archivo.
- Si el nombre del archivo tiene espacios, va entre comillas: `java -jar compilador.jar "mi prueba.txt"`.
- Opcional: agregando `-v` al final se muestra la traza interna del parser (`java -jar compilador.jar archivo.txt -v`).

Si no se pasa ningún archivo, o se pasa más de uno, el compilador muestra cómo se usa y termina.

## Qué muestra la salida

- Errores léxicos y sintácticos, con su número de línea.
- Las reglas de la gramática que se van reconociendo.
- Al final, la tabla de símbolos.

## Pruebas incluidas

En la carpeta `tests/` hay archivos de prueba. Cada uno se ejecuta por separado:

```
java -jar compilador.jar tests/prueba1.txt
java -jar compilador.jar tests/test_sin_end_prog.txt
```

## Generar el .jar desde el código fuente (opcional)

Solo hace falta si se quiere recompilar. Requiere un JDK 11 o superior.

Con `make`:

```
make jar
```

Sin `make` (por ejemplo, en PowerShell de Windows), desde la raíz del proyecto:

```
javac --release 11 -encoding UTF-8 -d bin (Get-ChildItem src\compilador\*.java, src\accion_semantica\*.java).FullName
Copy-Item -Recurse src\estructuras bin\src\
jar --create --file compilador.jar --main-class src.compilador.Parser -C bin .
```

## Estructura del proyecto

- `src/compilador/`: analizador léxico, parser (generado con byacc/j), tabla de símbolos.
- `src/accion_semantica/`: acciones semánticas del analizador léxico.
- `src/estructuras/`: tablas del léxico (se empaquetan dentro del `.jar`).
- `gram.y`: gramática de byacc/j. `make parser` regenera `Parser.java` y `ParserVal.java` (requiere byacc/j).
- `tests/`: programas de prueba.