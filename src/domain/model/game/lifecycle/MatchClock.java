package domain.model.game.lifecycle;
import domain.model.exception.DopoSoccerException;

/** Reloj de partido con duración y cuenta regresiva encapsuladas. */
public final class MatchClock {
    private final int durationSeconds;
    private double remainingSeconds;

    /**
     * Crea un reloj con la duración reglamentaria indicada.
     *
     * @param durationSeconds duración total en segundos
     * @throws DopoSoccerException si la duración no es positiva
     */
    public MatchClock(int durationSeconds) {
        if (durationSeconds <= 0) throw new DopoSoccerException("La duración debe ser positiva");
        this.durationSeconds = durationSeconds;
        this.remainingSeconds = durationSeconds;
    }

    /** @return duración total configurada en segundos */
    public int durationSeconds() { return durationSeconds; }
    /** @return tiempo restante exacto en segundos */
    public double remainingExactSeconds() { return remainingSeconds; }
    /** @return segundos restantes redondeados hacia arriba */
    public int remainingSeconds() { return (int) Math.ceil(remainingSeconds); }
    /** @return {@code true} cuando el tiempo llegó a cero */
    public boolean expired() { return remainingSeconds <= 0.0; }

    /**
     * Restaura el tiempo restante dentro de los límites válidos del reloj.
     *
     * @param remaining segundos restantes a restaurar
     */
    public void restoreRemaining(double remaining) { remainingSeconds = Math.max(0, Math.min(durationSeconds, remaining)); }

    /**
     * Descuenta el intervalo transcurrido del reloj.
     *
     * @param deltaSeconds segundos transcurridos
     * @throws DopoSoccerException si el intervalo es negativo
     */
    public void advance(double deltaSeconds) {
        if (deltaSeconds < 0) throw new DopoSoccerException("El delta no puede ser negativo");
        remainingSeconds = Math.max(0.0, remainingSeconds - deltaSeconds);
    }

    /** @return representación textual {@code MM:SS} del tiempo restante */
    public String formatted() {
        int total = remainingSeconds();
        return String.format("%02d:%02d", total / 60, total % 60);
    }
}
