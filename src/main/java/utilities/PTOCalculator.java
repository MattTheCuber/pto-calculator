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
     * @param startDate the start date to compute the accrual from
     * @param endDate   the end date to compute the accrual to
     * @param entries   the set of existing time off entries
     * @return the projected PTO balance with accrual between the specified dates
     */
    public double computeAccruedBalance(LocalDate startDate, LocalDate targetDate,
            Map<LocalDate, List<Entry<?>>> entries) {
        // Prepare variables
        double balance = userSettings.getCurrentBalance();
        double maxBalance = userSettings.getMaxBalance();
        double carryOverLimit = userSettings.getCarryOverLimit();
        LocalDate nextExpirationDate = userSettings.getNextExpirationDate(startDate);
        LocalDate currentDate = startDate;

        System.out.println("Starting date: " + startDate + ", Target date: " + targetDate);
        System.out.println("Starting balance: " + balance);

        System.out.println("START ENTRIES");

        // For each date in the range of entries (sorted using TreeMap)
        for (Map.Entry<LocalDate, List<Entry<?>>> currentEntries : new TreeMap<>(entries).entrySet()) {
            // Get the next entry date
            LocalDate nextEntryDate = currentEntries.getKey();

            System.out.println("Processing entries on " + nextEntryDate + " with a current date of " + currentDate
                    + ". Current balance: " + balance);

            // If the next entry date is before the start date, skip it
            if (nextEntryDate.isBefore(startDate)) {
                System.out.println("Skipping entry before start date: " + nextEntryDate);

                continue;
            }
            // If the next entry date is on or after the target date, break the loop
            // On or after since PTO is removed at the end of the day and wouldn't affect
            // the balance until the next day
            else if (!nextEntryDate.isBefore(targetDate)) {
                System.out.println("Reached target date, stopping processing entries: " + nextEntryDate);

                break;
            }

            // If the carry over limit is applied before the next entry usage
            if (nextExpirationDate != null && !nextExpirationDate.isAfter(nextEntryDate)) {
                System.out.println("Applying carry over limit before next entry usage due to expiration date: "
                        + nextExpirationDate);

                // Compute accrual until the next expiration date
                balance += computeAccrualBetweenDates(currentDate, nextExpirationDate);

                System.out.println("Accrued PTO until next expiration date: " + nextExpirationDate + " is "
                        + computeAccrualBetweenDates(currentDate, nextExpirationDate) + ". New balance: " + balance);

                // Account for max balance
                if (maxBalance > 0) {
                    balance = Math.min(balance, maxBalance);
                }

                System.out.println("Accounted for max balance. New balance: " + balance);

                // Apply carry over limit
                balance = Math.min(balance, carryOverLimit);

                System.out.println("Applied carry over limit. New balance: " + balance);

                // Update the current date and next expiration date
                currentDate = nextExpirationDate;
                nextExpirationDate = userSettings.getNextExpirationDate(currentDate);

                System.out.println("Current date updated to: " + currentDate);
                System.out.println("Next expiration date updated to: " + nextExpirationDate);
            }

            // Compute accrual until the current date
            balance += computeAccrualBetweenDates(currentDate, nextEntryDate);

            System.out.println("Accrued PTO until next entry date: " + nextEntryDate + " is "
                    + computeAccrualBetweenDates(currentDate, nextEntryDate) + ". New balance: " + balance);

            // Account for max balance
            if (maxBalance > 0) {
                balance = Math.min(balance, maxBalance);
            }

            System.out.println("Accounted for max balance. New balance: " + balance);

            // For each entry on the current date
            for (Entry<?> entry : currentEntries.getValue()) {
                System.out.println("Processing entry: " + entry.getTitle() + ". Is full day: " + entry.isFullDay()
                        + ", Is multi-day: " + entry.isMultiDay() + ", Duration (hours): "
                        + entry.getDuration().toHours());

                // If the entry is multi-day or full-day, deduct 8 hours
                if (entry.isMultiDay() || entry.isFullDay()) {
                    System.out.println("Deducting 8 hours for entry: " + entry.getTitle());

                    balance -= 8;
                }
                // Otherwise, deduct the entry duration capped at 8 hours
                else {
                    System.out.println("Deducting " + Math.min(entry.getDuration().toHours(), 8) + " hours for entry: "
                            + entry.getTitle());

                    balance -= Math.min(entry.getDuration().toHours(), 8);
                }
            }

            // Update the current date
            currentDate = nextEntryDate;

            // Account for max balance
            if (maxBalance > 0) {
                balance = Math.min(balance, maxBalance);
            }

            System.out.println("Accounted for max balance. New balance: " + balance);
            System.out.println("NEXT ENTRIES");
        }

        System.out.println("DONE ENTRIES");

        // If the carry over limit is applied before the target date
        if (nextExpirationDate != null && !nextExpirationDate.isAfter(targetDate)) {
            System.out.println("Applying carry over limit before next entry usage due to expiration date: "
                    + nextExpirationDate);

            // Compute accrual until the next expiration date
            balance += computeAccrualBetweenDates(currentDate, nextExpirationDate);

            System.out.println("Accrued PTO until next expiration date: " + nextExpirationDate + " is "
                    + computeAccrualBetweenDates(currentDate, nextExpirationDate) + ". New balance: " + balance);

            // Account for max balance
            if (maxBalance > 0) {
                balance = Math.min(balance, maxBalance);
            }

            System.out.println("Accounted for max balance. New balance: " + balance);

            // Apply carry over limit
            balance = Math.min(balance, carryOverLimit);

            System.out.println("Applied carry over limit. New balance: " + balance);

            // Update the current date
            currentDate = nextExpirationDate;

            System.out.println("Updated current date to next expiration date: " + currentDate);
        }

        // Compute accrual until the target date
        balance += computeAccrualBetweenDates(currentDate, targetDate);

        System.out.println("Accrued PTO until target date: " + targetDate + " is "
                + computeAccrualBetweenDates(currentDate, targetDate) + ". New balance: " + balance);

        // Account for max balance
        if (maxBalance > 0) {
            balance = Math.min(balance, maxBalance);
        }

        System.out.println("Accounted for max balance. New balance: " + balance);

        System.out.println("Final balance on target date " + targetDate + " is " + balance);

        return balance;
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
