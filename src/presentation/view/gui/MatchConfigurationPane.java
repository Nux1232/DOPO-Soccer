package presentation.view.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import presentation.model.MatchSetup;
import presentation.model.MatchTypeOption;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/** Presenta y valida los campos declarativos de la modalidad seleccionada. */
final class MatchConfigurationPane extends VBox {
    private final MatchSetup matchConfig;
    private final List<MatchTypeOption> matchTypes;
    private final VBox parameterBox = new VBox(8);
    private final Map<String, Control> parameterFields = new HashMap<>();
    private ComboBox<MatchTypeOption> selector;

    MatchConfigurationPane(MatchSetup matchConfig, List<MatchTypeOption> matchTypes) {
        this.matchConfig = matchConfig;
        this.matchTypes = List.copyOf(matchTypes);
        buildContent();
    }

    private void buildContent() {
        Label title = new Label("MODO DE JUEGO");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: white;");
        title.setMinHeight(30);
        title.setAlignment(Pos.CENTER);

        selector = new ComboBox<>();
        selector.getItems().setAll(matchTypes);
        MatchTypeOption selectedType = matchTypes.stream()
                .filter(type -> type.id().equals(matchConfig.matchType()))
                .findFirst()
                .orElse(matchTypes.isEmpty() ? null : matchTypes.get(0));
        if (selectedType != null) {
            matchConfig.matchType(selectedType.id());
            selector.setValue(selectedType);
        }
        selector.setPrefWidth(190);
        selector.setPrefHeight(38);
        selector.setConverter(new StringConverter<>() {
            @Override
            public String toString(MatchTypeOption type) {
                return type == null ? "" : type.displayName();
            }

            @Override
            public MatchTypeOption fromString(String value) {
                return matchTypes.stream()
                        .filter(type -> type.displayName().equals(value))
                        .findFirst()
                        .orElse(null);
            }
        });

        parameterBox.setAlignment(Pos.CENTER_LEFT);
        updateParameters(selectedType());
        selector.setOnAction(event -> {
            MatchTypeOption selected = selector.getValue();
            if (selected != null) {
                matchConfig.matchType(selected.id());
                matchConfig.clearParameters();
                updateParameters(selected);
            }
        });

        Label modeLabel = new Label("MODALIDAD");
        modeLabel.setStyle("-fx-text-fill: #c8d9eb; -fx-font-size: 12px; -fx-font-weight: bold;");
        VBox modeBox = new VBox(7, modeLabel, selector);
        modeBox.setAlignment(Pos.CENTER);

        HBox settingsRow = new HBox(18, modeBox, parameterBox);
        settingsRow.setAlignment(Pos.CENTER);
        VBox settingsOptions = new VBox(settingsRow);
        settingsOptions.setAlignment(Pos.CENTER);
        getChildren().setAll(title, settingsOptions);
        setSpacing(12);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(14, 20, 14, 20));
        setPrefWidth(560);
        setMaxWidth(560);
        setStyle("-fx-background-color: rgba(8,25,48,0.94);"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: rgba(255,255,255,0.18);"
                + "-fx-border-radius: 18;");
    }

    private MatchTypeOption selectedType() {
        return matchTypes.stream()
                .filter(type -> type.id().equals(matchConfig.matchType()))
                .findFirst()
                .orElse(matchTypes.isEmpty() ? null : matchTypes.get(0));
    }

    private void updateParameters(MatchTypeOption selected) {
        parameterBox.getChildren().clear();
        parameterFields.clear();
        if (selected == null) return;
        for (MatchTypeOption.Parameter parameter : selected.parameters()) {
            Label label = new Label(parameter.label());
            label.setStyle("-fx-text-fill: #c8d9eb; -fx-font-size: 12px; -fx-font-weight: bold;");
            String value = matchConfig.parameter(parameter.id(), parameter.defaultValue());
            Control control = createParameterControl(parameter, value);
            parameterFields.put(parameter.id(), control);
            matchConfig.setParameter(parameter.id(), value);
            bindParameterControl(parameter, control);
            parameterBox.getChildren().add(new VBox(5, label, control));
        }
    }

    /** Valida y aplica todos los valores visibles antes de iniciar el partido. */
    void validateAndApply() {
        MatchTypeOption selected = selector.getValue();
        if (selected == null) {
            throw new IllegalArgumentException("Selecciona una modalidad de partido.");
        }
        for (MatchTypeOption.Parameter parameter : selected.parameters()) {
            String value = parameterValue(parameter, parameterFields.get(parameter.id()));
            matchConfig.setParameter(parameter.id(), parameter.validate(value));
        }
    }

    private String parameterValue(MatchTypeOption.Parameter parameter, Control control) {
        if (control instanceof TextField field) return field.getText();
        if (control instanceof CheckBox field) return Boolean.toString(field.isSelected());
        if (control instanceof ComboBox<?> field) {
            Object value = field.getValue();
            return value == null ? null : value.toString();
        }
        throw new IllegalArgumentException("Control desconocido para " + parameter.label() + ".");
    }

    private Control createParameterControl(MatchTypeOption.Parameter parameter, String value) {
        return switch (parameter.type()) {
            case NUMBER -> {
                TextField field = new TextField(value);
                field.setTextFormatter(new TextFormatter<String>(change ->
                        change.getControlNewText().matches("\\d*") ? change : null));
                yield field;
            }
            case TEXT -> new TextField(value);
            case BOOLEAN -> {
                CheckBox field = new CheckBox();
                field.setSelected(Boolean.parseBoolean(value));
                yield field;
            }
            case SELECT -> {
                ComboBox<String> field = new ComboBox<>();
                field.getItems().setAll(parameter.options());
                field.setValue(value);
                yield field;
            }
        };
    }

    private void bindParameterControl(MatchTypeOption.Parameter parameter, Control control) {
        if (control instanceof TextField field) {
            field.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    matchConfig.setParameter(parameter.id(), parameter.validate(newValue));
                } catch (IllegalArgumentException ignored) {
                    // Se conserva el último valor válido mientras se edita el campo.
                }
            });
        } else if (control instanceof CheckBox field) {
            field.selectedProperty().addListener((observable, oldValue, selected) ->
                    matchConfig.setParameter(parameter.id(), Boolean.toString(selected)));
        } else if (control instanceof ComboBox<?> combo) {
            combo.valueProperty().addListener((observable, oldValue, selected) -> {
                if (selected != null) matchConfig.setParameter(parameter.id(), selected.toString());
            });
        }
    }

}
