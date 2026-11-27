package domain.model.ai;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.entity.PlayerSide;
import domain.model.game.Game;
import domain.model.game.PlayerInput;

import java.util.Random;

/** Estilos de comportamiento disponibles para la máquina, implementados como estrategias. */
public enum AIProfile {
    /** Perfil torpe que toma decisiones aleatorias sin coordinación táctica. */
    CLUMSY("TORPE", (random, machine, human, ball, machineGoals, humanGoals) -> {
        boolean left = random.nextBoolean();
        boolean right = random.nextBoolean();
        boolean jump = random.nextInt(8) == 0;
        boolean kick = random.nextInt(5) == 0;
        boolean power = random.nextInt(20) == 0;
        return new PlayerInput(left, right, jump, kick, power);
    }),
    /** Perfil ofensivo que persigue constantemente el balón para atacar. */
    OFFENSIVE("OFENSIVA", (random, machine, human, ball, machineGoals, humanGoals) -> {
        boolean moveLeft = ball.x() < machine.x();
        boolean moveRight = ball.x() > machine.x();
        boolean nearBall = distance(machine, ball) < Player.RADIUS + Ball.RADIUS + 45;
        boolean jump = ball.y() < machine.y() - 35 && Math.abs(ball.x() - machine.x()) < 110;
        return new PlayerInput(moveLeft, moveRight, jump, nearBall,
                nearBall && machine.powerCharge() >= Player.MAX_POWER_CHARGE);
    }),
    /** Perfil defensivo que resguarda su propio campo hasta que el balón se acerca. */
    DEFENSIVE("DEFENSIVA", (random, machine, human, ball, machineGoals, humanGoals) -> {
        boolean rightSide = machine.side() != PlayerSide.LEFT;
        double guardX = rightSide ? Game.WIDTH - 200.0 : 200.0;
        boolean ballInOwnHalf = rightSide ? ball.x() > Game.WIDTH - 360.0 : ball.x() < 360.0;
        double targetX = ballInOwnHalf ? ball.x() : guardX;
        boolean left = targetX < machine.x() - 12;
        boolean right = targetX > machine.x() + 12;
        boolean nearBall = distance(machine, ball) < Player.RADIUS + Ball.RADIUS + 35;
        boolean jump = ball.y() < machine.y() - 30 && Math.abs(ball.x() - machine.x()) < 95;
        return new PlayerInput(left, right, jump, nearBall,
                nearBall && machine.powerCharge() >= Player.MAX_POWER_CHARGE);
    }),
    /** Perfil experto que adapta su posicionamiento según el marcador del partido. */
    EXPERT("EXPERTA", (random, machine, human, ball, machineGoals, humanGoals) -> {
        boolean rightSide = machine.side() != PlayerSide.LEFT;
        double distance = distance(machine, ball);
        boolean losing = machineGoals < humanGoals;
        boolean winning = machineGoals > humanGoals;

        double targetX = ball.x();
        if (winning) {
            if (rightSide && ball.x() < 420.0) {
                targetX = Math.max(610.0, ball.x());
            } else if (!rightSide && ball.x() > Game.WIDTH - 420.0) {
                targetX = Math.min(Game.WIDTH - 610.0, ball.x());
            }
        } else if (!losing) {
            if (rightSide && ball.x() < 300.0) {
                targetX = Math.max(500.0, ball.x());
            } else if (!rightSide && ball.x() > Game.WIDTH - 300.0) {
                targetX = Math.min(Game.WIDTH - 500.0, ball.x());
            }
        }

        boolean moveLeft = targetX < machine.x() - 10;
        boolean moveRight = targetX > machine.x() + 10;
        boolean close = distance < Player.RADIUS + Ball.RADIUS + 42;
        boolean jump = ball.y() < machine.y() - 25
                && Math.abs(ball.x() - machine.x()) < (losing ? 145 : 115)
                && machine.grounded();
        boolean usePower = close && machine.powerCharge() >= Player.MAX_POWER_CHARGE;
        return new PlayerInput(moveLeft, moveRight, jump, close, usePower);
    });

    /** Contrato de estrategia que decide las acciones de la máquina en cada cuadro. */
    @FunctionalInterface
    public interface Strategy {
        /**
         * Calcula la entrada de control de la máquina para el cuadro actual.
         *
         * @param random generador pseudoaleatorio del perfil
         * @param machine jugador controlado por la máquina
         * @param human jugador oponente
         * @param ball balón del partido
         * @param machineGoals goles actuales de la máquina
         * @param humanGoals goles actuales del oponente
         * @return entrada de control decidida
         */
        PlayerInput decide(Random random, Player machine, Player human, Ball ball,
                int machineGoals, int humanGoals);
    }

    private final String displayName;
    private final Strategy strategy;

    AIProfile(String displayName, Strategy strategy) {
        this.displayName = displayName;
        this.strategy = strategy;
    }

    /**
     * Devuelve el nombre legible en español del perfil de IA.
     *
     * @return nombre visible del perfil
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Ejecuta la estrategia asociada a este perfil de inteligencia artificial.
     *
     * @param random generador pseudoaleatorio
     * @param machine jugador controlado por la máquina
     * @param human jugador rival
     * @param ball balón del partido
     * @param machineGoals goles de la máquina
     * @param humanGoals goles del rival
     * @return entrada de control generada
     */
    public PlayerInput decide(Random random, Player machine, Player human, Ball ball,
            int machineGoals, int humanGoals) {
        return strategy.decide(random, machine, human, ball, machineGoals, humanGoals);
    }

    private static double distance(Player player, Ball ball) {
        return Math.hypot(ball.x() - player.x(), ball.y() - player.y());
    }
}

