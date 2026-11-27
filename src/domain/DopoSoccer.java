package domain;

import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.entity.CountryCatalog;
import domain.model.entity.Player;
import domain.model.entity.Ball;
import domain.model.entity.Power;
import domain.factory.MatchFactory;
import domain.model.ai.FootballAI;
import domain.model.ai.AIProfile;
import domain.model.persistence.PlayerProfile;
import domain.model.persistence.MatchPersistenceService;
import domain.model.persistence.PlayerProfileService;
import domain.model.game.MatchRewardService;

import java.util.Arrays;
import java.util.List;

/**
 * Fachada pública del dominio DOPO-SOCCER.
 * La presentación se comunica exclusivamente con esta clase. Valida solicitudes,
 * administra el ciclo de vida del partido activo y delega las reglas en el dominio.
 */
public final class DopoSoccer {
    private Game currentMatch;
    private final FootballAI ai;
    private final PlayerProfileService profileService;
    private final MatchFactory matchFactory;
    private final MatchPersistenceService matchPersistenceService;
    private final MatchRewardService rewardService;
    private boolean rewardsApplied;
    private AIProfile aiProfile = AIProfile.EXPERT;

    /** Construye la fachada con las implementaciones predeterminadas. */
    public DopoSoccer() {
        this(new MatchFactory(), new PlayerProfileService());
    }

    /**
     * Construye la fachada con fábrica y servicio de perfiles configurables.
     * La fábrica compartida también se usa para reconstruir las partidas guardadas.
     *
     * @param matchFactory fábrica de modalidades y simulación
     * @param profileService servicio de perfiles
     */
    public DopoSoccer(MatchFactory matchFactory, PlayerProfileService profileService) {
        this(matchFactory, profileService, new MatchPersistenceService(matchFactory),
                new FootballAI(), new MatchRewardService());
    }

    /**
     * Construye la fachada inyectando sus dependencias de aplicación.
     *
     * @param matchFactory fábrica de modalidades y simulación
     * @param profileService servicio de perfiles
     * @param matchPersistenceService servicio de guardado y restauración
     * @param ai implementación de inteligencia artificial
     * @param rewardService servicio de recompensas
     */
    public DopoSoccer(MatchFactory matchFactory, PlayerProfileService profileService,
            MatchPersistenceService matchPersistenceService, FootballAI ai,
            MatchRewardService rewardService) {
        if (matchFactory == null || profileService == null || matchPersistenceService == null
                || ai == null || rewardService == null) {
            throw new DopoSoccerException("Las dependencias de la fachada son obligatorias.");
        }
        this.matchFactory = matchFactory;
        this.profileService = profileService;
        this.matchPersistenceService = matchPersistenceService;
        this.ai = ai;
        this.rewardService = rewardService;
    }


    public enum MatchKind { GOALS, TIME }
    public enum PlayerModeKind { PLAYER_VS_PLAYER, PLAYER_VS_MACHINE }
    public enum MatchStatus { NO_MATCH, ACTIVE, FINISHED }

    /** Resumen inmutable de un país disponible para la selección en UI. */
    public record CountrySummary(String id, String displayName, String color, String flagAsset) { }

    /** Resumen inmutable de un perfil de IA disponible en el dominio. */
    public record AIProfileSummary(String id, String displayName) { }

    /** Resumen inmutable de la progresión persistente de un jugador. */
    public record PlayerProfileSummary(int speed, int kick, int jump, int survival, int points, int nextUpgradeCost) { }

    /** Contrato público para iniciar un partido, sin exponer entidades. */
    public record MatchRequest(PlayerModeKind playerMode, MatchKind matchType,
            String firstCountry, String secondCountry, int goalLimit, int durationSeconds, String aiProfile) {
        public MatchRequest(PlayerModeKind mode, MatchKind type, String first, String second,
                int goals, int seconds) {
            this(mode, type, first, second, goals, seconds, "EXPERT");
        }

    }

