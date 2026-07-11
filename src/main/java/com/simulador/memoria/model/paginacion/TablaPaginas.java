package com.simulador.memoria.model.paginacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabla de páginas de un solo nivel para un proceso.
 * Mapea números de página a marcos físicos con bit de validez.
 */
public class TablaPaginas {

    private final String idProceso;
    private final int espacioVirtual;
    private final int tamanoPagina;
    private final List<EntradaPagina> entradas;

    public TablaPaginas(String idProceso, int espacioVirtual, int tamanoPagina) {
        this.idProceso = idProceso;
        this.espacioVirtual = espacioVirtual;
        this.tamanoPagina = tamanoPagina;
        this.entradas = new ArrayList<>();

        int totalPaginas = (int) Math.ceil((double) espacioVirtual / tamanoPagina);
        for (int i = 0; i < totalPaginas; i++) {
            entradas.add(new EntradaPagina(i));
        }
    }

    public String getIdProceso() {
        return idProceso;
    }

    public int getEspacioVirtual() {
        return espacioVirtual;
    }

    public int getTamanoPagina() {
        return tamanoPagina;
    }

    public int getTotalPaginas() {
        return entradas.size();
    }

    public List<EntradaPagina> getEntradas() {
        return new ArrayList<>(entradas);
    }

    public EntradaPagina getEntrada(int numeroPagina) {
        if (numeroPagina < 0 || numeroPagina >= entradas.size()) {
            return null;
        }
        return entradas.get(numeroPagina);
    }

    public void cargarPagina(int numeroPagina, int numeroMarco) {
        if (numeroPagina >= 0 && numeroPagina < entradas.size()) {
            entradas.get(numeroPagina).cargar(numeroMarco);
        }
    }

    public void descargarPagina(int numeroPagina) {
        if (numeroPagina >= 0 && numeroPagina < entradas.size()) {
            entradas.get(numeroPagina).invalidar();
        }
    }

    /**
     * Obtiene el número de página a partir de una dirección virtual.
     */
    public int obtenerNumeroPagina(int direccionVirtual) {
        return direccionVirtual / tamanoPagina;
    }

    /**
     * Obtiene el desplazamiento (offset) dentro de la página.
     */
    public int obtenerDesplazamiento(int direccionVirtual) {
        return direccionVirtual % tamanoPagina;
    }

    public boolean direccionValida(int direccionVirtual) {
        return direccionVirtual >= 0 && direccionVirtual < espacioVirtual;
    }
}
