package book.javafx.kenyattacatsystem.utils;

import javafx.application.Application;
import javafx.stage.Stage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test class to validate FXML files
 */
public class FXMLValidatorTest extends Application {
    private static final Logger LOGGER = Logger.getLogger(FXMLValidatorTest.class.getName());

    @Override
    public void start(Stage primaryStage) {
        // Test the student dashboard FXML
        boolean isValid = FXMLValidator.validateFXML("/book/javafx/kenyattacatsystem/views/student-dashboard.fxml");
        LOGGER.log(Level.INFO, "Student dashboard FXML is valid: " + isValid);
        
        // Test the login view FXML for comparison
        boolean loginValid = FXMLValidator.validateFXML("/book/javafx/kenyattacatsystem/views/login-view.fxml");
        LOGGER.log(Level.INFO, "Login view FXML is valid: " + loginValid);
        
        // Exit after validation
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
