package presentation.controller.input;

public record PlayerInput(boolean left, boolean right, boolean jump, boolean kick, boolean power) {
    public static final PlayerInput NONE = new PlayerInput(false, false, false, false, false);

    public double horizontalDirection() {
        return (right ? 1 : 0) - (left ? 1 : 0);
    }
}
