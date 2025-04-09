package book.javafx.kenyattacatsystem.utils;

import book.javafx.kenyattacatsystem.models.Topic;
import book.javafx.kenyattacatsystem.services.TopicService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to verify that topics are correctly stored in the database
 * and can be retrieved properly.
 */
public class TopicVerifier {
    private static final Logger LOGGER = Logger.getLogger(TopicVerifier.class.getName());
    
    /**
     * Main method to run the verification.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        verifyTopics();
    }
    
    /**
     * Verifies that all topics can be retrieved from the database,
     * with special attention to the Electronics unit (SCO100).
     */
    public static void verifyTopics() {
        try {
            // Get all topics
            List<Topic> allTopics = TopicService.getAllTopics();
            LOGGER.log(Level.INFO, "Total topics in database: " + allTopics.size());
            
            // Get topics for Electronics unit (SCO100)
            List<Topic> electronicsTopics = TopicService.getTopicsByUnit("SCO100");
            LOGGER.log(Level.INFO, "Electronics topics count: " + electronicsTopics.size());
            
            // Print all electronics topics
            LOGGER.log(Level.INFO, "Electronics topics:");
            for (Topic topic : electronicsTopics) {
                LOGGER.log(Level.INFO, "  - " + topic.getTopicName() + " (ID: " + topic.getTopicId() + ")");
            }
            
            // Check for specific topics
            boolean foundAnalogElectronics = false;
            boolean foundPowerElectronics = false;
            boolean foundMicrocontrollers = false;
            boolean foundElectronicMeasurements = false;
            boolean foundCommunicationSystems = false;
            
            for (Topic topic : electronicsTopics) {
                String name = topic.getTopicName().toLowerCase();
                if (name.contains("analog electronics")) foundAnalogElectronics = true;
                if (name.contains("power electronics")) foundPowerElectronics = true;
                if (name.contains("microcontrollers")) foundMicrocontrollers = true;
                if (name.contains("electronic measurements")) foundElectronicMeasurements = true;
                if (name.contains("communication systems")) foundCommunicationSystems = true;
            }
            
            // Report findings
            LOGGER.log(Level.INFO, "Found Analog Electronics: " + foundAnalogElectronics);
            LOGGER.log(Level.INFO, "Found Power Electronics: " + foundPowerElectronics);
            LOGGER.log(Level.INFO, "Found Microcontrollers: " + foundMicrocontrollers);
            LOGGER.log(Level.INFO, "Found Electronic Measurements: " + foundElectronicMeasurements);
            LOGGER.log(Level.INFO, "Found Communication Systems: " + foundCommunicationSystems);
            
            // Overall result
            if (foundAnalogElectronics && foundPowerElectronics && foundMicrocontrollers && 
                foundElectronicMeasurements && foundCommunicationSystems) {
                LOGGER.log(Level.INFO, "SUCCESS: All expected electronics topics were found in the database!");
            } else {
                LOGGER.log(Level.WARNING, "ISSUE: Some expected electronics topics were not found in the database.");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying topics", e);
        }
    }
}
