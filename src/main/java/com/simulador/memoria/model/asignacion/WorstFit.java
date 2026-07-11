package com.simulador.memoria.model.asignacion;

import java.util.List;

/**
 * Worst Fit: asigna el bloque libre más grande disponible.
 * Ventaja: deja fragmentos grandes que pueden ser útiles. Desventaja: agota bloques grandes rápido.
 */
public class WorstFit implements EstrategiaAsignacion {

    @Override
    public int buscarBloque(List<BloqueMemoria> bloques, int tamano) {
        int peorIndice = -1;
        int mayorTamano = -1;

        for (int i = 0; i < bloques.size(); i++) {
            BloqueMemoria bloque = bloques.get(i);
            if (bloque.isLibre() && bloque.getTamano() >= tamano) {
                if (bloque.getTamano() > mayorTamano) {
                    mayorTamano = bloque.getTamano();
                    peorIndice = i;
                }
            }
        }
        return peorIndice;
    }

    @Override
    public String getNombre() {
        return "Worst Fit";
    }
}
