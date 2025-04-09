package book.javafx.kenyattacatsystem.utils;

import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to validate FXML files by attempting to load them.
 */
public class FXMLValidator {
    private static final Logger LOGGER = Logger.getLogger(FXMLValidator.class.getName());

    /**
     * Validates an FXML file by attempting to load it.
     * 
     * @param fxmlPath The path to the FXML file to validate
     * @return true if the FXML file is valid, false otherwise
     */
    public static boolean validateFXML(String fxmlPath) {
        try {
            URL fxmlUrl = FXMLValidator.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                LOGGER.log(Level.SEVERE, "Could not find FXML file: " + fxmlPath);
                return false;
            }
            
            LOGGER.log(Level.INFO, "Validating FXML file: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.load();
            LOGGER.log(Level.INFO, "FXML file is valid: " + fxmlPath);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error validating FXML file: " + fxmlPath, e);
            return false;
        }
    }
}
