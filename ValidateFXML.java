import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;

public class ValidateFXML extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Try to load the FXML file
            File file = new File("src/main/resources/book/javafx/kenyattacatsystem/views/student-dashboard.fxml");
            URL url = file.toURI().toURL();
            System.out.println("Loading FXML from: " + url);
            
            // This will validate the FXML and check controller compatibility
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            
            System.out.println("FXML loaded successfully!");
            System.out.println("Controller: " + loader.getController().getClass().getName());
            
            // We don't actually need to show the UI for validation
            primaryStage.setTitle("FXML Validator");
            primaryStage.setScene(new javafx.scene.Scene(root, 800, 600));
            // primaryStage.show();
            
            // Exit after validation
            System.exit(0);
        } catch (Exception e) {
            System.out.println("FXML validation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
