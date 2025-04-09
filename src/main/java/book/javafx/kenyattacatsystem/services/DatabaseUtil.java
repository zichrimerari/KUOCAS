package book.javafx.kenyattacatsystem.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for database operations.
 */
public class DatabaseUtil {
    private static final Logger LOGGER = Logger.getLogger(DatabaseUtil.class.getName());
    
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/kenyatta_cat_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private static Connection connection;
    
    /**
     * Get a database connection.
     * 
     * @return A database connection
     * @throws SQLException If a connection error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                LOGGER.log(Level.INFO, "Database connection established");
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
                throw new SQLException("MySQL JDBC Driver not found", e);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to establish database connection", e);
                throw e;
            }
        }
        return connection;
    }
    
    /**
     * Prepare a statement with the given SQL.
     * 
     * @param sql The SQL to prepare
     * @return A prepared statement
     * @throws SQLException If an error occurs
     */
    public static PreparedStatement prepareStatement(String sql) throws SQLException {
        return getConnection().prepareStatement(sql);
    }
    
    /**
     * Close the database connection.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.log(Level.INFO, "Database connection closed");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing database connection", e);
            }
        }
    }
    
    /**
     * Test database connection and return connection status.
     * 
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                Logger.getLogger(DatabaseUtil.class.getName()).log(Level.INFO, "Database connection test successful");
                conn.close();
                return true;
            }
            return false;
        } catch (SQLException e) {
            Logger.getLogger(DatabaseUtil.class.getName()).log(Level.SEVERE, "Database connection test failed", e);
            return false;
        }
    }
}
