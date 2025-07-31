// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package utilities;

import java.time.LocalDate;
import java.time.MonthDay;
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
        // Prepare variables
        LocalDate today = LocalDate.now();
        double balance = userSettings.getCurrentBalance();
        double maxBalance = userSettings.getMaxBalance();
        double carryOverLimit = userSettings.getCarryOverLimit();

        // If there is a carry over PTO expiration during this period
        LocalDate nextExpirationDate = userSettings.getNextExpirationDate();
        if (carryOverLimit > 0 && nextExpirationDate != null && !date.isBefore(nextExpirationDate)) {
            // Add accrued PTO until the next expiration date
            balance += computePTOAccrualBetweenDates(today, nextExpirationDate, entries);

            // Account for max balance
            if (maxBalance > 0) {
                balance = Math.min(balance, maxBalance);
            }

            // Apply carry over limit
            balance = Math.min(balance, carryOverLimit);

            // Add remaining accrued PTO until the specified date
            balance += computePTOAccrualBetweenDates(nextExpirationDate, date, entries);
        } else {
            // Compute projected balance
            balance += computePTOAccrualBetweenDates(today, date, entries);
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
    private double computePTOAccrualBetweenDates(LocalDate startDate, LocalDate targetDate, List<Entry<?>> entries) {
        // Prepare variables
        double accrualRate = userSettings.getAccrualRate();
        AccrualPeriod accrualPeriod = userSettings.getAccrualPeriod();

        // Compute projected balance
        double daysSinceLastAccrual = targetDate.toEpochDay() - startDate.toEpochDay();
        double accruedPto = (daysSinceLastAccrual / AccrualPeriod.getDaysInPeriod(accrualPeriod)) * accrualRate;

        // Remove hours for entries
        for (Entry<?> entry : entries) {
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
     * @param targetDate     the target date to check against
     * @return the number of overlapping days
     */
    private double overlappingDays(LocalDate entryStartDate, LocalDate entryEndDate, LocalDate targetDate) {
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

    /**
     * Computes the accrued PTO balance since the last update.
     * 
     * @param lastUpdate the last update
     * @param entries    the list of existing time off entries
     * @return the accrued PTO balance
     */
    public double computeAccruedBalance(LocalDate lastUpdate, List<Entry<?>> entries) {
        // If the last update is not before today, return the current balance
        LocalDate today = LocalDate.now();
        if (!lastUpdate.isBefore(today)) {
            return 0.0;
        }

        // Prepare variables
        double balance = userSettings.getCurrentBalance();
        double accrualRate = userSettings.getAccrualRate();
        AccrualPeriod accrualPeriod = userSettings.getAccrualPeriod();
        double maxBalance = userSettings.getMaxBalance();
        double carryOverLimit = userSettings.getCarryOverLimit();
        MonthDay expirationDate = userSettings.getExpirationDate() == null ? null : userSettings.getExpirationDate();

        // Calculate the accrued PTO based on the days between last update and today
        double daysBetween = ChronoUnit.DAYS.between(lastUpdate, today);
        double accruedPTO = (daysBetween / AccrualPeriod.getDaysInPeriod(accrualPeriod)) * accrualRate;
        balance += accruedPTO;

        // TODO: Account for passed vacation entries

        // Account for max balance
        if (maxBalance > 0) {
            balance = Math.min(balance, maxBalance);
        }

        // Account for carry over limit
        if (carryOverLimit > 0 && expirationDate != null) {
            // Calculate the next expiration date based on the current year
            LocalDate nextExpirationDate = expirationDate.atYear(lastUpdate.getYear());
            if (nextExpirationDate.isBefore(lastUpdate)) {
                // If the expiration date has passed, set it to the next year
                nextExpirationDate = nextExpirationDate.plusYears(1);
            }

            // If today is on or after the next expiration date, apply carry over limit
            if (!today.isBefore(nextExpirationDate)) {
                // TODO: Don't set, instead subtract the difference since you will accrue more
                // after the expiration date
                balance = Math.min(balance, carryOverLimit);
            }
        }

        return balance;
    }
}
