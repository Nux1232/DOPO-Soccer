package domain.factory;

import domain.model.entity.Country;
import domain.model.entity.CountryCatalog;

/** Crea los países disponibles en el juego. */
public final class CountryFactory {
    private CountryFactory() {
    }

    /**
     * Obtiene la instancia de país correspondiente al identificador indicado.
     *
     * @param countryId identificador único del país en el catálogo
     * @return país correspondiente al identificador
     */
    public static Country create(String countryId) {
        return CountryCatalog.get(countryId);
    }
}
