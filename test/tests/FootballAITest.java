package tests;

import domain.factory.CountryFactory;
import domain.model.ai.AIProfile;
import domain.model.ai.FootballAI;
import domain.model.entity.Ball;
import domain.model.entity.LPlayer;
import domain.model.entity.Player;
import domain.model.entity.RPlayer;
import domain.model.game.Game;
import domain.model.game.PlayerInput;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FootballAITest {

    @Test
    void offensiveProfileChasesBallRegardlessOfSide() {
        FootballAI ai = new FootballAI(new Random(42));
        Player rightMachine = new RPlayer(CountryFactory.create("KR"), 600);
        Player leftHuman = new LPlayer(CountryFactory.create("CM"), 200);
        Ball ballLeftOfRightMachine = new Ball(400, Game.GROUND_Y - Ball.RADIUS);

        PlayerInput rightMove = ai.decide(AIProfile.OFFENSIVE, rightMachine, leftHuman, ballLeftOfRightMachine, 0, 0);
        assertTrue(rightMove.left());
        assertFalse(rightMove.right());

        Player leftMachine = new LPlayer(CountryFactory.create("KR"), 300);
        Player rightHuman = new RPlayer(CountryFactory.create("CM"), 700);
        Ball ballRightOfLeftMachine = new Ball(500, Game.GROUND_Y - Ball.RADIUS);

        PlayerInput leftMove = ai.decide(AIProfile.OFFENSIVE, leftMachine, rightHuman, ballRightOfLeftMachine, 0, 0);
        assertTrue(leftMove.right());
        assertFalse(leftMove.left());
    }

    @Test
    void defensiveProfileGuardsOwnGoalSymmetrically() {
        FootballAI ai = new FootballAI(new Random(42));
        Player rightMachine = new RPlayer(CountryFactory.create("NG"), 500);
        Player leftHuman = new LPlayer(CountryFactory.create("USA"), 200);
        Ball ballFarLeft = new Ball(200, Game.GROUND_Y - Ball.RADIUS);

        PlayerInput rightRetreat = ai.decide(AIProfile.DEFENSIVE, rightMachine, leftHuman, ballFarLeft, 0, 0);
        assertTrue(rightRetreat.right(), "Right-side defender should fall back toward guardX=760");

        Player leftMachine = new LPlayer(CountryFactory.create("NG"), 450);
        Player rightHuman = new RPlayer(CountryFactory.create("USA"), 750);
        Ball ballFarRight = new Ball(750, Game.GROUND_Y - Ball.RADIUS);

        PlayerInput leftRetreat = ai.decide(AIProfile.DEFENSIVE, leftMachine, rightHuman, ballFarRight, 0, 0);
        assertTrue(leftRetreat.left(), "Left-side defender should fall back toward guardX=200");
    }

    @Test
    void expertAndClumsyProfilesReturnValidInputs() {
        FootballAI ai = new FootballAI(new Random(1));
        Player machine = new RPlayer(CountryFactory.create("USA"), 700);
        machine.setPosition(700, Game.GROUND_Y - Player.RADIUS);
        machine.tick(Player.POWER_CHARGE_SECONDS);
        Player human = new LPlayer(CountryFactory.create("KR"), 250);
        Ball nearBall = new Ball(670, Game.GROUND_Y - Ball.RADIUS);

        PlayerInput expert = ai.decide(AIProfile.EXPERT, machine, human, nearBall, 1, 2);
        assertTrue(expert.kick());
        assertTrue(expert.power());

        PlayerInput clumsy = ai.decide(AIProfile.CLUMSY, machine, human, nearBall, 0, 0);
        assertNotNull(clumsy);
    }
}
