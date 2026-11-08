package presentation.view.render;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.game.Game;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Dibuja un partido sobre un lienzo de JavaFX.
 *
 * Solo lee el estado del Game (posiciones, goles, vida, barra de
 * poder) y lo pinta; no modifica nada del partido. Todas las coordenadas son
 * las de la cancha lógica (Game#WIDTH por Game#HEIGHT): esta
 * clase no escala nada, de eso se encarga quien le entrega el
 * GraphicsContext.
 *
 * Orden de dibujo (lo último queda encima): cancha, arcos, jugadores,
 * balón y marcador.
 */
public final class GameRenderer {

    /**
     * Dibuja el partido completo en un cuadro.
     *
     * @param graphics contexto gráfico del lienzo donde se dibuja
     * @param match partido cuyo estado se va a dibujar
     */
    public void render(GraphicsContext graphics, Game match) {
        drawField(graphics);
        drawGoal(graphics, 0, true);
        drawGoal(graphics, Game.WIDTH - Game.GOAL_DEPTH, false);
        drawPlayer(graphics, match.firstPlayer());
        drawPlayer(graphics, match.secondPlayer());
        drawBall(graphics, match.ball());
        drawScoreboard(graphics, match);
    }

    /**
     * Dibuja el fondo de la cancha: cielo, césped en dos tonos, la línea
     * del suelo, la línea y el círculo del centro y una mancha clara
     * translúcida encima del césped.
     *
     * @param graphics contexto gráfico del lienzo
     */
    private void drawField(GraphicsContext graphics) {
        graphics.setFill(Color.web("#88c9ee"));
        graphics.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);
        graphics.setFill(Color.web("#84c75b"));
        graphics.fillRect(0, Game.GROUND_Y, Game.WIDTH, Game.HEIGHT - Game.GROUND_Y);
        graphics.setFill(Color.web("#529b3e"));
        graphics.fillRect(0, Game.GROUND_Y + 18, Game.WIDTH, Game.HEIGHT - Game.GROUND_Y);
        graphics.setStroke(Color.rgb(255, 255, 255, 0.8));
        graphics.setLineWidth(4);
        graphics.strokeLine(0, Game.GROUND_Y, Game.WIDTH, Game.GROUND_Y);
        graphics.strokeLine(Game.WIDTH / 2, Game.GROUND_Y, Game.WIDTH / 2, Game.GROUND_Y - 70);
        graphics.strokeOval(Game.WIDTH / 2 - 48, Game.GROUND_Y - 118, 96, 96);
        graphics.setFill(Color.rgb(255, 255, 255, 0.12));
        graphics.fillOval(Game.WIDTH / 2 - 190, Game.GROUND_Y - 215, 380, 160);
    }

    /**
     * Dibuja un arco: el poste y el larguero en blanco y una red formada por
     * líneas verticales y horizontales delgadas y translúcidas.
     *
     * @param graphics contexto gráfico del lienzo
     * @param x coordenada x del borde izquierdo del arco
     * @param leftGoal true si es el arco izquierdo (su poste queda a
     *        la derecha del arco, del lado de la cancha) false si
     *        es el derecho (su poste queda en x)
     */
    private void drawGoal(GraphicsContext graphics, double x, boolean leftGoal) {
        graphics.setStroke(Color.WHITE);
        graphics.setLineWidth(9);
        double postX = leftGoal ? x + Game.GOAL_DEPTH : x;
        graphics.strokeLine(postX, Game.GROUND_Y, postX, Game.GOAL_TOP);
        graphics.strokeLine(x, Game.GOAL_TOP, x + Game.GOAL_DEPTH, Game.GOAL_TOP);
        graphics.setStroke(Color.rgb(255, 255, 255, 0.3));
        graphics.setLineWidth(1);
        for (int i = 1; i < 5; i++) {
            double netX = x + i * Game.GOAL_DEPTH / 5;
            graphics.strokeLine(netX, Game.GOAL_TOP + 4, netX, Game.GROUND_Y);
        }
        for (int i = 1; i < 5; i++) {
            double netY = Game.GOAL_TOP + i * (Game.GROUND_Y - Game.GOAL_TOP) / 5;
            graphics.strokeLine(x, netY, x + Game.GOAL_DEPTH, netY);
        }
    }

    /**
     * Dibuja a un jugador: un círculo amarillo translúcido de aura si tiene
     * un poder activo, la sombra en el suelo, la camiseta con el color de su
     * país, la cabeza, el ojo (que mira hacia el centro de la cancha) y las
     * piernas.
     *
     * @param graphics contexto gráfico del lienzo
     * @param player jugador que se dibuja
     */
    private void drawPlayer(GraphicsContext graphics, Player player) {
        double x = player.x();
        double y = player.y();
        Color jersey = Color.web(player.country().color());
        if (player.power() != domain.model.entity.Power.NONE) {
            graphics.setFill(Color.rgb(255, 227, 71, 0.32));
            graphics.fillOval(x - 39, y - 44, 78, 78);
        }
        graphics.setFill(Color.rgb(35, 44, 54, 0.22));
        graphics.fillOval(x - 28, Game.GROUND_Y - 5, 56, 10);
        graphics.setFill(jersey);
        graphics.fillRoundRect(x - 21, y - 9, 42, 30, 12, 12);
        graphics.setFill(Color.web("#f4c49a"));
        graphics.fillOval(x - Player.RADIUS, y - Player.RADIUS * 2, Player.RADIUS * 2, Player.RADIUS * 2);
        graphics.setFill(Color.web("#263238"));
        graphics.fillOval(x + (x < Game.WIDTH / 2 ? 5 : -11), y - 32, 6, 6);
        graphics.setStroke(Color.rgb(31, 45, 54, 0.6));
        graphics.setLineWidth(2);
        graphics.strokeLine(x - 13, y + 20, x - 16, y + 29);
        graphics.strokeLine(x + 13, y + 20, x + 16, y + 29);
    }

    /**
     * Dibuja el balón: su sombra en el suelo, un círculo blanco y un punto
     * oscuro en el centro.
     *
     * @param graphics contexto gráfico del lienzo
     * @param ball balón que se dibuja
     */
    private void drawBall(GraphicsContext graphics, Ball ball) {
        graphics.setFill(Color.rgb(25, 38, 48, 0.2));
        graphics.fillOval(ball.x() - Ball.RADIUS, Game.GROUND_Y - 3, Ball.RADIUS * 2, 7);
        graphics.setFill(Color.WHITE);
        graphics.fillOval(ball.x() - Ball.RADIUS, ball.y() - Ball.RADIUS,
                Ball.RADIUS * 2, Ball.RADIUS * 2);
        graphics.setFill(Color.web("#242a31"));
        graphics.fillOval(ball.x() - 5, ball.y() - 5, 10, 10);
    }

    /**
     * Dibuja el marcador central (nombres de los países, goles y el texto
     * fijo "PARTIDO LIBRE") y las barras de vida y poder de cada jugador,
     * a la izquierda y a la derecha de la pantalla.
     *
     * @param graphics contexto gráfico del lienzo
     * @param match partido del que se toman los nombres y los goles
     */
    private void drawScoreboard(GraphicsContext graphics, Game match) {
        graphics.setFill(Color.rgb(20, 32, 43, 0.78));
        graphics.fillRoundRect(330, 16, 300, 82, 16, 16);
        graphics.setFont(Font.font("System", FontWeight.BOLD, 22));
        graphics.setFill(Color.WHITE);
        graphics.fillText(match.firstPlayer().country().displayName(), 348, 43);
        graphics.fillText(match.secondPlayer().country().displayName(), 503, 43);
        graphics.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 38));
        graphics.fillText(match.firstPlayer().goals() + "  -  " + match.secondPlayer().goals(), 400, 84);
        graphics.setFont(Font.font("System", FontWeight.BOLD, 15));
        graphics.fillText("PARTIDO LIBRE", 423, 124);
        drawPlayerBars(graphics, match.firstPlayer(), 28, 26, 230);
        drawPlayerBars(graphics, match.secondPlayer(), Game.WIDTH - 258, 26, 230);
    }

    /**
     * Dibuja las dos barras de un jugador dentro de un panel oscuro: la de
     * vida (roja, arriba) y la de poder (amarilla, abajo). El largo de la
     * parte llena es proporcional a vida actual / vida máxima y a carga del
     * poder / carga máxima.
     *
     * @param graphics contexto gráfico del lienzo
     * @param player jugador cuyas barras se dibujan
     * @param x coordenada x de la esquina izquierda de las barras
     * @param y coordenada y de la barra de vida
     * @param width ancho total de cada barra (cuando está llena)
     */
    private void drawPlayerBars(GraphicsContext graphics, Player player, double x, double y, double width) {
        graphics.setFill(Color.rgb(20, 32, 43, 0.68));
        graphics.fillRoundRect(x - 8, y - 8, width + 16, 58, 10, 10);
        graphics.setFill(Color.rgb(0, 0, 0, 0.45));
        graphics.fillRoundRect(x, y, width, 15, 8, 8);
        graphics.setFill(Color.web("#e94b4b"));
        graphics.fillRoundRect(x, y, width * player.health() / player.maxHealth(), 15, 8, 8);
        graphics.setFill(Color.rgb(0, 0, 0, 0.45));
        graphics.fillRoundRect(x, y + 23, width, 11, 6, 6);
        graphics.setFill(Color.web("#f3d54b"));
        graphics.fillRoundRect(x, y + 23, width * player.powerCharge() / Player.MAX_POWER_CHARGE, 11, 6, 6);
    }

}