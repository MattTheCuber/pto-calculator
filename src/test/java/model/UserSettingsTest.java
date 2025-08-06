// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package model;

import static org.junit.Assert.assertThrows;

import java.time.LocalDate;
import java.time.MonthDay;

import org.junit.Before;
import org.junit.Test;

import utilities.AccrualPeriod;

public class UserSettingsTest {

    private UserSettings settings;

    @Before
    public void setUp() {
        settings = new UserSettings();
    }

    @Test
    public void testDefaultConstructor() {
        assert settings.getCurrentBalance() == 0.0;
        assert settings.getAccrualPeriod() == AccrualPeriod.WEEKLY;
        assert settings.getAccrualRate() == 0.0;
        assert settings.isMaxBalanceEnabled() == false;
        assert settings.getMaxBalance() == 0.0;
        assert settings.isCarryOverEnabled() == false;
        assert settings.getCarryOverLimit() == 0.0;
        assert settings.getExpirationDate() == null;
    }

    @Test
    public void testParameterizedConstructor() {
        MonthDay expiration = MonthDay.of(1, 1);
        UserSettings settings = new UserSettings(20.0, AccrualPeriod.MONTHLY, 40.0, 10.0, expiration, 5.0);
        assert settings.getAccrualRate() == 20.0;
        assert settings.getAccrualPeriod() == AccrualPeriod.MONTHLY;
        assert settings.isMaxBalanceEnabled() == true;
        assert settings.getMaxBalance() == 40.0;
        assert settings.isCarryOverEnabled() == true;
        assert settings.getCarryOverLimit() == 10.0;
        assert settings.getExpirationDate().equals(expiration);
        assert settings.getCurrentBalance() == 5.0;
    }

    @Test
    public void testSetAndGetAccrualRate() {
        settings.setAccrualRate(3.0);
        assert settings.getAccrualRate() == 3.0;
        assertThrows(IllegalArgumentException.class, () -> {
            settings.setAccrualRate(-1.0);
        });
    }

    @Test
    public void testSetAndGetAccrualPeriod() {
        settings.setAccrualPeriod(AccrualPeriod.MONTHLY);
        assert settings.getAccrualPeriod() == AccrualPeriod.MONTHLY;
    }

    @Test
    public void testSetAndGetMaxBalance() {
        settings.setMaxBalance(50.0);
        assert settings.getMaxBalance() == 50.0;
        assert settings.isMaxBalanceEnabled() == true;
        assertThrows(IllegalArgumentException.class, () -> {
            settings.setMaxBalance(-10.0);
        });
    }

    @Test
    public void testSetAndGetCarryOverLimit() {
        settings.setCarryOverLimit(15.0);
        assert settings.getCarryOverLimit() == 15.0;
        assertThrows(IllegalArgumentException.class, () -> {
            settings.setCarryOverLimit(-5.0);
        });
    }

    @Test
    public void testSetAndGetExpirationDate() {
        MonthDay monthDay = MonthDay.of(6, 30);
        settings.setExpirationDate(monthDay);
        assert settings.getExpirationDate().equals(monthDay);
    }

    @Test
    public void testSetAndGetCurrentBalance() {
        settings.setCurrentBalance(20.0);
        assert settings.getCurrentBalance() == 20.0;
        assertThrows(IllegalArgumentException.class, () -> {
            settings.setCurrentBalance(-2.0);
        });
    }

    @Test
    public void testGetNextExpirationDateNullExpiration() {
        settings.setExpirationDate(null);
        assert settings.getNextExpirationDate(LocalDate.of(2025, 1, 1)) == null;
    }

    @Test
    public void testGetNextExpirationDateBeforeExpiration() {
        MonthDay monthDay = MonthDay.of(12, 31);
        settings.setExpirationDate(monthDay);
        LocalDate start = LocalDate.of(2025, 6, 1);
        LocalDate expected = LocalDate.of(2025, 12, 31);
        assert settings.getNextExpirationDate(start).equals(expected);
    }

    @Test
    public void testGetNextExpirationDateAfterExpiration() {
        MonthDay monthDay = MonthDay.of(1, 1);
        settings.setExpirationDate(monthDay);
        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate expected = LocalDate.of(2025, 1, 1);
        assert settings.getNextExpirationDate(start).equals(expected);
    }

    @Test
    public void testToString() {
        settings.setCurrentBalance(10.0);
        settings.setAccrualRate(2.0);
        settings.setAccrualPeriod(AccrualPeriod.MONTHLY);
        settings.setMaxBalance(40.0);
        settings.setCarryOverLimit(5.0);
        settings.setExpirationDate(MonthDay.of(7, 15));

        String str = settings.toString();

        assert str.contains("currentBalance=10.0");
        assert str.contains("accrualRate=2.0");
        assert str.contains("accrualPeriod=MONTHLY");
        assert str.contains("maxBalance=40.0");
        assert str.contains("carryOverLimit=5.0");
        assert str.contains("expirationDate=--07-15");
    }
}
