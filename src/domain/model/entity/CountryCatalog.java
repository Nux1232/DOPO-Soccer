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
 * El archivo reúne identidad visual, alias y configuración del poder por país.
 */
public final class CountryCatalog {
    private static final String RESOURCE_PATH = "/resources/countries.properties";
    private static final List<Country> COUNTRIES;
    private static final Map<String, Country> COUNTRIES_BY_ID;

    static {
        try {
            Properties properties = loadProperties();
            COUNTRIES = loadCountries(properties);
            COUNTRIES_BY_ID = indexCountries(properties, COUNTRIES);
        } catch (IOException | IllegalArgumentException exception) {
            throw new ExceptionInInitializerError(
                    new DopoSoccerException("No se pudo cargar el catálogo de países.", exception));
        }
    }

    private CountryCatalog() { }

    /** @return lista inmutable de países configurados */
    public static List<Country> all() {
        return COUNTRIES;
    }

    /**
     * Busca un país por su ID o por un alias configurado.
     *
     * @param id identificador de país
     * @return selección encontrada
     * @throws DopoSoccerException si el ID no está registrado
     */
    public static Country get(String id) {
        if (id == null) throw new DopoSoccerException(DopoSoccerException.INVALID_COUNTRY);
        Country country = COUNTRIES_BY_ID.get(normalize(id));
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

    private static List<Country> loadCountries(Properties properties) {
        String configuredIds = required(properties, "countries");
        List<Country> countries = new ArrayList<>();
        for (String id : configuredIds.split(",")) {
            String countryId = id.trim();
            String prefix = "country." + countryId + ".";
            ProjectilePower power = new ImpactPower(
                    required(properties, prefix + "power.id"),
                    required(properties, prefix + "power.asset"),
                    required(properties, prefix + "power.color"),
                    decimal(properties, prefix + "power.duration"),
                    decimal(properties, prefix + "power.damage"),
                    decimal(properties, prefix + "power.speedX"),
                    decimal(properties, prefix + "power.speedY"),
                    PlayerStatusEffect.valueOf(required(properties, prefix + "power.effect")),
                    decimal(properties, prefix + "power.restitution"),
                    decimal(properties, prefix + "power.knockbackX"),
                    decimal(properties, prefix + "power.knockbackY"),
                    decimal(properties, prefix + "power.knockbackSeconds"),
                    Boolean.parseBoolean(required(properties, prefix + "power.renderCopies")));
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

    private static double decimal(Properties properties, String key) {
        return Double.parseDouble(required(properties, key));
    }

    private static String normalize(String id) {
        return id.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    }
}
