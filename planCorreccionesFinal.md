# Plan de Correcciones Validadas — TP3 y TP4 (Grupo 20)

Este documento reúne únicamente las correcciones reales y validadas contra los enunciados oficiales (`Tp1.pdf`, `Tp2.pdf`, `Tp3.pdf`, `Tp04-2026.pdf`) y `teoria/grupo20.y`, descartando falsos positivos y sobreingeniería innecesaria.

---

## Reglas de Implementación

1. **Cero Conflictos en `y.output`**: Toda modificación en `gram.y` debe mantener `0 shift/reduce conflicts, 0 reduce/reduce conflicts` al ejecutar `make parser`.
2. **Estilo Natural de Alumno**: Código Java imperativo tradicional (`for`, `if-else`, `ArrayList`, `HashMap`, `LinkedHashMap`, `Stack`), sin lambdas (`->`) ni Java Streams (`.stream()`).
3. **Sincronización**: Luego de editar `gram.y`, regenerar `Parser.java` y `ParserVal.java` con `make parser` y recompilar con `make jar`.

---

## FASE 1: Correcciones Léxicas y Semánticas (`AS*.java`, `gram.y`)

### Tarea 1.1: Reportar error léxico cuando una constante `SINGLEF` está fuera de rango
- **Archivos**: `src/accion_semantica/AS8.java`, `src/accion_semantica/AS9.java`, `src/accion_semantica/AS10.java`
- **Problema**: Cuando un flotante excede el rango permitido se invoca `mostrarWarning(...)` en lugar de `mostrarError(...)` (a diferencia de `AS14.java`).
- **Nota**: No modificar el cálculo `Math.pow(baseD, exponente)` de `AS9.java`, ya que la convención utilizada en los tests existentes (`1.5s2` = `2.25`, `10.0s38` = `10^38`) se apoya en esa definición.
- **Cambio**: Reemplazar `AnalizadorLexico.mostrarWarning(...)` por `AnalizadorLexico.mostrarError("Constante singlef fuera de rango: " + resultado);` en `AS8.java`, `AS9.java` y `AS10.java`.

### Tarea 1.2: Validar atributo `USO` en `asignacion`, `unica`, `factor` e `invocacion_metodo`
- **Archivo**: `gram.y`
- **Problema**: `Tp3.pdf` (pág. 3) exige detectar el uso incorrecto de un símbolo según el contexto (ej. usar el nombre de una función en el lado izquierdo de una asignación, como operando sin invocación, o invocar un atributo como método).
- **Cambios**:
  1. En `asignacion : IDENTIFICADOR ASIGNAR expresion` y `unica : IDENTIFICADOR '=' '(' expresion ')'`: verificar que `TablaSimbolos.obtenerUso(claveVar)` sea `"variable"`, `"parametro"` o `"atributo"`.
  2. En `factor : IDENTIFICADOR`: rechazar `"funcion"`, `"metodo"`, `"nombre_clase"` y `"nombre_programa"` cuando aparecen sin paréntesis como operando.
  3. En ambas reglas de `invocacion_metodo`: verificar que `TablaSimbolos.obtenerUso(claveMet)` sea `"metodo"`.

### Tarea 1.3: Ajustes en `acceso_posicional` y tipo en `condicion`
- **Archivo**: `gram.y`
- **Cambios**:
  1. En `acceso_posicional : IDENTIFICADOR '[' CTE_SHORTINT ']'`: validar `IMPORT FROM` / `EXPORT TO` contra la clase dueña real del atributo (`claseDueniaAtr = obtenerClaseDeAmbito(TablaSimbolos.obtenerAmbito(claveAtr))`) en lugar de `nomClase`, para contemplar atributos heredados.
  2. En `acceso_posicional : IDENTIFICADOR '[' IDENTIFICADOR ']'`: usar `TablaSimbolos.obtenerAtributosTotalesClase(claveClase)` en lugar de `obtenerAtributosClase(claveClase)` para contemplar atributos heredados.
  3. En `condicion : expresion comparador expresion`: pasar `tipo1` como cuarto parámetro a `GeneradorTerceto.crearTerceto($2.sval, $1.sval, $3.sval, tipo1);` para que el terceto de comparación conserve su tipo.

---

## FASE 2: Correcciones en Generación de Assembler (`GeneradorAssembler.java`, `gram.y`)

