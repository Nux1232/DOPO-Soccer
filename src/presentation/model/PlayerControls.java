package presentation.model;

/**
 * Acciones capturadas por la capa de presentación, independientes de los tipos de entrada del dominio.
 *
 * @param left desplazamiento hacia la izquierda
 * @param right desplazamiento hacia la derecha
 * @param jump solicitud de salto
 * @param kick solicitud de patada
 * @param power solicitud de activación de poder
 */
public record PlayerControls(boolean left, boolean right, boolean jump, boolean kick, boolean power) { }
