// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package utilities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;

import model.UserSettings;

public class PTOCalculatorTest {
    private UserSettings userSettings;
    private PTOCalculator ptoCalculator;

    @Before
    public void setUp() {
        userSettings = new UserSettings();
        userSettings.setCurrentBalance(40);
        userSettings.setAccrualRate(1);
        userSettings.setAccrualPeriod(AccrualPeriod.DAILY);
        userSettings.setMaxBalance(80);
        userSettings.setCarryOverLimit(40);
        userSettings.setExpirationDate(MonthDay.of(1, 1));

        ptoCalculator = new PTOCalculator(userSettings);
    }

    @Test
    public void testOverlappingDays() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 10);
        LocalDate targetDate = LocalDate.of(2025, 1, 5);

        double overlappingDays = ptoCalculator.overlappingDays(startDate, endDate, targetDate);

        // The overlapping days should be 4 (1st, 2nd, 3rd, and 4th)
        // Note: The targetDate is exclusive, so we count up to the 4th but not the 5th.
        assert overlappingDays == 4 : "Expected 4 overlapping days, but got " + overlappingDays;
    }

    @Test
    public void testOverlappingDaysEndDateOnly() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 10);
        LocalDate targetDate = LocalDate.of(2025, 1, 2);

        double overlappingDays = ptoCalculator.overlappingDays(startDate, endDate, targetDate);

        assert overlappingDays == 1 : "Expected 1 overlapping day, but got " + overlappingDays;
    }

    @Test
    public void testOverlappingDaysWithCompleteOverlap() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 10);
        LocalDate targetDate = LocalDate.of(2025, 1, 11);

        double overlappingDays = ptoCalculator.overlappingDays(startDate, endDate, targetDate);

        assert overlappingDays == 10 : "Expected 10 overlapping days, but got " + overlappingDays;
    }

    @Test
    public void testOverlappingDaysWithNoOverlap() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 10);
        LocalDate targetDate = LocalDate.of(2025, 1, 1);

        double overlappingDays = ptoCalculator.overlappingDays(startDate, endDate, targetDate);

        assert overlappingDays == 0 : "Expected 0 overlapping days, but got " + overlappingDays;
    }

    @Test
    public void testComputeAccrualBetweenDates() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 1, 2);
        List<Entry<?>> entries = List.of();

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate, entries);

        assert accruedPto == 1 : "Expected 1 hour of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesLonger() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2026, 1, 1);
        List<Entry<?>> entries = List.of();

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate, entries);

        assert accruedPto == 365 : "Expected 365 hours of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesDifferentAccrualRate() {
        userSettings.setAccrualRate(14);
        userSettings.setAccrualPeriod(AccrualPeriod.WEEKLY);

        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 1, 2);
        List<Entry<?>> entries = List.of();

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate, entries);

        assert accruedPto == 2 : "Expected 2 hours of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesDifferentAccrualRateLonger() {
        userSettings.setAccrualRate(14);
        userSettings.setAccrualPeriod(AccrualPeriod.WEEKLY);

        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 6, 30);
        List<Entry<?>> entries = List.of();

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate, entries);

        assert accruedPto == 360 : "Expected 360 hours of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesWithASingleDayEntry() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 2, 1);
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 20, 13, 0), LocalDateTime.of(2025, 1, 20, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        List<Entry<?>> entries = List.of(entry);

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate, entries);

        assert accruedPto == 27 : "Expected 27 hours of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesWithASingleFullDayEntry() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 2, 1);
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 20, 13, 0), LocalDateTime.of(2025, 1, 20, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setFullDay(true);
        List<Entry<?>> entries = List.of(entry);

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate, entries);

        assert accruedPto == 23 : "Expected 23 hours of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesWithAMultiDayEntry() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 2, 1);
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 29, 9, 0), LocalDateTime.of(2025, 1, 30, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setFullDay(true);
        List<Entry<?>> entries = List.of(entry);

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate, entries);

        assert accruedPto == 15 : "Expected 15 hours of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesWithASingleDayEntryNegative() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 1, 2);
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 1, 13, 0), LocalDateTime.of(2025, 1, 1, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        List<Entry<?>> entries = List.of(entry);

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate, entries);

        assert accruedPto == -3 : "Expected -3 hours of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesWithASingleFullDayEntryNegative() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 1, 2);
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 1, 13, 0), LocalDateTime.of(2025, 1, 1, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setFullDay(true);
        List<Entry<?>> entries = List.of(entry);

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate, entries);

        assert accruedPto == -7 : "Expected -7 hours of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesWithAMultiDayEntryNegative() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 1, 3);
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 1, 9, 0), LocalDateTime.of(2025, 1, 2, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setFullDay(true);
        List<Entry<?>> entries = List.of(entry);

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate, entries);

        assert accruedPto == -14 : "Expected -14 hours of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccruedBalance() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 1, 2);
        List<Entry<?>> entries = List.of();

        double balance = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getCurrentBalance() + 1;
        assert balance == expectedBalance : "Expected balance to be " + expectedBalance + ", but got " + balance;
    }

    @Test
    public void testComputeAccruedBalanceMaxBalanceCapped() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 6, 30);
        List<Entry<?>> entries = List.of();

        double balance = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getMaxBalance();
        assert balance == expectedBalance : "Expected balance to be " + expectedBalance + ", but got " + balance;
    }

    @Test
    public void testComputeAccruedBalanceCarryOverCapped() {
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate targetDate = LocalDate.of(2026, 1, 10);
        List<Entry<?>> entries = List.of();

        double balance = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getCarryOverLimit() + 9;
        assert balance == expectedBalance : "Expected balance to be " + expectedBalance + ", but got " + balance;
    }

    @Test
    public void testComputeAccruedBalanceEverything() {
        LocalDate startDate = LocalDate.of(2025, 7, 1);
        LocalDate targetDate = LocalDate.of(2026, 1, 10);
        Interval interval1 = new Interval(LocalDateTime.of(2025, 7, 20, 9, 0), LocalDateTime.of(2025, 7, 20, 17, 0));
        Entry<?> entry1 = new Entry<>("Test 1", interval1);
        Interval interval2 = new Interval(LocalDateTime.of(2026, 1, 2, 9, 0), LocalDateTime.of(2026, 1, 3, 17, 0));
        Entry<?> entry2 = new Entry<>("Test 2", interval2);
        entry2.setFullDay(true);
        List<Entry<?>> entries = List.of(entry1, entry2);

        double balance = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getCarryOverLimit() + 9 - 16;
        assert balance == expectedBalance : "Expected balance to be " + expectedBalance + ", but got " + balance;
    }
}
