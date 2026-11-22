package domain.model.game.lifecycle;
import domain.model.exception.DopoSoccerException;

/** Reloj de partido con duración y cuenta regresiva encapsuladas. */
public final class MatchClock {
    private final int durationSeconds;
    private double remainingSeconds;

    public MatchClock(int durationSeconds) {
        if (durationSeconds <= 0) throw new DopoSoccerException("La duración debe ser positiva");
        this.durationSeconds = durationSeconds;
        this.remainingSeconds = durationSeconds;
    }

    public int durationSeconds() { return durationSeconds; }
    public double remainingExactSeconds() { return remainingSeconds; }
    public int remainingSeconds() { return (int) Math.ceil(remainingSeconds); }
    public boolean expired() { return remainingSeconds <= 0.0; }

    public void restoreRemaining(double remaining) { remainingSeconds = Math.max(0, Math.min(durationSeconds, remaining)); }

    public void advance(double deltaSeconds) {
        if (deltaSeconds < 0) throw new DopoSoccerException("El delta no puede ser negativo");
        remainingSeconds = Math.max(0.0, remainingSeconds - deltaSeconds);
    }

    public String formatted() {
        int total = remainingSeconds();
        return String.format("%02d:%02d", total / 60, total % 60);
    }
}
