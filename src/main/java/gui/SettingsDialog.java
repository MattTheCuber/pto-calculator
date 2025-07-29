package gui;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import model.UserSettings;
import utilities.AccrualPeriod;

public class SettingsDialog {
    private boolean saved = false;
    private String validationError;

    private final Stage stage;
    private final VBox vbox;

    private Spinner<Double> balanceSpinner;
    private Spinner<Double> accrualRateSpinner;
    private ComboBox<AccrualPeriod> accrualPeriodCombo;
    private Spinner<Double> maxBalanceSpinner;
    private CheckBox maxBalanceDisableCheck;
    private Spinner<Double> carryOverSpinner;
    private CheckBox carryOverDisableCheck;
    private DatePicker expirationPicker;
    private Button saveButton;

    public SettingsDialog(Window parent, UserSettings userSettings) {
        vbox = new VBox();

        createControls(userSettings);
        validateFields();

        Scene scene = new Scene(vbox);
        stage = new Stage();
        stage.setTitle("PTO Configuration");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        stage.initOwner(parent);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(scene);
    }

    private void createControls(UserSettings userSettings) {
        vbox.setSpacing(16);
        vbox.setPadding(new javafx.geometry.Insets(8));

        // Current Balance
        Label balanceLabel = new Label("Current Balance:");
        balanceSpinner = new Spinner<>(0, 1000000, userSettings.getCurrentBalance(), 0.25);
        balanceSpinner.setEditable(true);
        // TODO: Could make this more reactive by listening to on type instead of just
        // value changes
        balanceSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                balanceSpinner.getValueFactory().setValue(0.0);
            }
            validateFields();
        });

        // Group current balance label and spinner
        HBox balanceBox = new HBox(8, balanceLabel, balanceSpinner);
        balanceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Accrual Rate
        Label accrualRateLabel = new Label("Accrual Rate:");
        accrualRateSpinner = new Spinner<>(0, 1000000, userSettings.getAccrualRate(), 0.01);
        accrualRateSpinner.setEditable(true);
        accrualRateSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                accrualRateSpinner.getValueFactory().setValue(0.0);
            }
            validateFields();
        });

        // Accrual Period
        accrualPeriodCombo = new ComboBox<>();
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
        HBox accrualBox = new HBox(8, accrualRateLabel, accrualRateSpinner, accrualPeriodCombo);
        accrualBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Max Balance
        Label maxBalanceLabel = new Label("Max Balance:");
        maxBalanceSpinner = new Spinner<>(0, 1000000, userSettings.getMaxBalance(), 0.25);
        maxBalanceSpinner.setEditable(true);
        maxBalanceSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                maxBalanceSpinner.getValueFactory().setValue(0.0);
            }
            validateFields();
        });

        // Disable max balance
        maxBalanceDisableCheck = new CheckBox("Disable");
        maxBalanceDisableCheck.setSelected(userSettings.getMaxBalance() == 0.0);
        maxBalanceSpinner.setDisable(maxBalanceDisableCheck.isSelected());
        maxBalanceDisableCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            maxBalanceSpinner.setDisable(isSelected);
            validateFields();
        });

        // Group max balance fields
        HBox maxBalanceBox = new HBox(10, maxBalanceLabel, maxBalanceSpinner);
        maxBalanceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox maxBalanceContainer = new VBox(4, maxBalanceDisableCheck, maxBalanceBox);

        // Carry Over Limit
        Label carryOverLabel = new Label("Carry Over Limit:");
        carryOverLabel.setMinWidth(88);
        carryOverSpinner = new Spinner<>(0, 1000000, userSettings.getCarryOverLimit(), 0.25);
        carryOverSpinner.setEditable(true);
        carryOverSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                carryOverSpinner.getValueFactory().setValue(0.0);
            }
            validateFields();
        });

        // Expiration Date
        Label expirationLabel = new Label("Expiration Date:");
        expirationLabel.setMinWidth(84);
        expirationPicker = new DatePicker(userSettings.getExpirationDate());
        expirationPicker.valueProperty().addListener((obs, oldVal, newVal) -> validateFields());

        // Disable carry over
        carryOverDisableCheck = new CheckBox("Disable");
        carryOverDisableCheck.setSelected(userSettings.getCarryOverLimit() == 0.0);
        carryOverSpinner.setDisable(carryOverDisableCheck.isSelected());
        expirationPicker.setDisable(carryOverDisableCheck.isSelected());
        carryOverDisableCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            carryOverSpinner.setDisable(isSelected);
            expirationPicker.setDisable(isSelected);
            validateFields();
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
        saveButton = new Button("Save");
        HBox buttonBox = new HBox(8, saveButton, cancelButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.setPadding(new javafx.geometry.Insets(8, 0, 0, 0));

        cancelButton.setOnAction(event -> {
            saved = false;
            stage.close();
        });
        saveButton.setOnAction(event -> {
            validateFields();
            if (validationError != null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Validation Error");
                alert.setHeaderText(null);
                alert.setContentText(validationError);
                alert.showAndWait();
                return;
            }
            saved = true;
            stage.close();
        });

        vbox.getChildren().add(buttonBox);
    }

    private void validateFields() {
        // Current Balance must be >= 0
        if (balanceSpinner.getValue() == null || balanceSpinner.getValue() < 0) {
            validationError = "Current Balance must be >= 0";
        }
        // Accrual Rate must be >= 0
        else if (accrualRateSpinner.getValue() == null || accrualRateSpinner.getValue() < 0) {
            validationError = "Accrual Rate must be >= 0";
        }
        // Max Balance must be > 0 if enabled
        else if (!maxBalanceDisableCheck.isSelected()) {
            if (maxBalanceSpinner.getValue() == null || maxBalanceSpinner.getValue() <= 0) {
                validationError = "Max Balance must be > 0";
            }
        }
        // Carry Over Limit must be >= 0 if enabled
        else if (!carryOverDisableCheck.isSelected()) {
            if (carryOverSpinner.getValue() == null || carryOverSpinner.getValue() < 0) {
                validationError = "Carry Over Limit must be >= 0";
            }
            // Expiration date must be set if carry over is enabled
            if (expirationPicker.getValue() == null) {
                validationError = "Expiration Date must be set";
            }
        } else {
            validationError = null; // Reset error
        }
    }

    public void open() {
        stage.showAndWait();
    }

    public boolean wasSaved() {
        return saved;
    }

    public void applyTo(UserSettings userSettings) {
        userSettings.setCurrentBalance(balanceSpinner.getValue());
        userSettings.setAccrualRate(accrualRateSpinner.getValue());
        userSettings.setAccrualPeriod(accrualPeriodCombo.getValue());
        userSettings.setMaxBalance(maxBalanceDisableCheck.isSelected() ? 0.0 : maxBalanceSpinner.getValue());
        userSettings.setCarryOverLimit(carryOverDisableCheck.isSelected() ? 0.0 : carryOverSpinner.getValue());
        userSettings.setExpirationDate(expirationPicker.getValue());
    }
}
