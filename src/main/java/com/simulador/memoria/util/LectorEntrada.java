package com.simulador.memoria.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee archivos de entrada (.json o .txt) para inicializar el simulador.
 */
public class LectorEntrada {

    private static final Gson GSON = new Gson();

    public ConfiguracionEntrada leer(Path ruta) throws IOException {
        String contenido = Files.readString(ruta);
        String nombre = ruta.getFileName().toString().toLowerCase();

        if (nombre.endsWith(".json")) {
            return parsearJson(contenido);
        }
        return parsearTexto(contenido);
    }

    private ConfiguracionEntrada parsearJson(String contenido) {
        JsonObject raiz = JsonParser.parseString(contenido).getAsJsonObject();
        ConfiguracionEntrada config = new ConfiguracionEntrada();

        config.setTipo(obtenerTexto(raiz, "tipo", "asignacion"));

        if ("paginacion".equalsIgnoreCase(config.getTipo())) {
            config.setTamanoPagina(obtenerEntero(raiz, "tamanoPagina", 4096));
            config.setMemoriaFisica(obtenerEntero(raiz, "memoriaFisica", 16384));
            config.setProcesosPaginacion(parsearProcesosPaginacion(raiz));
            config.setTraducciones(parsearTraducciones(raiz));
        } else {
            config.setMemoriaTotal(obtenerEntero(raiz, "memoriaTotal", 1024));
            config.setEstrategia(obtenerTexto(raiz, "estrategia", "FIRST_FIT"));
            config.setProcesosAsignacion(parsearProcesosAsignacion(raiz));
            config.setOperaciones(parsearOperaciones(raiz));
        }

        return config;
    }

    private List<ConfiguracionEntrada.ProcesoAsignacion> parsearProcesosAsignacion(JsonObject raiz) {
        List<ConfiguracionEntrada.ProcesoAsignacion> lista = new ArrayList<>();
        if (!raiz.has("procesos")) {
            return lista;
        }
        for (JsonElement elem : raiz.getAsJsonArray("procesos")) {
            JsonObject obj = elem.getAsJsonObject();
            lista.add(new ConfiguracionEntrada.ProcesoAsignacion(
                    obtenerTexto(obj, "id", "P"),
                    obtenerEntero(obj, "tamano", 0)
            ));
        }
        return lista;
    }

    private List<ConfiguracionEntrada.Operacion> parsearOperaciones(JsonObject raiz) {
        List<ConfiguracionEntrada.Operacion> lista = new ArrayList<>();
        if (!raiz.has("operaciones")) {
            return lista;
        }
        for (JsonElement elem : raiz.getAsJsonArray("operaciones")) {
            JsonObject obj = elem.getAsJsonObject();
            lista.add(new ConfiguracionEntrada.Operacion(
                    obtenerTexto(obj, "accion", "ASIGNAR"),
                    obtenerTexto(obj, "procesoId", ""),
                    obtenerEntero(obj, "tamano", 0)
            ));
        }
        return lista;
    }

    private List<ConfiguracionEntrada.ProcesoPaginacion> parsearProcesosPaginacion(JsonObject raiz) {
        List<ConfiguracionEntrada.ProcesoPaginacion> lista = new ArrayList<>();
        if (!raiz.has("procesos")) {
            return lista;
        }
        for (JsonElement elem : raiz.getAsJsonArray("procesos")) {
            JsonObject obj = elem.getAsJsonObject();
            ConfiguracionEntrada.ProcesoPaginacion proceso = new ConfiguracionEntrada.ProcesoPaginacion(
                    obtenerTexto(obj, "id", "P"),
                    obtenerEntero(obj, "espacioVirtual", 32768)
            );

            if (obj.has("paginasCargadas")) {
                JsonArray paginas = obj.getAsJsonArray("paginasCargadas");
                for (JsonElement p : paginas) {
                    JsonObject pag = p.getAsJsonObject();
                    proceso.getPaginasCargadas().add(new ConfiguracionEntrada.PaginaCargada(
                            obtenerEntero(pag, "pagina", 0),
                            obtenerEntero(pag, "marco", 0)
                    ));
                }
            }
            lista.add(proceso);
        }
        return lista;
    }

