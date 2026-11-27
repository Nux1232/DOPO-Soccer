package domain.model.game;

import java.io.Serializable;

/** Valor inmutable y serializable de configuración o estado específico de una modalidad. */
public sealed interface MatchValue extends Serializable
        permits MatchValue.Text, MatchValue.IntegerNumber, MatchValue.DecimalNumber, MatchValue.Flag {

    /**
     * Valor de texto, incluyendo opciones de selección.
     *
     * @param value texto almacenado; no puede ser {@code null}
     */
    record Text(String value) implements MatchValue {
        public Text {
            if (value == null) throw new IllegalArgumentException("El valor de texto es obligatorio.");
        }
    }

    /**
     * Número entero normalizado a la unidad propia del dominio.
     *
     * @param value valor entero
     */
    record IntegerNumber(int value) implements MatchValue { }

    /**
     * Número decimal utilizado para valores de estado como tiempo restante.
     *
     * @param value número decimal finito
     */
    record DecimalNumber(double value) implements MatchValue {
        public DecimalNumber {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("El valor decimal debe ser finito.");
            }
        }
    }

    /**
     * Valor lógico serializable.
     *
     * @param value valor booleano
     */
    record Flag(boolean value) implements MatchValue { }
}
