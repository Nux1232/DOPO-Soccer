
package domain.model.game;

import domain.model.entity.Country;
import domain.model.exception.DopoSoccerException;
import domain.model.physics.PhysicsSimulation;
import domain.model.game.lifecycle.MatchClock;

/**
 * Partido que termina cuando se agota el tiempo.
 */
public final class TimeGame extends Game {

    private final MatchClock clock;
    private boolean suddenDeath;

    /**
     * Crea un partido por tiempo reglamentario con soporte para muerte súbita.
     *
     * @param playerMode modalidad de participación
     * @param firstCountry país del jugador izquierdo
     * @param secondCountry país del jugador derecho
     * @param physicsSimulation motor físico inyectado
     * @param durationSeconds duración reglamentaria en segundos
     * @throws DopoSoccerException si la duración es menor o igual que cero
     */
    public TimeGame(
            PlayerMode playerMode,
            Country firstCountry,
            Country secondCountry,
            PhysicsSimulation physicsSimulation,
            int durationSeconds
    ) throws DopoSoccerException {

        super(playerMode, firstCountry, secondCountry, physicsSimulation);

        if (durationSeconds <= 0) {
            throw new DopoSoccerException(
                    DopoSoccerException.INVALID_MATCH_DURATION
            );
        }

        this.clock = new MatchClock(durationSeconds);
    }

    /** @return duración total configurada en segundos */
    public int durationSeconds() {
        return clock.durationSeconds();
    }

    /**
     * Indica si el tiempo reglamentario concluyó en empate y el partido está en gol de oro.
     *
     * @return {@code true} si el partido se encuentra en muerte súbita
     */
    public boolean suddenDeath() {
        return suddenDeath;
    }

    @Override
    public void scoreGoal(int scoringPlayer) {
        if (isFinished()) return;
        super.scoreGoal(scoringPlayer);
        if (suddenDeath) {
            int result = compareScores();
            if (result != 0) finishMatch(result > 0 ? 1 : 2);
        }
    }

    /**
     * Restaura el reloj interno y el estado de muerte súbita al cargar una partida.
     *
     * @param remaining segundos restantes en el reloj
     * @param suddenDeath {@code true} si la partida estaba en muerte súbita
     */
    public void restoreClock(double remaining, boolean suddenDeath) {
        clock.restoreRemaining(remaining);
        this.suddenDeath = suddenDeath;
    }

    /** @return tiempo restante exacto en segundos */
    public double remainingExactSeconds() { return clock.remainingExactSeconds(); }

    @Override
    public MatchConfiguration matchConfiguration() {
        return new MatchConfiguration(MatchType.TIME, 10, durationSeconds(),
                remainingExactSeconds(), suddenDeath);
    }

    @Override
    public void restoreMatchConfiguration(MatchConfiguration configuration) {
        if (configuration == null || !MatchType.TIME.equals(configuration.type())
                || configuration.durationSeconds() != durationSeconds()) {
            throw new DopoSoccerException("La configuración guardada no corresponde a este partido.");
        }
        clock.restoreRemaining(configuration.remainingSeconds());
        suddenDeath = configuration.suddenDeath();
    }

    /** @return segundos restantes redondeados hacia arriba para visualización */
    public int remainingSeconds() {
        return clock.remainingSeconds();
    }

    /** @return tiempo restante formateado como {@code MM:SS} */
    public String formattedTime() {
        return clock.formatted();
    }
    @Override
    public String modeDescription() {
        return suddenDeath ? "MUERTE SÚBITA — GOL DE ORO"
                : "TIEMPO RESTANTE: " + formattedTime();
    }

    @Override
    public void update(
            PlayerInput firstInput,
            PlayerInput secondInput,
            double deltaSeconds
    ) throws DopoSoccerException {

        if (isFinished()) {
            return;
        }

        if (deltaSeconds < 0) {
            throw new DopoSoccerException(
                    DopoSoccerException.NEGATIVE_TIME
            );
        }

        super.update(firstInput, secondInput, deltaSeconds);

        if (!isFinished() && !suddenDeath) {
            clock.advance(deltaSeconds);

            if (clock.expired()) {
                int result = compareScores();
                if (result == 0) {
                    // El empate al finalizar el tiempo reglamentario activa muerte súbita.
                    // El partido sigue activo hasta que se marque un gol.
                    suddenDeath = true;
                } else {
                    finishMatch(result > 0 ? 1 : 2);
                }
            }
        }
    }
}
