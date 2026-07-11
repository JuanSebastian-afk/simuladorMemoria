# Imagen base con JDK 17 y entorno gráfico ligero + VNC/noVNC
FROM eclipse-temurin:17-jdk-jammy

ENV DEBIAN_FRONTEND=noninteractive
ENV DISPLAY=:1
ENV VNC_PORT=5901
ENV NOVNC_PORT=6080
ENV VNC_PASSWORD=memoria123

# Dependencias: Maven, JavaFX nativas, Xvfb, escritorio ligero, VNC y noVNC
RUN apt-get update && apt-get install -y --no-install-recommends \
    maven \
    openjfx \
    libopenjfx-jni \
    libgtk-3-0 \
    libglib2.0-0 \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libfontconfig1 \
    fonts-dejavu-core \
    xvfb \
    x11vnc \
    fluxbox \
    wget \
    net-tools \
    procps \
    && rm -rf /var/lib/apt/lists/*

# Instalar noVNC para acceso vía navegador web
RUN mkdir -p /opt/novnc/utils/websockify \
    && wget -qO- https://github.com/novnc/noVNC/archive/refs/tags/v1.4.0.tar.gz \
       | tar xz -C /opt/novnc --strip-components=1 \
    && wget -qO- https://github.com/novnc/websockify/archive/refs/tags/v0.11.0.tar.gz \
       | tar xz -C /opt/novnc/utils/websockify --strip-components=1

WORKDIR /app

# Copiar proyecto y compilar
COPY pom.xml .
COPY src ./src
COPY ejemplos ./ejemplos

RUN mvn -q -DskipTests package

# Script de arranque: Xvfb + VNC + noVNC + aplicación JavaFX
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 5901 6080

ENTRYPOINT ["/entrypoint.sh"]
