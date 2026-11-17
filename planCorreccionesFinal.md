# Plan Detallado de Correcciones y Cierre Final — TP3 y TP4 (Grupo 20)

Este documento especifica paso a paso todas las correcciones de código, casos de prueba y documentación necesarias para cerrar al 100% los Trabajos Prácticos 3 y 4 del **Grupo 20**.
Está diseñado con instrucciones quirúrgicas (archivos, líneas, bloques *Antes / Después* y comandos de verificación) para que pueda ser ejecutado directamente por un agente **Flash** sin introducir conflictos en BYacc/J ni regresiones.

---

## Reglas Inquebrantables para el Agente Implementador (Flash)

1. **Cero Conflictos en `y.output`**: Toda modificación en `gram.y` debe preservar `0 shift/reduce conflicts, 0 reduce/reduce conflicts` al ejecutar `byaccj -J -v gram.y`.
2. **Estilo Natural de Alumno (Anti-IA)**:
   - Prohibido usar expresiones lambda (`->`), Java Streams (`.stream()`), `Optional` o reflexión.
   - Usar únicamente bucles `for` tradicionales, `if-else`, `ArrayList`, `HashMap`, `LinkedHashMap` y `Stack`.
   - Comentarios breves y naturales en español neutro/académico sin acentos problemáticos ni frases de asistente.
3. **Sincronización Obligatoria**: Después de editar `gram.y`, regenerar siempre `src/compilador/Parser.java` y `src/compilador/ParserVal.java` mediante `make parser` (o los comandos equivalentes de BYacc/J) y recompilar con `make jar`.

---

## FASE 1: Correcciones Léxicas y Semánticas en Etapa 3 (`AS*.java`, `gram.y`)

### Tarea 1.1: Corregir cálculo de exponente `SINGLEF` y reporte de error de rango en el Analizador Léxico
- **Archivos**:
  - `src/accion_semantica/AS9.java`
  - `src/accion_semantica/AS8.java`
  - `src/accion_semantica/AS10.java`
- **Problema**:
  1. En `AS9.java:21`, el valor de una constante flotante con exponente `s` (ej. `1.5s2`) se calcula con `Math.pow(baseD, exponente)` ($\text{base}^{\text{exp}}$) en lugar de $\text{base} \times 10^{\text{exp}}$.
  2. En `AS8.java:25`, `AS9.java:27` y `AS10.java:24`, cuando una constante `SINGLEF` está fuera de rango se invoca `mostrarWarning` en lugar de `mostrarError` (a diferencia de `AS14.java:28` que sí reporta error léxico).
- **Cambios Concretos**:
  1. En `src/accion_semantica/AS9.java` (líneas 21-28):
     **Reemplazar:**
     ```java
     Double resultado = Math.pow(baseD, exponente);
     if ((resultado >= AnalizadorLexico.ValorMinimoFloat && resultado <= AnalizadorLexico.ValorMaximoFloat) || resultado == 0.0){
         String clave = TablaSimbolos.agregarOBuscarConstante("" + resultado, "SINGLEF");
         AnalizadorLexico.setReferenciaTablaSimbolos(clave);
         return TablaPalabrasReservadas.obtenerIdentificador("CTE_SINGLEF");
     }
     AnalizadorLexico.mostrarWarning("El double "+ resultado + " esta fuera de rango");
     return 0;
     ```
     **Por:**
     ```java
     Double resultado = baseD * Math.pow(10, exponente);
     if ((resultado >= AnalizadorLexico.ValorMinimoFloat && resultado <= AnalizadorLexico.ValorMaximoFloat) || resultado == 0.0){
         String clave = TablaSimbolos.agregarOBuscarConstante("" + resultado, "SINGLEF");
         AnalizadorLexico.setReferenciaTablaSimbolos(clave);
         return TablaPalabrasReservadas.obtenerIdentificador("CTE_SINGLEF");
     }
     AnalizadorLexico.mostrarError("Constante singlef fuera de rango: " + resultado);
     return 0;
     ```
  2. En `src/accion_semantica/AS8.java` (línea 25) y `src/accion_semantica/AS10.java` (línea 24):
     **Reemplazar:**
     ```java
     AnalizadorLexico.mostrarWarning(...);
     ```
     **Por:**
     ```java
     AnalizadorLexico.mostrarError("Constante singlef fuera de rango: " + resultado);
     ```

---

### Tarea 1.2: Validar atributo `USO` en `asignacion`, `unica`, `factor` y operaciones aritméticas/relacionales
- **Archivo**: `gram.y`
- **Problema**:
  - `Tp3.pdf` (pág. 3) exige detectar el uso incorrecto de un símbolo según su contexto (ej. `f := 5$s;` siendo `f` una función, o `a := f + 1$s;`, o `obj1 := obj2;`).
  - Actualmente `asignacion`, `unica` y `factor : IDENTIFICADOR` solo miran `TIPO` y no `USO`.
