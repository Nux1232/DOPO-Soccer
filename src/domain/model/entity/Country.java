package domain.model.entity;

/**
 * Países disponibles como personaje en el juego.
 * Cada país tiene un nombre para mostrar en pantalla y un color
 * asociado, escrito en formato hexadecimal (por ejemplo, "#138A45").
 */
public enum Country {

    /** México. */
    MEXICO("México", "#138A45"),

    /** Argentina. */
    ARGENTINA("Argentina", "#72C7E8"),

    /** Brasil. */
    BRAZIL("Brasil", "#E8C547"),

    /** Francia. */
    FRANCE("Francia", "#3155A4"),

    /** España. */
    SPAIN("España", "#C83D3D");

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
     * en lugar del nombre de la constante (por ejemplo, "México"
     * en vez de "MEXICO").
     *
     * @return el nombre visible del país
     */
    @Override
    public String toString() {
        return displayName;
    }
}