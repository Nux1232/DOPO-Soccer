package presentation.input;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.util.EnumSet;
import java.util.Set;

/** Captura exclusivamente el estado de las teclas físicas presionadas en la interfaz. */
public final class KeyboardInputHandler {
    private final Set<KeyCode> pressed = EnumSet.noneOf(KeyCode.class);

    /**
     * Registra una tecla como presionada.
     *
     * @param event evento de teclado recibido
     */
    public void keyPressed(KeyEvent event) { pressed.add(event.getCode()); }

    /**
     * Registra una tecla como liberada.
     *
     * @param event evento de teclado recibido
     */
    public void keyReleased(KeyEvent event) { pressed.remove(event.getCode()); }

    /**
     * Consulta si una tecla específica está actualmente presionada.
     *
     * @param code código de tecla a consultar
     * @return {@code true} si la tecla permanece presionada
     */
    public boolean isPressed(KeyCode code) { return pressed.contains(code); }

    /** Limpia todas las teclas registradas como presionadas. */
    public void clear() { pressed.clear(); }
}
