package presentation.model;

import java.util.Locale;
import java.util.List;

/** Opción de modalidad de partido que se puede presentar en la interfaz. */
public record MatchTypeOption(String id, String displayName, List<Parameter> parameters) {
    public MatchTypeOption {
        parameters = List.copyOf(parameters);
    }

    /** Campo de configuración que se muestra en el formulario de selección. */
    public record Parameter(
            String id,
            String label,
            String defaultValue,
            int minimum,
            int maximum,
            int multiplier,
            Type type,
            List<String> options) {
        public Parameter(String id, String label, String defaultValue,
                int minimum, int maximum, int multiplier) {
            this(id, label, defaultValue, minimum, maximum, multiplier, Type.NUMBER, List.of());
        }

        public Parameter {
            options = List.copyOf(options);
        }

        /** Valida un valor del formulario y normaliza los booleanos a minúsculas. */
        public String validate(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Selecciona un valor para " + label + ".");
            }
            switch (type) {
                case NUMBER -> {
                    try {
                        int parsed = Integer.parseInt(value);
                        if (parsed < minimum || parsed > maximum) {
                            throw new IllegalArgumentException(label + " debe estar entre "
                                    + minimum + " y " + maximum + ".");
                        }
                        Math.multiplyExact(parsed, multiplier);
                    } catch (NumberFormatException | ArithmeticException exception) {
                        throw new IllegalArgumentException(label + " debe ser un valor numérico válido.", exception);
                    }
                }
                case TEXT -> {
                    if (value.length() < minimum || value.length() > maximum) {
                        throw new IllegalArgumentException(label + " debe tener entre "
                                + minimum + " y " + maximum + " caracteres.");
                    }
                }
                case BOOLEAN -> {
                    if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                        throw new IllegalArgumentException(label + " debe ser verdadero o falso.");
                    }
                    return value.toLowerCase(Locale.ROOT);
                }
                case SELECT -> {
                    if (!options.contains(value)) {
                        throw new IllegalArgumentException("Selecciona una opción válida para " + label + ".");
                    }
                }
            }
            return value;
        }
    }

    /** Tipos de campo compatibles con el formulario genérico de modalidades. */
    public enum Type { NUMBER, TEXT, BOOLEAN, SELECT }
}
