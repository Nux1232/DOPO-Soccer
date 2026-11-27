package domain;

import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.entity.Player;
import domain.model.entity.Ball;
import domain.model.entity.BallShot;
import domain.model.entity.Power;
import domain.model.entity.ProjectilePower;
import domain.factory.MatchFactory;
import domain.model.ai.FootballAI;
import domain.model.ai.AIProfile;
import domain.model.persistence.PlayerProfile;
import domain.model.persistence.MatchPersistenceService;
import domain.model.persistence.PlayerProfileService;
import domain.model.game.MatchRewardService;
import domain.model.game.MatchCompletionService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    private final MatchCompletionService completionService;
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
        this.completionService = new MatchCompletionService(rewardService, profileService);
    }


    /** Modalidades de partido conocidas por la fachada. */
    public enum MatchKind {
        /** Partido que termina al alcanzar un límite de goles. */
        GOALS,
        /** Partido que termina al agotarse el tiempo configurado. */
        TIME
    }

    /** Modos de participación disponibles para iniciar un partido. */
    public enum PlayerModeKind {
        /** Dos personas controlan a los jugadores. */
        PLAYER_VS_PLAYER,
        /** Una persona juega contra la inteligencia artificial. */
        PLAYER_VS_MACHINE
    }

    /** Estado del ciclo de vida del partido administrado por la fachada. */
    public enum MatchStatus {
        /** Todavía no se ha iniciado o se ha reiniciado un partido. */
        NO_MATCH,
        /** Hay un partido en curso. */
        ACTIVE,
        /** El partido terminó. */
        FINISHED
    }

    /** Resumen inmutable de un país disponible para la selección en UI.
     * @param id identificador estable usado por el dominio
     * @param displayName nombre que se muestra al usuario
     * @param color color de camiseta asociado al país
     * @param flagAsset recurso gráfico de la bandera
     */
    public record CountrySummary(String id, String displayName, String color, String flagAsset) { }

    /** Resumen inmutable de un perfil de IA disponible en el dominio.
     * @param id identificador del perfil
     * @param displayName nombre legible del perfil
     */
    public record AIProfileSummary(String id, String displayName) { }

    /** Metadatos de una modalidad disponible para la interfaz de selección.
     * @param id identificador estable de la modalidad
     * @param displayName nombre que se muestra al usuario
     * @param parameters definiciones de los campos que debe configurar la interfaz
     */
    public record MatchTypeSummary(
            String id, String displayName, List<MatchParameterSummary> parameters) { }

    /** Campo de configuración que una modalidad presenta al usuario.
     * @param id identificador del parámetro esperado por el dominio
     * @param label etiqueta visible del campo
     * @param defaultValue valor inicial representado como texto
     * @param minimum límite inferior, o longitud mínima para campos de texto
     * @param maximum límite superior, o longitud máxima para campos de texto
     * @param multiplier factor para convertir el valor de entrada a la unidad del dominio
     * @param type tipo de control que debe presentar la interfaz
     * @param options opciones permitidas cuando el campo es de selección
     */
    public record MatchParameterSummary(
            String id, String label, String defaultValue, int minimum, int maximum, int multiplier,
            String type, List<String> options) { }

    /** Resumen inmutable de la progresión persistente de un jugador.
     * @param speed nivel de velocidad
     * @param kick nivel de potencia de tiro
     * @param jump nivel de salto
     * @param survival nivel de supervivencia
     * @param points puntos disponibles para comprar mejoras
     * @param nextUpgradeCost coste de la siguiente mejora
     */
    public record PlayerProfileSummary(int speed, int kick, int jump, int survival, int points, int nextUpgradeCost) { }

    /** Contrato público para iniciar un partido, sin exponer entidades.
     * @param playerMode modo de participación
     * @param matchType identificador de la modalidad
     * @param firstCountry identificador del país del primer jugador
     * @param secondCountry identificador del país del segundo jugador
     * @param aiProfile identificador del perfil de IA; puede ser {@code null} para usar el perfil predeterminado
     * @param parameters valores de configuración asociados a los identificadores de parámetros de la modalidad
     */
    public record MatchRequest(PlayerModeKind playerMode, String matchType,
            String firstCountry, String secondCountry, String aiProfile, Map<String, String> parameters) {
        /** Crea una solicitud compatible con límites genéricos anteriores.
         * @param mode modo de participación
         * @param type identificador de la modalidad
         * @param first identificador del país del primer jugador
         * @param second identificador del país del segundo jugador
         * @param goals límite de goles para la modalidad correspondiente
         * @param seconds duración en segundos para la modalidad correspondiente
         * @param profile identificador del perfil de IA
         */
        public MatchRequest(PlayerModeKind mode, String type, String first, String second,
                int goals, int seconds, String profile) {
            this(mode, type, first, second, profile, legacyParameters(type, goals, seconds, Map.of()));
        }

        /** Compatibilidad con solicitudes previas que incluían límites genéricos.
         * @param mode modo de participación
         * @param type identificador de la modalidad
         * @param first identificador del país del primer jugador
         * @param second identificador del país del segundo jugador
         * @param goals límite de goles para la modalidad correspondiente
         * @param seconds duración en segundos para la modalidad correspondiente
         * @param profile identificador del perfil de IA
         * @param parameters valores adicionales de configuración
         */
        public MatchRequest(PlayerModeKind mode, String type, String first, String second,
                int goals, int seconds, String profile, Map<String, String> parameters) {
            this(mode, type, first, second, profile, legacyParameters(type, goals, seconds, parameters));
        }

        /** Crea una solicitud usando el tipo de modalidad heredado.
         * @param mode modo de participación
         * @param type modalidad conocida por la fachada
         * @param first identificador del país del primer jugador
         * @param second identificador del país del segundo jugador
         * @param goals límite de goles para la modalidad correspondiente
         * @param seconds duración en segundos para la modalidad correspondiente
         * @param profile identificador del perfil de IA
         */
        public MatchRequest(PlayerModeKind mode, MatchKind type, String first, String second,
                int goals, int seconds, String profile) {
            this(mode, type == null ? null : type.name().toLowerCase(java.util.Locale.ROOT),
                    first, second, goals, seconds, profile, Map.of());
        }

        /** Crea una solicitud heredada con el perfil de IA predeterminado.
         * @param mode modo de participación
         * @param type modalidad conocida por la fachada
         * @param first identificador del país del primer jugador
         * @param second identificador del país del segundo jugador
         * @param goals límite de goles para la modalidad correspondiente
         * @param seconds duración en segundos para la modalidad correspondiente
         */
        public MatchRequest(PlayerModeKind mode, MatchKind type, String first, String second,
                int goals, int seconds) {
            this(mode, type == null ? null : type.name().toLowerCase(java.util.Locale.ROOT),
                    first, second, goals, seconds, "EXPERT", Map.of());
        }

        /** Crea una solicitud heredada con tipo de modalidad textual y perfil predeterminado.
         * @param mode modo de participación
         * @param type identificador de la modalidad
         * @param first identificador del país del primer jugador
         * @param second identificador del país del segundo jugador
         * @param goals límite de goles para la modalidad correspondiente
         * @param seconds duración en segundos para la modalidad correspondiente
         */
        public MatchRequest(PlayerModeKind mode, String type, String first, String second,
                int goals, int seconds) {
            this(mode, type, first, second, goals, seconds, "EXPERT", Map.of());
        }

        public MatchRequest {
            parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        }
    }

    private static Map<String, String> legacyParameters(
            String type, int goals, int seconds, Map<String, String> supplied) {
        Map<String, String> values = new java.util.HashMap<>(supplied == null ? Map.of() : supplied);
        if ("goals".equalsIgnoreCase(type) && goals > 0) {
            values.putIfAbsent("goalLimit", Integer.toString(goals));
        } else if ("time".equalsIgnoreCase(type) && seconds > 0) {
            if (seconds % 60 != 0) {
                throw new DopoSoccerException("La duración heredada debe expresarse en minutos completos.");
            }
            values.putIfAbsent("durationSeconds", Integer.toString(seconds / 60));
        }
        return Map.copyOf(values);
    }

    /**
     * Acciones abstractas independientes del teclado o dispositivo.
     *
     * @param left indica si se solicita movimiento hacia la izquierda
     * @param right indica si se solicita movimiento hacia la derecha
     * @param jump indica si se solicita saltar
     * @param kick indica si se solicita patear
     * @param power indica si se solicita activar el poder
     */
    public record PlayerActions(boolean left, boolean right, boolean jump, boolean kick, boolean power) { }

    /**
     * Estado de jugador de solo lectura para construir una representación visual.
     *
     * @param countryName nombre visible del país
     * @param jerseyColor color de camiseta
     * @param flagAsset recurso gráfico de la bandera
     * @param x posición horizontal del jugador
     * @param y posición vertical del jugador
     * @param goals goles anotados por el jugador
     * @param health salud actual
     * @param maxHealth salud máxima
     * @param powerCharge carga disponible para el poder
     * @param points puntos de progresión del jugador
     * @param speed nivel de velocidad
     * @param kick nivel de potencia de tiro
     * @param jump nivel de salto
     * @param survival nivel de supervivencia
     * @param facingDirection dirección hacia la que mira el jugador
     * @param kicking indica si está ejecutando una patada
     * @param stunned indica si está aturdido
     * @param power nombre visible de su poder
     * @param statusEffect efecto de estado activo representado como texto
     */
    public record PlayerSnapshot(String countryName, String jerseyColor, String flagAsset, double x, double y,
            int goals, double health, double maxHealth, double powerCharge, int points,
            int speed, int kick, int jump, int survival,
            int facingDirection, boolean kicking, boolean stunned, String power,
            String statusEffect) { }

    /**
     * Estado de partido de solo lectura. No expone entidades modificables.
     *
     * @param width ancho del campo
     * @param height alto del área de juego
     * @param groundY coordenada vertical del suelo
     * @param goalTop coordenada vertical superior de la portería
     * @param goalDepth profundidad de la portería
     * @param modeDescription descripción visible de la modalidad
     * @param finished indica si el partido terminó
     * @param winner ganador: {@code 0} empate, {@code 1} primer jugador o {@code 2} segundo jugador
     * @param firstPlayer estado del primer jugador
     * @param secondPlayer estado del segundo jugador
     * @param machineOpponent indica si el segundo jugador es controlado por la IA
     * @param ball estado del balón y del tiro especial activo, si existe
     */
    public record MatchSnapshot(double width, double height, double groundY, double goalTop, double goalDepth,
            String modeDescription, boolean finished, int winner,
            PlayerSnapshot firstPlayer, PlayerSnapshot secondPlayer, boolean machineOpponent,
            BallSnapshot ball) {
        public MatchSnapshot(double width, double height, double groundY, double goalTop, double goalDepth,
                String modeDescription, boolean finished, int winner,
                PlayerSnapshot firstPlayer, PlayerSnapshot secondPlayer, boolean machineOpponent,
                double ballX, double ballY, double ballRadius,
                String activePowerShot, String powerShotAsset, String powerShotColor,
                double powerShotDuration, double powerShotElapsed, boolean powerShotRenderCopies) {
            this(width, height, groundY, goalTop, goalDepth, modeDescription, finished, winner,
                    firstPlayer, secondPlayer, machineOpponent,
                    new BallSnapshot(ballX, ballY, ballRadius, activePowerShot == null ? null
                            : new PowerShotSnapshot(activePowerShot, powerShotAsset, powerShotColor,
                                    powerShotDuration, powerShotElapsed, powerShotRenderCopies)));
        }

        public double ballX() { return ball.x(); }
        public double ballY() { return ball.y(); }
        public double ballRadius() { return ball.radius(); }
        public String activePowerShot() { return ball.powerShot() == null ? null : ball.powerShot().id(); }
        public String powerShotAsset() { return ball.powerShot() == null ? null : ball.powerShot().asset(); }
        public String powerShotColor() { return ball.powerShot() == null ? null : ball.powerShot().color(); }
        public double powerShotDuration() {
            return ball.powerShot() == null ? 0 : ball.powerShot().duration();
        }
        public double powerShotElapsed() {
            return ball.powerShot() == null ? 0 : ball.powerShot().elapsed();
        }
        public boolean powerShotRenderCopies() {
            return ball.powerShot() != null && ball.powerShot().renderCopies();
        }
    }

    /**
     * Estado del balón agrupado con la representación del tiro que lo afecta.
     *
     * @param x posición horizontal del balón
     * @param y posición vertical del balón
     * @param radius radio del balón
     * @param powerShot datos del tiro especial activo, o {@code null} si no hay uno
     */
    public record BallSnapshot(double x, double y, double radius, PowerShotSnapshot powerShot) { }

    /**
     * Datos visuales cohesivos de un tiro especial activo.
     *
     * @param id identificador del tipo de tiro
     * @param asset recurso gráfico asociado
     * @param color color usado para representarlo
     * @param duration duración total del efecto
     * @param elapsed tiempo transcurrido desde su activación
     * @param renderCopies indica si deben dibujarse copias visuales
     */
    public record PowerShotSnapshot(
            String id, String asset, String color, double duration, double elapsed, boolean renderCopies) { }

    /** Catálogo público de países disponibles sin exponer entidades del dominio. */
    public List<CountrySummary> availableCountries() {
        return matchFactory.availableCountries().stream()
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

    /** @return modalidades registradas en la fábrica inyectada */
    public List<MatchTypeSummary> availableMatchTypes() {
        return matchFactory.availableMatchTypes().stream()
                .map(type -> new MatchTypeSummary(
                        type.type().id(), type.displayName(),
                        type.parameters().stream().map(parameter -> new MatchParameterSummary(
                                parameter.id(), parameter.label(), parameter.defaultValue(),
                                parameter.minimum(), parameter.maximum(), parameter.multiplier(),
                                parameter.type().name(), parameter.options()))
                                .toList()))
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
        completionService.reset();
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
                    domain.model.game.MatchType.of(request.matchType()),
                    request.firstCountry(),
                    request.secondCountry(),
                    AIProfile.valueOf(request.aiProfile() == null ? "EXPERT" : request.aiProfile()),
                    request.parameters());
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
        if (!wasFinished && currentMatch.isFinished()) completionService.complete(currentMatch);
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
        completionService.complete(currentMatch);
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

    /** Guarda el estado completo del partido activo en la ruta por defecto. */
    public void saveMatch() {
        requireMatch();
        matchPersistenceService.save(currentMatch, aiProfile, completionService.rewardsApplied());
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
        new MatchPersistenceService(file, matchFactory)
                .save(currentMatch, aiProfile, completionService.rewardsApplied());
        saveProfiles();
    }

    /** Abre la partida guardada en la ruta por defecto y reconstruye sus entidades. */
    public void loadMatch() {
        MatchPersistenceService.LoadedMatch loaded = matchPersistenceService.load();
        currentMatch = loaded.game();
        aiProfile = loaded.aiProfile();
        completionService.restoreRewardsApplied(loaded.rewardsApplied());
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
        completionService.restoreRewardsApplied(loaded.rewardsApplied());
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
        BallShot shot = ball.activePowerShot();
        PowerShotSnapshot powerShot = shot == null ? null : new PowerShotSnapshot(
                shot.id(), shot.visualAsset(), shot.visualColor(), shot.durationSeconds(),
                shot.durationSeconds() - ball.powerShotTime(), shot.renderCopies());
        return new MatchSnapshot(Game.WIDTH, Game.HEIGHT, Game.GROUND_Y, Game.GOAL_TOP, Game.GOAL_DEPTH,
                currentMatch.modeDescription(), currentMatch.isFinished(), currentMatch.winner(),
                playerSnapshot(first), playerSnapshot(second),
                currentMatch.playerMode() == domain.model.game.PlayerMode.PLAYER_VS_MACHINE,
                new BallSnapshot(ball.x(), ball.y(), Ball.RADIUS, powerShot));
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
