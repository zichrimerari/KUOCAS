package book.javafx.kenyattacatsystem.utils;

/**
 * Utility class to initialize the database with all required tables.
 * This class can be run as a standalone application to set up the database.
 */
public class DatabaseInitializer {
    
    public static void main(String[] args) {
        System.out.println("Starting database initialization...");
        
        try {
            // Initialize the database with all required tables
            DatabaseUtil.initializeDatabase();
            System.out.println("Database initialization completed successfully!");
            
            // Close the database connection
            DatabaseUtil.closeConnection();
            System.out.println("Database connection closed.");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
