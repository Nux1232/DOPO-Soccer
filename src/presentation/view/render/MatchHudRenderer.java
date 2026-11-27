package presentation.view.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import presentation.model.GameSnapshot;
import presentation.model.GameSnapshot.PlayerSnapshot;

/** Renderiza marcador, barras de jugador y datos de estado del partido. */
final class MatchHudRenderer {
    private static final double HUD_WIDTH = 360;
    private static final double HUD_HEIGHT = 122;
    private static final double BAR_WIDTH = 230;

    void render(GraphicsContext graphics, GameSnapshot match) {
        double centerX = match.width() / 2;
        double panelX = centerX - HUD_WIDTH / 2;
        double panelY = 12;

        graphics.setFill(Color.rgb(12, 27, 45, 0.94));
        graphics.fillRoundRect(panelX, panelY, HUD_WIDTH, HUD_HEIGHT, 18, 18);

        graphics.setFont(Font.font("System", FontWeight.BOLD, 17));
        graphics.setFill(Color.WHITE);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.fillText(match.firstPlayer().countryName(), centerX - 88, 45, 165);
        graphics.fillText(match.secondPlayer().countryName(), centerX + 88, 45, 165);

        graphics.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 38));
        graphics.fillText(match.firstPlayer().goals() + "  -  " + match.secondPlayer().goals(), centerX, 88);

        graphics.setFont(Font.font("System", FontWeight.BOLD, 14));
        graphics.fillText(match.modeDescription(), centerX, 119);
        drawPlayerBars(graphics, match.firstPlayer(), 28, 26);
        drawPlayerBars(graphics, match.secondPlayer(), match.width() - 28 - BAR_WIDTH, 26);
        graphics.setTextAlign(TextAlignment.LEFT);
    }

    private void drawPlayerBars(GraphicsContext graphics, PlayerSnapshot player, double x, double y) {
        graphics.setFill(Color.rgb(12, 27, 45, 0.92));
        graphics.fillRoundRect(x - 8, y - 8, BAR_WIDTH + 16, 94, 10, 10);
        graphics.setFill(Color.rgb(0, 0, 0, 0.45));
        graphics.fillRoundRect(x, y, BAR_WIDTH, 15, 8, 8);

        double healthRatio = Math.max(0, Math.min(1, player.health() / player.maxHealth()));
        graphics.setFill(Color.web("#e94b4b"));
        graphics.fillRoundRect(x, y, BAR_WIDTH * healthRatio, 15, 8, 8);
        graphics.setFill(Color.rgb(0, 0, 0, 0.45));
        graphics.fillRoundRect(x, y + 23, BAR_WIDTH, 11, 6, 6);

        double powerRatio = Math.max(0, Math.min(1, player.powerCharge() / 100.0));
        graphics.setFill(Color.web("#f3d54b"));
        graphics.fillRoundRect(x, y + 23, BAR_WIDTH * powerRatio, 11, 6, 6);
        graphics.setFont(Font.font("System", FontWeight.BOLD, 12));
        graphics.setFill(Color.web("#d9e6f2"));
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.fillText("Pts: " + player.points(), x, y + 53);

        String badge = statusLabelFor(player);
        if (!badge.isEmpty()) {
            graphics.setFill(badgeColorFor(player));
            graphics.setTextAlign(TextAlignment.RIGHT);
            graphics.fillText(badge, x + BAR_WIDTH, y + 53);
        }

        graphics.setFill(Color.web("#9fb3c8"));
        graphics.setFont(Font.font("System", FontWeight.NORMAL, 10));
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.fillText("VEL " + player.speed() + " · PAT " + player.kick()
                + " · SAL " + player.jump() + " · VIDA " + player.survival(), x, y + 72);
    }

    private String statusLabelFor(PlayerSnapshot player) {
        if (player.stunned()) return "ATURDIDO";
        String effect = player.statusEffect();
        if (effect != null && !"NONE".equals(effect)) {
            return switch (effect) {
                case "PUSHED" -> "EMPUJADO";
                case "ELECTRIFIED" -> "ELECTROCUTADO";
                case "UNCONSCIOUS" -> "INCONSCIENTE";
                case "CONFUSED" -> "CONFUNDIDO";
                default -> effect;
            };
        }
        return player.auraActive() ? "AURA ACTIVA" : "";
    }

    private Color badgeColorFor(PlayerSnapshot player) {
        if (player.stunned()) return Color.web("#ffd43b");
        String effect = player.statusEffect();
        if (effect != null && !"NONE".equals(effect)) {
            return switch (effect) {
                case "PUSHED" -> Color.web("#66d9ff");
                case "ELECTRIFIED" -> Color.web("#ffe066");
                case "UNCONSCIENTE", "UNCONSCIOUS" -> Color.web("#e599f7");
                case "CONFUSED" -> Color.web("#ff85a2");
                default -> Color.WHITE;
            };
        }
        return Color.web("#74c0fc");
    }
}
