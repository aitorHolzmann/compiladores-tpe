# Tests del analizador léxico

Estos tests son programas Java ejecutables directamente. No requieren JUnit ni otra dependencia externa.

## Prerrequisitos

Ejecutá los comandos desde la raíz del proyecto, es decir, desde `compiladores-tpe/`. Se necesita tener disponible Java (`java` y `javac`).

Podés comprobarlo con:

```bash
java -version
javac -version
```

## Ejecutar `AnalizadorLexicoTest`

1. Crear una carpeta temporal para las clases compiladas:

```bash
rm -rf /tmp/compiladores-tpe-test-bin
mkdir -p /tmp/compiladores-tpe-test-bin
```

2. Compilar las clases del proyecto y el test:

```bash
javac -d /tmp/compiladores-tpe-test-bin \
  $(find src -name '*.java') tests/AnalizadorLexicoTest.java
```

3. Ejecutar el test usando el nombre completo de su paquete:

```bash
java -cp /tmp/compiladores-tpe-test-bin tests.AnalizadorLexicoTest
```

También se puede ejecutar todo en un solo comando:

```bash
rm -rf /tmp/compiladores-tpe-test-bin && \
mkdir -p /tmp/compiladores-tpe-test-bin && \
javac -d /tmp/compiladores-tpe-test-bin \
  $(find src -name '*.java') tests/AnalizadorLexicoTest.java && \
java -cp /tmp/compiladores-tpe-test-bin tests.AnalizadorLexicoTest
```

## Qué verifica

`AnalizadorLexicoTest` comprueba que `getTipoCaracter` clasifique correctamente:

| Entrada | Resultado esperado | Significado |
| --- | --- | --- |
| `'7'` | `'0'` | Dígito |
| `'a'` | `'a'` | Letra minúscula |
| `'S'` | `'A'` | Letra mayúscula |
| `'+'` | `'+'` | Símbolo reconocido |

Si todo funciona, aparece:

```text
AnalizadorLexicoTest: OK
```

Si una verificación falla, el programa termina con `AssertionError` e indica la entrada, el valor esperado y el valor obtenido. Si la compilación falla, primero hay que corregir el error de Java informado por `javac`.

## Agregar una verificación

Dentro de `main`, agregá otra llamada a `verificar` con la entrada y el resultado esperado:

```java
verificar('-', '-');
```

Después volvé a ejecutar el comando de compilación y ejecución. El test carga las tablas del analizador desde:

- `src/estructuras/tabla_transicion_estados`
- `src/estructuras/tabla_acciones_semanticas`

Por eso debe ejecutarse desde la raíz del proyecto; de lo contrario, las rutas relativas pueden no encontrarse.
