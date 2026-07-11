package com.simulador.memoria.model.paginacion;

/**
 * Representa un marco (frame) de memoria física.
 */
public class Marco {

    private final int numero;
    private boolean libre;
    private String idProceso;
    private int numeroPagina;

    public Marco(int numero) {
        this.numero = numero;
        this.libre = true;
        this.idProceso = null;
        this.numeroPagina = -1;
    }

    public int getNumero() {
        return numero;
    }

    public boolean isLibre() {
        return libre;
    }

    public String getIdProceso() {
        return idProceso;
    }

    public int getNumeroPagina() {
        return numeroPagina;
    }

    public void asignar(String idProceso, int numeroPagina) {
        this.libre = false;
        this.idProceso = idProceso;
        this.numeroPagina = numeroPagina;
    }

    public void liberar() {
        this.libre = true;
        this.idProceso = null;
        this.numeroPagina = -1;
    }

    public int getDireccionInicio(int tamanoPagina) {
        return numero * tamanoPagina;
    }

    @Override
    public String toString() {
        if (libre) {
            return String.format("Marco %d [LIBRE]", numero);
        }
        return String.format("Marco %d [%s - Pág. %d]", numero, idProceso, numeroPagina);
    }
}
