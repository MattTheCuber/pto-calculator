// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package gui;

import java.time.Month;
import java.time.MonthDay;
import java.time.YearMonth;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import model.UserSettings;
import utilities.AccrualPeriod;

/**
 * Dialog for configuring user settings.
 */
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
    private ComboBox<String> expirationMonthComboBox;
    private ComboBox<Integer> expirationDayComboBox;

    /**
     * Creates a new SettingsDialog with the user settings.
     * 
     * @param parent       the parent window for this dialog
     * @param userSettings the user settings to initialize the dialog with
     */
    public SettingsDialog(Window parent, UserSettings userSettings) {
        // Initialize the dialog layout
        vbox = new VBox();
        vbox.setSpacing(16);
        vbox.setPadding(new javafx.geometry.Insets(8));

        // Create controls for user settings
        createControls(userSettings);

        // Validate fields initially
        validateFields();

        // Create the scene and stage
        Scene scene = new Scene(vbox);
        stage = new Stage();
        stage.setTitle("PTO Configuration");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        stage.initOwner(parent);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(scene);
    }

    /**
     * Creates the controls for the settings dialog.
     * 
     * @param userSettings the user settings to initialize the controls with
     */
    private void createControls(UserSettings userSettings) {
        Label descriptionLabel = new Label("All numeric fields are in hours.");

        // Current Balance
        Label balanceLabel = new Label("Current Balance:");
        balanceLabel.setTooltip(new Tooltip("Your current PTO balance in hours."));
        balanceSpinner = new Spinner<>(0, 1000000, userSettings.getCurrentBalance(), 1);
        balanceSpinner.setEditable(true);
        balanceSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                balanceSpinner.getValueFactory().setValue(0.0);
            }
            validateFields();
        });
        balanceSpinner.setTooltip(new Tooltip("Your current PTO balance in hours."));

        // Group current balance label and spinner
        HBox balanceBox = new HBox(8, balanceLabel, balanceSpinner);
        balanceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Accrual Rate
        Label accrualRateLabel = new Label("Accrual Rate:");
        accrualRateLabel.setTooltip(new Tooltip("Your PTO accrual rate in hours per period."));
        accrualRateSpinner = new Spinner<>(0, 1000000, userSettings.getAccrualRate(), 0.1);
        accrualRateSpinner.setEditable(true);
        accrualRateSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                accrualRateSpinner.getValueFactory().setValue(0.0);
            }
            validateFields();
        });
        accrualRateSpinner.setTooltip(new Tooltip("The PTO accrual rate in hours."));

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
        accrualPeriodCombo.setTooltip(new Tooltip("The period for applying PTO accrual."));

        // Group accrual fields
        HBox accrualBox = new HBox(8, accrualRateLabel, accrualRateSpinner, accrualPeriodCombo);
        accrualBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Max Balance
        Label maxBalanceLabel = new Label("Max Balance:");
        maxBalanceLabel.setTooltip(new Tooltip("Your maximum PTO balance in hours."));
        maxBalanceSpinner = new Spinner<>(0, 1000000, userSettings.getMaxBalance(), 1);
        maxBalanceSpinner.setEditable(true);
        maxBalanceSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                maxBalanceSpinner.getValueFactory().setValue(0.0);
            }
            validateFields();
        });
        maxBalanceSpinner.setTooltip(new Tooltip("Your maximum PTO balance in hours."));

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
        carryOverLabel.setTooltip(
                new Tooltip("The maximum PTO hours that can be carried over each year on the specified date."));
        carryOverLabel.setMinWidth(88);
        carryOverSpinner = new Spinner<>(0, 1000000, userSettings.getCarryOverLimit(), 1);
        carryOverSpinner.setEditable(true);
        carryOverSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                carryOverSpinner.getValueFactory().setValue(0.0);
            }
            validateFields();
        });
        carryOverSpinner.setTooltip(new Tooltip("The maximum PTO hours that can be carried over."));

        // Expiration Date
        Label expirationLabel = new Label("Expiration Date:");
        expirationLabel.setTooltip(new Tooltip("The date when PTO hours expire."));
        expirationLabel.setMinWidth(84);
        MonthDay expirationDate = userSettings.getExpirationDate();
        expirationMonthComboBox = new ComboBox<>();
        expirationMonthComboBox
                .setValue(expirationDate != null ? expirationDate.getMonth().name() : Month.JANUARY.name());
        expirationMonthComboBox.setTooltip(new Tooltip("The month when PTO hours expire."));
        for (Month month : Month.values()) {
            expirationMonthComboBox.getItems().add(month.name());
        }
        expirationMonthComboBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String month) {
                return month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
            }

            @Override
            public String fromString(String string) {
                return string.toUpperCase();
            }
        });
        expirationDayComboBox = new ComboBox<>();
        expirationDayComboBox.setValue(expirationDate == null ? 1 : expirationDate.getDayOfMonth());
        expirationDayComboBox.setTooltip(new Tooltip("The day when PTO hours expire."));
        expirationMonthComboBox.setOnAction(e -> {
            int selectedIndex = expirationMonthComboBox.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                updateExpirationDays();
            }
            validateFields();
        });
        updateExpirationDays();

        // Disable carry over
        carryOverDisableCheck = new CheckBox("Disable");
        carryOverDisableCheck.setSelected(userSettings.getCarryOverLimit() == 0.0);
        carryOverSpinner.setDisable(carryOverDisableCheck.isSelected());
        expirationMonthComboBox.setDisable(carryOverDisableCheck.isSelected());
        expirationDayComboBox.setDisable(carryOverDisableCheck.isSelected());
        carryOverDisableCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            carryOverSpinner.setDisable(isSelected);
            expirationMonthComboBox.setDisable(isSelected);
            expirationDayComboBox.setDisable(isSelected);
            validateFields();
        });

        // Group carry over fields
        HBox carryOverBox = new HBox(8,
                carryOverLabel, carryOverSpinner, expirationLabel, expirationMonthComboBox, expirationDayComboBox);
        carryOverBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox carryOverContainer = new VBox(4, carryOverDisableCheck, carryOverBox);

        // Create buttons
        Button resetButton = new Button("Reset to defaults");
        resetButton.setOnAction(event -> onReset());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> onCancel());
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> onSave());

        // Group buttons with a spacer
        Region spacer = new Region();
        HBox buttonBox = new HBox(8, resetButton, spacer, saveButton, cancelButton);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.setPadding(new javafx.geometry.Insets(8, 0, 0, 0));

        // Add controls to vbox
        vbox.getChildren().addAll(
                descriptionLabel,
                balanceBox,
                accrualBox,
                maxBalanceContainer,
                carryOverContainer,
                buttonBox);
    }

    /**
     * Updates the expiration day combo box based on the selected month.
     */
    private void updateExpirationDays() {
        // Get the selected month and calculate the number of days in that month
        int monthIndex = expirationMonthComboBox.getSelectionModel().getSelectedIndex() + 1;
        YearMonth ym = YearMonth.of(2025, monthIndex); // Using 2025 to exclude leap day
        int daysInMonth = ym.lengthOfMonth();

        // Clear existing items and populate with days for the selected month
        expirationDayComboBox.getItems().clear();
        for (int i = 1; i <= daysInMonth; i++) {
            expirationDayComboBox.getItems().add(i);
        }
    }

    /**
     * Handles the reset to defaults action.
     */
    private void onReset() {
        // Reset fields to default values
        balanceSpinner.getValueFactory().setValue(0.0);
        accrualRateSpinner.getValueFactory().setValue(0.0);
        accrualPeriodCombo.setValue(AccrualPeriod.WEEKLY);
        maxBalanceDisableCheck.setSelected(true);
        carryOverDisableCheck.setSelected(true);
        maxBalanceSpinner.getValueFactory().setValue(0.0);
        carryOverSpinner.getValueFactory().setValue(0.0);
        expirationMonthComboBox.setValue(Month.JANUARY.name());
        expirationDayComboBox.setValue(1);
        validateFields();
    }

    /**
     * Handles the cancel action.
     */
    private void onCancel() {
        saved = false;
        stage.close();
    }

    /**
     * Handles the save action.
     */
    private void onSave() {
        // Validate fields before saving
        validateFields();

        // If there is a validation error, cancel and show an alert to the user
        if (validationError != null) {
            // Show an alert with the validation error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText(validationError);
            alert.showAndWait();
        } else {
            saved = true;
            stage.close();
        }
    }

    /**
     * Validates the fields in the settings dialog. Sets validationError if any
     * field is invalid.
     */
    private void validateFields() {
        // Current Balance must be >= 0
        if (balanceSpinner.getValue() == null || balanceSpinner.getValue() < 0) {
            validationError = "Current Balance must be greater than or equal to 0";
        }
        // Accrual Rate must be >= 0
        else if (accrualRateSpinner.getValue() == null || accrualRateSpinner.getValue() < 0) {
            validationError = "Accrual Rate must be greater than or equal to 0";
        }
        // Max Balance must be > 0 if enabled
        else if (!maxBalanceDisableCheck.isSelected()
                && (maxBalanceSpinner.getValue() == null || maxBalanceSpinner.getValue() <= 0)) {
            validationError = "Max Balance must be greater than 0";
        }
        // Carry Over Limit must be > 0 if enabled
        else if (!carryOverDisableCheck.isSelected()
                && (carryOverSpinner.getValue() == null || carryOverSpinner.getValue() <= 0)) {
            validationError = "Carry Over Limit must be greater than 0";
        }
        // Expiration month must be set if carry over is enabled
        else if (!carryOverDisableCheck.isSelected() && expirationMonthComboBox.getValue() == null) {
            validationError = "Expiration Month must be set";
        }
        // Expiration day must be set if carry over is enabled
        else if (!carryOverDisableCheck.isSelected() && expirationDayComboBox.getValue() == null) {
            validationError = "Expiration Day must be set";
        }
        // Reset error
        else {
            validationError = null;
        }
    }

    /**
     * Opens the settings dialog and waits for it to close.
     */
    public void open() {
        stage.showAndWait();
    }

    /**
     * Returns whether the settings were saved.
     * 
     * @return true if settings were saved, false otherwise
     */
    public boolean wasSaved() {
        return saved;
    }

    /**
     * Applies the settings from this dialog to the provided UserSettings object.
     * 
     * @param userSettings the UserSettings object to apply the settings to
     */
    public void applyTo(UserSettings userSettings) {
        userSettings.setCurrentBalance(balanceSpinner.getValue());
        userSettings.setAccrualRate(accrualRateSpinner.getValue());
        userSettings.setAccrualPeriod(accrualPeriodCombo.getValue());
        if (maxBalanceDisableCheck.isSelected()) {
            userSettings.setMaxBalance(maxBalanceSpinner.getValue());
        }
        if (carryOverDisableCheck.isSelected()) {
            userSettings.setCarryOverLimit(carryOverSpinner.getValue());
            MonthDay monthDay = MonthDay.of(
                    Month.valueOf(expirationMonthComboBox.getValue()),
                    expirationDayComboBox.getValue());
            userSettings.setExpirationDate(monthDay);
        }
    }
}
