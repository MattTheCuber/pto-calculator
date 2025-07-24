package utilities;

import java.time.LocalDate;
import java.util.List;

import com.calendarfx.model.Entry;

import model.UserSettings;

public class PTOCalculator {
    UserSettings userSettings;

    public PTOCalculator(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public double computeBalanceAtDate(LocalDate date, List<Entry<Object>> entries) {
        return 0.0;
    }

    public boolean validateEntry(Entry<Object> entry) {
        return true;
    }
}
