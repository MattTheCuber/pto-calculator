// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package utilities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AccrualPeriodTest {
    @Test
    public void testGetDaysInPeriod_Daily() {
        assertEquals(1, AccrualPeriod.getDaysInPeriod(AccrualPeriod.DAILY));
    }

    @Test
    public void testGetDaysInPeriod_Weekly() {
        assertEquals(7, AccrualPeriod.getDaysInPeriod(AccrualPeriod.WEEKLY));
    }

    @Test
    public void testGetDaysInPeriod_Monthly() {
        assertEquals(30, AccrualPeriod.getDaysInPeriod(AccrualPeriod.MONTHLY));
    }

    @Test
    public void testGetDaysInPeriod_Yearly() {
        assertEquals(365, AccrualPeriod.getDaysInPeriod(AccrualPeriod.YEARLY));
    }
}