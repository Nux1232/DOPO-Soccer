package domain.model.entity;
/** Estado de carga del aura del jugador, distinto del comportamiento especial {@link Power}. */
public enum PowerState {
    /** Sin aura activa lista para disparar. */
    NONE,
    /** Aura activa lista para convertir el siguiente contacto con el balón en tiro especial. */
    AURA
}
