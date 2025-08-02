// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.controlsfx.control.PopOver;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl.CreateEntryParameter;
import com.calendarfx.view.DateControl.EntryContextMenuParameter;
import com.calendarfx.view.MonthView;
import com.calendarfx.view.RequestEvent;
import com.calendarfx.view.popover.EntryDetailsView;
import com.calendarfx.view.popover.EntryHeaderView;
import com.calendarfx.view.popover.EntryPopOverContentPane;

import impl.com.calendarfx.view.CalendarViewSkin;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.PTODatabase;
import model.UserSettings;
import utilities.PTOCalculator;

/**
 * Main class for the Paid Time Off Planning Tool providing the user interface.
 */
public class PTOCalculatorApp extends Application {
    private UserSettings userSettings;
    private PTOCalculator ptoCalculator;
    private PTODatabase ptoDatabase;

    private Stage primaryStage;
    private CalendarView calendarView;
    private Calendar<?> calendar;

    PopOver balancePopOver;
    Label balanceLabel;

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
        // Initialize program classes
        ptoDatabase = new PTODatabase();
        userSettings = new UserSettings();
        ptoCalculator = new PTOCalculator(userSettings);

        // Create the main calendar view
        calendarView = new CalendarView();

        // Set the current date and time for the calendar view
        calendarView.setRequestedTime(LocalTime.now());

        // Create the time off calendar
        calendar = new Calendar<>("Time Off");
        calendar.setStyle(Style.STYLE1);

        // Add the existing entries to the calendar
        loadEntries();