    /** Acciones abstractas independientes del teclado o dispositivo. */
    public record PlayerActions(boolean left, boolean right, boolean jump, boolean kick, boolean power) { }

    /** Estado de jugador de solo lectura para construir una representación visual. */
    public record PlayerSnapshot(String countryName, String jerseyColor, String flagAsset, double x, double y,
            int goals, double health, double maxHealth, double powerCharge, int points,
            int speed, int kick, int jump, int survival,
            int facingDirection, boolean kicking, boolean stunned, String power,
            String statusEffect) { }

    /** Estado de partido de solo lectura. No expone entidades modificables. */
    public record MatchSnapshot(double width, double height, double groundY, double goalTop, double goalDepth,
            String modeDescription, boolean finished, int winner,
            PlayerSnapshot firstPlayer, PlayerSnapshot secondPlayer, boolean machineOpponent,
            double ballX, double ballY, double ballRadius,
            String activePowerShot, String powerShotAsset, String powerShotColor,
            double powerShotDuration, double powerShotElapsed, boolean powerShotRenderCopies) { }

    /** Catálogo público de países disponibles sin exponer entidades del dominio. */
    public List<CountrySummary> availableCountries() {
        return CountryCatalog.all().stream()
                .map(country -> new CountrySummary(
                        country.id(), country.displayName(), country.color(), country.flagAsset()))
                .toList();
    }

    /** Perfiles de inteligencia artificial disponibles sin exponer el enum interno. */
    public List<AIProfileSummary> availableAIProfiles() {
        return Arrays.stream(AIProfile.values())
                .map(profile -> new AIProfileSummary(profile.name(), profile.displayName()))
                .toList();
    }

    /**
     * Inicia un nuevo partido validando la solicitud y cargando los perfiles persistentes
     * de los jugadores humanos.
     *
     * @param request parámetros de configuración del partido
     * @throws DopoSoccerException si la configuración es inválida o incompleta
     */
    public void startMatch(MatchRequest request) throws DopoSoccerException {
        currentMatch = matchFactory.createMatch(toMatchParameters(request));
        rewardsApplied = false;
        aiProfile = request.aiProfile() == null ? AIProfile.EXPERT : AIProfile.valueOf(request.aiProfile());
        profileService.loadInto("player1", currentMatch.firstPlayer());
        if (currentMatch.playerMode() == domain.model.game.PlayerMode.PLAYER_VS_PLAYER) {
            profileService.loadInto("player2", currentMatch.secondPlayer());
        }
    }

    private MatchFactory.MatchParameters toMatchParameters(MatchRequest request) {
        if (request == null || request.playerMode() == null || request.matchType() == null
                || request.firstCountry() == null || request.secondCountry() == null) {
            throw new DopoSoccerException(DopoSoccerException.NULL_CONFIG);
        }
        try {
            return new MatchFactory.MatchParameters(
                    domain.model.game.PlayerMode.valueOf(request.playerMode().name()),
                    domain.model.game.MatchType.valueOf(request.matchType().name()),
                    request.firstCountry(),
                    request.secondCountry(),
                    request.goalLimit(),
                    request.durationSeconds(),
                    AIProfile.valueOf(request.aiProfile() == null ? "EXPERT" : request.aiProfile()));
        } catch (IllegalArgumentException exception) {
            throw new DopoSoccerException("La modalidad o el perfil seleccionado no existe.");
        }
    }