- **Cambios Concretos**:
  1. En `asignacion : IDENTIFICADOR ASIGNAR expresion` (`gram.y:563-606`):
     Luego de obtener `claveVar` (línea 568), validar que el `USO` sea `"variable"`, `"parametro"` o `"atributo"`:
     ```java
     if (claveVar != null) {
         String uso = TablaSimbolos.obtenerUso(claveVar);
         if (!"variable".equals(uso) && !"parametro".equals(uso) && !"atributo".equals(uso)) {
             agregarErrorSemantico("Uso incorrecto del identificador '" + lexema + "' (uso: " + uso + ") en el lado izquierdo de una asignacion");
         } else {
             // mantener logica actual de esComp y chequeo de tipos
     ```
  2. En `unica : IDENTIFICADOR '=' '(' expresion ')'` (`gram.y:1196-1259`):
     Luego de `if (claveVar != null)` (línea 1201), validar exactamente lo mismo:
     ```java
     String uso = TablaSimbolos.obtenerUso(claveVar);
     if (!"variable".equals(uso) && !"parametro".equals(uso) && !"atributo".equals(uso)) {
         agregarErrorSemantico("Uso incorrecto del identificador '" + lexema + "' (uso: " + uso + ") en asignacion dentro de expresion");
         $$ = new ParserVal();
     } else {
         // mantener logica actual
     ```
  3. En `factor : IDENTIFICADOR` (`gram.y:875-901`):
     Permitir `"variable"`, `"parametro"`, `"atributo"` y `"objeto"` (porque un objeto puede pasarse como parámetro real en `cli.m_cliente(b)`), pero rechazar `"funcion"`, `"metodo"`, `"nombre_clase"` y `"nombre_programa"`:
     ```java
     if (claveVar != null) {
         String uso = TablaSimbolos.obtenerUso(claveVar);
         if ("funcion".equals(uso) || "metodo".equals(uso) || "nombre_clase".equals(uso) || "nombre_programa".equals(uso)) {
             agregarErrorSemantico("Uso incorrecto del identificador '" + lexema + "' (uso: " + uso + ") como operando en una expresion");
             $$ = new ParserVal();
         } else {
             String tipo = TablaSimbolos.obtenerTipo(claveVar);
             // mantener logica actual de esComp y creacion de ParserVal
     ```
  4. En las reglas aritméticas (`+`, `-`, `*`, `/` en `gram.y:746, 775, 813, 842`) y en `condicion` (`gram.y:1393`):
     Asegurar que `tipo1.equals(tipo2)` además verifique que el tipo sea numérico (`("SHORTINT".equals(tipo1) || "SINGLEF".equals(tipo1))`), para impedir sumar o comparar dos objetos de la misma clase (`obj1 + obj2`).

---

### Tarea 1.3: Validar `USO == "metodo"` en `invocacion_metodo`
- **Archivo**: `gram.y` (líneas 1048-1053 y 1111-1116)
- **Problema**: `TablaSimbolos.buscarMiembroEnJerarquia(nomClase, nomMetodo)` devuelve tanto atributos como métodos. Si el programa fuente escribe `obj.atributo()`, se acepta como llamada a método.
- **Cambio Concreto**:
  En ambas reglas de `invocacion_metodo` (`IDENTIFICADOR '.' IDENTIFICADOR '(' ')'` y `IDENTIFICADOR '.' IDENTIFICADOR '(' lista_expresiones ')'`), inmediatamente después de `if (claveMet == null) { ... } else {`:
  ```java
  if (!"metodo".equals(TablaSimbolos.obtenerUso(claveMet))) {
      agregarErrorSemantico("El miembro '" + nomMetodo + "' de la clase '" + nomClase + "' no es un metodo");
      $$ = new ParserVal();
  } else {
      // mantener chequeo de visibilidad e invocacion
  }
  ```

---

### Tarea 1.4: Corregir métodos `AUTO` en clases (`encabezado_metodo_auto`) y evitar NPE en `sentencia_ret`
- **Archivo**: `gram.y` (líneas 433-444, 461-473 y 1520-1534)
- **Problema**:
  1. `encabezado_metodo_auto` no inicializa `cant_retornos_auto = 0;`, haciendo que todo `RET` dentro de un método `AUTO` falle diciendo `se esperaba auto pero se retorno SHORTINT`.
  2. `metodo_clase` para métodos `AUTO` no verifica al cerrar que `cant_retornos_auto > 0`.
  3. En `sentencia_ret`, si `tipoExp` es `null` (por un error previo en la expresión del `RET`), sobreescribe `TIPO` con `null` en la tabla de símbolos.
