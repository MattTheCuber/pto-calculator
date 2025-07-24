// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)
// July 23, 2025

package gui;

import java.time.LocalDate;
import java.time.LocalTime;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.CalendarView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class for the Paid Time Off Calculator GUI.
 */
public class PTOCalculatorApp extends Application {
    CalendarView calendarView;
    Calendar calendar;

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
        calendarView = new CalendarView();

        calendar = new Calendar("Time Off");
        calendar.setStyle(Style.STYLE1);

        EventHandler<CalendarEvent> handler = evt -> eventHandler(evt);
        calendar.addEventHandler(handler);

        Interval interval1 = new Interval(LocalDate.of(2025, 7, 24), LocalTime.of(0, 0), LocalDate.of(2025, 7, 25),
                LocalTime.of(23, 59));
        Entry entry1 = new Entry("Vacation", interval1);
        entry1.setFullDay(true);
        calendar.addEntry(entry1);

        CalendarSource calendarSource = new CalendarSource("Calendars");
        calendarSource.getCalendars().add(calendar);

        calendarView.getCalendarSources().add(calendarSource);
        calendarView.setDefaultCalendarProvider(control -> calendar);

        calendarView.setRequestedTime(LocalTime.now());
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowSourceTray(false);
        calendarView.setShowSourceTrayButton(false);
        calendarView.getCalendars().getFirst().setStyle(Style.STYLE2);
        calendarView.showMonthPage();

        Scene scene = new Scene(calendarView);
        primaryStage.setTitle("Paid Time Off Planning Tool");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1300);
        primaryStage.setHeight(1000);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public void startUpdateThread() {
        Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                while (true) {
                    Platform.runLater(() -> {
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

        updateTimeThread.setPriority(Thread.MIN_PRIORITY);
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();
    }

    private void eventHandler(CalendarEvent evt) {
        System.out.println("Event: " + evt.getEventType() + " - " + evt.getCalendar().getName());
    }
}
