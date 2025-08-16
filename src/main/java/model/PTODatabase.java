// Paid Time Off Calculator
// Matthew Vine
// CSIS 643-D01 (Liberty University)

package model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;

import utilities.AccrualPeriod;

/**
 * PTODatabase class for managing the data in the Paid Time Off Planning Tool.
 */
public class PTODatabase {
    private Connection connection;
    private final Path databasePath = Path.of(System.getenv("LOCALAPPDATA"), "PTO Planning Tool", "ptoCalculator.db");
    private int userId;
    private boolean firstTimeUser = false;

    /**
     * Constructor to initialize the database connection and create necessary
     * tables.
     */
    public PTODatabase() {
        // Create the database directory if it does not exist
        try {
            Files.createDirectories(databasePath.getParent());
        } catch (java.io.IOException e) {
            System.out.println("Failed to create database directory: " + e.getMessage());
        }

        // Establish the database connection
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath.toAbsolutePath());
            if (connection != null) {
                System.out.println("Connected to the database.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // Create necessary tables
        createUsersTable();
        createUserSettingsTable();
        createPTOEntriesTable();

        // Get or create the user
        getOrCreateUser();
    }

    /**
     * Creates the users table if it does not exist.
     */
    private void createUsersTable() {
        // SQL statement to create the users table
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL UNIQUE"
                + ");";

        // Execute the SQL statement
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Creates the user settings table if it does not exist.
     */
    private void createUserSettingsTable() {
        // SQL statement to create the user settings table
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

        // Execute the SQL statement
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Creates the PTO entries table if it does not exist.
     */
    private void createPTOEntriesTable() {
        // SQL statement to create the PTO entries table
        String sql = "CREATE TABLE IF NOT EXISTS ptoEntries ("
                + "id TEXT PRIMARY KEY,"
                + "userId INTEGER NOT NULL,"
                + "title TEXT NOT NULL,"
                + "startDate TEXT NOT NULL,"
                + "endDate TEXT NOT NULL,"
                + "fullDay BOOLEAN NOT NULL,"
                + "FOREIGN KEY(userId) REFERENCES users(id)"
                + ");";

        // Execute the SQL statement
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Gets or creates a user in the database based on the system username.
     * 
     * @return the user ID
     */
    private void getOrCreateUser() {
        // Get the system username
        String username = System.getProperty("user.name");

        // Create the user if they do not exist
        String sql = "INSERT OR IGNORE INTO users (name) VALUES (?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();
            firstTimeUser = rowsAffected == 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // Retrieve the user ID
        sql = "SELECT id FROM users WHERE name = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                userId = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Checks if the user is a first-time user.
     * 
     * @return true if the user is a first-time user, false otherwise
     */
    public boolean isFirstTimeUser() {
        return firstTimeUser;
    }

    /**
     * Updates the vacation entries in the database.
     * 
     * @param entries the list of vacation entries to update
     */
    public void updateVacations(List<Entry<?>> entries) {
        // SQL statements to delete and replace vacation entries
        String deleteSql = "DELETE FROM ptoEntries WHERE userId = ?;";
        String insertSql = "INSERT INTO ptoEntries (id, userId, title, startDate, endDate, fullDay) VALUES (?, ?, ?, ?, ?, ?);";

        // Delete existing entries for the user
        try (PreparedStatement deletePstmt = connection.prepareStatement(deleteSql)) {
            deletePstmt.setInt(1, userId);
            deletePstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // Insert new entries for the user
        try (PreparedStatement insertPstmt = connection.prepareStatement(insertSql)) {
            for (Entry<?> entry : entries) {
                insertPstmt.setString(1, entry.getId());
                insertPstmt.setInt(2, userId);
                insertPstmt.setString(3, entry.getTitle());
                insertPstmt.setString(4, entry.getStartAsLocalDateTime().toString());
                insertPstmt.setString(5, entry.getEndAsLocalDateTime().toString());
                insertPstmt.setBoolean(6, entry.isFullDay());
                insertPstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Retrieves the vacation entries.
     * 
     * @return a list of vacation entries
     */
    public List<Entry<?>> getVacations() {
        // SQL statement to select vacation entries
        String sql = "SELECT * FROM ptoEntries WHERE userId = ?;";

        // Initialize a list to hold the vacation entries
        List<Entry<?>> entries = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            // For each result
            while (rs.next()) {
                // Create an Interval and Entry object
                Interval interval = new Interval(
                        LocalDateTime.parse(rs.getString("startDate")),
                        LocalDateTime.parse(rs.getString("endDate")));
                Entry<Object> entry = new Entry<>(rs.getString("title"), interval, rs.getString("id"));
                entry.setFullDay(rs.getBoolean("fullDay"));

                // Add the entry to the list
                entries.add(entry);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return entries;
    }

    /**
     * Updates the user settings in the database.
     * 
     * @param userSettings the user settings to update
     */
    public void updateUserSettings(UserSettings userSettings) {
        // SQL statement to insert or replace user settings
        String sql = "INSERT OR REPLACE INTO userSettings (userId, currentBalance, accrualRate, accrualPeriod, maxBalance, carryOverLimit, expirationDate, lastUpdate) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        // Set the parameters and execute the query
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

    /**
     * Retrieves the user settings from the database.
     * 
     * @param userSettings the user settings object to populate
     * @return the last update
     */
    public LocalDate getUserSettings(UserSettings userSettings) {
        // SQL statement to select the user settings
        String sql = "SELECT * FROM userSettings WHERE userId = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Populate the user settings object with the retrieved data
                String expirationDateStr = rs.getString("expirationDate");
                userSettings.setCurrentBalance(rs.getDouble("currentBalance"));
                userSettings.setAccrualRate(rs.getDouble("accrualRate"));
                userSettings.setAccrualPeriod(AccrualPeriod.values()[rs.getInt("accrualPeriod")]);
                userSettings.setMaxBalance(rs.getDouble("maxBalance"));
                userSettings.setCarryOverLimit(rs.getDouble("carryOverLimit"));
                userSettings.setExpirationDate(expirationDateStr != null ? MonthDay.parse(expirationDateStr) : null);

                // Return the last update
                return LocalDate.parse(rs.getString("lastUpdate"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }
}
