package com.simulador.memoria.model.asignacion;

/**
 * Representa un proceso que solicita memoria en el esquema de asignación contigua.
 */
public class Proceso {

    private final String id;
    private final int tamanoSolicitado;
    private int direccionAsignada;
    private boolean asignado;

    public Proceso(String id, int tamanoSolicitado) {
        this.id = id;
        this.tamanoSolicitado = tamanoSolicitado;
        this.direccionAsignada = -1;
        this.asignado = false;
    }

    public String getId() {
        return id;
    }

    public int getTamanoSolicitado() {
        return tamanoSolicitado;
    }

    public int getDireccionAsignada() {
        return direccionAsignada;
    }

    public void setDireccionAsignada(int direccionAsignada) {
        this.direccionAsignada = direccionAsignada;
    }

    public boolean isAsignado() {
        return asignado;
    }

    public void setAsignado(boolean asignado) {
        this.asignado = asignado;
    }

    @Override
    public String toString() {
        return String.format("%s (%d KB)", id, tamanoSolicitado);
    }
}
