package domain.model.entity;

/**
 * Estadísticas de un jugador: velocidad, patada, salto y supervivencia,
 * junto con los puntos acumulados que se gastan para mejorarlas.
 *
 * Cada estadística tiene un valor entre 1 y 10. Mejorar una estadística
 * sube su valor en 1 y gasta 1 punto.
 */
public final class PlayerStats {

    /** Nivel de velocidad de desplazamiento. */
    private int speed = 5;

    /** Nivel de patada. */
    private int kick = 5;

    /** Nivel de salto. */
    private int jump = 5;

    /** Nivel de supervivencia (vida máxima). */
    private int survival = 5;

    /** Puntos acumulados disponibles para mejorar estadísticas. */
    private int points;

    /**
     * Devuelve el nivel de velocidad.
     *
     * @return el nivel de velocidad actual
     */
    public int speed() {
        return speed;
    }

    /**
     * Devuelve el nivel de patada.
     *
     * @return el nivel de patada actual
     */
    public int kick() {
        return kick;
    }

    /**
     * Devuelve el nivel de salto.
     *
     * @return el nivel de salto actual
     */
    public int jump() {
        return jump;
    }

    /**
     * Devuelve el nivel de supervivencia.
     *
     * @return el nivel de supervivencia actual
     */
    public int survival() {
        return survival;
    }

    /**
     * Devuelve los puntos disponibles para mejorar estadísticas.
     *
     * @return puntos acumulados sin gastar
     */
    public int points() {
        return points;
    }

    /**
     * Suma puntos a los disponibles del jugador.
     *
     * @param amount cantidad de puntos a sumar; no puede ser negativa
     * @throws IllegalArgumentException si la cantidad es negativa
     */
    public void addPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("La cantidad de puntos no puede ser negativa.");
        }
        points += amount;
    }

    /**
     * Mejora la velocidad en 1 nivel, gastando 1 punto.
     *
     * @throws IllegalStateException si no hay puntos o ya está en el máximo
     */
    public void improveSpeed() {
        speed = improve(speed);
    }

    /**
     * Mejora la patada en 1 nivel, gastando 1 punto.
     *
     * @throws IllegalStateException si no hay puntos o ya está en el máximo
     */
    public void improveKick() {
        kick = improve(kick);
    }

    /**
     * Mejora el salto en 1 nivel, gastando 1 punto.
     *
     * @throws IllegalStateException si no hay puntos o ya está en el máximo
     */
    public void improveJump() {
        jump = improve(jump);
    }

    /**
     * Mejora la supervivencia en 1 nivel, gastando 1 punto.
     *
     * @throws IllegalStateException si no hay puntos o ya está en el máximo
     */
    public void improveSurvival() {
        survival = improve(survival);
    }

    /**
     * Calcula la mejora de una estadística: valida que se pueda mejorar,
     * gasta 1 punto y devuelve el nuevo valor. Este método no modifica la
     * estadística por sí mismo; quien lo llama debe guardar el valor devuelto.
     *
     * @param currentValue valor actual de la estadística
     * @return el valor mejorado (el actual más 1)
     * @throws IllegalStateException si no hay puntos disponibles o la
     *         estadística ya está en 10
     */
    private int improve(int currentValue) {
        if (points == 0) {
            throw new IllegalStateException("No hay puntos disponibles para mejorar estadísticas.");
        }
        if (currentValue >= 10) {
            throw new IllegalStateException("La estadística ya está en su máximo.");
        }
        points--;
        return currentValue + 1;
    }
}