---
name: semantica-tercetos
description: Guía de diseño Java-tradicional para el generador de código intermedio por tercetos. Usar cuando se diseñen, implementen o depuren tercetos, acciones semánticas en gram.y o la clase GeneradorTercetos.
---

# Generación de Código Intermedio por Tercetos

Esta guía define las reglas para la implementación de tercetos en el compilador.

## 1. Estructura y Formato
- **Estructura del terceto**: `(operador, operando1, operando2)` + un campo adicional para el tipo resultante.
- **Formato de salida exigido**: `N ( op , op1 , op2 )`, donde `N` es el número del terceto.
- **Referencia cruzada**: Se debe utilizar el formato `[i]`, donde `i` es el índice del terceto cuyo resultado se utiliza como operando.

## 2. Catálogo de Tercetos
- **Aritméticos**: `+`, `-`, `*`, `/`
- **Asignación**: `:=`
- **Control de flujo**: `BF` (Branch False), `BI` (Branch Incondicional)
- **Comparación**: `CMP`
- **Llamadas y funciones**: `CALL`, `RET`, `POUT`
- **Temas Grupo 20**: `tos`, `ACCESO`

## 3. Implementación en Yacc (BYacc/J)
- **Atributos sintetizados**: Uso intensivo de `$$`, `$1`, `$3` en las acciones semánticas para propagar los índices numéricos de los tercetos que representan a los hijos en el árbol sintáctico.

## 4. Ejemplo Completo
Para la sentencia `a := b + c * d;`, los tercetos generados (asumiendo que empiezan en 1) son:
1 ( * , c , d )
2 ( + , b , [1] )
3 ( := , a , [2] )

## 5. Regla de Estilo Java
- Utilizar estructuras de datos básicas de Java: `ArrayList<Terceto>` para almacenar la lista global de tercetos y `Stack<Integer>` para las pilas de saltos.
- **Prohibido**: Utilizar expresiones lambda o Java Streams. Se debe usar código Java imperativo y tradicional (bucles `for` y `while`).
