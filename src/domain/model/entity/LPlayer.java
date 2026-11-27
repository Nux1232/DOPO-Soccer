package domain.model.entity;
/** Jugador situado en el lado izquierdo del campo. */
public final class LPlayer extends Player {
    /** @return {@link PlayerSide#LEFT} */
    @Override public PlayerSide side() { return PlayerSide.LEFT; }

    /**
     * Crea un jugador izquierdo orientado hacia la derecha.
     *
     * @param country selección nacional representada
     * @param x posición horizontal inicial en la cancha
     */
    public LPlayer(Country country, double x) { super(country, x); setFacingDirection(1); }
}
