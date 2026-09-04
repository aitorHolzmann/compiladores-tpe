# Imagen base oficial de VS Code Dev Containers: incluye JDK, Maven y Gradle
FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# La imagen base trae un repo de Yarn (dl.yarnpkg.com) cuya clave GPG
# quedo invalida/vencida. Eso hace fallar apt-get update por completo.
# No lo necesitamos para este TP, asi que lo sacamos antes de actualizar.
RUN rm -f /etc/apt/sources.list.d/yarn.list

# byacc-j: version de Berkeley YACC extendida para generar parsers en Java
# (paquete "byacc-j" del repositorio universe de Ubuntu/Debian, binario "byaccj")
RUN apt-get update \
    && apt-get install -y --no-install-recommends byacc-j \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*