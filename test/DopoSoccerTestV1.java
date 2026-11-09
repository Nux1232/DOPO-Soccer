package tests;

import domain.model.entity.Ball;
import domain.model.entity.Country;
import domain.model.entity.Player;
import domain.model.entity.PlayerStats;
import domain.model.entity.Power;
import domain.model.game.Game;
import domain.model.game.GameMatchConfig;
import domain.model.game.GameMode;
import domain.model.game.PlayerMode;
import domain.model.physics.PhysicsEngine;
import org.junit.jupiter.api.Test;
import presentation.controller.engine.GameEngine;
import presentation.controller.input.PlayerInput;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de DOPOSoccer (versión uno), reunidas en un solo archivo.
 *
 * Las pruebas están agrupadas por clase probada: Ball, Country, PlayerStats,
 * Player, GameMatchConfig, Game, PhysicsEngine, PlayerInput y GameEngine. El
 * nombre de cada prueba empieza con la clase que prueba.
 *
 * Las pruebas siguen el patrón preparar, actuar y verificar, y no fijan
 * valores que todavía están pendientes de corregir (estadísticas iniciales,
 * velocidad de carga del poder, duración del aura).
 */
class DopoSoccerTest {

    /** Un cuadro a 60 fotogramas por segundo. */
    private static final double FRAME = 1.0 / 60.0;

    /** Países usados para crear partidos de prueba. */
    private final Country firstCountry = Country.values()[0];
    private final Country secondCountry = Country.values()[1];

    /** Motor de física usado por las pruebas de física. */
    private final PhysicsEngine physicsEngine = new PhysicsEngine();

    /** Controles de un jugador que solo está pateando. */
    private final PlayerInput kickInput = new PlayerInput(false, false, false, true, false);

    /** Crea un partido jugador contra jugador con los dos primeros países. */
    private Game newGame() {
        return new Game(PlayerMode.PLAYER_VS_PLAYER, firstCountry, secondCountry);
    }

    /** Crea un jugador de prueba en x = 100. */
    private Player newPlayer() {
        return new Player(Country.values()[0], 100);
    }

    // Ball

    @Test
    void ball_nuevoBalon_guardaLaPosicionInicialYEmpiezaQuieto() {
        Ball ball = new Ball(100, 50);

        assertEquals(100.0, ball.x(), 1e-9);
        assertEquals(50.0, ball.y(), 1e-9);
        assertEquals(0.0, ball.velocityX(), 1e-9);
        assertEquals(0.0, ball.velocityY(), 1e-9);
    }

    @Test
    void ball_setPosition_cambiaLaPosicionSinTocarLaVelocidad() {
        Ball ball = new Ball(0, 0);
        ball.setVelocity(3, 4);

        ball.setPosition(10, 20);

        assertEquals(10.0, ball.x(), 1e-9);
        assertEquals(20.0, ball.y(), 1e-9);
        assertEquals(3.0, ball.velocityX(), 1e-9);
        assertEquals(4.0, ball.velocityY(), 1e-9);
    }

    @Test
    void ball_setVelocity_cambiaLaVelocidadSinTocarLaPosicion() {
        Ball ball = new Ball(7, 8);

        ball.setVelocity(-5, 9);

        assertEquals(-5.0, ball.velocityX(), 1e-9);
        assertEquals(9.0, ball.velocityY(), 1e-9);
        assertEquals(7.0, ball.x(), 1e-9);
        assertEquals(8.0, ball.y(), 1e-9);
    }

    // Country

    @Test
    void country_incluyeLosCuatroPaisesDelAnexo1() {
        Set<String> nombres = Arrays.stream(Country.values())
                .map(Country::displayName)
                .collect(Collectors.toSet());

        assertEquals(Set.of("Corea del Sur", "Camerún", "Nigeria", "Estados Unidos"), nombres);
    }

    @Test
    void country_toString_devuelveElNombreVisibleYNoElDeLaConstante() {
        for (Country country : Country.values()) {
            assertEquals(country.displayName(), country.toString());
        }
    }

