package com.simulador.memoria.model.asignacion;

import java.util.List;

/**
 * First Fit: asigna el primer bloque libre que sea lo suficientemente grande.
 * Ventaja: rápido. Desventaja: puede generar fragmentación al inicio de la memoria.
 */
public class FirstFit implements EstrategiaAsignacion {

    @Override
    public int buscarBloque(List<BloqueMemoria> bloques, int tamano) {
        for (int i = 0; i < bloques.size(); i++) {
            BloqueMemoria bloque = bloques.get(i);
            if (bloque.isLibre() && bloque.getTamano() >= tamano) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String getNombre() {
        return "First Fit";
    }
}
