
package presentation.view.render;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.entity.Power;
import domain.model.game.Game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

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

    public GameRenderer() {
        backgroundImage = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream(BACKGROUND_PATH),
                        "No se encontró background_match.png"
                )
        );
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

        // Aura del poder.
        if (player.power() != Power.NONE) {
            graphics.setFill(Color.rgb(255, 227, 71, 0.32));
            graphics.fillOval(x - 39, y - 44, 78, 78);
        }

        // Sombra.
        graphics.setFill(Color.rgb(35, 44, 54, 0.25));
        graphics.fillOval(
                x - 28,
                Game.GROUND_Y - 5,
                56,
                10
        );

        // Camiseta.
        graphics.setFill(jersey);
        graphics.fillRoundRect(
                x - 21,
                y - 9,
                42,
                30,
                12,
                12
        );

        // Cabeza.
        graphics.setFill(Color.web("#f4c49a"));
        graphics.fillOval(
                x - Player.RADIUS,
                y - Player.RADIUS * 2,
                Player.RADIUS * 2,
                Player.RADIUS * 2
        );

        // Ojo.
        graphics.setFill(Color.web("#263238"));
        graphics.fillOval(
                x + (x < Game.WIDTH / 2 ? 5 : -11),
                y - 32,
                6,
                6
        );

        // Piernas.
        graphics.setStroke(Color.rgb(31, 45, 54, 0.6));
        graphics.setLineWidth(2);

        graphics.strokeLine(
                x - 13, y + 20,
                x - 16, y + 29
        );

        graphics.strokeLine(
                x + 13, y + 20,
                x + 16, y + 29
        );
    }

    /**
     * Dibuja el balón.
     */
    private void drawBall(GraphicsContext graphics, Ball ball) {

        graphics.setFill(Color.rgb(25, 38, 48, 0.2));

        graphics.fillOval(
                ball.x() - Ball.RADIUS,
                Game.GROUND_Y - 3,
                Ball.RADIUS * 2,
                7
        );

        graphics.setFill(Color.WHITE);

        graphics.fillOval(
                ball.x() - Ball.RADIUS,
                ball.y() - Ball.RADIUS,
                Ball.RADIUS * 2,
                Ball.RADIUS * 2
        );

        graphics.setFill(Color.web("#242a31"));

        graphics.fillOval(
                ball.x() - 5,
                ball.y() - 5,
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
