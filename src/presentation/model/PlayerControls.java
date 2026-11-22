package presentation.model;

/** Actions captured by the presentation layer, independent from domain input types. */
public record PlayerControls(boolean left, boolean right, boolean jump, boolean kick, boolean power) { }
