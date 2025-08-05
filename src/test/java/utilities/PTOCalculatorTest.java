// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package utilities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;

import model.UserSettings;

public class PTOCalculatorTest {
    private UserSettings userSettings;
    private PTOCalculator ptoCalculator;
    private Calendar<?> calendar;

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

        calendar = new Calendar<>();
    }

    // region Calculate Deduction

    @Test
    public void testCalculateDeductionFullDay() {
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 1, 13, 0), LocalDateTime.of(2025, 1, 1, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setFullDay(true);

        double deduction = ptoCalculator.calculateDeduction(entry);

        assert deduction == 8 : "Expected 8 hours of deduction, but got " + deduction;
    }

    @Test
    public void testCalculateDeductionMultiDay() {
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 2, 23, 59));
        Entry<?> entry = new Entry<>("Test", interval);

        double deduction = ptoCalculator.calculateDeduction(entry);

        assert deduction == 8 : "Expected 8 hours of deduction, but got " + deduction;
    }

    @Test
    public void testCalculateDeductionPartialDay() {
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 1, 13, 0), LocalDateTime.of(2025, 1, 1, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);

        double deduction = ptoCalculator.calculateDeduction(entry);

        assert deduction == 4 : "Expected 4 hours of deduction, but got " + deduction;
    }

    // region Compute Accrual

    @Test
    public void testComputeAccrualBetweenDates() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 1, 2);

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate);

        assert accruedPto == 1 : "Expected 1 hour of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesLonger() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2026, 1, 1);

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate);

        assert accruedPto == 365 : "Expected 365 hours of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesDifferentAccrualRate() {
        userSettings.setAccrualRate(14);
        userSettings.setAccrualPeriod(AccrualPeriod.WEEKLY);

        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 1, 2);

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate);

        assert accruedPto == 2 : "Expected 2 hours of accrued PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesDifferentAccrualRateLonger() {
        userSettings.setAccrualRate(14);
        userSettings.setAccrualPeriod(AccrualPeriod.WEEKLY);

        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 6, 30);

        double accruedPto = ptoCalculator.computeAccrualBetweenDates(startDate, targetDate);

        assert accruedPto == 360 : "Expected 360 hours of accrued PTO, but got " + accruedPto;
    }

    // region Accrue-Limit

    @Test
    public void testAccrueAndApplyLimitsMaxBalanceCapped() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 6, 30);

        double newBalance = ptoCalculator.accrueAndApplyLimits(from, to, 40, false);

        double expectedBalance = userSettings.getMaxBalance();
        assert newBalance == expectedBalance : "Expected balance to be " + expectedBalance + ", but got " + newBalance;
    }

    @Test
    public void testAccrueAndApplyLimitsCarryOverCapped() {
        LocalDate from = LocalDate.of(2025, 12, 1);
        LocalDate to = LocalDate.of(2026, 1, 1);

        double newBalance = ptoCalculator.accrueAndApplyLimits(from, to, 40, true);

        assert newBalance == userSettings.getCarryOverLimit()
                : "Expected balance to be " + userSettings.getCarryOverLimit() + ", but got " + newBalance;
    }

    // region Accrued Balance

    @Test
    public void testComputeAccruedBalance() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 1, 2);
        Map<LocalDate, List<Entry<?>>> entries = Map.of();

        double balance = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getCurrentBalance() + 1;
        assert balance == expectedBalance : "Expected balance to be " + expectedBalance + ", but got " + balance;
    }

    @Test
    public void testComputeAccruedBalanceMaxBalanceCapped() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 6, 30);
        Map<LocalDate, List<Entry<?>>> entries = Map.of();

        double balance = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getMaxBalance();
        assert balance == expectedBalance : "Expected balance to be " + expectedBalance + ", but got " + balance;
    }

    @Test
    public void testComputeAccruedBalanceCarryOverCapped() {
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate targetDate = LocalDate.of(2026, 1, 10);
        Map<LocalDate, List<Entry<?>>> entries = Map.of();

        double balance = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getCarryOverLimit() + 9;
        assert balance == expectedBalance : "Expected balance to be " + expectedBalance + ", but got " + balance;
    }

    @Test
    public void testComputeAccruedBalanceWithASingleDayEntry() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 2, 1);
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 20, 13, 0), LocalDateTime.of(2025, 1, 20, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setCalendar(calendar);
        Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(startDate, targetDate, ZoneId.systemDefault());

        double accruedPto = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getCurrentBalance() + 27;
        assert accruedPto == expectedBalance : "Expected " + expectedBalance + " hours of PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccruedBalanceWithASingleFullDayEntry() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 2, 1);
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 20, 13, 0), LocalDateTime.of(2025, 1, 20, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setFullDay(true);
        entry.setCalendar(calendar);
        Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(startDate, targetDate, ZoneId.systemDefault());

        double accruedPto = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getCurrentBalance() + 23;
        assert accruedPto == expectedBalance : "Expected " + expectedBalance + " hours of PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesWithAMultiDayEntry() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 2, 1);
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 29, 9, 0), LocalDateTime.of(2025, 1, 30, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setFullDay(true);
        entry.setCalendar(calendar);
        Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(startDate, targetDate, ZoneId.systemDefault());

        double accruedPto = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getCurrentBalance() + 15;
        assert accruedPto == expectedBalance : "Expected " + expectedBalance + " hours of PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccrualBetweenDatesWithASingleDayEntryNegative() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 1, 2);
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 1, 13, 0), LocalDateTime.of(2025, 1, 1, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setCalendar(calendar);
        Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(startDate, targetDate, ZoneId.systemDefault());

        double accruedPto = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getCurrentBalance() + -3;
        assert accruedPto == expectedBalance : "Expected " + expectedBalance + " hours of PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccruedBalanceWithASingleFullDayEntryNegative() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 1, 2);
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 1, 13, 0), LocalDateTime.of(2025, 1, 1, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setFullDay(true);
        entry.setCalendar(calendar);
        Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(startDate, targetDate, ZoneId.systemDefault());

        double accruedPto = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getCurrentBalance() + -7;
        assert accruedPto == expectedBalance : "Expected " + expectedBalance + " hours of PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccruedBalanceWithAMultiDayEntryNegative() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate targetDate = LocalDate.of(2025, 1, 3);
        Interval interval = new Interval(LocalDateTime.of(2025, 1, 1, 9, 0), LocalDateTime.of(2025, 1, 2, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setFullDay(true);
        entry.setCalendar(calendar);
        Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(startDate, targetDate, ZoneId.systemDefault());

        double accruedPto = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getCurrentBalance() + -14;
        assert accruedPto == expectedBalance : "Expected " + expectedBalance + " hours of PTO, but got " + accruedPto;
    }

    @Test
    public void testComputeAccruedBalanceCapSameDayAsEntry() {
        LocalDate startDate = LocalDate.of(2025, 1, 1); // 31 days
        LocalDate targetDate = LocalDate.of(2025, 2, 11); // 28 days, should reach cap on 2/9/2025
        Interval interval = new Interval(LocalDateTime.of(2025, 2, 10, 9, 0), LocalDateTime.of(2025, 2, 10, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setFullDay(true);
        entry.setCalendar(calendar);
        Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(startDate, targetDate, ZoneId.systemDefault());

        double balance = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        assert balance == 73 : "Expected balance to be 73, but got " + balance;
    }

    @Test
    public void testComputeAccruedBalanceCarryOverExpirationSameDayAsEntry() {
        LocalDate startDate = LocalDate.of(2025, 12, 30);
        LocalDate targetDate = LocalDate.of(2026, 1, 2);
        Interval interval = new Interval(LocalDateTime.of(2026, 1, 1, 9, 0), LocalDateTime.of(2026, 1, 1, 17, 0));
        Entry<?> entry = new Entry<>("Test", interval);
        entry.setFullDay(true);
        entry.setCalendar(calendar);
        Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(startDate, targetDate, ZoneId.systemDefault());

        double balance = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        assert balance == 33 : "Expected balance to be 33, but got " + balance;
    }

    @Test
    public void testComputeAccruedBalanceEverything() {
        LocalDate startDate = LocalDate.of(2025, 7, 1);
        LocalDate targetDate = LocalDate.of(2026, 1, 10);
        Interval interval1 = new Interval(LocalDateTime.of(2025, 7, 20, 9, 0), LocalDateTime.of(2025, 7, 20, 17, 0));
        Entry<?> entry1 = new Entry<>("Test 1", interval1);
        entry1.setCalendar(calendar);
        Interval interval2 = new Interval(LocalDateTime.of(2026, 1, 2, 9, 0), LocalDateTime.of(2026, 1, 3, 17, 0));
        Entry<?> entry2 = new Entry<>("Test 2", interval2);
        entry2.setFullDay(true);
        entry2.setCalendar(calendar);
        Map<LocalDate, List<Entry<?>>> entries = calendar.findEntries(startDate, targetDate, ZoneId.systemDefault());

        double balance = ptoCalculator.computeAccruedBalance(startDate, targetDate, entries);

        double expectedBalance = userSettings.getCarryOverLimit() + 9 - 16;
        assert balance == expectedBalance : "Expected balance to be " + expectedBalance + ", but got " + balance;
    }
}
