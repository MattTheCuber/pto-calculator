// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package utilities;

/**
 * Enum representing different accrual periods for Paid Time Off.
 * Each period defines how often PTO is accrued.
 */
public enum AccrualPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    /**
     * Returns the number of days in the specified accrual period.
     *
     * @param period the accrual period
     * @return the number of days in the period
     */
    public static int getDaysInPeriod(AccrualPeriod period) {
        switch (period) {
            case YEARLY:
                return 365;
            case MONTHLY:
                return 30;
            case WEEKLY:
                return 7;
            default: // DAILY
                return 1;
        }
    }
}
