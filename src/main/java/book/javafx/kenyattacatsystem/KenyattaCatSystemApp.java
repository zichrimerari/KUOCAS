package book.javafx.kenyattacatsystem;

import book.javafx.kenyattacatsystem.services.AssessmentService;
import book.javafx.kenyattacatsystem.services.ReportService;
import book.javafx.kenyattacatsystem.utils.PracticeAssessmentMigrator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for the Kenyatta University Continuous Assessment System.
 * This class initializes the JavaFX application and loads the main login view.
 */
public class KenyattaCatSystemApp extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize the database
        try {
            book.javafx.kenyattacatsystem.utils.DatabaseUtil.initializeDatabase();
            System.out.println("Database initialized successfully");
            
            // Create test users for development
            book.javafx.kenyattacatsystem.utils.DatabaseUtil.createTestUsers();
            System.out.println("Test users created or verified");
            
            // Initialize default reports
            ReportService.createDefaultReports();
            System.out.println("Default reports initialized");
            
            // Initialize assessment status scheduler
            AssessmentService.initializeAssessmentStatusScheduler();
            System.out.println("Assessment status scheduler initialized");
            
            // Migrate practice assessments to the dedicated table
            int migratedCount = PracticeAssessmentMigrator.migratePracticeAssessments();
            System.out.println("Migrated " + migratedCount + " practice assessments");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
        
        FXMLLoader fxmlLoader = new FXMLLoader(KenyattaCatSystemApp.class.getResource("views/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        scene.getStylesheets().add(KenyattaCatSystemApp.class.getResource("styles/main.css").toExternalForm());
        
        stage.setTitle("Kenyatta University CAT System");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        // Prevent window from being resized too small
        stage.setOnCloseRequest(event -> {
            // Handle any cleanup operations here
            System.out.println("Application closing");
        });
        
        stage.show();
    }

    public static void main(String[] args) {
        // Register shutdown hook to close database connection when application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Closing database connection...");
            book.javafx.kenyattacatsystem.utils.DatabaseUtil.closeConnection();
        }));
        
        launch();
    }
}