package tests;

import domain.DopoSoccer;
import domain.model.game.Game;
import presentation.model.GameSnapshotMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DopoSoccerFacadeTest {

    @Test
    void facadeExposesCountriesAndAIProfilesWithoutDomainEntities() {
        DopoSoccer facade = new DopoSoccer();

        List<DopoSoccer.CountrySummary> countries = facade.availableCountries();
        List<DopoSoccer.AIProfileSummary> profiles = facade.availableAIProfiles();

        assertEquals(4, countries.size());
        assertTrue(countries.stream().anyMatch(c -> "south-korea".equals(c.id()) && "kr.png".equals(c.flagAsset())));
        assertTrue(countries.stream().anyMatch(c -> "usa".equals(c.id()) && "us.png".equals(c.flagAsset())));

        assertEquals(4, profiles.size());
        assertTrue(profiles.stream().anyMatch(p -> "EXPERT".equals(p.id()) && "EXPERTA".equals(p.displayName())));
    }

    @Test
    void facadeSnapshotIncludesGoalGeometryFlagAssetsAndRenderCopiesFlag() {
        DopoSoccer facade = new DopoSoccer();
        facade.startMatch(new DopoSoccer.MatchRequest(
                DopoSoccer.PlayerModeKind.PLAYER_VS_PLAYER,
                DopoSoccer.MatchKind.GOALS,
                "usa",
                "cameroon",
                5,
                120));

        assertEquals(DopoSoccer.MatchStatus.ACTIVE, facade.matchStatus());

        DopoSoccer.MatchSnapshot snapshot = facade.snapshot();
        assertEquals(Game.GOAL_TOP, snapshot.goalTop());
        assertEquals(Game.GOAL_DEPTH, snapshot.goalDepth());
        assertEquals("us.png", snapshot.firstPlayer().flagAsset());
        assertEquals("cm.png", snapshot.secondPlayer().flagAsset());
        assertFalse(snapshot.powerShotRenderCopies());
        assertEquals(snapshot.ballX(), snapshot.ball().x());
        assertNull(snapshot.ball().powerShot());
        var presentationSnapshot = GameSnapshotMapper.toPresentation(snapshot);
        assertEquals(snapshot.ballX(), presentationSnapshot.ball().x());
        assertNull(presentationSnapshot.ball().powerShot());

        facade.updateMatch(
                new DopoSoccer.PlayerActions(false, true, false, false, false),
                new DopoSoccer.PlayerActions(true, false, false, false, false),
                1.0 / 60.0);
        assertEquals(DopoSoccer.MatchStatus.ACTIVE, facade.matchStatus());

        facade.reset();
        assertEquals(DopoSoccer.MatchStatus.NO_MATCH, facade.matchStatus());
    }
}
