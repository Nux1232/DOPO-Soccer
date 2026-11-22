package presentation.model;

/** Immutable presentation-only representation of a match frame. */
public record GameSnapshot(
        double width, double height, double groundY,
        String modeDescription, boolean finished, int winner,
        PlayerSnapshot firstPlayer, PlayerSnapshot secondPlayer,
        double ballX, double ballY, double ballRadius,
        String activePowerShot, String powerShotAsset, String powerShotColor,
        double powerShotDuration, double powerShotElapsed) {

    public record PlayerSnapshot(String countryName, String jerseyColor,
            double x, double y, int goals, double health, double maxHealth,
            double powerCharge, int facingDirection, boolean kicking,
            boolean stunned, boolean auraActive, String statusEffect) { }
}
