package com.simulador.memoria.model.paginacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Paso individual del cálculo de traducción de direcciones.
 */
public class PasoTraduccion {

    private final int numero;
    private final String descripcion;
    private final String formula;
    private final String resultado;

    public PasoTraduccion(int numero, String descripcion, String formula, String resultado) {
        this.numero = numero;
        this.descripcion = descripcion;
        this.formula = formula;
        this.resultado = resultado;
    }

    public int getNumero() {
        return numero;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFormula() {
        return formula;
    }

    public String getResultado() {
        return resultado;
    }

    @Override
    public String toString() {
        return String.format("Paso %d: %s\n  %s\n  → %s", numero, descripcion, formula, resultado);
    }
}
