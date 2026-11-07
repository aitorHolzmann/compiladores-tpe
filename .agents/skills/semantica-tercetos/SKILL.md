---
name: semantica-tercetos
description: Guía de diseño Java-tradicional para el generador de código intermedio por tercetos. Usar cuando se diseñen, implementen o depuren tercetos, acciones semánticas en gram.y o la clase GeneradorTercetos.
---

# Generación de Código Intermedio por Tercetos

Esta guía define las reglas para la implementación de tercetos en el compilador.

## 1. Estructura y Formato
- **Estructura del terceto**: `(operador, operando1, operando2)` + un campo adicional para el tipo resultante.
- **Formato de salida exigido**: `N ( op , op1 , op2 )`, donde `N` es el número del terceto (inicia en `0` localmente para cada ámbito).
- **Referencia cruzada**: Se debe utilizar el formato `[i]`, donde `i` es el índice del terceto dentro del ámbito actual cuyo resultado se utiliza como operando.
- **Organización por ámbitos**: Código intermedio almacenado en `Map<String, ArrayList<Terceto>>` (`LinkedHashMap`), agrupando las listas de tercetos por su ámbito (`"main"`, `"main@funcion"`, `"main@Clase@metodo"`). No se emiten pseudo-tercetos delimitadores (`FUNCION`, `FIN_FUNCION`, `METODO`, `FIN_METODO`).

## 2. Catálogo de Tercetos
- **Aritméticos**: `+`, `-`, `*`, `/`
- **Asignación**: `:=`
- **Control de flujo**: `BF` (Branch False), `BI` (Branch Incondicional)
- **Comparación**: `CMP` (o directos `>`, `<`, `>=`, `<=`, `=`, `!=`)
- **Llamadas y funciones**: `CALL`, `RET`, `POUT` (sin delimitadores `FUNCION`/`FIN_FUNCION` al usarse listas separadas por ámbito)
- **Temas Grupo 20**: `tos`, acceso posicional `obj[idx]` en operandos

## 3. Implementación en Yacc (BYacc/J)
- **Atributos sintetizados**: Uso intensivo de `$$`, `$1`, `$3` en las acciones semánticas para propagar los índices numéricos de los tercetos que representan a los hijos en el árbol sintáctico.

## 4. Ejemplo Completo
Para la sentencia `a := b + c * d;` en el ámbito `main`, los tercetos generados (iniciando en 0) son:
Ambito: main
0 ( * , c , d )
1 ( + , b , [0] )
2 ( := , a , [1] )

## 5. Regla de Estilo Java
- Utilizar estructuras de datos básicas de Java: `Map<String, ArrayList<Terceto>>` (`LinkedHashMap`) para organizar los tercetos por ámbito, `ArrayList<Terceto>` para la lista interna de cada ámbito y `Stack<Integer>` para las pilas de saltos.
- **Prohibido**: Utilizar expresiones lambda o Java Streams. Se debe usar código Java imperativo y tradicional (bucles `for` y `while`).
