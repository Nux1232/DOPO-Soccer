package domain.factory;

import domain.model.entity.Country;
import domain.model.entity.CountryCatalog;
import domain.model.exception.DopoSoccerException;

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

    /**
     * Obtiene un país del catálogo asociado a una fábrica de partidos.
     *
     * @param countryId identificador o alias del país
     * @param catalog catálogo utilizado por la aplicación
     * @return país configurado
     */
    public static Country create(String countryId, CountryCatalog catalog) {
        if (catalog == null) {
            throw new DopoSoccerException("El catálogo de países es obligatorio.");
        }
        return catalog.find(countryId);
    }
}
