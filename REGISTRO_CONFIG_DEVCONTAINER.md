# Registro de Configuración: Integración DevContainer + Gentle AI (Opción 1)

Este documento registra los cambios introducidos para permitir el uso de **Gentle AI**, **Antigravity** y **Engram** trabajando **adentro del contenedor de desarrollo (DevContainer)**, junto con el procedimiento exacto para **revertirlos (rollback)** si se desea volver al estado original.

---

## 1. Motivación y Arquitectura (Opción 1)

* **Problema:** El compilador de la materia requiere un toolchain específico (`byacc-j`, `make`, `openjdk-21`) provisto por el DevContainer. Sin embargo, Gentle AI, sus binarios (`engram`, `gentle-ai`, `gga`), la base de memoria persistente SQLite (`~/.engram/engram.db`) y las configuraciones de MCP viven en el sistema operativo host (`/home/facha/`).
* **Solución adoptada:**  
  1. Montar los directorios y archivos de configuración del host dentro del contenedor en `/home/vscode/` vía `bind-mounts`.
  2. Crear un enlace simbólico (`symlink`) dentro del contenedor: `/home/facha -> /home/vscode`. Esto garantiza que rutas absolutas registradas en el host (como `/home/facha/.local/bin/engram` en `mcp.json`) se resuelvan sin error dentro del contenedor.
  3. Exportar `/home/vscode/.local/bin` al `PATH` del contenedor para que los comandos de Gentle AI sean accesibles directamente.

---

## 2. Archivos Modificados y Cambios Detallados

### A. `.devcontainer/Dockerfile`

Se agregaron las siguientes instrucciones al final del archivo:

```dockerfile
# Integracion Gentle AI / Antigravity:
# 1. Directorios precreados para bind-mounts limpios
# 2. Symlink de compatibilidad: /home/facha -> /home/vscode para resolver rutas del host
# 3. Inclusion de ~/.local/bin en PATH
RUN mkdir -p /home/vscode/.config/Code/User \
             /home/vscode/.local/bin \
             /home/vscode/.gentle-ai \
             /home/vscode/.engram \
             /home/vscode/.gemini \
             /home/vscode/.agents \
             /home/vscode/.copilot \
    && ln -s /home/vscode /home/facha \
    && chown -R vscode:vscode /home/vscode

ENV PATH="/home/vscode/.local/bin:${PATH}"
```

* **Razón técnica:**
  - `mkdir -p`: Evita el "gotcha" clásico de Docker donde montar un archivo individual (como `mcp.json`) crea una carpeta con ese nombre si la ruta padre no existía previamente.
  - `ln -s /home/vscode /home/facha`: Vincula de forma transparente cualquier referencia a la home del host con la home del contenedor (`UID/GID 1000` coincidentes).
  - `ENV PATH`: Asegura que cualquier shell interactiva o proceso dentro del contenedor encuentre `gentle-ai` y `engram`.

---

### B. `.devcontainer/devcontainer.json`

Se agregaron las claves `remoteUser`, los `mounts` bind hacia el host y la extensión de Antigravity:

```json
  "remoteUser": "vscode",
  "mounts": [
    "source=${localEnv:HOME}/.gentle-ai,target=/home/vscode/.gentle-ai,type=bind",
    "source=${localEnv:HOME}/.engram,target=/home/vscode/.engram,type=bind",
    "source=${localEnv:HOME}/.gemini,target=/home/vscode/.gemini,type=bind",
    "source=${localEnv:HOME}/.agents,target=/home/vscode/.agents,type=bind",
    "source=${localEnv:HOME}/.copilot,target=/home/vscode/.copilot,type=bind",
    "source=${localEnv:HOME}/.local/bin,target=/home/vscode/.local/bin,type=bind,readonly",
    "source=${localEnv:HOME}/.config/Code/User/mcp.json,target=/home/vscode/.config/Code/User/mcp.json,type=bind",
    "source=${localEnv:HOME}/.config/Code/User/prompts,target=/home/vscode/.config/Code/User/prompts,type=bind"
  ],
```

Y en `customizations.vscode.extensions`:
```json
  "google.google-antigravity"
```

* **Razón técnica:**
  - Los binarios en `~/.local/bin` son ejecutables x86_64 estáticos (ELF 64-bit), por lo que corren directamente dentro del contenedor sin problemas de bibliotecas dinámicas.
  - El bind-mount de `~/.engram` permite que la memoria persistente de Engram se comparta bidireccionalmente entre el host y las sesiones del contenedor.

---

## 3. Procedimiento para Probar la Configuración

1. En VS Code, presionar `F1` (o `Ctrl+Shift+P`).
2. Ejecutar el comando: **`Dev Containers: Rebuild and Reopen in Container`**.
3. Al finalizar el arranque del contenedor, abrir una terminal integrada y verificar:
   ```bash
   which gentle-ai engram gga
   engram --help
   byaccj -V
   javac -version
   ```

---

## 4. Procedimiento de Reversión Completa (Rollback)

Si en cualquier momento se desea deshacer esta configuración y volver al estado previo original:

### Opción Rápida con Git:
```bash
git checkout HEAD -- .devcontainer/
rm -f REGISTRO_CONFIG_DEVCONTAINER.md
```

### Opción Manual paso a paso:
1. En `.devcontainer/Dockerfile`, eliminar el bloque agregado (`RUN mkdir -p ... && ln -s ...` y `ENV PATH=...`).
2. En `.devcontainer/devcontainer.json`, eliminar:
   - La propiedad `"remoteUser": "vscode"`
   - Todo el bloque `"mounts": [ ... ]`
   - La entrada `"google.google-antigravity"` de `extensions`.
3. Eliminar este archivo de registro: `rm REGISTRO_CONFIG_DEVCONTAINER.md`.
4. En VS Code, ejecutar nuevamente: **`Dev Containers: Rebuild Container Without Cache`**.
