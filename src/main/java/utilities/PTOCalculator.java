// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package utilities;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.calendarfx.model.Entry;

import model.UserSettings;

/**
 * PTOCalculator class for calculating Paid Time Off balances and validating
 * time off entries.
 */
public class PTOCalculator {
    UserSettings userSettings;

    /**
     * Constructor to initialize PTOCalculator with user settings.
     * 
     * @param userSettings the user settings containing accrual rate and period
     */
    public PTOCalculator(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    /**
     * Computes the projected PTO balance at a given date, considering accrual and
     * existing time off entries.
     * 
     * @param date    the date to compute the balance for
     * @param entries the list of existing time off entries
     * @return the projected PTO balance at the beginning of the specified date
     */
    public double computeBalanceAtDate(LocalDate date, List<Entry<?>> entries) {
        return computeAccruedBalance(LocalDate.now(), date, entries);
    }

    public double computeAccruedBalance(LocalDate startDate, LocalDate targetDate, List<Entry<?>> entries) {
        // Prepare variables
        double balance = userSettings.getCurrentBalance();
        double maxBalance = userSettings.getMaxBalance();
        double carryOverLimit = userSettings.getCarryOverLimit();

        // If there is a carry over PTO expiration during this period
        LocalDate nextExpirationDate = userSettings.getNextExpirationDate(startDate);
        if (carryOverLimit > 0 && nextExpirationDate != null && !targetDate.isBefore(nextExpirationDate)) {
            // Add accrued PTO until the next expiration date
            balance += computeAccrualBetweenDates(startDate, nextExpirationDate, entries);

            // Account for max balance
            if (maxBalance > 0) {
                balance = Math.min(balance, maxBalance);
            }

            // Apply carry over limit
            balance = Math.min(balance, carryOverLimit);

            // Add remaining accrued PTO until the specified date
            balance += computeAccrualBetweenDates(nextExpirationDate, targetDate, entries);
        } else {
            // Compute projected balance
            balance += computeAccrualBetweenDates(startDate, targetDate, entries);
        }

        // Account for max balance
        if (maxBalance > 0) {
            balance = Math.min(balance, maxBalance);
        }

        return balance;
    }

    /**
     * Computes the projected PTO balance at a given date, considering accrual and
     * existing time off entries.
     * 
     * @param startDate  the date to start the calculation from
     * @param targetDate the date to compute the balance for
     * @param entries    the list of existing time off entries
     * @return the projected PTO balance at the beginning of the specified date
     */
    double computeAccrualBetweenDates(LocalDate startDate, LocalDate targetDate, List<Entry<?>> entries) {
        // Prepare variables
        double accrualRate = userSettings.getAccrualRate();
        AccrualPeriod accrualPeriod = userSettings.getAccrualPeriod();

        // Compute projected balance
        double daysSinceLastAccrual = targetDate.toEpochDay() - startDate.toEpochDay();
        double accruedPto = (daysSinceLastAccrual / AccrualPeriod.getDaysInPeriod(accrualPeriod)) * accrualRate;

        // Remove hours for entries
        for (Entry<?> entry : entries) {
            // Skip entries that are completely outside the range
            if (entry.getEndDate().isBefore(startDate) || entry.getStartDate().isAfter(targetDate)) {
                continue;
            }

            if (entry.isMultiDay()) {
                double days = overlappingDays(entry.getStartDate(), entry.getEndDate(), targetDate);
                accruedPto -= days * 8;
            } else if (targetDate.isAfter(entry.getStartDate())) {
                accruedPto -= entry.getDuration().toHours();
            }
        }

        return accruedPto;
    }

    /**
     * Calculates the number of overlapping days between a PTO entry and a target
     * date.
     * 
     * @param entryStartDate the start date of the PTO entry
     * @param entryEndDate   the end date of the PTO entry
     * @param targetDate     the target date to check against, exclusive
     * @return the number of overlapping days
     */
    double overlappingDays(LocalDate entryStartDate, LocalDate entryEndDate, LocalDate targetDate) {
        // Adjust targetDate to be exclusive (calculate up to the day before)
        targetDate = targetDate.minusDays(1);

        // Determine the overlap range between the entry and the target date
        LocalDate overlapStart = entryStartDate.isAfter(LocalDate.MIN) ? entryStartDate : LocalDate.MIN;
        LocalDate overlapEnd = entryEndDate.isBefore(targetDate) ? entryEndDate : targetDate;

        // If there is an overlap, return the number of overlapping days (inclusive)
        // Otherwise, return 0
        return !overlapStart.isAfter(overlapEnd)
                ? ChronoUnit.DAYS.between(overlapStart, overlapEnd.plusDays(1))
                : 0;
    }

    /**
     * Validates a new time off entry against the current balance and existing
     * entries.
     * 
     * @param entry   the time off entry to validate
     * @param entries the list of existing time off entries
     * @return true if the entry is valid, otherwise false
     */
    public boolean validateEntry(Entry<?> entry, List<Entry<?>> entries) {
        // If the entry is in the future
        if (!entry.getEndDate().isBefore(LocalDate.now())) {
            if (entry.isMultiDay()) {
                // Get the number of days in the entry
                double days = entry.getStartDate().toEpochDay() - entry.getEndDate().toEpochDay();
                // Ensure the balance is sufficient for the entire duration
                return computeBalanceAtDate(entry.getEndDate(), entries) >= days * 8;
            } else {
                // For single-day entries, check if the balance is sufficient for the entry
                // duration
                return computeBalanceAtDate(entry.getStartDate(), entries) >= entry.getDuration().toHours();
            }
        } else {
            return true;
        }
    }
}
