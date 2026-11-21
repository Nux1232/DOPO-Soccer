
package presentation.view.render;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.entity.PowerShot;
import domain.model.game.Game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
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
    private final Map<PowerShot, Image> powerShotImages = new HashMap<>();

    public GameRenderer() {
        backgroundImage = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream(BACKGROUND_PATH),
                        "No se encontró background_match.png"
                )
        );
        for (PowerShot shot : PowerShot.available()) {
            String path = "/resources/effects/powers/" + shot.visualAsset();
            var resource = getClass().getResource(path);
            if (resource == null) {
                System.err.println("Recurso visual opcional no encontrado: " + path);
                continue;
            }
            Image image = new Image(resource.toExternalForm(), false);
            if (image.isError()) {
                throw new IllegalStateException("No se pudo cargar el recurso visual: " + path,
                        image.getException());
            }
            powerShotImages.put(shot, image);
        }
    }

    /**
     * Dibuja todos los elementos del partido.
     */
    public void render(GraphicsContext graphics, Game match) {

        drawField(graphics);

        // Los arcos ya están incluidos en el fondo.
        drawPlayer(graphics, match.firstPlayer());
        drawPlayer(graphics, match.secondPlayer());

        drawBall(graphics, match.ball());

        // El HUD siempre queda encima del resto.
        drawScoreboard(graphics, match);
    }

    /**
     * Dibuja el estadio y alinea el césped
     * con la coordenada lógica del suelo.
     */
    private void drawField(GraphicsContext graphics) {

        graphics.setFill(Color.web("#061b35"));
        graphics.fillRect(
                0,
                0,
                Game.WIDTH,
                Game.HEIGHT
        );

        graphics.drawImage(
                backgroundImage,
                0,
                56,
                Game.WIDTH,
                Game.HEIGHT
        );
    }

    /**
     * Dibuja un jugador.
     */
    private void drawPlayer(GraphicsContext graphics, Player player) {

        double x = player.x();
        double y = player.y();
        Color jersey = Color.web(player.country().color());

        // Sombra.
        graphics.setFill(Color.rgb(35, 44, 54, 0.25));
        graphics.fillOval(
                x - PlayerVisualMetrics.WIDTH / 2,
                Game.GROUND_Y - 5,
                PlayerVisualMetrics.WIDTH,
                10
        );

        graphics.save();
        graphics.translate(x, y);
        graphics.scale(PlayerVisualMetrics.scaleX(), PlayerVisualMetrics.scaleY());

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

        // Cabeza.
        graphics.setFill(Color.web("#f4c49a"));
        graphics.fillOval(
                -Player.RADIUS,
                -PlayerVisualMetrics.TOP_OFFSET,
                Player.RADIUS * 2,
                Player.RADIUS * 2
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
        graphics.restore();
    }

    /**
     * Dibuja el balón.
     */
    private void drawBall(GraphicsContext graphics, Ball ball) {
        PowerShot shot = ball.activePowerShot();
        if (shot != null && shot.renderCopies()) {
            double elapsed = shot.durationSeconds() - ball.powerShotTime();
            double rotation = elapsed * 1.6;
            for (int i = 0; i < 8; i++) {
                double angle = rotation + Math.PI * 2 * i / 8;
                double x = ball.x() + Math.cos(angle) * 38;
                double y = ball.y() + Math.sin(angle) * 26;
                drawBallAt(graphics, x, y, shot);
            }
            return;
        }
        drawBallAt(graphics, ball.x(), ball.y(), shot);
    }

    private void drawBallAt(GraphicsContext graphics, double x, double y, PowerShot shot) {
        graphics.setFill(Color.rgb(25, 38, 48, 0.2));

        graphics.fillOval(
                x - Ball.RADIUS,
                Game.GROUND_Y - 3,
                Ball.RADIUS * 2,
                7
        );

        if (shot != null) {
            Image effectImage = powerShotImages.get(shot);
            if (effectImage != null) {
                graphics.drawImage(effectImage, x - 40, y - 30, 80, 60);
            } else {
                graphics.setFill(Color.web(shot.visualColor(), 0.42));
                graphics.fillOval(x - 25, y - 25, 50, 50);
            }
        }

        graphics.setFill(Color.WHITE);

        graphics.fillOval(
                x - Ball.RADIUS,
                y - Ball.RADIUS,
                Ball.RADIUS * 2,
                Ball.RADIUS * 2
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
     * Marcador central y barras de ambos jugadores.
     */
    private void drawScoreboard(GraphicsContext graphics, Game match) {

        double centerX = Game.WIDTH / 2;

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
                match.firstPlayer().country().displayName(),
                centerX - 88,
                45,
                165
        );

        graphics.fillText(
                match.secondPlayer().country().displayName(),
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
                "PARTIDO LIBRE",
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
                Game.WIDTH - 28 - BAR_WIDTH,
                26
        );

        graphics.setTextAlign(TextAlignment.LEFT);
    }

    /**
     * Dibuja las barras de vida y poder.
     */
    private void drawPlayerBars(
            GraphicsContext graphics,
            Player player,
            double x,
            double y
    ) {

        graphics.setFill(Color.rgb(12, 27, 45, 0.92));

        graphics.fillRoundRect(
                x - 8,
                y - 8,
                BAR_WIDTH + 16,
                58,
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
        double powerRatio = Math.max(0, Math.min(1, player.powerCharge() / Player.MAX_POWER_CHARGE));
        graphics.setFill(Color.web("#f3d54b"));
        graphics.fillRoundRect(
                x,
                y + 23,
                BAR_WIDTH * powerRatio,
                11,
                6,
                6
        );
    }
}
