package domain.model.ai;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.game.PlayerInput;

import java.util.Random;

/** IA sencilla con cuatro estilos seleccionables. */
public class FootballAI {
    private final Random random = new Random();

    public PlayerInput decide(AIProfile profile, Player machine, Player human, Ball ball,
            int machineGoals, int humanGoals) {
        if (profile == null) profile = AIProfile.EXPERT;

        if (profile == AIProfile.CLUMSY) {
            return decideClumsy(machine, ball);
        }
        if (profile == AIProfile.OFFENSIVE) {
            return decideOffensive(machine, ball);
        }
        if (profile == AIProfile.DEFENSIVE) {
            return decideDefensive(machine, ball);
        }
        return decideExpert(machine, human, ball, machineGoals, humanGoals);
    }

    private PlayerInput decideClumsy(Player player, Ball ball) {
        // La máquina torpe elige acciones simples de manera aleatoria.
        boolean left = random.nextBoolean();
        boolean right = random.nextBoolean();
        boolean jump = random.nextInt(8) == 0;
        boolean kick = random.nextInt(5) == 0;
        boolean power = random.nextInt(20) == 0;
        return new PlayerInput(left, right, jump, kick, power);
    }

    private PlayerInput decideOffensive(Player player, Ball ball) {
        boolean moveLeft = ball.x() < player.x();
        boolean moveRight = ball.x() > player.x();
        boolean nearBall = distance(player, ball) < Player.RADIUS + Ball.RADIUS + 45;
        boolean jump = ball.y() < player.y() - 35 && Math.abs(ball.x() - player.x()) < 110;
        return new PlayerInput(moveLeft, moveRight, jump, nearBall, nearBall && player.powerCharge() >= Player.MAX_POWER_CHARGE);
    }

    private PlayerInput decideDefensive(Player player, Ball ball) {
        // El jugador derecho defiende su arco: no se aleja demasiado de su mitad.
        double guardX = 760;
        double targetX = ball.x() > 600 ? ball.x() : guardX;
        boolean left = targetX < player.x() - 12;
        boolean right = targetX > player.x() + 12;
        boolean nearBall = distance(player, ball) < Player.RADIUS + Ball.RADIUS + 35;
        boolean jump = ball.y() < player.y() - 30 && Math.abs(ball.x() - player.x()) < 95;
        return new PlayerInput(left, right, jump, nearBall, nearBall && player.powerCharge() >= Player.MAX_POWER_CHARGE);
    }

    private PlayerInput decideExpert(Player player, Player opponent, Ball ball,
            int machineGoals, int humanGoals) {
        double distance = distance(player, ball);
        boolean losing = machineGoals < humanGoals;
        boolean winning = machineGoals > humanGoals;

        // El jugador derecho ataca hacia la izquierda y protege su arco a la derecha.
        double targetX = ball.x();
        if (winning && ball.x() < 420) {
            // Cuando va ganando, no abandona innecesariamente su zona defensiva.
            targetX = Math.max(610, ball.x());
        } else if (losing) {
            // Cuando va perdiendo, intenta acercarse al balón con más agresividad.
            targetX = ball.x();
        } else if (ball.x() < 300) {
            // Si el balón está lejos del arco propio, mantiene una posición intermedia.
            targetX = Math.max(500, ball.x());
        }

        boolean moveLeft = targetX < player.x() - 10;
        boolean moveRight = targetX > player.x() + 10;
        boolean close = distance < Player.RADIUS + Ball.RADIUS + 42;
        boolean jump = ball.y() < player.y() - 25
                && Math.abs(ball.x() - player.x()) < (losing ? 145 : 115)
                && player.grounded();
        boolean usePower = close && player.powerCharge() >= Player.MAX_POWER_CHARGE;
        return new PlayerInput(moveLeft, moveRight, jump, close, usePower);
    }

    private double distance(Player player, Ball ball) {
        return Math.hypot(ball.x() - player.x(), ball.y() - player.y());
    }
}
