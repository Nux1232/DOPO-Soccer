package domain.model.ai;
import domain.model.entity.Player;
import domain.model.entity.Ball;
import domain.model.game.PlayerInput;
/** Deterministic lightweight opponent AI, independent from presentation/key input. */
public final class FootballAI {
 public PlayerInput decide(Player player, Player opponent, Ball ball) {
  double dx=ball.x()-player.x(), dy=ball.y()-player.y();
  boolean left=dx < -12, right=dx > 12;
  double distance=Math.hypot(dx,dy);
  boolean kick=distance <= Player.RADIUS+Ball.RADIUS+48 && !player.stunned();
  boolean jump=dy < -Player.RADIUS*0.65 && Math.abs(dx)<125 && player.grounded();
  boolean power=player.powerCharge()>=Player.MAX_POWER_CHARGE && distance<260;
  return new PlayerInput(left,right,jump,kick,power);
 }
}
