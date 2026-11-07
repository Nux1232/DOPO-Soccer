package presentation.controller.engine;

import domain.model.game.Game;
import domain.model.physics.PhysicsEngine;
import presentation.controller.input.PlayerInput;

public final class GameEngine {
    private final PhysicsEngine physicsEngine = new PhysicsEngine();
    private final Game match;

    public GameEngine(Game match) {
        this.match = match;
    }

    public Game match() {
        return match;
    }

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
