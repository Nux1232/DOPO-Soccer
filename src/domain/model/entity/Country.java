package domain.model.entity;

import java.util.List;
import java.util.Locale;

/** Personaje seleccionable; una subclase reúne identidad, recursos y poder. */
public abstract class Country {
    public static final Country SOUTH_KOREA = new SouthKorea();
    public static final Country CAMEROON = new Cameroon();
    public static final Country NIGERIA = new Nigeria();
    public static final Country USA = new UnitedStates();
    private static final List<Country> AVAILABLE = List.of(SOUTH_KOREA, CAMEROON, NIGERIA, USA);

    private final String id;
    private final String displayName;
    private final String color;
    private final String flagAsset;
    private final PowerShot powerShot;

    protected Country(String id, String displayName, String color, String flagAsset, PowerShot powerShot) {
        this.id = id; this.displayName = displayName; this.color = color;
        this.flagAsset = flagAsset; this.powerShot = powerShot;
    }
    public final String id() { return id; }
    public final String displayName() { return displayName; }
    public final String color() { return color; }
    public final String flagAsset() { return flagAsset; }
    public final PowerShot powerShot() { return powerShot; }
    /** Compatibilidad para consumidores que antes usaban Enum.name(). */
    public final String name() { return id.toUpperCase(Locale.ROOT).replace('-', '_'); }
    public static List<Country> available() { return AVAILABLE; }
    /** Compatibilidad con el código anterior basado en enum. */
    public static Country[] values() { return AVAILABLE.toArray(Country[]::new); }
    @Override public final String toString() { return displayName; }
}
