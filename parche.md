# Parche de validación MASM32

Documento de auditoría de los cambios aplicados sobre el commit base `e97cdf1a237cda2144b1e8848d43bf0db5151d99` (`main`) durante la validación real en Windows con MASM32 siguiendo el archivo `instrucciones`.

---

## 1. Entorno de validación

- **Sistema operativo:** Windows
- **JDK:** Java 25.0.1 (`javac --release 11`, bytecode verificado con `javap -v` -> `major version: 55` / Java 11)
- **Ensamblador:** `C:\masm32\bin\ml.exe` — Microsoft (R) Macro Assembler Version `6.14.8444`
- **Enlazador:** `C:\masm32\bin\link.exe` — Microsoft (R) Incremental Linker Version `5.12.8078`
- **SDK:** MASM32 v11 instalado en `C:\masm32`

---

## 2. Diagnóstico inicial (antes del parche)

Al ejecutar el pipeline original (`java -jar compilador.jar <test>.txt` seguido de `C:\masm32\bin\ml.exe /c /Zd /coff <base>.asm`), **las 11 pruebas fallaron en el ensamblado (`ml.exe` exit code `1`)** con el mismo patrón de errores:

```text
\masm32\include\masm32rt.inc(33) : warning A4011: multiple .MODEL directives found : .MODEL ignored
<archivo>.asm(8) : error A2005: symbol redefinition : printf
<archivo>.asm(<n>) : error A2148: invalid symbol type in expression : printf
```

### Causa raíz verificada en los headers de MASM32

1. **`C:\masm32\include\masm32rt.inc` (líneas 32–72)** ya incluye y declara:
   - `.486`
   - `.model flat, stdcall`
   - `option casemap :none`
   - `include \masm32\include\msvcrt.inc`
   - `include \masm32\macros\macros.asm`
   - `includelib \masm32\lib\masm32.lib`, `kernel32.lib` y `msvcrt.lib`

   Por lo tanto, emitir `.386`, `.model flat, stdcall`, `option casemap :none` y los `includelib` antes/después de `masm32rt.inc` provocaba la advertencia `A4011` por directiva `.MODEL` duplicada.

2. **`C:\masm32\macros\macros.asm` (línea 839)** define `printf` como **macro** (`printf MACRO format:REQ, args:VARARG`), que internamente delega en `crt_printf`.
   - Declarar luego `printf PROTO C :VARARG` en el `.asm` generado causaba `error A2005: symbol redefinition : printf`.
   - Usar `invoke printf, ...` sobre un símbolo que era una macro causaba `error A2148: invalid symbol type in expression : printf`.

3. **`C:\masm32\include\msvcrt.inc` (líneas 5 y 1930–1931)** ya declara correctamente la función importada de la CRT como:
   ```asm
   c_msvcrt typedef PROTO C :VARARG
   externdef _imp__printf:PTR c_msvcrt
   crt_printf equ <_imp__printf>
   ```
   Por lo tanto, `invoke crt_printf, cfm$(...), ...` es la forma nativa y compatible con `masm32rt.inc` y `msvcrt.lib`.

---

## 3. Cambios realizados

Se modificó únicamente **`src/compilador/GeneradorAssembler.java`** (y se regeneró **`compilador.jar`**).

### 3.1. Encabezado del ensamblador (`generarAssembler()`, líneas 691–692)
Se eliminaron las directivas duplicadas y los prototipos conflictivos/innecesarios (`dll_dllcrt0` y `printf`), dejando únicamente `include \masm32\include\masm32rt.inc`.

### 3.2. Llamadas de impresión en `POUT` (`generarCodigoAmbito()`, líneas 598–609)
Se reemplazó `invoke printf,` por `invoke crt_printf,` en las tres ramas (`CADENA`, `SHORTINT` y `SINGLEF`).

### 3.3. Llamadas de impresión en handlers de errores runtime (`generarAssembler()`, líneas 804–815)
Se reemplazó `invoke printf,` por `invoke crt_printf,` en los cuatro handlers:
- `_err_overflow_float`
- `_err_overflow_entero`
- `_err_perdida_info`
- `_err_recursion`

### 3.4. Diff completo (`git diff src/compilador/GeneradorAssembler.java`)