    /**
     * Avanza el partido activo un intervalo de tiempo, aplicando la IA si corresponde
     * y otorgando recompensas al finalizar.
     *
     * @param first acciones del primer jugador
     * @param second acciones del segundo jugador (ignoradas en modo máquina)
     * @param deltaSeconds segundos transcurridos desde el cuadro anterior
     * @throws DopoSoccerException si no hay partido activo o el tiempo es negativo
     */
    public void updateMatch(PlayerActions first, PlayerActions second, double deltaSeconds)
            throws DopoSoccerException {
        requireMatch();
        if (deltaSeconds < 0) throw new DopoSoccerException(DopoSoccerException.NEGATIVE_TIME);
        boolean wasFinished=currentMatch.isFinished();
        PlayerActions effectiveSecond = currentMatch.playerMode() == domain.model.game.PlayerMode.PLAYER_VS_MACHINE
                ? toActions(ai.decide(aiProfile, currentMatch.secondPlayer(), currentMatch.firstPlayer(), currentMatch.ball(), currentMatch.secondPlayer().goals(), currentMatch.firstPlayer().goals()))
                : second;
        currentMatch.update(toInput(first), toInput(effectiveSecond), deltaSeconds);
        if (!wasFinished && currentMatch.isFinished()) { awardMatchPoints(); saveProfiles(); }
    }

    /**
     * Finaliza anticipadamente el partido activo en cualquier momento, determina el
     * ganador según el marcador actual y aplica los puntos de recompensa correspondientes.
     *
     * @throws DopoSoccerException si no hay un partido activo
     */
    public void endMatch() throws DopoSoccerException {
        requireMatch();
        if (!currentMatch.isFinished()) {
            currentMatch.endMatchNow();
        }
        awardMatchPoints();
        saveProfiles();
    }

    private PlayerActions toActions(domain.model.game.PlayerInput input) {
        return new PlayerActions(input.left(),input.right(),input.jump(),input.kick(),input.power());
    }

    /**
     * Persiste los perfiles de progresión de los jugadores humanos participantes.
     */
    public void saveProfiles() {
        requireMatch();
        if (currentMatch.playerMode() == domain.model.game.PlayerMode.PLAYER_VS_MACHINE) {
            profileService.saveProfile("player1", currentMatch.firstPlayer());
        } else {
            profileService.saveMatchProfiles(currentMatch.firstPlayer(), currentMatch.secondPlayer());
        }
    }

    /**
     * Carga el perfil guardado del slot indicado para mostrarlo en la interfaz.
     *
     * @param slot identificador del perfil ({@code "player1"} o {@code "player2"})
     * @return resumen inmutable del perfil guardado
     */
    public PlayerProfileSummary loadSavedProfile(String slot) {
        return toProfileSummary(profileService.load(slot));
    }


    /**
     * Compra una mejora en el perfil persistente antes de iniciar un partido.
     *
     * @param slot identificador del perfil ({@code "player1"} o {@code "player2"})
     * @param stat estadística a mejorar ({@code speed}, {@code kick}, {@code jump} o {@code survival})
     * @return resumen actualizado del perfil tras aplicar la mejora
     * @throws DopoSoccerException si faltan puntos, la estadística está al máximo o es inválida
     */
    public PlayerProfileSummary improveSavedProfile(String slot, String stat) {
        return toProfileSummary(profileService.improve(slot, stat));
    }

    private PlayerProfileSummary toProfileSummary(PlayerProfile profile) {
        return new PlayerProfileSummary(profile.speed(), profile.kick(), profile.jump(),
                profile.survival(), profile.points(), profile.nextUpgradeCost());
    }


    private domain.model.game.PlayerInput toInput(PlayerActions actions) {
        if (actions == null) throw new DopoSoccerException("Las acciones del jugador son obligatorias.");
        return new domain.model.game.PlayerInput(actions.left(), actions.right(), actions.jump(), actions.kick(), actions.power());
    }

    private void awardMatchPoints() {
        if (rewardsApplied) return;
        rewardService.award(currentMatch);
        rewardsApplied = true;
    }

    /** Guarda el estado completo del partido activo en la ruta por defecto. */
    public void saveMatch() {
        requireMatch();
        matchPersistenceService.save(currentMatch, aiProfile, rewardsApplied);
        saveProfiles();
    }

