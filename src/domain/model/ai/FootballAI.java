package domain.model.ai;

import domain.model.entity.Ball;
import domain.model.entity.Player;
import domain.model.game.PlayerInput;

import java.util.Random;

/** Coordina la toma de decisiones de la máquina delegando en la estrategia del perfil. */
public class FootballAI {
    private final Random random;

    /**
     * Crea una instancia de IA con un generador aleatorio estándar.
     */
    public FootballAI() {
        this(new Random());
    }

    /**
     * Crea una instancia de IA con un generador aleatorio específico para pruebas reproducibles.
     *
     * @param random generador pseudoaleatorio; si es {@code null} se crea uno por defecto
     */
    public FootballAI(Random random) {
        this.random = random == null ? new Random() : random;
    }

    /**
     * Calcula la acción de la máquina según el perfil indicado.
     *
     * @param profile perfil de comportamiento; si es {@code null} usa {@link AIProfile#EXPERT}
     * @param machine jugador controlado por la máquina
     * @param human jugador rival
     * @param ball balón actual del partido
     * @param machineGoals goles anotados por la máquina
     * @param humanGoals goles anotados por el rival
     * @return entrada de control generada para el cuadro actual
     */
    public PlayerInput decide(AIProfile profile, Player machine, Player human, Ball ball,
            int machineGoals, int humanGoals) {
        AIProfile effectiveProfile = profile == null ? AIProfile.EXPERT : profile;
        return effectiveProfile.decide(random, machine, human, ball, machineGoals, humanGoals);
    }
}

