package presentation.controller.engine;

import domain.model.game.Game;
import domain.model.physics.PhysicsEngine;
import presentation.controller.input.PlayerInput;

public final class GameEngine {
    private final PhysicsEngine physicsEngine;
    private final Game match;
    private boolean firstKickWasPressed;
    private boolean secondKickWasPressed;

    public GameEngine(Game match) {
        this.match = match;
        physicsEngine = new PhysicsEngine(match);
    }

    public Game match() {
        return match;
    }

    public void update(PlayerInput firstInput, PlayerInput secondInput, double deltaSeconds) {
        boolean firstKickPressed = firstInput.kick() && !firstKickWasPressed;
        boolean secondKickPressed = secondInput.kick() && !secondKickWasPressed;
        firstKickWasPressed = firstInput.kick();
        secondKickWasPressed = secondInput.kick();

        double delta = physicsEngine.update(firstInput, secondInput, deltaSeconds,
                !match.kickoffDelayActive());
        match.updateKickoffDelay(delta);
        if (!match.kickoffDelayActive()) {
            if (firstKickPressed) {
                physicsEngine.kick(match.firstPlayer(), 1);
            }
            if (secondKickPressed) {
                physicsEngine.kick(match.secondPlayer(), -1);
            }
        }
    }
}
