package presentation.view.render;

/**
 * Constantes geométricas compartidas entre el renderizado y la capa de efectos visuales
 * para mantener alineadas las dimensiones de cada jugador.
 */
public final class PlayerVisualMetrics {
    /** Ancho visual base del jugador en unidades lógicas de cancha. */
    public static final double WIDTH = 56;
    /** Alto visual base del jugador en unidades lógicas de cancha. */
    public static final double HEIGHT = 80;
    /** Desplazamiento desde el centro físico hacia el borde superior de la cabeza. */
    public static final double TOP_OFFSET = 50;
    /** Desplazamiento desde el centro físico hacia la base de los pies. */
    public static final double BOTTOM_OFFSET = 30;
    /** Desplazamiento vertical del centro visual respecto al centro físico. */
    public static final double CENTER_OFFSET_Y = (BOTTOM_OFFSET - TOP_OFFSET) / 2;

    private PlayerVisualMetrics() {
    }

    /**
     * Devuelve el factor de escala horizontal respecto al modelo base de dibujo.
     *
     * @return escala horizontal
     */
    public static double scaleX() {
        return WIDTH / 56;
    }

    /**
     * Devuelve el factor de escala vertical respecto al modelo base de dibujo.
     *
     * @return escala vertical
     */
    public static double scaleY() {
        return HEIGHT / 80;
    }
}
