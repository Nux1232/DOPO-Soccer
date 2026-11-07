package domain.model.entity;

public final class PlayerStats {
    private int speed = 5;
    private int kick = 5;
    private int jump = 5;
    private int survival = 5;
    private int points;

    public int speed() {
        return speed;
    }

    public int kick() {
        return kick;
    }

    public int jump() {
        return jump;
    }

    public int survival() {
        return survival;
    }

    public int points() {
        return points;
    }

    public void addPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("La cantidad de puntos no puede ser negativa.");
        }
        points += amount;
    }

    public void improveSpeed() {
        speed = improve(speed);
    }

    public void improveKick() {
        kick = improve(kick);
    }

    public void improveJump() {
        jump = improve(jump);
    }

    public void improveSurvival() {
        survival = improve(survival);
    }

    private int improve(int currentValue) {
        if (points == 0) {
            throw new IllegalStateException("No hay puntos disponibles para mejorar estadísticas.");
        }
        if (currentValue >= 10) {
            throw new IllegalStateException("La estadística ya está en su máximo.");
        }
        points--;
        return currentValue + 1;
    }
}
