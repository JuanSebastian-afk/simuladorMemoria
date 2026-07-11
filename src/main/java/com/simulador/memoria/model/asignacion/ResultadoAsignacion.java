package com.simulador.memoria.model.asignacion;

/**
 * Resultado de una operación de asignación o liberación de memoria.
 */
public class ResultadoAsignacion {

    private final boolean exito;
    private final String mensaje;
    private final int direccionAsignada;

    private ResultadoAsignacion(boolean exito, String mensaje, int direccionAsignada) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.direccionAsignada = direccionAsignada;
    }

    public static ResultadoAsignacion exito(String mensaje, int direccion) {
        return new ResultadoAsignacion(true, mensaje, direccion);
    }

    public static ResultadoAsignacion fallo(String mensaje) {
        return new ResultadoAsignacion(false, mensaje, -1);
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getDireccionAsignada() {
        return direccionAsignada;
    }
}