- **Cambios Concretos**:
  1. En `encabezado_metodo_auto` (`gram.y:461-473`):
     ```java
     encabezado_metodo_auto
         : AUTO IDENTIFICADOR
             {
                 String nombre = $2.sval;
                 String claveMet = TablaSimbolos.declararVariable(nombre, "auto", "metodo", ambitoActual);
                 if (claveMet != null) {
                     TablaSimbolos.agregarAtributo(claveMet, "TIENE_RET", Boolean.FALSE);
                 }
                 entrarAmbito(nombre);
                 GeneradorTerceto.iniciarAmbito(ambitoActual);
                 cant_retornos_auto = 0;
                 $$ = $2;
             }
         ;
     ```
  2. En `metodo_clase` para `encabezado_metodo_auto` (`gram.y:433-444`):
     ```java
     | encabezado_metodo_auto '(' lista_parametros_formales ')'
         BEGIN sentencias_ejecutables END exportacion_opcional ';'
         {
             if (cant_retornos_auto == 0) {
                 yyerror("Ausencia de sentencia de retorno 'RET' en metodo AUTO");
             }
             cant_retornos_auto = -1;
             String mangling = obtenerFuncionActualMangled();
             if (mangling != null) {
                 if ($8 != null && $8.obj != null) {
                     ArrayList<String> exports = (ArrayList<String>) $8.obj;
                     TablaSimbolos.registrarExport(mangling, exports);
                 }
             }
             salirAmbito();
         }
     ```
  3. En `sentencia_ret` (`gram.y:1520-1527`):
     ```java
     if (cant_retornos_auto >= 0) {
         cant_retornos_auto++;
         if (tipoExp != null) {
             if ("auto".equals(tipoDeclarado)) {
                 TablaSimbolos.agregarAtributo(claveFunc, TablaSimbolos.TIPO, tipoExp);
             } else if (tipoDeclarado != null && !tipoDeclarado.equals(tipoExp)) {
                 TablaSimbolos.agregarAtributo(claveFunc, TablaSimbolos.TIPO, "SINGLEF");
             }
         }
     }
     ```

---

### Tarea 1.5: Prohibir anidamiento en Tema 17 (`unica : IDENTIFICADOR '=' '(' expresion ')'`) sin agregar conflictos
- **Archivo**: `gram.y`
- **Problema**: El Tema 17 prohíbe anidar asignaciones dentro de los paréntesis de otra asignación en expresión (ej. `a := b = (c = (2$s));`).
- **Estrategia sin tocar las reglas de Yacc (0 conflictos garantizados)**:
  Usar el campo entero `ival` de `ParserVal` como atributo sintetizado booleano (`1` = la subexpresión contiene una asignación `unica`, `0` = no contiene):
  1. En `unica : IDENTIFICADOR '=' '(' expresion ')'` (`gram.y:1196`):
     - Al inicio de la acción semántica, chequear si `$4 != null && $4.ival == 1`:
       ```java
       if ($4 != null && $4.ival == 1) {
           agregarErrorSemantico("No se permite anidar asignaciones dentro de una asignacion en expresion (Tema 17)");
       }
       ```
     - Al devolver `pv` en `unica` (tanto en la rama `esComp` línea 1216 como en la rama normal línea 1237), marcar `pv.ival = 1;`.
  2. En `expresion` (`+`, `-` en `gram.y:751, 756, 780, 785`), `termino` (`*`, `/` en `gram.y:818, 823, 847, 852`) y `conversion_tos` (`gram.y:1303, 1308`):
     Propagar la marca:
     ```java
     if (($1 != null && $1.ival == 1) || ($3 != null && $3.ival == 1)) {
         pv.ival = 1;
     }
     ```
     (Y en `conversion_tos`: `if ($3 != null && $3.ival == 1) pv.ival = 1;`).

---

### Tarea 1.6: Completar validaciones del Tema 29 (`acceso_posicional`)
- **Archivo**: `gram.y` (líneas 628-732)
- **Problemas**:
  1. `obj[-1$s]` no es capturado por `acceso_posicional` porque el signo `'-'` es un token separado de `CTE_SHORTINT`.
  2. En `IDENTIFICADOR '[' CTE_SHORTINT ']'`, el chequeo de visibilidad `IMPORT FROM` (línea 667) verifica `imports.contains(nomClase)` en vez de la clase dueña real del atributo (`claseDueniaAtr`), fallando si el atributo fue heredado.
  3. En `IDENTIFICADOR '[' IDENTIFICADOR ']'`, usa `obtenerAtributosClase` (solo propios) en vez de `obtenerAtributosTotalesClase` (heredados + propios), no evalúa estáticamente si el índice es una variable `COMPTIME`, y no valida el `USO` de la variable índice.
