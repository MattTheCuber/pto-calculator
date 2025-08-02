
// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package utilities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;

public class EntriesHelperTest {

    private Calendar<?> calendar;
    private EntriesHelper entriesHelper;

    @Before
    public void setUp() {
        // Single day - 1/1/25
        Interval interval1 = new Interval(LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 1, 23, 59));
        Entry<?> entry1 = new Entry<>("Test 1", interval1);
        entry1.setFullDay(true);
        // Single day - 1/2/25
        Interval interval2 = new Interval(LocalDateTime.of(2025, 1, 2, 0, 0), LocalDateTime.of(2025, 1, 2, 23, 59));
        Entry<?> entry2 = new Entry<>("Test 2", interval2);
        entry2.setFullDay(true);
        // Multi day - 1/3/25 to 1/4/25
        Interval interval3 = new Interval(LocalDateTime.of(2025, 1, 3, 0, 0), LocalDateTime.of(2025, 1, 4, 23, 59));
        Entry<?> entry3 = new Entry<>("Test 3", interval3);
        entry3.setFullDay(true);

        calendar = new Calendar<>();
        calendar.addEntries(entry1, entry2, entry3);

        entriesHelper = new EntriesHelper(calendar);
    }

    @Test
    public void testGetDateEntriesAll() {
        List<Entry<?>> entries = entriesHelper.getDateEntries(LocalDate.of(2025, 1, 1));
        assert entries.size() == 3 : "Expected 3 entries, but got " + entries.size();
    }

    @Test
    public void testGetDateEntriesOnlyMultiDay() {
        List<Entry<?>> entries = entriesHelper.getDateEntries(LocalDate.of(2025, 1, 4));
        assert entries.size() == 1 : "Expected 1 entry, but got " + entries.size();
    }

    @Test
    public void testGetDateEntriesEndDate() {
        List<Entry<?>> entries = entriesHelper.getDateEntries(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2));
        assert entries.size() == 2 : "Expected 2 entries, but got " + entries.size();
    }

    @Test
    public void getFutureEntries() {
        Interval interval = new Interval(LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setFullDay(true);
        calendar.addEntries(entry);

        List<Entry<?>> entries = entriesHelper.getFutureEntries();
        assert entries.size() == 1 : "Expected 1 entry, but got " + entries.size();
    }

    @Test
    public void testGetAllEntries() {
        List<Entry<?>> entries = entriesHelper.getAllEntries();
        assert entries.size() == 3 : "Expected 3 entries, but got " + entries.size();
    }
}