```diff
diff --git a/src/compilador/GeneradorAssembler.java b/src/compilador/GeneradorAssembler.java
index 1ce0d18..837dfd5 100644
--- a/src/compilador/GeneradorAssembler.java
+++ b/src/compilador/GeneradorAssembler.java
@@ -596,16 +596,16 @@ public class GeneradorAssembler {
                 }
                 String opAsm = resolverOperandoLectura(op1Raw, tipo, ambito, ambitoLimpio, i, 1, listaTercetos, sb);
                 if ("CADENA".equals(tipo)) {
-                    sb.append("    invoke printf, cfm$(\"%s\\n\"), OFFSET ").append(opAsm).append("\n");
+                    sb.append("    invoke crt_printf, cfm$(\"%s\\n\"), OFFSET ").append(opAsm).append("\n");
                 } else if ("SHORTINT".equals(tipo)) {
                     sb.append("    MOV AL, ").append(opAsm).append("\n");
                     sb.append("    MOVSX AX, AL\n");
                     sb.append("    MOV @aux_print_short, AX\n");
-                    sb.append("    invoke printf, cfm$(\"%hd\\n\"), @aux_print_short\n");
+                    sb.append("    invoke crt_printf, cfm$(\"%hd\\n\"), @aux_print_short\n");
                 } else if ("SINGLEF".equals(tipo)) {
                     sb.append("    FLD ").append(opAsm).append("\n");
                     sb.append("    FSTP @aux_print_float\n");
-                    sb.append("    invoke printf, cfm$(\"%Lf\\n\"), @aux_print_float\n");
+                    sb.append("    invoke crt_printf, cfm$(\"%Lf\\n\"), @aux_print_float\n");
                 }
             } else if (">".equals(op) || "<".equals(op) || ">=".equals(op) || "<=".equals(op) || "==".equals(op) || "!=".equals(op)) {
                 ultimoOpComp = op;
@@ -689,14 +689,7 @@ public class GeneradorAssembler {
         registrarAuxiliaresYConstantes(tercetosPorAmbito);
 
         StringBuilder encabezado = new StringBuilder();
-        encabezado.append(".386\n");
-        encabezado.append(".model flat, stdcall\n");
-        encabezado.append("option casemap :none\n");
-        encabezado.append("include \\masm32\\include\\masm32rt.inc\n");
-        encabezado.append("includelib \\masm32\\lib\\kernel32.lib\n");
-        encabezado.append("includelib \\masm32\\lib\\masm32.lib\n");
-        encabezado.append("dll_dllcrt0 PROTO C\n");
-        encabezado.append("printf PROTO C :VARARG\n\n");
+        encabezado.append("include \\masm32\\include\\masm32rt.inc\n\n");
 
         codigoData.append(".data\n");
         Map<String, Map<String, Object>> tabla = TablaSimbolos.obtenerTabla();
@@ -809,16 +802,16 @@ public class GeneradorAssembler {
 
         codigoCode.append("    invoke ExitProcess, 0\n\n");
         codigoCode.append("_err_overflow_float:\n");
-        codigoCode.append("    invoke printf, cfm$(\"%s\\n\"), OFFSET _msg_err_ovf_float\n");
+        codigoCode.append("    invoke crt_printf, cfm$(\"%s\\n\"), OFFSET _msg_err_ovf_float\n");
         codigoCode.append("    invoke ExitProcess, 1\n\n");
         codigoCode.append("_err_overflow_entero:\n");
-        codigoCode.append("    invoke printf, cfm$(\"%s\\n\"), OFFSET _msg_err_ovf_entero\n");
+        codigoCode.append("    invoke crt_printf, cfm$(\"%s\\n\"), OFFSET _msg_err_ovf_entero\n");
         codigoCode.append("    invoke ExitProcess, 1\n\n");
         codigoCode.append("_err_perdida_info:\n");
-        codigoCode.append("    invoke printf, cfm$(\"%s\\n\"), OFFSET _msg_err_perdida_tos\n");
+        codigoCode.append("    invoke crt_printf, cfm$(\"%s\\n\"), OFFSET _msg_err_perdida_tos\n");
         codigoCode.append("    invoke ExitProcess, 1\n\n");
         codigoCode.append("_err_recursion:\n");
-        codigoCode.append("    invoke printf, cfm$(\"%s\\n\"), OFFSET _msg_err_recursion\n");
+        codigoCode.append("    invoke crt_printf, cfm$(\"%s\\n\"), OFFSET _msg_err_recursion\n");
         codigoCode.append("    invoke ExitProcess, 1\n\n");
         codigoCode.append("end start\n");
```

---

## 4. Recompilación de `compilador.jar`

Comandos ejecutados desde la raíz del repositorio:

```powershell
New-Item -ItemType Directory -Force -Path bin | Out-Null
javac --release 11 -encoding UTF-8 -d bin (Get-ChildItem src\compilador\*.java, src\accion_semantica\*.java).FullName
Copy-Item -Recurse -Force src\estructuras bin\src\
& "C:\Program Files\Java\jdk-25\bin\jar.exe" --create --file compilador.jar --main-class src.compilador.Parser -C bin .
Remove-Item -Recurse -Force bin
```

Verificación de compatibilidad Java 11:
- `javap -v -cp compilador.jar src.compilador.GeneradorAssembler` -> `major version: 55` (Java 11).

---

## 5. Resultados de validación después del parche

Pipeline ejecutado para cada prueba (`<base>` sin `.txt`):
1. `java -jar compilador.jar <archivo>.txt`
2. `C:\masm32\bin\ml.exe /c /Zd /coff <base>.asm`
3. `C:\masm32\bin\link.exe /SUBSYSTEM:CONSOLE <nombre>.obj`
4. Ejecución de `.\<nombre>.exe` con verificación de salida estándar y exit code.

