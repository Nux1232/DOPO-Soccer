package domain.factory;

import domain.model.entity.Country;
import domain.model.entity.SouthKorea;
import domain.model.entity.Cameroon;
import domain.model.entity.Nigeria;
import domain.model.entity.UnitedStates;
import domain.model.exception.DopoSoccerException;

/** Crea los países disponibles en el juego. */
public final class CountryFactory {
    private CountryFactory() {
    }

    public static Country create(String countryId) {
        if (countryId == null) {
            throw new DopoSoccerException(DopoSoccerException.INVALID_COUNTRY);
        }

        if (countryId.equalsIgnoreCase("SOUTH_KOREA") || countryId.equalsIgnoreCase("SOUTH-KOREA") || countryId.equalsIgnoreCase("KR")) {
            return new SouthKorea();
        }
        if (countryId.equalsIgnoreCase("CAMEROON") || countryId.equalsIgnoreCase("CM")) {
            return new Cameroon();
        }
        if (countryId.equalsIgnoreCase("NIGERIA") || countryId.equalsIgnoreCase("NG")) {
            return new Nigeria();
        }
        if (countryId.equalsIgnoreCase("USA") || countryId.equalsIgnoreCase("UNITED_STATES")) {
            return new UnitedStates();
        }

        throw new DopoSoccerException(DopoSoccerException.INVALID_COUNTRY + " " + countryId);
    }
}
