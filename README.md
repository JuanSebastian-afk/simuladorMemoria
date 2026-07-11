# Simulador de Administración de Memoria

Simulador de planificador de memoria para la asignatura de **Sistemas Operativos** que permite experimentar con dos mecanismos fundamentales de gestión de memoria: **asignación contigua** (First Fit, Best Fit, Worst Fit) y **paginación de un solo nivel** con traducción de direcciones virtuales a físicas.

Desarrollado en **Java 17+** con **JavaFX** para la interfaz gráfica, **Maven** como gestor de dependencias y **Docker** para despliegue multiplataforma con acceso remoto a la GUI.

---

## Objetivos

El proyecto tiene como finalidad facilitar el aprendizaje práctico de los conceptos de administración de memoria en sistemas operativos:

1. **Comprender la asignación contigua de memoria** mediante la simulación de tres estrategias clásicas (First Fit, Best Fit y Worst Fit), observando cómo cada una afecta la fragmentación externa e interna.
2. **Visualizar el estado de la memoria principal** de forma dinámica: bloques libres, bloques ocupados por procesos y métricas de fragmentación en tiempo real.
3. **Simular la paginación de un solo nivel**, modelando memoria virtual, memoria física, tabla de páginas, marcos disponibles y el proceso de traducción DV → DF.
4. **Mostrar el cálculo paso a paso** de la traducción de direcciones, incluyendo la detección de fallos de página (page fault).
5. **Automatizar escenarios de prueba** mediante la carga de archivos de entrada (`.json` o `.txt`) sin necesidad de ingresar datos manualmente.
6. **Demostrar buenas prácticas de ingeniería de software**: arquitectura MVC, patrón Strategy, principios SOLID y separación de responsabilidades.

---

## Metodología

El desarrollo siguió un enfoque incremental y orientado a la separación de capas:

| Fase | Descripción |
|------|-------------|
| **1. Modelado** | Diseño de las entidades del dominio (`Proceso`, `BloqueMemoria`, `Marco`, `TablaPaginas`, etc.) sin dependencias de la interfaz gráfica. |
| **2. Algoritmos** | Implementación de las estrategias de asignación mediante el patrón **Strategy** y la lógica de traducción de direcciones con cálculo documentado paso a paso. |
| **3. Controladores** | Capa intermedia que conecta la GUI con el modelo, gestiona eventos y actualiza las vistas. |
| **4. Interfaz gráfica** | Vistas en FXML con estilos CSS personalizados para una experiencia visual clara y didáctica. |
| **5. Entrada automatizada** | Lector de archivos JSON y texto plano para inicializar escenarios y ejecutar operaciones en lote. |
| **6. Contenedorización** | Empaquetado con Docker usando Xvfb + VNC + noVNC para ejecutar JavaFX en entornos sin escritorio nativo. |

---

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|------------|---------|-----|
| **Java** | 17+ | Lenguaje principal del proyecto |
| **JavaFX** | 21.0.2 | Interfaz gráfica de usuario (GUI) |
| **FXML** | — | Definición declarativa de las vistas |
| **Maven** | 3.9+ | Gestión de dependencias, compilación y ejecución |
| **Gson** | 2.10.1 | Parseo de archivos de entrada en formato JSON |
| **Docker** | — | Empaquetado y despliegue del simulador |
| **Docker Compose** | 3.8 | Orquestación del contenedor con VNC/noVNC |
| **Xvfb** | — | Servidor X virtual para renderizar la GUI en contenedor |
| **x11vnc + noVNC** | — | Acceso remoto a la interfaz vía cliente VNC o navegador web |

---

## Estructura del proyecto

```
simuladorMemoria/
├── pom.xml                          # Configuración Maven y dependencias
├── Dockerfile                       # Imagen Docker con JDK, JavaFX y VNC
├── docker-compose.yml               # Orquestación del contenedor
├── docker/
│   └── entrypoint.sh                # Script de arranque (Xvfb + VNC + app)
├── ejemplos/                        # Archivos de entrada de ejemplo
│   ├── asignacion.json
│   ├── asignacion.txt
│   ├── paginacion.json
│   └── paginacion.txt
└── src/main/
    ├── java/com/simulador/memoria/
    │   ├── Main.java                # Punto de entrada de la aplicación
    │   ├── model/                   # Capa de dominio (sin dependencias de GUI)
    │   │   ├── asignacion/
    │   │   │   ├── BloqueMemoria.java
    │   │   │   ├── Proceso.java
    │   │   │   ├── GestorMemoria.java
    │   │   │   ├── EstrategiaAsignacion.java
    │   │   │   ├── FirstFit.java
    │   │   │   ├── BestFit.java
    │   │   │   ├── WorstFit.java
    │   │   │   └── ...
    │   │   └── paginacion/
    │   │       ├── Marco.java
    │   │       ├── TablaPaginas.java
    │   │       ├── GestorPaginacion.java
    │   │       ├── ResultadoTraduccion.java
    │   │       └── ...
    │   ├── controller/              # Controladores JavaFX (MVC)
    │   │   ├── MainController.java
    │   │   ├── AsignacionController.java
    │   │   └── PaginacionController.java
    │   └── util/                    # Utilidades auxiliares
    │       ├── LectorEntrada.java
    │       └── ConfiguracionEntrada.java
    └── resources/
        ├── fxml/                    # Vistas FXML
        │   ├── main.fxml
        │   ├── asignacion.fxml
        │   └── paginacion.fxml
        └── css/
            └── estilos.css          # Estilos visuales de la aplicación
```

---

## Módulos implementados

### 1. Asignación de Memoria

Simula la gestión de memoria principal con partición dinámica y tres estrategias seleccionables:

