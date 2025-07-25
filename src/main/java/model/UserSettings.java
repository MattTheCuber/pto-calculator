package model;

import java.time.LocalDate;

import utilities.AccrualPeriod;

public class UserSettings {
    double accrualRate;
    AccrualPeriod accrualPeriod;
    // Add acrrual compounding period
    double maxBalance;
    double carryOverLimit;
    LocalDate expirationDate;
    double currentBalance;

    public UserSettings() {
        this.currentBalance = 0.0;
        this.accrualPeriod = AccrualPeriod.WEEKLY;
    }

    public UserSettings(double accrualRate, AccrualPeriod accrualPeriod, double maxBalance, double carryOverLimit,
            LocalDate expirationDate, double currentBalance) {
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
     */
    public void setMaxBalance(double maxBalance) {
        if (maxBalance <= 0) {
            throw new IllegalArgumentException("Maximum balance must be greater than zero");
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
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the expiration date.
     * 
     * @param expirationDate the expiration date to set
     */
    public void setExpirationDate(LocalDate expirationDate) {
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
     */
    public void setCurrentBalance(double currentBalance) {
        if (currentBalance < 0) {
            throw new IllegalArgumentException("Current balance cannot be negative");
        }

        this.currentBalance = currentBalance;
    }
}
