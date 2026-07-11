package com.simulador.memoria.model.asignacion;

/**
 * Representa un bloque contiguo de memoria principal.
 * Puede estar libre u ocupado por un proceso.
 */
public class BloqueMemoria {

    private final int direccionInicio;
    private int tamano;
    private boolean libre;
    private String idProceso;
    private int tamanoSolicitado;

    public BloqueMemoria(int direccionInicio, int tamano, boolean libre) {
        this.direccionInicio = direccionInicio;
        this.tamano = tamano;
        this.libre = libre;
        this.idProceso = null;
        this.tamanoSolicitado = 0;
    }

    public BloqueMemoria(int direccionInicio, int tamano, String idProceso, int tamanoSolicitado) {
        this.direccionInicio = direccionInicio;
        this.tamano = tamano;
        this.libre = false;
        this.idProceso = idProceso;
        this.tamanoSolicitado = tamanoSolicitado;
    }

    public int getDireccionInicio() {
        return direccionInicio;
    }

    public int getDireccionFin() {
        return direccionInicio + tamano - 1;
    }

    public int getTamano() {
        return tamano;
    }

    public void setTamano(int tamano) {
        this.tamano = tamano;
    }

    public boolean isLibre() {
        return libre;
    }

    public void setLibre(boolean libre) {
        this.libre = libre;
        if (libre) {
            this.idProceso = null;
            this.tamanoSolicitado = 0;
        }
    }

    public String getIdProceso() {
        return idProceso;
    }

    public void setIdProceso(String idProceso) {
        this.idProceso = idProceso;
    }

    public int getTamanoSolicitado() {
        return tamanoSolicitado;
    }

    public void setTamanoSolicitado(int tamanoSolicitado) {
        this.tamanoSolicitado = tamanoSolicitado;
    }

    /**
     * Fragmentación interna: espacio desperdiciado dentro del bloque asignado.
     * En partición dinámica ocurre cuando el bloque asignado es mayor al solicitado.
     */
    public int getFragmentacionInterna() {
        if (libre || tamanoSolicitado == 0) {
            return 0;
        }
        return Math.max(0, tamano - tamanoSolicitado);
    }

    public BloqueMemoria copia() {
        if (libre) {
            return new BloqueMemoria(direccionInicio, tamano, true);
        }
        BloqueMemoria copia = new BloqueMemoria(direccionInicio, tamano, idProceso, tamanoSolicitado);
        return copia;
    }

    @Override
    public String toString() {
        if (libre) {
            return String.format("LIBRE [%d - %d] (%d KB)", direccionInicio, getDireccionFin(), tamano);
        }
        return String.format("%s [%d - %d] (%d KB)", idProceso, direccionInicio, getDireccionFin(), tamano);
    }
}
