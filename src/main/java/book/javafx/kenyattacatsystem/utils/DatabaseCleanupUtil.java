package book.javafx.kenyattacatsystem.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for cleaning up database issues
 */
public class DatabaseCleanupUtil {
    private static final Logger LOGGER = Logger.getLogger(DatabaseCleanupUtil.class.getName());
    
    /**
     * Executes an SQL script file
     * 
     * @param scriptPath The path to the SQL script file
     * @return True if the script was executed successfully, false otherwise
     */
    public static boolean executeScript(String scriptPath) {
        LOGGER.log(Level.INFO, "Executing SQL script: {0}", scriptPath);
        
        StringBuilder scriptContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(scriptPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                    continue;
                }
                scriptContent.append(line).append("\n");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading SQL script: {0}", e.getMessage());
            return false;
        }
        
        // Split the script into individual statements
        String[] statements = scriptContent.toString().split(";");
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try (Statement stmt = conn.createStatement()) {
                for (String statement : statements) {
                    if (statement.trim().isEmpty()) {
                        continue;
                    }
                    
                    LOGGER.log(Level.INFO, "Executing statement: {0}", statement);
                    stmt.execute(statement);
                }
                
                conn.commit();
                LOGGER.log(Level.INFO, "SQL script executed successfully");
                return true;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error executing SQL statement: {0}", e.getMessage());
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting database connection: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Main method for running the cleanup utility
     */
    public static void main(String[] args) {
        try {
            // Initialize the database
            DatabaseUtil.initializeDatabase();
            System.out.println("Database initialized successfully");
            
            // Execute the cleanup script
            String scriptPath = "cleanup_duplicate_practice_tests.sql";
            boolean success = executeScript(scriptPath);
            
            if (success) {
                System.out.println("Database cleanup completed successfully");
            } else {
                System.err.println("Database cleanup failed");
            }
            
        } catch (Exception e) {
            System.err.println("Error during database cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
