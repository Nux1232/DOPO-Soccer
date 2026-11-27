package domain.model.game.lifecycle;
import domain.model.exception.DopoSoccerException;

/** Centraliza el estado terminal de un partido y su resultado. */
public final class MatchLifecycle {
    /** Posibles estados del ciclo de vida de un partido. */
    public enum State {
        /** Partido en curso. */
        ACTIVE,
        /** Partido terminado con resultado definido. */
        FINISHED
    }

    private State state = State.ACTIVE;
    private int winner;

    /** @return estado actual del ciclo de vida */
    public State state() { return state; }
    /** @return {@code true} si el partido ya terminó */
    public boolean isFinished() { return state == State.FINISHED; }
    /** @return ganador registrado ({@code 0} empate, {@code 1} o {@code 2}) */
    public int winner() { return winner; }

    /**
     * Marca el partido como finalizado registrando al ganador.
     *
     * @param winner {@code 0} empate, {@code 1} primer jugador o {@code 2} segundo jugador
     * @throws DopoSoccerException si el identificador de ganador es inválido
     */
    public void finish(int winner) {
        if (winner < 0 || winner > 2) throw new DopoSoccerException("Resultado inválido");
        if (state == State.FINISHED) return;
        this.winner = winner;
        this.state = State.FINISHED;
    }
}
