package domain.model.physics;
import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.game.PlayerInput;

/**
 * Contrato que define las operaciones de simulación física
 * necesarias para ejecutar un partido.
 *
 * Game depende de esta abstracción y no de un motor
 * físico específico como Dyn4j.
 *
 * Esto permite sustituir o extender la implementación
 * física sin modificar las reglas generales del partido.
 */
public interface PhysicsSimulation {

    /**
     * Avanza la simulación física del partido.
     *
     * @param game partido que se está simulando
     * @param firstInput controles del primer jugador
     * @param secondInput controles del segundo jugador
     * @param deltaSeconds tiempo transcurrido
     */
    void update(
            Game game,
            PlayerInput firstInput,
            PlayerInput secondInput,
            double deltaSeconds
    );

    /**
     * Ejecuta una patada considerando al jugador que patea,
     * al oponente y al balón.
     *
     * @param kicker jugador que ejecuta la patada
     * @param opponent jugador contrario
     * @param ball balón del partido
     * @param direction dirección de la patada
     */
    void kick(
            Player kicker,
            Player opponent,
            Ball ball,
            int direction
    ) throws DopoSoccerException;
}
