package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;

import utilities.AccrualPeriod;

public class PTODatabase {
    private Connection connection;
    private final String url = "jdbc:sqlite:ptoCalculator.db";

    private int userId;

    public PTODatabase() {
        try {
            connection = DriverManager.getConnection(url);
            if (connection != null) {
                System.out.println("Connected to the database.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        createUsersTable();
        createUserSettingsTable();
        createPTOEntriesTable();
        userId = getOrCreateUser();
    }

    private void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createUserSettingsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS userSettings ("
                + "userId INTEGER PRIMARY KEY NOT NULL,"
                + "currentBalance REAL NOT NULL,"
                + "accrualRate REAL NOT NULL,"
                + "accrualPeriod INTEGER NOT NULL,"
                + "maxBalance REAL,"
                + "carryOverLimit REAL,"
                + "expirationDate TEXT,"
                + "lastUpdate TEXT NOT NULL,"
                + "FOREIGN KEY(userId) REFERENCES users(id)"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createPTOEntriesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS ptoEntries ("
                + "id TEXT PRIMARY KEY,"
                + "userId INTEGER NOT NULL,"
                + "title TEXT NOT NULL,"
                + "startDate TEXT NOT NULL,"
                + "endDate TEXT NOT NULL,"
                + "fullDay BOOLEAN NOT NULL,"
                + "FOREIGN KEY(userId) REFERENCES users(id)"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private Integer getOrCreateUser() {
        String username = System.getProperty("user.name");
        String sql = "INSERT OR IGNORE INTO users (name) VALUES (?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "SELECT id FROM users WHERE name = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public void updateVacations(List<Entry<?>> entries) {
        String sql = "INSERT OR REPLACE INTO ptoEntries (id, userId, title, startDate, endDate, fullDay) VALUES (?, ?, ?, ?, ?, ?);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Entry<?> entry : entries) {
                pstmt.setString(1, entry.getId());
                pstmt.setInt(2, userId);
                pstmt.setString(3, entry.getTitle());
                pstmt.setString(4, entry.getStartAsLocalDateTime().toString());
                pstmt.setString(5, entry.getEndAsLocalDateTime().toString());
                pstmt.setBoolean(6, entry.isFullDay());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Entry<?>> getVacations() {
        String sql = "SELECT * FROM ptoEntries WHERE userId = ?;";
        List<Entry<?>> entries = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Interval interval = new Interval(
                        LocalDateTime.parse(rs.getString("startDate")),
                        LocalDateTime.parse(rs.getString("endDate")));
                Entry<Object> entry = new Entry<>(rs.getString("title"), interval, rs.getString("id"));
                entry.setFullDay(rs.getBoolean("fullDay"));
                entries.add(entry);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return entries;
    }

    public void updateUserSettings(UserSettings userSettings) {
        String sql = "INSERT OR REPLACE INTO userSettings (userId, currentBalance, accrualRate, accrualPeriod, maxBalance, carryOverLimit, expirationDate, lastUpdate) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDouble(2, userSettings.getCurrentBalance());
            pstmt.setDouble(3, userSettings.getAccrualRate());
            pstmt.setInt(4, userSettings.getAccrualPeriod().ordinal());
            pstmt.setDouble(5, userSettings.getMaxBalance());
            pstmt.setDouble(6, userSettings.getCarryOverLimit());
            String expirationDate = userSettings.getExpirationDate() != null
                    ? userSettings.getExpirationDate().toString()
                    : null;
            pstmt.setString(7, expirationDate);
            pstmt.setString(8, LocalDate.now().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public UserSettings getUserSettings() {
        String sql = "SELECT * FROM userSettings WHERE userId = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Retrieved user settings: " + rs.getDouble("maxBalance"));
                String expirationDateStr = rs.getString("expirationDate");
                // TODO: Handle last update
                return new UserSettings(
                        rs.getDouble("accrualRate"),
                        AccrualPeriod.values()[rs.getInt("accrualPeriod")],
                        rs.getDouble("maxBalance"),
                        rs.getDouble("carryOverLimit"),
                        expirationDateStr != null ? LocalDate.parse(expirationDateStr) : null,
                        rs.getDouble("currentBalance"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return new UserSettings();
    }
}
