package domain.model.entity;

/**
 * Países disponibles como personaje en el juego: los cuatro del Anexo 1
 * del enunciado (Corea del Sur, Camerún, Nigeria y Estados Unidos).
 * Cada país tiene un nombre para mostrar en pantalla y un color
 * asociado, escrito en formato hexadecimal (por ejemplo, "#008751").
 */
public enum Country {

    /** Corea del Sur. */
    SOUTH_KOREA("Corea del Sur", "#CD2E3A"),

    /** Camerún. */
    CAMEROON("Camerún", "#F2B705"),

    /** Nigeria. */
    NIGERIA("Nigeria", "#008751"),

    /** Estados Unidos. */
    USA("Estados Unidos", "#3C3B6E");

    /** Nombre del país tal como se muestra al usuario. */
    private final String displayName;

    /** Color asociado al país, en formato hexadecimal. */
    private final String color;

    /**
     * Crea un país con su nombre visible y su color.
     * Solo se usa para definir las constantes de este enum.
     *
     * @param displayName nombre del país para mostrar en pantalla
     * @param color color del país en formato hexadecimal
     */
    Country(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    /**
     * Devuelve el nombre del país para mostrar en pantalla.
     *
     * @return el nombre visible del país
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Devuelve el color asociado al país.
     *
     * @return el color en formato hexadecimal
     */
    public String color() {
        return color;
    }

    /**
     * Representa el país como texto usando su nombre visible,
     * en lugar del nombre de la constante (por ejemplo, "Corea del Sur"
     * en vez de "SOUTH_KOREA").
     *
     * @return el nombre visible del país
     */
    @Override
    public String toString() {
        return displayName;
    }
}