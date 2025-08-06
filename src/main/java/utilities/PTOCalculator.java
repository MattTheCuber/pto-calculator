// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package utilities;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
     * Computes the projected PTO balance at a given date, considering accrual
     * configuration and existing time off entries.
     * 
     * @param date    the date to compute the balance for
     * @param entries the set of existing time off entries
     * @return the projected PTO balance at the beginning of the specified date
     */
    public double computeBalanceAtDate(LocalDate date, Map<LocalDate, List<Entry<?>>> entries) {
        return computeAccruedBalance(LocalDate.now(), date, entries);
    }

    /**
     * Computes the projected PTO balance with accrual between two dates,
     * considering accrual configuration and existing time off entries.
     * 
     * @param startDate  the start date to compute the accrual from
     * @param targetDate the end date to compute the accrual to
     * @param entries    the set of existing time off entries
     * @return the projected PTO balance with accrual between the specified dates
     */
    public double computeAccruedBalance(LocalDate startDate, LocalDate targetDate,
            Map<LocalDate, List<Entry<?>>> entries) {
        // Prepare variables
        double balance = userSettings.getCurrentBalance();
        LocalDate nextExpirationDate = userSettings.getNextExpirationDate(startDate);
        LocalDate currentDate = startDate;

        // For each date in the range of entries (sorted using TreeMap)
        TreeMap<LocalDate, List<Entry<?>>> sortedEntries = new TreeMap<>(entries);
        for (Map.Entry<LocalDate, List<Entry<?>>> currentEntries : sortedEntries.entrySet()) {
            // Get the next entry date
            LocalDate nextEntryDate = currentEntries.getKey();

            // If the next entry date is before the start date, skip it
            if (nextEntryDate.isBefore(startDate)) {
                continue;
            }
            // If the next entry date is on or after the target date, break the loop
            // On or after since PTO is removed at the end of the day and wouldn't affect
            // the balance until the next day
            else if (!nextEntryDate.isBefore(targetDate)) {
                break;
            }

            // If the carry over limit is applied before the next entry usage
            if (userSettings.isCarryOverEnabled() && !nextExpirationDate.isAfter(nextEntryDate)) {
                // Compute accrual until the next expiration date
                balance = accrueAndApplyLimits(currentDate, nextExpirationDate, balance, true);

                // Update the current date and next expiration date
                currentDate = nextExpirationDate;
                nextExpirationDate = userSettings.getNextExpirationDate(currentDate);
            }

            // Compute accrual until the current date
            balance = accrueAndApplyLimits(currentDate, nextEntryDate, balance, false);

            // If it is not a weekend
            if (nextEntryDate.getDayOfWeek().getValue() < 6) {
                // For each entry on the current date, deduct the PTO used
                for (Entry<?> entry : currentEntries.getValue()) {
                    balance -= calculateDeduction(entry);
                }
            }

            // Update the current date
            currentDate = nextEntryDate;

            // Account for max balance
            if (userSettings.isMaxBalanceEnabled()) {
                balance = Math.min(balance, userSettings.getMaxBalance());
            }
        }

        // If the carry over limit is applied before the target date
        if (userSettings.isCarryOverEnabled() && !nextExpirationDate.isAfter(targetDate)) {
            // Compute accrual until the next expiration date and apply limits
            balance = accrueAndApplyLimits(currentDate, nextExpirationDate, balance, true);

            // Update the current date
            currentDate = nextExpirationDate;
        }

        // Compute accrual until the target date
        balance = accrueAndApplyLimits(currentDate, targetDate, balance, false);

        return balance;
    }

    /**
     * Accrues PTO between two dates, then applies the max balance limit and,
     * optionally, the carry over limit.
     * 
     * @param from                the start date for accrual
     * @param to                  the end date for accrual
     * @param currentBalance      the current PTO balance
     * @param applyCarryOverLimit whether to apply the carry over limit
     * @return the new PTO balance after accrual and limits
     */
    double accrueAndApplyLimits(LocalDate from, LocalDate to, double currentBalance,
            boolean applyCarryOverLimit) {
        double accrued = computeAccrualBetweenDates(from, to);
        double newBalance = currentBalance + accrued;

        // Apply max balance
        if (userSettings.isMaxBalanceEnabled()) {
            newBalance = Math.min(newBalance, userSettings.getMaxBalance());
        }

        // Apply carry over limit
        if (applyCarryOverLimit) {
            newBalance = Math.min(newBalance, userSettings.getCarryOverLimit());
        }

        return newBalance;
    }

    /**
     * Calculates the PTO deduction for a given time off entry.
     */
    double calculateDeduction(Entry<?> entry) {
        // If the entry is a full day or multi-day event, deduct 8 hours
        if (entry.isFullDay() || entry.isMultiDay()) {
            return 8;
        }
        // Otherwise, deduct the actual duration in hours, capped at 8 hours
        return Math.min(entry.getDuration().toHours(), 8);
    }

    /**
     * Computes the projected PTO accrual between two dates.
     * 
     * @param startDate  the date to start the calculation from
     * @param targetDate the date to compute the balance for
     * @return the projected PTO balance at the beginning of the specified date
     */
    double computeAccrualBetweenDates(LocalDate startDate, LocalDate targetDate) {
        // Prepare variables
        double accrualRate = userSettings.getAccrualRate();
        AccrualPeriod accrualPeriod = userSettings.getAccrualPeriod();

        // Compute projected balance
        double daysSinceLastAccrual = targetDate.toEpochDay() - startDate.toEpochDay();
        double accruedPto = (daysSinceLastAccrual / AccrualPeriod.getDaysInPeriod(accrualPeriod)) * accrualRate;

        return accruedPto;
    }

    /**
     * Validates a new time off entry against the current balance and existing
     * entries.
     * 
     * @param entry   the time off entry to validate
     * @param entries the set of existing time off entries
     * @return true if the entry is valid, otherwise false
     */
    public boolean validateEntry(Entry<?> entry, Map<LocalDate, List<Entry<?>>> entries) {
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
                double hours = entry.isFullDay() ? 8 : entry.getDuration().toHours();
                return computeBalanceAtDate(entry.getStartDate(), entries) >= hours;
            }
        } else {
            return true;
        }
    }
}