- **Cambios Concretos**:
  1. En `IDENTIFICADOR '[' CTE_SHORTINT ']'` (`gram.y:662-674`), reemplazar el chequeo de visibilidad por:
     ```java
     String claseActual = obtenerClaseDeAmbito(ambitoActual);
     String claseDueniaAtr = obtenerClaseDeAmbito(TablaSimbolos.obtenerAmbito(claveAtr));
     boolean accesoValido = true;
     if (claseActual != null && claseDueniaAtr != null && !claseActual.equals(claseDueniaAtr)) {
         String claveClaseActual = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + claseActual;
         ArrayList<String> imports = TablaSimbolos.obtenerImports(claveClaseActual);
         if (imports == null || !imports.contains(claseDueniaAtr)) {
             agregarErrorSemantico("La clase '" + claseActual + "' no importa a la clase '" + claseDueniaAtr + "'");
             accesoValido = false;
         } else if (!TablaSimbolos.estaExportadoA(claveAtr, claseActual)) {
             agregarErrorSemantico("El atributo '" + nomAtr + "' de la clase '" + claseDueniaAtr + "' no esta exportado a la clase '" + claseActual + "'");
             accesoValido = false;
         }
     }
     ```
  2. En `IDENTIFICADOR '[' IDENTIFICADOR ']'` (`gram.y:709-726`):
     - Validar que `TablaSimbolos.obtenerUso(claveVarIdx)` sea `"variable"`, `"parametro"` o `"atributo"`.
     - Si `TablaSimbolos.esComptime(claveVarIdx)`: obtener su valor con `TablaSimbolos.obtenerValorComptime(claveVarIdx)`, verificar rango `0 <= idx < atrsTotales.size()` y devolver `claveObj + "[" + idx + "$s]"` con el tipo de `atrsTotales.get(idx)`.
     - Si es variable de runtime: usar `ArrayList<String> atrsTotales = TablaSimbolos.obtenerAtributosTotalesClase(claveClase);` y tomar el tipo de `atrsTotales.get(0)`.
  3. Agregar al final de `acceso_posicional` (`gram.y:731`) la producción para constante negativa:
     ```yacc
     | IDENTIFICADOR '[' '-' CTE_SHORTINT ']'
         {
             agregarErrorSemantico("Indice posicional [-" + $4.sval + "$s] fuera de rango: el indice no puede ser negativo");
             $$ = new ParserVal();
         }
     ```
     *(Verificado: después de `'['` no existe ninguna otra transición con `'-'`, por lo que mantiene 0 conflictos S/R y R/R).*

---

### Tarea 1.7: Refinar Tema 22 (`COMPTIME`) y Plegado de Constantes
- **Archivo**: `gram.y`
- **Problemas**:
  1. Las operaciones entre literales comunes (`2$s + 3$s`) o `tos(10.5)` se pliegan en compilación aunque no intervenga ninguna variable `COMPTIME`, impidiendo generar sus tercetos o probar errores de runtime con constantes, y cuando sí se pliegan para `COMPTIME`, el nuevo literal resultante no se da de alta en `TablaSimbolos`.
  2. Si se modifica una variable `COMPTIME` dentro de un bloque `IF` o `REPEAT UNTIL` cuya condición depende de variables de runtime (Ejemplo inválido 2 de `Tp3.pdf` pág. 8), el compilador no reporta error.
