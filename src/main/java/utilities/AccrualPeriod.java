package utilities;

public enum AccrualPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    public static int getDaysInPeriod(AccrualPeriod period) {
        switch (period) {
            case DAILY:
                return 1;
            case WEEKLY:
                return 7;
            case MONTHLY:
                return 30;
            case YEARLY:
                return 365;
            default:
                throw new IllegalArgumentException("Unknown accrual period: " + period);
        }
    }
}