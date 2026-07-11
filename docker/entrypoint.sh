#!/bin/bash
set -e

# Crear contraseña VNC
mkdir -p ~/.vnc
x11vnc -storepasswd "${VNC_PASSWORD}" ~/.vnc/passwd

# Iniciar servidor X virtual en display :1
Xvfb :1 -screen 0 1280x800x24 &
sleep 2

# Gestor de ventanas ligero
DISPLAY=:1 fluxbox &
sleep 1

# Servidor VNC (acceso con cliente VNC en puerto 5901)
x11vnc -display :1 -forever -shared -rfbport ${VNC_PORT} -rfbauth ~/.vnc/passwd &
sleep 1

# Proxy noVNC → acceso desde navegador en puerto 6080
/opt/novnc/utils/novnc_proxy --vnc localhost:${VNC_PORT} --listen ${NOVNC_PORT} &
sleep 1

echo "============================================"
echo " Simulador de Memoria — Contenedor listo"
echo " Acceso VNC:    localhost:5901"
echo " Acceso Web:    http://localhost:6080/vnc.html"
echo " Contraseña:    ${VNC_PASSWORD}"
echo "============================================"

# Ejecutar la aplicación JavaFX
cd /app
exec mvn -q javafx:run
