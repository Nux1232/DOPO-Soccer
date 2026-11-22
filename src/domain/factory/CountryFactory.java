package domain.factory;

import domain.model.entity.Cameroon;
import domain.model.entity.Country;
import domain.model.entity.Nigeria;
import domain.model.entity.SouthKorea;
import domain.model.entity.UnitedStates;
import domain.model.exception.DopoSoccerException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;

/** Registro extensible: los países nuevos se añaden mediante providers. */
public final class CountryFactory {
    private static final Map<String, CountryProvider> PROVIDERS = new LinkedHashMap<String, CountryProvider>();

    static {
        registerProvider(new BuiltInCountryProvider("south-korea", SouthKorea.class));
        registerProvider(new BuiltInCountryProvider("cameroon", Cameroon.class));
        registerProvider(new BuiltInCountryProvider("nigeria", Nigeria.class));
        registerProvider(new BuiltInCountryProvider("usa", UnitedStates.class));

        for (CountryProvider provider : ServiceLoader.load(CountryProvider.class)) {
            registerProvider(provider);
        }
    }

    private CountryFactory() { }

    public static synchronized void registerProvider(CountryProvider provider) {
        if (provider == null) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_COUNTRY_PROVIDER);
        }
        String id = normalize(provider.countryId());
        if (id.isEmpty()) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_COUNTRY_PROVIDER);
        }
        PROVIDERS.put(id, provider);
    }

    public static synchronized Country create(String countryId) {
        if (countryId == null) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_COUNTRY);
        }
        CountryProvider provider = PROVIDERS.get(normalize(countryId));
        if (provider == null) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_COUNTRY + " " + countryId);
        }
        Country country = provider.create();
        if (country == null) {
            throw new DopoSoccerException("El proveedor devolvió un país vacío: " + countryId);
        }
        return country;
    }

    public static synchronized String[] availableCountryIds() {
        return PROVIDERS.keySet().toArray(new String[0]);
    }

    private static String normalize(String countryId) {
        if (countryId == null) {
            return "";
        }
        return countryId.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** Provider para países incluidos en el juego; usa reflexión solo aquí. */
    private static final class BuiltInCountryProvider implements CountryProvider {
        private final String id;
        private final Class<? extends Country> countryClass;

        private BuiltInCountryProvider(String id, Class<? extends Country> countryClass) {
            this.id = id;
            this.countryClass = countryClass;
        }

        public String countryId() { return id; }

        public Country create() {
            try {
                return countryClass.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException exception) {
                throw new DopoSoccerException("No se pudo crear el país: " + id, exception);
            }
        }
    }
}
