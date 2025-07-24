package utilities;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.calendarfx.model.Entry;

import model.UserSettings;

public class PTOCalculator {
    UserSettings userSettings;

    public PTOCalculator(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public double computeBalanceAtDate(LocalDate date, List<Entry<?>> entries) {
        // Prepare varables
        double balance = userSettings.getCurrentBalance();
        double accrualRate = userSettings.getAccrualRate();
        AccrualPeriod accrualPeriod = userSettings.getAccrualPeriod();

        // Compute project balance
        double daysSinceLastAccrual = date.toEpochDay() - LocalDate.now().toEpochDay();
        double accruedPto = 0.0;
        switch (accrualPeriod) {
            case DAILY:
                accruedPto = daysSinceLastAccrual * accrualRate;
                break;
            case WEEKLY:
                accruedPto = (daysSinceLastAccrual / 7) * accrualRate;
                break;
            case MONTHLY:
                accruedPto = (daysSinceLastAccrual / 30) * accrualRate;
                break;
            case YEARLY:
                accruedPto = (daysSinceLastAccrual / 365) * accrualRate;
                break;
        }
        balance += accruedPto;

        // Remove hours for entries
        for (Entry<?> entry : entries) {
            if (entry.isMultiDay()) {
                double days = overlappingDays(entry.getStartDate(), entry.getEndDate(), date);
                balance -= days * 8;
            } else if (date.isAfter(entry.getStartDate())) {
                balance -= entry.getDuration().toHours();
            }
        }

        return balance;
    }

    private double overlappingDays(LocalDate entryStartDate, LocalDate entryEndDate, LocalDate targetDate) {
        targetDate = targetDate.minusDays(1);
        LocalDate overlapStart = entryStartDate.isAfter(LocalDate.MIN) ? entryStartDate : LocalDate.MIN;
        LocalDate overlapEnd = entryEndDate.isBefore(targetDate) ? entryEndDate : targetDate;

        return !overlapStart.isAfter(overlapEnd)
                ? ChronoUnit.DAYS.between(overlapStart, overlapEnd.plusDays(1))
                : 0;
    }

    public boolean validateEntry(Entry<?> entry) {
        return true;
    }
}
