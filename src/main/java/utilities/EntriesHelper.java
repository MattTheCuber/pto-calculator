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

    /**
     * Checks if the given entry intersects with any existing entries in the
     * calendar.
     * 
     * @param entry The entry to check for intersection.
     * 
     * @return true if the entry intersects with any existing entries, false
     *         otherwise
     */
    public boolean intersects(Entry<?> entry) {
        for (Entry<?> existingEntry : getAllEntries()) {
            // Check if the existing entry is not the same as the new entry and if they
            // intersect
            if (existingEntry.getId() != entry.getId() && existingEntry.intersects(entry)) {
                return true;
            }
        }
        return false;
    }
}
