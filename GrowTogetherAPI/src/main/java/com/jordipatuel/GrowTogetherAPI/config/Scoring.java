package com.jordipatuel.GrowTogetherAPI.config;

/**
 * Constantes de puntuación de desafíos.
 * Cada día completado otorga {@link #PUNTOS_BASE} multiplicados por un bonus
 * que crece con la racha actual. La racha rota reinicia el multiplicador.
 */
public final class Scoring {

    /** Puntos base que otorga un día COMPLETADO sin racha (racha = 1). */
    public static final int PUNTOS_BASE = 10;

    /** Incremento del multiplicador por cada día adicional de racha. 0.10 = +10% por día. */
    public static final double BONUS_POR_DIA_RACHA = 0.10;

    /** Tope de días que aporta bonus. Más allá de este valor el multiplicador no crece. */
    public static final int TOPE_RACHA = 20;

    private Scoring() {
        // No instanciable
    }

    /**
     * Calcula el multiplicador de puntos según la racha de días consecutivos.
     * Para racha=1 devuelve 1.0; para racha=10 devuelve 1.9; para racha>=21 devuelve 3.0 (tope).
     */
    public static double multiplicador(int racha) {
        if (racha <= 0) return 0;
        int diasBonus = Math.min(racha - 1, TOPE_RACHA);
        return 1.0 + diasBonus * BONUS_POR_DIA_RACHA;
    }

    /**
     * Calcula los puntos que otorga un día completado dado el valor de racha resultante.
     */
    public static int puntosDia(int racha) {
        if (racha <= 0) return 0;
        return (int) Math.round(PUNTOS_BASE * multiplicador(racha));
    }
}
