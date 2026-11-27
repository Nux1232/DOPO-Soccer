
package presentation.view.render;

import presentation.model.GameSnapshot;
import presentation.model.GameSnapshot.PlayerSnapshot;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Map;
import java.util.Objects;

/**
 * Renderiza visualmente el partido.
 *
 * Esta clase pertenece a Presentation:
 * solamente dibuja el estado del dominio.
 * No modifica las reglas ni la física.
 */
public final class GameRenderer {

    private static final String BACKGROUND_PATH =
            "/resources/Images/background_match.png";

    private static final double HUD_WIDTH = 360;
    private static final double HUD_HEIGHT = 122;
    private static final double BAR_WIDTH = 230;

    private final Image backgroundImage;
    private final Map<String, Image> powerShotImages = new java.util.HashMap<>();
    private final Map<String, Image> flagImages = new java.util.HashMap<>();
    private final java.util.Set<String> missingAssets = new java.util.HashSet<>();

    /**
     * Inicializa el renderizador precargando la imagen de fondo del estadio.
     */
    public GameRenderer() {
        backgroundImage = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream(BACKGROUND_PATH),
                        "No se encontró background_match.png"
                )
        );
    }

    private Image powerShotImage(String asset) {
        if (asset == null || asset.isBlank()) {
            return null;
        }
        Image cached = powerShotImages.get(asset);
        if (cached != null) {
            return cached;
        }
        String path = "/resources/effects/powers/" + asset;
        if (missingAssets.contains(path)) {
            return null;
        }
        var resource = getClass().getResource(path);
        if (resource == null) {
            missingAssets.add(path);
            System.err.println("Recurso visual opcional no encontrado: " + path);
            return null;
        }
        Image image = new Image(resource.toExternalForm(), false);
        if (image.isError()) {
            throw new IllegalStateException("No se pudo cargar el recurso visual: " + path,
                    image.getException());
        }
        powerShotImages.put(asset, image);
        return image;
    }

    /**
     * Dibuja todos los elementos visibles del partido en el orden de capas correcto.
     *
     * @param graphics contexto gráfico del lienzo JavaFX
     * @param match instantánea actual del partido
     */
    public void render(GraphicsContext graphics, GameSnapshot match) {

        drawField(graphics, match);

        drawGoals(graphics, match);
        drawPlayer(graphics, match.firstPlayer(), match.groundY());
        drawPlayer(graphics, match.secondPlayer(), match.groundY());

        drawBall(graphics, match);

        // El HUD siempre queda encima del resto.
        drawScoreboard(graphics, match);
    }

    /**
     * Dibuja el estadio y alinea el césped con la coordenada lógica del suelo.
     */
    private void drawField(GraphicsContext graphics, GameSnapshot match) {

        graphics.setFill(Color.web("#061b35"));
        graphics.fillRect(
                0,
                0,
                match.width(),
                match.height()
        );

        graphics.drawImage(
                backgroundImage,
                0,
                56,
                match.width(),
                match.height()
        );
    }

    /** Arcos alineados con las dimensiones físicas de la cancha. */
    private void drawGoals(GraphicsContext g, GameSnapshot match) {
        double goalHeight = match.goalTop() > 0 && match.groundY() > match.goalTop()
                ? match.groundY() - match.goalTop()
                : match.height() * 0.19;
        double goalWidth = match.goalDepth() > 0
                ? match.goalDepth()
                : goalHeight * 0.58;
        double bottom = match.groundY() + 2;
        drawGoal(g, 8, bottom - goalHeight, goalWidth, goalHeight, false);
        drawGoal(g, match.width() - goalWidth - 8, bottom - goalHeight, goalWidth, goalHeight, true);
    }

    private void drawGoal(GraphicsContext g, double x, double y, double w, double h, boolean mirrored) {
        g.save();
        if (mirrored) {
            g.translate(x + w, 0);
            g.scale(-1, 1);
            x = 0;
        }
        // Red detrás del marco
        g.setStroke(Color.rgb(225, 245, 255, 0.42));
        g.setLineWidth(1.0);
        for (int i = 1; i < 7; i++) g.strokeLine(x + w * i / 7.0, y + 5, x + w * i / 7.0, y + h);
        for (int i = 1; i < 9; i++) g.strokeLine(x + 2, y + h * i / 9.0, x + w, y + h * i / 9.0);
        // Profundidad y postes blancos
        g.setStroke(Color.rgb(24, 39, 55, 0.9));
        g.setLineWidth(Math.max(5, w * 0.07));
        g.strokeLine(x + 1, y, x + 1, y + h);
        g.strokeLine(x + w, y, x + w, y + h);
        g.strokeLine(x + 1, y, x + w, y);
        g.setStroke(Color.WHITE);
        g.setLineWidth(Math.max(3, w * 0.045));
        g.strokeLine(x + 3, y + 2, x + 3, y + h);
        g.strokeLine(x + w - 2, y + 2, x + w - 2, y + h);
        g.strokeLine(x + 3, y + 2, x + w - 2, y + 2);
        g.restore();
    }

    /**
     * Dibuja un jugador, su sombra, su indumentaria y sus indicadores de estado.
     */
    private void drawPlayer(GraphicsContext graphics, PlayerSnapshot player, double groundY) {

        double x = player.x();
        double y = player.y();
        Color jersey = Color.web(player.jerseyColor());

        // Sombra.
        graphics.setFill(Color.rgb(35, 44, 54, 0.25));
        graphics.fillOval(
                x - PlayerVisualMetrics.WIDTH / 2,
                groundY - 5,
                PlayerVisualMetrics.WIDTH,
                10
        );

        graphics.save();
        graphics.translate(x, y);
        graphics.scale(PlayerVisualMetrics.scaleX(), PlayerVisualMetrics.scaleY());

        if (player.auraActive()) {
            graphics.setStroke(Color.rgb(80, 215, 255, 0.85));
            graphics.setLineWidth(3.0);
            graphics.strokeOval(-32, -55, 64, 88);
        }

        // Camiseta.
        graphics.setFill(jersey);
        graphics.fillRoundRect(
                -21,
                -9,
                42,
                30,
                12,
                12
        );

        Image flag = flagFor(player.flagAsset());
        if (flag != null) {
            double fw = 17, fh = 11;
            graphics.drawImage(flag, -fw / 2, 1, fw, fh);
            graphics.setStroke(Color.rgb(255,255,255,0.9));
            graphics.setLineWidth(0.8);
            graphics.strokeRect(-fw / 2, 1, fw, fh);
        }

        // Cabeza.
        graphics.setFill(Color.web("#f4c49a"));
        graphics.fillOval(
                -25,
                -PlayerVisualMetrics.TOP_OFFSET,
                25 * 2,
                25 * 2
        );

        // El ojo y la pupila se orientan hacia el lado al que apunta la patada.
        int facing = player.facingDirection();
        double eyeX = facing > 0 ? 7 : -16;
        graphics.setFill(Color.WHITE);
        graphics.fillOval(eyeX, -34, 10, 8);
        graphics.setFill(Color.web("#263238"));
        graphics.fillOval(eyeX + (facing > 0 ? 5 : 1), -33, 4, 6);

        graphics.setStroke(Color.rgb(31, 45, 54, 0.6));
        if (player.kicking()) {
            double direction = facing;
            graphics.setLineWidth(2);
            graphics.strokeLine(-13 * direction, 20, -16 * direction, 29);
            graphics.setStroke(Color.web("#263238"));
            graphics.setLineWidth(4);
            graphics.strokeLine(13 * direction, 18, 24 * direction, 24);
            graphics.strokeLine(24 * direction, 24, 34 * direction, 16);
        } else {
            graphics.setLineWidth(2);
            graphics.strokeLine(-13, 20, -16, 29);
            graphics.strokeLine(13, 20, 16, 29);
        }

        drawPlayerStatusOverlay(graphics, player);
        graphics.restore();
    }

    private void drawPlayerStatusOverlay(GraphicsContext graphics, PlayerSnapshot player) {
        String effect = player.statusEffect();
        if (effect != null && !"NONE".equals(effect)) {
            switch (effect) {
                case "PUSHED" -> {
                    graphics.setStroke(Color.web("#2b9cff", 0.9));
                    graphics.setLineWidth(3.0);
                    graphics.strokeOval(-30, -52, 60, 82);
                }
                case "ELECTRIFIED" -> {
                    graphics.setStroke(Color.web("#ffd43b"));
                    graphics.setLineWidth(2.6);
                    graphics.strokeLine(-26, -40, -14, -22);
                    graphics.strokeLine(-14, -22, -24, -4);
                    graphics.strokeLine(24, -36, 12, -16);
                    graphics.strokeLine(12, -16, 22, 4);
                }
                case "UNCONSCIOUS" -> {
                    graphics.setStroke(Color.web("#d56bff", 0.9));
                    graphics.setLineWidth(2.4);
                    graphics.strokeOval(-20, -64, 40, 10);
                    graphics.setFill(Color.web("#ffe066"));
                    graphics.fillOval(-16, -62, 5, 5);
                    graphics.fillOval(11, -62, 5, 5);
                }
                case "CONFUSED" -> {
                    graphics.setFill(Color.web("#ff7eb6"));
                    graphics.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 15));
                    graphics.setTextAlign(TextAlignment.CENTER);
                    graphics.fillText("?!", 0, -58);
                }
                default -> { }
            }
        }
        if (player.stunned()) {
            graphics.setFill(Color.web("#ffd43b"));
            graphics.fillOval(-14, -60, 6, 6);
            graphics.fillOval(0, -64, 6, 6);
            graphics.fillOval(10, -59, 6, 6);
        }
    }

    private Image flagFor(String flagAsset) {
        if (flagAsset == null || flagAsset.isBlank()) return null;
        Image cached = flagImages.get(flagAsset);
        if (cached != null) return cached;
        String path = "/resources/Images/flags/" + flagAsset;
        if (missingAssets.contains(path)) return null;
        var stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            missingAssets.add(path);
            return null;
        }
        Image image = new Image(stream);
        flagImages.put(flagAsset, image);
        return image;
    }

    /**
     * Dibuja el balón o las copias visuales ilusorias del tiro especial activo.
     */
    private void drawBall(GraphicsContext graphics, GameSnapshot match) {
        if (match.powerShotRenderCopies()) {
            double elapsed = match.powerShotElapsed();
            double rotation = elapsed * 1.6;
            for (int i = 0; i < 8; i++) {
                double angle = rotation + Math.PI * 2 * i / 8;
                double x = match.ballX() + Math.cos(angle) * 38;
                double y = match.ballY() + Math.sin(angle) * 26;
                drawBallAt(graphics, x, y, match);
            }
            return;
        }
        drawBallAt(graphics, match.ballX(), match.ballY(), match);
    }

    private void drawBallAt(GraphicsContext graphics, double x, double y, GameSnapshot match) {
        graphics.setFill(Color.rgb(25, 38, 48, 0.2));

        graphics.fillOval(
                x - match.ballRadius(),
                match.groundY() - 3,
                match.ballRadius() * 2,
                7
        );

        if (match.activePowerShot() != null) {
            Image effectImage = powerShotImage(match.powerShotAsset());
            if (effectImage != null) {
                graphics.drawImage(effectImage, x - 40, y - 30, 80, 60);
            } else {
                graphics.setFill(Color.web(match.powerShotColor(), 0.42));
                graphics.fillOval(x - 25, y - 25, 50, 50);
            }
        }

        graphics.setFill(Color.WHITE);

        graphics.fillOval(
                x - match.ballRadius(),
                y - match.ballRadius(),
                match.ballRadius() * 2,
                match.ballRadius() * 2
        );

        graphics.setFill(Color.web("#242a31"));

        graphics.fillOval(
                x - 5,
                y - 5,
                10,
                10
        );
    }

    /**
     * Marcador central y barras laterales de ambos jugadores.
     */
    private void drawScoreboard(GraphicsContext graphics, GameSnapshot match) {

        double centerX = match.width() / 2;

        double panelX = centerX - HUD_WIDTH / 2;
        double panelY = 12;

        // Panel central.
        graphics.setFill(Color.rgb(12, 27, 45, 0.94));

        graphics.fillRoundRect(
                panelX,
                panelY,
                HUD_WIDTH,
                HUD_HEIGHT,
                18,
                18
        );

        // Nombres de países.
        graphics.setFont(
                Font.font("System", FontWeight.BOLD, 17)
        );

        graphics.setFill(Color.WHITE);
        graphics.setTextAlign(TextAlignment.CENTER);

        graphics.fillText(
                match.firstPlayer().countryName(),
                centerX - 88,
                45,
                165
        );

        graphics.fillText(
                match.secondPlayer().countryName(),
                centerX + 88,
                45,
                165
        );

        // Goles.
        graphics.setFont(
                Font.font("System", FontWeight.EXTRA_BOLD, 38)
        );

        graphics.fillText(
                match.firstPlayer().goals()
                        + "  -  "
                        + match.secondPlayer().goals(),
                centerX,
                88
        );

        // Modalidad.
        graphics.setFont(
                Font.font("System", FontWeight.BOLD, 14)
        );

        graphics.fillText(
                match.modeDescription(),
                centerX,
                119
        );

        // Barras laterales.
        drawPlayerBars(
                graphics,
                match.firstPlayer(),
                28,
                26
        );

        drawPlayerBars(
                graphics,
                match.secondPlayer(),
                match.width() - 28 - BAR_WIDTH,
                26
        );

        graphics.setTextAlign(TextAlignment.LEFT);
    }

    /**
     * Dibuja las barras de vida, carga de poder, puntos acumulados y estado activo.
     */
    private void drawPlayerBars(
            GraphicsContext graphics,
            PlayerSnapshot player,
            double x,
            double y
    ) {

        graphics.setFill(Color.rgb(12, 27, 45, 0.92));

        graphics.fillRoundRect(
                x - 8,
                y - 8,
                BAR_WIDTH + 16,
                74,
                10,
                10
        );

        // Fondo de vida.
        graphics.setFill(Color.rgb(0, 0, 0, 0.45));

        graphics.fillRoundRect(
                x,
                y,
                BAR_WIDTH,
                15,
                8,
                8
        );

        // Vida actual.
        double healthRatio = Math.max(
                0,
                Math.min(1, player.health() / player.maxHealth())
        );

        graphics.setFill(Color.web("#e94b4b"));

        graphics.fillRoundRect(
                x,
                y,
                BAR_WIDTH * healthRatio,
                15,
                8,
                8
        );

        // Fondo del poder.
        graphics.setFill(Color.rgb(0, 0, 0, 0.45));

        graphics.fillRoundRect(
                x,
                y + 23,
                BAR_WIDTH,
                11,
                6,
                6
        );

        // Carga actual.
        double powerRatio = Math.max(0, Math.min(1, player.powerCharge() / 100.0));
        graphics.setFill(Color.web("#f3d54b"));
        graphics.fillRoundRect(
                x,
                y + 23,
                BAR_WIDTH * powerRatio,
                11,
                6,
                6
        );

        // Puntos acumulados y estado activo permanente en HUD.
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
