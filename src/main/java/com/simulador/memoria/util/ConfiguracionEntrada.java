package com.simulador.memoria.util;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa la configuración cargada desde un archivo de entrada.
 */
public class ConfiguracionEntrada {

    private String tipo = "asignacion";
    private int memoriaTotal = 1024;
    private String estrategia = "FIRST_FIT";
    private int memoriaFisica = 16384;
    private int tamanoPagina = 4096;

    private List<ProcesoAsignacion> procesosAsignacion = new ArrayList<>();
    private List<Operacion> operaciones = new ArrayList<>();
    private List<ProcesoPaginacion> procesosPaginacion = new ArrayList<>();
    private List<Traduccion> traducciones = new ArrayList<>();

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getMemoriaTotal() {
        return memoriaTotal;
    }

    public void setMemoriaTotal(int memoriaTotal) {
        this.memoriaTotal = memoriaTotal;
    }

    public String getEstrategia() {
        return estrategia;
    }

    public void setEstrategia(String estrategia) {
        this.estrategia = estrategia;
    }

    public int getMemoriaFisica() {
        return memoriaFisica;
    }

    public void setMemoriaFisica(int memoriaFisica) {
        this.memoriaFisica = memoriaFisica;
    }

    public int getTamanoPagina() {
        return tamanoPagina;
    }

    public void setTamanoPagina(int tamanoPagina) {
        this.tamanoPagina = tamanoPagina;
    }

    public List<ProcesoAsignacion> getProcesosAsignacion() {
        return procesosAsignacion;
    }

    public void setProcesosAsignacion(List<ProcesoAsignacion> procesosAsignacion) {
        this.procesosAsignacion = procesosAsignacion;
    }

    public List<Operacion> getOperaciones() {
        return operaciones;
    }

    public void setOperaciones(List<Operacion> operaciones) {
        this.operaciones = operaciones;
    }

    public List<ProcesoPaginacion> getProcesosPaginacion() {
        return procesosPaginacion;
    }

    public void setProcesosPaginacion(List<ProcesoPaginacion> procesosPaginacion) {
        this.procesosPaginacion = procesosPaginacion;
    }

    public List<Traduccion> getTraducciones() {
        return traducciones;
    }

    public void setTraducciones(List<Traduccion> traducciones) {
        this.traducciones = traducciones;
    }

    public boolean esPaginacion() {
        return "paginacion".equalsIgnoreCase(tipo);
    }

    public static class ProcesoAsignacion {
        private final String id;
        private final int tamano;

        public ProcesoAsignacion(String id, int tamano) {
            this.id = id;
            this.tamano = tamano;
        }

        public String getId() {
            return id;
        }

        public int getTamano() {
            return tamano;
        }
    }

    public static class Operacion {
        private final String accion;
        private final String procesoId;
        private final int tamano;

        public Operacion(String accion, String procesoId, int tamano) {
            this.accion = accion;
            this.procesoId = procesoId;
            this.tamano = tamano;
        }

        public String getAccion() {
            return accion;
        }

        public String getProcesoId() {
            return procesoId;
        }

        public int getTamano() {
            return tamano;
        }
    }

    public static class ProcesoPaginacion {
        private final String id;
        private final int espacioVirtual;
        private final List<PaginaCargada> paginasCargadas = new ArrayList<>();

        public ProcesoPaginacion(String id, int espacioVirtual) {
            this.id = id;
            this.espacioVirtual = espacioVirtual;
        }

        public String getId() {
            return id;
        }

        public int getEspacioVirtual() {
            return espacioVirtual;
        }

        public List<PaginaCargada> getPaginasCargadas() {
            return paginasCargadas;
        }
    }

    public static class PaginaCargada {
        private final int pagina;
        private final int marco;

        public PaginaCargada(int pagina, int marco) {
            this.pagina = pagina;
            this.marco = marco;
        }

        public int getPagina() {
            return pagina;
        }

        public int getMarco() {
            return marco;
        }
    }

    public static class Traduccion {
        private final String procesoId;
        private final int direccionVirtual;

        public Traduccion(String procesoId, int direccionVirtual) {
            this.procesoId = procesoId;
            this.direccionVirtual = direccionVirtual;
        }

        public String getProcesoId() {
            return procesoId;
        }

        public int getDireccionVirtual() {
            return direccionVirtual;
        }
    }
}
