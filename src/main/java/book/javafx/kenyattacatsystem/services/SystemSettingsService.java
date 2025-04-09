package book.javafx.kenyattacatsystem.services;

import book.javafx.kenyattacatsystem.utils.DatabaseUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for system settings operations.
 * Handles system-wide settings including enrollment limits and fee clearance requirements.
 */
public class SystemSettingsService {
    private static final Logger LOGGER = Logger.getLogger(SystemSettingsService.class.getName());
    
    // Constants for setting keys
    public static final String SYSTEM_NAME = "system_name";
    public static final String ACADEMIC_YEAR = "academic_year";
    public static final String SESSION_TIMEOUT = "session_timeout";
    public static final String MIN_PASSWORD_LENGTH = "min_password_length";
    public static final String PASSWORD_COMPLEXITY = "password_complexity";
    public static final String ACCOUNT_LOCKOUT_THRESHOLD = "account_lockout_threshold";
    public static final String MAX_UNITS_PER_SEMESTER = "max_units_per_semester";
    public static final String FEE_CLEARANCE_REQUIRED = "fee_clearance_required";
    
    /**
     * Initializes the system settings table if it doesn't exist.
     * This method should be called during application startup.
     */
    public static void initializeSystemSettings() {
        try {
            // Create the system_settings table if it doesn't exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS system_settings ("
                    + "setting_key VARCHAR(50) PRIMARY KEY,"
                    + "setting_value VARCHAR(255) NOT NULL,"
                    + "description TEXT,"
                    + "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            
            DatabaseUtil.executeUpdate(createTableSQL);
            
            // Initialize default settings if they don't exist
            initializeDefaultSetting(SYSTEM_NAME, "Kenyatta CAT System", "System name displayed in the UI");
            initializeDefaultSetting(ACADEMIC_YEAR, "2023-2024", "Current academic year");
            initializeDefaultSetting(SESSION_TIMEOUT, "30", "Session timeout in minutes");
            initializeDefaultSetting(MIN_PASSWORD_LENGTH, "8", "Minimum password length");
            initializeDefaultSetting(PASSWORD_COMPLEXITY, "Medium", "Password complexity level (Low, Medium, High)");
            initializeDefaultSetting(ACCOUNT_LOCKOUT_THRESHOLD, "5", "Number of failed login attempts before account lockout");
            initializeDefaultSetting(MAX_UNITS_PER_SEMESTER, "6", "Maximum number of units a student can enroll in per semester");
            initializeDefaultSetting(FEE_CLEARANCE_REQUIRED, "true", "Whether fee clearance is required for unit enrollment");
            
            LOGGER.info("System settings initialized successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing system settings", e);
        }
    }
    
    /**
     * Initializes a default setting if it doesn't exist.
     *
     * @param key         The setting key
     * @param value       The default value
     * @param description The setting description
     * @throws SQLException If a database access error occurs
     */
    private static void initializeDefaultSetting(String key, String value, String description) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM system_settings WHERE setting_key = ?";
        try (PreparedStatement checkStmt = DatabaseUtil.prepareStatement(checkQuery)) {
            checkStmt.setString(1, key);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) == 0) {
                String insertQuery = "INSERT INTO system_settings (setting_key, setting_value, description) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = DatabaseUtil.prepareStatement(insertQuery)) {
                    insertStmt.setString(1, key);
                    insertStmt.setString(2, value);
                    insertStmt.setString(3, description);
                    insertStmt.executeUpdate();
                }
            }
        }
    }
    
    /**
     * Gets all system settings as a map.
     *
     * @return A map of setting keys to values
     */
    public static Map<String, String> getAllSettings() {
        Map<String, String> settings = new HashMap<>();
        
        try {
            String query = "SELECT setting_key, setting_value FROM system_settings";
            ResultSet rs = DatabaseUtil.executeQuery(query);
            
            while (rs.next()) {
                settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting system settings", e);
        }
        
        return settings;
    }
    
    /**
     * Gets a specific system setting value.
     *
     * @param key The setting key
     * @return The setting value, or null if not found
     */
    public static String getSetting(String key) {
        try {
            String query = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
                stmt.setString(1, key);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting system setting: " + key, e);
        }
        
        return null;
    }
    
    /**
     * Updates a system setting.
     *
     * @param key   The setting key
     * @param value The new value
     * @return True if the update was successful, false otherwise
     */
    public static boolean updateSetting(String key, String value) {
        try {
            String query = "UPDATE system_settings SET setting_value = ?, last_updated = CURRENT_TIMESTAMP WHERE setting_key = ?";
            try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
                stmt.setString(1, value);
                stmt.setString(2, key);
                
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating system setting: " + key, e);
            return false;
        }
    }
    
    /**
     * Gets the maximum number of units a student can enroll in per semester.
     *
     * @return The maximum number of units, or 6 as default if not set
     */
    public static int getMaxUnitsPerSemester() {
        String value = getSetting(MAX_UNITS_PER_SEMESTER);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid max units per semester value: " + value, e);
            }
        }
        return 6; // Default value
    }
    
    /**
     * Checks if fee clearance is required for enrollment.
     *
     * @return True if fee clearance is required, false otherwise
     */
    public static boolean isFeeClearanceRequired() {
        String value = getSetting(FEE_CLEARANCE_REQUIRED);
        return value != null && value.equalsIgnoreCase("true");
    }
}