    @Test
    void country_color_esUnHexadecimalValidoDeSeisDigitos() {
        for (Country country : Country.values()) {
            assertTrue(country.color().matches("#[0-9A-Fa-f]{6}"),
                    "Color inválido para " + country.name() + ": " + country.color());
        }
    }

    @Test
    void country_cadaPaisTieneUnColorDistinto() {
        long coloresDistintos = Arrays.stream(Country.values())
                .map(Country::color)
                .distinct()
                .count();

        assertEquals(Country.values().length, coloresDistintos);
    }

    // PlayerStats

    @Test
    void playerStats_nuevasEstadisticas_empiezanSinPuntos() {
        assertEquals(0, new PlayerStats().points());
    }

    @Test
    void playerStats_addPoints_acumulaLosPuntos() {
        PlayerStats stats = new PlayerStats();

        stats.addPoints(3);
        stats.addPoints(2);

        assertEquals(5, stats.points());
    }

    @Test
    void playerStats_addPoints_conCantidadNegativa_lanzaExcepcion() {
        PlayerStats stats = new PlayerStats();

        assertThrows(IllegalArgumentException.class, () -> stats.addPoints(-1));
    }

    @Test
    void playerStats_improveSpeed_subeUnNivelYGastaUnPunto() {
        PlayerStats stats = new PlayerStats();
        stats.addPoints(1);
        int antes = stats.speed();

        stats.improveSpeed();

        assertEquals(antes + 1, stats.speed());
        assertEquals(0, stats.points());
    }

    @Test
    void playerStats_improveKick_subeUnNivelYGastaUnPunto() {
        PlayerStats stats = new PlayerStats();
        stats.addPoints(1);
        int antes = stats.kick();

        stats.improveKick();

        assertEquals(antes + 1, stats.kick());
        assertEquals(0, stats.points());
    }

    @Test
    void playerStats_improveJump_subeUnNivelYGastaUnPunto() {
        PlayerStats stats = new PlayerStats();
        stats.addPoints(1);
        int antes = stats.jump();

        stats.improveJump();

        assertEquals(antes + 1, stats.jump());
        assertEquals(0, stats.points());
    }

    @Test
    void playerStats_improveSurvival_subeUnNivelYGastaUnPunto() {
        PlayerStats stats = new PlayerStats();
        stats.addPoints(1);
        int antes = stats.survival();

        stats.improveSurvival();

        assertEquals(antes + 1, stats.survival());
        assertEquals(0, stats.points());
    }

    @Test
    void playerStats_mejorarSinPuntos_lanzaExcepcionYNoCambiaLaEstadistica() {
        PlayerStats stats = new PlayerStats();
        int antes = stats.speed();

        assertThrows(IllegalStateException.class, stats::improveSpeed);

        assertEquals(antes, stats.speed());
    }

    @Test
    void playerStats_mejorarUnaEstadisticaEnSuMaximo_lanzaExcepcionYNoGastaPuntos() {
        PlayerStats stats = new PlayerStats();
        stats.addPoints(50);
        while (stats.speed() < 10) {
            stats.improveSpeed();
        }
        int puntosAntes = stats.points();

        assertThrows(IllegalStateException.class, stats::improveSpeed);

        assertEquals(10, stats.speed());
        assertEquals(puntosAntes, stats.points());
    }

    // Player

    @Test
    void player_nuevoJugador_empiezaConVidaMaximaSinGolesYSinPoder() {
        Player player = newPlayer();

        assertEquals(Country.values()[0], player.country());
        assertEquals(100.0, player.x(), 1e-9);
        assertEquals(0, player.goals());
        assertEquals(player.maxHealth(), player.health(), 1e-9);
        assertEquals(0.0, player.powerCharge(), 1e-9);
        assertEquals(Power.NONE, player.power());
    }

    @Test
    void player_scoreGoal_sumaUnGol() {
        Player player = newPlayer();

        player.scoreGoal();
        player.scoreGoal();

        assertEquals(2, player.goals());
    }

    @Test
    void player_takeDamage_restaLaVida() {
        Player player = newPlayer();

        player.takeDamage(30);

        assertEquals(player.maxHealth() - 30, player.health(), 1e-9);
    }

