package com.simulador.memoria.model.paginacion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gestor del esquema de paginación de un solo nivel.
 * Administra memoria física, marcos libres, tablas de páginas y traducción de direcciones.
 */
public class GestorPaginacion {

    private final int memoriaFisicaTotal;
    private final int tamanoPagina;
    private final List<Marco> marcos;
    private final List<Integer> marcosDisponibles;
    private final Map<String, TablaPaginas> tablasProcesos;

    public GestorPaginacion(int memoriaFisicaTotal, int tamanoPagina) {
        if (tamanoPagina <= 0 || memoriaFisicaTotal <= 0) {
            throw new IllegalArgumentException("Memoria física y tamaño de página deben ser positivos.");
        }
        if (memoriaFisicaTotal % tamanoPagina != 0) {
            throw new IllegalArgumentException("La memoria física debe ser múltiplo del tamaño de página.");
        }

        this.memoriaFisicaTotal = memoriaFisicaTotal;
        this.tamanoPagina = tamanoPagina;
        this.marcos = new ArrayList<>();
        this.marcosDisponibles = new ArrayList<>();
        this.tablasProcesos = new LinkedHashMap<>();

        int totalMarcos = memoriaFisicaTotal / tamanoPagina;
        for (int i = 0; i < totalMarcos; i++) {
            marcos.add(new Marco(i));
            marcosDisponibles.add(i);
        }
    }

    public void reiniciar() {
        marcos.clear();
        marcosDisponibles.clear();
        tablasProcesos.clear();

        int totalMarcos = memoriaFisicaTotal / tamanoPagina;
        for (int i = 0; i < totalMarcos; i++) {
            marcos.add(new Marco(i));
            marcosDisponibles.add(i);
        }
    }

    public TablaPaginas registrarProceso(String idProceso, int espacioVirtual) {
        TablaPaginas tabla = new TablaPaginas(idProceso, espacioVirtual, tamanoPagina);
        tablasProcesos.put(idProceso, tabla);
        return tabla;
    }

    /**
     * Carga una página del proceso en un marco físico específico.
     */
    public boolean cargarPagina(String idProceso, int numeroPagina, int numeroMarco) {
        TablaPaginas tabla = tablasProcesos.get(idProceso);
        if (tabla == null || numeroPagina < 0 || numeroPagina >= tabla.getTotalPaginas()) {
            return false;
        }
        if (numeroMarco < 0 || numeroMarco >= marcos.size()) {
            return false;
        }

        Marco marco = marcos.get(numeroMarco);
        if (!marco.isLibre() && !idProceso.equals(marco.getIdProceso())) {
            return false;
        }

        if (marco.isLibre()) {
            marcosDisponibles.remove(Integer.valueOf(numeroMarco));
        }

        marco.asignar(idProceso, numeroPagina);
        tabla.cargarPagina(numeroPagina, numeroMarco);
        return true;
    }

    /**
     * Asigna automáticamente un marco libre para cargar la página.
     */
    public Optional<Integer> cargarPaginaAutomatica(String idProceso, int numeroPagina) {
        if (marcosDisponibles.isEmpty()) {
            return Optional.empty();
        }
        int marco = marcosDisponibles.remove(0);
        if (cargarPagina(idProceso, numeroPagina, marco)) {
            return Optional.of(marco);
        }
        return Optional.empty();
    }

    /**
     * Traduce una dirección virtual a física mostrando el cálculo paso a paso.
     */
    public ResultadoTraduccion traducirDireccion(String idProceso, int direccionVirtual) {
        List<PasoTraduccion> pasos = new ArrayList<>();

        TablaPaginas tabla = tablasProcesos.get(idProceso);
        if (tabla == null) {
            pasos.add(new PasoTraduccion(1, "Verificar proceso",
                    "Proceso = " + idProceso, "Proceso no encontrado"));
            return ResultadoTraduccion.fallo("El proceso " + idProceso + " no está registrado.", direccionVirtual, pasos);
        }

        pasos.add(new PasoTraduccion(1, "Verificar dirección virtual",
                String.format("0 ≤ DV (%d) < Espacio Virtual (%d)", direccionVirtual, tabla.getEspacioVirtual()),
                tabla.direccionValida(direccionVirtual) ? "Dirección válida" : "Dirección fuera de rango"));

        if (!tabla.direccionValida(direccionVirtual)) {
            return ResultadoTraduccion.fallo("Dirección virtual fuera del espacio del proceso.", direccionVirtual, pasos);
        }

        int numeroPagina = tabla.obtenerNumeroPagina(direccionVirtual);
        int desplazamiento = tabla.obtenerDesplazamiento(direccionVirtual);

        pasos.add(new PasoTraduccion(2, "Calcular número de página",
                String.format("Página = DV ÷ Tamaño_Página = %d ÷ %d", direccionVirtual, tamanoPagina),
                "Página = " + numeroPagina));

        pasos.add(new PasoTraduccion(3, "Calcular desplazamiento (offset)",
                String.format("Offset = DV mod Tamaño_Página = %d mod %d", direccionVirtual, tamanoPagina),
                "Offset = " + desplazamiento));

        EntradaPagina entrada = tabla.getEntrada(numeroPagina);
        pasos.add(new PasoTraduccion(4, "Consultar tabla de páginas",
                String.format("Tabla[%d] → Marco=%d, V=%s", numeroPagina,
                        entrada.getNumeroMarco(), entrada.isValida() ? "1" : "0"),
                entrada.toString()));

        if (!entrada.isValida()) {
            pasos.add(new PasoTraduccion(5, "Resultado",
                    "Bit de validez = 0", "FALLO DE PÁGINA (Page Fault)"));
            return ResultadoTraduccion.falloPagina(
                    "FALLO DE PÁGINA: La página " + numeroPagina + " no está cargada en memoria física.",
                    direccionVirtual, numeroPagina, desplazamiento, pasos);
        }

        int numeroMarco = entrada.getNumeroMarco();
        int direccionFisica = (numeroMarco * tamanoPagina) + desplazamiento;

        pasos.add(new PasoTraduccion(5, "Calcular dirección física",
                String.format("DF = (Marco × Tamaño_Página) + Offset = (%d × %d) + %d",
                        numeroMarco, tamanoPagina, desplazamiento),
                "DF = " + direccionFisica));

        pasos.add(new PasoTraduccion(6, "Resultado final",
                String.format("DV %d → DF %d", direccionVirtual, direccionFisica),
                "Traducción exitosa"));

        return ResultadoTraduccion.exito(direccionVirtual, direccionFisica,
                numeroPagina, desplazamiento, numeroMarco, pasos);
    }

    public int getMemoriaFisicaTotal() {
        return memoriaFisicaTotal;
    }

    public int getTamanoPagina() {
        return tamanoPagina;
    }

    public int getTotalMarcos() {
        return marcos.size();
    }

    public List<Marco> getMarcos() {
        return new ArrayList<>(marcos);
    }

    public List<Integer> getMarcosDisponibles() {
        return new ArrayList<>(marcosDisponibles);
    }

    public Map<String, TablaPaginas> getTablasProcesos() {
        return new LinkedHashMap<>(tablasProcesos);
    }

    public List<String> getIdsProcesos() {
        return new ArrayList<>(tablasProcesos.keySet());
    }
}
