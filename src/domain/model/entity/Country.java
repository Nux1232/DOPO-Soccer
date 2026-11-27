package domain.model.entity;

import domain.model.exception.DopoSoccerException;

/** Identidad visual de una selección y el poder especial que utiliza durante el partido. */
public final class Country {
    private final String id;
    private final String displayName;
    private final String color;
    private final String flagAsset;
    private final Power power;

    /**
     * Crea una selección con su identidad visual y su poder asociado.
     *
     * @param id identificador único del país
     * @param displayName nombre visible en la interfaz
     * @param color color hexadecimal de la camiseta
     * @param flagAsset nombre del archivo de imagen de la bandera
     * @param power poder especial asignado al país
     * @throws DopoSoccerException si alguno de los parámetros obligatorios es nulo
     */
    public Country(String id, String displayName, String color, String flagAsset, Power power) {
        if (id == null || displayName == null || color == null || flagAsset == null || power == null) {
            throw new DopoSoccerException("La configuración del país está incompleta.");
        }
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.flagAsset = flagAsset;
        this.power = power;
    }

    /** @return identificador único del país */
    public String id() { return id; }

    /** @return nombre visible del país */
    public String displayName() { return displayName; }

    /** @return color hexadecimal de la indumentaria */
    public String color() { return color; }

    /** @return nombre del archivo de imagen de la bandera */
    public String flagAsset() { return flagAsset; }

    /** @return poder especial del país */
    public Power power() { return power; }

    /** @return nombre normalizado en mayúsculas y con guiones bajos */
    public String name() { return id.toUpperCase(java.util.Locale.ROOT).replace('-', '_'); }

    @Override
    public String toString() { return displayName; }
}
