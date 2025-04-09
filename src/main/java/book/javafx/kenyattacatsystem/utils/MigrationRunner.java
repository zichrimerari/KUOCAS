package book.javafx.kenyattacatsystem.utils;

/**
 * Utility class to run database migrations manually.
 * This is useful for one-time operations or testing.
 */
public class MigrationRunner {
    public static void main(String[] args) {
        // Initialize the database
        try {
            DatabaseUtil.initializeDatabase();
            System.out.println("Database initialized successfully");
            
            // Run the practice assessment migration
            int migratedCount = PracticeAssessmentMigrator.migratePracticeAssessments();
            System.out.println("Migrated " + migratedCount + " practice assessments");
            
        } catch (Exception e) {
            System.err.println("Error during migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
