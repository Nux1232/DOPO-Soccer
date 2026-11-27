package domain.model.entity;
/**
 * Adaptador de compatibilidad para código que instancia explícitamente al jugador izquierdo.
 *
 * @deprecated crea {@link Player} indicando {@link PlayerSide#LEFT}
 */
@Deprecated
public final class LPlayer extends Player {
    /**
     * Crea un jugador izquierdo orientado hacia la derecha.
     *
     * @param country selección nacional representada
     * @param x posición horizontal inicial en la cancha
     */
    public LPlayer(Country country, double x) { super(country, x, PlayerSide.LEFT); }
}
