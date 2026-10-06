---
name: verificaciones-semanticas-grupo20
description: Catálogo completo de errores semánticos a detectar por el Grupo 20 en la Etapa 3, incluyendo name mangling, inferencia de tipos, visibilidad y herencia. Usar como referencia al implementar chequeos semánticos.
---

# Verificaciones Semánticas - Grupo 20

Este catálogo lista los errores semánticos requeridos y las reglas específicas para la Etapa 3.

## 1. Reglas Generales
- **Formato de error**: Los errores deben reportarse estrictamente con el formato:
  `Error semantico (linea X): <descripcion>`
- **Name Mangling**: Las variables y funciones se codifican con el separador `:` para mantener unicidad según su ámbito.
  - Ejemplo: `a:main`, `x:main:f1:f2`.
  - Se debe utilizar una variable global `ambitoActual` en el analizador sintáctico para construir los nombres.

## 2. Chequeos de Declaración
Se debe verificar y emitir error semántico para:
- Variable no declarada.
- Redeclaración de variable en el mismo ámbito.
- Función no declarada al ser invocada.
- Clase no declarada.
- Métodos no declarados.
- Uso incorrecto de símbolos.

## 3. Chequeos de Tipos y Compatibilidad
- **Incompatibilidad**: Operaciones entre `SHORTINT` y `SINGLEF` arrojan error sin conversión.
- **Conversiones implícitas**: Se permite la conversión implícita de entero a flotante en asignaciones. No se permite de flotante a entero sin conversión explícita.
- **Conversión explícita**: Se valida el uso de `tos()` para convertir a entero corto.

## 4. Chequeos Específicos por Tema (Grupo 20)

### Tema 21 (AUTO FUNCTION)
- **Inferencia de tipo**: El tipo de retorno de la función se infiere al reducir la regla `RET(expresion)`.
- **Consistencia**: Si hay múltiples retornos (`RET`), todos deben coincidir en el tipo resultante.

### Tema 22 (COMPTIME)
- **Evaluación**: Se marca con un flag en la Tabla de Símbolos. Se reemplazan por constantes en tiempo de compilación.
- **Validación**: Solo admiten asignaciones con expresiones que puedan resolverse estáticamente, sin generar tercetos en runtime.

### Tema 26 (IMPORT/EXPORT)
- Se debe validar la visibilidad de las clases y miembros importados/exportados, emitiendo un error si no existen o no están permitidos.

### Tema 29 (Acceso posicional a atributos)
- Se mapea el índice al atributo por su orden de declaración.
- Se debe verificar semánticamente que el índice no sea negativo o inválido.

### Tema 30 (Herencia múltiple)
- **Resolución de nombres**: La búsqueda de atributos y métodos en clases base se realiza respetando el orden de la lista de `EXTENDS` (Opción 1).
