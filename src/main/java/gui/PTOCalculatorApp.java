// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)
// July 23, 2025

package gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
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
import javafx.scene.control.Tooltip;
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
import utilities.AccrualPeriod;
import utilities.PTOCalculator;

/**
 * Main class for the Paid Time Off Calculator GUI.
 */
public class PTOCalculatorApp extends Application {
    private UserSettings userSettings;
    private PTOCalculator ptoCalculator;
    private PTODatabase ptoDatabase;

    private Stage primaryStage;
    private CalendarView calendarView;
    private Calendar<?> calendar;

    /**
     * Main method to run the application.
     * 
     * @param args Unused command-line arguments.
     */
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize program classes
        userSettings = new UserSettings();
        // TODO: Remove hardcoded values
        userSettings.setCurrentBalance(20);
        userSettings.setAccrualRate(2.308);
        userSettings.setAccrualPeriod(AccrualPeriod.WEEKLY);
        ptoCalculator = new PTOCalculator(userSettings);
        ptoDatabase = new PTODatabase();

        // Create the main calendar view
        calendarView = new CalendarView();

        // Set the current date and time for the calendar view
        calendarView.setRequestedTime(LocalTime.now());

        // Create the time off calendar
        calendar = new Calendar<>("Time Off");
        calendar.setStyle(Style.STYLE1);

        // Add the existing entries to the calendar
        addEntries();

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

        // TODO: Hack the search panel to always be visible with all entries shown by
        // default
        calendarView.setShowSearchResultsTray(true);
        calendarView.getSearchResultView();

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

            return popUp;
        });

        // Customize the context menu for entry creation
        Callback<EntryContextMenuParameter, ContextMenu> defaultEntryContextMenuFactory = calendarView
                .getEntryContextMenuCallback();
        calendarView.setEntryContextMenuCallback(param -> {
            // Remove the calendar selection
            ContextMenu contextMenu = defaultEntryContextMenuFactory.call(param);
            contextMenu.getItems().remove(1);
            return contextMenu;
        });

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
    }

    public void startUpdateThread() {
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

    private void addEntries() {
        Interval interval1 = new Interval(LocalDate.of(2025, 7, 24), LocalTime.of(9, 0), LocalDate.of(2025, 7, 25),
                LocalTime.of(17, 00));
        Entry<Object> entry1 = new Entry<>("Vacation", interval1);
        entry1.setFullDay(true);
        calendar.addEntry(entry1);
    }

    public List<Entry<?>> getDateEntries(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                startDate,
                endDate,
                calendarView.getZoneId());
        List<Entry<?>> entries = entriesMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
        return entries;
    }

    public List<Entry<?>> getFutureEntries() {
        Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                LocalDate.now(),
                LocalDate.MAX,
                calendarView.getZoneId());
        List<Entry<?>> entries = entriesMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
        return entries;
    }

    public List<Entry<?>> getAllEntries() {
        Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                // LocalDate.MIN results in 0 entries
                LocalDate.of(-99999999, 1, 1),
                LocalDate.MAX,
                calendarView.getZoneId());
        List<Entry<?>> entries = entriesMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
        return entries;
    }

    private void eventHandler(CalendarEvent evt) {
        // TODO: Save entries to database
        if (evt.getEventType().equals(CalendarEvent.ENTRY_CALENDAR_CHANGED)) {
            if (evt.getEntry().getCalendar() != null) {
                // TODO: Restrict to weekdays
                System.out.println("Added entry " + evt.getEntry().getTitle());
            } else {
                System.out.println("Removed entry " + evt.getEntry().getTitle());
            }

        } else if (evt.getEventType().equals(CalendarEvent.ENTRY_FULL_DAY_CHANGED)) {
            System.out.println("Entry " + evt.getEntry().getTitle() + " changed to "
                    + (evt.getEntry().isFullDay() ? "full day" : "partial day"));
        } else if (evt.getEventType().equals(CalendarEvent.ENTRY_INTERVAL_CHANGED)) {
            // TODO: Enforce configurable increments (e.g., 15 minutes or 8 hours)
            System.out.println("Entry " + evt.getEntry().getTitle() + " changed to "
                    + evt.getEntry().getInterval());
        } else if (evt.getEventType().equals(CalendarEvent.ENTRY_TITLE_CHANGED)) {
            System.out.println("Entry title changed to " + evt.getEntry().getTitle());
        }

        if (evt.getEntry().getCalendar() != null && !ptoCalculator.validateEntry(evt.getEntry(), getFutureEntries())) {
            evt.getEntry().setCalendar(null);
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Entry");
            alert.setHeaderText("Not enough PTO balance");
            alert.setContentText("You will not have enough PTO balance to take this day off!");
            alert.showAndWait();
        }

        System.out.println("Number of entries: " + getAllEntries().size());
    }

    private void onClick(MouseEvent evt) {
        if (evt.getButton().equals(MouseButton.PRIMARY)) {
            MonthView monthView = calendarView.getMonthPage().getMonthView();
            ZonedDateTime date = monthView.getZonedDateTimeAt(evt.getX(), evt.getY(), calendarView.getZoneId());
            if (date != null && !date.toLocalDate().isBefore(LocalDate.now())) {
                double balance = ptoCalculator.computeBalanceAtDate(date.toLocalDate(), getFutureEntries());

                Tooltip tooltip = new Tooltip(String.format("Projected PTO Balance: %.2f", balance));
                tooltip.setAutoHide(true);
                tooltip.show(calendarView.getScene().getWindow(), evt.getScreenX(), evt.getScreenY());
            }
        }
    }

    private void changeView(Event evt) {
        if (evt instanceof ActionEvent) {
            updateToolbar();
        }
    }

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

    private void openSettings() {
        SettingsDialog dialog = new SettingsDialog(primaryStage, userSettings);
        dialog.open();
        if (dialog.wasSaved()) {
            dialog.applyTo(userSettings);
            // TODO: Refresh UI or recalculate PTO
        }
    }
}
