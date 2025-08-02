// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package gui;

import java.time.LocalDateTime;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.TimeField;
import com.calendarfx.view.popover.EntryDetailsView;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class AddEntryDialog {
    private boolean saved = false;
    Callback<Entry<?>, Boolean> validationCallback;

    private final Stage stage;
    private final VBox vbox;

    private EntryDetailsView details;
    private CheckBox fullDayCheck;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private TimeField startTimeField;
    private TimeField endTimeField;

    /**
     * Creates a new AddEntryDialog.
     * 
     * @param parent      the parent window for this dialog
     * @param dateControl the date control object for the calendar view
     */
    public AddEntryDialog(Window parent, DateControl dateControl, Callback<Entry<?>, Boolean> validationCallback) {
        this.validationCallback = validationCallback;

        // Initialize the dialog layout
        vbox = new VBox();
        vbox.setSpacing(16);
        vbox.setPadding(new javafx.geometry.Insets(8));

        // Create controls for adding an entry
        createControls(dateControl);

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
     */
    private void createControls(DateControl dateControl) {
        // Create a default entry
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withHour(9).withMinute(0);
        LocalDateTime end = now.withHour(17).withMinute(0);
        Interval interval = new Interval(start, end);
        Entry<?> entry = new Entry<>("Vacation", interval);
        entry.setFullDay(true);
        entry.setCalendar(new Calendar<>());

        // Create the EntryDetailsView to display the entry details
        details = new EntryDetailsView(entry, dateControl);
        GridPane box = (GridPane) details.getChildren().getFirst();
        box.getColumnConstraints().get(0).setPrefWidth(75);
        box.getChildren().remove(9);
        box.getChildren().remove(8);

        HBox startDateBox = (HBox) box.getChildren().get(3);
        HBox endDateBox = (HBox) box.getChildren().get(5);
        fullDayCheck = (CheckBox) box.getChildren().get(1);
        startDatePicker = (DatePicker) startDateBox.getChildren().get(0);
        endDatePicker = (DatePicker) endDateBox.getChildren().get(0);
        startTimeField = (TimeField) startDateBox.getChildren().get(1);
        endTimeField = (TimeField) endDateBox.getChildren().get(1);

        // Create buttons
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> onCancel());
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> onSave());

        // Group buttons
        HBox buttonBox = new HBox(8, saveButton, cancelButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.setPadding(new javafx.geometry.Insets(8, 0, 0, 0));

        vbox.getChildren().addAll(details, buttonBox);
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
        // If there is a validation error, cancel and show an alert to the user
        if (!validationCallback.call(getEntry())) {
            // Show an alert with the validation error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Entry");
            alert.setHeaderText("Not enough PTO balance");
            alert.setContentText("You will not have enough PTO balance to take this day off!");
            alert.showAndWait();
        } else {
            saved = true;
            stage.close();
        }
    }

    /**
     * Opens the add entry dialog and waits for it to close.
     */
    public void open() {
        stage.showAndWait();
    }

    /**
     * Returns whether the entry was saved.
     * 
     * @return true if entry was saved, false otherwise
     */
    public boolean wasSaved() {
        return saved;
    }

    public Entry<?> getEntry() {
        return details.getEntry();
    }
}
