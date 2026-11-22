package domain.model.game.lifecycle;
import domain.model.exception.DopoSoccerException;

/** Centraliza el estado terminal de un partido y su resultado. */
public final class MatchLifecycle {
    public enum State { ACTIVE, FINISHED }

    private State state = State.ACTIVE;
    private int winner;

    public State state() { return state; }
    public boolean isFinished() { return state == State.FINISHED; }
    public int winner() { return winner; }

    /** Resultado: 0 empate, 1 jugador uno, 2 jugador dos. */
    public void finish(int winner) {
        if (winner < 0 || winner > 2) throw new DopoSoccerException("Resultado inválido");
        if (state == State.FINISHED) return;
        this.winner = winner;
        this.state = State.FINISHED;
    }
}
