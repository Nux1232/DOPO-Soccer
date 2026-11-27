package domain.model.entity;
/**
 * Adaptador de compatibilidad para código que instancia explícitamente al jugador derecho.
 *
 * @deprecated crea {@link Player} indicando {@link PlayerSide#RIGHT}
 */
@Deprecated
public final class RPlayer extends Player {
    /**
     * Crea un jugador derecho orientado hacia la izquierda.
     *
     * @param country selección nacional representada
     * @param x posición horizontal inicial en la cancha
     */
    public RPlayer(Country country, double x) { super(country, x, PlayerSide.RIGHT); }
}