    @Test
    void player_takeDamage_conMasDanoQueVida_dejaLaVidaEnCero() {
        Player player = newPlayer();

        player.takeDamage(100000);

        assertEquals(0.0, player.health(), 1e-9);
    }

    @Test
    void player_takeDamage_conDanoNegativo_lanzaExcepcion() {
        Player player = newPlayer();

        assertThrows(IllegalArgumentException.class, () -> player.takeDamage(-1));
    }

    @Test
    void player_tick_recargaLaBarraDePoderSinPasarDelMaximo() {
        Player player = newPlayer();

        player.tick(1);
        assertTrue(player.powerCharge() > 0);

        player.tick(100000);
        assertEquals(Player.MAX_POWER_CHARGE, player.powerCharge(), 1e-9);
    }

    @Test
    void player_tick_reduceLaEsperaDeLaPatadaSinBajarDeCero() {
        Player player = newPlayer();
        player.startKickCooldown(0.48);

        player.tick(0.2);
        assertEquals(0.28, player.kickCooldown(), 1e-9);

        player.tick(1);
        assertEquals(0.0, player.kickCooldown(), 1e-9);
    }

    @Test
    void player_activatePower_conBarraIncompleta_noActivaElPoder() {
        Player player = newPlayer();

        assertFalse(player.activatePower());
        assertEquals(Power.NONE, player.power());
    }

    @Test
    void player_activatePower_conBarraLlena_activaElAuraYVaciaLaBarra() {
        Player player = newPlayer();
        player.tick(100000);

        assertTrue(player.activatePower());

        assertEquals(Power.AURA, player.power());
        assertEquals(0.0, player.powerCharge(), 1e-9);
    }

    @Test
    void player_moveTo_colocaAlJugadorYLoDejaQuieto() {
        Player player = newPlayer();
        player.setVelocity(5, 7);
        player.setGrounded(true);
        player.startKickCooldown(1);

        player.moveTo(300, 40);

        assertEquals(300.0, player.x(), 1e-9);
        assertEquals(40.0, player.y(), 1e-9);
        assertEquals(0.0, player.velocityX(), 1e-9);
        assertEquals(0.0, player.velocityY(), 1e-9);
        assertFalse(player.grounded());
        assertEquals(0.0, player.kickCooldown(), 1e-9);
    }

    @Test
    void player_setPosition_cambiaLaPosicionSinTocarLaVelocidad() {
        Player player = newPlayer();
        player.setVelocity(5, 7);

        player.setPosition(200, 60);

        assertEquals(200.0, player.x(), 1e-9);
        assertEquals(60.0, player.y(), 1e-9);
        assertEquals(5.0, player.velocityX(), 1e-9);
        assertEquals(7.0, player.velocityY(), 1e-9);
    }

    // GameMatchConfig

    @Test
    void gameMatchConfig_nuevaConfiguracion_noTieneModoPeroSiPersonajesPorDefecto() {
        GameMatchConfig config = new GameMatchConfig();

        assertNull(config.getGameMode());
        assertNotNull(config.getPlayer1Character());
        assertNotNull(config.getPlayer2Character());
    }

    @Test
    void gameMatchConfig_setGameMode_guardaElModo() {
        GameMatchConfig config = new GameMatchConfig();

        config.setGameMode(GameMode.PvP);

        assertEquals(GameMode.PvP, config.getGameMode());
    }

    @Test
    void gameMatchConfig_setPlayer1Character_guardaElPersonaje() {
        GameMatchConfig config = new GameMatchConfig();
        Country elegido = Country.values()[Country.values().length - 1];

        config.setPlayer1Character(elegido);

        assertEquals(elegido, config.getPlayer1Character());
    }

    @Test
    void gameMatchConfig_setPlayer2Character_guardaElPersonaje() {
        GameMatchConfig config = new GameMatchConfig();
        Country elegido = Country.values()[Country.values().length - 1];

        config.setPlayer2Character(elegido);

        assertEquals(elegido, config.getPlayer2Character());
    }

    @Test
    void gameMatchConfig_setPlayer1Character_conNull_lanzaExcepcionConMensajeClaro() {
        GameMatchConfig config = new GameMatchConfig();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> config.setPlayer1Character(null));

