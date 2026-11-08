package presentation.controller.engine;

import domain.model.game.Game;
import domain.model.physics.PhysicsEngine;
import presentation.controller.input.PlayerInput;

/**
 * Coordina el avance del juego cuadro a cuadro.
 *
 * En cada cuadro recibe los controles de los dos jugadores y le pide al
 * PhysicsEngine que avance la simulación. Además hace dos cosas que
 * el motor de física no hace dentro de update: descontar la espera
 * del saque y ejecutar las patadas (que solo se permiten cuando esa espera
 * ya terminó).
 *
 * Esta clase no dibuja nada ni lee el teclado: recibe los controles ya
 * armados y modifica el Game que se le entregó.
 */
public final class GameEngine {

    /** Motor de física que mueve a los jugadores y al balón. */
    private final PhysicsEngine physicsEngine = new PhysicsEngine();

    /** Partido que este motor hace avanzar. */
    private final Game match;

    /**
     * Crea un motor para un partido.
     *
     * @param match partido que se va a hacer avanzar
     */
    public GameEngine(Game match) {
        this.match = match;
    }

    /**
     * Devuelve el partido que controla este motor.
     *
     * @return el partido en curso
     */
    public Game match() {
        return match;
    }

    /**
     * Avanza el partido un cuadro.
     *
     * Pasos, en orden:
     *
     * El motor de física mueve a los jugadores y al balón y revisa
     * si hubo gol.
     * Se descuenta de la espera del saque el tiempo transcurrido
     * (limitado a 1/30 de segundo, igual que en la física).
     * Si la espera del saque ya terminó, se ejecuta la patada de
     * cada jugador que la haya pedido: el primero patea hacia la
     * derecha (1) y el segundo hacia la izquierda (-1).
     *
     * @param firstInput controles del primer jugador
     * @param secondInput controles del segundo jugador
     * @param deltaSeconds segundos transcurridos desde el cuadro anterior
     */
    public void update(PlayerInput firstInput, PlayerInput secondInput, double deltaSeconds) {
        physicsEngine.update(match, firstInput, secondInput, deltaSeconds);
        double delta = Math.min(deltaSeconds, 1.0 / 30.0);
        match.updateKickoffDelay(delta);
        if (!match.kickoffDelayActive()) {
            if (firstInput.kick()) {
                physicsEngine.kick(match.firstPlayer(), match.ball(), 1);
            }
            if (secondInput.kick()) {
                physicsEngine.kick(match.secondPlayer(), match.ball(), -1);
            }
        }
    }
}