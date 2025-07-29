package gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import model.UserSettings;
import utilities.AccrualPeriod;

public class SettingsDialog {

    private final Stage stage;
    private final VBox vbox;

    public SettingsDialog(Window parent, UserSettings userSettings) {
        vbox = new VBox();

        createControls(userSettings);

        Scene scene = new Scene(vbox);
        stage = new Stage();
        stage.setTitle("PTO Configuration");
        stage.initOwner(parent);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(scene);
    }

    private void createControls(UserSettings userSettings) {
        vbox.setSpacing(16);
        vbox.setPadding(new javafx.geometry.Insets(8));

        // Current Balance
        Label balanceLabel = new Label("Current Balance:");
        Spinner<Double> balanceSpinner = new Spinner<>(0, 1000000, userSettings.getCurrentBalance(), 0.25);
        balanceSpinner.setEditable(true);

        // Group current balance label and spinner
        HBox balanceBox = new HBox(8, balanceLabel, balanceSpinner);
        balanceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Accrual Rate
        Label accrualRateLabel = new Label("Accrual Rate:");
        Spinner<Double> accrualRateSpinner = new Spinner<>(0, 1000000, userSettings.getAccrualRate(), 0.01);
        accrualRateSpinner.setEditable(true);

        // Accrual Period
        Label accrualPeriodLabel = new Label("Accrual Period:");
        ComboBox<AccrualPeriod> accrualPeriodCombo = new ComboBox<>();
        accrualPeriodCombo.setConverter(new StringConverter<AccrualPeriod>() {
            @Override
            public String toString(AccrualPeriod period) {
                String name = period.name();
                return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            }

            @Override
            public AccrualPeriod fromString(String string) {
                return AccrualPeriod.valueOf(string.toUpperCase());
            }
        });
        accrualPeriodCombo.getItems().addAll(AccrualPeriod.values());
        accrualPeriodCombo.setValue(userSettings.getAccrualPeriod());

        // Group accrual fields
        HBox accrualBox = new HBox(8, accrualRateLabel, accrualRateSpinner, accrualPeriodLabel, accrualPeriodCombo);
        accrualBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Max Balance
        Label maxBalanceLabel = new Label("Max Balance:");
        Spinner<Double> maxBalanceSpinner = new Spinner<>(0, 1000000, userSettings.getMaxBalance(), 0.25);
        maxBalanceSpinner.setEditable(true);

        // Disable max balance
        CheckBox maxBalanceDisableCheck = new CheckBox("Disable");
        maxBalanceDisableCheck.setSelected(userSettings.getMaxBalance() == 0.0);
        maxBalanceSpinner.setDisable(maxBalanceDisableCheck.isSelected());
        maxBalanceDisableCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            maxBalanceSpinner.setDisable(isSelected);
        });

        // Group max balance fields
        HBox maxBalanceBox = new HBox(10, maxBalanceLabel, maxBalanceSpinner);
        maxBalanceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox maxBalanceContainer = new VBox(4, maxBalanceDisableCheck, maxBalanceBox);

        // Carry Over Limit
        Label carryOverLabel = new Label("Carry Over Limit:");
        Spinner<Double> carryOverSpinner = new Spinner<>(0, 1000000, userSettings.getCarryOverLimit(), 0.25);
        carryOverSpinner.setEditable(true);

        // Expiration Date
        Label expirationLabel = new Label("Expiration Date:");
        DatePicker expirationPicker = new DatePicker(userSettings.getExpirationDate());

        // Disable carry over
        CheckBox carryOverDisableCheck = new CheckBox("Disable");
        carryOverDisableCheck.setSelected(userSettings.getCarryOverLimit() == 0.0);
        carryOverSpinner.setDisable(carryOverDisableCheck.isSelected());
        expirationPicker.setDisable(carryOverDisableCheck.isSelected());
        carryOverDisableCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            carryOverSpinner.setDisable(isSelected);
            expirationPicker.setDisable(isSelected);
        });

        // Group carry over fields
        HBox carryOverBox = new HBox(8, carryOverLabel, carryOverSpinner, expirationLabel, expirationPicker);
        carryOverBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox carryOverContainer = new VBox(4, carryOverDisableCheck, carryOverBox);

        // Add controls to vbox
        vbox.getChildren().addAll(
                balanceBox,
                accrualBox,
                maxBalanceContainer,
                carryOverContainer);

        // Buttons
        Button cancelButton = new Button("Cancel");
        Button saveButton = new Button("Save");
        HBox buttonBox = new HBox(8, saveButton, cancelButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.setPadding(new javafx.geometry.Insets(8, 0, 0, 0));

        cancelButton.setOnAction(event -> stage.close());
        saveButton.setOnAction(event -> stage.close());

        vbox.getChildren().add(buttonBox);
    }

    public void open() {
        stage.showAndWait();
    }
}
