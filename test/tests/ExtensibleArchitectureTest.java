package tests;

import domain.DopoSoccer;
import domain.factory.MatchFactory;
import domain.model.ai.AIProfile;
import domain.model.exception.DopoSoccerException;
import domain.model.entity.Power;
import domain.model.entity.PowerActivationContext;
import domain.model.entity.PowerFactoryRegistry;
import domain.model.entity.CountryCatalog;
import domain.model.entity.BallShot;
import domain.model.game.Game;
import domain.model.game.GoalGame;
import domain.model.game.MatchConfiguration;
import domain.model.game.MatchValue;
import domain.model.game.MatchCompletionService;
import domain.model.game.MatchRestoreRequest;
import domain.model.game.MatchRestorer;
import domain.model.game.MatchType;
import domain.model.game.PlayerMode;
import domain.model.persistence.MatchPersistenceService;
import domain.model.persistence.MatchSaveData;
import domain.model.persistence.MatchSaveRepository;
import domain.model.persistence.PlayerProfile;
import domain.model.persistence.PlayerProfileRepository;
import domain.model.persistence.PlayerProfileService;
import presentation.view.effects.PlayerEffectCatalog;
import presentation.view.render.PowerShotRenderer;
import presentation.view.render.PowerShotRendererRegistry;
import presentation.model.MatchTypeOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensibleArchitectureTest {
    @TempDir
    Path temporaryDirectory;


    @Test
    void facadeUsesInjectedMatchCreatorAndRepositoriesAreReplaceable() throws Exception {
        AtomicInteger creatorCalls = new AtomicInteger();
        MatchFactory factory = new MatchFactory();
        factory.register(MatchType.GOALS, (request, first, second, physics) -> {
            creatorCalls.incrementAndGet();
            return new GoalGame(request.playerMode(), first, second, physics,
                    Integer.parseInt(request.parameters().get("goalLimit")));
        });
        InMemoryProfileRepository profiles = new InMemoryProfileRepository();
        PlayerProfileService profileService = new PlayerProfileService(profiles);
        DopoSoccer facade = new DopoSoccer(factory, profileService);

        facade.startMatch(new DopoSoccer.MatchRequest(
                DopoSoccer.PlayerModeKind.PLAYER_VS_PLAYER,
                DopoSoccer.MatchKind.GOALS,
                "usa",
                "cameroon",
                5,
                120));

        assertEquals(1, creatorCalls.get());
        assertEquals(DopoSoccer.MatchStatus.ACTIVE, facade.matchStatus());
    }

    @Test
    void matchPersistenceUsesInjectedRepositoryAndSerializableSnapshot() {
        AtomicInteger creatorCalls = new AtomicInteger();
        MatchFactory factory = new MatchFactory();
        factory.register(MatchType.GOALS, (request, first, second, physics) -> {
            creatorCalls.incrementAndGet();
            return new GoalGame(request.playerMode(), first, second, physics,
                    Integer.parseInt(request.parameters().get("goalLimit")));
        });
        InMemoryMatchRepository repository = new InMemoryMatchRepository();
        MatchPersistenceService persistence = new MatchPersistenceService(repository, factory);
        Game game = factory.createMatch(new MatchFactory.MatchParameters(
                PlayerMode.PLAYER_VS_PLAYER,
                MatchType.GOALS,
                "usa",
                "cameroon",
                5,
                120,
                AIProfile.EXPERT));

        persistence.save(game, AIProfile.EXPERT, false);

        assertNotNull(repository.data);
        assertInstanceOf(Serializable.class, repository.data);
        MatchPersistenceService.LoadedMatch loaded = persistence.load();
        assertInstanceOf(GoalGame.class, loaded.game());
        assertEquals(2, creatorCalls.get());
    }

    @Test
    void profileServiceUsesInjectedRepository() {
        InMemoryProfileRepository repository = new InMemoryProfileRepository();
        PlayerProfile profile = new PlayerProfile(2, 3, 4, 5, 600, 700);
        repository.save("player1", profile);

        assertEquals(profile, new PlayerProfileService(repository).load("player1"));
        assertTrue(repository.profiles.containsKey("player1"));
    }

    @Test
    void matchCompletionAwardsAndPersistsRewardsExactlyOnce() {
        InMemoryProfileRepository repository = new InMemoryProfileRepository();
        Game game = new Game(
                PlayerMode.PLAYER_VS_PLAYER,
                domain.factory.CountryFactory.create("KR"),
                domain.factory.CountryFactory.create("CM"));
        MatchCompletionService completion = new MatchCompletionService(
                new domain.model.game.MatchRewardService(), new PlayerProfileService(repository));
        game.endMatchNow();

        completion.complete(game);
        completion.complete(game);

        assertEquals(25, game.firstPlayer().stats().points());
        assertEquals(25, game.secondPlayer().stats().points());
        assertEquals(25, repository.profiles.get("player1").points());
        assertEquals(25, repository.profiles.get("player2").points());
    }

    @Test
    void matchCompletionDoesNotConsumeRewardsWhenProfilePersistenceFails() {
        AtomicInteger saves = new AtomicInteger();
        PlayerProfileRepository failingRepository = new PlayerProfileRepository() {
            @Override
            public PlayerProfile load(String slot) {
                return null;
            }

            @Override
            public void save(String slot, PlayerProfile profile) {
                if (saves.getAndIncrement() == 0) {
                    throw new DopoSoccerException("Fallo de persistencia simulado.");
                }
            }
        };
        Game game = new Game(
                PlayerMode.PLAYER_VS_PLAYER,
                domain.factory.CountryFactory.create("KR"),
                domain.factory.CountryFactory.create("CM"));
        MatchCompletionService completion = new MatchCompletionService(
                new domain.model.game.MatchRewardService(), new PlayerProfileService(failingRepository));
        game.endMatchNow();

        assertThrows(DopoSoccerException.class, () -> completion.complete(game));
        assertEquals(0, game.firstPlayer().stats().points());
        assertEquals(0, game.secondPlayer().stats().points());

        completion.complete(game);

        assertEquals(25, game.firstPlayer().stats().points());
        assertEquals(25, game.secondPlayer().stats().points());
    }

    @Test
    void powerFactoriesCanCreateCustomNonProjectilePowerTypes() {
        PowerFactoryRegistry registry = new PowerFactoryRegistry();
        registry.register("shield", configuration -> new Power() {
            @Override
            public String id() {
                return configuration.get("id");
            }

            @Override
            public void activate(PowerActivationContext context) { }
        });

        Power power = registry.create("shield", Map.of("id", "blue-shield"));

        assertEquals("blue-shield", power.id());
    }

    @Test
    void customCountryCatalogAndNonProjectilePowerStateAreInjectedAndRestored() throws Exception {
        PowerFactoryRegistry registry = PowerFactoryRegistry.withDefaults();
        registry.register("shield", configuration -> new Power() {
            @Override
            public String id() {
                return configuration.get("id");
            }

            @Override
            public void activate(PowerActivationContext context) {
                context.owner().setPowerExtensionState("charges", "2");
            }
        });
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/resources/countries.properties")) {
            assertNotNull(input);
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        }
        properties.setProperty("country.south-korea.power.type", "shield");
        properties.setProperty("country.south-korea.power.id", "shield-kr");
        CountryCatalog catalog = CountryCatalog.fromProperties(properties, registry);
        MatchFactory factory = new MatchFactory(catalog, domain.model.physics.PhysicsEngine::new);
        Game game = factory.createMatch(new MatchFactory.MatchParameters(
                PlayerMode.PLAYER_VS_PLAYER,
                MatchType.GOALS,
                "KR",
                "CM",
                5,
                120,
                AIProfile.EXPERT));
        game.firstPlayer().setPowerExtensionState("charges", "2");
        MatchPersistenceService persistence = new MatchPersistenceService(
                temporaryDirectory.resolve("non-projectile-power.ser"), factory);

        persistence.save(game, AIProfile.EXPERT, false);
        Game restored = persistence.load().game();

        assertEquals("shield-kr", restored.firstPlayer().country().power().id());
        assertEquals("2", restored.firstPlayer().powerExtensionState().get("charges"));
    }

    @Test
    void customPlayerStatusVisualCanBeRegisteredWithoutChangingTheEffectsLayer() {
        PlayerEffectCatalog catalog = PlayerEffectCatalog.defaults();
        catalog.register("SHIELDED", "/resources/effects/powers/shielded.gif", 1.5, 1.5, -4);

        assertEquals("/resources/effects/powers/shielded.gif",
                catalog.styles().get("SHIELDED").resourcePath());
        assertThrows(IllegalArgumentException.class, () ->
                catalog.register("shielded", "/duplicate.gif", 1, 1, 0));
    }

    @Test
    void matchPersistenceRestoresThroughAnIndependentRestorerPort() {
        MatchFactory creator = new MatchFactory();
        Game original = creator.createMatch(new MatchFactory.MatchParameters(
                PlayerMode.PLAYER_VS_PLAYER, MatchType.GOALS, "usa", "cameroon",
                7, 240, AIProfile.EXPERT));
        InMemoryMatchRepository repository = new InMemoryMatchRepository();
        new MatchPersistenceService(repository, creator).save(original, AIProfile.EXPERT, false);
        AtomicInteger restoreCalls = new AtomicInteger();
        MatchRestorer restorer = request -> {
            restoreCalls.incrementAndGet();
            assertEquals(new MatchValue.IntegerNumber(7),
                    request.configuration().parameters().get("goalLimit"));
            assertEquals(MatchType.GOALS, request.configuration().type());
            return creator.restore(request);
        };

        Game restored = new MatchPersistenceService(repository, restorer).load().game();

        assertEquals(1, restoreCalls.get());
        assertEquals(7, ((GoalGame) restored).goalLimit());
    }

    @Test
    void customBallShotCanBeActivatedAndHandledWithoutProjectilePower() {
        BallShot shot = new TestBallShot();
        Power power = new Power() {
            @Override
            public String id() {
                return shot.id();
            }

            @Override
            public void activate(PowerActivationContext context) { }

            @Override
            public void activateInMatch(
                    PowerActivationContext context, domain.model.entity.Ball ball,
                    PowerShotLauncher launcher) throws DopoSoccerException {
                try {
                    launcher.launch(shot);
                } catch (DopoSoccerException exception) {
                    throw exception;
                }
            }

            @Override
            public boolean restoreActiveShot(domain.model.entity.Ball ball,
                    domain.model.entity.Player owner, String powerId, double remainingSeconds,
                    boolean effectApplied) {
                if (!shot.id().equals(powerId)) return false;
                ball.restorePowerShot(shot, owner, remainingSeconds, effectApplied);
                return true;
            }
        };
        domain.model.entity.Country country = new domain.model.entity.Country(
                "custom", "Custom", "#ffffff", "custom.png", power);
        domain.model.entity.Player owner = new domain.model.entity.Player(
                country, 100, domain.model.entity.PlayerSide.LEFT);
        owner.restoreMatchState(0, owner.maxHealth(), 100, domain.model.entity.PowerState.NONE,
                100, 100, 0, 0, true, 0, 0, 0, 1,
                domain.model.entity.PlayerStatusEffect.NONE, 0);
        owner.activatePower();
        domain.model.entity.Ball ball = new domain.model.entity.Ball(100, 100);
        domain.model.physics.PhysicsEngine physics = new domain.model.physics.PhysicsEngine();

        physics.kick(owner, ball, 1);

        assertEquals(shot, ball.activePowerShot());
        assertEquals(230, ball.velocityX());
    }

    @Test
    void customShotRenderersAreRegisteredByPowerId() {
        PowerShotRendererRegistry registry = new PowerShotRendererRegistry();
        PowerShotRenderer renderer = (graphics, snapshot, x, y) -> { };
        registry.register("custom-shot", renderer);

        assertEquals(renderer, registry.find("CUSTOM-SHOT"));
        assertThrows(IllegalArgumentException.class,
                () -> registry.register("custom-shot", renderer));
    }

    private static final class TestBallShot implements BallShot {
        @Override public String id() { return "test-shot"; }
        @Override public double durationSeconds() { return 2; }
        @Override public double restitution() { return 0.8; }
        @Override public double horizontalSpeed() { return 230; }
        @Override public double verticalSpeed() { return -15; }
        @Override public String visualAsset() { return null; }
        @Override public String visualColor() { return "#ffffff"; }
        @Override public boolean renderCopies() { return false; }
        @Override public void applyImpact(
                domain.model.entity.Player owner, domain.model.entity.Player opponent) { }
    }

    @Test
    void customMatchTypesCanBeRequestedCreatedAndRestoredWithoutChangingTheTypeRegistry() {
        MatchType customType = MatchType.of("sudden-death");
        AtomicInteger creatorCalls = new AtomicInteger();
        MatchFactory factory = new MatchFactory();
        factory.register(customType, "Muerte súbita", List.of(
                        new MatchFactory.MatchParameter("rounds", "Rondas", "3", 1, 9, 1),
                        new MatchFactory.MatchParameter("arena", "Cancha", "classic",
                                0, 0, 1, MatchFactory.ParameterType.SELECT,
                                List.of("classic", "compact")),
                        new MatchFactory.MatchParameter("overtime", "Prórroga", "true",
                                0, 0, 1, MatchFactory.ParameterType.BOOLEAN, List.of()),
                        new MatchFactory.MatchParameter("label", "Etiqueta", "Final",
                                1, 12, 1, MatchFactory.ParameterType.TEXT, List.of())),
                (request, first, second, physics) -> {
                    creatorCalls.incrementAndGet();
                    return new CustomGame(request.playerMode(), first, second, physics, customType,
                            request.parameters());
                });
        DopoSoccer facade = new DopoSoccer(factory, new PlayerProfileService(new InMemoryProfileRepository()));
        assertTrue(facade.availableMatchTypes().stream()
                .anyMatch(type -> type.id().equals("sudden-death")
                        && type.displayName().equals("Muerte súbita")
                        && type.parameters().size() == 4
                        && type.parameters().get(1).type().equals("SELECT")));
        MatchPersistenceService persistence = new MatchPersistenceService(
                temporaryDirectory.resolve("custom-match.ser"), factory);
        MatchFactory.MatchParameters parameters = new MatchFactory.MatchParameters(
                PlayerMode.PLAYER_VS_PLAYER,
                customType,
                "usa",
                "cameroon",
                AIProfile.EXPERT,
                Map.of("rounds", "5", "arena", "compact", "overtime", "false", "label", "Copa"));
        Game game = factory.createMatch(parameters);

        persistence.save(game, AIProfile.EXPERT, false);
        MatchPersistenceService.LoadedMatch restored = persistence.load();

        assertEquals(customType, restored.game().matchConfiguration().type());
        assertEquals(new MatchValue.IntegerNumber(5),
                restored.game().matchConfiguration().parameters().get("rounds"));
        assertEquals(4, restored.game().matchConfiguration().parameters().size());
        assertEquals(2, creatorCalls.get());

        facade.startMatch(new DopoSoccer.MatchRequest(
                DopoSoccer.PlayerModeKind.PLAYER_VS_PLAYER,
                customType.id(),
                "usa",
                "cameroon",
                "EXPERT",
                Map.of("rounds", "5", "arena", "compact", "overtime", "false", "label", "Copa")));
        assertEquals(DopoSoccer.MatchStatus.ACTIVE, facade.matchStatus());
        assertEquals(3, creatorCalls.get());
    }

    @Test
    void matchConfigurationRequiresOnlyTheParametersRelevantToItsType() {
        MatchType customType = MatchType.of("custom-rounds");

        assertEquals(customType,
                new MatchConfiguration(customType, Map.of()).type());
        assertEquals(Map.of("rounds", new MatchValue.IntegerNumber(5)),
                new MatchConfiguration(customType,
                        Map.of("rounds", new MatchValue.IntegerNumber(5))).parameters());
        assertFalse(java.util.Arrays.stream(MatchConfiguration.class.getRecordComponents())
                .anyMatch(component -> component.getName().equals("goalLimit")
                        || component.getName().equals("durationSeconds")));
        assertFalse(java.util.Arrays.stream(MatchSaveData.class.getRecordComponents())
                .anyMatch(component -> component.getName().equals("goalLimit")
                        || component.getName().equals("durationSeconds")));
    }

    @Test
    void parameterDefinitionsValidateFormValuesByType() {
        MatchTypeOption.Parameter number = new MatchTypeOption.Parameter(
                "minutes", "Minutos", "4", 1, 60, 60, MatchTypeOption.Type.NUMBER, List.of());
        MatchTypeOption.Parameter text = new MatchTypeOption.Parameter(
                "label", "Etiqueta", "Final", 2, 8, 1, MatchTypeOption.Type.TEXT, List.of());
        MatchTypeOption.Parameter enabled = new MatchTypeOption.Parameter(
                "enabled", "Activo", "false", 0, 0, 1, MatchTypeOption.Type.BOOLEAN, List.of());
        MatchTypeOption.Parameter arena = new MatchTypeOption.Parameter(
                "arena", "Cancha", "classic", 0, 0, 1, MatchTypeOption.Type.SELECT,
                List.of("classic", "compact"));

        assertEquals("4", number.validate("4"));
        assertEquals("Final", text.validate("Final"));
        assertEquals("true", enabled.validate("TRUE"));
        assertEquals("compact", arena.validate("compact"));
        assertThrows(IllegalArgumentException.class, () -> number.validate("0"));
        assertThrows(IllegalArgumentException.class, () -> number.validate("99999999"));
        assertThrows(IllegalArgumentException.class, () -> text.validate("x"));
        assertThrows(IllegalArgumentException.class, () -> enabled.validate("yes"));
        assertThrows(IllegalArgumentException.class, () -> arena.validate("unknown"));
    }

    @Test
    void invalidModeConfigurationIsRejectedBeforeItIsPersisted() {
        MatchType type = MatchType.of("validated-rounds");
        MatchFactory factory = new MatchFactory();
        factory.register(type, "Rondas", List.of(
                        new MatchFactory.MatchParameter("rounds", "Rondas", "3", 1, 9, 1)),
                (request, first, second, physics) -> new CustomGame(
                        request.playerMode(), first, second, physics, type, Map.of()));
        Game game = factory.createMatch(new MatchFactory.MatchParameters(
                PlayerMode.PLAYER_VS_PLAYER, type, "usa", "cameroon", AIProfile.EXPERT,
                Map.of("rounds", "4")));
        InMemoryMatchRepository repository = new InMemoryMatchRepository();
        MatchPersistenceService persistence = new MatchPersistenceService(repository, factory);

        assertThrows(DopoSoccerException.class, () -> persistence.save(game, AIProfile.EXPERT, false));
        assertNull(repository.data);
        assertThrows(DopoSoccerException.class, () -> factory.validateConfiguration(
                new MatchConfiguration(type, Map.of("rounds", new MatchValue.Text("4")))));
    }

    private static final class InMemoryMatchRepository implements MatchSaveRepository {
        private MatchSaveData data;

        @Override
        public void save(MatchSaveData data) {
            this.data = data;
        }

        @Override
        public MatchSaveData load() {
            if (data == null) throw new DopoSoccerException("No hay una partida guardada.");
            return data;
        }

        @Override
        public boolean exists() {
            return data != null;
        }
    }

    private static final class InMemoryProfileRepository implements PlayerProfileRepository {
        private final Map<String, PlayerProfile> profiles = new HashMap<>();

        @Override
        public PlayerProfile load(String slot) {
            return profiles.get(slot);
        }

        @Override
        public void save(String slot, PlayerProfile profile) {
            profiles.put(slot, profile);
        }
    }

    private static final class CustomGame extends Game {
        private final MatchType type;
        private Map<String, String> parameters;

        private CustomGame(PlayerMode mode, domain.model.entity.Country first,
                domain.model.entity.Country second, domain.model.physics.PhysicsSimulation physics,
                MatchType type, Map<String, String> parameters) {
            super(mode, first, second, physics);
            this.type = type;
            this.parameters = Map.copyOf(parameters);
        }

        @Override
        public MatchConfiguration matchConfiguration() {
            Map<String, MatchValue> typedParameters = new HashMap<>();
            parameters.forEach((key, value) -> {
                if ("rounds".equals(key)) {
                    typedParameters.put(key, new MatchValue.IntegerNumber(Integer.parseInt(value)));
                } else if ("overtime".equals(key)) {
                    typedParameters.put(key, new MatchValue.Flag(Boolean.parseBoolean(value)));
                } else {
                    typedParameters.put(key, new MatchValue.Text(value));
                }
            });
            return new MatchConfiguration(type, typedParameters);
        }

        @Override
        public void restoreMatchConfiguration(MatchConfiguration configuration) {
            if (configuration == null || !type.equals(configuration.type())) {
                throw new DopoSoccerException("La configuración no corresponde a la modalidad personalizada.");
            }
            parameters = new HashMap<>();
            configuration.parameters().forEach((key, value) -> {
                if (value instanceof MatchValue.IntegerNumber number) {
                    parameters.put(key, Integer.toString(number.value()));
                } else if (value instanceof MatchValue.Flag flag) {
                    parameters.put(key, Boolean.toString(flag.value()));
                } else if (value instanceof MatchValue.Text text) {
                    parameters.put(key, text.value());
                } else {
                    throw new DopoSoccerException("Tipo de parámetro no soportado por la modalidad de prueba.");
                }
            });
        }
    }
}