| Estrategia | Descripción |
|------------|-------------|
| **First Fit** | Asigna el primer bloque libre suficientemente grande |
| **Best Fit** | Asigna el bloque libre más pequeño que aún contenga el proceso |
| **Worst Fit** | Asigna el bloque libre más grande disponible |

**Funcionalidades:**
- Visualización dinámica de bloques libres y ocupados con identificadores de proceso
- Cálculo de **fragmentación externa**: `(memoria_libre − mayor_bloque_libre) / memoria_libre × 100`
- Cálculo de **fragmentación interna** cuando el bloque asignado excede el tamaño solicitado
- Creación y liberación de procesos por ID
- Fusión automática de bloques libres adyacentes al liberar memoria

### 2. Paginación y Traducción de Direcciones

Simula un esquema de paginación de un solo nivel con:

- **Memoria virtual** (espacio de direcciones del proceso)
- **Memoria física** dividida en marcos (frames)
- **Tamaño de página** configurable (1 KB, 2 KB, 4 KB, 8 KB)
- **Tabla de páginas** con mapeo página → marco y bit de validez
- **Lista de marcos disponibles**

**Traducción paso a paso:**

```
Página   = DV ÷ Tamaño_Página
Offset   = DV mod Tamaño_Página
DF       = (Marco × Tamaño_Página) + Offset
```

Si el bit de validez es 0, se reporta un **fallo de página** (page fault).

---

## Arquitectura

El proyecto sigue el patrón **MVC (Modelo-Vista-Controlador)** con principios **SOLID**:

```
┌─────────────┐     eventos      ┌──────────────────┐     llamadas     ┌─────────────┐
│   Vista     │ ◄──────────────► │   Controlador    │ ◄──────────────► │   Modelo    │
│  (FXML/CSS) │                  │  (JavaFX Ctrl)   │                  │  (POO puro) │
└─────────────┘                  └──────────────────┘                  └─────────────┘
```

- **Modelo**: clases puras de POO sin dependencias de JavaFX. Contienen toda la lógica de negocio y algoritmos.
- **Vista**: archivos FXML y hojas de estilo CSS. Definen la estructura visual sin lógica.
- **Controlador**: mediadores que reciben eventos de la GUI, invocan al modelo y actualizan la vista.

**Patrones de diseño aplicados:**
- **Strategy** → `EstrategiaAsignacion` con implementaciones `FirstFit`, `BestFit`, `WorstFit`
- **DTO** → `ConfiguracionEntrada`, `ResultadoAsignacion`, `ResultadoTraduccion`

---

## Requisitos previos

### Ejecución local

- JDK 17 o superior
- Maven 3.9+
- Sistema operativo con soporte gráfico (para JavaFX)

### Ejecución con Docker

- Docker 20+
- Docker Compose 2+

---

## Instalación y ejecución

### Opción 1: Ejecución local

```bash
# Clonar el repositorio
git clone https://github.com/<usuario>/simuladorMemoria.git
cd simuladorMemoria

# Compilar el proyecto
mvn compile

# Ejecutar la aplicación
mvn javafx:run
```

### Opción 2: Ejecución con Docker

El contenedor incluye un entorno gráfico virtual (Xvfb + Fluxbox) y servidores VNC para acceder a la GUI sin necesidad de un escritorio local.

```bash
# Construir y levantar el contenedor
docker compose up --build
```

Una vez iniciado, accede a la interfaz de cualquiera de estas formas:

| Método | Dirección | Contraseña |
|--------|-----------|------------|
| **Navegador web (noVNC)** | http://localhost:6080/vnc.html | `memoria123` |
| **Cliente VNC** (Remmina, TigerVNC, etc.) | `localhost:5901` | `memoria123` |

**Componentes del contenedor:**

1. **Xvfb** — servidor de display virtual (`:1`)
2. **Fluxbox** — gestor de ventanas ligero
3. **x11vnc** — servidor VNC en el puerto `5901`
4. **noVNC** — proxy web en el puerto `6080` para acceso desde el navegador
5. **Aplicación JavaFX** — se inicia automáticamente al arrancar el contenedor

Para detener el contenedor:

```bash
docker compose down
```

---

## Archivos de entrada

El simulador permite cargar escenarios desde archivos `.json` o `.txt` mediante el botón **"Cargar Archivo"** en cada pestaña.

### Formato JSON — Asignación

```json
{
  "tipo": "asignacion",
  "memoriaTotal": 1024,
  "estrategia": "BEST_FIT",
  "procesos": [
    { "id": "P1", "tamano": 200 },
    { "id": "P2", "tamano": 150 }
  ],
  "operaciones": [
    { "accion": "LIBERAR", "procesoId": "P2", "tamano": 0 },
    { "accion": "ASIGNAR", "procesoId": "P4", "tamano": 180 }
  ]
}
```

### Formato JSON — Paginación

```json
{
  "tipo": "paginacion",
  "tamanoPagina": 4096,
  "memoriaFisica": 16384,
  "procesos": [
    {
      "id": "P1",
      "espacioVirtual": 32768,
      "paginasCargadas": [
        { "pagina": 0, "marco": 0 },
        { "pagina": 1, "marco": 2 }
      ]
    }
  ],
  "traducciones": [
    { "procesoId": "P1", "direccionVirtual": 5000 }
  ]
}
```

### Formato texto plano

```
TIPO asignacion
MEMORIA 1024
ESTRATEGIA WORST_FIT
PROCESO P1 200
OPERACION LIBERAR P2
OPERACION ASIGNAR P4 180
```

```
TIPO paginacion
MEMORIA_FISICA 16384
TAMANO_PAGINA 4096
PROCESO P1 32768
PAGINA 0 0
TRADUCIR P1 5000
```

Ejemplos completos disponibles en la carpeta [`ejemplos/`](ejemplos/).
