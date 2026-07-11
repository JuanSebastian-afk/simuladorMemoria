package com.simulador.memoria.model.asignacion;

import java.util.List;

/**
 * Interfaz Strategy para las políticas de asignación de memoria.
 * Cada implementación define cómo seleccionar un bloque libre.
 */
public interface EstrategiaAsignacion {

    /**
     * Busca el índice del bloque libre más adecuado según la estrategia.
     *
     * @param bloques lista actual de bloques de memoria
     * @param tamano  tamaño solicitado por el proceso
     * @return índice del bloque seleccionado, o -1 si no hay espacio suficiente
     */
    int buscarBloque(List<BloqueMemoria> bloques, int tamano);

    String getNombre();
}
