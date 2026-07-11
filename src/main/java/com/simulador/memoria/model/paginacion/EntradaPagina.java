package com.simulador.memoria.model.paginacion;

/**
 * Entrada de la tabla de páginas: mapeo página -> marco con bit de validez.
 */
public class EntradaPagina {

    private final int numeroPagina;
    private int numeroMarco;
    private boolean valida;

    public EntradaPagina(int numeroPagina) {
        this.numeroPagina = numeroPagina;
        this.numeroMarco = -1;
        this.valida = false;
    }

    public int getNumeroPagina() {
        return numeroPagina;
    }

    public int getNumeroMarco() {
        return numeroMarco;
    }

    public boolean isValida() {
        return valida;
    }

    public void cargar(int numeroMarco) {
        this.numeroMarco = numeroMarco;
        this.valida = true;
    }

    public void invalidar() {
        this.numeroMarco = -1;
        this.valida = false;
    }

    @Override
    public String toString() {
        if (!valida) {
            return String.format("Pág. %d -> INVÁLIDA", numeroPagina);
        }
        return String.format("Pág. %d -> Marco %d [V=1]", numeroPagina, numeroMarco);
    }
}
