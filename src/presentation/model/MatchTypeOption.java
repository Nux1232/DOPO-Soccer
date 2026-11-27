package presentation.model;

import java.util.List;

/** Opción de modalidad de partido que se puede presentar en la interfaz. */
public record MatchTypeOption(String id, String displayName, List<Parameter> parameters) {
    public MatchTypeOption {
        parameters = List.copyOf(parameters);
    }

    /** Campo de configuración que se muestra en el formulario de selección. */
    public record Parameter(
            String id, String label, String defaultValue, int minimum, int maximum, int multiplier) { }
}
