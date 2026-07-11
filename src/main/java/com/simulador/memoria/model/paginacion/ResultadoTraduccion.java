package com.simulador.memoria.model.paginacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado completo de la traducción de una dirección virtual a física.
 */
public class ResultadoTraduccion {

    private final boolean exito;
    private final boolean falloPagina;
    private final String mensaje;
    private final int direccionVirtual;
    private final int direccionFisica;
    private final int numeroPagina;
    private final int desplazamiento;
    private final int numeroMarco;
    private final List<PasoTraduccion> pasos;

    private ResultadoTraduccion(boolean exito, boolean falloPagina, String mensaje,
                                int direccionVirtual, int direccionFisica,
                                int numeroPagina, int desplazamiento, int numeroMarco,
                                List<PasoTraduccion> pasos) {
        this.exito = exito;
        this.falloPagina = falloPagina;
        this.mensaje = mensaje;
        this.direccionVirtual = direccionVirtual;
        this.direccionFisica = direccionFisica;
        this.numeroPagina = numeroPagina;
        this.desplazamiento = desplazamiento;
        this.numeroMarco = numeroMarco;
        this.pasos = pasos;
    }

    public static ResultadoTraduccion fallo(String mensaje, int direccionVirtual, List<PasoTraduccion> pasos) {
        return new ResultadoTraduccion(false, false, mensaje, direccionVirtual, -1, -1, -1, -1, pasos);
    }

    public static ResultadoTraduccion falloPagina(String mensaje, int direccionVirtual,
                                                   int numeroPagina, int desplazamiento,
                                                   List<PasoTraduccion> pasos) {
        return new ResultadoTraduccion(false, true, mensaje, direccionVirtual, -1,
                numeroPagina, desplazamiento, -1, pasos);
    }

    public static ResultadoTraduccion exito(int direccionVirtual, int direccionFisica,
                                            int numeroPagina, int desplazamiento, int numeroMarco,
                                            List<PasoTraduccion> pasos) {
        return new ResultadoTraduccion(true, false,
                "Traducción exitosa: DV " + direccionVirtual + " → DF " + direccionFisica,
                direccionVirtual, direccionFisica, numeroPagina, desplazamiento, numeroMarco, pasos);
    }

    public boolean isExito() {
        return exito;
    }

    public boolean isFalloPagina() {
        return falloPagina;
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getDireccionVirtual() {
        return direccionVirtual;
    }

    public int getDireccionFisica() {
        return direccionFisica;
    }

    public int getNumeroPagina() {
        return numeroPagina;
    }

    public int getDesplazamiento() {
        return desplazamiento;
    }

    public int getNumeroMarco() {
        return numeroMarco;
    }

    public List<PasoTraduccion> getPasos() {
        return new ArrayList<>(pasos);
    }
}
