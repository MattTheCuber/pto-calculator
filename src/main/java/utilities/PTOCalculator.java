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
        double accruedPto = (daysSinceLastAccrual / AccrualPeriod.getDaysInPeriod(accrualPeriod)) * accrualRate;
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

    public boolean validateEntry(Entry<?> entry, List<Entry<?>> entries) {
        if (entry.isMultiDay()) {
            double days = entry.getStartDate().toEpochDay() - entry.getEndDate().toEpochDay();
            return computeBalanceAtDate(entry.getEndDate(), entries) >= days * 8;
        } else if (!entry.getStartDate().isBefore(LocalDate.now())) {
            return computeBalanceAtDate(entry.getStartDate(), entries) >= entry.getDuration().toHours();
        } else {
            return true;
        }
    }

    public double computeAccruedBalance(LocalDate lastUpdate, List<Entry<?>> entries) {
        LocalDate today = LocalDate.now();
        if (lastUpdate.isAfter(today)) {
            return userSettings.getCurrentBalance();
        }

        double daysBetween = ChronoUnit.DAYS.between(lastUpdate, today);
        double accrualRate = userSettings.getAccrualRate();
        AccrualPeriod accrualPeriod = userSettings.getAccrualPeriod();
        double accruedPTO = (daysBetween / AccrualPeriod.getDaysInPeriod(accrualPeriod)) * accrualRate;

        System.out.println("Accrued PTO from " + lastUpdate + " to " + today + ": " + accruedPTO);
        return userSettings.getCurrentBalance() + accruedPTO;
    }
}
