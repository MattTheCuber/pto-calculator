package utilities;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;

public class EntriesHelper {
    private final Calendar<?> calendar;

    public EntriesHelper(Calendar<?> calendar) {
        this.calendar = calendar;
    }

    /**
     * Gets all entries starting from a specific date.
     * 
     * @param startDate The date from which to start fetching entries.
     * @return A list of entries starting from the specified date.
     */
    public List<Entry<?>> getDateEntries(LocalDate startDate) {
        // Fetch all entries from the calendar starting from the specified date
        Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                startDate,
                LocalDate.MAX,
                ZoneId.systemDefault());
        // Flatten the map values into a list and remove duplicates
        List<Entry<?>> entries = entriesMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
        return entries;
    }

    /**
     * Gets all entries starting from a specific date.
     * 
     * @param startDate The date from which to start fetching entries.
     * @param endDate   The date until which to fetch entries.
     * @return A list of entries starting from the specified date.
     */
    public List<Entry<?>> getDateEntries(LocalDate startDate, LocalDate endDate) {
        // Fetch all entries from the calendar starting from the specified date
        Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                startDate,
                endDate,
                ZoneId.systemDefault());
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
    public List<Entry<?>> getFutureEntries() {
        // Fetch all entries from the calendar starting from today
        Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                LocalDate.now(),
                LocalDate.MAX,
                ZoneId.systemDefault());
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
    public List<Entry<?>> getAllEntries() {
        // Fetch all entries from the calendar
        Map<LocalDate, List<Entry<?>>> entriesMap = calendar.findEntries(
                // LocalDate.MIN results in 0 entries
                LocalDate.of(-99999999, 1, 1),
                LocalDate.MAX,
                ZoneId.systemDefault());
        // Flatten the map values into a list and remove duplicates
        List<Entry<?>> entries = entriesMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
        return entries;
    }
}