        // Add listeners to handle calendar events
        calendar.addEventHandler(evt -> eventHandler(evt));
        calendarView.getMonthPage().getMonthView().addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> onClick(evt));

        // Add the calendar to the calendar view
        CalendarSource calendarSource = new CalendarSource("Calendars");
        calendarSource.getCalendars().add(calendar);
        calendarView.getCalendarSources().add(calendarSource);

        // Set the default calendar to the time off calendar
        calendarView.setDefaultCalendarProvider(control -> calendar);

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

        // TODO: Make editing the date or time not close the popover immediately
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

            // Ensure the balance popup is closed
            balancePopOver.hide();

            return popUp;
        });

        // Customize the context menu for entry creation
        Callback<EntryContextMenuParameter, ContextMenu> defaultEntryContextMenuFactory = calendarView
                .getEntryContextMenuCallback();
        calendarView.setEntryContextMenuCallback(param -> {
            // Remove the calendar selection
            ContextMenu contextMenu = defaultEntryContextMenuFactory.call(param);
            contextMenu.getItems().remove(1);

            // Ensure the balance popup is closed
            balancePopOver.hide();

            return contextMenu;
        });

        // Balance popover
        balanceLabel = new Label();
        balanceLabel.getStyleClass().add("no-entries-label");
        balancePopOver = new PopOver();
        balancePopOver.getRoot().getStylesheets().add(CalendarView.class.getResource("calendar.css").toExternalForm());
        balancePopOver.getRoot().getStyleClass().add("root");
        balancePopOver.setContentNode(balanceLabel);
        balancePopOver.getStyleClass().add("date-popover");
        balancePopOver.setArrowIndent(4);
        balancePopOver.setDetachable(false);
        balancePopOver.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
        balancePopOver.setCornerRadius(4);
        balancePopOver.setHideOnEscape(true);
        balancePopOver.setAutoHide(true);

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

        // Add the settings button to the toolbar
        calendarView.addEventHandler(RequestEvent.ANY, evt -> changeView(evt));
        updateToolbar();

        // Load user settings from the database
        loadUserSettings();

        // Keep the current date and balance updated
        startUpdateThread();
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
                        // TODO: If date changes, update userSettings.currentBalance
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
     * Loads user settings from the database and computes accrued PTO.
     */
    private void loadUserSettings() {
        // Load user settings from the database
        LocalDate lastUpdate = ptoDatabase.getUserSettings(userSettings);

        // If the last update date is before today
        if (lastUpdate != null && lastUpdate.isBefore(LocalDate.now())) {
            // Compute the accrued PTO since the last update
            List<Entry<?>> entries = getDateEntries(lastUpdate);
            double newBalance = ptoCalculator.computeAccruedBalance(lastUpdate, LocalDate.now(), entries);
            // Update the current balance and user settings
            userSettings.setCurrentBalance(newBalance);
            // Update the last update date to today
            ptoDatabase.updateUserSettings(userSettings);
            // Print the accrued PTO
            double accrued = (userSettings.getCurrentBalance() - newBalance);
            System.out.println("Accrued PTO since " + lastUpdate + ": " + accrued);
        }

        // Print the loaded user settings
        System.out.println("Loaded User Settings: " + userSettings);
    }

    /**
     * Loads existing vacation entries from the database and adds them to the
     * calendar.
     */
    private void loadEntries() {
        List<Entry<?>> entries = ptoDatabase.getVacations();
        calendar.addEntries(entries);
        System.out.println("Loaded " + entries.size() + " entries from the database.");
    }

    /**
     * Gets all entries starting from a specific date.
     * 
     * @param startDate The date from which to start fetching entries.
     * @return A list of entries starting from the specified date.
     */
    private List<Entry<?>> getDateEntries(LocalDate startDate) {
        // Fetch all entries from the calendar starting from the specified date
        Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                startDate,
                LocalDate.MAX,
                calendarView.getZoneId());
        // Flatten the map values into a list and remove duplicates
        List<Entry<?>> entries = entriesMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
        return entries;
    }

    /**
     * Gets all future entries in the calendar.
     * 
     * @return A list of future entries.
     */
    private List<Entry<?>> getFutureEntries() {
        // Fetch all entries from the calendar starting from today
        Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                LocalDate.now(),
                LocalDate.MAX,
                calendarView.getZoneId());
        // Flatten the map values into a list and remove duplicates
        List<Entry<?>> entries = entriesMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
        return entries;
    }

    /**
     * Gets all entries in the calendar.
     * 
     * @return A list of all entries in the calendar.
     */
    private List<Entry<?>> getAllEntries() {
        // Fetch all entries from the calendar
        Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                // LocalDate.MIN results in 0 entries
                LocalDate.of(-99999999, 1, 1),
                LocalDate.MAX,
                calendarView.getZoneId());
        // Flatten the map values into a list and remove duplicates
        List<Entry<?>> entries = entriesMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
        return entries;
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
                // TODO: Restrict to weekdays
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
            // TODO: Enforce configurable increments (e.g., 15 minutes or 8 hours)
            System.out.println("Entry " + evt.getEntry().getTitle() + " changed to "
                    + evt.getEntry().getInterval());
        }
        // Entry title property changes
        else if (evt.getEventType().equals(CalendarEvent.ENTRY_TITLE_CHANGED)) {
            System.out.println("Entry title changed to " + evt.getEntry().getTitle());
        }

        // If the entry is invalid
        // TODO: Revert entry changes if not new
        // TODO: Make this only include entries up to the entry date
        if (evt.getEntry().getCalendar() != null && !ptoCalculator.validateEntry(evt.getEntry(), getFutureEntries())) {
            // Remove the entry from the calendar
            evt.getEntry().setCalendar(null);

            // Show an alert to the user
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Entry");
            alert.setHeaderText("Not enough PTO balance");
            alert.setContentText("You will not have enough PTO balance to take this day off!");
            alert.showAndWait();
        } else {
            // Otherwise, update the database with the current entries
            ptoDatabase.updateVacations(getAllEntries());
        }
    }

    /**
     * Handles mouse clicks on the calendar view to show the projected PTO balance.
     * 
     * @param evt The mouse event that occurred.
     */
    private void onClick(MouseEvent evt) {
        // If the left mouse button was clicked
        if (evt.getButton().equals(MouseButton.PRIMARY) && evt.getClickCount() == 1) {
            // Get the month view and the date at the clicked position
            // TODO: Make this work with every view
            MonthView monthView = calendarView.getMonthPage().getMonthView();
            ZonedDateTime date = monthView.getZonedDateTimeAt(evt.getX(), evt.getY(), calendarView.getZoneId());

            // If the date is in the future
            if (date != null && !date.toLocalDate().isBefore(LocalDate.now())) {
                // Compute the projected PTO balance at the start of the date
                double balance = ptoCalculator.computeBalanceAtDate(date.toLocalDate(), getFutureEntries());

                // Show the balance in the popover
                balanceLabel.setText(String.format("Projected PTO Balance (start of date): %.2f", balance));
                balancePopOver.show(monthView, evt.getScreenX() + 10, evt.getScreenY());
            }
        } else {
            balancePopOver.hide();
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
        if (evt instanceof ActionEvent) {
            updateToolbar();
        }
    }

    /**
     * Updates the toolbar with the current PTO balance and other relevant
     * information.
     */
    private void updateToolbar() {
        // TODO: Add current PTO balance
        // Fetch the toolbar from the calendar view
        CalendarViewSkin skin = (CalendarViewSkin) calendarView.skinProperty().get();
        BorderPane borderPane = (BorderPane) skin.getChildren().get(0);
        GridPane toolBarGridPane = (GridPane) borderPane.getTop();
        HBox leftToolBarBox = (HBox) toolBarGridPane.getChildren().get(0);

        // If the settings button already exists, do not add it again
        if (leftToolBarBox.getChildren().get(0).getId().equals("settings-button")) {
            return;
        }

        // Build the settings button
        // Reference:
        // https://github.com/dlsc-software-consulting-gmbh/CalendarFX/blob/c684652aa413abf35a05fbb880360ab5c8e7aa0f/CalendarFXView/src/main/java/impl/com/calendarfx/view/CalendarViewSkin.java
        FontIcon settingsIcon = new FontIcon(FontAwesome.COG);
        settingsIcon.getStyleClass().addAll("button-icon", "settings-button-icon");
        Button settingsButton = new Button();
        settingsButton.setId("settings-button");
        settingsButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        settingsButton.setOnAction(evt -> openSettings());
        settingsButton.setMaxHeight(Double.MAX_VALUE);
        settingsButton.setGraphic(settingsIcon);

        // Add settings button
        leftToolBarBox.getChildren().add(0, settingsButton);
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
            dialog.applyTo(userSettings);
            ptoDatabase.updateUserSettings(userSettings);
            // TODO: Refresh UI and validate entries
        }
    }
}