        assertEquals("El personaje del jugador 1 es obligatorio.", error.getMessage());
    }

    @Test
    void gameMatchConfig_setPlayer2Character_conNull_lanzaExcepcionConMensajeClaro() {
        GameMatchConfig config = new GameMatchConfig();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> config.setPlayer2Character(null));

        assertEquals("El personaje del jugador 2 es obligatorio.", error.getMessage());
    }

    // Game

    @Test
    void game_constructor_conArgumentoNull_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Game(null, firstCountry, secondCountry));
        assertThrows(IllegalArgumentException.class,
                () -> new Game(PlayerMode.PLAYER_VS_PLAYER, null, secondCountry));
        assertThrows(IllegalArgumentException.class,
                () -> new Game(PlayerMode.PLAYER_VS_PLAYER, firstCountry, null));
    }

    @Test
    void game_nuevoPartido_guardaModalidadYPersonajes() {
        Game game = newGame();

        assertEquals(PlayerMode.PLAYER_VS_PLAYER, game.playerMode());
        assertEquals(firstCountry, game.firstPlayer().country());
        assertEquals(secondCountry, game.secondPlayer().country());
    }

    @Test
    void game_nuevoPartido_empiezaCeroACeroConElPrimerJugadorALaIzquierda() {
        Game game = newGame();

        assertEquals(0, game.firstPlayer().goals());
        assertEquals(0, game.secondPlayer().goals());
        assertTrue(game.firstPlayer().x() < game.secondPlayer().x());
        assertEquals(Game.WIDTH / 2, game.ball().x(), 1e-9);
    }

    @Test
    void game_nuevoPartido_empiezaConLaEsperaDelSaqueActiva() {
        assertTrue(newGame().kickoffDelayActive());
    }

    @Test
    void game_updateKickoffDelay_conTiempoSuficiente_terminaLaEspera() {
        Game game = newGame();

        game.updateKickoffDelay(5.0);

        assertFalse(game.kickoffDelayActive());
    }

    @Test
    void game_updateKickoffDelay_conTiempoNegativo_lanzaExcepcion() {
        Game game = newGame();

        assertThrows(IllegalArgumentException.class, () -> game.updateKickoffDelay(-0.1));
    }

    @Test
    void game_scoreGoal_jugador1_sumaUnGolAlPrimerJugador() {
        Game game = newGame();

        game.scoreGoal(1);

        assertEquals(1, game.firstPlayer().goals());
        assertEquals(0, game.secondPlayer().goals());
    }

    @Test
    void game_scoreGoal_jugador2_sumaUnGolAlSegundoJugador() {
        Game game = newGame();

        game.scoreGoal(2);

        assertEquals(0, game.firstPlayer().goals());
        assertEquals(1, game.secondPlayer().goals());
    }

    @Test
    void game_scoreGoal_conNumeroInvalido_noHaceNada() {
        Game game = newGame();

        game.scoreGoal(3);
        game.scoreGoal(0);

        assertEquals(0, game.firstPlayer().goals());
        assertEquals(0, game.secondPlayer().goals());
    }

    @Test
    void game_scoreGoal_preparaElSaque() {
        Game game = newGame();
        game.updateKickoffDelay(5.0);
        game.ball().setPosition(100, 100);
        game.ball().setVelocity(50, 50);

        game.scoreGoal(1);

        assertTrue(game.kickoffDelayActive());
        assertEquals(Game.WIDTH / 2, game.ball().x(), 1e-9);
        assertEquals(0.0, game.ball().velocityX(), 1e-9);
        assertEquals(0.0, game.ball().velocityY(), 1e-9);
        assertEquals(Game.WIDTH / 2 - 60, game.firstPlayer().x(), 1e-9);
        assertEquals(Game.WIDTH / 2 + 60, game.secondPlayer().x(), 1e-9);
    }

    // PhysicsEngine

    // ---------- kick ----------

    @Test
    void physicsEngine_kick_haciaLaDerecha_daVelocidadPositivaEnXYHaciaArriba() {
        Player player = new Player(Country.values()[0], 100);
        Ball ball = new Ball(110, 10);

        physicsEngine.kick(player, ball, 1);

        assertTrue(ball.velocityX() > 0);
        assertTrue(ball.velocityY() < 0);
    }

    @Test
    void physicsEngine_kick_haciaLaIzquierda_daVelocidadNegativaEnX() {
        Player player = new Player(Country.values()[0], 100);
        Ball ball = new Ball(110, 10);

        physicsEngine.kick(player, ball, -1);

        assertTrue(ball.velocityX() < 0);
    }

    @Test
    void physicsEngine_kick_conBalonLejos_noHaceNada() {
        Player player = new Player(Country.values()[0], 100);
        Ball ball = new Ball(500, 10);

        physicsEngine.kick(player, ball, 1);

        assertEquals(0.0, ball.velocityX(), 1e-9);
        assertEquals(0.0, ball.velocityY(), 1e-9);
    }

    @Test
    void physicsEngine_kick_iniciaLaEsperaYUnaSegundaPatadaInmediataNoHaceNada() {
        Player player = new Player(Country.values()[0], 100);
        Ball ball = new Ball(110, 10);

        physicsEngine.kick(player, ball, 1);
        assertTrue(player.kickCooldown() > 0);

        ball.setVelocity(0, 0);
        physicsEngine.kick(player, ball, 1);

        assertEquals(0.0, ball.velocityX(), 1e-9);
        assertEquals(0.0, ball.velocityY(), 1e-9);
    }

    // ---------- update: balón ----------

    @Test
    void physicsEngine_update_laGravedadHaceCaerAlBalon() {
        Game game = newGame();
        game.ball().setPosition(Game.WIDTH / 2, 100);
        game.ball().setVelocity(0, 0);

        physicsEngine.update(game, PlayerInput.NONE, PlayerInput.NONE, FRAME);

        assertTrue(game.ball().velocityY() > 0, "El eje y crece hacia abajo");
    }

    @Test
    void physicsEngine_update_elBalonRebotaEnElSueloHaciaArriba() {
        Game game = newGame();
        game.ball().setPosition(Game.WIDTH / 2, Game.GROUND_Y - Ball.RADIUS - 5);
        game.ball().setVelocity(0, 600);

        physicsEngine.update(game, PlayerInput.NONE, PlayerInput.NONE, FRAME);

        assertTrue(game.ball().velocityY() < 0, "Debe salir hacia arriba");
        assertTrue(game.ball().y() + Ball.RADIUS <= Game.GROUND_Y + 1e-9,
                "No debe quedar hundido en el suelo");
    }

    // ---------- update: goles ----------

    @Test
    void physicsEngine_update_balonEnElArcoIzquierdo_esGolDelJugador2() {
        Game game = newGame();
        game.ball().setPosition(10, 450);
        game.ball().setVelocity(0, 0);

        physicsEngine.update(game, PlayerInput.NONE, PlayerInput.NONE, FRAME);

        assertEquals(0, game.firstPlayer().goals());
        assertEquals(1, game.secondPlayer().goals());
    }

    @Test
    void physicsEngine_update_balonEnElArcoDerecho_esGolDelJugador1() {
        Game game = newGame();
        game.ball().setPosition(Game.WIDTH - 10, 450);
        game.ball().setVelocity(0, 0);

        physicsEngine.update(game, PlayerInput.NONE, PlayerInput.NONE, FRAME);

        assertEquals(1, game.firstPlayer().goals());
        assertEquals(0, game.secondPlayer().goals());
    }

    @Test
    void physicsEngine_update_balonJuntoALaParedPeroSobreElArco_noEsGol() {
        Game game = newGame();
        game.ball().setPosition(10, 100);
        game.ball().setVelocity(0, 0);

        physicsEngine.update(game, PlayerInput.NONE, PlayerInput.NONE, FRAME);

        assertEquals(0, game.firstPlayer().goals());
        assertEquals(0, game.secondPlayer().goals());
    }

    // ---------- update: jugadores ----------

    @Test
    void physicsEngine_update_duranteLaEsperaDelSaque_losJugadoresNoSeMuevenEnX() {
        Game game = newGame();
        double xInicial = game.firstPlayer().x();
        PlayerInput derecha = new PlayerInput(false, true, false, false, false);

        physicsEngine.update(game, derecha, PlayerInput.NONE, FRAME);

        assertEquals(xInicial, game.firstPlayer().x(), 1e-9);
    }

    @Test
    void physicsEngine_update_despuesDeLaEspera_elJugadorSeMueveHaciaLaDerecha() {
        Game game = newGame();
        game.updateKickoffDelay(5.0);
        double xInicial = game.firstPlayer().x();
        PlayerInput derecha = new PlayerInput(false, true, false, false, false);

        physicsEngine.update(game, derecha, PlayerInput.NONE, FRAME);

        assertTrue(game.firstPlayer().x() > xInicial);
    }

    @Test
    void physicsEngine_update_elJugadorNoSeSaleDeLaCancha() {
        Game game = newGame();
        game.updateKickoffDelay(5.0);
        game.firstPlayer().setPosition(Player.RADIUS, 0);
        PlayerInput izquierda = new PlayerInput(true, false, false, false, false);

        physicsEngine.update(game, izquierda, PlayerInput.NONE, FRAME);

        assertTrue(game.firstPlayer().x() >= Player.RADIUS);
    }

    // PlayerInput

    @Test
    void playerInput_horizontalDirection_soloDerecha_esUno() {
        assertEquals(1.0, new PlayerInput(false, true, false, false, false).horizontalDirection(), 1e-9);
    }

    @Test
    void playerInput_horizontalDirection_soloIzquierda_esMenosUno() {
        assertEquals(-1.0, new PlayerInput(true, false, false, false, false).horizontalDirection(), 1e-9);
    }

    @Test
    void playerInput_horizontalDirection_ambasTeclas_esCero() {
        assertEquals(0.0, new PlayerInput(true, true, false, false, false).horizontalDirection(), 1e-9);
    }

    @Test
    void playerInput_horizontalDirection_ningunaTecla_esCero() {
        assertEquals(0.0, PlayerInput.NONE.horizontalDirection(), 1e-9);
    }

    @Test
    void playerInput_none_tieneTodasLasAccionesApagadas() {
        assertFalse(PlayerInput.NONE.left());
        assertFalse(PlayerInput.NONE.right());
        assertFalse(PlayerInput.NONE.jump());
        assertFalse(PlayerInput.NONE.kick());
        assertFalse(PlayerInput.NONE.power());
    }

    // GameEngine

    @Test
    void gameEngine_match_devuelveElPartidoRecibido() {
        Game game = newGame();

        assertSame(game, new GameEngine(game).match());
    }

    @Test
    void gameEngine_update_duranteLaEsperaDelSaque_noSeEjecutanPatadas() {
        Game game = newGame();
        GameEngine engine = new GameEngine(game);
        game.ball().setPosition(game.firstPlayer().x(), 60);
        game.ball().setVelocity(0, 0);

        engine.update(kickInput, PlayerInput.NONE, FRAME);

        assertEquals(0.0, game.ball().velocityX(), 1e-9);
    }

    @Test
    void gameEngine_update_terminadaLaEspera_elPrimerJugadorPateaHaciaLaDerecha() {
        Game game = newGame();
        GameEngine engine = new GameEngine(game);
        game.updateKickoffDelay(5.0);
        game.ball().setPosition(game.firstPlayer().x(), 60);
        game.ball().setVelocity(0, 0);

        engine.update(kickInput, PlayerInput.NONE, FRAME);

        assertTrue(game.ball().velocityX() > 0);
    }

    @Test
    void gameEngine_update_terminadaLaEspera_elSegundoJugadorPateaHaciaLaIzquierda() {
        Game game = newGame();
        GameEngine engine = new GameEngine(game);
        game.updateKickoffDelay(5.0);
        game.ball().setPosition(game.secondPlayer().x(), 60);
        game.ball().setVelocity(0, 0);

        engine.update(PlayerInput.NONE, kickInput, FRAME);

        assertTrue(game.ball().velocityX() < 0);
    }
}