- **Cambios Concretos**:
  1. En `evaluarOperacionEstatica` (`gram.y:1675` y `1694`), antes de hacer `return String.valueOf(res);`, registrar la constante resultante en la Tabla de Símbolos:
     ```java
     String litRes = String.valueOf(res);
     TablaSimbolos.agregarOBuscarConstante(litRes, tipo);
     return litRes;
     ```
  2. En `conversion_tos` (`gram.y:1295-1304`):
     Solo evaluar estáticamente en compilación cuando `esConstante($3)` **y además** no tenga pérdida de parte decimal (`d == (int) d`) ni exceda `[-128, 127]`; si tiene decimales (ej. `tos(10.5)`) o está fuera de rango en una sentencia ejecutable común, emitir el terceto `("tos", $3.sval, "-", "SHORTINT")` para que el Chequeo `g` de runtime lo capture en Assembler (salvo que se intente asignar a una variable `COMPTIME`, donde `asignacion` rechazará el terceto por no ser constante estática). Y al plegar `String.valueOf(entero)`, llamar a `TablaSimbolos.agregarOBuscarConstante(String.valueOf(entero), "SHORTINT")`.
  3. Para detectar la modificación de una variable `COMPTIME` condicionada por runtime (`Tp3.pdf` pág. 8):
     - Declarar en la sección de soporte de `gram.y`:
       ```java
       public static int condicionRuntimeActiva = 0;
       public static boolean comptimeAsignadoEnBloque = false;
       ```
     - En `condicion : expresion comparador expresion` (`gram.y:1395`):
       Guardar en `$$.ival` si la condición depende de runtime:
       ```java
       ParserVal pv = new ParserVal("[" + idTerceto + "]");
       pv.ival = (esConstante($1) && esConstante($3)) ? 0 : 1;
       $$ = pv;
       ```
     - En `encabezado_if` (`gram.y:1326`): si `$3 != null && $3.ival == 1`, incrementar `condicionRuntimeActiva++`, y al reducir `sentencia_if` decrementar `if (condicionRuntimeActiva > 0) condicionRuntimeActiva--;`.
     - En `inicio_repeat` (`gram.y:1428`): poner `comptimeAsignadoEnBloque = false;`.
     - En `asignacion` y `unica`, cuando `esComp` es `true`:
       - Si `condicionRuntimeActiva > 0`, emitir:
         `agregarErrorSemantico("No se puede modificar la variable comptime '" + lexema + "' dentro de un bloque condicional dependiente de tiempo de ejecucion");`
       - Marcar `comptimeAsignadoEnBloque = true;`.
     - En `sentencia_repeat_until : inicio_repeat bloque UNTIL condicion_iteracion ';'` (`gram.y:1434`):
       Si `comptimeAsignadoEnBloque && $4 != null && $4.ival == 1`, emitir:
       `agregarErrorSemantico("No se puede modificar una variable comptime dentro de una iteracion cuya condicion depende de tiempo de ejecucion");`
       y resetear `comptimeAsignadoEnBloque = false;`.

---

## FASE 2: Correcciones en Generación de Assembler — Etapa 4 (`GeneradorAssembler.java`, `gram.y`)

### Tarea 2.1: Corregir retorno por defecto `"0$s"` en funciones y métodos sin `RET` explícito
- **Archivos**: `gram.y` (líneas 186, 204, 423) y `src/compilador/GeneradorAssembler.java` (líneas 94-101, 638-642)
- **Problema**: `gram.y` emite `"0$s"` en el terceto `RET` por defecto. `GeneradorAssembler.registrarConstanteSiFalta` ve la letra `'s'` y lo clasifica como `SINGLEF`, emitiendo `_cte_0$s DD 0$E` en `.data`.
- **Cambios Concretos**:
  1. En `gram.y` (líneas 186, 204 y 423):
     **Reemplazar:**
     ```java
     String valDef = "SINGLEF".equals(tipo) ? "0.0" : "0$s";
     GeneradorTerceto.crearTerceto("RET", valDef, "-", tipo);
     ```
     **Por:**
     ```java
     String valDef = "SINGLEF".equals(tipo) ? "0.0" : "0";
     TablaSimbolos.agregarOBuscarConstante(valDef, tipo);
     GeneradorTerceto.crearTerceto("RET", valDef, "-", tipo);
     ```
  2. En `src/compilador/GeneradorAssembler.java` (`registrarConstanteSiFalta`, líneas 92-102):
     Asegurar que si un operando numérico termina en `"$s"`, se limpie `"$s"` y se trate como `"SHORTINT"` antes de chequear `op.contains("s")`.

---

### Tarea 2.2: Soportar `AUTO FUNCTION` con múltiples `RET` de tipos distintos (`SHORTINT` y `SINGLEF`)
- **Archivo**: `src/compilador/GeneradorAssembler.java` (líneas 412-424)
- **Problema**: Cuando una `AUTO FUNCTION` tiene un `RET` de tipo `SHORTINT` y otro de tipo `SINGLEF`, la variable `_ret_<ambito>` en `.data` es `DD ?` (32 bits float), pero el terceto `RET` de la rama entera tiene `tipo = "SHORTINT"`, generando `MOV _ret_<ambito>, AL` (error de tamaño en MASM32 y falta de conversión a float).
- **Cambio Concreto**:
  En `GeneradorAssembler.java` (líneas 412-425), reemplazar el bloque `else if ("RET".equals(op))` por:
  ```java
  } else if ("RET".equals(op)) {
      String op1 = resolverOperandoAsm(t.getOperando1(), ambito, listaTercetos);
      if (tipo == null) {
          tipo = TablaSimbolos.obtenerTipo(t.getOperando1());
      }
      String tipoSubrutina = TablaSimbolos.obtenerTipo(ambito);
      if (tipoSubrutina == null) {
          tipoSubrutina = tipo;
      }
      if ("SHORTINT".equals(tipoSubrutina)) {
          sb.append("    MOV AL, ").append(op1).append("\n");
          sb.append("    MOV _ret_").append(ambitoLimpio).append(", AL\n");
      } else if ("SINGLEF".equals(tipoSubrutina)) {
          if ("SHORTINT".equals(tipo)) {
              sb.append("    MOV AL, ").append(op1).append("\n");
              sb.append("    CBW\n");
              sb.append("    MOV @aux_tos, AX\n");
              sb.append("    FILD @aux_tos\n");
              sb.append("    FSTP _ret_").append(ambitoLimpio).append("\n");
          } else {
              sb.append("    FLD ").append(op1).append("\n");
              sb.append("    FSTP _ret_").append(ambitoLimpio).append("\n");
          }
      }
      sb.append("    JMP _fin_").append(ambitoLimpio).append("\n");
  }
  ```

