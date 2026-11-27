package presentation.model;

import domain.DopoSoccer;

/** Convierte contratos públicos del dominio en modelos inmutables de presentación. */
public final class GameSnapshotMapper {
    private GameSnapshotMapper() { }

    /** Convierte una instantánea del dominio a su representación visual. */
    public static GameSnapshot toPresentation(DopoSoccer.MatchSnapshot state) {
        return new GameSnapshot(state.width(), state.height(), state.groundY(), state.goalTop(), state.goalDepth(),
                state.modeDescription(), state.finished(), state.winner(),
                toPresentation(state.firstPlayer()), toPresentation(state.secondPlayer()),
                state.machineOpponent(), new GameSnapshot.BallSnapshot(
                        state.ball().x(), state.ball().y(), state.ball().radius(),
                        state.ball().powerShot() == null ? null : new GameSnapshot.PowerShotSnapshot(
                                state.ball().powerShot().id(), state.ball().powerShot().asset(),
                                state.ball().powerShot().color(), state.ball().powerShot().duration(),
                                state.ball().powerShot().elapsed(), state.ball().powerShot().renderCopies())));
    }

    /** Convierte el estado de un jugador del dominio al modelo de presentación. */
    public static GameSnapshot.PlayerSnapshot toPresentation(DopoSoccer.PlayerSnapshot player) {
        return new GameSnapshot.PlayerSnapshot(player.countryName(), player.jerseyColor(), player.flagAsset(),
                player.x(), player.y(), player.goals(), player.health(), player.maxHealth(),
                player.powerCharge(), player.points(), player.speed(), player.kick(), player.jump(),
                player.survival(), player.facingDirection(), player.kicking(),
                player.stunned(), "AURA".equals(player.power()), player.statusEffect());
    }

    /** Convierte controles de presentación en acciones neutras de la fachada. */
    public static DopoSoccer.PlayerActions toDomain(PlayerControls controls) {
        return new DopoSoccer.PlayerActions(
                controls.left(), controls.right(), controls.jump(), controls.kick(), controls.power());
    }
}
