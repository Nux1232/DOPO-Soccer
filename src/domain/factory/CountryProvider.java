package domain.factory;
import domain.model.entity.Country;
/** Plugin contract: add a provider without editing CountryFactory. */
public interface CountryProvider {
    String countryId();
    Country create();
}
