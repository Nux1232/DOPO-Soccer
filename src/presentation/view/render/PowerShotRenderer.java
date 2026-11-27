package presentation.view.render;

import javafx.scene.canvas.GraphicsContext;
import presentation.model.GameSnapshot;

/** Estrategia de presentación para el efecto de un tiro especial sobre el balón. */
@FunctionalInterface
public interface PowerShotRenderer {
    /** Dibuja el efecto visual alrededor de una copia del balón. */
    void render(GraphicsContext graphics, GameSnapshot match, double ballX, double ballY);
}
