// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package utilities;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;

public class EntriesHelperTest {
    private EntriesHelper entriesHelper;
    private Calendar<?> calendar;

    @Before
    public void setUp() {
        calendar = new Calendar<>();
        entriesHelper = new EntriesHelper(calendar);

        Interval interval1 = new Interval(LocalDateTime.of(2025, 1, 1, 9, 0), LocalDateTime.of(2025, 1, 1, 17, 0));
        Entry<?> entry1 = new Entry<>("Test", interval1);
        entry1.setFullDay(true);
        calendar.addEntry(entry1);

        Interval interval2 = new Interval(LocalDateTime.of(2025, 1, 2, 9, 0), LocalDateTime.of(2025, 1, 3, 17, 0));
        Entry<?> entry2 = new Entry<>("Test 2", interval2);
        entry2.setFullDay(true);
        calendar.addEntry(entry2);

        Interval interval3 = new Interval(LocalDateTime.of(2025, 1, 4, 9, 0), LocalDateTime.of(2025, 1, 5, 17, 0));
        Entry<?> entry3 = new Entry<>("Test 3", interval3);
        calendar.addEntry(entry3);
    }

    @Test
    public void testGetAllEntries() {
        assertEquals(3, entriesHelper.getAllEntries().size());
    }

    @Test
    public void testIntersects() {
        Interval intersectingInterval = new Interval(LocalDateTime.of(2025, 1, 1, 9, 0),
                LocalDateTime.of(2025, 1, 1, 17, 0));
        Entry<?> intersectingEntry = new Entry<>("Intersecting", intersectingInterval);
        assertEquals(true, entriesHelper.intersects(intersectingEntry));
    }

    @Test
    public void testNotIntersects() {
        Interval nonIntersectingInterval = new Interval(LocalDateTime.of(2025, 1, 6, 9, 0),
                LocalDateTime.of(2025, 1, 6, 17, 0));
        Entry<?> nonIntersectingEntry = new Entry<>("Non-Intersecting", nonIntersectingInterval);
        assertEquals(false, entriesHelper.intersects(nonIntersectingEntry));
    }
}
