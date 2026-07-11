package com.simulador.memoria.model.asignacion;

import java.util.List;

/**
 * Best Fit: asigna el bloque libre más pequeño que aún pueda contener el proceso.
 * Ventaja: minimiza el desperdicio por bloque. Desventaja: deja fragmentos muy pequeños.
 */
public class BestFit implements EstrategiaAsignacion {

    @Override
    public int buscarBloque(List<BloqueMemoria> bloques, int tamano) {
        int mejorIndice = -1;
        int menorDiferencia = Integer.MAX_VALUE;

        for (int i = 0; i < bloques.size(); i++) {
            BloqueMemoria bloque = bloques.get(i);
            if (bloque.isLibre() && bloque.getTamano() >= tamano) {
                int diferencia = bloque.getTamano() - tamano;
                if (diferencia < menorDiferencia) {
                    menorDiferencia = diferencia;
                    mejorIndice = i;
                }
            }
        }
        return mejorIndice;
    }

    @Override
    public String getNombre() {
        return "Best Fit";
    }
}
