package domain.model.physics;

import domain.model.entity.Player;
import domain.model.entity.PlayerStatusEffect;
import domain.model.game.Game;
import domain.model.game.PlayerInput;

/**
 * Traduce las entradas del jugador a movimiento y activación de poderes.
 * Los efectos que solo modifican la presentación, como {@code CONFUSED},
 * no cambian la interpretación de los controles.
 */
public final class PlayerInputHandler {
    private static final double PLAYER_MOVE_SPEED = 280.0;
    private static final double PLAYER_JUMP_SPEED = -560.0;

    /**
     * Aplica las entradas del jugador sobre su velocidad, orientación, salto y activación de aura,
     * respetando aturdimiento, bloqueos de saque, bloqueo de impacto y efectos de estado.
     *
     * @param player jugador sobre el cual aplicar las acciones
     * @param input acciones solicitadas en el cuadro actual
     * @param kickoffLocked {@code true} si el saque aún está bloqueado
     */
    public void apply(Player player, PlayerInput input, boolean kickoffLocked) {
        if (kickoffLocked || player.stunned()) { player.setVelocity(0, player.velocityY()); return; }
        if (player.impactLocked()) return;
        double horizontalDirection = input.horizontalDirection();
        player.setFacingDirection((int) horizontalDirection);
        if (input.power() && player.statusEffect().canKick()) player.activatePower();
        if (player.statusEffect() == PlayerStatusEffect.PUSHED) return;
        if (!player.statusEffect().canMove()) { player.setVelocity(0, player.velocityY()); return; }

        double speedMultiplier = 1.0 + 0.10 * (player.stats().speed() - 1);
        double velocityX = horizontalDirection * PLAYER_MOVE_SPEED * speedMultiplier
                * player.statusEffect().movementMultiplier();
        double velocityY = player.velocityY();
        boolean onGround = player.grounded() || player.y() + Player.RADIUS >= Game.GROUND_Y - 2.0;
        if (input.jump() && player.canJump() && onGround) {
            velocityY = PLAYER_JUMP_SPEED * (1.0 + 0.10 * (player.stats().jump() - 1));
            player.setPosition(player.x(), Game.GROUND_Y - Player.RADIUS - 1.5);
            player.setGrounded(false);
        }
        player.setVelocity(velocityX, velocityY);
    }
}
