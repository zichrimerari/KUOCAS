package book.javafx.kenyattacatsystem.utils;

import book.javafx.kenyattacatsystem.services.TopicService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to refresh cached data in the application.
 * This is used to ensure the UI reflects the latest data from the database.
 */
public class CacheRefresher {
    private static final Logger LOGGER = Logger.getLogger(CacheRefresher.class.getName());
    
    /**
     * Clears any cached topics data to ensure fresh data is loaded from the database.
     * Call this method after adding new topics to the database.
     */
    public static void refreshTopicsCache() {
        try {
            // Force a reload of topics by calling getAllTopics
            // This will ensure any subsequent calls to get topics will fetch fresh data
            TopicService.getAllTopics();
            LOGGER.log(Level.INFO, "Topics cache refreshed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error refreshing topics cache", e);
        }
    }
}
