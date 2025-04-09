package book.javafx.kenyattacatsystem.services;

import book.javafx.kenyattacatsystem.models.Topic;
import book.javafx.kenyattacatsystem.models.Unit;
import book.javafx.kenyattacatsystem.utils.DatabaseUtil;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for topic-related operations.
 * Handles topic creation, retrieval, and management.
 */
public class TopicService {
    private static final Logger LOGGER = Logger.getLogger(TopicService.class.getName());

    /**
     * Creates a new topic in the database.
     *
     * @param topic The topic to create
     * @return True if the topic was created successfully, false otherwise
     */
    public static boolean createTopic(Topic topic) {
        // Generate ID if not provided
        if (topic.getTopicId() == null || topic.getTopicId().isEmpty()) {
            topic.setTopicId(UUID.randomUUID().toString());
        }
        
        String query = "INSERT INTO topics (topic_id, topic_name, description, unit_code, created_by, creation_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, topic.getTopicId());
            stmt.setString(2, topic.getTopicName());
            stmt.setString(3, topic.getDescription());
            stmt.setString(4, topic.getUnitCode());
            stmt.setString(5, topic.getCreatedBy());
            stmt.setTimestamp(6, Timestamp.valueOf(topic.getCreationDate() != null ? 
                    topic.getCreationDate() : LocalDateTime.now()));

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating topic", e);
            return false;
        }
    }

    /**
     * Updates an existing topic in the database.
     *
     * @param topic The topic to update
     * @return True if the topic was updated successfully, false otherwise
     */
    public static boolean updateTopic(Topic topic) {
        String query = "UPDATE topics SET topic_name = ?, description = ? WHERE topic_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, topic.getTopicName());
            stmt.setString(2, topic.getDescription());
            stmt.setString(3, topic.getTopicId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating topic", e);
            return false;
        }
    }

    /**
     * Deletes a topic from the database.
     *
     * @param topicId The topic ID
     * @return True if the topic was deleted successfully, false otherwise
     */
    public static boolean deleteTopic(String topicId) {
        String query = "DELETE FROM topics WHERE topic_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, topicId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting topic", e);
            return false;
        }
    }

    /**
     * Gets a topic by its ID.
     *
     * @param topicId The topic ID
     * @return The topic, or null if not found
     */
    public static Topic getTopicById(String topicId) {
        String query = "SELECT t.*, u.unit_name FROM topics t "
                + "JOIN units u ON t.unit_code = u.unit_code "
                + "WHERE t.topic_id = ?";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, topicId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToTopic(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting topic by ID", e);
        }

        return null;
    }

    /**
     * Gets all topics.
     *
     * @return A list of all topics
     */
    public static List<Topic> getAllTopics() {
        List<Topic> topics = new ArrayList<>();
        String query = "SELECT t.*, u.unit_name FROM topics t "
                + "JOIN units u ON t.unit_code = u.unit_code "
                + "ORDER BY t.topic_name";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                topics.add(mapResultSetToTopic(rs));
            }
            LOGGER.log(Level.INFO, "Retrieved " + topics.size() + " topics from database");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all topics", e);
        }

        return topics;
    }

    /**
     * Gets topics by unit code.
     *
     * @param unitCode The unit code
     * @return A list of topics for the unit
     */
    public static List<Topic> getTopicsByUnit(String unitCode) {
        List<Topic> topics = new ArrayList<>();
        String query = "SELECT t.*, u.unit_name FROM topics t "
                + "JOIN units u ON t.unit_code = u.unit_code "
                + "WHERE t.unit_code = ? "
                + "ORDER BY t.topic_name";

        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            stmt.setString(1, unitCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                topics.add(mapResultSetToTopic(rs));
            }
            LOGGER.log(Level.INFO, "Retrieved " + topics.size() + " topics for unit " + unitCode);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting topics by unit", e);
        }

        return topics;
    }

    /**
     * Maps a ResultSet to a Topic object.
     *
     * @param rs The ResultSet
     * @return The Topic object
     * @throws SQLException If a database access error occurs
     */
    private static Topic mapResultSetToTopic(ResultSet rs) throws SQLException {
        Topic topic = new Topic();
        topic.setTopicId(rs.getString("topic_id"));
        topic.setTopicName(rs.getString("topic_name"));
        topic.setDescription(rs.getString("description"));
        topic.setUnitCode(rs.getString("unit_code"));
        topic.setUnitName(rs.getString("unit_name"));
        topic.setCreatedBy(rs.getString("created_by"));
        
        Timestamp creationDate = rs.getTimestamp("creation_date");
        if (creationDate != null) {
            topic.setCreationDate(creationDate.toLocalDateTime());
        }
        
        return topic;
    }

    /**
     * Shows a dialog to create or edit a topic.
     *
     * @param existingTopic The topic to edit, or null to create a new topic
     * @param unitCode      The unit code (required for new topics)
     * @param userId        The user ID of the creator (required for new topics)
     * @return The created or updated topic, or null if canceled
     */
    public static Topic showTopicDialog(Topic existingTopic, String unitCode, String userId) {
        boolean isEditing = existingTopic != null;
        
        // Create dialog
        Dialog<Topic> dialog = new Dialog<>();
        dialog.setTitle(isEditing ? "Edit Topic" : "Create New Topic");
        dialog.setHeaderText(isEditing ? "Edit Topic Details" : "Create a New Topic");
        
        // Create the grid pane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Create form fields
        final TextField topicNameField = new TextField();
        final TextArea descriptionField = new TextArea();
        descriptionField.setPrefRowCount(3);
        
        final ComboBox<Unit> unitComboBox;
        
        if (isEditing) {
            // For editing, show the topic ID (disabled)
            topicNameField.setText(existingTopic.getTopicName());
            descriptionField.setText(existingTopic.getDescription());
            unitComboBox = null; // Unit cannot be changed when editing
        } else {
            // For creating, generate a new topic ID
            topicNameField.setPromptText("Topic Name");
            descriptionField.setPromptText("Description");
            
            // For creating, show unit selection if not provided
            if (unitCode == null || unitCode.isEmpty()) {
                unitComboBox = new ComboBox<>();
                unitComboBox.setPromptText("Select Unit");
                
                // Load units
                try {
                    List<Unit> units = UnitService.getAllUnits();
                    unitComboBox.getItems().addAll(units);
                    
                    // Set converter for display
                    unitComboBox.setConverter(new StringConverter<Unit>() {
                        @Override
                        public String toString(Unit unit) {
                            return unit != null ? unit.getUnitCode() + " - " + unit.getUnitName() : "";
                        }
                        
                        @Override
                        public Unit fromString(String string) {
                            return null; // Not needed for this use case
                        }
                    });
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error loading units", e);
                }
            } else {
                unitComboBox = null;
            }
        }
        
        // Add fields to grid
        grid.add(new Label("Topic Name:"), 0, 0);
        grid.add(topicNameField, 1, 0);
        
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        
        if (unitComboBox != null) {
            grid.add(new Label("Unit:"), 0, 2);
            grid.add(unitComboBox, 1, 2);
        }
        
        dialog.getDialogPane().setContent(grid);
        
        // Add buttons
        final ButtonType saveButtonType = new ButtonType(isEditing ? "Save" : "Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Enable/Disable save button depending on whether fields are filled
        final Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        // Validation listener
        final Runnable validateInput = () -> {
            boolean isValid = !topicNameField.getText().trim().isEmpty();
            if (unitComboBox != null) {
                isValid = isValid && unitComboBox.getValue() != null;
            }
            saveButton.setDisable(!isValid);
        };
        
        topicNameField.textProperty().addListener((observable, oldValue, newValue) -> validateInput.run());
        if (unitComboBox != null) {
            unitComboBox.valueProperty().addListener((observable, oldValue, newValue) -> validateInput.run());
        }
        
        // Set initial validation state
        validateInput.run();
        
        // Convert result to topic
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                final Topic topic = isEditing ? existingTopic : new Topic();
                
                // Set topic properties
                if (!isEditing) {
                    // Generate a new topic ID for new topics
                    final String topicId = UUID.randomUUID().toString();
                    topic.setTopicId(topicId);
                }
                
                topic.setTopicName(topicNameField.getText().trim());
                topic.setDescription(descriptionField.getText().trim());
                
                // Set unit code
                if (!isEditing) {
                    if (unitCode != null && !unitCode.isEmpty()) {
                        topic.setUnitCode(unitCode);
                    } else if (unitComboBox != null && unitComboBox.getValue() != null) {
                        topic.setUnitCode(unitComboBox.getValue().getUnitCode());
                    }
                }
                
                // Set creator and creation date
                if (!isEditing) {
                    topic.setCreatedBy(userId);
                    topic.setCreationDate(LocalDateTime.now());
                }
                
                return topic;
            }
            return null;
        });
        
        // Show dialog and return result
        return dialog.showAndWait().orElse(null);
    }

    /**
     * Debug method to directly check if the newly added electronics topics exist in the database.
     * This bypasses any caching mechanisms and directly queries the database.
     */
    public static void checkElectronicsTopics() {
        String query = "SELECT topic_id, topic_name FROM topics WHERE unit_code = 'SCO100'";
        
        try (PreparedStatement stmt = DatabaseUtil.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            
            LOGGER.log(Level.INFO, "======= ELECTRONICS TOPICS CHECK =======");
            int count = 0;
            while (rs.next()) {
                String topicId = rs.getString("topic_id");
                String topicName = rs.getString("topic_name");
                LOGGER.log(Level.INFO, "Topic found: " + topicName + " (ID: " + topicId + ")");
                count++;
            }
            LOGGER.log(Level.INFO, "Total Electronics Topics found: " + count);
            LOGGER.log(Level.INFO, "======================================");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking electronics topics", e);
        }
    }
}