En todos los casos, `ml.exe` y `link.exe` terminaron con exit code `0`, **sin errores ni advertencias**.

### 5.1. Casos válidos (exit code esperado: `0`)

| Prueba | `java` | `ml.exe` | `link.exe` | Exit code | Salida obtenida | Verificación |
| :--- | :---: | :---: | :---: | :---: | :--- | :--- |
| `tests\test_ambitos_ok.txt` | 0 | 0 | 0 | `0` | `15` | OK (`5 + 10 = 15`) |
| `tests\test_tipos_y_aritmetica_ok.txt` | 0 | 0 | 0 | `0` | `Constantes:`, `10`, `-5`, `3.140000`, `-0.500000`, `2.250000`, `14`, `4`, `14`, `11.000000`, `2`, `6`, `11`, `6`, `10.000000`, `10.000000` | OK |
| `tests\test_control_ok.txt` | 0 | 0 | 0 | `0` | `1`, `2`, `10.000000`, `1`, `5`, `2.000000`, `14.000000`, `10`, `Fin de control de flujo ok` | OK |
| `tests\test_funciones_ok.txt` | 0 | 0 | 0 | `0` | `30`, `25`, `10.000000`, `9`, `15`, `0`, `21`, `Fin de pruebas funciones ok` | OK |
| `tests\test_comptime_ok.txt` | 0 | 0 | 0 | `0` | `5`, `25`, `4.000000`, `10` | OK |
| `tests\test_clases_ok.txt` | 0 | 0 | 0 | `0` | `42`, `50`, `Fin de pruebas clases ok` | OK |
| `tests\test_integrador.txt` | 0 | 0 | 0 | `0` | `0`, `3`, `Integracion Grupo 20finalizada exitosamente` | OK |

### 5.2. Controles runtime (exit code esperado: `1`)

| Prueba | `java` | `ml.exe` | `link.exe` | Exit code | Salida obtenida | Verificación |
| :--- | :---: | :---: | :---: | :---: | :--- | :--- |
| `tests\test_runtime_overflow_float.txt` | 0 | 0 | 0 | `1` | `Error en tiempo de ejecucion: Overflow en suma de punto flotante (SINGLEF)` | OK |
| `tests\test_runtime_overflow_entero.txt` | 0 | 0 | 0 | `1` | `Error en tiempo de ejecucion: Overflow en producto de enteros (SHORTINT)` | OK |
| `tests\test_runtime_perdida_tos.txt` | 0 | 0 | 0 | `1` | `Error en tiempo de ejecucion: Perdida de informacion en conversion tos()` | OK |
| `tests\test_runtime_recursion.txt` | 0 | 0 | 0 | `1` | `Error en tiempo de ejecucion: Recursion mutua o directa no permitida en funciones` | OK |

### 5.3. Prueba adicional de `pout` dentro de función

Se ejecutó una prueba temporal fuera del repositorio (`%TEMP%\test_pout_fn.txt`) invocando `pout("en funcion")` y `pout(x)` dentro de una función `shortint FUNCTION f(shortint x)` con `RET(x + 1$s)` llamado desde `main` con `41$s`.
- Resultado: `ml.exe` = `0`, `link.exe` = `0`, `.exe` exit code = `0`, salida = `en funcion`, `41`, `42`.
- Los archivos temporales de esta prueba fueron eliminados al finalizar.

---

## 6. Puntos evaluados que no requirieron cambios

1. **Declaraciones flotantes `DD` / `DQ` (`SINGLEF` y `@aux_print_float`):** `ml.exe 6.14.8444` aceptó `DD` con literales flotantes (`3.40282347E+38`, `2.5E38`, `3.14`, `-0.5`, etc.) y `FLD`/`FSTP` junto a `@aux_print_float DQ ?` sin errores ni advertencias, imprimiendo correctamente todos los valores flotantes. No fue necesario migrar a `REAL4`/`REAL8`.
2. **Prólogo/epílogo de subrutinas:** `crt_printf` dentro de funciones no corrompió la pila ni el retorno (`RET`), por lo que no fue necesario agregar `SUB ESP, 4 / PUSH EDI / PUSH ESI` ni `POP ESI / POP EDI`.

---

## 7. Limpieza y estado del árbol de trabajo

- Se eliminaron todos los archivos temporales generados durante las pruebas (`.asm`, `.obj`, `.exe` y la carpeta `bin/`).
- Archivos modificados/agregados en el repositorio:
  - `src/compilador/GeneradorAssembler.java` (modificado)
  - `compilador.jar` (recompilado con `--release 11`)
  - `parche.md` (agregado a pedido para auditoría)
- **Nota sobre `bin/` en `git status`:** aunque `bin/` figura en `.gitignore`, existían 24 archivos `.class` viejos trackeados desde un commit anterior (`be367f3c6f6938fcbe56cc7244ed9744f435baa8`). Al eliminar `bin/` como indica `instrucciones`, `git status` los muestra como `deleted` hasta que se decida restaurarlos (`git checkout -- bin`) o quitarlos del seguimiento de Git (`git rm -r --cached bin`).
