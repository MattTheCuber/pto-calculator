// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.controlsfx.control.PopOver;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.CalendarView.Page;
import com.calendarfx.view.DateControl.CreateEntryParameter;
import com.calendarfx.view.DateControl.EntryContextMenuParameter;
import com.calendarfx.view.MonthView;
import com.calendarfx.view.RequestEvent;
import com.calendarfx.view.popover.EntryDetailsView;
import com.calendarfx.view.popover.EntryHeaderView;
import com.calendarfx.view.popover.EntryPopOverContentPane;
import com.calendarfx.view.print.OptionsView;
import com.calendarfx.view.print.PrintView;
import com.calendarfx.view.print.SettingsView;

import impl.com.calendarfx.view.CalendarViewSkin;
import impl.com.calendarfx.view.print.OptionsViewSkin;
import impl.com.calendarfx.view.print.SettingsViewSkin;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.PTODatabase;
import model.UserSettings;
import utilities.EntriesHelper;
import utilities.PTOCalculator;

/**
 * Main class for the Paid Time Off Planning Tool providing the user interface.
 */
public class PTOCalculatorApp extends Application {
    private Stage primaryStage;
    private final CalendarView calendarView = new CalendarView(Page.MONTH, Page.YEAR);
    private final Calendar<?> calendar = new Calendar<>("Time Off");

    private final UserSettings userSettings = new UserSettings();
    private final PTOCalculator ptoCalculator = new PTOCalculator(userSettings);
    private final PTODatabase ptoDatabase = new PTODatabase();
    private final EntriesHelper entriesHelper = new EntriesHelper(calendar);

    private final Label currentBalanceLabel = new Label();
    private final PopOver projectedBalancePopOver = new PopOver();
    private final Label projectedBalanceLabel = new Label();
    private final Button settingsButton = new Button();
    private final Button addEntryButton = new Button();

    /**
     * Main method to run the application.
     * 
     * @param args Unused command-line arguments.
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Starts the application and initializes the main calendar view.
     * 
     * @param primaryStage The primary stage for the application.
     * @throws Exception If an error occurs during initialization.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        configureCalendarView();
        customizeCalendarView();
        buildExtraToolbarControls();

        // Create the main application layout
        Scene scene = new Scene(calendarView);
        primaryStage.setTitle("Paid Time Off Planning Tool");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        primaryStage.setScene(scene);
        primaryStage.setWidth(1300);
        primaryStage.setHeight(1000);
        primaryStage.centerOnScreen();
        primaryStage.show();
        this.primaryStage = primaryStage;

        calendarView.addEventHandler(RequestEvent.ANY, evt -> changeView(evt));
        updateToolbar();
        loadUserSettings();
        loadEntries();
        startUpdateThread();

        if (ptoDatabase.isFirstTimeUser()) {
            openSettings();
        }
    }

    /*
     * Configure the initial setup for the calendar view.
     */
    public void configureCalendarView() {
        // Set the current date and time for the calendar view
        calendarView.setRequestedTime(LocalTime.now());

        // Create the time off calendar
        calendar.setStyle(Style.STYLE1);

        // Add listeners to handle calendar events
        calendar.addEventHandler(evt -> eventHandler(evt));

        // Add the calendar to the calendar view
        CalendarSource calendarSource = new CalendarSource("Calendars");
        calendarSource.getCalendars().add(calendar);
        calendarView.getCalendarSources().add(calendarSource);

        // Set the default calendar to the time off calendar
        calendarView.setDefaultCalendarProvider(control -> calendar);
    }

