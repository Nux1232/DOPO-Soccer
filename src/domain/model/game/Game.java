package domain.model.game;

import domain.model.entity.Ball;
import domain.model.entity.Country;
import domain.model.entity.Player;

public final class Game {
    public static final double WIDTH = 960;
    public static final double HEIGHT = 540;
    public static final double GROUND_Y = 480;
    public static final double GOAL_TOP = 365;
    public static final double GOAL_DEPTH = 90;

    private final PlayerMode playerMode;
    private final Player firstPlayer;
    private final Player secondPlayer;
    private final Ball ball;
    private double kickoffDelay = 1.1;

    public Game(PlayerMode playerMode, Country firstCountry, Country secondCountry) {
        if (playerMode == null || firstCountry == null || secondCountry == null) {
            throw new IllegalArgumentException("La modalidad y los países son obligatorios.");
        }
        this.playerMode = playerMode;
        firstPlayer = new Player(firstCountry, WIDTH / 2 - 60);
        secondPlayer = new Player(secondCountry, WIDTH / 2 + 60);
        ball = new Ball(WIDTH / 2, 40);
    }

    public PlayerMode playerMode() {
        return playerMode;
    }

    public Player firstPlayer() {
        return firstPlayer;
    }

    public Player secondPlayer() {
        return secondPlayer;
    }

    public Ball ball() {
        return ball;
    }

    public boolean kickoffDelayActive() {
        return kickoffDelay > 0;
    }

    public void scoreGoal(int playerNumber) {
        if (playerNumber != 1 && playerNumber != 2) {
            return;
        }
        Player scorer = playerNumber == 1 ? firstPlayer : secondPlayer;
        scorer.scoreGoal();
        prepareKickoff();
    }

    public void updateKickoffDelay(double seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("El tiempo no puede ser negativo.");
        }
        kickoffDelay = Math.max(0, kickoffDelay - seconds);
    }

    private void prepareKickoff() {
        firstPlayer.moveTo(WIDTH / 2 - 60, 38);
        secondPlayer.moveTo(WIDTH / 2 + 60, 38);
        ball.setPosition(WIDTH / 2, 25);
        ball.setVelocity(0, 0);
        kickoffDelay = 1.1;
    }

}
