---
name: yacc-backpatching
description: Técnicas seguras para insertar marcadores vacíos en BYacc/J sin generar conflictos, y uso de pilas de saltos para parcheo de tercetos incompletos. Usar cuando se implementen sentencias IF, IF-ELSE o REPEAT UNTIL con tercetos.
---

# Backpatching en BYacc/J

Esta guía explica la técnica de backpatching (parcheo de saltos) para sentencias de control.

## 1. Concepto de Backpatching
El backpatching resuelve el problema de generar saltos condicionales e incondicionales hacia instrucciones (tercetos) que aún no han sido generadas. Como no se conoce la dirección de destino al momento de crear el salto, se deja vacío y se guarda su índice en una pila para parchearlo más adelante cuando el destino sea conocido.

## 2. Marcadores Vacíos en Yacc
Para capturar el índice del próximo terceto a generar o para ejecutar código en medio de una producción, se utilizan producciones "marcador" vacías.
- **Advertencia**: Los marcadores pueden introducir conflictos shift/reduce si se colocan de forma ambigua. Siempre compilar y verificar con `byaccj -J -v gram.y`.

## 3. Ejemplos Detallados

### IF Simple
```yacc
sentencia_if : IF condicion marcador_if THEN bloque END_IF
```
- El `marcador_if` genera un terceto `BF` incompleto y guarda su índice.
- Al reducir `sentencia_if`, se parchea el `BF` guardado con el índice actual (siguiente instrucción tras el bloque).

### IF-ELSE
Requiere doble parcheo y el uso de un salto incondicional `BI`.
```yacc
sentencia_if_else : IF condicion marcador_if THEN bloque marcador_else ELSE bloque END_IF
```
- `marcador_if` genera el `BF` y lo apila.
- `marcador_else` genera un `BI` (para saltar el bloque else), parchea el `BF` anterior hacia el inicio del bloque else, y apila el índice del `BI`.
- Al reducir `sentencia_if_else`, se parchea el `BI` al final del bloque else.

### REPEAT UNTIL (Sin Backpatching)
```yacc
sentencia_repeat : REPEAT marcador_inicio bloque UNTIL condicion
```
- No necesita backpatching porque la condición se evalúa al final y se salta hacia el `marcador_inicio`, cuyo destino es conocido de antemano.

## 4. Pila de Saltos
- Emplear `Stack<Integer>` (ej. `pilaSaltos`).
- Asegurar que las operaciones `push` y `pop` contemplen correctamente el anidamiento de estructuras de control.
