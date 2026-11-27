package domain.model.entity;

import domain.model.exception.DopoSoccerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Catálogo de selecciones cargado desde {@code resources/countries.properties}.
 * El archivo reúne identidad visual, alias y tipo/configuración del poder por país.
 */
public final class CountryCatalog {
    private static final String RESOURCE_PATH = "/resources/countries.properties";
    private static final class DefaultHolder {
        private static final CountryCatalog INSTANCE = loadDefault(PowerFactoryRegistry.defaultRegistry());
    }

    private final List<Country> countries;
    private final Map<String, Country> countriesById;

    private CountryCatalog(Properties properties, PowerFactoryRegistry powerFactories) {
        countries = loadCountries(properties, powerFactories);
        countriesById = indexCountries(properties, countries);
    }

    /** @return catálogo predeterminado del juego */
    public static CountryCatalog defaultCatalog() {
        return DefaultHolder.INSTANCE;
    }

    /**
     * Carga el catálogo incluido en recursos usando el registro de poderes indicado.
     *
     * @param powerFactories fábricas disponibles antes de procesar los países
     * @return catálogo independiente de países
     */
    public static CountryCatalog loadDefault(PowerFactoryRegistry powerFactories) {
        if (powerFactories == null) {
            throw new DopoSoccerException("El registro de fábricas de poderes es obligatorio.");
        }
        try {
            return new CountryCatalog(loadProperties(), powerFactories);
        } catch (IOException | IllegalArgumentException exception) {
            throw new DopoSoccerException("No se pudo cargar el catálogo de países.", exception);
        }
    }

    /**
     * Construye un catálogo aislado desde propiedades, útil para aplicaciones y pruebas.
     *
     * @param properties propiedades con países y sus poderes
     * @param powerFactories registro de fábricas de poderes
     * @return catálogo independiente
     */
    public static CountryCatalog fromProperties(
            Properties properties, PowerFactoryRegistry powerFactories) {
        if (properties == null || powerFactories == null) {
            throw new DopoSoccerException("Las propiedades y las fábricas del catálogo son obligatorias.");
        }
        return new CountryCatalog(properties, powerFactories);
    }

    /** @return lista inmutable de países configurados */
    public static List<Country> all() {
        return defaultCatalog().countries();
    }

    /** @return países de esta instancia inmutables */
    public List<Country> countries() {
        return countries;
    }

    /**
     * Busca un país por su ID o por un alias configurado.
     *
     * @param id identificador de país
     * @return selección encontrada
     * @throws DopoSoccerException si el ID no está registrado
     */
    public static Country get(String id) {
        return defaultCatalog().find(id);
    }

    /** Busca un país en esta instancia por ID o alias. */
    public Country find(String id) {
        if (id == null) throw new DopoSoccerException(DopoSoccerException.INVALID_COUNTRY);
        Country country = countriesById.get(normalize(id));
        if (country == null) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_COUNTRY + " " + id);
        }
        return country;
    }

    private static Properties loadProperties() throws IOException {
        var resource = CountryCatalog.class.getResourceAsStream(RESOURCE_PATH);
        if (resource == null) throw new IOException("No existe el recurso " + RESOURCE_PATH);
        Properties properties = new Properties();
        try (InputStream input = resource;
                InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private static List<Country> loadCountries(Properties properties, PowerFactoryRegistry powerFactories) {
        String configuredIds = required(properties, "countries");
        List<Country> countries = new ArrayList<>();
        for (String id : configuredIds.split(",")) {
            String countryId = id.trim();
            String prefix = "country." + countryId + ".";
            Map<String, String> powerConfiguration = new LinkedHashMap<>();
            String powerPrefix = prefix + "power.";
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith(powerPrefix) && !"type".equals(key.substring(powerPrefix.length()))) {
                    powerConfiguration.put(key.substring(powerPrefix.length()), properties.getProperty(key));
                }
            }
            Power power = powerFactories.create(
                    required(properties, powerPrefix + "type"), powerConfiguration);
            countries.add(new Country(
                    countryId,
                    required(properties, prefix + "name"),
                    required(properties, prefix + "color"),
                    required(properties, prefix + "flag"),
                    power));
        }
        if (countries.isEmpty()) throw new IllegalArgumentException("El catálogo no define países.");
        return List.copyOf(countries);
    }

    private static Map<String, Country> indexCountries(Properties properties, List<Country> countries) {
        Map<String, Country> index = new LinkedHashMap<>();
        for (Country country : countries) {
            putAlias(index, country.id(), country);
            String aliases = properties.getProperty("country." + country.id() + ".aliases", "");
            for (String alias : aliases.split(",")) {
                if (!alias.isBlank()) putAlias(index, alias, country);
            }
        }
        return Map.copyOf(index);
    }

    private static void putAlias(Map<String, Country> index, String alias, Country country) {
        Country previous = index.putIfAbsent(normalize(alias), country);
        if (previous != null && previous != country) {
            throw new IllegalArgumentException("Alias de país duplicado: " + alias);
        }
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Falta configuración obligatoria: " + key);
        }
        return value.trim();
    }

    private static String normalize(String id) {
        return id.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    }
}
