package presentation.view.render;

public final class PlayerVisualMetrics {
    public static final double WIDTH = 56;
    public static final double HEIGHT = 80;
    public static final double TOP_OFFSET = 50;
    public static final double BOTTOM_OFFSET = 30;
    public static final double CENTER_OFFSET_Y = (BOTTOM_OFFSET - TOP_OFFSET) / 2;

    private PlayerVisualMetrics() {
    }

    public static double scaleX() {
        return WIDTH / 56;
    }

    public static double scaleY() {
        return HEIGHT / 80;
    }
}
