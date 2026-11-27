package presentation.model;

/** Opción de país desacoplada de las entidades del dominio. */
public record CountryOption(String id, String displayName, String color, String flagAsset) {
    @Override
    public String toString() {
        return displayName;
    }
}
