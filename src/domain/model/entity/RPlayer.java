package domain.model.entity;
/** Jugador situado en el lado derecho del campo. */
public final class RPlayer extends Player {
    /** @return {@link PlayerSide#RIGHT} */
    @Override public PlayerSide side() { return PlayerSide.RIGHT; }

    /**
     * Crea un jugador derecho orientado hacia la izquierda.
     *
     * @param country selección nacional representada
     * @param x posición horizontal inicial en la cancha
     */
    public RPlayer(Country country, double x) { super(country, x); setFacingDirection(-1); }
}
