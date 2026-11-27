
package tests;

import domain.factory.CountryFactory;
import domain.model.entity.*;
import domain.model.exception.DopoSoccerException;
import domain.model.game.*;
import domain.model.physics.PhysicsSimulation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DopoSoccerV2Test {

    // Motor físico falso: permite probar las reglas del dominio
    // sin ejecutar la simulación real de Dyn4j.
    private final PhysicsSimulation physics = new PhysicsSimulation() {
        @Override
        public void update(Game game, PlayerInput firstInput,
                           PlayerInput secondInput, double deltaSeconds) {
            // No realiza simulación física.
        }

        @Override
        public void kick(Player kicker, Player opponent,
                         Ball ball, int direction) {
            // No realiza una patada física.
        }
    };

    @Test
    void playerStartsWithFullHealth() {
        Player player = new LPlayer(CountryFactory.create("south-korea"), 100);

        assertEquals(100.0, player.health());
        assertEquals(0, player.goals());
        assertEquals(PowerState.NONE, player.power());
    }

    @Test
    void playerReceivesDamage() throws DopoSoccerException {
        Player player = new LPlayer(CountryFactory.create("cameroon"), 100);

        player.takeDamage(30);

        assertEquals(70.0, player.health());
    }

    @Test
    void negativeDamageThrowsException() {
        Player player = new LPlayer(CountryFactory.create("nigeria"), 100);

        assertThrows(DopoSoccerException.class,
                () -> player.takeDamage(-10));
    }

    @Test
    void playerGetsStunnedWhenHealthReachesZero()
            throws DopoSoccerException {

        Player player = new LPlayer(CountryFactory.create("usa"), 100);

        player.takeDamage(100);

        assertTrue(player.stunned());
        assertEquals(0.0, player.health());
        assertFalse(player.canJump());
        assertFalse(player.canKick());
    }

    @Test
    void playerRecoversAfterStunDuration()
            throws DopoSoccerException {

        Player player = new LPlayer(CountryFactory.create("usa"), 100);

        player.takeDamage(100);
        player.tick(Player.STUN_DURATION_SECONDS);

        assertFalse(player.stunned());
        assertEquals(player.maxHealth(), player.health());
    }

    @Test
    void playerCanRechargePower() {
        Player player = new LPlayer(CountryFactory.create("cameroon"), 100);

        player.tick(Player.POWER_CHARGE_SECONDS);

        assertEquals(Player.MAX_POWER_CHARGE,
                player.powerCharge());
    }

    @Test
    void playerActivatesAuraWithFullCharge() {
        Player player = new LPlayer(CountryFactory.create("south-korea"), 100);

        player.tick(Player.POWER_CHARGE_SECONDS);

        assertTrue(player.activatePower());
        assertEquals(PowerState.AURA, player.power());
        assertEquals(0.0, player.powerCharge());
    }

    @Test
    void playerCannotActivateAuraWithoutFullCharge() {
        Player player = new LPlayer(CountryFactory.create("south-korea"), 100);

        assertFalse(player.activatePower());
        assertEquals(PowerState.NONE, player.power());
    }

    @Test
    void playerConsumesActivePower() {
        Player player = new LPlayer(CountryFactory.create("south-korea"), 100);

        player.tick(Player.POWER_CHARGE_SECONDS);
        player.activatePower();

        assertTrue(player.consumePower());
        assertEquals(PowerState.NONE, player.power());
    }

    @Test
    void playerGetsPowerChargeWhenConcedingGoal() {
        Player player = new LPlayer(CountryFactory.create("usa"), 100);

        player.onGoalConceded();

        assertEquals(25.0, player.powerCharge());
    }

    @Test
    void playerStatusEffectExpires() {
        Player player = new LPlayer(CountryFactory.create("cameroon"), 100);

        player.applyStatusEffect(PlayerStatusEffect.ELECTRIFIED, 2);

        assertFalse(player.canKick());

        player.tick(2);

        assertEquals(PlayerStatusEffect.NONE, player.statusEffect());
        assertTrue(player.canKick());
    }

    // =========================
    // PRUEBAS DE PLAYER STATS
    // =========================

    @Test
    void statsStartAtInitialLevel() {
        PlayerStats stats = new PlayerStats();

        assertEquals(1, stats.speed());
        assertEquals(1, stats.kick());
        assertEquals(1, stats.jump());
        assertEquals(1, stats.survival());
    }

    @Test
    void statsCanBeImprovedWithEnoughPoints()
            throws DopoSoccerException {

        PlayerStats stats = new PlayerStats();

        stats.addPoints(500);
        stats.improveSpeed();

        assertEquals(2, stats.speed());
        assertEquals(0, stats.points());
        assertEquals(1000, stats.nextUpgradeCost());
    }

    @Test
    void negativePointsThrowException() {
        PlayerStats stats = new PlayerStats();

        assertThrows(DopoSoccerException.class,
                () -> stats.addPoints(-100));
    }

    @Test
    void insufficientPointsPreventImprovement() {
        PlayerStats stats = new PlayerStats();

        assertThrows(DopoSoccerException.class,
                stats::improveKick);

        assertEquals(1, stats.kick());
    }

    // =========================
    // PRUEBAS DE GAME
    // =========================

    @Test
    void gameIsCreatedWithCorrectPlayers()
            throws DopoSoccerException {

        Game game = new Game(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("south-korea"),
                CountryFactory.create("cameroon"),
                physics
        );

        assertNotNull(game.firstPlayer());
        assertNotNull(game.secondPlayer());
        assertNotNull(game.ball());
    }

    @Test
    void gameRejectsNullConfiguration() {
        assertThrows(DopoSoccerException.class,
                () -> new Game(
                        null,
                        CountryFactory.create("south-korea"),
                        CountryFactory.create("cameroon"),
                        physics
                ));
    }

    @Test
    void scoringGoalIncreasesPlayerScore()
            throws DopoSoccerException {

        Game game = new Game(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("south-korea"),
                CountryFactory.create("cameroon"),
                physics
        );

        game.scoreGoal(1);

        assertEquals(1, game.firstPlayer().goals());
        assertEquals(0, game.secondPlayer().goals());
    }

    @Test
    void invalidScoringPlayerDoesNotIncreaseScore()
            throws DopoSoccerException {

        Game game = new Game(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("south-korea"),
                CountryFactory.create("cameroon"),
                physics
        );

        game.scoreGoal(3);

        assertEquals(0, game.firstPlayer().goals());
        assertEquals(0, game.secondPlayer().goals());
    }

    @Test
    void negativeKickoffTimeThrowsException()
            throws DopoSoccerException {

        Game game = new Game(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("south-korea"),
                CountryFactory.create("cameroon"),
                physics
        );

        assertThrows(DopoSoccerException.class,
                () -> game.updateKickoffDelay(-1));
    }

    // =========================
    // PRUEBAS DE GOAL GAME
    // =========================

    @Test
    void goalGameRejectsZeroGoalLimit() {
        assertThrows(DopoSoccerException.class,
                () -> new GoalGame(
                        PlayerMode.PLAYER_VS_PLAYER,
                        CountryFactory.create("south-korea"),
                        CountryFactory.create("cameroon"),
                        physics,
                        0
                ));
    }

    @Test
    void goalGameFinishesWhenPlayerReachesGoalLimit()
            throws DopoSoccerException {

        GoalGame game = new GoalGame(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("south-korea"),
                CountryFactory.create("cameroon"),
                physics,
                2
        );

        game.scoreGoal(1);
        game.scoreGoal(1);

        assertTrue(game.isFinished());
        assertEquals(1, game.winner());
    }

    @Test
    void goalGameDoesNotFinishBeforeGoalLimit()
            throws DopoSoccerException {

        GoalGame game = new GoalGame(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("south-korea"),
                CountryFactory.create("cameroon"),
                physics,
                3
        );

        game.scoreGoal(1);

        assertFalse(game.isFinished());
    }

    // =========================
    // PRUEBAS DE TIME GAME
    // =========================

    @Test
    void timeGameRejectsZeroDuration() {
        assertThrows(DopoSoccerException.class,
                () -> new TimeGame(
                        PlayerMode.PLAYER_VS_PLAYER,
                        CountryFactory.create("south-korea"),
                        CountryFactory.create("cameroon"),
                        physics,
                        0
                ));
    }

    @Test
    void timeGameStartsWithConfiguredDuration()
            throws DopoSoccerException {

        TimeGame game = new TimeGame(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("south-korea"),
                CountryFactory.create("cameroon"),
                physics,
                240
        );

        assertEquals(240, game.remainingSeconds());
        assertEquals("04:00", game.formattedTime());
    }

    @Test
    void timeGameUpdatesRemainingTime()
            throws DopoSoccerException {

        TimeGame game = new TimeGame(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("south-korea"),
                CountryFactory.create("cameroon"),
                physics,
                240
        );

        game.update(PlayerInput.NONE, PlayerInput.NONE, 61);

        assertEquals(179, game.remainingSeconds());
        assertEquals("02:59", game.formattedTime());
    }

    @Test
    void timeGameEntersSuddenDeathWhenTimeRunsOutInTie()
            throws DopoSoccerException {

        TimeGame game = new TimeGame(
                PlayerMode.PLAYER_VS_PLAYER,
                CountryFactory.create("south-korea"),
                CountryFactory.create("cameroon"),
                physics,
                1
        );

        game.update(PlayerInput.NONE, PlayerInput.NONE, 1);

        assertFalse(game.isFinished());
        assertTrue(game.suddenDeath());
        assertEquals(0, game.winner());
        assertEquals("MUERTE SÚBITA — GOL DE ORO", game.modeDescription());
    }

}