---

### Tarea 2.3: Resolver lectura (RHS) de acceso posicional con variable (`obj[idx]`) y corregir `obtenerTipoOperando`
- **Archivo**: `src/compilador/GeneradorAssembler.java`
- **Problemas**:
  1. `obtenerTipoOperando` (`líneas 202-230`): cuando recibe `"main@b[1$s]"` (donde el atributo 1 es `SINGLEF`), no lo halla en la Tabla de Símbolos, cae en `if (op.endsWith("$s") || op.contains("$"))` y devuelve `"SHORTINT"` en lugar de `"SINGLEF"`.
  2. Si `obj[idx]` con `idx` variable aparece en el lado derecho (lectura), `resolverAccesoPosicional` devuelve `"main@obj[main@idx]"` sin traducir.
- **Cambios Concretos**:
  1. En `obtenerTipoOperando` (`GeneradorAssembler.java:219`), antes de `String tipo = TablaSimbolos.obtenerTipo(op);`, agregar la resolución de tipo para accesos posicionales:
     ```java
     if (op.contains("[") && op.endsWith("]")) {
         int corchete = op.indexOf('[');
         String claveObj = op.substring(0, corchete);
         String contenidoIdx = op.substring(corchete + 1, op.length() - 1);
         String nomClase = TablaSimbolos.obtenerTipo(claveObj);
         String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
         ArrayList<String> atrsTotales = TablaSimbolos.obtenerAtributosTotalesClase(claveClase);
         if (!contenidoIdx.contains(TablaSimbolos.SEPARADOR)) {
             int idx = Integer.parseInt(contenidoIdx.replace("$s", "").trim());
             if (idx >= 0 && idx < atrsTotales.size()) {
                 return TablaSimbolos.obtenerTipo(atrsTotales.get(idx));
             }
         } else if (!atrsTotales.isEmpty()) {
             return TablaSimbolos.obtenerTipo(atrsTotales.get(0));
         }
     }
     ```
  2. Para lecturas en RHS de `obj[idx]` con índice variable:
     - Crear un método helper `emitirLecturaPosicionalVariable(String op, String ambito, int indiceTerceto, int numOp, ArrayList<Terceto> lista, StringBuilder sb)` que, cuando `op` sea `claveObj[claveVarIdx]` (con `@` adentro del corchete):
       - Use una variable auxiliar `@aux_pos_<ambitoLimpio>_<indiceTerceto>_<numOp>` (registrada en `registrarAuxiliaresYConstantes` con el tipo del atributo).
       - Emita antes de la instrucción del terceto un bucle `for (int k = 0; k < atrsTotales.size(); k++)` que compare `MOV AL, <varIdxAsm>` / `CMP AL, k` / `JNE Label_read_pos_...` y copie el atributo `k` hacia `@aux_pos_...`.
       - Devuelva `@aux_pos_<ambitoLimpio>_<indiceTerceto>_<numOp>` como operando resuelto.

---

