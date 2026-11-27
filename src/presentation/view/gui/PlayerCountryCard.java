package presentation.view.gui;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import presentation.model.CountryOption;
import presentation.model.MatchSetup;

import java.util.List;

/** Selector visual de país para un participante del partido. */
final class PlayerCountryCard extends VBox {
    PlayerCountryCard(
            String heading, String selectedCountryId, boolean firstPlayer,
            MatchSetup matchConfig, List<CountryOption> countries) {
        Label playerLabel = new Label(heading);
        playerLabel.setStyle("-fx-font-size: 19px; -fx-font-weight: 900; -fx-text-fill: white;");
        Label instruction = new Label("ELIGE TU PAÍS");
        instruction.setStyle("-fx-font-size: 12px; -fx-text-fill: #c8d9eb;");

        ComboBox<CountryOption> countrySelector = new ComboBox<>();
        countrySelector.getItems().setAll(countries);
        CountryOption selectedCountry = countries.stream()
                .filter(country -> country.id().equalsIgnoreCase(selectedCountryId))
                .findFirst()
                .orElse(countries.isEmpty() ? null : countries.get(0));
        countrySelector.setValue(selectedCountry);
        if (selectedCountry != null) {
            setCountry(matchConfig, firstPlayer, selectedCountry.id());
        }
        countrySelector.setPrefWidth(285);
        countrySelector.setPrefHeight(42);
        countrySelector.setMaxWidth(Double.MAX_VALUE);
        countrySelector.setStyle("-fx-font-size: 15px; -fx-background-radius: 10;");
        ImageView flagView = new ImageView();
        flagView.setFitWidth(58);
        flagView.setFitHeight(38);
        flagView.setPreserveRatio(true);
        flagView.setSmooth(true);
        updateFlag(flagView, selectedCountry);

        countrySelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) setCountry(matchConfig, firstPlayer, newValue.id());
            updateFlag(flagView, newValue);
        });
        HBox countryChoice = new HBox(12, flagView, countrySelector);
        countryChoice.setAlignment(Pos.CENTER);
        countryChoice.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(countryChoice, javafx.scene.layout.Priority.ALWAYS);

        getChildren().setAll(playerLabel, instruction, countryChoice);
        setSpacing(16);
        setAlignment(Pos.CENTER);
        setPadding(new javafx.geometry.Insets(15));
        setPrefWidth(420);
        setMinWidth(260);
        setMaxWidth(520);
        setMinHeight(140);
        setStyle("-fx-background-color: rgba(255,255,255,0.09);"
                + "-fx-background-radius: 20;"
                + "-fx-border-color: rgba(255,255,255,0.35);"
                + "-fx-border-radius: 20;"
                + "-fx-border-width: 1.3;");
    }

    private static void setCountry(MatchSetup config, boolean firstPlayer, String id) {
        if (firstPlayer) config.firstCountry(id);
        else config.secondCountry(id);
    }

    private void updateFlag(ImageView flagView, CountryOption country) {
        if (country == null) {
            flagView.setImage(null);
            return;
        }
        var resource = getClass().getResource("/resources/Images/flags/" + country.flagAsset());
        flagView.setImage(resource == null ? null : new Image(resource.toExternalForm()));
    }
}
