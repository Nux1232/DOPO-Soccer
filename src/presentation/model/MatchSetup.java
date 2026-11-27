package presentation.model;

import java.util.HashMap;
import java.util.Map;

/** Configuración de partido gestionada por la interfaz, independiente de las clases del dominio. */
public final class MatchSetup {
    /** Resoluciones predefinidas disponibles para la ventana del partido. */
    public enum Resolution {
        /** Resolución pequeña (800 × 450). */
        SMALL(800, 450),
        /** Resolución mediana (960 × 540). */
        MEDIUM(960, 540),
        /** Resolución grande (1280 × 720). */
        LARGE(1280, 720);

        private final int width, height;
        Resolution(int width, int height) { this.width = width; this.height = height; }

        /** @return ancho de ventana en píxeles */
        public int width() { return width; }
        /** @return alto de ventana en píxeles */
        public int height() { return height; }
    }

    private final String playerMode;
    private String matchType = "goals";
    private int goalLimit = 10;
    private int durationSeconds = 240;
    private String firstCountry = "south-korea";
    private String secondCountry = "cameroon";
    private String aiProfile = "EXPERT";
    private Resolution resolution = Resolution.MEDIUM;
    private final Map<String, String> parameters = new HashMap<>();

    /**
     * Crea una configuración inicial para el modo de jugadores indicado.
     *
     * @param playerMode identificador del modo ({@code PLAYER_VS_PLAYER} o {@code PLAYER_VS_MACHINE})
     */
    public MatchSetup(String playerMode) { this.playerMode = playerMode; }
    /** @return modo de participación configurado */
    public String playerMode() { return playerMode; }
    /** @return identificador de la modalidad seleccionada */
    public String matchType() { return matchType; }
    /** @param value identificador de la modalidad */
    public void matchType(String value) {
        if (value != null && !value.isBlank()) matchType = value;
    }
    /** @return valor del parámetro o el predeterminado indicado */
    public String parameter(String id, String defaultValue) {
        return parameters.getOrDefault(id, defaultValue);
    }
    /** @param id identificador del parámetro @param value valor textual introducido */
    public void setParameter(String id, String value) {
        if (id != null && !id.isBlank() && value != null) parameters.put(id, value);
    }
    /** @return copia inmutable de los parámetros configurados */
    public Map<String, String> parameters() { return Map.copyOf(parameters); }
    /** @return límite de goles configurado */
    public int goalLimit() { return goalLimit; }
    /** @param value nuevo límite de goles (si es positivo) */
    public void goalLimit(int value) { if (value > 0) goalLimit = value; }
    /** @return duración del partido en segundos */
    public int durationSeconds() { return durationSeconds; }
    /** @param value nueva duración en segundos (si es positiva) */
    public void durationSeconds(int value) { if (value > 0) durationSeconds = value; }
    /** @return identificador del país del jugador 1 */
    public String firstCountry() { return firstCountry; }
    /** @param value nuevo país del jugador 1 */
    public void firstCountry(String value) { if (value != null) firstCountry = value; }
    /** @return resolución seleccionada para el partido */
    public Resolution resolution() { return resolution; }
    /** @param value nueva resolución de ventana */
    public void resolution(Resolution value) { if (value != null) resolution = value; }
    /** @return identificador del perfil de máquina seleccionado */
    public String aiProfile() { return aiProfile; }
    /** @param value nuevo perfil de máquina */
    public void aiProfile(String value) { if (value != null) aiProfile = value; }
    /** @return identificador del país del jugador 2 o máquina */
    public String secondCountry() { return secondCountry; }
    /** @param value nuevo país del jugador 2 o máquina */
    public void secondCountry(String value) { if (value != null) secondCountry = value; }
}