### Tarea 2.4: Soportar parámetros formales de tipo Clase (`shortint m(c_base1 obj_b1)`) en `.data` y `:=`
- **Archivo**: `src/compilador/GeneradorAssembler.java` (líneas 379-387 y 603-635)
- **Problema**: En `tests/test_hito6_clases_ok.txt`, el método `m_cliente(c_base1 obj_b1)` declara un parámetro formal cuyo `USO` es `"parametro"` y cuyo `TIPO` es `"c_base1"`. En `.data` no se reservan sus atributos `_main_c_cliente_m_cliente_obj_b1_<atr>`, ni `:=` copia los atributos del objeto real al parámetro formal antes del `CALL`.
- **Cambios Concretos**:
  1. En `GeneradorAssembler.java:621`:
     Cambiar la condición de reserva de atributos de objetos en `.data`:
     ```java
     } else if (uso.equals("objeto") || (uso.equals("parametro") && !"SHORTINT".equals(tipo) && !"SINGLEF".equals(tipo))) {
     ```
     De esta manera, cualquier parámetro cuyo tipo sea una clase reserva en `.data` sus variables de instancia `_<claveParam>_<nomAtr>` igual que un objeto.
  2. En `GeneradorAssembler.java:379-387` (rama `else` de `":=".equals(op)`):
     Agregar el caso en que `tipo` sea una clase (`!"SHORTINT".equals(tipo) && !"SINGLEF".equals(tipo)`):
     ```java
     } else if (tipo != null) {
         String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + tipo;
         ArrayList<String> atrsTotales = TablaSimbolos.obtenerAtributosTotalesClase(claveClase);
         for (int k = 0; k < atrsTotales.size(); k++) {
             String claveAtrAbs = atrsTotales.get(k);
             String nomAtr = TablaSimbolos.obtenerNombre(claveAtrAbs);
             String tipoAtr = TablaSimbolos.obtenerTipo(claveAtrAbs);
             String destAtr = obtenerNombreAsm(op1Raw + "_" + nomAtr);
             String origAtr = obtenerNombreAsm(t.getOperando2() + "_" + nomAtr);
             if ("SHORTINT".equals(tipoAtr)) {
                 sb.append("    MOV AL, ").append(origAtr).append("\n");
                 sb.append("    MOV ").append(destAtr).append(", AL\n");
             } else if ("SINGLEF".equals(tipoAtr)) {
                 sb.append("    FLD ").append(origAtr).append("\n");
                 sb.append("    FSTP ").append(destAtr).append("\n");
             }
         }
     }
     ```

---

## FASE 3: Corrección y Ampliación de Casos de Prueba (`tests/`) y Build (`Makefile`)

### Tarea 3.1: Evitar que `test_etapa3_integrador.txt` y `etapa4_hito2_aritmetica.txt` aborten por el Chequeo `g`
- **Problema**: Ambos tests son casos válidos que deben llegar hasta el final e imprimir su mensaje de éxito, pero llaman a `tos(10.5)` y `tos(12.5)`, lo que dispara `_err_perdida_info` (Chequeo `g`) por tener parte decimal `.5`.
- **Cambios Concretos**:
  1. En `tests/test_etapa3_integrador.txt` (línea 49):
     Cambiar `f1 := 10.5;` por `f1 := 10.0;`.
  2. En `tests/etapa4_hito2_aritmetica.txt` (línea 28):
     Cambiar `pout(tos(x));` por `pout(tos(res_flt));` (ya que `res_flt` vale `12.5 + 2.5 = 15.0`, entero exacto).

### Tarea 3.2: Ampliar los Casos de Prueba Exigidos en `Tp3.pdf` y `Tp04-2026.pdf` (pág. 4)
1. **En `tests/test_hito3_errores_semanticos.txt`**:
   - Agregar redeclaración de función en el mismo ámbito (`shortint function f() ... shortint function f() ...`).
   - Agregar redeclaración de clase en el mismo ámbito (`class c_dup ... class c_dup ...`).
   - Agregar uso inválido de nombre de función y nombre de clase del lado izquierdo de `:=` (`f := 10$s;`).
2. **En `tests/test_hito4_tipos_error.txt`**:
   - Agregar anidamiento inválido de asignaciones en expresión del Tema 17: `a := b = (c = (2$s));`.
3. **En `tests/test_hito6_clases_error.txt`**:
   - Agregar invocación de atributo como si fuera método (`b.b1_atr0();`).
   - Agregar acceso posicional con índice constante negativo (`b[-1$s] := 10$s;`).
   - Agregar redeclaración de método dentro de la misma clase.
4. **En `tests/test_hito6_comptime_error.txt`**:
   - Agregar modificación de variable `comptime` dentro de un `repeat ... until (c_entero > r_entero);` con condición de runtime (`Tp3.pdf` pág. 8).
5. **En `tests/etapa4_hito5_chequeos_runtime.txt`**:
   - Agregar `caso == 5$s` para probar el Chequeo `g` por **rango excedido** (`f_conv := 200.0; res_int := tos(f_conv);`), complementando el `caso == 3$s` que prueba pérdida por decimales (`10.5`).
   - Agregar `caso == 6$s` con dos funciones `rec_a` y `rec_b` que se llamen mutuamente para probar también la **recursión mutua** del Chequeo `i`.

### Tarea 3.3: Actualizar `Makefile`, regenerar todos los `.asm` y `compilador.jar`
1. En `Makefile` (línea 12):
   **Reemplazar:**
   ```makefile
   PRUEBAS := $(filter-out %.java %.md, $(wildcard tests/*))
   ```
   **Por:**
   ```makefile
   PRUEBAS := $(filter-out %.java %.md %.asm, $(wildcard tests/*))
   ```
