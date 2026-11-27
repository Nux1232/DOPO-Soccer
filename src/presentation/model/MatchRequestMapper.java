package presentation.model;

import domain.DopoSoccer;

import java.util.Objects;

/** Convierte la selección de presentación al contrato de inicio de la fachada. */
public final class MatchRequestMapper {
    private MatchRequestMapper() { }

    /** @param setup configuración elegida en la pantalla de selección */
    public static DopoSoccer.MatchRequest toDomain(MatchSetup setup) {
        Objects.requireNonNull(setup, "La configuración del partido es obligatoria.");
        return new DopoSoccer.MatchRequest(
                DopoSoccer.PlayerModeKind.valueOf(setup.playerMode()),
                setup.matchType(),
                setup.firstCountry(),
                setup.secondCountry(),
                setup.aiProfile(),
                setup.parameters());
    }
}
