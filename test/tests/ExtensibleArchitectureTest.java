package tests;

import domain.DopoSoccer;
import domain.factory.MatchFactory;
import domain.model.ai.AIProfile;
import domain.model.exception.DopoSoccerException;
import domain.model.game.Game;
import domain.model.game.GoalGame;
import domain.model.game.MatchType;
import domain.model.game.PlayerMode;
import domain.model.persistence.MatchPersistenceService;
import domain.model.persistence.MatchSaveData;
import domain.model.persistence.MatchSaveRepository;
import domain.model.persistence.PlayerProfile;
import domain.model.persistence.PlayerProfileRepository;
import domain.model.persistence.PlayerProfileService;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensibleArchitectureTest {

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
}