    /*
     * Customize the calendar view and controls.
     */
    public void customizeCalendarView() {
        // Customize the calendar view and controls
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowSourceTray(false);
        calendarView.setShowSourceTrayButton(false);

        // Show the month page by default
        calendarView.showMonthPage();
        calendarView.getMonthPage().getMonthView().setShowWeekNumbers(false);
        calendarView.getMonthPage().getMonthView().setShowCurrentWeek(false);

        // Customize the default entry factory to create entries with specific
        // properties
        Callback<CreateEntryParameter, Entry<?>> defaultEntryFactory = calendarView.getEntryFactory();
        calendarView.setEntryFactory(param -> {
            // Use the default entry factory to create a new entry
            Entry<?> entry = defaultEntryFactory.call(param);
            // Customize the entry properties
            entry.setTitle("Vacation");
            entry.setInterval(LocalTime.of(9, 0), LocalTime.of(17, 0));
            entry.setFullDay(true);
            return entry;
        });

        // Customize the entry details popover content for adding/editing entries
        calendarView.setEntryDetailsPopOverContentCallback(param -> {
            // Default implementation for creating the popover content
            EntryPopOverContentPane popUp = new EntryPopOverContentPane(
                    param.getPopOver(),
                    param.getDateControl(),
                    param.getEntry());

            // Remove the location and calendar selection controls
            EntryHeaderView header = (EntryHeaderView) popUp.getHeader();
            header.getChildren().remove(2);
            header.getChildren().remove(1);

            // Remove the recurrence controls
            EntryDetailsView details = (EntryDetailsView) popUp.getPanes().getFirst().getContent();
            GridPane box = (GridPane) details.getChildren().getFirst();
            box.getChildren().remove(9);
            box.getChildren().remove(8);

            // Ensure the projected balance popup is closed
            projectedBalancePopOver.hide();

            return popUp;
        });

        // Customize the context menu for entry creation
        Callback<EntryContextMenuParameter, ContextMenu> defaultEntryContextMenuFactory = calendarView
                .getEntryContextMenuCallback();
        calendarView.setEntryContextMenuCallback(param -> {
            // Remove the calendar selection
            ContextMenu contextMenu = defaultEntryContextMenuFactory.call(param);
            contextMenu.getItems().remove(1);

            // Make the delete option show a confirmation dialog
            MenuItem deleteItem = (MenuItem) contextMenu.getItems().get(1);
            deleteItem.setOnAction(evt -> {
                // Delete the selected entry with a confirmation dialog
                confirmDeletion(FXCollections.observableSet(param.getEntryView().getEntry()));
            });

            // Ensure the projected balance popup is closed
            projectedBalancePopOver.hide();

            return contextMenu;
        });

        // Customize the month view single click popup to show the projected balance
        calendarView.getMonthPage().getMonthView().addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            // If the mouse was dragged, do not open the popup
            if (!evt.isStillSincePress()) {
                return;
            }

            // Get the month view and the date at the clicked position
            MonthView monthView = calendarView.getMonthPage().getMonthView();
            ZonedDateTime date = monthView.getZonedDateTimeAt(evt.getX(), evt.getY(), calendarView.getZoneId());

            // Open the balance popup for the clicked date
            openBalancePopup(evt, monthView, date.toLocalDate());
        });

        // Customize the year view single click popup to show the projected balance
        calendarView.setDateDetailsCallback(param -> {
            // Extract the mouse event from the parameter
            InputEvent evt = param.getInputEvent();
            if (evt instanceof MouseEvent) {
                MouseEvent mouseEvent = (MouseEvent) evt;
                // Open the balance popup for the clicked date
                openBalancePopup(mouseEvent, param.getOwner(), param.getLocalDate());
                return true;
            }
            return false;
        });

        // Catch the delete key to remove entries and show a confirmation dialog
        calendarView.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode() == KeyCode.DELETE || evt.getCode() == KeyCode.BACK_SPACE) {
                // Delete the selected entries with a confirmation dialog
                confirmDeletion(calendarView.getSelections());

                // Prevent the default delete action
                evt.consume();
            }
        });

        // Projected balance popover
        projectedBalanceLabel.getStyleClass().add("no-entries-label");
        projectedBalancePopOver.getRoot().getStylesheets()
                .add(CalendarView.class.getResource("calendar.css").toExternalForm());
        projectedBalancePopOver.getRoot().getStyleClass().add("root");
        projectedBalancePopOver.setContentNode(projectedBalanceLabel);
        projectedBalancePopOver.getStyleClass().add("date-popover");
        projectedBalancePopOver.setArrowIndent(4);
        projectedBalancePopOver.setDetachable(false);
        projectedBalancePopOver.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
        projectedBalancePopOver.setCornerRadius(4);
        projectedBalancePopOver.setHideOnEscape(true);
        projectedBalancePopOver.setAutoHide(true);
    }

    /*
     * Builds the extra toolbar controls for the calendar view.
     */
    public void buildExtraToolbarControls() {
        // Build the settings button
        // Reference:
        // https://github.com/dlsc-software-consulting-gmbh/CalendarFX/blob/c684652aa413abf35a05fbb880360ab5c8e7aa0f/CalendarFXView/src/main/java/impl/com/calendarfx/view/CalendarViewSkin.java
        FontIcon settingsIcon = new FontIcon(FontAwesome.COG);
        settingsIcon.getStyleClass().addAll("button-icon");
        settingsButton.setId("settings-button");
        settingsButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        settingsButton.setOnAction(evt -> openSettings());
        settingsButton.setMaxHeight(Double.MAX_VALUE);
        settingsButton.setGraphic(settingsIcon);
        settingsButton.setTooltip(new Tooltip("Settings"));

        // Build the add entry button
        // Reference:
        // https://github.com/dlsc-software-consulting-gmbh/CalendarFX/blob/c684652aa413abf35a05fbb880360ab5c8e7aa0f/CalendarFXView/src/main/java/impl/com/calendarfx/view/CalendarViewSkin.java
        FontIcon addEntryIcon = new FontIcon(FontAwesome.PLUS);
        addEntryIcon.getStyleClass().addAll("button-icon");
        addEntryButton.setId("add-button");
        addEntryButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        addEntryButton.setOnAction(evt -> openAddEntryDialog());
        addEntryButton.setMaxHeight(Double.MAX_VALUE);
        addEntryButton.setGraphic(addEntryIcon);
        addEntryButton.setTooltip(new Tooltip("Add Entry"));

        // Center the current balance label vertically
        currentBalanceLabel.setMaxHeight(Double.MAX_VALUE);
        currentBalanceLabel.setStyle("-fx-font-size: 14px;");
        currentBalanceLabel.setTooltip(new Tooltip("Balance is shown from the start of today"));
    }

    /**
     * Continuously updates the calendar and current balance.
     */
    private void startUpdateThread() {
        // Create a thread to update the calendar view time every 10 seconds
        Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                // Continuously update the calendar view with the current date and time
                while (true) {
                    Platform.runLater(() -> {
                        // If the date changed, accrue PTO
                        if (!calendarView.getToday().equals(LocalDate.now())) {
                            accruePto(calendarView.getToday());
                            // Update the current balance label
                            updateCurrentBalanceLabel();
                        }

                        // Update the calendar view with the current date and time
                        calendarView.setToday(LocalDate.now());
                        calendarView.setTime(LocalTime.now());
                    });

                    try {
                        // Update every 10 seconds
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        // Configure and start the thread
        updateTimeThread.setPriority(Thread.MIN_PRIORITY);
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();
    }

    /**
     * Accrues PTO since the last update date.
     * 
     * @param lastUpdate The last date when PTO was accrued.
     */
    private void accruePto(LocalDate lastUpdate) {
        // Compute the accrued PTO since the last update
        List<Entry<?>> entries = entriesHelper.getDateEntries(lastUpdate);
        double originalBalance = userSettings.getCurrentBalance();
        double newBalance = ptoCalculator.computeAccruedBalance(lastUpdate, LocalDate.now(), entries);

        // Update the current balance and user settings
        userSettings.setCurrentBalance(newBalance);

        // Update the last update date to today
        ptoDatabase.updateUserSettings(userSettings);

        // Print the accrued PTO
        double accrued = newBalance - originalBalance;
        System.out.println("Accrued PTO since " + lastUpdate + ": " + accrued);
    }

    /**
     * Loads user settings from the database and computes accrued PTO.
     */
    private void loadUserSettings() {
        // Load user settings from the database
        LocalDate lastUpdate = ptoDatabase.getUserSettings(userSettings);

        // If the last update date is before today, accrue PTO
        if (lastUpdate != null && lastUpdate.isBefore(LocalDate.now())) {
            accruePto(lastUpdate);
        }

        // Update the current balance label
        updateCurrentBalanceLabel();

        // Print the loaded user settings
        System.out.println("Loaded User Settings: " + userSettings);
    }

    /**
     * Loads existing vacation entries from the database and adds them to the
     * calendar.
     */
    private void loadEntries() {
        // Fetch all vacation entries from the database
        List<Entry<?>> entries = ptoDatabase.getVacations();

        // Add the valid entries to the calendar
        calendar.addEntries(entries);
        System.out.println("Loaded " + entries.size() + " entries from the database.");

        // Remove invalid entries from the calendar and database
        removeInvalidEntries();
    }

    /**
     * Removes invalid entries from the calendar and database.
     */
    private void removeInvalidEntries() {
        // Get all entries starting from today
        List<Entry<?>> entries = entriesHelper.getAllEntries();

        // Validate the entries and remove any invalid ones
        int invalidCount = 0;
        Iterator<Entry<?>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry<?> entry = iterator.next();
            // If the entry is invalid, remove it from the list
            if (!entry.getEndDate().isBefore(LocalDate.now())
                    && !ptoCalculator.validateEntry(entry, entriesHelper.getFutureEntries())) {
                iterator.remove();
                calendar.removeEntry(entry);
                invalidCount++;
            }
        }

        if (invalidCount > 0) {
            // Update the database to remove invalid entries
            ptoDatabase.updateVacations(entries);

            // Show a warning alert if there are invalid entries
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Invalid Entries Found");
            alert.setContentText("Found " + invalidCount
                    + " invalid entries stored in the database. They have been removed from the calendar.");
            alert.show();
        }
    }

    /**
     * Handles calendar events for when an entry changes.
     * 
     * @param evt The calendar event to handle.
     */
    private void eventHandler(CalendarEvent evt) {
        // Added or removed entries
        if (evt.getEventType().equals(CalendarEvent.ENTRY_CALENDAR_CHANGED)) {
            if (evt.getEntry().getCalendar() != null) {
                System.out.println("Added entry " + evt.getEntry().getTitle());
            } else {
                System.out.println("Removed entry " + evt.getEntry().getTitle());
            }
        }
        // Entry full day property changes
        else if (evt.getEventType().equals(CalendarEvent.ENTRY_FULL_DAY_CHANGED)) {
            System.out.println("Entry " + evt.getEntry().getTitle() + " changed to "
                    + (evt.getEntry().isFullDay() ? "full day" : "partial day"));
        }
        // Entry interval property changes
        else if (evt.getEventType().equals(CalendarEvent.ENTRY_INTERVAL_CHANGED)) {
            System.out.println("Entry " + evt.getEntry().getTitle() + " changed to "
                    + evt.getEntry().getInterval());
        }
        // Entry title property changes
        else if (evt.getEventType().equals(CalendarEvent.ENTRY_TITLE_CHANGED)) {
            System.out.println("Entry title changed to " + evt.getEntry().getTitle());
        }

        // If the entry is invalid
        List<Entry<?>> entries = entriesHelper.getDateEntries(LocalDate.now(),
                evt.getEntry().getInterval().getEndDate());

        // Check if the entry intersects with any existing entries
        boolean intersects = entriesHelper.intersects(evt.getEntry());

        if (evt.getEntry().getCalendar() != null
                && (intersects || !ptoCalculator.validateEntry(evt.getEntry(), entries))) {
            Alert alert = new Alert(Alert.AlertType.WARNING);

            // If the entry was added
            if (evt.getEventType().equals(CalendarEvent.ENTRY_CALENDAR_CHANGED)) {
                // Remove the entry from the calendar
                evt.getEntry().removeFromCalendar();
            }
            // Entry full day property changes
            else if (evt.getEventType().equals(CalendarEvent.ENTRY_FULL_DAY_CHANGED)) {
                // Remove the entry from the calendar
                evt.getEntry().removeFromCalendar();

                // Create a new entry with the old interval
                Entry<Object> entry = new Entry<>(
                        evt.getEntry().getTitle(),
                        evt.getEntry().getInterval(),
                        evt.getEntry().getId());
                entry.setFullDay(!evt.getEntry().isFullDay()); // Can't use getOldFullDay() here because it's broken
                calendar.addEntry(entry);
            }
            // Entry interval property changes
            else if (evt.getEventType().equals(CalendarEvent.ENTRY_INTERVAL_CHANGED)) {
                // Remove the entry from the calendar
                evt.getEntry().removeFromCalendar();

                // Create a new entry with the old interval
                Entry<Object> entry = new Entry<>(
                        evt.getEntry().getTitle(),
                        evt.getOldInterval(),
                        evt.getEntry().getId());
                entry.setFullDay(evt.getEntry().isFullDay());
                calendar.addEntry(entry);
            }

            // Show an alert to the user
            boolean isNew = evt.getEventType().equals(CalendarEvent.ENTRY_CALENDAR_CHANGED);
            alert.setTitle(intersects ? "Conflicting Entry" : isNew ? "Invalid Entry" : "Invalid Entry Change");
            alert.setHeaderText(intersects ? "Entry intersects with an existing entry" : "Not enough PTO balance");
            alert.setContentText(
                    intersects ? "You cannot take this day off because it conflicts with an existing entry."
                            : isNew ? "You will not have enough PTO balance to take this day off!"
                                    : "You will not have enough PTO balance to make this change!");
            alert.showAndWait();
        } else {
            // Otherwise, update the database with the current entries
            ptoDatabase.updateVacations(entriesHelper.getAllEntries());
        }
    }

    /**
     * Open the balance popup when the user clicks on a date in the calendar.
     * 
     * @param evt The mouse event that occurred.
     */
    private void openBalancePopup(MouseEvent evt, Node owner, LocalDate date) {
        // If the left mouse button was clicked
        if (evt.getButton().equals(MouseButton.PRIMARY) && evt.getClickCount() == 1) {
            // If the date is in the future
            if (date != null && !date.isBefore(LocalDate.now())) {
                // Compute the projected PTO balance at the start of the date
                double balance = ptoCalculator.computeBalanceAtDate(date, entriesHelper.getFutureEntries());

                // Show the projected balance in the popover
                projectedBalanceLabel.setText(String.format("Projected PTO balance (start of date): %.2f", balance));
                projectedBalancePopOver.show(owner, evt.getScreenX() + 10, evt.getScreenY());
            }
        } else {
            projectedBalancePopOver.hide();
        }
    }

    /**
     * Handle events that change the view, such as switching between month and week
     * views.
     * 
     * @param evt The event that triggered the view change.
     */
    private void changeView(Event evt) {
        // If the event is an action event (calendar switching), update the toolbar
        if (evt instanceof ActionEvent || evt instanceof RequestEvent) {
            projectedBalancePopOver.hide();
            updateToolbar();
        }
    }

    /**
     * Updates the toolbar with the current PTO balance and other relevant
     * information.
     */
    private void updateToolbar() {
        // Fetch the toolbar from the calendar view
        CalendarViewSkin skin = (CalendarViewSkin) calendarView.skinProperty().get();
        BorderPane borderPane = (BorderPane) skin.getChildren().get(0);
        GridPane toolBarGridPane = (GridPane) borderPane.getTop();
        HBox leftToolBarBox = (HBox) toolBarGridPane.getChildren().get(0);

        // If the settings button already exists, do not add it again
        if (leftToolBarBox.getChildren().get(0).getId().equals("settings-button")) {
            return;
        }

        // Add the settings button
        leftToolBarBox.getChildren().add(0, settingsButton);

        // Add the add entry button
        leftToolBarBox.getChildren().add(2, new Separator(Orientation.VERTICAL));
        leftToolBarBox.getChildren().add(3, addEntryButton);

        // Add the current balance label
        if (calendarView.getSelectedPage().equals(Page.YEAR)) {
            leftToolBarBox.getChildren().add(6, new Separator(Orientation.VERTICAL));
            leftToolBarBox.getChildren().add(7, currentBalanceLabel);
        } else {
            leftToolBarBox.getChildren().add(4, new Separator(Orientation.VERTICAL));
            leftToolBarBox.getChildren().add(5, currentBalanceLabel);
        }

        // Customize the print button action
        Button printButton = (Button) leftToolBarBox.getChildren().get(1);
        EventHandler<ActionEvent> printButtonHandler = printButton.getOnAction();
        printButton.setOnAction(evt -> {
            // Call the original print button handler
            printButtonHandler.handle(evt);

            // Remove the sources section from the print settings
            PrintView printView = calendarView.getPrintView();
            SettingsView settingsView = printView.getSettingsView();
            SettingsViewSkin settingsViewSkin = (SettingsViewSkin) settingsView.getSkin();
            VBox settingsViewContainer = (VBox) settingsViewSkin.getChildren().get(0);
            if (settingsViewContainer.getChildren().size() == 8) {
                settingsViewContainer.getChildren().remove(5);
                settingsViewContainer.getChildren().remove(4);

                // Disable printing calendar keys and hide the checkbox
                OptionsView optionsView = settingsView.getOptionsView();
                optionsView.setShowCalendarKeys(false);
                OptionsViewSkin optionsViewSkin = (OptionsViewSkin) optionsView.getSkin();
                VBox optionsViewContainer = (VBox) optionsViewSkin.getChildren().get(0);
                optionsViewContainer.getChildren().remove(3);
            }
        });

        // Clear focus on the button by requesting focus on something else
        calendarView.requestFocus();
    }

    /**
     * Updates the current balance label with the current PTO balance
     */
    private void updateCurrentBalanceLabel() {
        currentBalanceLabel.setText(String.format("Current PTO Balance: %.2f", userSettings.getCurrentBalance()));
    }

    /*
     * Confirms deletion of selected entries.
     */
    private void confirmDeletion(ObservableSet<Entry<?>> entries) {
        // Open a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        int count = entries.size();
        alert.setTitle(count == 1 ? "Delete Entry" : "Delete Entries");
        alert.setHeaderText("Are you sure you want to delete the selected " + (count == 1 ? "entry?" : "entries?"));
        Optional<ButtonType> result = alert.showAndWait();

        // If the user confirmed, delete the entries
        if (result.isPresent() && result.get() == ButtonType.OK) {
            calendar.removeEntries(entries);
        }
    }

    /**
     * Opens the settings dialog to allow the user to configure their PTO settings.
     */
    private void openSettings() {
        // Create and open the settings dialog
        SettingsDialog dialog = new SettingsDialog(primaryStage, userSettings);
        dialog.open();

        // If the dialog was saved, apply the changes to user settings
        if (dialog.wasSaved()) {
            // Apply the changes to user settings and update the database
            dialog.applyTo(userSettings);
            ptoDatabase.updateUserSettings(userSettings);

            // Remove all entries that are invalid with the new settings
            removeInvalidEntries();

            // Update the current balance label
            updateCurrentBalanceLabel();
        }
    }

    /**
     * Opens the add entry dialog to allow the user to add a new time off entry.
     */
    private void openAddEntryDialog() {
        // Create and open the add entry dialog
        AddEntryDialog dialog = new AddEntryDialog(
                primaryStage,
                calendarView,
                entry -> {
                    // Check if the entry intersects with any existing entries
                    boolean intersects = entriesHelper.intersects(entry);

                    // Check if the entry is valid
                    boolean isValid = ptoCalculator.validateEntry(entry, entriesHelper.getFutureEntries());

                    return intersects ? "Entry intersects with an existing entry."
                            : isValid ? null : "You will not have enough PTO balance to take this day off!";
                });
        dialog.open();

        // If the dialog was saved, update the database with the new entries
        if (dialog.wasSaved()) {
            // Add the new entry to the calendar and update the database
            dialog.getEntry().setCalendar(calendar);
            calendarView.refreshData();
        }
    }
}
