package presentation.model;

/** UI-owned match configuration, independent of domain configuration classes. */
public final class MatchSetup {
    public enum Type { GOALS, TIME }
    public enum Country { SOUTH_KOREA, CAMEROON, NIGERIA, USA }

    private final String playerMode;
    private Type type = Type.GOALS;
    private int goalLimit = 10;
    private int durationSeconds = 240;
    private Country firstCountry = Country.SOUTH_KOREA;
    private Country secondCountry = Country.CAMEROON;

    public MatchSetup(String playerMode) { this.playerMode = playerMode; }
    public String playerMode() { return playerMode; }
    public Type type() { return type; }
    public void type(Type value) { if (value != null) type = value; }
    public int goalLimit() { return goalLimit; }
    public void goalLimit(int value) { if (value > 0) goalLimit = value; }
    public int durationSeconds() { return durationSeconds; }
    public void durationSeconds(int value) { if (value > 0) durationSeconds = value; }
    public Country firstCountry() { return firstCountry; }
    public void firstCountry(Country value) { if (value != null) firstCountry = value; }
    public Country secondCountry() { return secondCountry; }
    public void secondCountry(Country value) { if (value != null) secondCountry = value; }
}
