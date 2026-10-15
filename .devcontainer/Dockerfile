# Imagen base oficial de VS Code Dev Containers: incluye JDK, Maven y Gradle
FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# La imagen base trae un repo de Yarn (dl.yarnpkg.com) cuya clave GPG
# quedo invalida/vencida. Eso hace fallar apt-get update por completo.
# No lo necesitamos para este TP, asi que lo sacamos antes de actualizar.
RUN rm -f /etc/apt/sources.list.d/yarn.list

# make: para correr las pruebas con "make test"
# byacc-j: version de Berkeley YACC extendida para generar parsers en Java
# (paquete "byacc-j" del repositorio universe de Ubuntu/Debian, binario "byaccj")
RUN apt-get update \
    && apt-get install -y --no-install-recommends byacc-j make \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

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