    private List<ConfiguracionEntrada.Traduccion> parsearTraducciones(JsonObject raiz) {
        List<ConfiguracionEntrada.Traduccion> lista = new ArrayList<>();
        if (!raiz.has("traducciones")) {
            return lista;
        }
        for (JsonElement elem : raiz.getAsJsonArray("traducciones")) {
            JsonObject obj = elem.getAsJsonObject();
            lista.add(new ConfiguracionEntrada.Traduccion(
                    obtenerTexto(obj, "procesoId", ""),
                    obtenerEntero(obj, "direccionVirtual", 0)
            ));
        }
        return lista;
    }

    /**
     * Formato texto plano:
     * TIPO=asignacion|paginacion
     * MEMORIA=1024
     * ESTRATEGIA=FIRST_FIT
     * PROCESO P1 200
     * OPERACION ASIGNAR P2 150
     * OPERACION LIBERAR P1
     */
    private ConfiguracionEntrada parsearTexto(String contenido) throws IOException {
        ConfiguracionEntrada config = new ConfiguracionEntrada();

        try (BufferedReader reader = new BufferedReader(new java.io.StringReader(contenido))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }

                String[] partes = linea.split("\\s+");
                String comando = partes[0].toUpperCase();

                switch (comando) {
                    case "TIPO" -> config.setTipo(partes.length > 1 ? partes[1].toLowerCase() : "asignacion");
                    case "MEMORIA" -> {
                        if ("paginacion".equals(config.getTipo())) {
                            config.setMemoriaFisica(Integer.parseInt(partes[1]));
                        } else {
                            config.setMemoriaTotal(Integer.parseInt(partes[1]));
                        }
                    }
                    case "MEMORIA_FISICA" -> config.setMemoriaFisica(Integer.parseInt(partes[1]));
                    case "TAMANO_PAGINA" -> config.setTamanoPagina(Integer.parseInt(partes[1]));
                    case "ESTRATEGIA" -> config.setEstrategia(partes[1]);
                    case "PROCESO" -> {
                        if ("paginacion".equals(config.getTipo())) {
                            config.getProcesosPaginacion().add(
                                    new ConfiguracionEntrada.ProcesoPaginacion(partes[1], Integer.parseInt(partes[2])));
                        } else {
                            config.getProcesosAsignacion().add(
                                    new ConfiguracionEntrada.ProcesoAsignacion(partes[1], Integer.parseInt(partes[2])));
                        }
                    }
                    case "PAGINA" -> {
                        if (config.getProcesosPaginacion().isEmpty()) {
                            break;
                        }
                        ConfiguracionEntrada.ProcesoPaginacion ultimo =
                                config.getProcesosPaginacion().get(config.getProcesosPaginacion().size() - 1);
                        ultimo.getPaginasCargadas().add(
                                new ConfiguracionEntrada.PaginaCargada(
                                        Integer.parseInt(partes[1]), Integer.parseInt(partes[2])));
                    }
                    case "OPERACION" -> config.getOperaciones().add(
                            new ConfiguracionEntrada.Operacion(partes[1], partes[2],
                                    partes.length > 3 ? Integer.parseInt(partes[3]) : 0));
                    case "TRADUCIR" -> config.getTraducciones().add(
                            new ConfiguracionEntrada.Traduccion(partes[1], Integer.parseInt(partes[2])));
                    default -> { /* ignorar líneas desconocidas */ }
                }
            }
        }
        return config;
    }

    private String obtenerTexto(JsonObject obj, String clave, String defecto) {
        return obj.has(clave) ? obj.get(clave).getAsString() : defecto;
    }

    private int obtenerEntero(JsonObject obj, String clave, int defecto) {
        return obj.has(clave) ? obj.get(clave).getAsInt() : defecto;
    }
}
