package com.simulador.memoria.model.asignacion;

/**
 * Estrategias de asignación de memoria disponibles.
 */
public enum TipoEstrategia {
    FIRST_FIT("First Fit"),
    BEST_FIT("Best Fit"),
    WORST_FIT("Worst Fit");

    private final String nombre;

    TipoEstrategia(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public static TipoEstrategia desdeTexto(String texto) {
        if (texto == null) {
            return FIRST_FIT;
        }
        return switch (texto.toUpperCase().replace(" ", "_")) {
            case "BEST_FIT", "BESTFIT" -> BEST_FIT;
            case "WORST_FIT", "WORSTFIT" -> WORST_FIT;
            default -> FIRST_FIT;
        };
    }
}
