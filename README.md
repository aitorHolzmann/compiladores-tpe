# TPE - Compiladores

Compilador hecho en Java. El analizador léxico lo armamos a mano y el sintáctico lo generamos con byacc/j a partir de `gram.y`.

El parser ya viene generado (`src/compilador/Parser.java` y `ParserVal.java`), así que para correr las pruebas no hace falta tener byacc/j ni compilar la gramática. Alcanza con Java y `make`.



### Linux
### Con Docker (sin instalar Java)

Si en la máquina no está Java pero sí Docker, se puede correr todo adentro de un contenedor. Solo hace falta tener `docker` y `make`, y que el usuario pueda usar Docker sin `sudo` (o sea, que esté en el grupo `docker`).

Desde la raíz del proyecto:

```
make docker-test
```

La primera vez tarda unos minutos porque arma la imagen (usa el mismo `Dockerfile` de la carpeta `.devcontainer`, que ya trae Java y make) y necesita internet. Las veces siguientes arranca enseguida. Hace exactamente lo mismo que `make test`, así que todo lo que se explica más abajo sobre la carpeta `tests/` vale igual.

### Con VS Code

Nosotros trabajamos con VS Code y la extensión **Dev Containers**. Si la tiene instalada, puede abrir la carpeta, hacer `Ctrl+Shift+P` → **Dev Containers: Reopen in Container**, y en la terminal que se abre (ya adentro del contenedor) correr `make test` normalmente. En Windows hace falta Docker Desktop abierto.

## Cómo correr las pruebas

Parada en la carpeta raíz del proyecto (donde está el `Makefile`):

```
make test
```

Eso compila todo el proyecto y después pasa uno por uno todos los archivos de la carpeta `tests/` por el compilador. Para cada archivo se muestra:

- el nombre de la prueba
- el código fuente de la prueba
- la salida del compilador: errores léxicos y sintácticos con su número de línea, las reglas que se van reconociendo y al final la tabla de símbolos

(Si se usa Docker, lo mismo pero con `make docker-test`.)

Como la salida es larga, a veces conviene mandarla a un archivo y leerla con calma:

```
make test > salida.txt
```

Hay que correrlo sí o sí desde la raíz, porque el compilador lee las tablas del léxico (`src/estructuras/...`) con rutas relativas.

`make clean` borra la carpeta `bin/` con las clases compiladas, por si quiere compilar todo de cero.

## Cómo agregar o sacar pruebas

Todas las pruebas están en la carpeta **`tests/`**. `make test` toma todo lo que haya ahí adentro, así que:

- **Para probar un código nuevo:** crear un archivo en `tests/` (por ejemplo `tests/mi_prueba.txt`) con el programa y volver a correr `make test`. No hay que tocar el Makefile.
- **Para no correr una prueba:** sacar el archivo de `tests/` o moverlo a otra carpeta.

Algunas cosas a tener en cuenta:

- Los nombres de archivo no pueden tener espacios (`mi_prueba.txt` anda, `mi prueba.txt` no).
- Las pruebas se ejecutan en orden alfabético.
- Los `.java` y `.md` de esa carpeta se ignoran (hay un test unitario del léxico y un README).

