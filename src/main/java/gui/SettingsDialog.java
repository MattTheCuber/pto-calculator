package gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
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
        Spinner<Double> balanceSpinner = new Spinner<>(0, 10000, userSettings.getCurrentBalance(), 0.25);
        balanceSpinner.setEditable(true);

        // Accrual Rate
        Label accrualRateLabel = new Label("Accrual Rate:");
        Spinner<Double> accrualRateSpinner = new Spinner<>(0, 100, userSettings.getAccrualRate(), 0.01);
        accrualRateSpinner.setEditable(true);

        // Accrual Period
        Label accrualPeriodLabel = new Label("Accrual Period:");
        ComboBox<AccrualPeriod> accrualPeriodCombo = new ComboBox<>();
        accrualPeriodCombo.getItems().addAll(AccrualPeriod.values());
        accrualPeriodCombo.setValue(userSettings.getAccrualPeriod());

        // Group accrual fields
        HBox accrualBox = new HBox(8, accrualRateLabel, accrualRateSpinner, accrualPeriodLabel, accrualPeriodCombo);

        // Max Balance
        Label maxBalanceLabel = new Label("Max Balance:");
        Spinner<Double> maxBalanceSpinner = new Spinner<>(0, 10000, userSettings.getMaxBalance(), 0.25);
        maxBalanceSpinner.setEditable(true);

        // Carry Over Limit
        Label carryOverLabel = new Label("Carry Over Limit:");
        Spinner<Double> carryOverSpinner = new Spinner<>(0, 10000, userSettings.getCarryOverLimit(), 0.25);
        carryOverSpinner.setEditable(true);

        // Expiration Date
        Label expirationLabel = new Label("Expiration Date:");
        DatePicker expirationPicker = new DatePicker(userSettings.getExpirationDate());

        // Group carry over fields
        HBox carryOverBox = new HBox(8, carryOverLabel, carryOverSpinner, expirationLabel, expirationPicker);

        // Add controls to vbox
        vbox.getChildren().addAll(
                new HBox(8, balanceLabel, balanceSpinner),
                accrualBox,
                new HBox(8, maxBalanceLabel, maxBalanceSpinner),
                carryOverBox);

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
