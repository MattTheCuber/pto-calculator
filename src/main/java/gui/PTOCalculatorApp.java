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
import com.calendarfx.view.popover.EntryDetailsView;
import com.calendarfx.view.popover.EntryHeaderView;
import com.calendarfx.view.popover.EntryPopOverContentPane;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
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
    UserSettings userSettings;
    PTOCalculator ptoCalculator;
    PTODatabase ptoDatabase;

    CalendarView calendarView;
    Calendar<?> calendar;

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
        primaryStage.setScene(scene);
        primaryStage.setWidth(1300);
        primaryStage.setHeight(1000);
        primaryStage.centerOnScreen();
        primaryStage.show();
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
        if (evt.getEventType().equals(CalendarEvent.ENTRY_CALENDAR_CHANGED)) {
            if (evt.getEntry().getCalendar() != null) {
                System.out.println("Added entry " + evt.getEntry().getTitle());
            } else {
                System.out.println("Removed entry " + evt.getEntry().getTitle());
            }

            System.out.println("Number of entries: " + getAllEntries().size());
        } else if (evt.getEventType().equals(CalendarEvent.ENTRY_FULL_DAY_CHANGED)) {
            System.out.println("Entry " + evt.getEntry().getTitle() + " changed to "
                    + (evt.getEntry().isFullDay() ? "full day" : "partial day"));
        } else if (evt.getEventType().equals(CalendarEvent.ENTRY_INTERVAL_CHANGED)) {
            // TODO: Enforce configurable increments (e.g., 15 minutes or 8 hours)
            System.out.println("Entry " + evt.getEntry().getTitle() + " changed to "
                    + evt.getEntry().getInterval());
        } else if (evt.getEventType().equals(CalendarEvent.ENTRY_TITLE_CHANGED)) {
            System.out.println("Entry title changed to" + evt.getEntry().getTitle());
        }
    }

    private void onClick(MouseEvent evt) {
        if (evt.getButton().equals(MouseButton.PRIMARY)) {
            MonthView monthView = calendarView.getMonthPage().getMonthView();
            ZonedDateTime date = monthView.getZonedDateTimeAt(evt.getX(), evt.getY(), calendarView.getZoneId());
            if (date != null && !date.toLocalDate().isBefore(LocalDate.now())) {
                double balance = ptoCalculator.computeBalanceAtDate(date.toLocalDate(), getFutureEntries());
                System.out.println("Balance at start of " + date.toLocalDate() + ": " + balance);
            }
        }
    }
}
