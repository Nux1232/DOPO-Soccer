package presentation.model;

/** Opción de perfil de máquina desacoplada del enum interno del dominio. */
public record AIProfileOption(String id, String displayName) {
    @Override
    public String toString() {
        return displayName;
    }
}
