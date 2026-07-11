package com.simulador.memoria.model.asignacion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestor central de la memoria principal.
 * Mantiene la lista de bloques, aplica estrategias de asignación y calcula fragmentación.
 */
public class GestorMemoria {

    private final int memoriaTotal;
    private final List<BloqueMemoria> bloques;
    private final Map<String, Proceso> procesos;
    private EstrategiaAsignacion estrategia;

    public GestorMemoria(int memoriaTotal) {
        this.memoriaTotal = memoriaTotal;
        this.bloques = new ArrayList<>();
        this.procesos = new LinkedHashMap<>();
        this.estrategia = new FirstFit();
        reiniciar();
    }

    public GestorMemoria(int memoriaTotal, TipoEstrategia tipo) {
        this(memoriaTotal);
        setEstrategia(tipo);
    }

    public void reiniciar() {
        bloques.clear();
        procesos.clear();
        bloques.add(new BloqueMemoria(0, memoriaTotal, true));
    }

    public void setEstrategia(TipoEstrategia tipo) {
        this.estrategia = switch (tipo) {
            case BEST_FIT -> new BestFit();
            case WORST_FIT -> new WorstFit();
            default -> new FirstFit();
        };
    }

    public void setEstrategia(EstrategiaAsignacion estrategia) {
        this.estrategia = estrategia;
    }

    public ResultadoAsignacion asignar(String idProceso, int tamano) {
        if (tamano <= 0) {
            return ResultadoAsignacion.fallo("El tamaño del proceso debe ser mayor a 0.");
        }
        if (tamano > memoriaTotal) {
            return ResultadoAsignacion.fallo("El proceso excede el tamaño total de la memoria.");
        }
        if (procesos.containsKey(idProceso)) {
            return ResultadoAsignacion.fallo("Ya existe un proceso con ID: " + idProceso);
        }

        int indice = estrategia.buscarBloque(bloques, tamano);
        if (indice == -1) {
            return ResultadoAsignacion.fallo("No hay espacio contiguo suficiente para el proceso " + idProceso);
        }

        BloqueMemoria bloque = bloques.get(indice);
        int direccion = bloque.getDireccionInicio();

        if (bloque.getTamano() == tamano) {
            bloques.set(indice, new BloqueMemoria(direccion, tamano, idProceso, tamano));
        } else {
            BloqueMemoria ocupado = new BloqueMemoria(direccion, tamano, idProceso, tamano);
            BloqueMemoria sobrante = new BloqueMemoria(direccion + tamano, bloque.getTamano() - tamano, true);
            bloques.set(indice, ocupado);
            bloques.add(indice + 1, sobrante);
        }

        Proceso proceso = new Proceso(idProceso, tamano);
        proceso.setAsignado(true);
        proceso.setDireccionAsignada(direccion);
        procesos.put(idProceso, proceso);

        return ResultadoAsignacion.exito(
                "Proceso " + idProceso + " asignado en dirección " + direccion + " usando " + estrategia.getNombre(),
                direccion
        );
    }

    public ResultadoAsignacion liberar(String idProceso) {
        if (!procesos.containsKey(idProceso)) {
            return ResultadoAsignacion.fallo("No existe el proceso: " + idProceso);
        }

        int indice = -1;
        for (int i = 0; i < bloques.size(); i++) {
            BloqueMemoria bloque = bloques.get(i);
            if (!bloque.isLibre() && idProceso.equals(bloque.getIdProceso())) {
                indice = i;
                break;
            }
        }

        if (indice == -1) {
            return ResultadoAsignacion.fallo("El proceso no tiene bloque asignado en memoria.");
        }

        BloqueMemoria liberado = bloques.get(indice);
        bloques.set(indice, new BloqueMemoria(liberado.getDireccionInicio(), liberado.getTamano(), true));
        procesos.remove(idProceso);

        fusionarBloquesAdyacentes(indice);

        return ResultadoAsignacion.exito("Memoria del proceso " + idProceso + " liberada correctamente.", -1);
    }

    /**
     * Fusiona bloques libres adyacentes para reducir fragmentación externa.
     */
    private void fusionarBloquesAdyacentes(int indice) {
        if (indice > 0 && bloques.get(indice - 1).isLibre() && bloques.get(indice).isLibre()) {
            BloqueMemoria anterior = bloques.get(indice - 1);
            BloqueMemoria actual = bloques.get(indice);
            bloques.set(indice - 1, new BloqueMemoria(anterior.getDireccionInicio(),
                    anterior.getTamano() + actual.getTamano(), true));
            bloques.remove(indice);
            indice--;
        }

        if (indice < bloques.size() - 1 && bloques.get(indice).isLibre() && bloques.get(indice + 1).isLibre()) {
            BloqueMemoria actual = bloques.get(indice);
            BloqueMemoria siguiente = bloques.get(indice + 1);
            bloques.set(indice, new BloqueMemoria(actual.getDireccionInicio(),
                    actual.getTamano() + siguiente.getTamano(), true));
            bloques.remove(indice + 1);
        }
    }

    /**
     * Fragmentación externa = (memoria libre total - mayor bloque libre) / memoria libre total * 100
     */
    public double calcularFragmentacionExterna() {
        int memoriaLibre = bloques.stream()
                .filter(BloqueMemoria::isLibre)
                .mapToInt(BloqueMemoria::getTamano)
                .sum();

        if (memoriaLibre == 0) {
            return 0.0;
        }

        int mayorBloqueLibre = bloques.stream()
                .filter(BloqueMemoria::isLibre)
                .mapToInt(BloqueMemoria::getTamano)
                .max()
                .orElse(0);

        return ((double) (memoriaLibre - mayorBloqueLibre) / memoriaLibre) * 100.0;
    }

    /**
     * Fragmentación interna total: suma del espacio desperdiciado dentro de bloques ocupados.
     */
    public int calcularFragmentacionInternaTotal() {
        return bloques.stream()
                .filter(b -> !b.isLibre())
                .mapToInt(BloqueMemoria::getFragmentacionInterna)
                .sum();
    }

    public double calcularPorcentajeFragmentacionInterna() {
        int memoriaOcupada = bloques.stream()
                .filter(b -> !b.isLibre())
                .mapToInt(BloqueMemoria::getTamano)
                .sum();

        if (memoriaOcupada == 0) {
            return 0.0;
        }
        return ((double) calcularFragmentacionInternaTotal() / memoriaOcupada) * 100.0;
    }

    public int getMemoriaTotal() {
        return memoriaTotal;
    }

    public List<BloqueMemoria> getBloques() {
        return bloques.stream().map(BloqueMemoria::copia).collect(Collectors.toList());
    }

    public Map<String, Proceso> getProcesos() {
        return new LinkedHashMap<>(procesos);
    }

    public EstrategiaAsignacion getEstrategia() {
        return estrategia;
    }

    public int getMemoriaLibre() {
        return bloques.stream().filter(BloqueMemoria::isLibre).mapToInt(BloqueMemoria::getTamano).sum();
    }

    public int getMemoriaOcupada() {
        return memoriaTotal - getMemoriaLibre();
    }
}
