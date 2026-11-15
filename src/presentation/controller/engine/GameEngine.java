package presentation.controller.engine;

import domain.model.game.Game;
import domain.model.game.PlayerInput;

/**
 * Controlador que conecta la vista con el modelo del partido cuadro a cuadro.
 */
public final class GameEngine {

    /** Partido que este controlador hace avanzar. */
    private final Game match;

    public GameEngine(Game match) {
        this.match = match;
    }

    public Game match() {
        return match;
    }

    public void update(PlayerInput firstInput, PlayerInput secondInput, double deltaSeconds) {
        match.update(firstInput, secondInput, deltaSeconds);
    }
}