    /**
     * Guarda el partido activo en el archivo elegido por el usuario.
     *
     * @param file ruta del archivo destino
     * @throws DopoSoccerException si no hay partido activo o falla la escritura
     */
    public void saveMatch(java.nio.file.Path file) {
        requireMatch();
        new MatchPersistenceService(file, matchFactory).save(currentMatch, aiProfile, rewardsApplied);
        saveProfiles();
    }

    /** Abre la partida guardada en la ruta por defecto y reconstruye sus entidades. */
    public void loadMatch() {
        MatchPersistenceService.LoadedMatch loaded = matchPersistenceService.load();
        currentMatch = loaded.game();
        aiProfile = loaded.aiProfile();
        rewardsApplied = loaded.rewardsApplied();
    }

    /**
     * Carga un partido desde un archivo seleccionado y lo establece como partido activo.
     *
     * @param file ruta del archivo guardado
     * @throws DopoSoccerException si el archivo no existe, es incompatible o está corrupto
     */
    public void loadMatch(java.nio.file.Path file) {
        MatchPersistenceService.LoadedMatch loaded = new MatchPersistenceService(file, matchFactory).load();
        currentMatch = loaded.game();
        aiProfile = loaded.aiProfile();
        rewardsApplied = loaded.rewardsApplied();
    }


    /**
     * Genera una instantánea inmutable del estado actual del partido para renderizado.
     *
     * @return estado completo de solo lectura del partido activo
     * @throws DopoSoccerException si no hay un partido activo
     */
    public MatchSnapshot snapshot() {
        requireMatch();
        Player first = currentMatch.firstPlayer();
        Player second = currentMatch.secondPlayer();
        Ball ball = currentMatch.ball();
        Power shot = ball.activePowerShot();
        return new MatchSnapshot(Game.WIDTH, Game.HEIGHT, Game.GROUND_Y, Game.GOAL_TOP, Game.GOAL_DEPTH,
                currentMatch.modeDescription(), currentMatch.isFinished(), currentMatch.winner(),
                playerSnapshot(first), playerSnapshot(second),
                currentMatch.playerMode() == domain.model.game.PlayerMode.PLAYER_VS_MACHINE,
                ball.x(), ball.y(), Ball.RADIUS,
                shot == null ? null : shot.id(), shot == null ? null : shot.visualAsset(),
                shot == null ? null : shot.visualColor(), shot == null ? 0 : shot.durationSeconds(),
                shot == null ? 0 : shot.durationSeconds() - ball.powerShotTime(),
                shot != null && shot.renderCopies());
    }

    private PlayerSnapshot playerSnapshot(Player player) {
        return new PlayerSnapshot(player.country().displayName(), player.country().color(),
                player.country().flagAsset(),
                player.x(), player.y(), player.goals(), player.health(), player.maxHealth(),
                player.powerCharge(), player.stats().points(), player.stats().speed(), player.stats().kick(),
                player.stats().jump(), player.stats().survival(),
                player.facingDirection(), player.kicking(), player.stunned(),
                player.power().name(), player.statusEffect().name());
    }

    /**
     * Consulta el estado general del partido en la fachada.
     *
     * @return estado actual ({@code NO_MATCH}, {@code ACTIVE} o {@code FINISHED})
     */
    public MatchStatus matchStatus() {
        if (currentMatch == null) return MatchStatus.NO_MATCH;
        return currentMatch.isFinished() ? MatchStatus.FINISHED : MatchStatus.ACTIVE;
    }


    /**
     * Devuelve el ganador del partido activo.
     *
     * @return {@code 0} empate, {@code 1} primer jugador o {@code 2} segundo jugador
     * @throws DopoSoccerException si no hay un partido activo
     */
    public int winner() {
        requireMatch();
        return currentMatch.winner();
    }

    /** Finaliza la sesión en la fachada y libera la referencia al partido. */
    public void reset() { currentMatch = null; }

    private void requireMatch() {
        if (currentMatch == null) throw new DopoSoccerException("No hay un partido activo.");
    }
}