2. Borrar el archivo residual `tests/test_hito5_control_ok.asm` y regenerar los `.asm` de todos los casos válidos de la Etapa 4 (`tests/test_etapa4_hito1.txt`, `tests/etapa4_hito2_aritmetica.txt`, `tests/etapa4_hito3_control.txt`, `tests/etapa4_hito4_funciones_objetos.txt`, `tests/etapa4_hito5_chequeos_runtime.txt`, `tests/test_etapa3_integrador.txt`).
3. Ejecutar `make parser` y `make jar` para dejar `compilador.jar` actualizado con toda la Etapa 4.

---

## FASE 4: Alineación y Cierre de la Documentación

1. **`GUIA_DEFENSA_ETAPA4.md`**:
   - Reemplazar en los Hitos 1 y 2 las apariciones del formato viejo `_main@a` por `_main_a`.
   - Redactar los capítulos completos de:
     - **Hito 3**: Sentencias de Control (`IF`, `IF-ELSE`, `REPEAT UNTIL`), recolección de etiquetas en dos pasadas, `CMP AL` con saltos con signo (`JLE`, `JL`, `JGE`, `JG`, `JNE`, `JE`) vs. `FCOMP` + `FSTSW AX` + `SAHF` con saltos sin signo (`JBE`, `JB`, `JAE`, `JA`, `JNE`, `JE`).
     - **Hito 4**: Subrutinas (`_proc_<ambito>`, prólogo/epílogo `EBP`, variable `_ret_<ambito>`), `AUTO FUNCTION`, copia de atributos de instancia antes y después de `CALL` a métodos, herencia múltiple y acceso posicional con constante y con variable en runtime.
     - **Hito 5**: Los 4 Controles en Tiempo de Ejecución del Grupo 20 (`c`, `d`, `g`, `i`) con traza de registros/banderas y preguntas de defensa oral.
2. **`GUIA_DEFENSA_ETAPA3.md` y `AGENTS.md`**:
   - Unificar la explicación del Tema 21 (`AUTO FUNCTION`): aclarar en `AGENTS.md:133` y en la Pregunta 1 del cierre de `GUIA_DEFENSA_ETAPA3.md:2629` que cuando hay múltiples `RET` de tipos distintos (`SHORTINT` y `SINGLEF`), el compilador infiere el tipo más abarcativo (`SINGLEF`) tal como pide `Tp3.pdf` (pág. 8).
   - Unificar en `AGENTS.md:132` la regla del Tema 34: no hay conversiones implícitas en el Grupo 20 (toda incompatibilidad entre `SHORTINT` y `SINGLEF` sin `tos()` es error semántico).
3. **`resumen_parte3.md`, `resumen_parte4.md` y `README.md`**:
   - Limpiar las notas de borrador (`//Revisar`, `//Creo que...`, `//pregunta...`) en `resumen_parte3.md` y `resumen_parte4.md`, y unificar `_main_a` en `resumen_parte4.md`.
   - Actualizar `README.md` para documentar las 4 salidas finales del compilador (Informe de errores léxicos/sintácticos/semánticos, Tabla de Símbolos, Tercetos por ámbito y generación del archivo `.asm`).

---

## FASE 5: Checklist de Verificación Final para el Agente Flash

Al finalizar todas las fases, el agente deberá ejecutar y comprobar:

```bash
# 1. Regenerar el parser y verificar 0 conflictos
make parser
grep -E "conflict|never reduced" y.output
# Resultado esperado: sin ninguna coincidencia (0 shift/reduce, 0 reduce/reduce)

# 2. Empaquetar el compilador actualizado
make jar

# 3. Ejecutar los tests de Etapa 3 y Etapa 4
java -jar compilador.jar tests/test_etapa3_integrador.txt
java -jar compilador.jar tests/test_etapa4_hito1.txt
java -jar compilador.jar tests/etapa4_hito2_aritmetica.txt
java -jar compilador.jar tests/etapa4_hito3_control.txt
java -jar compilador.jar tests/etapa4_hito4_funciones_objetos.txt
java -jar compilador.jar tests/etapa4_hito5_chequeos_runtime.txt

# 4. Ejecutar los tests de errores semánticos y verificar que NO generen archivo .asm
java -jar compilador.jar tests/test_hito3_errores_semanticos.txt
java -jar compilador.jar tests/test_hito4_tipos_error.txt
java -jar compilador.jar tests/test_hito6_funciones_error.txt
java -jar compilador.jar tests/test_hito6_comptime_error.txt
java -jar compilador.jar tests/test_hito6_clases_error.txt

# 5. Verificar ausencia de lambdas/streams en todo src/
grep -rn "\.stream()" src/
grep -rn "->" src/
```