### Tarea 2.1: Corregir retorno por defecto `"0$s"` en funciones y métodos sin `RET` explícito
- **Archivo**: `gram.y` (en `declaracion_funciones` y `metodo_clase`)
- **Problema**: Las constantes `SHORTINT` se almacenan en la Tabla de Símbolos sin sufijo (`"0"`). Al emitir `"0$s"` en el terceto `RET` por defecto, `GeneradorAssembler.registrarConstanteSiFalta` detecta la `'s'` y lo clasifica erróneamente como `SINGLEF` (`_cte_0$s DD 0$E`).
- **Cambio**: Reemplazar `"0$s"` por `"0"` en los tres retornos por defecto de `gram.y`.

### Tarea 2.2: Soportar `AUTO FUNCTION` con múltiples `RET` de tipos distintos (`SHORTINT` y `SINGLEF`)
- **Archivo**: `src/compilador/GeneradorAssembler.java`
- **Problema**: En una `AUTO FUNCTION` con retornos mixtos, el tipo inferido final de la función en la Tabla de Símbolos es `SINGLEF` (`_ret_<ambito> DD ?`), pero el terceto `RET` de la rama `SHORTINT` genera `MOV _ret_<ambito>, AL` (incompatibilidad de tamaño en MASM32).
- **Cambio**: En la traducción de `RET`, consultar `TablaSimbolos.obtenerTipo(ambito)` y, si la subrutina devuelve `SINGLEF` pero el `RET` puntual es `SHORTINT`, convertir `AL` a flotante (`CBW` / `MOV @aux_tos, AX` / `FILD @aux_tos` / `FSTP _ret_<ambito>`).

### Tarea 2.3: Soportar parámetros formales de tipo Clase en `.data` y `:=`
- **Archivo**: `src/compilador/GeneradorAssembler.java`
- **Problema**: En métodos con parámetros formales de tipo clase (ej. `m_cliente(c_base1 obj_b1)`), `.data` no reserva las variables de instancia del parámetro ni el terceto `:=` copia los atributos del objeto real al formal antes del `CALL`.
- **Cambios**:
  1. En la emisión de `.data`, tratar los parámetros cuyo tipo no sea `"SHORTINT"` ni `"SINGLEF"` igual que a `"objeto"`, reservando `_<claveParam>_<nomAtr>` para cada atributo en `obtenerAtributosTotalesClase`.
  2. En la traducción de `:=`, cuando el tipo sea una clase, emitir el copiado atributo por atributo desde el objeto origen hacia el parámetro destino.

### Tarea 2.4: Usar `t.getTipo()` en tercetos de comparación y corregir doble prefijo en `resolverAccesoPosicional`
- **Archivo**: `src/compilador/GeneradorAssembler.java`
- **Cambios**:
  1. En la rama de operadores relacionales (`>`, `<`, `>=`, `<=`, `==`, `!=`), priorizar `t.getTipo()` cuando esté presente antes de inferir por operandos.
  2. En `resolverAccesoPosicional`, devolver `claveObj + "_" + TablaSimbolos.obtenerNombre(claveAtrAbs)` sin llamar a `obtenerNombreAsm(...)`, evitando que `resolverOperandoAsm` agregue un doble guión bajo (`__main_...`) en lecturas RHS de acceso posicional.

---

## FASE 3: Casos de Prueba (`tests/`) y `Makefile`

1. En `tests/test_etapa3_integrador.txt`: cambiar `f1 := 10.5;` por `f1 := 10.0;` para evitar que el test válido aborte en runtime por pérdida de información en `tos(f1)` (Chequeo `g`).
2. En `tests/etapa4_hito2_aritmetica.txt`: cambiar `pout(tos(x));` por `pout(tos(res_flt));` (`15.0`) por el mismo motivo.
3. En `Makefile`: excluir `%.asm` en `PRUEBAS := $(filter-out %.java %.md %.asm, $(wildcard tests/*))` para que `make test` no intente compilar archivos `.asm`.
4. Eliminar el archivo residual `tests/test_hito5_control_ok.asm`.

---

## FASE 4: Verificación Final

```bash
make parser
grep -E "conflict|never reduced" y.output
make jar
grep -rn "\.stream()" src/
grep -rn "->" src/
```
Ejecutar `java -jar compilador.jar` sobre los tests válidos y de error de Etapa 3 y Etapa 4 para comprobar que compilen sin regresiones y generen `.asm` únicamente cuando no hay errores.
