package tests;

import domain.DopoSoccer;
import domain.factory.MatchFactory;
import domain.model.ai.AIProfile;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.game.GoalGame;
import domain.model.game.MatchConfiguration;
import domain.model.game.MatchCompletionService;
import domain.model.game.MatchType;
import domain.model.game.PlayerMode;
import domain.model.persistence.MatchPersistenceService;
import domain.model.persistence.MatchSaveData;
import domain.model.persistence.MatchSaveRepository;
import domain.model.persistence.PlayerProfile;
import domain.model.persistence.PlayerProfileRepository;
import domain.model.persistence.PlayerProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
            return new GoalGame(request.playerMode(), first, second, physics, request.goalLimit());
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
            return new GoalGame(request.playerMode(), first, second, physics, request.goalLimit());
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
    void customMatchTypesCanBeRequestedCreatedAndRestoredWithoutChangingTheTypeRegistry() {
        MatchType customType = MatchType.of("sudden-death");
        AtomicInteger creatorCalls = new AtomicInteger();
        MatchFactory factory = new MatchFactory();
        factory.register(customType, "Muerte súbita", List.of(
                        new MatchFactory.MatchParameter("rounds", "Rondas", "3", 1, 9, 1)),
                (request, first, second, physics) -> {
                    creatorCalls.incrementAndGet();
                    return new CustomGame(request.playerMode(), first, second, physics, customType,
                            request.parameters());
                });
        DopoSoccer facade = new DopoSoccer(factory, new PlayerProfileService(new InMemoryProfileRepository()));
        assertTrue(facade.availableMatchTypes().stream()
                .anyMatch(type -> type.id().equals("sudden-death")
                        && type.displayName().equals("Muerte súbita")
                        && type.parameters().get(0).id().equals("rounds")));
        MatchPersistenceService persistence = new MatchPersistenceService(
                temporaryDirectory.resolve("custom-match.ser"), factory);
        MatchFactory.MatchParameters parameters = new MatchFactory.MatchParameters(
                PlayerMode.PLAYER_VS_PLAYER,
                customType,
                "usa",
                "cameroon",
                10,
                240,
                AIProfile.EXPERT,
                Map.of("rounds", "5"));
        Game game = factory.createMatch(parameters);

        persistence.save(game, AIProfile.EXPERT, false);
        MatchPersistenceService.LoadedMatch restored = persistence.load();

        assertEquals(customType, restored.game().matchConfiguration().type());
        assertEquals("5", restored.game().matchConfiguration().parameters().get("rounds"));
        assertEquals(1, restored.game().matchConfiguration().parameters().size());
        assertEquals(2, creatorCalls.get());

        facade.startMatch(new DopoSoccer.MatchRequest(
                DopoSoccer.PlayerModeKind.PLAYER_VS_PLAYER,
                customType.id(),
                "usa",
                "cameroon",
                10,
                240,
                "EXPERT",
                Map.of("rounds", "5")));
        assertEquals(DopoSoccer.MatchStatus.ACTIVE, facade.matchStatus());
        assertEquals(3, creatorCalls.get());
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
            return new MatchConfiguration(type, 10, 240, 0, false, parameters);
        }

        @Override
        public void restoreMatchConfiguration(MatchConfiguration configuration) {
            if (configuration == null || !type.equals(configuration.type())) {
                throw new DopoSoccerException("La configuración no corresponde a la modalidad personalizada.");
            }
            parameters = configuration.parameters();
        }
    }
}