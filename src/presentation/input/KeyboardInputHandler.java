package presentation.input;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.util.EnumSet;
import java.util.Set;

/** Captura exclusivamente teclas físicas de la interfaz. */
public final class KeyboardInputHandler {
    private final Set<KeyCode> pressed = EnumSet.noneOf(KeyCode.class);
    public void keyPressed(KeyEvent event) { pressed.add(event.getCode()); }
    public void keyReleased(KeyEvent event) { pressed.remove(event.getCode()); }
    public boolean isPressed(KeyCode code) { return pressed.contains(code); }
    public void clear() { pressed.clear(); }
}
