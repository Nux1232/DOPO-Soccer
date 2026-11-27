package domain.model.entity;

import domain.model.exception.DopoSoccerException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Catálogo único del dominio con los países disponibles y sus tiros de poder por defecto,
 * parametrizados según el Anexo 1 de la especificación.
 */
public final class CountryCatalog {
    private static final List<Country> COUNTRIES = List.of(
            new Country("south-korea", "Corea del Sur", "#CD2E3A", "kr.png",
                    new ImpactPower("south-korea", "Ko_power.gif", "#36a9ff",
                            1.5, 40, 1020, 0, PlayerStatusEffect.PUSHED, .88,
                            820, -440, .55, false)),
            new Country("cameroon", "Camerún", "#F2B705", "cm.png",
                    new ImpactPower("cameroon", "Cm_power.gif", "#ffe34a",
                            3.0, 60, 980, 0, PlayerStatusEffect.ELECTRIFIED, .84,
                            640, -300, .4, false)),
            new Country("nigeria", "Nigeria", "#008751", "ng.png",
                    new ImpactPower("nigeria", "Ng_power.gif", "#48d99a",
                            3.0, 40, 880, -200, PlayerStatusEffect.UNCONSCIOUS, .99,
                            420, -900, .75, false)),
            new Country("usa", "Estados Unidos", "#3C3B6E", "us.png",
                    new ImpactPower("usa", "usa-illusion.gif", "#c5a0ff",
                            2.5, 30, 940, -120, PlayerStatusEffect.CONFUSED, .84,
                            560, -500, .45, true))
    );

    private static final Map<String, Country> COUNTRIES_BY_ID = indexCountries();

    private CountryCatalog() { }

    /**
     * Devuelve la lista inmutable de todos los países registrados.
     *
     * @return lista de países disponibles
     */
    public static List<Country> all() {
        return COUNTRIES;
    }

    /**
     * Busca un país por identificador o alias reconocido.
     *
     * @param id identificador o código del país
     * @return país correspondiente
     * @throws DopoSoccerException si el identificador es nulo o no existe
     */
    public static Country get(String id) {
        if (id == null) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_COUNTRY);
        }
        Country country = COUNTRIES_BY_ID.get(normalize(id));
        if (country == null) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_COUNTRY + " " + id);
        }
        return country;
    }

    private static Map<String, Country> indexCountries() {
        Map<String, Country> countries = new LinkedHashMap<>();
        for (Country country : COUNTRIES) {
            countries.put(normalize(country.id()), country);
        }
        countries.put("KR", COUNTRIES.get(0));
        countries.put("CM", COUNTRIES.get(1));
        countries.put("NG", COUNTRIES.get(2));
        countries.put("SOUTH_KOREA", COUNTRIES.get(0));
        countries.put("UNITED_STATES", COUNTRIES.get(3));
        countries.put("USA", COUNTRIES.get(3));
        return Map.copyOf(countries);
    }

    private static String normalize(String id) {
        return id.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    }
}
