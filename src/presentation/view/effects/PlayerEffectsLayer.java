
package presentation.view.effects;

import domain.model.entity.Player;
import domain.model.entity.Power;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public final class PlayerEffectsLayer extends Pane {

    private static final double AURA_SIZE = 145;
    private static final double STUN_SIZE = 95;

    private final ImageView firstAura;
    private final ImageView secondAura;
    private final ImageView firstStun;
    private final ImageView secondStun;

    public PlayerEffectsLayer() {
        firstAura = createGif("/resources/effects/chakra-energy-charge.gif",
                AURA_SIZE, AURA_SIZE);
        secondAura = createGif("/resources/effects/chakra-energy-charge.gif",
                AURA_SIZE, AURA_SIZE);
        firstStun = createGif("/resources/effects/stun-stars.gif",
                STUN_SIZE, STUN_SIZE);
        secondStun = createGif("/resources/effects/stun-stars.gif",
                STUN_SIZE, STUN_SIZE);
        getChildren().addAll(firstAura, secondAura, firstStun, secondStun);
        setMouseTransparent(true);
        setPickOnBounds(false);
    }

    private ImageView createGif(String resourcePath, double width, double height) {
        var resource = getClass().getResource(resourcePath);

        if (resource == null) {
            throw new IllegalStateException("No se encontró el GIF: " + resourcePath);
        }
        Image image = new Image(resource.toExternalForm());
        if (image.isError()) {
            throw new IllegalStateException("No se pudo cargar el GIF: " + resourcePath, image.getException());
        }

        ImageView view = new ImageView(image);
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setVisible(false);
        view.setMouseTransparent(true);

        return view;
    }

    public void update(
            Player firstPlayer,
            Player secondPlayer,
            double scale,
            double offsetX,
            double offsetY) {

        updatePlayerEffects(firstPlayer, firstAura, firstStun, scale, offsetX, offsetY);
        updatePlayerEffects(secondPlayer, secondAura, secondStun, scale, offsetX, offsetY);
    }

    private void updatePlayerEffects(
            Player player,
            ImageView aura,
            ImageView stun,
            double scale,
            double offsetX,
            double offsetY) {
        double screenX = offsetX + player.x() * scale;
        double screenY = offsetY + player.y() * scale;

        boolean auraActive = player.power() == Power.AURA;
        aura.setVisible(auraActive);

        if (auraActive) {
            double size = AURA_SIZE * scale;
            aura.setFitWidth(size);
            aura.setFitHeight(size);
            double auraOffsetX = 20 * scale;
            double auraOffsetY = -25 * scale;
            aura.relocate(screenX - size / 2 + auraOffsetX, screenY - size / 2 + auraOffsetY);
        }
        boolean stunned = player.stunned();
        stun.setVisible(stunned);

        if (stunned) {
            double size = STUN_SIZE * scale;
            stun.setFitWidth(size);
            stun.setFitHeight(size);
            stun.relocate(screenX - size / 2, screenY - Player.RADIUS * scale - size * 0.8);
        }
    }
}
