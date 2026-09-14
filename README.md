# Dev Container - Diseño de Compiladores I

Este contenedor trae instalado todo lo que necesitás para las materias de
compiladores basadas en Java + **byacc/j** (la variante de YACC que genera
parsers en Java en vez de C):

- JDK 21 (`java`, `javac`)
- **byacc/j** (comando `byaccj`) — genera `Parser.java` y `ParserVal.java`
  a partir de un archivo de gramática `.y`
- Extension Pack for Java en VS Code, ya preconfigurado

## Cómo abrir el proyecto

1. Necesitás **Docker Desktop** instalado y corriendo, y la extensión de
   VS Code **Dev Containers** (`ms-vscode-remote.remote-containers`).
2. Abrí esta carpeta en VS Code.
3. Apretá `Ctrl+Shift+P` → escribí **"Dev Containers: Reopen in Container"**.
4. La primera vez va a tardar un par de minutos: arma la imagen y instala
   `byacc-j` dentro. Las siguientes veces es instantáneo.
5. Al terminar, la terminal integrada de VS Code ya corre *adentro* del
   contenedor — `java -version`, `javac -version`, `byaccj -V` y `yacc -V`
   van a andar sin que tengas que instalar nada en tu Windows. Si ya tenías
   el contenedor creado antes de agregar una herramienta, ejecutá el comando
   **Dev Containers: Rebuild Container** desde la paleta de comandos.

## Flujo de trabajo típico (gramática → parser → compilado)

1. Escribís o editás tu archivo de gramática, por ejemplo `gramatica.y`
   (reglas yacc + acciones semánticas en Java entre `%{ %}`).
2. Generás el parser:
   ```
   yacc -J gramatica.y
   ```
   También podés usar `byaccj -J gramatica.y`: ambos comandos los instala el
   paquete `byacc-j`. Esto crea `Parser.java` y `ParserVal.java` en la carpeta
   actual. **No se editan a mano** — si cambiás la gramática, se regeneran.
3. Movés esos dos archivos generados a tu paquete (en el ejemplo,
   `src/compilador/`), junto con el resto de tus clases (`AnalizadorLexico`,
   `FileHelper`, tus acciones semánticas `AS0`, `AS1`, etc.).
4. Compilás todo el proyecto:
   ```
   javac -d bin $(find src -name "*.java")
   ```
5. Corrés el analizador (el prefijo del paquete depende de tu código,
   en el ejemplo es `compilador`):
   ```
   cd bin && java compilador.Parser
   ```
   Ojo con las rutas relativas: si tu código lee archivos como
   `src/matrizEstados.txt` (como en `FileHelper.java` del ejemplo), tenés
   que ejecutar `java` parado en la **raíz del proyecto**, no adentro de
   `bin`, para que esa ruta relativa exista.

## Carpeta `ejemplo-2021/`

Es el trabajo de un grupo de años anteriores que me pasaste, ya reorganizado
en la estructura estándar de un proyecto Java (`src/compilador`,
`src/accion_semantica`) para que puedas mirarlo como referencia de cómo se
arma un analizador léxico + sintáctico con esta herramienta. Dos cosas a
tener en cuenta:

- **No está completo**: solo subiste 3 de las clases de acción semántica
  (`AS0`, `AS1`, `AS6`) y falta la interfaz `AccionSemantica` — no va a
  compilar tal cual. Sirve para leer el enfoque, no para correrlo entero.
- `Parser.java` y `ParserVal.java` ya están generados (los generó
  `byaccj -J gramatica.y` — lo verifiqué regenerándolos yo mismo a partir
  de `gramatica.y` y el resultado es *idéntico* al que subiste, así que es
  exactamente el comando que necesitás para tu propia gramática).
- El informe (`Informe_tp_1_y_2_-_Compiladores_2021.docx`) explica las
  consignas y decisiones de diseño de ese grupo, útil para entender el
  criterio general de la materia.
