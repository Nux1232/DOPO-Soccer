package domain.model.entity;

public enum Country {
    MEXICO("México", "#138A45"),
    ARGENTINA("Argentina", "#72C7E8"),
    BRAZIL("Brasil", "#E8C547"),
    FRANCE("Francia", "#3155A4"),
    SPAIN("España", "#C83D3D");

    private final String displayName;
    private final String color;

    Country(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String displayName() {
        return displayName;
    }

    public String color() {
        return color;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
