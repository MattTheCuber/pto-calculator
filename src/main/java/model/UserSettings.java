// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package model;

import java.time.LocalDate;
import java.time.MonthDay;

import utilities.AccrualPeriod;

/**
 * Represents user settings for the Paid Time Off Planning Tool.
 */
public class UserSettings {
    double accrualRate;
    AccrualPeriod accrualPeriod;
    // TODO: Add accrual compounding period
    double maxBalance;
    double carryOverLimit;
    MonthDay expirationDate;
    double currentBalance;

    /**
     * Default constructor initializing with default values.
     */
    public UserSettings() {
        this.currentBalance = 0.0;
        this.accrualPeriod = AccrualPeriod.WEEKLY;
    }

    /**
     * Constructor to initialize user settings with specified values.
     * 
     * @param accrualRate    the rate at which PTO is accrued
     * @param accrualPeriod  the period for accruing PTO
     * @param maxBalance     the maximum balance of PTO allowed
     * @param carryOverLimit the limit for carrying over unused PTO
     * @param expirationDate the date when PTO expires
     * @param currentBalance the current balance of PTO
     */
    public UserSettings(
            double accrualRate,
            AccrualPeriod accrualPeriod,
            double maxBalance,
            double carryOverLimit,
            MonthDay expirationDate,
            double currentBalance) {
        this.accrualRate = accrualRate;
        this.accrualPeriod = accrualPeriod;
        this.maxBalance = maxBalance;
        this.carryOverLimit = carryOverLimit;
        this.expirationDate = expirationDate;
        this.currentBalance = currentBalance;
    }

    /**
     * Gets the accrual rate.
     * 
     * @return the accrual rate
     */
    public double getAccrualRate() {
        return accrualRate;
    }

    /**
     * Sets the accrual rate.
     * 
     * @param accrualRate the accrual rate to set
     * @throws IllegalArgumentException if the accrual rate is negative
     */
    public void setAccrualRate(double accrualRate) {
        if (accrualRate < 0) {
            throw new IllegalArgumentException("Accrual rate cannot be negative");
        }

        this.accrualRate = accrualRate;
    }

    /**
     * Gets the accrual period.
     * 
     * @return the accrual period
     */
    public AccrualPeriod getAccrualPeriod() {
        return accrualPeriod;
    }

    /**
     * Sets the accrual period.
     * 
     * @param accrualPeriod the accrual period to set
     */
    public void setAccrualPeriod(AccrualPeriod accrualPeriod) {
        this.accrualPeriod = accrualPeriod;
    }

    /**
     * Gets the maximum balance.
     * 
     * @return the maximum balance
     */
    public double getMaxBalance() {
        return maxBalance;
    }

    /**
     * Sets the maximum balance.
     * 
     * @param maxBalance the maximum balance to set
     * @throws IllegalArgumentException if the maximum balance is negative
     */
    public void setMaxBalance(double maxBalance) {
        if (maxBalance < 0) {
            throw new IllegalArgumentException("Maximum balance cannot be negative");
        }

        this.maxBalance = maxBalance;
    }

    /**
     * Gets the carry over limit.
     * 
     * @return the carry over limit
     */
    public double getCarryOverLimit() {
        return carryOverLimit;
    }

    /**
     * Sets the carry over limit.
     * 
     * @param carryOverLimit the carry over limit to set
     * @throws IllegalArgumentException if the carry over limit is negative
     */
    public void setCarryOverLimit(double carryOverLimit) {
        if (carryOverLimit < 0) {
            throw new IllegalArgumentException("Carry over limit cannot be negative");
        }

        this.carryOverLimit = carryOverLimit;
    }

    /**
     * Gets the expiration date.
     * 
     * @return the expiration date
     */
    public MonthDay getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the expiration date.
     * 
     * @param expirationDate the expiration date to set
     */
    public void setExpirationDate(MonthDay expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Gets the current balance.
     * 
     * @return the current balance
     */
    public double getCurrentBalance() {
        return currentBalance;
    }

    /**
     * Sets the current balance.
     * 
     * @param currentBalance the current balance to set
     * @throws IllegalArgumentException if the current balance is negative
     */
    public void setCurrentBalance(double currentBalance) {
        if (currentBalance < 0) {
            throw new IllegalArgumentException("Current balance cannot be negative");
        }

        this.currentBalance = currentBalance;
    }

    /**
     * Calculates the next expiration date based on the current year.
     * 
     * @return the next expiration date, or null if no expiration date is set
     */
    public LocalDate getNextExpirationDate() {
        LocalDate today = LocalDate.now();
        if (expirationDate == null) {
            return null;
        }

        // Calculate the next expiration date based on the current year
        LocalDate nextExpirationDate = expirationDate.atYear(today.getYear());
        if (nextExpirationDate.isBefore(today)) {
            // If the expiration date has passed, set it to the next year
            nextExpirationDate = nextExpirationDate.plusYears(1);
        }

        return nextExpirationDate;
    }

    /**
     * Returns a string representation of the user settings.
     * 
     * @return a string containing the user settings
     */
    @Override
    public String toString() {
        return "UserSettings(" +
                "currentBalance=" + currentBalance +
                ", accrualRate=" + accrualRate +
                ", accrualPeriod=" + accrualPeriod +
                ", maxBalance=" + maxBalance +
                ", carryOverLimit=" + carryOverLimit +
                ", expirationDate=" + expirationDate +
                ')';
    }
}
